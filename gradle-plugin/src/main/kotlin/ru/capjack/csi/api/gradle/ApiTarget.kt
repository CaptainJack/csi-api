package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate

abstract class ApiTarget(
	val name: String
) {
	private lateinit var project: Project
	
	open fun configureProject(sourceProject: Project) {
		project = sourceProject.project(sourceProject.path + ":" + name)
	}
	
	abstract fun generate(delegate: KotlinApiModelDelegate)
	
	override fun equals(other: Any?) = when {
		this === other                                                   -> true
		other !is ApiTarget -> false
		else                                                             -> name == other.name
	}
	
	override fun hashCode() = name.hashCode()
}