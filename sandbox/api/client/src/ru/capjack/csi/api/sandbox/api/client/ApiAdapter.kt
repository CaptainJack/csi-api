package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.client.AbstractApiAdapter
import ru.capjack.csi.api.client.ApiSluice
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.server.ServerApi
import ru.capjack.csi.api.RealCallbacksRegister

class ApiAdapter(
	sluice: ApiSluice<InternalClientApi, ServerApi>,
	byteBuffers: ObjectPool<ByteBuffer>
) : AbstractApiAdapter<InternalClientApi, ServerApi>(sluice, byteBuffers) {
	
	override fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: InternalClientApi): ConnectionHandler {
		return ApiConnection(messagePool, connection, callbacks, api)
	}
	
	override fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): ServerApi {
		return ServerApiImpl(messagePool.writers, connection, callbacks)
	}
	
	override fun provideCallbacksRegister(): CallbacksRegister {
		return RealCallbacksRegister()
	}
}
