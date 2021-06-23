package ru.capjack.csi.api

import ru.capjack.tool.biser.BiserReader
import ru.capjack.tool.logging.debug
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.pool.use
import kotlin.jvm.Volatile

abstract class InnerSubscription(
	protected val context: Context,
	protected val service: OuterService,
	protected val name: String,
	val id: Int
) : Cancelable {
	
	@Volatile
	private var canceled = false
	
	override fun cancel() {
		if (!canceled) {
			canceled = true
			
			service._removeSubscription(id)
			
			context.logger.debug {
				"-> ${service._name}.$name[~$id] [cancel]"
			}
			
			context.messagePool.writers.use { message ->
				message.writer.apply {
					writeByte(ApiMessageType.SUBSCRIPTION_CANCEL.code)
					writeInt(id)
				}
				context.connection.sendMessage(message.buffer)
			}
			
		}
	}
	
	abstract fun call(argumentId: Int, message: BiserReader): Boolean
	
	protected inline fun logCall(method: String, data: StringBuilder.() -> Unit) {
		context.logger.debug {
			StringBuilder()
				.append("<~ ").append(service._name).append('.').append(name).append("[~").append(id).append("].").append(method)
				.append('(')
				.apply(data)
				.append(')')
				.toString()
		}
	}
}
