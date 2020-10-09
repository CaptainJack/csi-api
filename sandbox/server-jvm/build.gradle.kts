plugins {
	kotlin("jvm")
}

dependencies {
	implementation(project(":runtime:csi-api-server"))
	implementation(project(":sandbox:api:server"))
	
	implementation("ru.capjack.csi:csi-core-server")
	implementation("ru.capjack.csi:csi-transport-netty-server")
	implementation("ru.capjack.tool:tool-logging")
	implementation("ru.capjack.tool:tool-utils")
	implementation("ru.capjack.tool:tool-io")
	
	implementation("ch.qos.logback:logback-classic:1.2.3")
}
