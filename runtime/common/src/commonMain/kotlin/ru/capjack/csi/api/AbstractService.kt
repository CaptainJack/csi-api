package ru.capjack.csi.api

import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.tool.io.biser.BiserWriter
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.tool.utils.concurrency.use

abstract class AbstractService(
	private val serviceId: Int,
	protected val writers: ObjectPool<OutputApiMessage>,
	private val connection: Connection,
	private val callbacks: CallbacksRegister
) {
	protected inline fun send(methodId: Int, noinline callback: BiserReader.() -> Unit, data: BiserWriter.() -> Unit) {
		send(methodId) {
			writeInt(registerCallback(callback))
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
	
	protected fun registerCallback(callback: BiserReader.() -> Unit): Int {
		return callbacks.put(callback)
	}
	
	protected fun prepareMessage(methodId: Int, message: BiserWriter) {
		message.writeInt(serviceId)
		message.writeInt(methodId)
	}
	
	protected fun sendMessage(message: InputByteBuffer) {
		connection.sendMessage(message)
	}
}