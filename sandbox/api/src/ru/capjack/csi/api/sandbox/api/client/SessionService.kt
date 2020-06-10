package ru.capjack.csi.api.sandbox.api.client

interface SessionService {
	fun updateUserCoins(value: Long)
	
	fun askQuestion(question: String, callback: (success: Boolean, answer: String) -> Unit)
	
	fun askQuestionAgain(question: String, callback: (success: Boolean, answer: String) -> Unit)
}