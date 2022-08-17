package ru.capjack.csi.api.generator

import ru.capjack.tool.biser.generator.model.Type

internal interface TypeCollector {
	fun add(type: Type)
}