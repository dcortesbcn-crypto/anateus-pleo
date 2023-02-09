package io.pleo.antaeus.invoice

import io.pleo.antaeus.core.ports.InvoiceRepository
import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InvoiceRepositorySql(private val db: Database) : InvoiceRepository {

    override fun fetchInvoice(id: Int): Invoice? {
        return transaction(db) {
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    override fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    override fun fetchPendingInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .select{ InvoiceTable.status.eq(InvoiceStatus.PENDING.toString()) }
                .map { it.toInvoice() }
        }
    }

    override fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus): Invoice? {
        val id = transaction(db) {
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }

        return fetchInvoice(id)
    }
}

fun ResultRow.toInvoice(): Invoice = Invoice(
    id = this[InvoiceTable.id],
    amount = Money(
        value = this[InvoiceTable.value],
        currency = Currency.valueOf(this[InvoiceTable.currency])
    ),
    status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
    customerId = this[InvoiceTable.customerId]
)