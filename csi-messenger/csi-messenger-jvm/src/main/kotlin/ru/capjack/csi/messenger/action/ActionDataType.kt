package ru.capjack.csi.messenger.action

import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
actual class ActionDataType<T : Any>(obj: Any, arg: Int) {
	val kType: KType = obj::class.supertypes[0].arguments[arg].type!!
	val kClass: KClass<T> = kType.classifier!! as KClass<T>
}