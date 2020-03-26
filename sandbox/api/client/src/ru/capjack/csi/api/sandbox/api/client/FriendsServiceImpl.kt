package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.sandbox.api.server.FriendsService
import ru.capjack.csi.api.AbstractService
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.User
import ru.capjack.csi.api.sandbox.api.ApiDecoders

internal class FriendsServiceImpl(
	serviceId: Int,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
) : AbstractService(serviceId, writers, connection, callbacks), FriendsService {
	
	override fun getFriends(offset: Int, limit: Int, receiver: (List<User>) -> Unit) {
		send(1, {
			val p0 = readList(ApiDecoders.ENTITY_User)
			receiver(p0)
		}) {
			writeInt(offset)
			writeInt(limit)
		}
	}
}
