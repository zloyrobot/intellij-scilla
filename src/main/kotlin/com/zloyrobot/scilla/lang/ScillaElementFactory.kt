package com.zloyrobot.scilla.lang

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.descendantsOfType

class ScillaElementFactory(private val project: Project) {
	val factory = PsiFileFactory.getInstance(project)
	
	fun createTypeElement(code: String): ScillaTypeElement? {
		return create("library Lib\nlet x: $code = Int32 0")
	}
	fun createType(code: String): ScillaType {
		return createTypeElement("library Lib\nlet x: $code = Int32 0")?.ownType ?: ScillaUnknownType
	}
	
	private inline fun <reified T : PsiElement> create(code: String): T? {
		return factory.createFileFromText("tmp.scilla", ScillaFileType, code).descendantsOfType<T>().firstOrNull()
	}

}