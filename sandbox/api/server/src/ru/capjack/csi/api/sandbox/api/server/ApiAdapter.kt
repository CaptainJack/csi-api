package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.server.AbstractApiAdapter
import ru.capjack.csi.api.server.ApiSluice
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.client.ClientApi
import ru.capjack.csi.api.RealCallbacksRegister

class ApiAdapter<I : Any>(
	sluice: ApiSluice<I, InternalServerApi, ClientApi>,
	byteBuffers: ObjectPool<ByteBuffer>
) : AbstractApiAdapter<I, InternalServerApi, ClientApi>(sluice, byteBuffers) {
	
	override fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: InternalServerApi): ConnectionHandler {
		return ApiConnection(messagePool, connection, callbacks, api)
	}
	
	override fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): ClientApi {
		return ClientApiImpl(messagePool.writers, connection, callbacks)
	}
	
	override fun provideCallbacksRegister(): CallbacksRegister {
		return RealCallbacksRegister()
	}
}
