plugins {
	kotlin("jvm")
	id("ru.capjack.bintray")
}

dependencies {
	implementation(kotlin("compiler-embeddable"))
	
	implementation("ru.capjack.tool:tool-utils")
	implementation("org.yaml:snakeyaml:1.25")
	api("ru.capjack.tool:tool-io-biser-generator")
}