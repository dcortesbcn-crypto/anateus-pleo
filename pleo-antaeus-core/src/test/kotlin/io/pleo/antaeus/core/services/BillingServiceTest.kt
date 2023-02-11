package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.events.CurrencyMismatch
import io.pleo.antaeus.core.events.CustomerNotFoundOnPaymentProvider
import io.pleo.antaeus.core.events.InvoicePayed
import io.pleo.antaeus.core.events.NoBalanceToPay
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.ports.InvoiceEventSender
import io.pleo.antaeus.models.Currency.EUR
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus.*
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BillingServiceTest {

    private val paymentProvider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService>()
    private val invoiceEventSender = mockk<InvoiceEventSender>()

    private val service = BillingService(paymentProvider, invoiceService, invoiceEventSender)

    @Test
    fun `given no pending invoices no billing is requested`() {
        // Given
        every { invoiceService.fetchPendingInvoices() } returns emptyList()

        // When
        service.chargePendingSubscriptions()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 0) { paymentProvider.charge(any()) }
        verify(exactly = 0) { invoiceEventSender.send(any()) }

    }

    @Test
    fun `given pending invoices and charge to a provider is succeed we should update invoice status`() {
        // Given
        val invoice = Invoice(12, 15, Money(BigDecimal(345.2), EUR), PENDING)
        every { invoiceService.fetchPendingInvoices() } returns listOf(invoice)
        every { paymentProvider.charge(invoice) } returns true
        every { invoiceService.updateStatus(12, PROCESSING) } returns invoice.copy(status = PROCESSING)
        every { invoiceService.updateStatus(12, PAID) } returns invoice.copy(status = PAID)
        every { invoiceEventSender.send(InvoicePayed(15)) } returns Unit

        // When
        service.chargePendingSubscriptions()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 1) { paymentProvider.charge(invoice) }
        verify(exactly = 1) { invoiceService.updateStatus(12, PROCESSING) }
        verify(exactly = 1) { invoiceService.updateStatus(12, PAID) }
        verify(exactly = 1) { invoiceEventSender.send(InvoicePayed(15)) }
    }

    @Test
    fun `given pending invoices and charge to a provider is succeed with false we should update invoice status`() {
        // Given
        val invoice = Invoice(12, 15, Money(BigDecimal(345.2), EUR), PENDING)
        every { invoiceService.fetchPendingInvoices() } returns listOf(invoice)
        every { paymentProvider.charge(invoice) } returns false
        every { invoiceService.updateStatus(12, PROCESSING) } returns invoice.copy(status = PROCESSING)
        every { invoiceService.updateStatus(12, NO_BALANCE) } returns invoice.copy(status = NO_BALANCE)
        every { invoiceEventSender.send(NoBalanceToPay(15)) } returns Unit

        // When
        service.chargePendingSubscriptions()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 1) { paymentProvider.charge(invoice) }
        verify(exactly = 1) { invoiceService.updateStatus(12, PROCESSING) }
        verify(exactly = 1) { invoiceService.updateStatus(12, NO_BALANCE) }
        verify(exactly = 1) { invoiceEventSender.send(NoBalanceToPay(15)) }
    }

    @Test
    fun `given pending invoices and charge to a provider is failure with customer not found we should update invoice status`() {
        // Given
        val invoice = Invoice(12, 15, Money(BigDecimal(345.2), EUR), PENDING)
        every { invoiceService.fetchPendingInvoices() } returns listOf(invoice)
        every { paymentProvider.charge(invoice) } throws  CustomerNotFoundException(15)
        every { invoiceService.updateStatus(12, PROCESSING) } returns invoice.copy(status = PROCESSING)
        every { invoiceService.updateStatus(12, CUSTOMER_NOT_ON_PROVIDER) } returns invoice.copy(status = CUSTOMER_NOT_ON_PROVIDER)
        every { invoiceEventSender.send(CustomerNotFoundOnPaymentProvider(15)) } returns Unit

        // When
        service.chargePendingSubscriptions()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 1) { paymentProvider.charge(invoice) }
        verify(exactly = 1) { invoiceService.updateStatus(12, PROCESSING) }
        verify(exactly = 1) { invoiceService.updateStatus(12, CUSTOMER_NOT_ON_PROVIDER) }
        verify(exactly = 1) { invoiceEventSender.send(CustomerNotFoundOnPaymentProvider(15)) }
    }

    @Test
    fun `given pending invoices and charge to a provider is failure with currency mismatch we should update invoice status`() {
        // Given
        val invoice = Invoice(12, 15, Money(BigDecimal(345.2), EUR), PENDING)
        every { invoiceService.fetchPendingInvoices() } returns listOf(invoice)
        every { paymentProvider.charge(invoice) } throws  CurrencyMismatchException(12, 15)
        every { invoiceService.updateStatus(12, PROCESSING) } returns invoice.copy(status = PROCESSING)
        every { invoiceService.updateStatus(12, CURRENCY_MISMATCH) } returns invoice.copy(status = CURRENCY_MISMATCH)
        every { invoiceEventSender.send(CurrencyMismatch(15)) } returns Unit

        // When
        service.chargePendingSubscriptions()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 1) { paymentProvider.charge(invoice) }
        verify(exactly = 1) { invoiceService.updateStatus(12, PROCESSING) }
        verify(exactly = 1) { invoiceService.updateStatus(12, CURRENCY_MISMATCH) }
        verify(exactly = 1) { invoiceEventSender.send(CurrencyMismatch(15)) }
    }

    @Test
    fun `given pending invoices and charge to a provider is failure do it to connection error we should log it`() {
        // Given
        val invoice = Invoice(12, 15, Money(BigDecimal(345.2), EUR), PENDING)
        every { invoiceService.fetchPendingInvoices() } returns listOf(invoice)
        every { invoiceService.updateStatus(12, PROCESSING) } returns invoice.copy(status = PROCESSING)
        every { paymentProvider.charge(invoice) } throws  NetworkException()

        // When
        service.chargePendingSubscriptions()

        // Then
        verify(exactly = 1) { invoiceService.fetchPendingInvoices() }
        verify(exactly = 1) { paymentProvider.charge(invoice) }
        verify(exactly = 1) { invoiceService.updateStatus(12, PROCESSING) }
        verify(exactly = 1) { invoiceService.updateStatus(any(), any()) }
    }

}