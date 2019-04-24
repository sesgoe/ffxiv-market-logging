package io.sesgoe.ffxivmarkethistory.table

import org.jetbrains.exposed.sql.Table

object ServerTable : Table() {
    val id = integer("id").primaryKey()
    val name = text("name")
}