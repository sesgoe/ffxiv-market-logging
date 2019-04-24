package io.sesgoe.ffxivmarkethistory.ffxivapi

import com.google.common.util.concurrent.RateLimiter
import io.sesgoe.ffxivmarkethistory.constant.FFXIV_API_BASEURL
import io.sesgoe.ffxivmarkethistory.constant.FFXIV_API_PRIVATE_KEY
import io.sesgoe.ffxivmarkethistory.datatype.*
import khttp.get
import kotlinx.serialization.json.Json

val userAgentMap = mapOf("User-Agent" to "<User-Agent>")

fun getItemCategoriesList() : List<ItemCategory> {

    val url = "$FFXIV_API_BASEURL/market/categories?private_key=$FFXIV_API_PRIVATE_KEY"
    val request = get (
            url = url,
            headers = userAgentMap
    )
    val requestTextAlteredForSerialization = "{\"Result\":".plus(request.text).plus("}")
    val itemCategoryResponse = Json.nonstrict.parse(ItemCategoryResponse.serializer(), requestTextAlteredForSerialization)
    return itemCategoryResponse.itemCategoryList

}

fun getItemsForCategoryId(categoryId : Int) : List<Item> {

    val itemList = mutableListOf<Item>()
    val rateLimiter = RateLimiter.create(5.0)

    val initialUrl = "$FFXIV_API_BASEURL/search?indexes=item&filters=ItemSearchCategory.ID=$categoryId&page=1&private_key=$FFXIV_API_PRIVATE_KEY"
    val initialRequest = get(
            url = initialUrl,
            headers = userAgentMap
    )
    val initialResponse = Json.nonstrict.parse(ItemResponse.serializer(), initialRequest.text)
    itemList.addAll(convertApiItemListToItemList(initialResponse.itemList, categoryId))
    var pageNext = initialResponse.pagination.pageNext

    while(pageNext != null) {
        rateLimiter.acquire()
        val url = "$FFXIV_API_BASEURL/search?indexes=item&filters=ItemSearchCategory.ID=$categoryId&page=$pageNext&private_key=$FFXIV_API_PRIVATE_KEY"
        val request = get(
                url = url,
                headers = userAgentMap
        )
        val response = Json.nonstrict.parse(ItemResponse.serializer(), request.text)
        itemList.addAll(convertApiItemListToItemList(response.itemList, categoryId))
        pageNext = response.pagination.pageNext
    }

    return itemList
}

fun convertApiItemListToItemList(apiItemList: List<ApiItem>, itemCategory : Int) : List<Item> = apiItemList.map{Item(it.id, it.name, itemCategory)}.toList()

fun getHistoryForItemId(itemId: Int, server: Server) : List<ItemHistory> {

    val url = "$FFXIV_API_BASEURL/market/${server.name}/item/$itemId?private_key=$FFXIV_API_PRIVATE_KEY"

    val request = get(
            url = url,
            headers = userAgentMap
    )

    if(request.statusCode != 200) {
        return emptyList()
    }

    val historyResponse = Json.nonstrict.parse(ItemHistoryResponse.serializer(), request.text)

    return historyResponse.itemHistoryList

}