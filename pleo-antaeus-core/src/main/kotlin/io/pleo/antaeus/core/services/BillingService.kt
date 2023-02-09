package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    fun chargeSubscription() {
        invoiceService.fetchPendingInvoices()
            .forEach { TODO() }
    }
}
