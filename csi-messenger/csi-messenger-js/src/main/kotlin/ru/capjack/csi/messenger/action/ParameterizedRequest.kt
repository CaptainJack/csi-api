package ru.capjack.csi.messenger.action

actual abstract class ParameterizedRequest<P : Any, R : Any> : Action() {
	actual val parameterType: ActionDataType<P> = ActionDataType()
	actual val responseType: ActionDataType<R> = ActionDataType()
}