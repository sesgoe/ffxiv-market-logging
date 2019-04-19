package io.sesgoe.ffxivmarkethistory.table

import org.jetbrains.exposed.sql.Table

object CategoryTable : Table() {
    val id = integer("id").primaryKey()
    val category = integer("category")
    val name = text("name")
}