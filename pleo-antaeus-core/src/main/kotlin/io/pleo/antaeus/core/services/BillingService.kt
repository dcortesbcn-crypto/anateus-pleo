package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus.PAID

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    private fun chargeInvoice(invoice: Invoice) {
        val hasSucceed = paymentProvider.charge(invoice)

        if (hasSucceed) {
            invoiceService.updateStatus(invoice.id, PAID)
        }
    }

    fun chargeSubscription() {
        invoiceService.fetchPendingInvoices()
            .forEach { chargeInvoice(it) }
    }
}
