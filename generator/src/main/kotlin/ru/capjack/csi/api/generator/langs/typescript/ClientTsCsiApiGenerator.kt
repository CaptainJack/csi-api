package ru.capjack.csi.api.generator.langs.typescript

import ru.capjack.csi.api.generator.CsiApiGenerator
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.langs.typescript.TsCodersGenerator
import java.nio.file.Path
import kotlin.io.path.name

class ClientTsCsiApiGenerator(private val sourcePackage: String) : CsiApiGenerator {
	override fun generate(model: ApiModel, targetSourceDir: Path) {
		val codersGenerator = TsCodersGenerator(model, "$sourcePackage._impl")
		val apiGenerator = ClientTsApiGenerator(model, codersGenerator, sourcePackage)
		
		val target = targetSourceDir.resolve(apiGenerator.targetPackage.full.joinToString("/")).toFile()
		val files = mutableSetOf<Path>()
		
		files.addAll(apiGenerator.generate(targetSourceDir))
		files.addAll(codersGenerator.generate(targetSourceDir))
		
		target.walkBottomUp().forEach {
			if (it.isDirectory) {
				if (it.listFiles()?.size == 0) it.delete()
			}
			else if (!files.contains(it.toPath())) {
				if (it.extension == "meta") {
					if (!it.resolveSibling(it.nameWithoutExtension).exists()) it.delete()
				}
				else {
					it.delete()
				}
			}
		}
	}
}