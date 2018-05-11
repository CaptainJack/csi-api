package ru.capjack.csi.messenger.action

expect abstract class ParameterizedNotice<P : Any>() : Action {
	val parameterType: ActionDataType<P>
}