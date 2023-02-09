package io.pleo.antaeus.core.ports

import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money

interface InvoiceRepository {

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus): Invoice?
    fun fetchInvoice(id: Int): Invoice?
    fun fetchInvoices(): List<Invoice>
    fun fetchPendingInvoices(): List<Invoice>

}