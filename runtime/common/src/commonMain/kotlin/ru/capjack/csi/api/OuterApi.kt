package ru.capjack.csi.api

import kotlin.js.JsName

interface OuterApi {
	@JsName("closeConnection")
	fun closeConnection()
}