package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.events.*
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.ports.InvoiceEventSender
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceStatus.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val invoiceEventsSender: InvoiceEventSender
) {

    private fun getStatusFromCharge(hasSucceed: Boolean): InvoiceStatus = if (hasSucceed) {
        PAID
    } else {
        NO_BALANCE
    }

    private fun getEventFromCharge(hasSucceed: Boolean, customerId: Int): InvoiceEvent = if (hasSucceed) {
        InvoicePayed(customerId)
    } else {
        NoBalanceToPay(customerId)
    }

    private fun chargeSubscriptionFrom(invoice: Invoice) = try {
        invoiceService.updateStatus(invoice.id, PROCESSING)
        paymentProvider.charge(invoice)
            .let {
                invoiceService.updateStatus(invoice.id, getStatusFromCharge(it))
                invoiceEventsSender.send(getEventFromCharge(it, invoice.customerId))
            }
    } catch (e: CustomerNotFoundException) {
        invoiceService.updateStatus(invoice.id, CUSTOMER_NOT_ON_PROVIDER)
        invoiceEventsSender.send(CustomerNotFoundOnPaymentProvider(invoice.customerId))
    } catch (e: CurrencyMismatchException) {
        invoiceService.updateStatus(invoice.id, CURRENCY_MISMATCH)
        invoiceEventsSender.send(CurrencyMismatch(invoice.customerId))
    } catch (e: NetworkException) {
        logger.error { "Unable to connect to provider for invoice ${invoice.id}" }
    } catch (e: Exception) {
        logger.error { "Unknown exception for ${invoice.id}" }
    }

    fun chargeSubscriptions() {
        invoiceService.fetchPendingInvoices()
            .forEach { chargeSubscriptionFrom(it) }
    }
}
