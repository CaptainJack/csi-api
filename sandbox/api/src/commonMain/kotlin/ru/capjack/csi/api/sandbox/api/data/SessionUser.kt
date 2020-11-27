package ru.capjack.csi.api.sandbox.api.data

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
class SessionUser(
	id: Long,
	name: String?,
	var coins: Long
) : User(id, name)