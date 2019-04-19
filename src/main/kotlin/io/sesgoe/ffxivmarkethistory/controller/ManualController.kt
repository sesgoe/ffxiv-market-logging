package io.sesgoe.ffxivmarkethistory.controller

import io.sesgoe.ffxivmarkethistory.database.*
import io.sesgoe.ffxivmarkethistory.datatype.Item
import io.sesgoe.ffxivmarkethistory.datatype.ItemCategory
import io.sesgoe.ffxivmarkethistory.ffxivapi.getItemCategoriesList
import io.sesgoe.ffxivmarkethistory.ffxivapi.getItemsForCategoryId
import io.sesgoe.ffxivmarkethistory.global.GlobalStateVariables
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ManualController {

    val logger : Logger = LoggerFactory.getLogger("ManualController")

    @GetMapping("/toggleHistoryScanTurnedOn")
    fun toggleHistoryScanTurnedOn(): Boolean {
        GlobalStateVariables.historyScanTurnedOn = !GlobalStateVariables.historyScanTurnedOn
        return GlobalStateVariables.historyScanTurnedOn
    }

    @GetMapping("/createTables")
    fun createDatabaseTables() {
        createTables()
    }

    @GetMapping("/populateCategories")
    fun populateCategoryTable() {
        val itemCategoryList = getItemCategoriesList()
        batchInsertItemCategoryListIntoDatabase(itemCategoryList)
    }

    @GetMapping("/getCategoriesFromDatabase")
    fun getCategoriesFromDB() : List<ItemCategory> {
        return getItemCategoryListFromDatabase()
    }

    @GetMapping("/populateItems")
    fun populateItemTable() {

        val categoryList = getCategoriesFromDB()
        for(category in categoryList) {
            logger.info("Starting insertion for category ${category.id}")
            val itemList = getItemsForCategoryId(category.id)
            batchInsertItemListIntoDatabase(itemList)
            logger.info("Completed insertion for category ${category.id}")
        }

        logger.info("Completed populating Item Table")

    }

    @GetMapping("/getItemsFromDatabase")
    fun getItemsFromDB() : List<Item> {
        return getItemListFromDatabase()
    }

}