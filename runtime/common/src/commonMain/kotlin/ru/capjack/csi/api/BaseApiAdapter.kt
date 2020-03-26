package ru.capjack.csi.api

import ru.capjack.csi.core.BaseConnectionHandler
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.ObjectPool

abstract class BaseApiAdapter<IA : BaseInternalApi, OA : Any, CH : BaseConnectionHandler>(byteBuffers: ObjectPool<ByteBuffer>) {
	protected val messagePool: ApiMessagePool = ApiMessagePoolImp(byteBuffers)
	
	protected abstract fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): OA
	
	protected abstract fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: IA): CH
	
	protected abstract fun provideCallbacksRegister(): CallbacksRegister
}


