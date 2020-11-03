package ru.capjack.csi.api.client

import ru.capjack.csi.api.BaseInnerApi
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import kotlin.js.JsName

interface InnerApi : BaseInnerApi {
	@JsName("handleConnectionCloseTimeout")
	fun handleConnectionCloseTimeout(seconds: Int)
	
	@JsName("handleConnectionLost")
	fun handleConnectionLost(): ConnectionRecoveryHandler
}