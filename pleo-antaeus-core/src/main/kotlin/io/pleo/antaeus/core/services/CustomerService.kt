package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.ports.CustomerRepository
import io.pleo.antaeus.models.Customer

class CustomerService(private val customerRepository: CustomerRepository) {
    fun fetchAll(): List<Customer> {
        return customerRepository.fetchCustomers()
    }

    fun fetch(id: Int): Customer {
        return customerRepository.fetchCustomer(id) ?: throw CustomerNotFoundException(id)
    }
}
