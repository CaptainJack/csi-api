package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.client.ClientApi
import ru.capjack.csi.api.sandbox.api.client.SessionService

internal class ClientApiImpl(writers: ObjectPool<OutputApiMessage>, connection: Connection, callbacks: CallbacksRegister): ClientApi{
	override val session: SessionService = SessionServiceImpl(1, writers, connection, callbacks)
}
