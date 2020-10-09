package ru.capjack.csi.api

import ru.capjack.csi.core.BaseConnectionHandler
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.tool.io.biser.BiserWriter
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.debug
import ru.capjack.tool.logging.error
import ru.capjack.tool.utils.pool.use

abstract class BaseApiConnection<IA : BaseInnerApi>(
	protected val logger: Logger,
	private val messagePool: ApiMessagePool,
	private val connection: Connection,
	private val callbacks: CallbacksRegister,
	protected val api: IA
) : BaseConnectionHandler {
	
	protected val writers
		get() = messagePool.writers
	
	init {
		logger.debug { "[${connection.loggingName}] Open" }
	}
	
	override fun handleConnectionMessage(message: InputByteBuffer) {
		messagePool.readers.use { reader ->
			reader.buffer = message
			
			val serviceId = reader.readInt()
			val methodOrResponseId = reader.readInt()
			
			if (serviceId == 0) {
				callbacks.take(methodOrResponseId)?.invoke(reader, methodOrResponseId)
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
		logger.debug { "[${connection.loggingName}] Close" }
		try {
			api.handleConnectionClose()
		}
		catch (e: Throwable) {
			logger.error(e) { "[${connection.loggingName}] Error on close" }
		}
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
		/* TODO Legacy
		message.writeInt(0)
		message.writeInt(responseId)
		*/
		message.writeInt(responseId)
	}
	
	protected fun sendResponseMessage(message: InputByteBuffer) {
		connection.sendMessage(message)
	}
	
	
	protected inline fun logCallback(service: String, method: String, callback: Int, data: StringBuilder.() -> Unit) {
		if (logger.debugEnabled) {
			prepareLogCallback(service, method, callback).apply {
				append('(')
				data()
				append(')')
				logger.debug(toString())
			}
		}
	}
	
	protected inline fun logReceive(service: String, method: String, data: StringBuilder.() -> Unit) {
		if (logger.debugEnabled) {
			prepareLogReceive(service, method).apply {
				append('(')
				data()
				append(')')
				logger.debug(toString())
			}
		}
	}
	
	protected inline fun logReceive(service: String, method: String, callback: Int, data: StringBuilder.() -> Unit) {
		if (logger.debugEnabled) {
			prepareLogReceive(service, method).apply {
				append('[')
				append(callback)
				append(']')
				append('(')
				data()
				append(')')
				logger.debug(toString())
			}
		}
	}
	
	protected fun prepareLogReceive(service: String, method: String): StringBuilder {
		return StringBuilder()
			.append('[').append(connection.loggingName).append("] -> ")
			.append(service).append('.').append(method)
	}
	
	protected fun prepareLogCallback(service: String, method: String, callback: Int): StringBuilder {
		return StringBuilder()
			.append('[').append(connection.loggingName).append("] <~ ")
			.append(service)
			.append('.')
			.append(method)
			.append('[')
			.append(callback)
			.append(']')
	}
}

