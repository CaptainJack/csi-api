package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.server.ServerApi
import ru.capjack.csi.api.sandbox.api.server.SessionService
import ru.capjack.csi.api.sandbox.api.server.FriendsService

internal class ServerApiImpl(writers: ObjectPool<OutputApiMessage>, connection: Connection, callbacks: CallbacksRegister): ServerApi{
	override val session: SessionService = SessionServiceImpl(1, writers, connection, callbacks)
	override val fiends: FriendsService = FriendsServiceImpl(2, writers, connection, callbacks)
}
