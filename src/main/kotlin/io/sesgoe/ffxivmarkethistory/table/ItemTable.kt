package io.sesgoe.ffxivmarkethistory.table

import org.jetbrains.exposed.sql.Table

object ItemTable : Table() {
    val id = integer("id").primaryKey()
    val categoryId = integer("categoryid")
    val name = text("name")
}