package ru.capjack.csi.api.server

interface ApiSluice<I : Any, IA : InternalApi, OA : Any> {
	fun connect(identity: I, outerApi: OA): IA
}