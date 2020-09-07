package ru.capjack.csi.api.generator

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.io.biser.generator.CodeFile
import ru.capjack.tool.io.biser.generator.model.EntityDescriptor
import ru.capjack.tool.io.biser.generator.model.EnumDescriptor
import ru.capjack.tool.io.biser.generator.model.ListType
import ru.capjack.tool.io.biser.generator.model.NullableType
import ru.capjack.tool.io.biser.generator.model.ObjectDescriptor
import ru.capjack.tool.io.biser.generator.model.PrimitiveType
import ru.capjack.tool.io.biser.generator.model.StructureDescriptor
import ru.capjack.tool.io.biser.generator.model.StructureType
import ru.capjack.tool.io.biser.generator.model.TypeVisitor
import java.nio.file.Files
import java.nio.file.Path

class JsLegacyCsiApiGenerator : CsiApiGenerator {
	
	
	override fun generate(model: ApiModel, path: Path) {
		Files.createDirectories(path.resolve("io"))
		Files.createDirectories(path.resolve("csi/Objects"))
		
		generateIoEntity(path)
		generateIoEntityFabric(model, path)
		
		generateCsiApiIds(model, path)
		
		generateCsiEnums(model, path)
		generateCsiEntities(model, path)
		generateCsiObjects(model, path)
	}
	
	val typeNameVisitor = object : TypeVisitor<String, Unit> {
		override fun visitPrimitiveType(type: PrimitiveType, data: Unit): String {
			return when (type) {
				PrimitiveType.BOOLEAN -> "boolean"
				PrimitiveType.BYTE -> "number"
				PrimitiveType.INT -> "number"
				PrimitiveType.LONG -> "bigInt.BigInteger"
				PrimitiveType.DOUBLE -> "number"
				PrimitiveType.STRING -> "string"
				PrimitiveType.BOOLEAN_ARRAY -> "boolean[]"
				PrimitiveType.BYTE_ARRAY -> "number[]"
				PrimitiveType.INT_ARRAY -> "number[]"
				PrimitiveType.LONG_ARRAY -> "number[]"
				PrimitiveType.DOUBLE_ARRAY -> "number[]"
			}
		}
		
		override fun visitStructureType(type: StructureType, data: Unit): String {
			return type.tsName()
		}
		
		override fun visitListType(type: ListType, data: Unit): String {
			return type.element.accept(this) + "[]"
		}
		
		override fun visitNullableType(type: NullableType, data: Unit): String {
			return type.original.accept(this)
		}
		
	}
	
	val typeDefaultValueVisitor = object : TypeVisitor<String, Unit> {
		override fun visitPrimitiveType(type: PrimitiveType, data: Unit): String {
			return when (type) {
				PrimitiveType.BOOLEAN -> "false"
				PrimitiveType.BYTE -> "0"
				PrimitiveType.INT -> "0"
				PrimitiveType.LONG -> "bigInt(0)"
				PrimitiveType.DOUBLE -> "0.0"
				PrimitiveType.STRING -> "\"\""
				PrimitiveType.BOOLEAN_ARRAY -> "[]"
				PrimitiveType.BYTE_ARRAY -> "[]"
				PrimitiveType.INT_ARRAY -> "[]"
				PrimitiveType.LONG_ARRAY -> "[]"
				PrimitiveType.DOUBLE_ARRAY -> "[]"
			}
		}
		
		override fun visitListType(type: ListType, data: Unit): String {
			return "[]"
		}
		
		override fun visitStructureType(type: StructureType, data: Unit): String {
			return "null"
		}
		
		override fun visitNullableType(type: NullableType, data: Unit): String {
			return "null"
		}
		
	}
	
