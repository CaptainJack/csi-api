package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.RealCallbacksRegister
import ru.capjack.csi.api.server.AbstractApiAdapter
import ru.capjack.csi.api.server.ApiSluice
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ObjectPool

class ApiAdapter<I : Any>(
	sluice: ApiSluice<I, InternalServerApi, InternalClientApi>,
	byteBuffers: ObjectPool<ByteBuffer>
) : AbstractApiAdapter<I, InternalServerApi, InternalClientApi>(sluice, byteBuffers) {
	
	private val logger = Logging.getLogger("ru.capjack.csi.api.sandbox.api.server")
	
	override fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: InternalServerApi): ConnectionHandler {
		return ApiConnection(logger, messagePool, connection, callbacks, api)
	}
	
	override fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): InternalClientApi {
		return InternalClientApiImpl(logger, messagePool.writers, connection, callbacks)
	}
	
	override fun provideCallbacksRegister(): CallbacksRegister {
		return RealCallbacksRegister()
	}
}
