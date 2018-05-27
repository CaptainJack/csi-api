import org.jetbrains.kotlin.gradle.plugin.Kotlin2JsPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("kotlin") version "1.2.41" apply false
	id("ru.capjack.degos.publish") version "1.6.0" apply false
	id("nebula.release") version "6.0.0"
}

subprojects {
	group = "ru.capjack.csi"
	
	repositories.maven("http://artifactory.capjack.ru/public")
	
	plugins.withType<KotlinPluginWrapper> {
		configure<JavaPluginConvention> {
			sourceCompatibility = JavaVersion.VERSION_1_8
		}
		tasks.withType<KotlinCompile> {
			kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
		}
	}

	plugins.withType<Kotlin2JsPluginWrapper> {
		tasks.withType<Kotlin2JsCompile> {
			kotlinOptions {
				moduleKind = "amd"
				sourceMap = true
				sourceMapEmbedSources = "always"
			}
		}
	}
}