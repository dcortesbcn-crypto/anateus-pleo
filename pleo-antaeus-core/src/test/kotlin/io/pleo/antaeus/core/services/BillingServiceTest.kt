package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import org.junit.jupiter.api.Test

internal class BillingServiceTest{

    private val paymentProvider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService>()

    private val service = BillingService(paymentProvider, invoiceService)

    @Test
    fun `given no pending invoices no billing is requested`() {
        // Given
        every { invoiceService.fetchPendingInvoices() } returns emptyList()

        // When
        service.chargeSubscription()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 0) { paymentProvider.charge(any()) }

    }

}