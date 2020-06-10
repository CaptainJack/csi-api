package ru.capjack.csi.api.server

import ru.capjack.csi.api.OuterApi

interface ApiSluice<I : Any, IA : InnerApi, OA : OuterApi> {
	fun connect(identity: I, client: OA): IA
}