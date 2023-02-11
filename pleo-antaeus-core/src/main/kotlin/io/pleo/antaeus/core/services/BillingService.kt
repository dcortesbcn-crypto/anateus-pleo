package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceStatus.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    private fun getStatusFromCharge(hasSucceed: Boolean): InvoiceStatus = if (hasSucceed) {
        PAID
    } else {
        NO_BALANCE
    }

    private fun chargeSubscriptionFrom(invoice: Invoice) = try {
        paymentProvider.charge(invoice)
            .let {
                invoiceService.updateStatus(invoice.id, getStatusFromCharge(it))
            }
    } catch (e: CustomerNotFoundException) {
        invoiceService.updateStatus(invoice.id, CUSTOMER_NOT_ON_PROVIDER)
    } catch (e: CurrencyMismatchException) {
        invoiceService.updateStatus(invoice.id, CURRENCY_MISMATCH)
    } catch (e: NetworkException) {
        logger.error { "Unable to connect to provider for invoice ${invoice.id}" }
    }

    fun chargeSubscriptions() {
        invoiceService.fetchPendingInvoices()
            .forEach { chargeSubscriptionFrom(it) }
    }
}
