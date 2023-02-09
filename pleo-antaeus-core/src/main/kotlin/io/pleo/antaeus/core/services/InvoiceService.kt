package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.ports.InvoiceRepository
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

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

    fun updateStatus(id: Int, status: InvoiceStatus): Invoice {
        return invoiceRepository.updateStatusInvoice(id, status) ?: throw InvoiceNotFoundException(id)
    }
}