	private val writeVisitor = object : TypeVisitor<String, String> {
		override fun visitPrimitiveType(type: PrimitiveType, data: String): String {
			return when (type) {
				PrimitiveType.BOOLEAN -> "writer.writeBool(this.$data)"
				PrimitiveType.BYTE -> "writer.writeByte(this.$data)"
				PrimitiveType.INT -> "writer.writeInt(this.$data)"
				PrimitiveType.LONG -> "writer.writeLong(this.$data)"
				PrimitiveType.DOUBLE -> "writer.writeDouble(this.$data)"
				PrimitiveType.STRING -> "writer.writeString(this.$data)"
				PrimitiveType.BOOLEAN_ARRAY -> "writer.writeRawArray(this.$data, DataTypes.BOOLEAN)"
				PrimitiveType.BYTE_ARRAY -> "writer.writeRawArray(this.$data, DataTypes.BYTE)"
				PrimitiveType.INT_ARRAY -> "writer.writeRawArray(this.$data, DataTypes.INTEGER)"
				PrimitiveType.LONG_ARRAY -> "writer.writeRawArray(this.$data, DataTypes.LONGLONG)"
				PrimitiveType.DOUBLE_ARRAY -> "writer.writeRawArray(this.$data, DataTypes.DOUBLE)"
			}
		}
		
		override fun visitStructureType(type: StructureType, data: String): String {
			if (type.descriptor is EnumDescriptor) {
				return "writer.writeEnum(this.$data)"
			}
			return "writer.writeEntity(this.$data)"
		}
		
		override fun visitListType(type: ListType, data: String): String {
			return when (val elementType = type.element) {
				is StructureType -> "writer.writeObjectArray(this.$data)"
				is PrimitiveType -> {
					when (elementType) {
						PrimitiveType.BOOLEAN -> "writer.writeRawArray(this.$data, DataTypes.BOOLEAN)"
						PrimitiveType.BYTE    -> "writer.writeRawArray(this.$data, DataTypes.BYTE)"
						PrimitiveType.INT     -> "writer.writeRawArray(this.$data, DataTypes.INTEGER)"
						PrimitiveType.LONG    -> "writer.writeRawArray(this.$data, DataTypes.LONGLONG)"
						PrimitiveType.DOUBLE  -> "writer.writeRawArray(this.$data, DataTypes.DOUBLE)"
						PrimitiveType.STRING  -> "writer.writeRawArray(this.$data, DataTypes.STRING)"
						else                  -> throw UnsupportedOperationException()
					}
				}
				else             -> throw UnsupportedOperationException()
			}
		}
		
		override fun visitNullableType(type: NullableType, data: String): String {
			return type.original.accept(this, data)
		}
		
	}
	
	private val readVisitor = object : TypeVisitor<String, Unit> {
		override fun visitPrimitiveType(type: PrimitiveType, data: Unit): String {
			return when (type) {
				PrimitiveType.BOOLEAN -> "reader.readBool()"
				PrimitiveType.BYTE -> "reader.readByte()"
				PrimitiveType.INT -> "reader.readInt()"
				PrimitiveType.LONG -> "reader.readLong()"
				PrimitiveType.DOUBLE -> "reader.readDouble()"
				PrimitiveType.STRING -> "reader.readString()"
				PrimitiveType.BOOLEAN_ARRAY -> "reader.readRawArray(DataTypes.BOOLEAN)"
				PrimitiveType.BYTE_ARRAY -> "reader.readRawArray(DataTypes.BYTE)"
				PrimitiveType.INT_ARRAY -> "reader.readRawArray(DataTypes.INTEGER)"
				PrimitiveType.LONG_ARRAY -> "reader.readRawArray(DataTypes.LONGLONG)"
				PrimitiveType.DOUBLE_ARRAY -> "reader.readRawArray(DataTypes.DOUBLE)"
			}
		}
		
		override fun visitStructureType(type: StructureType, data: Unit): String {
			if (type.descriptor is EnumDescriptor) {
				return "<${type.accept(typeNameVisitor)}>reader.readEnum()"
			}
			return "<${type.accept(typeNameVisitor)}>reader.readEntity()"
		}
		
		override fun visitListType(type: ListType, data: Unit): String {
			val result = "<${type.accept(typeNameVisitor)}>reader."
			
			return when (val elementType = type.element) {
				is StructureType -> result + "readObjectArray()"
				is PrimitiveType -> {
					when (elementType) {
						PrimitiveType.BOOLEAN -> "readRawArray(DataTypes.BOOLEAN)"
						PrimitiveType.BYTE    -> "readRawArray(DataTypes.BYTE)"
						PrimitiveType.INT     -> "readRawArray(DataTypes.INTEGER)"
						PrimitiveType.LONG    -> "readRawArray(DataTypes.LONGLONG)"
						PrimitiveType.DOUBLE  -> "readRawArray(DataTypes.DOUBLE)"
						PrimitiveType.STRING  -> "readRawArray(DataTypes.STRING)"
						else                  -> throw UnsupportedOperationException()
					}
				}
				else             -> throw UnsupportedOperationException()
			}
		}
		
		override fun visitNullableType(type: NullableType, data: Unit): String {
			return type.original.accept(this, data)
		}
		
	}
	
