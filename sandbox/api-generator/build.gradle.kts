plugins {
	kotlin("jvm")
}

dependencies {
	implementation(project(":csi-api-generator"))
	implementation("ch.qos.logback:logback-classic:1.2.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}