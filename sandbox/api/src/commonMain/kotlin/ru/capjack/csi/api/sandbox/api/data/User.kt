package ru.capjack.csi.api.sandbox.api.data

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
open class User(
	val id: Long,
	val name: String?,
	val rank: Rank = Rank.JUNIOR
) {
	
	sealed class Rank {
		object JUNIOR : Rank()
		object MAJOR : Rank()
		object SENIOR : Rank()
	}
}
