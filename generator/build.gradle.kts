plugins {
	kotlin("jvm")
	id("ru.capjack.publisher")
}

dependencies {
	implementation(kotlin("compiler-embeddable"))
	
	implementation("ru.capjack.tool:tool-utils")
	api("ru.capjack.tool:tool-biser-generator")
}