	private fun generateCsiObjects(model: ApiModel, path: Path) {
		model.structures.filterIsInstance<ObjectDescriptor>().forEach { desc ->
			val name = desc.tsName()
			CodeFile().apply {
				desc.parent.also { p ->
					if (p == null) {
						line("import Entity from \"../../io/Entity\";")
					}
					else {
						val n = p.tsName()
						line("import $n from \"./$n\";")
					}
				}
				
				line()
				val parent = desc.parent.let { it?.tsName() ?: "Entity" }
				identBracketsCurly("export default class $name extends $parent ") {
					line("static readonly ID: number = ${desc.id};")
					line("static readonly INSTANCE: $name = new $name();")
					
					line()
					
					identBracketsCurly("constructor() ") {
						line("super();")
						line("this.setEId($name.ID);")
					}
				}
				
			}.write(path.resolve("csi/Objects/$name.ts"))
		}
	}
	
	private fun generateCsiEntities(model: ApiModel, path: Path) {
		model.structures.filterIsInstance<EntityDescriptor>().forEach { desc ->
			val name = desc.tsName()
			CodeFile().apply {
				line("import DataWriter from \"../../io/DataWriter\";")
				line("import DataReader from \"../../io/DataReader\";")
				line("import { DataTypes } from \"../../io/DataTypes\";")
				
				val hasParent = desc.parent != null
				
				desc.parent.also { p ->
					if (p == null) {
						line("import Entity from \"../../io/Entity\";")
					}
					else {
						val n = p.tsName()
						line("import $n from \"./$n\";")
					}
				}
				
				val fields = if (hasParent) desc.fields.filter { f ->
					var p = desc.parent!!.descriptor as EntityDescriptor?
					while (p != null) {
						if (p.fields.any { it.name == f.name }) return@filter false
						p = p.parent?.descriptor as EntityDescriptor?
					}
					
					true
				}
				else desc.fields
				
				fields.map { it.type }.forEach {
					if (it is StructureType) {
						val n = it.tsName()
						if (it.descriptor is EnumDescriptor) {
							line("import { $n } from \"./CsiEnums\";")
						}
						else {
							line("import $n from \"./$n\";")
						}
					}
					else if (it is ListType) {
						val e = it.element
						if (e is StructureType) {
							val n = e.tsName()
							line("import $n from \"./$n\";")
						}
					}
				}
				
				line()
				val parent = desc.parent.let { it?.tsName() ?: "Entity" }
				identBracketsCurly("export default class $name extends $parent ") {
					line("static readonly ID: number = ${desc.id};")
					line()
					
					if (fields.isNotEmpty()) {
						fields.forEach {
							line("public ${it.name}: " + it.type.accept(typeNameVisitor) + " = " + it.type.accept(typeDefaultValueVisitor) + ";")
						}
						line()
					}
					
					identBracketsCurly("constructor() ") {
						line("super();")
						line("this.setEId($name.ID);")
					}
					
					if (fields.isNotEmpty()) {
						line()
						identBracketsCurly("public write(writer: DataWriter) ") {
							if (hasParent) line("super.write(writer);")
							fields.forEach {
								line {
									append(it.type.accept(writeVisitor, it.name))
									append(';')
								}
							}
						}
						
						line()
						identBracketsCurly("public read(reader: DataReader) ") {
							if (hasParent) line("super.read(reader);")
							fields.forEach {
								line {
									append("this.").append(it.name).append(" = ").append(it.type.accept(readVisitor)).append(';')
								}
							}
						}
					}
				}
				
			}.write(path.resolve("csi/Objects/$name.ts"))
		}
	}
	
