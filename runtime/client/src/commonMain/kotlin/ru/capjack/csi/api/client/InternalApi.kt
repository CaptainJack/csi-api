package ru.capjack.csi.api.client

import ru.capjack.csi.api.BaseInternalApi
import ru.capjack.csi.core.client.ConnectionRecoveryHandler

interface InternalApi : BaseInternalApi {
	fun handleConnectionCloseTimeout(seconds: Int)
	
	fun handleConnectionLost(): ConnectionRecoveryHandler
}