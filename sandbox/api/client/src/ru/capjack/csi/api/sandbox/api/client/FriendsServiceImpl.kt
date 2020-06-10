package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.AbstractService
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.api.log
import ru.capjack.csi.api.logS
import ru.capjack.csi.api.sandbox.api.ApiDecoders
import ru.capjack.csi.api.sandbox.api.User
import ru.capjack.csi.api.sandbox.api.server.FriendsService
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool
import kotlin.coroutines.suspendCoroutine

internal class FriendsServiceImpl(
	serviceId: Int,
	serviceName: String,
	logger: Logger,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
) : AbstractService(serviceId, serviceName, logger, writers, connection, callbacks), FriendsService {
	
	override fun getFriends(offset: Int, limit: Int, callback: (List<User>) -> Unit) {
		val c = registerCallback {
			val p0 = readList(ApiDecoders.ENTITY_User)
			logCallback("getFriends", it) {
				log(p0, LOG_ENTITY_User)
			}
			callback(p0)
		}
		logSend("getFriends", c) {
			logS("offset", offset)
			log("limit", limit)
		}
		send(1, c) {
			writeInt(offset)
			writeInt(limit)
		}
	}
}
