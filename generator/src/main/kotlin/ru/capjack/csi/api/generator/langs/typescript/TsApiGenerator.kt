@file:Suppress("DuplicatedCode")

package ru.capjack.csi.api.generator.langs.typescript

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
import ru.capjack.tool.biser.generator.langs.typescript.TsCodeFile
import ru.capjack.tool.biser.generator.langs.typescript.TsCodersGenerator
import ru.capjack.tool.biser.generator.model.*
import java.nio.file.Path

abstract class TsApiGenerator(
	protected val model: ApiModel,
	protected val coders: TsCodersGenerator,
	targetPackage: String,
	private val side: String,
	private val generateSources: Boolean = true
) {
	private val targetPackage = model.nameSpace.resolvePackageName(targetPackage)
	private val implPackage = this.targetPackage.resolvePackageName("_impl")
	
	protected abstract fun generate(files: MutableList<TsCodeFile>)
	
	protected abstract fun generateApiAdapterDeclaration(code: Code, iaName: String, oaName: String): Code
	
	open fun generate(targetSourceDir: Path) {
		targetSourceDir.resolve(targetPackage.full.joinToString("/")).toFile().deleteRecursively()
		
		val files = mutableListOf<TsCodeFile>()
		generate(files)
		
		files.forEach { it.save(targetSourceDir) }
	}
	
	protected fun generate(innerApi: Api, outerApi: Api, files: MutableList<TsCodeFile>) {
		val loggers = TypeAggregator()
		
		files.add(generateApiVersion())
		files.add(generateInnerApi(innerApi))
		files.add(generateOuterApi(outerApi))
		files.add(generateOuterApiImpl(outerApi))
		files.add(generateApiAdapter(innerApi, outerApi))
		files.add(generateApiConnection(innerApi))
		
		val allServices = hashSetOf<ServiceDescriptor>()
		
		innerApi.services
			.fold(hashSetOf<ServiceDescriptor>()) { a, it -> collectServiceDescriptors(a, it.descriptor) }
			.onEach { s ->
				allServices.add(s)
				s.methods.forEach { m -> if (m.arguments.any { it is Method.Argument.Subscription }) files.add(generateOuterSubscription(s, m, loggers)) }
			}
			.mapTo(files) { generateInnerService(it, loggers) }
		
		outerApi.services
			.fold(hashSetOf<ServiceDescriptor>()) { a, it -> collectServiceDescriptors(a, it.descriptor) }
			.onEach { s ->
				allServices.add(s)
				s.methods.forEach { m -> if (m.arguments.any { it is Method.Argument.Subscription }) files.add(generateInnerSubscription(s, m, loggers)) }
			}
			.mapTo(files) { generateOuterService(it, loggers) }
		
		if (loggers.hasNext()) {
			files.add(generateLogging(loggers))
		}
		
		if (generateSources) {
			files.add(generateSourceApi(innerApi))
			files.add(generateSourceApi(outerApi))
			
			allServices.mapTo(files) { generateSourceService(it) }
		}
	}
	
	private fun generateApiVersion(): TsCodeFile {
		return TsCodeFile(implPackage.resolveEntityName("_version")).apply {
			body.line("export const API_VERSION = ${model.version.compatible}")
		}
	}
	
	private fun generateSourceApi(api: Api): TsCodeFile {
		return TsCodeFile(targetPackage.resolveEntityName(api.name)).apply {
			body.identBracketsCurly("export interface ${api.name} ") {
				api.services.sortedBy(Service::id).forEach {
					addDependency(it.descriptor.name)
					line("readonly ${it.name}: ${coders.getTypeName(model.resolveEntityType(it.descriptor.name), this)}")
				}
			}
		}
	}
	
	private fun generateSourceService(descriptor: ServiceDescriptor): TsCodeFile {
		val type = model.resolveEntityType(descriptor.name)
		return TsCodeFile(descriptor.name).apply {
			body.identBracketsCurly("export interface ${coders.getTypeName(type, this)} ") {
				descriptor.methods.forEach { m ->
					line {
						append("${m.name}(")
						m.arguments.forEachIndexed { i, a ->
							if (i != 0) append(", ")
							append(getArgumentDeclaration(this@identBracketsCurly, a))
						}
						append(")")
						when (val r = m.result) {
							is Method.Result.Value           -> append(": Promise<" + coders.getTypeName(r.type, this@identBracketsCurly) + ">")
							is Method.Result.InstanceService -> {
								addDependency("ru.capjack.csi.api/ServiceInstance")
								append(": Promise<ServiceInstance<" + coders.getTypeName(model.resolveEntityType(r.descriptor.name), this@identBracketsCurly) + ">>")
							}
							Method.Result.Subscription       -> {
								addDependency("ru.capjack.tool.utils/Cancelable")
								append(": Promise<Cancelable>")
							}
						}
					}
					line()
				}
			}
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
	
	private fun generateInnerApi(api: Api): TsCodeFile {
		val name = "Internal${api.name}"
		
		return TsCodeFile(implPackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api.$side/InnerApi")
			addDependency(targetPackage.resolveEntityName(api.name))
			
			body.line("export interface $name extends ${api.name}, InnerApi {}")
		}
	}
	
	private fun generateOuterApi(api: Api): TsCodeFile {
		val name = "Internal${api.name}"
		
		return TsCodeFile(implPackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/OuterApi")
			addDependency(targetPackage.resolveEntityName(api.name))
			
			body.line("export interface $name extends ${api.name}, OuterApi {}")
		}
	}
	
	private fun generateOuterApiImpl(api: Api): TsCodeFile {
		val name = "Internal${api.name}Impl"
		
		return TsCodeFile(implPackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/AbstractOuterApi")
			addDependency("ru.capjack.csi.api/Context")
			addDependency(implPackage.resolveEntityName("InternalServerApi"))
			
			body.apply {
				identBracketsCurly("export class $name extends AbstractOuterApi implements Internal${api.name}") {
					val services = api.services.sortedBy(Service::id)
					
					services.forEach {
						addDependency(it.descriptor.name)
						line("readonly ${it.name}: ${it.descriptor.name.self}")
					}
					line()
					identBracketsCurly("constructor(context: Context) ") {
						line("super(context.connection)")
						
						services.forEach {
							addDependency(implPackage.resolveEntityName("${it.descriptor.name.self}Outer"))
							line("this.${it.name} = new ${it.descriptor.name.self}Outer(context, false, ${it.id}, \"${it.name}\")")
						}
					}
				}
			}
		}
	}
	
	private fun generateApiAdapter(innerApi: Api, outerApi: Api): TsCodeFile {
		return TsCodeFile(implPackage.resolveEntityName("ApiAdapter")).apply {
			addDependency("ru.capjack.csi.api/CallbacksRegister")
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api.$side/AbstractApiAdapter")
			addDependency("ru.capjack.csi.api.$side/ApiSluice")
			addDependency("ru.capjack.csi.core.$side/ConnectionHandler")
			addDependency("ru.capjack.tool.io/ByteBuffer")
			addDependency("ru.capjack.tool.utils.pool/ObjectPool")
			
			val iaName = "Internal" + innerApi.name
			val oaName = "Internal" + outerApi.name
			
			addDependency(implPackage.resolveEntityName(iaName))
			addDependency(implPackage.resolveEntityName(oaName))
			addDependency(implPackage.resolveEntityName("ApiConnection"))
			addDependency(implPackage.resolveEntityName("${oaName}Impl"))
			
			
			generateApiAdapterDeclaration(body, iaName, oaName).apply {
				line()
				identBracketsCurly("getLoggerName(): string ") {
					line("return \"${targetPackage.full.joinToString(".")}\"")
				}
				
				line()
				identBracketsCurly("createConnectionHandler(context: Context, api: $iaName): ConnectionHandler ") {
					line("return new ApiConnection(context, api)")
				}
				
				line()
				identBracketsCurly("createOuterApi(context: Context): $oaName ") {
					line("return new ${oaName}Impl(context)")
				}
				
				line()
				identBracketsCurly("provideCallbacksRegister(): CallbacksRegister ") {
					if (outerApi.services.any { s -> s.descriptor.methods.any { it.result != null } }) {
						addDependency("ru.capjack.csi.api/RealCallbacksRegister")
						line("return new RealCallbacksRegister()")
					}
					else {
						addDependency("ru.capjack.csi.api/NothingCallbacksRegister")
						line("return new NothingCallbacksRegister()")
					}
				}
			}
			
		}
	}
	
	private fun generateApiConnection(api: Api): TsCodeFile {
		val iaInternalName = "Internal${api.name}"
		
		return TsCodeFile(implPackage.resolveEntityName("ApiConnection")).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/InnerServiceDelegate")
			addDependency("ru.capjack.csi.api.$side/AbstractApiConnection")
			addDependency(implPackage.resolveEntityName(iaInternalName))
			
			body.apply {
				identBracketsCurly("export class ApiConnection extends AbstractApiConnection<$iaInternalName> ") {
					
					val services = api.services.sortedBy(Service::id)
					services.forEach {
						addDependency(implPackage.resolveEntityName("${it.descriptor.name.self}InnerDelegate"))
						line("private readonly ${it.name}Delegate: ${it.descriptor.name.self}InnerDelegate")
					}
					line()
					
					line("constructor(")
					ident {
						line("context: Context,")
						line("api: $iaInternalName")
					}
					line(") {")
					ident {
						line("super(context, api)")
						
						services.forEach {
							line("this.${it.name}Delegate = new ${it.descriptor.name.self}InnerDelegate(context, api.${it.name}, \"${it.name}\")")
						}
					}
					line("}")
					line()
					
					identBracketsCurly("findService(serviceId: number): InnerServiceDelegate<any> | null ") {
						identBracketsCurly("switch (serviceId) ") {
							services.forEach {
								line("case ${it.id}: return this.${it.name}Delegate")
							}
							line("default: return null")
						}
					}
					
				}
			}
		}
	}
	
	private fun generateInnerService(descriptor: ServiceDescriptor, loggers: TypeCollector): TsCodeFile {
		val name = "${descriptor.name.self}InnerDelegate"
		
		return TsCodeFile(implPackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/InnerServiceDelegate")
			addDependency("ru.capjack.tool.biser/BiserReader")
			addDependency(descriptor.name)
			
			body.identBracketsCurly("export class $name extends InnerServiceDelegate<${descriptor.name.self}> ") {
				
				line("constructor(")
				ident {
					line("context: Context,")
					line("service: ${descriptor.name.self},")
					line("name: string")
				}
				line(") {")
				ident {
					line("super(context, service, name)")
				}
				line("}")
				line()
				
				identBracketsCurly("callMethod(methodId: number, message: BiserReader): boolean ") {
					identBracketsCurly("switch (methodId) ") {
						descriptor.methods.sortedBy(Method::id).forEach {
							line("case ${it.id}: this.call_${it.name}(message); break")
						}
						line("default: return false")
					}
					line("return true")
				}
				
				descriptor.methods.sortedBy(Method::id).forEach { m ->
					line()
					identBracketsCurly("private call_${m.name}(message: BiserReader) ") {
						val result = m.result
						val arguments = m.arguments
						val hasArguments = arguments.isNotEmpty()
						if (result != null) {
							line("const c = message." + coders.provideReadCall(this, PrimitiveType.INT))
						}
						arguments.forEachIndexed { i, a ->
							if (a is Method.Argument.Value) {
								line("const a$i = message." + coders.provideReadCall(this, a.type))
							}
						}
						
						line {
							if (result == null) {
								append("this.logMethodCall(\"${m.name}\", lb => {")
							}
							else {
								append("this.logMethodCallWithCallback(\"${m.name}\", c, lb => {")
							}
							if (!hasArguments) append("})")
						}
						if (hasArguments) {
							ident {
								arguments.forEachIndexed { i, a ->
									if (a is Method.Argument.Value) logCall(loggers, i == arguments.lastIndex, a.type, a.name, "a$i")
								}
							}
							line("})")
						}
						
						when (result) {
							null                             -> {
								line {
									append("this.service.${m.name}(")
									arguments.indices.joinTo(this) { "a$it" }
									append(')')
								}
							}
							is Method.Result.Value           -> {
								line {
									append("this.service.${m.name}(")
									arguments.indices.joinTo(this) { "a$it" }
									append(").then(r => {")
								}
								ident {
									line("this.logMethodResponse(\"${m.name}\", c, lb => {")
									ident {
										logCall(loggers, true, result.type, null, "r")
									}
									line("})")
									
									line("this.sendMethodResponse(c, w => {")
									ident {
										line("w." + coders.provideWriteCall(this, result.type, "r"))
									}
									line("})")
								}
								line("})")
							}
							is Method.Result.InstanceService -> {
								val hasSubscription = arguments.any { it is Method.Argument.Subscription }
								if (hasSubscription) {
									line("const ss = ${descriptor.name.self}_${m.name}_OuterSubscription(context, this@${name})")
								}
								
								line {
									append("this.service.${m.name}(")
									arguments.forEachIndexed { i, a ->
										when (a) {
											is Method.Argument.Value        -> append("a$i")
											is Method.Argument.Subscription -> append("ss.${a.name}")
										}
										if (i != arguments.lastIndex) append(", ")
									}
									append(").then(i => {")
								}
								ident {
									line("const s = this.registerInstanceService(i, ${result.descriptor.name.self}InnerDelegate(context, i.service, \"\$name.${m.name}\"))")
									if (hasSubscription) {
										addDependency("ru.capjack.tool.utils/Cancelable")
										line("const ssi = this.registerSubscription(ss, Cancelable.DUMMY)")
									}
									line("this.logInstanceServiceResponse(\"${m.name}\", c, s) ")
									line("this.sendInstanceServiceResponse(c, s) ")
									if (hasSubscription) {
										line("this.logSubscriptionResponse(\"${m.name}\", c, ssi) ")
										line("this.sendSubscriptionResponse(c, ssi) ")
									}
								}
								line("})")
							}
							Method.Result.Subscription       -> {
								line("const s = new ${descriptor.name.self}_${m.name}_OuterSubscription(context, this@${name})")
								line {
									append("this.service.${m.name}(")
									arguments.forEachIndexed { i, a ->
										when (a) {
											is Method.Argument.Value        -> append("a$i")
											is Method.Argument.Subscription -> append("s.${a.name}")
										}
										if (i != arguments.lastIndex) append(", ")
									}
									append(").then(e => {")
								}
								ident {
									line("const i = this.registerSubscription(s, e)")
									line("this.logSubscriptionResponse(\"${m.name}\", c, i) ")
									line("this.sendSubscriptionResponse(c, i) ")
								}
								line("})")
							}
						}
						
					}
				}
				
			}
		}
	}
	
	private fun generateOuterService(descriptor: ServiceDescriptor, loggers: TypeCollector): TsCodeFile {
		val name = descriptor.name.self
		
		return TsCodeFile(implPackage.resolveEntityName("${name}Outer")).apply {
			addDependency(descriptor.name)
			addDependency("ru.capjack.csi.api/OuterService")
			addDependency("ru.capjack.csi.api/Context")
			
			body.line("// noinspection JSUnusedLocalSymbols")
			body.identBracketsCurly("export class ${name}Outer extends OuterService implements $name ") {
				line("constructor(")
				ident {
					line("context: Context,")
					line("instance: boolean,")
					line("serviceId: number,")
					line("serviceName: string")
				}
				identBracketsCurly(") ") {
					line("super(context, instance, serviceId, serviceName)")
				}
				
				descriptor.methods.sortedBy(Method::id).forEach { m ->
					
					line()
					line {
						append("${m.name}(")
						m.arguments.joinTo(this) { getArgumentDeclaration(this@identBracketsCurly, it) }
						append(")")
						m.result?.also {
							append(": Promise<")
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
							append(">")
						}
						append(" {")
					}
					
					ident {
						line("this._checkClosed()")
						
						val r = m.result
						
						(if (r != null) {
							line("return new Promise(_continuation => {")
							ident()
						}
						else this).apply {
							
							if (r != null) {
								line("const _callback = this._registerCallback((_reader, _callbackLocal) => {")
								ident {
									when (r) {
										is Method.Result.Value           -> {
											line("const _result = _reader." + coders.provideReadCall(this, r.type))
											line("this._logMethodResponse(\"${m.name}\", _callbackLocal, lb => {")
											ident {
												logCall(loggers, true, r.type, null, "_result")
											}
											line("})")
											line("_continuation(_result)")
										}
										is Method.Result.InstanceService -> {
											val hasSubscription = m.arguments.any { it is Method.Argument.Subscription }
											addDependency(r.descriptor.name)
											addDependency(implPackage.resolveEntityName("${r.descriptor.name.self}Outer"))
											line("const _service = _reader." + coders.provideReadCall(this, PrimitiveType.INT))
											
											if (hasSubscription) {
												line("const _subscription = _reader." + coders.provideReadCall(this, PrimitiveType.INT))
											}
											
											line("this._logInstanceOpen(\"${m.name}\", _callbackLocal, _service) ")
											line("const _si = this._createServiceInstance(new ${r.descriptor.name.self}Outer(this._context, true, _service, `\${this._name}.${m.name}[+\${_service}]`))")
											if (hasSubscription) {
												line("this._logSubscriptionBegin(\"${m.name}\", _callbackLocal, _subscription) ")
												line {
													addDependency(implPackage.resolveEntityName("${name}_${m.name}_InnerSubscription"))
													append("const t = new ${name}_${m.name}_InnerSubscription(this._context, this, _subscription, ")
													m.arguments.filterIsInstance<Method.Argument.Subscription>().joinTo(this) { it.name }
													append(")")
												}
												line("_si.service._registerSubscription(t)")
											}
											line("_continuation(_si)")
											
										}
										Method.Result.Subscription       -> {
											line("const _subscription = _reader." + coders.provideReadCall(this, PrimitiveType.INT))
											line("this._logSubscriptionBegin(\"${m.name}\", _callbackLocal, _subscription) ")
											line {
												addDependency(implPackage.resolveEntityName("${name}_${m.name}_InnerSubscription"))
												append("const t = new ${name}_${m.name}_InnerSubscription(this._context, this, _subscription, ")
												m.arguments.filterIsInstance<Method.Argument.Subscription>().joinTo(this) { it.name }
												append(")")
											}
											line("this._registerSubscription(t)")
											line("_continuation(t)")
										}
									}
								}
								line("})")
							}
							
							val send: String
							val log: String
							
							if (r == null) {
								log = "this._logMethodCall(\"${m.name}\", lb => {"
								send = "this._callMethod(${m.id}, w => {"
							}
							else {
								log = "this._logMethodCallWithCallback(\"${m.name}\", _callback, lb => {"
								send = "this._callMethodWithCallback(${m.id}, _callback, w => {"
							}
							
							if (m.arguments.isEmpty()) {
								line("$log})")
							}
							else {
								line(log)
								ident {
									m.arguments.forEachIndexed { i, a ->
										if (a is Method.Argument.Value) logCall(loggers, m.arguments.lastIndex == i, a.type, a.name)
									}
								}
								line("})")
							}
							
							if (m.arguments.isEmpty()) {
								line("$send})")
							}
							else {
								line(send)
								ident {
									m.arguments.forEach { a ->
										if (a is Method.Argument.Value) line("w." + coders.provideWriteCall(this, a.type, a.name))
									}
								}
								line("})")
							}
						}
						if (r != null) line("})")
					}
					line("}")
				}
				
			}
		}
	}
	
	private fun generateInnerSubscription(service: ServiceDescriptor, method: Method, loggers: TypeAggregator): TsCodeFile {
		val name = "${service.name.self}_${method.name}_InnerSubscription"
		val arguments = method.arguments.filterIsInstance<Method.Argument.Subscription>()
		return TsCodeFile(implPackage.resolveEntityName(name)).apply {
			addDependency("ru.capjack.csi.api/Context")
			addDependency("ru.capjack.csi.api/OuterService")
			addDependency("ru.capjack.csi.api/InnerSubscription")
			addDependency("ru.capjack.tool.biser/BiserReader")
			
			body.identBracketsCurly("export class $name extends InnerSubscription ") {
				line("constructor(")
				ident {
					line("context: Context,")
					line("service: OuterService,")
					line("id: number,")
					arguments.forEach {
						line("private readonly _" + getArgumentDeclaration(this, it) + ",")
					}
				}
				identBracketsCurly(") ") {
					line("super(context, service, \"${method.name}\", id)")
				}
				line()
				
				identBracketsCurly("call(argumentId: number, message: BiserReader): boolean ") {
					
					if (arguments.size == 1) {
						line("if (argumentId != 0) return false")
						val a = arguments.first()
						
						a.parameters.forEachIndexed { i, p ->
							line("const p$i = message." + coders.provideReadCall(this, p.type))
						}
						
						line("this.logCall(\"${a.name}\", lb => {")
						ident {
							a.parameters.forEachIndexed { i, p ->
								logCall(loggers, i == a.parameters.lastIndex, p.type, p.name, "p$i")
							}
						}
						line("})")
						
						line {
							append("this._${a.name}(")
							a.parameters.indices.joinTo(this) { "p$it" }
							append(')')
						}
					}
					else {
						identBracketsCurly("switch (argumentId) ") {
							arguments.forEachIndexed { q, a ->
								identBracketsCurly("case $q: ") {
									a.parameters.forEachIndexed { i, p ->
										line("const p$i = message." + coders.provideReadCall(this, p.type))
									}
									
									line("this.logCall(\"${a.name}\", lb => {")
									ident {
										a.parameters.forEachIndexed { i, p ->
											logCall(loggers, i == a.parameters.lastIndex, p.type, p.name, "p$i")
										}
									}
									line("})")
									
									line {
										append("this._${a.name}(")
										a.parameters.indices.joinTo(this) { "p$it" }
										append(')')
									}
									line("break")
								}
							}
							line("default: return false")
						}
					}
					line("return true")
				}
				
			}
		}
	}
	
	private fun generateOuterSubscription(service: ServiceDescriptor, method: Method, loggers: TypeAggregator): TsCodeFile {
		val name = "${service.name.self}_${method.name}_OuterSubscription"
		val arguments = method.arguments.filterIsInstance<Method.Argument.Subscription>()
		return TsCodeFile(implPackage.resolveEntityName(name)).apply {
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
						append(p.name ?: "p$i").append(": ")
						append(coders.getTypeName(p.type, depended))
						if (argument.parameters.lastIndex != i) append(", ")
					}
					append(") => void")
				}
			}
			
		}.toString()
	}
	
	private fun generateLogging(loggers: TypeAggregator): TsCodeFile {
		return TsCodeFile(implPackage.resolveEntityName("_logging")).apply {
			addDependency("ru.capjack.csi.api/LogBuilder")
			header.line("// noinspection DuplicatedCode,JSUnusedLocalSymbols")
			header.line()
			body.apply {
				loggers.forEach { type ->
					val name = defineLogName(type)
					identBracketsCurly("export function $name(lb: LogBuilder, v: ${coders.getTypeName(type, this)}) ") {
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
		
		override fun visitPrimitiveType(type: PrimitiveType, data: LogCallVisitorData) {
			var fn = if (type.array) "logArray" else "log"
			if (data.sep) fn += "Sep"
			
			if (data.argName == null) {
				data.code.line("lb.$fn(${data.argVal})")
			}
			else {
				data.code.line("lb.${fn}Arg(\"${data.argName}\", ${data.argVal})")
			}
		}
		
		override fun visitListType(type: ListType, data: LogCallVisitorData) {
			if (type.element.let { it is PrimitiveType && !it.array }) {
				val fn = if (data.sep) "logArraySep" else "logArray"
				if (data.argName == null) {
					data.code.line("lb.$fn(${data.argVal})")
				}
				else {
					data.code.line("lb.${fn}Arg(\"${data.argName}\", ${data.argVal})")
				}
			}
			else {
				val fn = if (data.sep) "logSep" else "log"
				val logger = defineLogName(type)
				data.loggers.add(type)
				data.code.addDependency(implPackage.resolveEntityName("_logging"), logger)
				if (data.argName == null) {
					data.code.line("lb.${fn}With(${data.argVal}, $logger)")
				}
				else {
					data.code.line("lb.${fn}WithArg(\"${data.argName}\", ${data.argVal}, $logger)")
				}
			}
		}
		
		override fun visitMapType(type: MapType, data: LogCallVisitorData) {
			val fn = if (data.sep) "logSep" else "log"
			val logger = defineLogName(type)
			data.loggers.add(type)
			data.code.addDependency(implPackage.resolveEntityName("_logging"), logger)
			if (data.argName == null) {
				data.code.line("lb.${fn}With(${data.argVal}, $logger)")
			}
			else {
				data.code.line("lb.${fn}WithArg(\"${data.argName}\", ${data.argVal}, $logger)")
			}
		}
		
		override fun visitNullableType(type: NullableType, data: LogCallVisitorData) {
			val fn = if (data.sep) "logSepWith" else "logWith"
			val logger = defineLogName(type)
			data.loggers.add(type)
			data.code.addDependency(implPackage.resolveEntityName("_logging"), logger)
			if (data.argName == null) {
				data.code.line("lb.${fn}(${data.argVal}, $logger)")
			}
			else {
				data.code.line("lb.${fn}Arg(\"${data.argName}\", ${data.argVal}, $logger)")
			}
		}
		
		override fun visitEntityType(type: EntityType, data: LogCallVisitorData) {
			if (model.getEntity(type.name) is EnumEntity) {
				val fn = if (data.sep) "logSep" else "log"
				val n = coders.getTypeName(type, data.code)
				if (data.argName == null) {
					data.code.line("lb.$fn($n[${data.argVal}])")
				}
				else {
					data.code.line("lb.${fn}Arg(\"${data.argName}\", $n[${data.argVal}])")
				}
			}
			else {
				val fn = if (data.sep) "logSepWith" else "logWith"
				val logger = defineLogName(type)
				data.loggers.add(type)
				data.code.addDependency(implPackage.resolveEntityName("_logging"), logger)
				if (data.argName == null) {
					data.code.line("lb.${fn}(${data.argVal}, $logger)")
				}
				else {
					data.code.line("lb.${fn}Arg(\"${data.argName}\", ${data.argVal}, $logger)")
				}
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
			data.code.line(if (type.array) "lb.logArray(v)" else "lb.log(v)")
		}
		
		override fun visitListType(type: ListType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.element)
			val logger = defineLogName(type.element)
			data.code.addDependency(implPackage.resolveEntityName("_logging"), logger)
			data.code.line("lb.logArrayWith(v, $logger)")
		}
		
		override fun visitMapType(type: MapType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.key)
			data.loggers.add(type.value)
			val loggerK = defineLogName(type.key)
			val loggerV = defineLogName(type.value)
			data.code.addDependency(implPackage.resolveEntityName("_logging"), loggerK, loggerV)
			data.code.line("lb.logMapWith(v, $loggerK, $loggerV)")
		}
		
		override fun visitNullableType(type: NullableType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.original)
			val logger = defineLogName(type.original)
			data.code.addDependency(implPackage.resolveEntityName("_logging"), logger)
			data.code.line("if (v === null) lb.log(\"NULL\"); else lb.logWith(v, $logger)")
		}
		
		override fun visitEntityType(type: EntityType, data: GenerateLoggingVisitorData) {
			model.getEntity(type.name).accept(this, data)
		}
		
		///
		
		override fun visitEnumEntity(entity: EnumEntity, data: GenerateLoggingVisitorData) {
			data.code.line("lb.log(${coders.getTypeName(model.resolveEntityType(entity.name), data.code)}[v])")
		}
		
		override fun visitClassEntity(entity: ClassEntity, data: GenerateLoggingVisitorData) {
			data.code.apply {
				if (entity.children.isEmpty()) {
					visitClassEntity0(entity, data)
				}
				else {
					identBracketsCurly("switch (true) ") {
						entity.children.forEach {
							val type = model.resolveEntityType(it.name)
							data.loggers.add(type)
							val typeName = coders.getTypeName(type, data.code)
							line("case v instanceof $typeName: ${defineLogName(type)}(lb, v as $typeName); break")
						}
						if (entity.abstract) {
							data.code.addDependency("ru.capjack.tool.lang.exceptions/UnsupportedOperationException")
							line("default: throw new UnsupportedOperationException()")
						}
						else {
							identBracketsCurly("default:  ") {
								visitClassEntity0(entity, data)
								line("break")
							}
						}
					}
				}
			}
		}
		
		private fun Code.visitClassEntity0(entity: ClassEntity, data: GenerateLoggingVisitorData) {
			line("lb.log('{')")
			entity.fields.forEachIndexed { i, f ->
				logCall(data.loggers, entity.fields.lastIndex == i, f.type, f.name, "v.${f.name}")
			}
			line("lb.log('}')")
		}
		
		override fun visitObjectEntity(entity: ObjectEntity, data: GenerateLoggingVisitorData) {
			data.code.line("lb.log(\"${entity.name.self}\")")
		}
	}
	
	private class GenerateLoggingVisitorData(val code: Code, val loggers: TypeCollector)
}
