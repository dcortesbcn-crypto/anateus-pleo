package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.ports.InvoiceRepository
import io.pleo.antaeus.models.InvoiceStatus.PAID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {

    private val invoiceRepository = mockk<InvoiceRepository>()
    private val invoiceService = InvoiceService(invoiceRepository = invoiceRepository)

    @Test
    fun `when retrieving a missing invoice it should throw an invoice not found error`() {
        // Given
        every { invoiceRepository.fetchInvoice(404) } returns null

        // When - Then
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `when updating a missing invoice it should throw an invoice not found error`() {
        // Given
        every { invoiceRepository.updateStatusInvoice(404, PAID) } returns null

        // When - Then
        assertThrows<InvoiceNotFoundException> {
            invoiceService.updateStatus(404, PAID)
        }
    }
}
