package ru.capjack.csi.messenger.action


actual abstract class ParameterizedNotice<P : Any> : Action() {
	actual val parameterType: ActionDataType<P> = ActionDataType()
}