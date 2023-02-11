package io.pleo.antaeus.models

enum class InvoiceStatus {
    PENDING,
    PAID,
    CUSTOMER_NOT_ON_PROVIDER,
    CURRENCY_MISMATCH,
    NO_BALANCE
}
