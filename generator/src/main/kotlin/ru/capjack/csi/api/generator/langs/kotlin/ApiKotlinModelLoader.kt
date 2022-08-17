package ru.capjack.csi.api.generator.langs.kotlin

import org.jetbrains.kotlin.builtins.extractParameterNameFromFunctionTypeArgument
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnit
import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.Method
import ru.capjack.csi.api.generator.model.ServiceDescriptor
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinLoader
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinSource
import java.util.function.Predicate

class ApiKotlinModelLoader(
	source: KotlinSource,
	model: ApiModel,
) : KotlinLoader<ApiModel>(source, model) {
	
	override fun load(filter: Predicate<ClassDescriptor>) {
		super.load(filter)
		model.removeDeprecatedServices()
	}
	
	override fun processClassDescriptor(descriptor: ClassDescriptor) {
		when (descriptor.kind) {
			ClassKind.INTERFACE -> {
				val name = resolveName(descriptor)
				val api = when (name.self) {
					"ClientApi" -> model.client
					"ServerApi" -> model.server
					else        -> null
				}
				if (api != null) {
					loadApi(api, descriptor)
				}
			}
			
			else                -> super.processClassDescriptor(descriptor)
		}
		
		super.processClassDescriptor(descriptor)
	}
	
	private fun loadApi(api: Api, descriptor: ClassDescriptor) {
		
		val deprecatedServiceNames = api.services.map { it.name }.toMutableSet()
		
		descriptor.unsubstitutedMemberScope.getContributedDescriptors().asSequence()
			.filterIsInstance<PropertyDescriptor>()
			.forEach {
				val serviceName = it.name.toString()
				deprecatedServiceNames.remove(serviceName)
				
				val serviceDescriptorSource = it.type.constructor.declarationDescriptor as ClassDescriptor
				val serviceDescriptorName = resolveName(serviceDescriptorSource)
				val serviceDescriptor = model.resolveServicesDescriptor(serviceDescriptorName)
				
				api.provideService(serviceName, serviceDescriptor)
				loadServiceDescriptor(serviceDescriptor, serviceDescriptorSource)
			}
		
		api.removeServices(deprecatedServiceNames)
	}
	
	private val loadedServiceDescriptor = HashSet<ServiceDescriptor>()
	
	private fun loadServiceDescriptor(target: ServiceDescriptor, source: ClassDescriptor) {
		if (loadedServiceDescriptor.add(target)) {
			val deprecatedMethodsNames = target.methods.map { it.name }.toMutableSet()
			
			source.unsubstitutedMemberScope.getContributedDescriptors().asSequence()
				.filterIsInstance<FunctionDescriptor>()
				.filter { it.kind == CallableMemberDescriptor.Kind.DECLARATION }
				.forEach {
					deprecatedMethodsNames.remove(it.name.toString())
					loadMethod(target, it)
				}
			
			target.removeMethods(deprecatedMethodsNames)
		}
	}
	
	private fun loadMethod(target: ServiceDescriptor, source: FunctionDescriptor) {
		val name = source.name.toString()
		
		val result = source.returnType?.takeUnless(KotlinType::isUnit)?.let {
			val string = it.toString()
			when {
				string == "[Error type: Unresolved type for Cancelable]" -> Method.Result.Subscription
				string.startsWith("[Error type: Unresolved type for ServiceInstance<") -> {
					val serviceDescriptorSource = it.arguments.first().type.constructor.declarationDescriptor as ClassDescriptor
					val serviceDescriptorName = resolveName(serviceDescriptorSource)
					val serviceDescriptor = model.resolveServicesDescriptor(serviceDescriptorName)
					loadServiceDescriptor(serviceDescriptor, serviceDescriptorSource)
					Method.Result.InstanceService(serviceDescriptor)
				}
				
				else -> {
					Method.Result.Value(resolveType(it))
				}
			}
		}
		
		
		val suspend = source.isSuspend
		
		if (result != null) {
			require(suspend) { "Method mast by suspend when it has result (${target.name.full.joinToString(".")}:$name)" }
		}
		
		val arguments = source.valueParameters.map { a ->
			if (a.type.isFunctionType) {
				require(result == Method.Result.Subscription || result is Method.Result.InstanceService) {
					"Subscription method require Cancelable or Closable result (${
						target.name.full.joinToString(
							"."
						)
					}:$name)"
				}
				Method.Argument.Subscription(
					a.name.toString(),
					a.type.getValueParameterTypesFromFunctionType().map {
						Method.Parameter(
							it.type.extractParameterNameFromFunctionTypeArgument()?.toString(),
							resolveType(it.type)
						)
					}
				)
			}
			else Method.Argument.Value(a.name.toString(), resolveType(a.type))
		}
		
		if (result == Method.Result.Subscription) {
			require(arguments.any { it is Method.Argument.Subscription }) { "Subscription method require at least one subscription argument (${target.name.full.joinToString(".")}:$name)" }
		}
		
		return target.provideMethod(
			name,
			suspend,
			arguments,
			result
		)
	}
}