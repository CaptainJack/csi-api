package ru.capjack.csi.api.sandbox.api

import ru.capjack.csi.api.sandbox.api.server.FriendsService
import ru.capjack.csi.api.sandbox.api.server.SessionService
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
interface ServerApi {
	val session: SessionService
	val fiends: FriendsService
}