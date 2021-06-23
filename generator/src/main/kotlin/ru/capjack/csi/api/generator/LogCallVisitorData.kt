package ru.capjack.csi.api.generator

import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.TypeCollector

internal class LogCallVisitorData(
	val loggers: TypeCollector,
	val code: Code,
	val argName: String?,
	val argVal: String,
	val sep: Boolean
)