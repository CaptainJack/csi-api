package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.AbstractOuterApi
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.sandbox.api.client.SessionService

internal class InternalClientApiImpl(
	logger: Logger,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
): AbstractOuterApi(connection), InternalClientApi {
	override val session: SessionService = SessionServiceImpl(2, "session", logger, writers, connection, callbacks)
}
