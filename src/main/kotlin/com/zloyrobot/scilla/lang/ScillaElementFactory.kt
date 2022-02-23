package com.zloyrobot.scilla.lang

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.descendantsOfType

class ScillaElementFactory(private val project: Project) {
	val factory = PsiFileFactory.getInstance(project)
	
	fun createIdent(name: String): PsiElement {
		return if (name.startsWith("'"))
			create<ScillaTFunExpression>("library Lib\nlet xxx = tfun $name => 0x0")!!.nameIdentifier!!
		else if (name.first().isUpperCase())
			create<ScillaLibrary>("library $name\nlet xxx = 0x0")!!.nameIdentifier!!
		else
			create<ScillaLibraryLet>("library Lib\nlet $name = 0x0")!!.nameIdentifier!! 
	}
	
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