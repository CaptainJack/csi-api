package ru.capjack.csi.messenger.action

expect abstract class Request<R : Any>() : Action {
	val responseType: ActionDataType<R>
}