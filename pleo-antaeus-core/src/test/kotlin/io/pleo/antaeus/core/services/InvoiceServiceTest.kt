package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.ports.InvoiceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {
    private val invoiceRepository = mockk<InvoiceRepository> {
        every { fetchInvoice(404) } returns null
    }

    private val invoiceService = InvoiceService(invoiceRepository = invoiceRepository)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }
}
