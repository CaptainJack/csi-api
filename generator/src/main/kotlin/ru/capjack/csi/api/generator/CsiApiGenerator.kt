package ru.capjack.csi.api.generator

import ru.capjack.csi.api.generator.model.ApiModel
import java.nio.file.Path

interface CsiApiGenerator {
	fun generate(model: ApiModel, path: Path)
}