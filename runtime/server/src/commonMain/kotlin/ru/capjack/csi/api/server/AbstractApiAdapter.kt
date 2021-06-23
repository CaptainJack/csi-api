package ru.capjack.csi.api.server

import kotlinx.coroutines.CoroutineScope
import ru.capjack.csi.api.BaseApiAdapter
import ru.capjack.csi.api.OuterApi
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.pool.ObjectPool

abstract class AbstractApiAdapter<I : Any, IA : InnerApi, OA : OuterApi>(
	private val sluice: ApiSluice<I, IA, OA>,
	coroutineScope: CoroutineScope,
	byteBuffers: ObjectPool<ByteBuffer>
) : BaseApiAdapter<IA, OA, ConnectionHandler>(coroutineScope, byteBuffers), ConnectionAcceptor<I> {
	
	override fun acceptConnection(identity: I, connection: Connection): ConnectionHandler {
		val context = createContext(connection)
		val outerApi = createOuterApi(context)
		val innerApi = sluice.connect(identity, outerApi)
		return createConnectionHandler(context, innerApi)
	}
}