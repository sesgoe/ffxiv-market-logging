package io.sesgoe.ffxivmarkethistory.controller

import com.google.common.util.concurrent.RateLimiter
import io.sesgoe.ffxivmarkethistory.constant.*
import io.sesgoe.ffxivmarkethistory.datatype.History
import io.sesgoe.ffxivmarkethistory.datatype.HistoryResponse
import io.sesgoe.ffxivmarkethistory.table.HistoryTable
import khttp.get
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class ScheduledController {

    @GetMapping("/test")
    fun getTest() : OreTypeResponse {
        return oreTypesList
    }

    @GetMapping("/getHistory/{itemId}")
    fun getHistory(@PathVariable itemId: Int) : List<History> {

        val url = "$FFXIV_API_BASEURL/market/$SERVER_NAME/item/$itemId?private_key=$FFXIV_API_PRIVATE_KEY"

        val request = get(
                        url = url,
                        headers = mapOf("User-Agent" to "<User-Agent>")
                    )

        val historyResponse = Json.nonstrict.parse(HistoryResponse.serializer(), request.text)

        return historyResponse.historyList

    }

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun pullFullHistory() {

        println("Starting history pull.")

        val startTime = System.currentTimeMillis()
        val rateLimiter = RateLimiter.create(5.0)
        var totalNewRows = 0

        for(i in oreTypesList.results.indices) {
            rateLimiter.acquire()
            val itemId = oreTypesList.results[i].id
            val itemName = oreTypesList.results[i].name
            val itemHistoryList = getHistory(itemId)
            val transactionIdList = extractTransactionIdsFromItemHistoryList(itemHistoryList)

            totalNewRows += getNewRowCountForListOfTransactionIds(transactionIdList)

            batchInsertIntoDatabase(
                    itemId = itemId,
                    itemName = itemName,
                    historyList = itemHistoryList
            )
            if(i % 10 == 0) {
                println("Batch ${(i / 10) + 1} complete of ${(oreTypesList.results.size / 10) + 1}")
            }
        }
        val endTime = System.currentTimeMillis()

        println("History pull complete. Time to execute: ${endTime - startTime}")
        println("New rows added: $totalNewRows")

    }

    fun extractTransactionIdsFromItemHistoryList(itemHistoryList: List<History>) = itemHistoryList.map { it.transactionId }.toList()

    fun getNewRowCountForListOfTransactionIds(transactionIds: List<String>) : Int {

        Database.connect(
                url = "jdbc:postgresql://localhost:5432/postgres",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "docker"
        )

        var queryCount = 0

        transaction {
            queryCount = HistoryTable.select {
                HistoryTable.transactionId.inList(transactionIds)
            }.count()
        }

        return transactionIds.size - queryCount

    }

    fun batchInsertIntoDatabase(itemId: Int, itemName: String, historyList : List<History>) {

        Database.connect(
                url = "jdbc:postgresql://localhost:5432/postgres",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "docker"
        )

        transaction {

            SchemaUtils.create(HistoryTable)

            HistoryTable.batchInsert(historyList, ignore = true) { history ->
                this[HistoryTable.transactionId] = history.transactionId
                this[HistoryTable.itemId] = itemId
                this[HistoryTable.itemName] = itemName
                this[HistoryTable.addedTimeStamp] = DateTime(history.addedTimeInMillis)
                this[HistoryTable.purchasedTimeStamp] = DateTime(history.purchaseTimeInMillis)
                this[HistoryTable.pricePerUnit] = history.pricePerUnit
                this[HistoryTable.priceTotal] = history.priceTotal
                this[HistoryTable.quantity] = history.quantity
                this[HistoryTable.isHighQuality] = history.isHighQuality
            }

        }

    }

}