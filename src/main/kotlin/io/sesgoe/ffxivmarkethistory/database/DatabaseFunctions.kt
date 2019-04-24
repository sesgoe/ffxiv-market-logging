package io.sesgoe.ffxivmarkethistory.database

import io.sesgoe.ffxivmarkethistory.constant.SERVER_LIST
import io.sesgoe.ffxivmarkethistory.datatype.Item
import io.sesgoe.ffxivmarkethistory.datatype.ItemCategory
import io.sesgoe.ffxivmarkethistory.datatype.ItemHistory
import io.sesgoe.ffxivmarkethistory.datatype.Server
import io.sesgoe.ffxivmarkethistory.table.CategoryTable
import io.sesgoe.ffxivmarkethistory.table.HistoryTable
import io.sesgoe.ffxivmarkethistory.table.ItemTable
import io.sesgoe.ffxivmarkethistory.table.ServerTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime


fun connectToDatabase() = Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "docker"
)


fun createTables() {
    connectToDatabase()
    transaction {
        SchemaUtils.create(CategoryTable)
        SchemaUtils.create(ItemTable)
        SchemaUtils.create(HistoryTable)
        SchemaUtils.create(ServerTable)

        ServerTable.batchInsert(SERVER_LIST, ignore = true) { server ->
            this[ServerTable.id] = server.id
            this[ServerTable.name] = server.name
        }
    }
}

fun batchInsertItemCategoryListIntoDatabase(itemCategoryList : List<ItemCategory>) {
    connectToDatabase()
    transaction {
        CategoryTable.batchInsert(itemCategoryList, ignore = true) { itemCategory ->
            this[CategoryTable.id] = itemCategory.id
            this[CategoryTable.category] = itemCategory.category
            this[CategoryTable.name] = itemCategory.name
        }
    }
}

fun batchInsertItemListIntoDatabase(itemList: List<Item>) {
    connectToDatabase()
    transaction {
        ItemTable.batchInsert(itemList, ignore = true) { item ->
            this[ItemTable.id] = item.id
            this[ItemTable.categoryId] = item.categoryId
            this[ItemTable.name] = item.name
        }
    }
}

fun batchInsertItemHistoryListIntoDatabase(itemId: Int, server: Server, itemHistoryList : List<ItemHistory>) {
    connectToDatabase()
    transaction {
        HistoryTable.batchInsert(itemHistoryList, ignore = true) { history ->
            this[HistoryTable.transactionId] = history.transactionId
            this[HistoryTable.itemId] = itemId
            this[HistoryTable.addedTimeStamp] = DateTime(history.addedTimeInMillis)
            this[HistoryTable.purchasedTimeStamp] = DateTime(history.purchaseTimeInMillis)
            this[HistoryTable.pricePerUnit] = history.pricePerUnit
            this[HistoryTable.priceTotal] = history.priceTotal
            this[HistoryTable.quantity] = history.quantity
            this[HistoryTable.isHighQuality] = history.isHighQuality
            this[HistoryTable.insertTimeStamp] = DateTime(System.currentTimeMillis())
            this[HistoryTable.serverId] = server.id
        }
    }
}

fun getItemCategoryListFromDatabase() : List<ItemCategory> {

    var categoryList = mutableListOf<ItemCategory>()

    connectToDatabase()
    transaction {

        categoryList = CategoryTable.selectAll().map { ItemCategory(
                category = it[CategoryTable.category],
                id = it[CategoryTable.id],
                name = it[CategoryTable.name])
        }.toMutableList()

    }

    return categoryList
}

fun getItemListFromDatabase() : List<Item> {

    var itemList = mutableListOf<Item>()

    connectToDatabase()
    transaction {

        itemList = ItemTable.selectAll().map { Item(
                id = it[ItemTable.id],
                categoryId = it[ItemTable.categoryId],
                name = it[ItemTable.name]
        )
        }.toMutableList()

    }

    return itemList
}


fun getNewRowCountForListOfTransactionIds(transactionIds: List<String>) : Int {

    connectToDatabase()

    var queryCount = 0

    transaction {
        queryCount = HistoryTable.select {
            HistoryTable.transactionId.inList(transactionIds)
        }.count()
    }

    return transactionIds.size - queryCount

}