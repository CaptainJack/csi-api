@file:Suppress("DuplicatedCode")

package ru.capjack.csi.api.generator.langs.kotlin

import ru.capjack.csi.api.generator.LogCallVisitorData
import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.Method
import ru.capjack.csi.api.generator.model.Service
import ru.capjack.csi.api.generator.model.ServiceDescriptor
import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.TypeAggregator
import ru.capjack.tool.biser.generator.TypeCollector
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodeFile
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodersGenerator
import ru.capjack.tool.biser.generator.model.*
import java.nio.file.Path

abstract class KotlinApiGenerator(
	protected val model: ApiModel,
	protected val coders: KotlinCodersGenerator,
	targetPackage: String,
	private val side: String
) {
	private val targetPackage = model.nameSpace.resolvePackageName(targetPackage)
	private val sidePackage = this.targetPackage.resolvePackageName(side)
	
	protected abstract fun generate(files: MutableList<KotlinCodeFile>)
	
	protected abstract fun generateApiAdapterDeclaration(code: Code, iaName: String, oaName: String): Code
	
	open fun generate(targetSourceDir: Path) {
		targetSourceDir.resolve(targetPackage.full.joinToString("/")).toFile().deleteRecursively()
		
		val files = mutableListOf<KotlinCodeFile>()
		generate(files)
		
		files.forEach { it.save(targetSourceDir) }
	}
	
	protected fun generate(innerApi: Api, outerApi: Api, files: MutableList<KotlinCodeFile>) {
		val loggers = TypeAggregator()
		
		files.add(generateApiVersion())
		files.add(generateInnerApi(innerApi))
		files.add(generateOuterApi(outerApi))
		files.add(generateOuterApiImpl(outerApi))
		files.add(generateApiAdapter(innerApi, outerApi))
		files.add(generateApiConnection(innerApi))
		
		innerApi.services
			.fold(hashSetOf<ServiceDescriptor>()) { a, it -> collectServiceDescriptors(a, it.descriptor) }
			.onEach { s ->
				s.methods.forEach { m -> if (m.result == Method.Result.Subscription) files.add(generateOuterSubscription(s, m, loggers)) }
			}
			.mapTo(files) { generateInnerService(it, loggers) }
		
		outerApi.services
			.fold(hashSetOf<ServiceDescriptor>()) { a, it -> collectServiceDescriptors(a, it.descriptor) }
			.onEach { s ->
				s.methods.forEach { m -> if (m.result == Method.Result.Subscription) files.add(generateInnerSubscription(s, m, loggers)) }
			}
			.mapTo(files) { generateOuterService(it, loggers) }
		
		if (loggers.hasNext()) {
			files.add(generateLogging(loggers))
		}
	}
	
	private fun generateApiVersion(): KotlinCodeFile {
		return KotlinCodeFile(sidePackage.resolveEntityName("_version")).apply {
			body.line("const val API_VERSION = ${model.version.compatible}")
		}
	}
	
	private fun collectServiceDescriptors(target: HashSet<ServiceDescriptor>, descriptor: ServiceDescriptor): HashSet<ServiceDescriptor> {
		if (target.add(descriptor)) {
			descriptor.methods.forEach { m ->
				(m.result as? Method.Result.InstanceService)?.also { collectServiceDescriptors(target, it.descriptor) }
			}
		}
		return target
	}
	
	private fun generateInnerApi(api: Api): KotlinCodeFile {
		val name = "Internal${api.name}"
		
		return KotlinCodeFile(sidePackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api.$side/InnerApi")
			addDependency(targetPackage.resolveEntityName(api.name))
			
			body.line("interface $name : ${api.name}, InnerApi")
		}
	}
	
	private fun generateOuterApi(api: Api): KotlinCodeFile {
		val name = "Internal${api.name}"
		
		return KotlinCodeFile(sidePackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/OuterApi")
			addDependency(targetPackage.resolveEntityName(api.name))
			
			body.line("interface $name : ${api.name}, OuterApi")
		}
	}
	
	private fun generateOuterApiImpl(api: Api): KotlinCodeFile {
		val name = "Internal${api.name}Impl"
		
		return KotlinCodeFile(sidePackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/AbstractOuterApi")
			addDependency("ru.capjack.csi.api/Context")
			
			body.apply {
				line("internal class $name(")
				ident {
					line("context: Context")
				}
				identBracketsCurly("): AbstractOuterApi(context.connection), Internal${api.name} ") {
					api.services.sortedBy(Service::id).forEach {
						addDependency(it.descriptor.name)
						line("override val ${it.name}: ${it.descriptor.name.self} = ${it.descriptor.name.self}Outer(context, false, ${it.id}, \"${it.name}\")")
					}
				}
			}
		}
	}
	
	private fun generateApiAdapter(innerApi: Api, outerApi: Api): KotlinCodeFile {
		return KotlinCodeFile(sidePackage.resolveEntityName("ApiAdapter")).apply {
			addDependency("kotlinx.coroutines/CoroutineScope")
			addDependency("ru.capjack.csi.api/CallbacksRegister")
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api.$side/AbstractApiAdapter")
			addDependency("ru.capjack.csi.api.$side/ApiSluice")
			addDependency("ru.capjack.csi.core.$side/ConnectionHandler")
			addDependency("ru.capjack.tool.io/ByteBuffer")
			addDependency("ru.capjack.tool.utils.pool/ObjectPool")
			
			val iaName = "Internal" + innerApi.name
			val oaName = "Internal" + outerApi.name
			
			generateApiAdapterDeclaration(body, iaName, oaName).apply {
				line()
				identBracketsCurly("override fun getLoggerName(): String ") {
					line("return \"${sidePackage.full.joinToString(".")}\"")
				}
				
				line()
				identBracketsCurly("override fun createConnectionHandler(context: Context, api: $iaName): ConnectionHandler ") {
					line("return ApiConnection(context, api)")
				}
				
				line()
				identBracketsCurly("override fun createOuterApi(context: Context): $oaName ") {
					line("return ${oaName}Impl(context)")
				}
				
				line()
				identBracketsCurly("override fun provideCallbacksRegister(): CallbacksRegister ") {
					if (outerApi.services.any { s -> s.descriptor.methods.any { it.result != null } }) {
						addDependency("ru.capjack.csi.api/RealCallbacksRegister")
						line("return RealCallbacksRegister()")
					}
					else {
						addDependency("ru.capjack.csi.api/NothingCallbacksRegister")
						line("return NothingCallbacksRegister")
					}
				}
			}
			
		}
	}
	
	private fun generateApiConnection(api: Api): KotlinCodeFile {
		val iaInternalName = "Internal${api.name}"
		
		return KotlinCodeFile(sidePackage.resolveEntityName("ApiConnection")).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/InnerServiceDelegate")
			addDependency("ru.capjack.csi.api.$side/AbstractApiConnection")
			
			body.apply {
				line("internal class ApiConnection(")
				ident {
					line("context: Context,")
					line("api: $iaInternalName")
				}
				identBracketsCurly(") : AbstractApiConnection<$iaInternalName>(context, api) ") {
					
					val services = api.services.sortedBy(Service::id)
					
					line()
					services.forEach {
						line("private val ${it.name}Delegate = ${it.descriptor.name.self}InnerDelegate(context, api.${it.name}, \"${it.name}\")")
					}
					
					line()
					identBracketsCurly("override fun findService(serviceId: Int): InnerServiceDelegate<*>? ") {
						identBracketsCurly("return when (serviceId) ") {
							services.forEach {
								line("${it.id} -> ${it.name}Delegate")
							}
							line("else -> null")
						}
					}
				}
			}
		}
	}
	
	private fun generateInnerService(descriptor: ServiceDescriptor, loggers: TypeCollector): KotlinCodeFile {
		val name = "${descriptor.name.self}InnerDelegate"
		
		return KotlinCodeFile(sidePackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/InnerServiceDelegate")
			addDependency("ru.capjack.tool.biser/BiserReader")
			addDependency(descriptor.name)
			
			body.apply {
				line("@Suppress(\"FunctionName\")")
				line("internal class $name(")
				ident {
					line("context: Context,")
					line("service: ${descriptor.name.self},")
					line("name: String")
				}
				identBracketsCurly(") : InnerServiceDelegate<${descriptor.name.self}>(context, service, name) ") {
					
					line()
					
					identBracketsCurly("override fun callMethod(methodId: Int, message: BiserReader): Boolean ") {
						identBracketsCurly("when (methodId) ") {
							descriptor.methods.sortedBy(Method::id).forEach {
								line("${it.id} -> call_${it.name}(message)")
							}
							line("else -> return false")
						}
						line("return true")
					}
					
					descriptor.methods.sortedBy(Method::id).forEach { m ->
						line()
						identBracketsCurly("private fun call_${m.name}(message: BiserReader) ") {
							val result = m.result
							val arguments = m.arguments
							val hasArguments = arguments.isNotEmpty()
							if (result != null) {
								line("val c = message." + coders.provideReadCall(this, PrimitiveType.INT))
							}
							arguments.forEachIndexed { i, a ->
								if (a is Method.Argument.Value) {
									line("val a$i = message." + coders.provideReadCall(this, a.type))
								}
							}
							
							line {
								append("logMethodCall(\"${m.name}\"")
								if (result != null) {
									append(", c")
								}
								if (hasArguments) append(") {")
								else append(") {}")
							}
							if (hasArguments) {
								ident {
									arguments.forEachIndexed { i, a ->
										if (a is Method.Argument.Value) logCall(loggers, i == arguments.lastIndex, a.type, a.name, "a$i")
									}
								}
								line("}")
							}
							
							(if (m.suspend) identBracketsCurly("launchCoroutine ") else this).apply {
								
								when (result) {
									null                             -> {
										line {
											append("service.${m.name}(")
											arguments.indices.joinTo(this) { "a$it" }
											append(')')
										}
									}
									is Method.Result.Value           -> {
										line {
											append("val r = service.${m.name}(")
											arguments.indices.joinTo(this) { "a$it" }
											append(")")
										}
										identBracketsCurly("logMethodResponse(\"${m.name}\", c) ") {
											logCall(loggers, true, result.type, null, "r")
										}
										identBracketsCurly("sendMethodResponse(c) ") {
											line(coders.provideWriteCall(this, result.type, "r"))
										}
									}
									is Method.Result.InstanceService -> {
										line {
											append("val i = service.${m.name}(")
											arguments.indices.joinTo(this) { "a$it" }
											append(")")
										}
										line("val s = registerInstanceService(i, ${result.descriptor.name.self}InnerDelegate(context, i.service, \"\$name.${m.name}\"))")
										line("logInstanceServiceResponse(\"${m.name}\", c, s) ")
										line("sendInstanceServiceResponse(c, s) ")
									}
									Method.Result.Subscription       -> {
										line("val s = ${descriptor.name.self}_${m.name}_OuterSubscription(context, this@${name})")
										line {
											append("val e = service.${m.name}(")
											arguments.forEachIndexed { i, a ->
												when (a) {
													is Method.Argument.Value        -> append("a$i")
													is Method.Argument.Subscription -> append("s.${a.name}")
												}
												if (i != arguments.lastIndex) append(", ")
											}
											append(")")
										}
										line("val i = registerSubscription(s, e)")
										line("logSubscriptionResponse(\"${m.name}\", c, i) ")
										line("sendSubscriptionResponse(c, i) ")
									}
								}
							}
							
						}
					}
					
				}
			}
		}
	}
	
	private fun generateOuterService(descriptor: ServiceDescriptor, loggers: TypeCollector): KotlinCodeFile {
		val name = descriptor.name.self
		
		return KotlinCodeFile(sidePackage.resolveEntityName("${name}Outer")).apply {
			addDependency(descriptor.name)
			addDependency("ru.capjack.csi.api/OuterService")
			addDependency("ru.capjack.csi.api/Context")
			
			body.apply {
				line("@Suppress(\"LocalVariableName\")")
				line("internal class ${name}Outer(")
				ident {
					line("context: Context,")
					line("instance: Boolean,")
					line("serviceId: Int,")
					line("serviceName: String")
				}
				
				identBracketsCurly(") : OuterService(context, instance, serviceId, serviceName), $name ") {
					descriptor.methods.sortedBy(Method::id).forEach { m ->
						
						line()
						line {
							append("override ${if (m.suspend) "suspend " else ""}fun ${m.name}(")
							m.arguments.joinTo(this) { getArgumentDeclaration(this@identBracketsCurly, it) }
							append(")")
							m.result?.also {
								append(": ")
								when (it) {
									is Method.Result.Value           -> append(coders.getTypeName(it.type, this@identBracketsCurly))
									is Method.Result.InstanceService -> {
										addDependency("ru.capjack.csi.api/ServiceInstance")
										addDependency(it.descriptor.name)
										append("ServiceInstance<${it.descriptor.name.self}>")
									}
									Method.Result.Subscription       -> {
										addDependency("ru.capjack.tool.utils/Cancelable")
										append("Cancelable")
									}
								}
							}
							append(" {")
						}
						
						ident {
							line("_checkClosed()")
							
							val r = m.result
							
							(if (r != null) {
								addDependency("kotlin.coroutines/suspendCoroutine")
								addDependency("kotlin.coroutines/resume")
								line("return suspendCoroutine { _continuation ->")
								ident()
							}
							else this).apply {
								
								if (r != null) {
									line("val _callback = _registerCallback { _callbackLocal -> ")
									ident {
										when (r) {
											is Method.Result.Value           -> {
												line("val _result = " + coders.provideReadCall(this, r.type))
												identBracketsCurly("_logMethodResponse(\"${m.name}\", _callbackLocal) ") {
													logCall(loggers, true, r.type, null, "_result")
												}
												line("_continuation.resume(_result)")
											}
											is Method.Result.InstanceService -> {
												addDependency(r.descriptor.name)
												line("val _service = " + coders.provideReadCall(this, PrimitiveType.INT))
												line("_logInstanceOpen(\"${m.name}\", _callbackLocal, _service) ")
												line("_continuation.resume(_createServiceInstance(${r.descriptor.name.self}Outer(_context, true, _service, \"\$_name.${m.name}[+\$_service]\")))")
												
											}
											Method.Result.Subscription       -> {
												line("val _subscription = " + coders.provideReadCall(this, PrimitiveType.INT))
												line("_logSubscriptionBegin(\"${m.name}\", _callbackLocal, _subscription) ")
												
												line {
													append("val t = ${name}_${m.name}_InnerSubscription(_context, this@${name}Outer, _subscription, ")
													m.arguments.filterIsInstance<Method.Argument.Subscription>().joinTo(this) { it.name }
													append(")")
												}
												line("_registerSubscription(t)")
												line("_continuation.resume(t)")
											}
										}
									}
									line("}")
								}
								
								val send: String
								val log: String
								
								if (r == null) {
									log = "_logMethodCall(\"${m.name}\") "
									send = "_callMethod(${m.id})"
								}
								else {
									log = "_logMethodCall(\"${m.name}\", _callback) "
									send = "_callMethod(${m.id}, _callback)"
								}
								
								identBracketsCurly(log) {
									m.arguments.forEachIndexed { i, a ->
										if (a is Method.Argument.Value) logCall(loggers, m.arguments.lastIndex == i, a.type, a.name)
									}
								}
								
								if (m.arguments.isEmpty()) {
									line(send)
								}
								else {
									identBracketsCurly("$send ") {
										m.arguments.forEach { a ->
											if (a is Method.Argument.Value) line(coders.provideWriteCall(this, a.type, a.name))
										}
									}
								}
							}
							if (r != null) line("}")
						}
						line("}")
					}
				}
			}
		}
	}
	
	private fun generateInnerSubscription(service: ServiceDescriptor, method: Method, loggers: TypeAggregator): KotlinCodeFile {
		val name = "${service.name.self}_${method.name}_InnerSubscription"
		val arguments = method.arguments.filterIsInstance<Method.Argument.Subscription>()
		return KotlinCodeFile(sidePackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/OuterService")
			addDependency("ru.capjack.csi.api/InnerSubscription")
			addDependency("ru.capjack.tool.biser/BiserReader")
			
			body.apply {
				line("@Suppress(\"ClassName\")")
				line("internal class $name(")
				ident {
					line("context: Context,")
					line("service: OuterService,")
					line("id: Int,")
					arguments.forEach {
						line("private val _" + getArgumentDeclaration(this, it) + ",")
					}
				}
				
				identBracketsCurly(") : InnerSubscription(context, service, \"${method.name}\", id) ") {
					identBracketsCurly("override fun call(argumentId: Int, message: BiserReader): Boolean ") {
						
						if (arguments.size == 1) {
							line("if (argumentId != 0) return false")
							val a = arguments.first()
							
							a.parameters.forEachIndexed { i, p ->
								line("val p$i = message." + coders.provideReadCall(this, p.type))
							}
							
							identBracketsCurly("logCall(\"${a.name}\") ") {
								a.parameters.forEachIndexed { i, a ->
									logCall(loggers, i == arguments.lastIndex, a.type, a.name, "p$i")
								}
							}
							
							line {
								append("_${a.name}(")
								a.parameters.indices.joinTo(this) { "p$it" }
								append(')')
							}
						}
						else {
							identBracketsCurly("when (argumentId) ") {
								arguments.forEachIndexed { q, a ->
									identBracketsCurly("$q -> ") {
										a.parameters.forEachIndexed { i, p ->
											line("val p$i = message." + coders.provideReadCall(this, p.type))
										}
										
										identBracketsCurly("logCall(\"${a.name}\") ") {
											a.parameters.forEachIndexed { i, a ->
												logCall(loggers, i == arguments.lastIndex, a.type, a.name, "p$i")
											}
										}
										
										line {
											append("_${a.name}(")
											a.parameters.indices.joinTo(this) { "p$it" }
											append(')')
										}
									}
								}
								line("else -> return false")
							}
						}
						line("return true")
					}
				}
			}
		}
	}
	
	private fun generateOuterSubscription(service: ServiceDescriptor, method: Method, loggers: TypeAggregator): KotlinCodeFile {
		val name = "${service.name.self}_${method.name}_OuterSubscription"
		val arguments = method.arguments.filterIsInstance<Method.Argument.Subscription>()
		return KotlinCodeFile(sidePackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/InnerServiceDelegate")
			addDependency("ru.capjack.csi.api/OuterSubscription")
			
			body.apply {
				line("@Suppress(\"ClassName\")")
				line("internal class $name(")
				ident {
					line("context: Context,")
					line("service: InnerServiceDelegate<*>")
				}
				identBracketsCurly(") : OuterSubscription(context, service, \"${method.name}\") ") {
					
					arguments.forEachIndexed { id, a ->
						line()
						line {
							append("val ${a.name} = { ")
							if (a.parameters.isNotEmpty()) {
								a.parameters.forEachIndexed { i, p ->
									append("p").append(i).append(": ").append(coders.getTypeName(p.type, this@identBracketsCurly))
									if (a.parameters.lastIndex != i) append(", ")
								}
								append(" ->")
							}
						}
						ident {
							identBracketsCurly("logCall(\"${a.name}\") ") {
								a.parameters.forEachIndexed { i, p ->
									logCall(loggers, a.parameters.lastIndex == i, p.type, p.name, "p$i")
								}
							}
							if (a.parameters.isEmpty()) {
								line("call($id)")
							}
							else {
								identBracketsCurly("call($id) ") {
									a.parameters.forEachIndexed { i, p ->
										line(coders.provideWriteCall(this, p.type, "p$i"))
									}
								}
							}
						}
						line("}")
					}
				}
			}
		}
	}
	
	
	private fun getArgumentDeclaration(depended: DependedCode, argument: Method.Argument): String {
		return StringBuilder().apply {
			append(argument.name).append(": ")
			
			when (argument) {
				is Method.Argument.Value        -> append(coders.getTypeName(argument.type, depended))
				is Method.Argument.Subscription -> {
					append("(")
					argument.parameters.forEachIndexed { i, p ->
						if (p.name != null) append(p.name).append(": ")
						append(coders.getTypeName(p.type, depended))
						if (argument.parameters.lastIndex != i) append(", ")
					}
					append(") -> Unit")
				}
			}
			
		}.toString()
	}
	
	private fun generateLogging(loggers: TypeAggregator): KotlinCodeFile {
		return KotlinCodeFile(sidePackage.resolveEntityName("_logging")).apply {
			body.apply {
				loggers.forEach { type ->
					val name = defineLogName(type)
					identBracketsCurly("internal val $name: StringBuilder.(${coders.getTypeName(type, this)}) -> Unit = ") {
						type.accept(loggingVisitor, GenerateLoggingVisitorData(this, loggers))
					}
					line()
				}
			}
		}
	}
	
	
	private fun Code.logCall(loggers: TypeCollector, last: Boolean, type: Type, argName: String?, valName: String = argName!!) {
		type.accept(logCallVisitor, LogCallVisitorData(loggers, this, argName, valName, !last))
	}
	
	private fun defineLogName(type: Type): String {
		return "log_" + type.accept(logNamesVisitor)
	}
	
	private val logCallVisitor = object : TypeVisitor<Unit, LogCallVisitorData> {
		
		private fun defineFn(data: LogCallVisitorData): String {
			return if (data.sep) {
				data.code.addDependency("ru.capjack.csi.api/logS")
				"logS"
			}
			else {
				data.code.addDependency("ru.capjack.csi.api/log")
				"log"
			}
		}
		
		private fun visitGenerated(type: Type, data: LogCallVisitorData) {
			val fn = defineFn(data)
			val logger = defineLogName(type)
			data.loggers.add(type)
			
			if (data.argName == null) {
				data.code.line("$fn(${data.argVal}, $logger)")
			}
			else {
				data.code.line("$fn(\"${data.argName}\", ${data.argVal}, $logger)")
			}
		}
		
		override fun visitPrimitiveType(type: PrimitiveType, data: LogCallVisitorData) {
			val fn = defineFn(data)
			if (data.argName == null) {
				data.code.line("$fn(${data.argVal})")
			}
			else {
				data.code.line("$fn(\"${data.argName}\", ${data.argVal})")
			}
		}
		
		override fun visitListType(type: ListType, data: LogCallVisitorData) {
			visitGenerated(type.element, data)
		}
		
		override fun visitMapType(type: MapType, data: LogCallVisitorData) {
			visitGenerated(type, data)
		}
		
		override fun visitNullableType(type: NullableType, data: LogCallVisitorData) {
			visitGenerated(type, data)
		}
		
		override fun visitEntityType(type: EntityType, data: LogCallVisitorData) {
			if (model.getEntity(type.name) is EnumEntity) {
				val fn = defineFn(data)
				if (data.argName == null) {
					data.code.line("$fn(${data.argVal}.name)")
				}
				else {
					data.code.line("$fn(\"${data.argName}\", ${data.argVal}.name)")
				}
			}
			else {
				visitGenerated(type, data)
			}
		}
	}
	
	private val logNamesVisitor = object : TypeVisitor<String, Unit> {
		override fun visitPrimitiveType(type: PrimitiveType, data: Unit): String {
			return type.name
		}
		
		override fun visitEntityType(type: EntityType, data: Unit): String {
			return "ENTITY_" + type.name.internal.joinToString("_")
		}
		
		override fun visitListType(type: ListType, data: Unit): String {
			return "list_" + type.element.accept(this, data)
		}
		
		override fun visitMapType(type: MapType, data: Unit): String {
			return "MAP_" + type.key.accept(this, data) + "__" + type.value.accept(this, data)
		}
		
		override fun visitNullableType(type: NullableType, data: Unit): String {
			return "NULLABLE_" + type.original.accept(this, data)
		}
	}
	
	private val loggingVisitor = object : TypeVisitor<Unit, GenerateLoggingVisitorData>, EntityVisitor<Unit, GenerateLoggingVisitorData> {
		override fun visitPrimitiveType(type: PrimitiveType, data: GenerateLoggingVisitorData) {
			data.code.line("log(it)")
		}
		
		override fun visitListType(type: ListType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.element)
			data.code.line("log(it, ${defineLogName(type.element)})")
		}
		
		override fun visitMapType(type: MapType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.key)
			data.loggers.add(type.value)
			data.code.line("log(it, ${defineLogName(type.key)}, ${defineLogName(type.value)})")
		}
		
		override fun visitNullableType(type: NullableType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.original)
			data.code.line("if (it == null) append(\"NULL\") else log(it, ${defineLogName(type.original)})")
		}
		
		override fun visitEntityType(type: EntityType, data: GenerateLoggingVisitorData) {
			model.getEntity(type.name).accept(this, data)
		}
		
		///
		
		override fun visitEnumEntity(entity: EnumEntity, data: GenerateLoggingVisitorData) {
			data.code.line("log(it.name)")
		}
		
		override fun visitClassEntity(entity: ClassEntity, data: GenerateLoggingVisitorData) {
			data.code.apply {
				if (entity.children.isEmpty()) {
					visitClassEntity0(entity, data)
				}
				else {
					identBracketsCurly("when (it) ") {
						entity.children.forEach {
							val type = model.resolveEntityType(it.name)
							data.loggers.add(type)
							line("is ${coders.getTypeName(type, data.code)} -> ${defineLogName(type)}(it)")
						}
						if (entity.abstract) {
							line("else -> throw UnsupportedOperationException()")
						}
						else {
							identBracketsCurly("else -> ") {
								visitClassEntity0(entity, data)
							}
						}
					}
				}
			}
		}
		
		private fun Code.visitClassEntity0(entity: ClassEntity, data: GenerateLoggingVisitorData) {
			line("append('{')")
			entity.fields.forEachIndexed { i, f ->
				logCall(data.loggers, entity.fields.lastIndex == i, f.type, f.name, "it.${f.name}")
			}
			line("append('}')")
		}
		
		override fun visitObjectEntity(entity: ObjectEntity, data: GenerateLoggingVisitorData) {
			data.code.line("log(\"${entity.name.self}\")")
		}
	}
	
	private class GenerateLoggingVisitorData(val code: Code, val loggers: TypeCollector)
}
