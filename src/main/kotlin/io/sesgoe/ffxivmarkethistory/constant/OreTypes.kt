package io.sesgoe.ffxivmarkethistory.constant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.util.ResourceUtils

@Serializable
data class Item(
        @SerialName("ID")
        val id: Int,
        @SerialName("Name")
        val name: String
)

@Serializable
data class OreTypeResponse(
        @SerialName("Results")
        val results: List<Item>
)


val oreTypesJsonString = ResourceUtils.getFile("classpath:oreTypes.json").readText()

val oreTypesList = Json.nonstrict.parse(OreTypeResponse.serializer(), oreTypesJsonString)