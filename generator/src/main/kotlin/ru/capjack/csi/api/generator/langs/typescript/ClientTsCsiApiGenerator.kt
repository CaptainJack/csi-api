package ru.capjack.csi.api.generator.langs.typescript

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.langs.typescript.SeparatedTsCodeSource
import ru.capjack.tool.biser.generator.langs.typescript.TsCodersGenerator
import java.nio.file.Path

class ClientTsCsiApiGenerator(private val sourcePackage: String) {
	fun generateSeparated(
		model: ApiModel,
		targetSourceDir: Path,
		genPrefix: String,
		genPath: String,
		libPath: String,
		cutGenPrefix: String? = null,
		cutLibPrefix: String? = null
	) {
		val codersGenerator = TsCodersGenerator(model, "$sourcePackage._impl")
		val apiGenerator = ClientTsApiGenerator(model, codersGenerator, sourcePackage)
		
		val target = targetSourceDir.resolve(apiGenerator.targetPackage.full.joinToString("/")).toFile()
		val codeSource = SeparatedTsCodeSource(targetSourceDir, genPrefix, genPath, libPath, cutGenPrefix, cutLibPrefix)
		
		apiGenerator.generate(codeSource)
		codersGenerator.generate(codeSource)
		
		val files = codeSource.saveNewFiles()
		
		target.walkBottomUp().forEach {
			if (it.isDirectory) {
				if (it.listFiles()?.size == 0) it.delete()
			} else if (!files.contains(it.toPath())) {
				if (it.extension == "meta") {
					if (!it.resolveSibling(it.nameWithoutExtension).exists()) it.delete()
				} else {
					it.delete()
				}
			}
		}
	}
}