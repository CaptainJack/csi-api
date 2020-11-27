package ru.capjack.csi.api.generator.kotlin

import ru.capjack.tool.biser.generator.CodePath
import ru.capjack.tool.biser.generator.ImportsCollection
import ru.capjack.tool.biser.generator.kotlin.DefaultKotlinGearFactory
import ru.capjack.tool.biser.generator.kotlin.KotlinCodersGenerator
import ru.capjack.tool.biser.generator.kotlin.KotlinReadCallVisitor
import ru.capjack.tool.biser.generator.kotlin.KotlinTypeNameVisitor
import ru.capjack.tool.biser.generator.kotlin.KotlinWriteCallVisitor
import ru.capjack.tool.biser.generator.model.EnumDescriptor
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.StructureType
import ru.capjack.tool.biser.generator.model.TypeVisitor

class ClientJsKotlinCsiApiGenerator(module: String) : KotlinCsiApiGenerator(CodePath(module)) {
	override fun createApiGenerator(sourcePackage: CodePath, codersGenerator: KotlinCodersGenerator): KotlinApiGenerator {
		return ClientJsKotlinApiGenerator(sourcePackage, codersGenerator)
	}
	
	override fun createCodersGenerator(): KotlinCodersGenerator {
		return KotlinCodersGenerator(sourcePackage, true, gearFactory = JsKotlinKotlinGearFactory())
	}
}

class JsKotlinKotlinGearFactory : DefaultKotlinGearFactory() {
	override fun createTypeNameVisitor(targetPackage: CodePath): TypeVisitor<String, ImportsCollection> {
		return JsKotlinTypeNameVisitor(targetPackage)
	}
	
	override fun createReadCallVisitor(innerDecodeNameVisitor: TypeVisitor<String, Unit>): TypeVisitor<String, Unit> {
		return JsKotlinReadCallVisitor(innerDecodeNameVisitor)
	}
	
	override fun createWriteCallVisitor(innerEncoderNameVisitor: TypeVisitor<String, Unit>): TypeVisitor<String, String> {
		return JsKotlinWriteCallVisitor(innerEncoderNameVisitor)
	}
}

class JsKotlinTypeNameVisitor(targetPackage: CodePath) : KotlinTypeNameVisitor(targetPackage) {
	override fun visitListType(type: ListType, data: ImportsCollection): String {
		return "Array<${type.element.accept(this, data)}>"
	}
}

class JsKotlinReadCallVisitor(names: TypeVisitor<String, Unit>) : KotlinReadCallVisitor(names) {
	override fun visitPrimitiveType(type: PrimitiveType, data: Unit): String {
		if (type == PrimitiveType.LONG) {
			return "readJsLong()"
		}
		return super.visitPrimitiveType(type, data)
	}
	
	override fun visitListType(type: ListType, data: Unit): String {
		return "readArray(${type.element.accept(names)})"
	}
}

class JsKotlinWriteCallVisitor(names: TypeVisitor<String, Unit>) : KotlinWriteCallVisitor(names) {
	
	override fun visitPrimitiveType(type: PrimitiveType, data: String): String {
		if (type == PrimitiveType.LONG) {
			return "writeJsLong($data)"
		}
		return super.visitPrimitiveType(type, data)
	}
	
	override fun visitListType(type: ListType, data: String): String {
		return "writeArray($data, ${type.element.accept(names)})"
	}
}