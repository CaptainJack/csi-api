package ru.capjack.csi.api.client

import ru.capjack.csi.api.BaseInnerApi
import ru.capjack.csi.core.client.ConnectionRecoveryHandler

interface InnerApi : BaseInnerApi {
	fun handleConnectionCloseTimeout(seconds: Int)
	
	fun handleConnectionLost(): ConnectionRecoveryHandler
}