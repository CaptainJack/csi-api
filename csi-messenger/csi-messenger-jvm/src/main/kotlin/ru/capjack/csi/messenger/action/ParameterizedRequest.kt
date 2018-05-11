package ru.capjack.csi.messenger.action

actual abstract class ParameterizedRequest<P : Any, R : Any> : Action() {
	actual val parameterType: ActionDataType<P> = ActionDataType(this, 0)
	actual val responseType: ActionDataType<R> = ActionDataType(this, 1)
}