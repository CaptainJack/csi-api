package ru.capjack.csi.messenger.action

expect abstract class ParameterizedRequest<P : Any, R : Any>() : Action {
	val parameterType: ActionDataType<P>
	val responseType: ActionDataType<R>
}

