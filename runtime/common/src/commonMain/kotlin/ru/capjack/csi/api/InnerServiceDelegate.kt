package ru.capjack.csi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.capjack.tool.biser.BiserReader
import ru.capjack.tool.biser.BiserWriter
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.logging.debug
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.pool.use

abstract class InnerServiceDelegate<S : Any>(
	protected val context: Context,
	protected val service: S,
	var name: String
) {
	abstract fun callMethod(methodId: Int, message: BiserReader): Boolean
	
	fun setup(id: Int) {
		name += "[+$id]"
	}
	
	protected fun <S : Any> registerInstanceService(instance: ServiceInstance<S>, delegate: InnerServiceDelegate<S>): Int {
		return context.innerInstanceServices.add(InnerServiceHolderItem(instance, delegate))
	}
	
	protected fun registerSubscription(subscription: OuterSubscription, cancelable: Cancelable): Int {
		return context.outerSubscriptions.add(subscription, cancelable)
	}
	
	protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit) {
		context.coroutineScope.launch(block = block)
	}
	
	protected inline fun sendMethodResponse(callback: Int, data: BiserWriter.() -> Unit) {
		context.messagePool.writers.use { message ->
			val writer = message.writer
			prepareMethodResponseMessage(callback, writer)
			writer.data()
			sendResponseMessage(message.buffer)
		}
	}
	
	protected fun sendInstanceServiceResponse(callback: Int, serviceId: Int) {
		sendMethodResponse(callback) {
			writeInt(serviceId)
		}
	}
	
	protected fun sendSubscriptionResponse(callback: Int, subscriptionId: Int) {
		sendMethodResponse(callback) {
			writeInt(subscriptionId)
		}
	}
	
	
	protected fun prepareMethodResponseMessage(callback: Int, message: BiserWriter) {
		message.writeByte(ApiMessageType.METHOD_RESPONSE.code)
		message.writeInt(callback)
	}
	
	protected fun sendResponseMessage(message: InputByteBuffer) {
		context.connection.sendMessage(message)
	}
	
	protected fun logInstanceServiceResponse(method: String, callback: Int, serviceId: Int) {
		logMethodResponse(method, callback) {
			append("+").append(serviceId)
		}
	}
	
	protected fun logSubscriptionResponse(method: String, callback: Int, subscriptionId: Int) {
		logMethodResponse(method, callback) {
			append("~").append(subscriptionId)
		}
	}
	
	protected inline fun logMethodCall(method: String, data: StringBuilder.() -> Unit) {
		context.logger.debug {
			prepareLogMethodCall(method)
				.append('(')
				.apply(data)
				.append(')')
				.toString()
		}
	}
	
	protected inline fun logMethodCall(method: String, callback: Int, data: StringBuilder.() -> Unit) {
		context.logger.debug {
			prepareLogMethodCall(method, callback)
				.append('(')
				.apply(data)
				.append(')')
				.toString()
		}
	}
	
	protected inline fun logMethodResponse(method: String, callback: Int, data: StringBuilder.() -> Unit) {
		context.logger.debug {
			prepareLogMethodResponse(method, callback)
				.append(": ")
				.apply(data)
				.toString()
		}
	}

	protected fun prepareLogMethodCall(method: String): StringBuilder {
		return StringBuilder()
			.append("<- ")
			.append(name)
			.append('.')
			.append(method)
	}
	
	protected fun prepareLogMethodCall(method: String, callback: Int): StringBuilder {
		return StringBuilder()
			.append("<- [").append(callback).append("] ")
			.append(name)
			.append('.')
			.append(method)
	}
	
	protected fun prepareLogMethodResponse(method: String, callback: Int): StringBuilder {
		return StringBuilder()
			.append("~> [").append(callback).append("] ")
			.append(name)
			.append('.')
			.append(method)
	}
	
	internal fun close() {
		context.logger.debug {
			"-> $name [close]"
		}
	}
}