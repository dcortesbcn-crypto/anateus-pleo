package io.pleo.antaeus.core.events


sealed class InvoiceEvent(val result: String, val type: String="INVOICE_STATUS_CHANGED")
data class InvoicePayed(val customerId: Int) : InvoiceEvent("INVOICE_PAYED")
data class NoBalanceToPay(val customerId: Int) : InvoiceEvent("NO_BALANCE_TO_PAY")
data class CurrencyMismatch(val customerId: Int) : InvoiceEvent("CURRENCY_MISMATCH")
data class CustomerNotFoundOnPaymentProvider(val customerId: Int) : InvoiceEvent("CUSTOMER_NOT_FOUND_ON_PAYMENT_PROVIDER")