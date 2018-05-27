package test

import ru.capjack.csi.core.Service

fun main(args: Array<String>) {
	
	val c = "00123456789\n\r\t\n\t\r\n0±!@#$%^&*()_+<>,./?{}[];\'\\:\"|00"
	
	println(c.toByteArray().size)
	println(c.toByteArray().map { it.toInt().and(0xFF).toString(16).toUpperCase() }.joinToString(", 0x"))
	
	
//	val conversation = ConversationImpl()
//
//	val userService = conversation.provideSender(UserService::class)
//
//	userService.getUser(1)
}


interface UserService : Service {
	fun getUser(id: Int)
}

enum class A {
	B, C
}