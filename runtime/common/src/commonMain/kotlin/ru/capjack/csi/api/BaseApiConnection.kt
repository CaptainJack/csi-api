package ru.capjack.csi.api

import ru.capjack.csi.core.BaseConnectionHandler
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.alsoFalse
import ru.capjack.tool.logging.debug
import ru.capjack.tool.utils.pool.use

abstract class BaseApiConnection<IA : BaseInnerApi>(
	protected val context: Context,
	protected val api: IA
) : BaseConnectionHandler {
	
	init {
		context.logger.debug { "Open" }
	}
	
	override fun handleConnectionMessage(message: InputByteBuffer) {
		context.messagePool.readers.use { reader ->
			reader.buffer = message
			
			val messageType = when (val messageTypeCode = reader.readByte()) {
				ApiMessageType.METHOD_CALL.code          -> ApiMessageType.METHOD_CALL
				ApiMessageType.METHOD_RESPONSE.code      -> ApiMessageType.METHOD_RESPONSE
				ApiMessageType.SUBSCRIPTION_CALL.code    -> ApiMessageType.SUBSCRIPTION_CALL
				ApiMessageType.SUBSCRIPTION_CANCEL.code  -> ApiMessageType.SUBSCRIPTION_CANCEL
				ApiMessageType.INSTANCE_METHOD_CALL.code -> ApiMessageType.INSTANCE_METHOD_CALL
				ApiMessageType.INSTANCE_CLOSE.code       -> ApiMessageType.INSTANCE_CLOSE
				else                                     -> throw ProtocolBrokenException("Bad api message type code $messageTypeCode")
			}
			
			when (messageType) {
				ApiMessageType.METHOD_CALL          -> {
					val serviceId = reader.readInt()
					val methodId = reader.readInt()
					(findService(serviceId)?.callMethod(methodId, reader) ?: false).alsoFalse {
						throw ProtocolBrokenException("Calling an unknown service $serviceId.$methodId")
					}
				}
				ApiMessageType.INSTANCE_METHOD_CALL -> {
					val serviceId = reader.readInt()
					val methodId = reader.readInt()
					(context.innerInstanceServices.get(serviceId)?.callMethod(methodId, reader) ?: false).alsoFalse {
						throw ProtocolBrokenException("Calling an unknown service $serviceId.$methodId")
					}
				}
				ApiMessageType.METHOD_RESPONSE      -> {
					val responseId = reader.readInt()
					context.callbacks.take(responseId)?.invoke(reader, responseId)
						?: throw ProtocolBrokenException("Response an unknown callback $responseId")
				}
				ApiMessageType.SUBSCRIPTION_CALL    -> {
					val subscriptionId = reader.readInt()
					val argumentId = reader.readInt()
					(context.innerSubscriptions.get(subscriptionId)?.call(argumentId, reader) ?: false).alsoFalse {
						context.logger.warn("Calling an unknown subscription $subscriptionId.$argumentId")
						message.skipRead()
					}
				}
				ApiMessageType.SUBSCRIPTION_CANCEL  -> {
					val subscriptionId = reader.readInt()
					context.outerSubscriptions.cancel(subscriptionId).alsoFalse {
						throw ProtocolBrokenException("Calling an unknown subscription $subscriptionId")
					}
				}
				ApiMessageType.INSTANCE_CLOSE       -> {
					val serviceId = reader.readInt()
					context.innerInstanceServices.close(serviceId).alsoFalse {
						throw ProtocolBrokenException("Sub service $serviceId is not exists")
					}
				}
			}
			
		}
	}
	
	override fun handleConnectionClose() {
		context.logger.debug { "Close" }
		
		context.outerSubscriptions.cancelAll()
		context.innerSubscriptions.cancelAll()
		context.innerInstanceServices.closeAll()
		
		try {
			api.handleConnectionClose()
		}
		catch (e: Throwable) {
			context.logger.error("Error on close", e)
		}
	}
	
	protected abstract fun findService(serviceId: Int): InnerServiceDelegate<*>?
}

