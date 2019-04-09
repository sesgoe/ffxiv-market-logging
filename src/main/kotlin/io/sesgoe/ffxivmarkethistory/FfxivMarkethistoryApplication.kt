package io.sesgoe.ffxivmarkethistory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FfxivMarkethistoryApplication

fun main(args: Array<String>) {
	runApplication<FfxivMarkethistoryApplication>(*args)
}
