package io.sesgoe.ffxivmarkethistory.datatype

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemCategory (
        @SerialName("Category")
        val category : Int,
        @SerialName("ID")
        val id : Int,
        @SerialName("Name")
        val name : String
)

@Serializable
data class ItemCategoryResponse(
        @SerialName("Result")
        val itemCategoryList : List<ItemCategory>
)