package ru.capjack.csi.api.sandbox.api.client

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
interface SessionService {
	fun updateUserCoins(value: Long)
	
	fun askQuestion(question: String, callback: (success: Boolean, answer: String) -> Unit)
	
	fun askQuestionAgain(question: String, callback: (success: Boolean, answer: String) -> Unit)
}