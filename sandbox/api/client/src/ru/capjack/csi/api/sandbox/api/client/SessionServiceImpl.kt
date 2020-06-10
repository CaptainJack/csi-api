package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.sandbox.api.server.SessionService
import ru.capjack.csi.api.AbstractService
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.log
import ru.capjack.csi.api.sandbox.api.SessionUser
import ru.capjack.csi.api.sandbox.api.ApiDecoders

internal class SessionServiceImpl(
	serviceId: Int,
	serviceName: String,
	logger: Logger,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
) : AbstractService(serviceId, serviceName, logger, writers, connection, callbacks), SessionService {
	
	override fun addCoins(value: Long) {
		logSend("addCoins") {
			log("value", value)
		}
		send(2) {
			writeLong(value)
		}
	}
	
	override fun getUser(callback: (SessionUser) -> Unit) {
		val c = registerCallback{
			val p0 = read(ApiDecoders.ENTITY_SessionUser)
			logCallback("getUser", it) {
				log(p0, LOG_ENTITY_SessionUser)
			}
			callback(p0)
		}
		logSend("getUser", c) {
		}
		send(3, c)
	}
}