	private fun generateCsiEnums(model: ApiModel, path: Path) {
		CodeFile().apply {
			
			model.structures.filterIsInstance<EnumDescriptor>().forEach {
				identBracketsCurly("export enum ${it.tsName()} ") {
					it.values.forEachIndexed { i, v ->
						if (i == it.values.lastIndex) line(v.name)
						else line("${v.name},")
					}
				}
				line()
			}
			
		}.write(path.resolve("csi/Objects/CsiEnums.ts"))
	}
	
	private fun generateCsiApiIds(model: ApiModel, path: Path) {
		CodeFile().apply {
			
			identBracketsCurly("export default class CsiApi ") {
				line("// Services")
				model.server.services.forEach {
					line("public static readonly CSI_SERVICE_${it.name.toUpperUnderscoreCase()} = ${it.id};")
				}
				line()
				line("// > Server actions")
				model.server.services.forEach { s ->
					val prefix = "CSI_SERVER_${s.name.toUpperUnderscoreCase()}_"
					s.descriptor.methods.forEach { m ->
						line("public static readonly $prefix${m.name.toUpperUnderscoreCase()} = ${m.id};")
						if (m.result != null) {
							line("public static readonly $prefix${m.name.toUpperUnderscoreCase()}_CALLBACK = ${100 * s.id + m.id};")
						}
					}
					line()
				}
				
				line("// < Client actions")
				
				model.client.services.forEach { s ->
					val prefix = "CSI_CLIENT_${s.name.toUpperUnderscoreCase()}_"
					s.descriptor.methods.forEach { m ->
						line("public static readonly $prefix${m.name.toUpperUnderscoreCase()} = ${m.id};")
					}
					line()
				}
			}
		}.write(path.resolve("csi/Objects/CsiApiIds.ts"))
	}
	
	private fun generateIoEntityFabric(model: ApiModel, path: Path) {
		val structures = model.structures.filterNot { it is EnumDescriptor }
		
		CodeFile().apply {
			
			line("import Entity from \"./Entity\";")
			line()
			
			structures.forEach {
				val name = it.tsName()
				line("import $name from \"../csi/Objects/$name\";")
			}
			line()
			
			identBracketsCurly("export default class EntityFabric ") {
				identBracketsCurly("public static create(eId: number) : Entity ") {
					identBracketsCurly("switch (eId) ") {
						structures.forEach {
							val name = it.tsName()
							line {
								append("case $name.ID: return ")
								if (it is ObjectDescriptor) {
									append("$name.INSTANCE")
								}
								else {
									append("new $name()")
								}
								append(';')
							}
						}
						line("case -1: return null;")
						line("default: throw new Error(\"Unknown entity id \" + eId)")
					}
				}
			}
			
		}.write(path.resolve("io/EntityFabric.ts"))
	}
	
	private fun generateIoEntity(path: Path) {
		path.resolve("io/Entity.ts").toFile().writeText(
			"""
			import DataWriter from "./DataWriter";
			import DataReader from "./DataReader";

			export default class Entity {
				protected _eId: number = -1;
				
				public setEId(value: number) {
					this._eId = value;
				}
				
				public getEId() : number {
					return this._eId;
				}
				
				public write(writer: DataWriter) {}
				
				public read(reader: DataReader) {}
			}
		""".trimIndent()
		)
	}
	
	private fun StructureDescriptor.tsName(): String {
		return type.tsName()
	}
	
	private fun StructureType.tsName(): String {
		return path.name.replace('.', '_')
	}
	
	private fun String.toUpperUnderscoreCase(): String {
		val v = this
		
		val result = StringBuffer()
		var begin = true
		var lastUppercase = false
		for (i in v.indices) {
			val ch = v[i]
			if (Character.isUpperCase(ch)) {
				if (begin) {
					result.append(ch)
				}
				else {
					if (lastUppercase) {
						if (i + 1 < v.length) {
							val next = v[i + 1]
							if (Character.isUpperCase(next)) {
								result.append(ch)
							}
							else {
								result.append('_').append(ch)
							}
						}
						else {
							result.append(ch)
						}
					}
					else {
						result.append('_').append(ch)
					}
				}
				lastUppercase = true
			}
			else {
				result.append(Character.toUpperCase(ch))
				lastUppercase = false
			}
			begin = false
		}
		return result.toString()
	}
}

