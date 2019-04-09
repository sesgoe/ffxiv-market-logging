package io.sesgoe.ffxivmarkethistory.datatype

import com.google.gson.annotations.SerializedName

data class History(
        @SerializedName("Added")
        val addedTimeInSeconds : Long,
        @SerializedName("ID")
        val transactionId : String,
        @SerializedName("IsHQ")
        val isHighQuality : Boolean,
        @SerializedName("PricePerUnit")
        val pricePerUnit: Int,
        @SerializedName("PriceTotal")
        val priceTotal: Int,
        @SerializedName("PurchaseDate")
        val purchaseTimeInSeconds: Long,
        @SerializedName("Quantity")
        val quantity: Int
) {
        val addedTimeInMillis : Long
                get() = addedTimeInSeconds * 1000
        val purchaseTimeInMillis : Long
                get() = purchaseTimeInSeconds * 1000
}

data class HistoryResponse(
        @SerializedName("History")
        val historyList: List<History>
)