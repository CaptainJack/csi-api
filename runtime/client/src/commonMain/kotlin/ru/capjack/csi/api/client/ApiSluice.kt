package ru.capjack.csi.api.client

import ru.capjack.csi.core.client.ConnectFailReason

interface ApiSluice<IA : InternalApi, OA : Any> {
	fun connect(outerApi: OA): IA
	
	fun fail(reason: ConnectFailReason)
}