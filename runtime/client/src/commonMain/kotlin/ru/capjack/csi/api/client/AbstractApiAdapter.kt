package ru.capjack.csi.api.client

import kotlinx.coroutines.CoroutineScope
import ru.capjack.csi.api.BaseApiAdapter
import ru.capjack.csi.api.OuterApi
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.pool.ObjectPool

abstract class AbstractApiAdapter<IA : InnerApi, OA : OuterApi>(
	private val sluice: ApiSluice<IA, OA>,
	coroutineScope: CoroutineScope,
	byteBuffers: ObjectPool<ByteBuffer>
) : BaseApiAdapter<IA, OA, ConnectionHandler>(coroutineScope, byteBuffers), ConnectionAcceptor {
	
	override fun acceptConnection(connection: Connection): ConnectionHandler {
		val context = createContext(connection)
		val outerApi = createOuterApi(context)
		val innerApi = sluice.connect(outerApi)
		return createConnectionHandler(context, innerApi)
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		sluice.fail(reason)
	}
}