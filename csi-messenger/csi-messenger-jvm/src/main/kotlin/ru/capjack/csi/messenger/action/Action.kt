package ru.capjack.csi.messenger.action

actual abstract class Action {
	actual val name: String = this::class.simpleName!!
}