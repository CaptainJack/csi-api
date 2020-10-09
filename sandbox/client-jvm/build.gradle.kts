plugins {
	kotlin("jvm")
}

dependencies {
	implementation(project(":runtime:csi-api-client"))
	implementation(project(":sandbox:api:client"))
	
	implementation("ru.capjack.csi:csi-core-client")
	implementation("ru.capjack.csi:csi-transport-netty-client")
	implementation("ru.capjack.tool:tool-logging")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-io")
	
	implementation("ch.qos.logback:logback-classic:1.2.3")
}
