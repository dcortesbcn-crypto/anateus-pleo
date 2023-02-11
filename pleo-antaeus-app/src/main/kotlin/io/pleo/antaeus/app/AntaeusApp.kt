/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getInvoiceEventSender
import getPaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.customer.*
import io.pleo.antaeus.invoice.InvoiceRepositorySql
import io.pleo.antaeus.invoice.InvoiceTable
import io.pleo.antaeus.rest.AntaeusRest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.io.File
import java.sql.Connection
import java.util.*
import kotlin.concurrent.timerTask


fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    val dbFile: File = File.createTempFile("antaeus-db", ".sqlite")
    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
        .connect(url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC",
            user = "root",
            password = "")
        .also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            transaction(it) {
                addLogger(StdOutSqlLogger)
                // Drop all existing tables to ensure a clean slate on each run
                SchemaUtils.drop(*tables)
                // Create all tables
                SchemaUtils.create(*tables)
            }
        }

    // Set up data access layer.
    val customerRepository = CustomerRepositorySql(db)
    val invoiceRepository = InvoiceRepositorySql(db)

    // Insert example data in the database.
    setupInitialData(customerRepository, invoiceRepository)

    // Get third parties
    val paymentProvider = getPaymentProvider()

    val invoiceEventSender = getInvoiceEventSender()

    // Create core services
    val invoiceService = InvoiceService(invoiceRepository)
    val customerService = CustomerService(customerRepository)

    // This is _your_ billing service to be included where you see fit
    val billingService = BillingService(
        paymentProvider = paymentProvider,
        invoiceService=invoiceService,
        invoiceEventsSender = invoiceEventSender
    )

    billingService.chargePendingSubscriptions()


    val date = Calendar.getInstance()
    date[Calendar.HOUR] = 6
    date[Calendar.MINUTE] = 0
    date[Calendar.SECOND] = 0

    Timer().schedule(
        timerTask { billingService.chargePendingSubscriptions() },
        date.time,
        1000 * 60 * 60 * 24
    )


    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService,
        customerService = customerService
    ).run()
}
