plugins {
	id("kotlin2js")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation(project(":csi-messenger:csi-messenger-js"))
}