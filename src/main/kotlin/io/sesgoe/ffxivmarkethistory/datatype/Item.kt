package io.sesgoe.ffxivmarkethistory.datatype

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiItem (
        @SerialName("ID")
        val id : Int,
        @SerialName("Name")
        val name : String
)

@Serializable
data class ItemResponse(
        @SerialName("Pagination")
        val pagination: Pagination,
        @SerialName("Results")
        val itemList : List<ApiItem>
)

data class Item(
        val id : Int,
        val name : String,
        val categoryId : Int
)