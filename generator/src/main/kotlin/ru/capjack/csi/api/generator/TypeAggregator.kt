package ru.capjack.csi.api.generator

import ru.capjack.tool.biser.generator.TypeCollector
import ru.capjack.tool.biser.generator.model.Type

internal class TypeAggregator : TypeCollector, Iterator<Type> {
	private val passedTypes = mutableSetOf<Type>()
	private val remainedTypes = mutableSetOf<Type>()
	
	override fun add(type: Type) {
		if (!passedTypes.contains(type)) {
			remainedTypes.add(type)
		}
	}
	
	override fun hasNext(): Boolean {
		return remainedTypes.isNotEmpty()
	}
	
	override fun next(): Type {
		return remainedTypes.first().also {
			remainedTypes.remove(it)
			passedTypes.add(it)
		}
	}
}