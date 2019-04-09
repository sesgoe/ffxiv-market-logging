package io.sesgoe.ffxivmarkethistory.table

import org.jetbrains.exposed.sql.Table

object HistoryTable : Table() {
    val transactionId = text("transactionid").primaryKey()
    val itemId = integer("itemid")
    val itemName = text("itemname")
    val addedTimeStamp = datetime("addedtimestamp")
    val purchasedTimeStamp = datetime("purchasedtimestamp")
    val pricePerUnit = integer("priceperunit")
    val priceTotal = integer("pricetotal")
    val quantity = integer("quantity")
    val isHighQuality = bool("ishighquality")
}