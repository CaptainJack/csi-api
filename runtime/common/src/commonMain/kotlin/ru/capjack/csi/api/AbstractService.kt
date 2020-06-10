package ru.capjack.csi.api

import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.tool.io.biser.BiserWriter
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.tool.utils.concurrency.use

abstract class AbstractService(
	private val serviceId: Int,
	private val serviceName: String,
	protected val logger: Logger,
	protected val writers: ObjectPool<OutputApiMessage>,
	private val connection: Connection,
	private val callbacks: CallbacksRegister
) {
	protected fun send(methodId: Int, callback: Int) {
		send(methodId, callback) {}
	}
	
	protected fun send(methodId: Int) {
		send(methodId) {}
	}
	
	protected inline fun send(methodId: Int, callback: Int, data: BiserWriter.() -> Unit) {
		send(methodId) {
			writeInt(callback)
			data()
		}
	}
	
	protected inline fun send(methodId: Int, data: BiserWriter.() -> Unit) {
		writers.use { message ->
			val writer = message.writer
			prepareMessage(methodId, writer)
			writer.data()
			sendMessage(message.buffer)
		}
	}
	
	protected fun registerCallback(callback: BiserReader.(Int) -> Unit): Int {
		return callbacks.put(callback)
	}
	
	protected fun prepareMessage(methodId: Int, message: BiserWriter) {
		message.writeInt(0) //TODO Legacy
		
		message.writeInt(serviceId)
		message.writeInt(methodId)
	}
	
	protected fun sendMessage(message: InputByteBuffer) {
		connection.sendMessage(message)
	}
	
	protected inline fun logCallback(method: String, callback: Int, data: StringBuilder.() -> Unit) {
		if (logger.debugEnabled) {
			prepareLogCallback(method, callback).apply {
				append('(')
				data()
				append(')')
				logger.debug(toString())
			}
		}
	}
	
	protected inline fun logSend(method: String, data: StringBuilder.() -> Unit) {
		if (logger.debugEnabled) {
			prepareLogSend(method).apply {
				append('(')
				data()
				append(')')
				logger.debug(toString())
			}
		}
	}
	
	protected inline fun logSend(method: String, callback: Int, data: StringBuilder.() -> Unit) {
		if (logger.debugEnabled) {
			prepareLogSend(method).apply {
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
	
	protected fun prepareLogSend(method: String): StringBuilder {
		return StringBuilder("<- ").append(serviceName).append('.').append(method)
	}
	
	protected fun prepareLogCallback(method: String, callback: Int): StringBuilder {
		return StringBuilder("~> ")
			.append(serviceName)
			.append('.')
			.append(method)
			.append('[')
			.append(callback)
			.append(']')
	}
}