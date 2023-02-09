package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Currency.EUR
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceStatus.PENDING
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BillingServiceTest {

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

    @Test
    fun `given pending invoices a charge to a provider is requested`() {
        // Given
        val invoice = Invoice(12, 15, Money(BigDecimal(345.2), EUR), PENDING)
        every { invoiceService.fetchPendingInvoices() } returns listOf(invoice)
        every { paymentProvider.charge(invoice) } returns true

        // When
        service.chargeSubscription()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 1) { paymentProvider.charge(invoice) }
    }

}