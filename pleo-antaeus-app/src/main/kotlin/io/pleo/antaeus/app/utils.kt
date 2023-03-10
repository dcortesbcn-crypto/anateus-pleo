
import io.pleo.antaeus.core.events.InvoiceEvent
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.ports.CustomerRepository
import io.pleo.antaeus.core.ports.InvoiceEventSender
import io.pleo.antaeus.core.ports.InvoiceRepository
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.math.BigDecimal
import kotlin.random.Random

// This will create all schemas and setup initial data
internal fun setupInitialData(customerRepository: CustomerRepository, invoiceRepository: InvoiceRepository) {
    val customers = (1..100).mapNotNull {
        customerRepository.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
        )
    }

    customers.forEach { customer ->
        (1..10).forEach {
            invoiceRepository.createInvoice(
                amount = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    currency = customer.currency
                ),
                customer = customer,
                status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
            )
        }
    }
}

// This is the mocked instance of the payment provider
internal fun getPaymentProvider(): PaymentProvider {
    return object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
        }
    }
}

internal fun getInvoiceEventSender(): InvoiceEventSender {
    return object : InvoiceEventSender {
        override fun send(event: InvoiceEvent) {
            // Here we should implement our event handler
        }
    }
}
