package utils

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

class DBGenerator {

    companion object{
        fun createDb(name: String, tables: Array<Table>) = Database
            .connect(url = "jdbc:sqlite:${File.createTempFile(name, ".sqlite").absolutePath}",
                     driver = "org.sqlite.JDBC",
                     user = "root",
                     password = "")
            .also {
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                transaction(it) {
                    addLogger(StdOutSqlLogger)
                    SchemaUtils.drop(*tables)
                    SchemaUtils.create(*tables)
                }
            }

    }
}