package ru.capjack.csi.api

import kotlin.js.JsName

interface BaseInnerApi {
	@JsName("handleConnectionClose")
	fun handleConnectionClose()
}