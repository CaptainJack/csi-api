plugins {
	kotlin("jvm")
}

dependencies {
	implementation(project(":sandbox:app-jvm-common"))
	implementation(project(":sandbox:api:client"))
	
	implementation("ru.capjack.csi:csi-core-client")
	implementation("ru.capjack.csi:csi-transport-netty-client")
}
