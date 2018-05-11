package ru.capjack.csi.messenger

import ru.capjack.csi.messenger.action.Notice
import ru.capjack.csi.messenger.action.ParameterizedNotice
import ru.capjack.csi.messenger.action.ParameterizedRequest
import ru.capjack.csi.messenger.action.Request

interface Messenger {
	fun send(notice: Notice)
	
	fun <R : Any> send(request: Request<R>, receiver: (R) -> Unit)
	
	fun <P : Any> send(notice: ParameterizedNotice<P>, parameter: P)
	
	fun <P : Any, R : Any> send(request: ParameterizedRequest<P, R>, parameter: P, receiver: (R) -> Unit)
	
	fun handle(notice: Notice, handler: () -> Unit)
	
	fun <P : Any> handle(notice: ParameterizedNotice<P>, handler: (P) -> Unit)
	
	fun <R : Any> handle(request: Request<R>, handler: () -> R)
	
	fun <P : Any, R : Any> handle(request: ParameterizedRequest<P, R>, handler: (P) -> R)
	
	fun <R : Any> handle(request: Request<R>, handler: (Response<R>) -> Unit)
	
	fun <P : Any, R : Any> handle(request: ParameterizedRequest<P, R>, handler: (P, Response<R>) -> Unit)
	
	fun repair()
}