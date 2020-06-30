package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.RealCallbacksRegister
import ru.capjack.csi.api.client.AbstractApiAdapter
import ru.capjack.csi.api.client.ApiSluice
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ObjectPool

class ApiAdapter(
	sluice: ApiSluice<InternalClientApi, InternalServerApi>,
	byteBuffers: ObjectPool<ByteBuffer>
) : AbstractApiAdapter<InternalClientApi, InternalServerApi>(sluice, byteBuffers) {
	
	private val logger = Logging.getLogger("ru.capjack.csi.api.sandbox.api.client")
	
	override fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: InternalClientApi): ConnectionHandler {
		return ApiConnection(logger, messagePool, connection, callbacks, api)
	}
	
	override fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): InternalServerApi {
		return InternalServerApiImpl(logger, messagePool.writers, connection, callbacks)
	}
	
	override fun provideCallbacksRegister(): CallbacksRegister {
		return RealCallbacksRegister()
	}
}
