package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus.PAID

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    private fun chargeSubscriptionFrom(invoice: Invoice) =
        paymentProvider.charge(invoice)
            .let {
                if (it) {
                    invoiceService.updateStatus(invoice.id, PAID)
                }
            }

    fun chargeSubscriptions() {
        invoiceService.fetchPendingInvoices()
            .forEach { chargeSubscriptionFrom(it) }
    }
}
