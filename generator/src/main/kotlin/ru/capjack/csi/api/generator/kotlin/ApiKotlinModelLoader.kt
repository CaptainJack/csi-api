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
		
		
		val oldServiceNames = api.services.map { it.name }.toMutableSet()
		
		descriptor.unsubstitutedMemberScope.getContributedDescriptors().asSequence()
			.filterIsInstance<PropertyDescriptor>()
			.forEach {
				val serviceDescriptor = it.type.constructor.declarationDescriptor as ClassDescriptor
				val serviceDescriptorName = extractName(serviceDescriptor)
					?: throw GeneratorException(it.toString())
				
				val serviceName = it.name.toString()
				oldServiceNames.remove(serviceName)
				
				change = change.raiseTo(
					api.provideService(serviceName, loadService(serviceDescriptorName, serviceDescriptor))
				)
			}
		
		change = change.raiseTo(api.removeServices(oldServiceNames))
		
		model.raiseChange(change)
	}
	
	private fun loadService(name: String, descriptor: ClassDescriptor): ServiceDescriptor {
		val service = model.provideServicesDescriptor(name)
		var change = Change.ABSENT
		
		val oldMethodsNames = service.methods.map { it.name }.toMutableSet()
		
		descriptor.unsubstitutedMemberScope.getContributedDescriptors().asSequence()
			.filterIsInstance<FunctionDescriptor>()
			.filter { it.kind == CallableMemberDescriptor.Kind.DECLARATION }
			.forEach {
				oldMethodsNames.remove(it.name.toString())
				change = change.raiseTo(loadServiceMethod(service, it))
			}
		
		change = change.raiseTo(service.removeMethods(oldMethodsNames))
		
		model.raiseChange(change)
		
		return service
	}
	
	private fun loadServiceMethod(service: ServiceDescriptor, descriptor: FunctionDescriptor): Change {
		val arguments = mutableListOf<Parameter>()
		
		var result: List<Parameter>? = null
		
		descriptor.valueParameters.forEach { parameter ->
			if (parameter.type.toString().startsWith("[ERROR : Callback<")) {
				result = listOf(Parameter(null, defineType(parameter.type.arguments[0].type)))
			}
			else if (parameter.type.isFunctionType) {
				result = parameter.type.getValueParameterTypesFromFunctionType().map {
					Parameter(it.type.extractParameterNameFromFunctionTypeArgument()?.toString(), defineType(it.type))
				}
			}
			else {
				arguments.add(Parameter(parameter.name.toString(), defineType(parameter.type)))
			}
		}
		
		return service.provideMethod(descriptor.name.toString(), arguments, result)
	}
}