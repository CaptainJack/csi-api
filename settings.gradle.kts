rootProject.name = "csi"

include(
	"csi-messenger:csi-messenger-common",
	"csi-messenger:csi-messenger-js",
	"csi-messenger:csi-messenger-jvm",
	
	"csi-client:csi-client-js",
	"csi-server:csi-server-jvm"
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