import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
	kotlin("multiplatform") version "1.3.50" apply false
	id("ru.capjack.depver") version "0.2.2"
	id("ru.capjack.logging") version "0.14.7"
	id("ru.capjack.bintray") version "0.20.1"
	id("nebula.release") version "12.0.0"
}

depver {
	"ru.capjack.csi:csi-transport-*"("0.1.0-dev.3+6e89523")
}

subprojects {
	group = "ru.capjack.csi"
	
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
	}
	
	tasks.withType<KotlinJvmCompile> {
		kotlinOptions.jvmTarget = "1.8"
	}
}
