package io.sesgoe.ffxivmarkethistory.datatype

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
        @SerialName("Page")
        val page : Int,
        @SerialName("PageNext")
        val pageNext: Int?
)