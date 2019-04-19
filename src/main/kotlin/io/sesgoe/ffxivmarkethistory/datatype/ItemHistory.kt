package io.sesgoe.ffxivmarkethistory.datatype

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ItemHistory(
        @SerialName("Added")
        val addedTimeInSeconds : Long,
        @SerialName("ID")
        val transactionId : String,
        @SerialName("IsHQ")
        val isHighQuality : Boolean,
        @SerialName("PricePerUnit")
        val pricePerUnit: Int,
        @SerialName("PriceTotal")
        val priceTotal: Int,
        @SerialName("PurchaseDate")
        val purchaseTimeInSeconds: Long,
        @SerialName("Quantity")
        val quantity: Int
) {

        @Transient
        val addedTimeInMillis = addedTimeInSeconds * 1000
        @Transient
        val purchaseTimeInMillis = purchaseTimeInSeconds * 1000
}


@Serializable
data class ItemHistoryResponse(
        @SerialName("History")
        val itemHistoryList: List<ItemHistory>
)