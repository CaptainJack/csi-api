package ru.capjack.csi.api.server

import ru.capjack.csi.api.BaseApiConnection
import ru.capjack.csi.api.Context
import ru.capjack.csi.core.server.ConnectionHandler

abstract class AbstractApiConnection<IA : InnerApi>(
	context: Context,
	api: IA
) : BaseApiConnection<IA>(context, api), ConnectionHandler