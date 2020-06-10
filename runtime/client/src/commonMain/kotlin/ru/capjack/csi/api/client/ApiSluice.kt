package ru.capjack.csi.api.client

import ru.capjack.csi.api.OuterApi
import ru.capjack.csi.core.client.ConnectFailReason

interface ApiSluice<IA : InnerApi, OA : OuterApi> {
	fun connect(server: OA): IA
	
	fun fail(reason: ConnectFailReason)
}