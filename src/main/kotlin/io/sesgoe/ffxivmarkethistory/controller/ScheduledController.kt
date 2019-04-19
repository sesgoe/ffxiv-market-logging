package io.sesgoe.ffxivmarkethistory.controller

import com.google.common.util.concurrent.RateLimiter
import io.sesgoe.ffxivmarkethistory.database.batchInsertItemHistoryListIntoDatabase
import io.sesgoe.ffxivmarkethistory.database.getItemListFromDatabase
import io.sesgoe.ffxivmarkethistory.database.getNewRowCountForListOfTransactionIds
import io.sesgoe.ffxivmarkethistory.datatype.ItemHistory
import io.sesgoe.ffxivmarkethistory.ffxivapi.getHistoryForItemId
import io.sesgoe.ffxivmarkethistory.global.GlobalStateVariables
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController
class ScheduledController {

    val logger : Logger = LoggerFactory.getLogger("ScheduledController")

    @GetMapping("/pullHistory")
    fun manuallyTriggerHistoryScan() {
        pullFullHistory()
    }


    @Scheduled(fixedDelay = 1000 * 60 * 60 * 4) //delay each execution by 4 hours
    fun pullFullHistory() {

        if(!GlobalStateVariables.historyScanTurnedOn) {
            logger.info("History scan turned off--not initiating scan.")
            return
        }

        logger.info("Starting full history pull at ${LocalDateTime.now()}")

        val startTime = System.currentTimeMillis()
        val rateLimiter = RateLimiter.create(5.0)
        var totalNewRows = 0

        val itemList = getItemListFromDatabase()

        for(i in itemList.indices) {
            rateLimiter.acquire()
            val itemHistoryList = getHistoryForItemId(itemList[i].id)
            val transactionIdList = extractTransactionIdsFromItemHistoryList(itemHistoryList)

            totalNewRows += getNewRowCountForListOfTransactionIds(transactionIdList)

            batchInsertItemHistoryListIntoDatabase(
                    itemId = itemList[i].id,
                    itemHistoryList = itemHistoryList
            )

            if((i > 0 && i % 100 == 0) || i == itemList.lastIndex) {
                logger.info("Completed History Pull for $i of ${itemList.size} items.")
            }

        }
        val endTime = System.currentTimeMillis()

        logger.info("ItemHistory pull complete. Time to execute: ${endTime - startTime} seconds")
        logger.info("New rows added: $totalNewRows")

    }

    fun extractTransactionIdsFromItemHistoryList(itemItemHistoryList: List<ItemHistory>) = itemItemHistoryList.map { it.transactionId }.toList()

}