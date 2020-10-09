package ru.capjack.csi.api.server

import ru.capjack.csi.api.BaseApiAdapter
import ru.capjack.csi.api.OuterApi
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.pool.ObjectPool

abstract class AbstractApiAdapter<I : Any, IA : InnerApi, OA : OuterApi>(
	private val sluice: ApiSluice<I, IA, OA>,
	byteBuffers: ObjectPool<ByteBuffer>
) : BaseApiAdapter<IA, OA, ConnectionHandler>(byteBuffers), ConnectionAcceptor<I> {
	
	override fun acceptConnection(identity: I, connection: Connection): ConnectionHandler {
		val callbacks = provideCallbacksRegister()
		val outerApi = createOuterApi(connection, callbacks)
		val innerApi = sluice.connect(identity, outerApi)
		return createConnectionHandler(connection, callbacks, innerApi)
	}
}