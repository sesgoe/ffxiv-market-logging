package io.sesgoe.ffxivmarkethistory.controller

import com.google.common.util.concurrent.RateLimiter
import com.google.gson.Gson
import io.sesgoe.ffxivmarkethistory.constant.FFXIV_API_BASEURL
import io.sesgoe.ffxivmarkethistory.constant.FFXIV_API_PRIVATE_KEY
import io.sesgoe.ffxivmarkethistory.constant.SERVER_NAME
import io.sesgoe.ffxivmarkethistory.constants.OreTypeResponse
import io.sesgoe.ffxivmarkethistory.constants.oreTypesList
import io.sesgoe.ffxivmarkethistory.datatype.History
import io.sesgoe.ffxivmarkethistory.datatype.HistoryResponse
import io.sesgoe.ffxivmarkethistory.table.HistoryTable
import khttp.get
import org.jetbrains.exposed.sql.*
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

        val historyResponse = Gson().fromJson(request.text, HistoryResponse::class.java)

        return historyResponse.historyList

    }

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun pullFullHistory() {

        println("Starting history pull.")

        val startTime = System.currentTimeMillis()
        val rateLimiter = RateLimiter.create(5.0)

        for(i in oreTypesList.results.indices) {
            rateLimiter.acquire()
            val id = oreTypesList.results[i].id
            val name = oreTypesList.results[i].name
            insertIntoDatabase(id, name, getHistory(id))
            if(i % 10 == 0) {
                println("Batch ${i / 10} complete of ${oreTypesList.results.size / 10}")
            }
        }
        val endTime = System.currentTimeMillis()

        println("History pull complete. Time to execute: ${endTime - startTime}")

    }

    fun insertIntoDatabase(itemId: Int, name: String, historyList : List<History>) {

        val db = Database.connect(
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
                this[HistoryTable.itemName] = name
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