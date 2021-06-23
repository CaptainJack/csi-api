package ru.capjack.csi.api

import kotlinx.coroutines.CoroutineScope
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger

class Context(
	val logger: Logger,
	val coroutineScope: CoroutineScope,
	val messagePool: ApiMessagePool,
	val connection: Connection,
	val callbacks: CallbacksRegister,
	val innerInstanceServices: InnerServiceHolder,
	val innerSubscriptions: InnerSubscriptionHolder,
	val outerSubscriptions: OuterSubscriptionHolder
)