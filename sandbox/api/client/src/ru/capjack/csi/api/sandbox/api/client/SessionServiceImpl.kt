package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.sandbox.api.server.SessionService
import ru.capjack.csi.api.AbstractService
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.SessionUser
import ru.capjack.csi.api.sandbox.api.ApiDecoders

internal class SessionServiceImpl(
	serviceId: Int,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
) : AbstractService(serviceId, writers, connection, callbacks), SessionService {
	
	override fun getUser(receiver: (user: SessionUser) -> Unit) {
		send(1, {
			val p0 = read(ApiDecoders.ENTITY_SessionUser)
			receiver(p0)
		}) {
		}
	}
	
	override fun addCoins(value: Long) {
		send(2) {
			writeLong(value)
		}
	}
}
