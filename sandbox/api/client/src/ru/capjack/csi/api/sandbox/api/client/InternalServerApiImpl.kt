package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.AbstractOuterApi
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.api.sandbox.api.server.FriendsService
import ru.capjack.csi.api.sandbox.api.server.SessionService
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class InternalServerApiImpl(
	logger: Logger,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
): AbstractOuterApi(connection), InternalServerApi {
	override val session: SessionService = SessionServiceImpl(1, "session", logger, writers, connection, callbacks)
	override val fiends: FriendsService = FriendsServiceImpl(2, "fiends", logger, writers, connection, callbacks)
}
