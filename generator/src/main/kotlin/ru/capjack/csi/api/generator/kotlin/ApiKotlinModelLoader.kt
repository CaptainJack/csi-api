package ru.capjack.csi.api.generator.kotlin

import org.jetbrains.kotlin.builtins.extractParameterNameFromFunctionTypeArgument
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.Parameter
import ru.capjack.csi.api.generator.model.ServiceDescriptor
import ru.capjack.tool.io.biser.generator.GeneratorException
import ru.capjack.tool.io.biser.generator.kotlin.KotlinModelLoader
import ru.capjack.tool.io.biser.generator.kotlin.KotlinSource
import ru.capjack.tool.io.biser.generator.model.Change

class ApiKotlinModelLoader(
	model: ApiModel,
	source: KotlinSource,
	sourcePackage: String
) : KotlinModelLoader<ApiModel>(model, source, sourcePackage) {
	override fun processClassDescriptor(descriptor: ClassDescriptor, name: String) {
		when (descriptor.kind) {
			ClassKind.INTERFACE -> {
				val api = when {
					name.endsWith("ClientApi") -> model.client
					name.endsWith("ServerApi") -> model.server
					else                       -> null
				}
				if (api != null) {
					loadApi(api, name, descriptor)
				}
			}
			else                -> super.processClassDescriptor(descriptor, name)
		}
	}
	
	private fun loadApi(api: Api, name: String, descriptor: ClassDescriptor) {
		var change = Change.ABSENT
		
		change = change.raiseTo(
			api.updatePath(name)
		)
		
		descriptor.unsubstitutedMemberScope.getContributedDescriptors().asSequence()
			.filterIsInstance<PropertyDescriptor>()
			.forEach {
				val serviceDescriptor = it.type.constructor.declarationDescriptor as ClassDescriptor
				val serviceDescriptorName = extractName(serviceDescriptor)
					?: throw GeneratorException(it.toString())
				
				change = change.raiseTo(
					api.provideService(it.name.toString(), loadService(serviceDescriptorName, serviceDescriptor))
				)
			}
		
		model.raiseChange(change)
	}
	
	private fun loadService(name: String, descriptor: ClassDescriptor): ServiceDescriptor {
		val service = model.provideServicesDescriptor(name)
		
		descriptor.unsubstitutedMemberScope.getContributedDescriptors().asSequence()
			.filterIsInstance<FunctionDescriptor>()
			.filter { it.kind == CallableMemberDescriptor.Kind.DECLARATION }
			.forEach {
				loadServiceMethod(service, it)
			}
		
		return service
	}
	
	private fun loadServiceMethod(service: ServiceDescriptor, descriptor: FunctionDescriptor) {
		val arguments = mutableListOf<Parameter>()
		
		var result: List<Parameter>? = null
		
		descriptor.valueParameters.forEach { parameter ->
			if (parameter.type.isFunctionType) {
				result = parameter.type.getValueParameterTypesFromFunctionType().map {
					Parameter(it.type.extractParameterNameFromFunctionTypeArgument()?.toString(), defineType(it.type))
				}
			}
			else {
				arguments.add(Parameter(parameter.name.toString(), defineType(parameter.type)))
			}
		}
		
		service.provideMethod(descriptor.name.toString(), arguments, result)
	}
}