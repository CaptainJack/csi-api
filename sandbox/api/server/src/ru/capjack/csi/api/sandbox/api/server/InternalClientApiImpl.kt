package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.AbstractOuterApi
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.api.sandbox.api.client.SessionService
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class InternalClientApiImpl(
	logger: Logger,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
): AbstractOuterApi(connection), InternalClientApi {
	override val session: SessionService = SessionServiceImpl(1, "session", logger, writers, connection, callbacks)
}
