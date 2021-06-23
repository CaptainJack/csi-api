package ru.capjack.csi.api

import kotlinx.coroutines.CoroutineScope
import ru.capjack.csi.core.BaseConnectionHandler
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.pool.ObjectPool

abstract class BaseApiAdapter<IA : BaseInnerApi, OA : OuterApi, CH : BaseConnectionHandler>(
	protected val coroutineScope: CoroutineScope,
	byteBuffers: ObjectPool<ByteBuffer>
) {
	protected val messagePool: ApiMessagePool = ApiMessagePoolImp(byteBuffers)
	
	protected abstract fun getLoggerName(): String
	
	protected abstract fun createOuterApi(context: Context): OA
	
	protected abstract fun createConnectionHandler(context: Context, api: IA): CH
	
	protected abstract fun provideCallbacksRegister(): CallbacksRegister
	
	protected fun createContext(connection: Connection): Context {
		return Context(
			Logging.getLogger(getLoggerName()).wrap("[${connection.loggingName}] "),
			coroutineScope,
			messagePool,
			connection,
			provideCallbacksRegister()
		)
	}
}


