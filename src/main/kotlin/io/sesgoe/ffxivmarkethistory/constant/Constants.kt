package io.sesgoe.ffxivmarkethistory.constant

import io.sesgoe.ffxivmarkethistory.datatype.Server


const val FFXIV_API_BASEURL = "https://xivapi.com"

val SERVER_NAME = System.getenv("WORLD_NAME")
val SERVER_NAME_LIST = SERVER_NAME.split(",")

val SERVER_LIST = SERVER_NAME_LIST.mapIndexed { index, s -> Server(index, s) }.toList()
val FFXIV_API_PRIVATE_KEY = System.getenv("FFXIV_API_PRIVATE_KEY")