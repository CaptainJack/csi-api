plugins {
	kotlin("jvm")
}

dependencies {
	api("ru.capjack.tool:tool-logging")
	api("ru.capjack.tool:tool-utils")
	api("ru.capjack.tool:tool-io")
	
	api("ch.qos.logback:logback-classic:1.2.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}
