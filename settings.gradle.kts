rootProject.name = "csi"

include(
	"csi-core:csi-core-common",
	"csi-core:csi-core-js",
	"csi-core:csi-core-jvm",
	
	"csi-client:csi-client-js",
	"csi-server:csi-server-jvm",
	
	"test:test-jvm"
)


pluginManagement {
	repositories.maven("http://artifactory.capjack.ru/public")
	resolutionStrategy.eachPlugin {
		val id = requested.id.id
		when {
			id.startsWith("kotlin")            ->
				useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
			id.startsWith("ru.capjack.degos.") ->
				useModule("ru.capjack.degos:degos-${id.substringAfterLast('.')}:${requested.version}")
		}
	}
}