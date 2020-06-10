@file:Suppress("unused", "UNUSED_PARAMETER")

package ru.capjack.csi.api

fun StringBuilder.log(arg: String, value: Boolean) = logPrefix(arg).append(value)
fun StringBuilder.log(arg: String, value: Byte) = logPrefix(arg).append(value)
fun StringBuilder.log(arg: String, value: Int) = logPrefix(arg).append(value)
fun StringBuilder.log(arg: String, value: Double) = logPrefix(arg).append(value)
fun StringBuilder.log(arg: String, value: Long) = logPrefix(arg).append(value)
fun StringBuilder.log(arg: String, value: String) = logPrefix(arg).append(value)
fun StringBuilder.log(arg: String, value: BooleanArray) = logPrefix(arg).apply { value.joinTo(this, SEP, "[", "]") }
fun StringBuilder.log(arg: String, value: ByteArray) = logPrefix(arg).apply { value.joinTo(this, SEP, "[", "]") }
fun StringBuilder.log(arg: String, value: IntArray) = logPrefix(arg).apply { value.joinTo(this, SEP, "[", "]") }
fun StringBuilder.log(arg: String, value: DoubleArray) = logPrefix(arg).apply { value.joinTo(this, SEP, "[", "]") }
fun StringBuilder.log(arg: String, value: LongArray) = logPrefix(arg).apply { value.joinTo(this, SEP, "[", "]") }

fun StringBuilder.log(value: Boolean) = append(value)
fun StringBuilder.log(value: Byte) = append(value)
fun StringBuilder.log(value: Int) = append(value)
fun StringBuilder.log(value: Double) = append(value)
fun StringBuilder.log(value: Long) = append(value)
fun StringBuilder.log(value: String) = append(value)
fun StringBuilder.log(value: BooleanArray) = value.joinTo(this, SEP, "[", "]")
fun StringBuilder.log(value: ByteArray) = value.joinTo(this, SEP, "[", "]")
fun StringBuilder.log(value: IntArray) = value.joinTo(this, SEP, "[", "]")
fun StringBuilder.log(value: DoubleArray) = value.joinTo(this, SEP, "[", "]")
fun StringBuilder.log(value: LongArray) = value.joinTo(this, SEP, "[", "]")

fun StringBuilder.logS(arg: String, value: Boolean) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: Byte) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: Int) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: Double) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: Long) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: String) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: BooleanArray) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: ByteArray) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: IntArray) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: DoubleArray) = log(value).append(SEP)
fun StringBuilder.logS(arg: String, value: LongArray) = log(value).append(SEP)

fun StringBuilder.logS(value: Boolean) = append(value).append(SEP)
fun StringBuilder.logS(value: Byte) = append(value).append(SEP)
fun StringBuilder.logS(value: Int) = append(value).append(SEP)
fun StringBuilder.logS(value: Double) = append(value).append(SEP)
fun StringBuilder.logS(value: Long) = append(value).append(SEP)
fun StringBuilder.logS(value: String) = append(value).append(SEP)
fun StringBuilder.logS(value: BooleanArray) = append(value).append(SEP)
fun StringBuilder.logS(value: ByteArray) = append(value).append(SEP)
fun StringBuilder.logS(value: IntArray) = append(value).append(SEP)
fun StringBuilder.logS(value: DoubleArray) = append(value).append(SEP)
fun StringBuilder.logS(value: LongArray) = append(value).append(SEP)

fun <T> StringBuilder.log(arg: String, value: List<T>, log: StringBuilder.(T) -> Unit) = logPrefix(arg).log(value, log)
fun <T> StringBuilder.logS(arg: String, value: List<T>, log: StringBuilder.(T) -> Unit) = log(arg, value, log).append(SEP)

fun <T> StringBuilder.log(value: List<T>, log: StringBuilder.(T) -> Unit): StringBuilder {
	append('[')
	value.forEach { log(it) }
	append(']')
	return this
}
fun <T> StringBuilder.logS(value: List<T>, log: StringBuilder.(T) -> Unit) = log(value, log).append(SEP)

fun <T> StringBuilder.log(arg: String, value: T, log: StringBuilder.(T) -> Unit) = logPrefix(arg).log(value, log)
fun <T> StringBuilder.logS(arg: String, value: T, log: StringBuilder.(T) -> Unit) = log(arg, value, log).append(SEP)

fun <T> StringBuilder.log(value: T, log: StringBuilder.(T) -> Unit) = apply {  log(value)}
fun <T> StringBuilder.logS(value: T, log: StringBuilder.(T) -> Unit) = log(value, log).append(SEP)


private const val SEP = ", "
private fun StringBuilder.logPrefix(arg: String) = append(arg).append(": ")