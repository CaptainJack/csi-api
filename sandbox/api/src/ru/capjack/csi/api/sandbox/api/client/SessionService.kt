package ru.capjack.csi.api.sandbox.api.client

interface SessionService {
	
	fun updateUserCoins(value: Long)
	
	fun updateUserLevel(value: Long)
	
	fun askQuestion(question: String, receiver: (success: Boolean, answer: String) -> Unit)
	
	fun askQuestionAgain(question: String, receiver: (success: Boolean, answer: String) -> Unit)
}