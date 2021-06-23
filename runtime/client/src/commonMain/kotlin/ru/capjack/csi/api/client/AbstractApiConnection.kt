package ru.capjack.csi.api.client

import ru.capjack.csi.api.BaseApiConnection
import ru.capjack.csi.api.Context
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.csi.core.client.ConnectionRecoveryHandler

abstract class AbstractApiConnection<IA : InnerApi>(
	context: Context,
	api: IA
) : BaseApiConnection<IA>(context, api), ConnectionHandler {
	override fun handleConnectionCloseTimeout(seconds: Int) {
		api.handleConnectionCloseTimeout(seconds)
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		return api.handleConnectionLost()
	}
}