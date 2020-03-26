plugins {
	kotlin("jvm")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	
	implementation(project(":csi-api-generator"))
	implementation("ch.qos.logback:logback-classic:1.2.3")
}