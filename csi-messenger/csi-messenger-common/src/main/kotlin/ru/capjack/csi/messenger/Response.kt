package ru.capjack.csi.messenger

interface Response<R> {
	fun send(data: R)
}