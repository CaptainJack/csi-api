package ru.capjack.csi.api

import ru.capjack.csi.core.BaseConnectionHandler
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.tool.io.biser.BiserWriter
import ru.capjack.tool.utils.concurrency.use

abstract class BaseApiConnection<IA : BaseInternalApi>(
	private val messagePool: ApiMessagePool,
	private val connection: Connection,
	private val callbacks: CallbacksRegister,
	protected val api: IA
) : BaseConnectionHandler {
	
	protected val writers
		get() = messagePool.writers
	
	override fun handleConnectionMessage(message: InputByteBuffer) {
		messagePool.readers.use { reader ->
			reader.buffer = message
			
			val serviceId = reader.readInt()
			val methodOrResponseId = reader.readInt()
			
			if (serviceId == 0) {
				callbacks.take(methodOrResponseId)?.invoke(reader)
					?: throw ProtocolBrokenException("Response an unknown callback $methodOrResponseId")
			}
			else {
				val methodExists = call(serviceId, methodOrResponseId, reader)
				if (!methodExists) {
					throw ProtocolBrokenException("Calling an unknown method $serviceId.$methodOrResponseId")
				}
			}
		}
	}
	
	override fun handleConnectionClose() {
		api.handleConnectionClose()
	}
	
	protected abstract fun call(serviceId: Int, methodId: Int, message: BiserReader): Boolean
	
	protected inline fun sendResponse(responseId: Int, data: BiserWriter.() -> Unit) {
		writers.use { message ->
			val writer = message.writer
			prepareResponseMessage(responseId, writer)
			writer.data()
			sendResponseMessage(message.buffer)
		}
	}
	
	protected fun prepareResponseMessage(responseId: Int, message: BiserWriter) {
		message.writeInt(0)
		message.writeInt(responseId)
	}
	
	protected fun sendResponseMessage(message: InputByteBuffer) {
		connection.sendMessage(message)
	}
}

