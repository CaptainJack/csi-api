package ru.capjack.csi.api.server

import ru.capjack.csi.api.ApiMessagePool
import ru.capjack.csi.api.BaseApiConnection
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.logging.Logger

abstract class AbstractApiConnection<IA : InnerApi>(
	logger: Logger,
	messagePool: ApiMessagePool,
	connection: Connection,
	callbacks: CallbacksRegister,
	api: IA
) : BaseApiConnection<IA>(logger, messagePool, connection, callbacks, api), ConnectionHandler