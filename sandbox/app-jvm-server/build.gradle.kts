plugins {
	kotlin("jvm")
}

dependencies {
	implementation(project(":sandbox:app-jvm-common"))
	implementation(project(":sandbox:api:server"))
	
	implementation("ru.capjack.csi:csi-core-server")
	implementation("ru.capjack.csi:csi-transport-netty-server")
}
