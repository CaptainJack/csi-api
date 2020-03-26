package ru.capjack.csi.api.client

import ru.capjack.csi.api.ApiMessagePool
import ru.capjack.csi.api.BaseApiConnection
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.csi.core.client.ConnectionRecoveryHandler

abstract class AbstractApiConnection<IA : InternalApi>(
	messagePool: ApiMessagePool,
	connection: Connection,
	callbacks: CallbacksRegister,
	api: IA
) : BaseApiConnection<IA>(messagePool, connection, callbacks, api), ConnectionHandler {
	override fun handleConnectionCloseTimeout(seconds: Int) {
		api.handleConnectionCloseTimeout(seconds)
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		return api.handleConnectionLost()
	}
}