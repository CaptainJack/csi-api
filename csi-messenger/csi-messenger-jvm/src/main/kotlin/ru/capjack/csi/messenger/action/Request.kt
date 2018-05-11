package ru.capjack.csi.messenger.action

actual abstract class Request<R : Any> : Action() {
	actual val responseType: ActionDataType<R> = ActionDataType(this, 0)
}