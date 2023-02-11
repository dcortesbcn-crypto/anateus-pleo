package io.pleo.antaeus.core.ports

import io.pleo.antaeus.core.events.InvoiceEvent

interface InvoiceEventSender {

    fun send(event: InvoiceEvent)

}