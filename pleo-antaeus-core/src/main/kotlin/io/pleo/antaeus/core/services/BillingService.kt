package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.events.*
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.ports.InvoiceEventSender
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceStatus.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("Billing Service log")

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val invoiceEventsSender: InvoiceEventSender
) {
    fun chargePendingSubscriptions() {
        invoiceService.fetchPendingInvoices()
            .forEach { chargeSubscriptionFrom(it) }
    }

    private fun chargeSubscriptionFrom(invoice: Invoice) = try {
        invoiceService.updateStatus(invoice.id, PROCESSING)
        val hasSucceed = paymentProvider.charge(invoice)
        updateStatus(invoice.id, invoice.customerId, getStatusFromCharge(hasSucceed))
    } catch (e: Exception) {
        dealWithError(e, invoice)
    }

    private fun getStatusFromCharge(hasSucceed: Boolean): InvoiceStatus = when {
        hasSucceed -> PAID
        else -> NO_BALANCE
    }

    private fun updateStatus(invoiceId: Int, customerId: Int, status: InvoiceStatus) {
        invoiceService.updateStatus(invoiceId, status)
        invoiceEventsSender.send(statusToEvent(status, customerId))
    }

    private fun statusToEvent(status: InvoiceStatus, customerId: Int): InvoiceEvent = when (status) {
        PAID -> InvoicePayed(customerId)
        CUSTOMER_NOT_ON_PROVIDER -> CustomerNotFoundOnPaymentProvider(customerId)
        CURRENCY_MISMATCH -> CurrencyMismatch(customerId)
        NO_BALANCE -> NoBalanceToPay(customerId)
        else -> throw IllegalStateException()
    }

    private fun dealWithError(exception: Exception, invoice: Invoice): Unit = when (exception) {
        is CustomerNotFoundException -> updateStatus(invoice.id, invoice.customerId, CUSTOMER_NOT_ON_PROVIDER)
        is CurrencyMismatchException -> updateStatus(invoice.id, invoice.customerId, CURRENCY_MISMATCH)
        is NetworkException -> logger.error { "Unable to connect to provider for invoice ${invoice.id}" }
        else -> logger.error { "Unknown exception for ${invoice.id}" }
    }
}
