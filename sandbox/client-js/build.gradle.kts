plugins {
	kotlin("js")
}

kotlin {
	js(IR) {
		browser()
		binaries.executable()
	}
}

dependencies {
	implementation(project(":runtime:csi-api-client"))
	implementation(project(":sandbox:api:client"))
	
	implementation("ru.capjack.csi:csi-core-client")
	implementation("ru.capjack.csi:csi-transport-js-client-browser")
	implementation("ru.capjack.tool:tool-logging")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-io")
}
