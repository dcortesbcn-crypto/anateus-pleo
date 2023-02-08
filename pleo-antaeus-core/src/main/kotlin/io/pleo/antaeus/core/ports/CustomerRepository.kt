package io.pleo.antaeus.core.ports

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money

interface CustomerRepository {

    fun fetchCustomer(id: Int): Customer?
    fun fetchCustomers(): List<Customer>
    fun createCustomer(currency: Currency): Customer?

}