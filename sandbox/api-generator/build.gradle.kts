plugins {
	kotlin("jvm")
}

dependencies {
	implementation(project(":csi-api-generator"))
	implementation("ch.qos.logback:logback-classic:1.2.3")
}