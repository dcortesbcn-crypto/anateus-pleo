package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.ports.InvoiceRepository
import io.pleo.antaeus.models.Invoice

class InvoiceService(private val invoiceRepository: InvoiceRepository) {
    fun fetchAll(): List<Invoice> {
        return invoiceRepository.fetchInvoices()
    }

    fun fetchPendingInvoices(): List<Invoice> {
        return invoiceRepository.fetchPendingInvoices()
    }

    fun fetch(id: Int): Invoice {
        return invoiceRepository.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }
}
