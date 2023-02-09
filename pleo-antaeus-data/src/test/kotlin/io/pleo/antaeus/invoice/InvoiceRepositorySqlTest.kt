package io.pleo.antaeus.invoice

import io.pleo.antaeus.models.Currency.EUR
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus.PAID
import io.pleo.antaeus.models.InvoiceStatus.PENDING
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import utils.DBGenerator
import java.math.BigDecimal

internal class InvoiceRepositorySqlTest{

    private val db = DBGenerator.createDb("invoice-tests-db", arrayOf(InvoiceTable))

    val repositorySql = InvoiceRepositorySql(db)

    @Nested
    inner class FetchInvoicesTest{

        @Test
        fun `given invoice not found on the system should return a null`(){
            // When
            assert(repositorySql.fetchInvoices() == emptyList<Invoice>())
        }

        @Test
        fun `given invoice found on the system should return the invoice`(){
            // Given
            val invoice1 = repositorySql.createInvoice(Money(BigDecimal(24), EUR), Customer(1, EUR), PENDING)
            val invoice2 = repositorySql.createInvoice(Money(BigDecimal(25), EUR), Customer(2, EUR), PENDING)

            // When - Then
            assert(repositorySql.fetchInvoices() == listOf(invoice1, invoice2))
        }
    }

    @Nested
    inner class FetchPendingInvoicesTest{

        @Test
        fun `given any pending invoice found on the system should return a null`(){
            // Given
            repositorySql.createInvoice(Money(BigDecimal(24), EUR), Customer(1, EUR), PAID)

            // When - Then
            assert(repositorySql.fetchPendingInvoices() == emptyList<Invoice>())
        }

        @Test
        fun `given invoice pending found on the system should return the invoice`(){
            // Given
            val invoice = repositorySql.createInvoice(Money(BigDecimal(24), EUR), Customer(1, EUR), PENDING)
            repositorySql.createInvoice(Money(BigDecimal(25), EUR), Customer(2, EUR), PAID)

            // When - Then
            assert(repositorySql.fetchPendingInvoices() == listOf(invoice))
        }
    }
}