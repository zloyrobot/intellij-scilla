package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.stubs.StubElement


interface ScillaField : ScillaNamedElement

class ScillaBuiltinFieldElement(private val fieldName: String, private val element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaField {

	override fun getName(): String = fieldName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinFieldElement && another.fieldName == fieldName
	}

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}


class ScillaUserFieldStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaUserField>(parent, ScillaElementType.FIELD_DEFINITION, name)

class ScillaUserField : ScillaContractEntry<ScillaUserFieldStub, ScillaUserField>, ScillaField {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaUserFieldStub) : super(stub)
}

class ScillaFieldRefElement(node: ASTNode): ScillaNamedPsiElement(node) {
	override fun getReference(): PsiReferenceBase<ScillaFieldRefElement>? {
		val token = nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent

		val parent = parent //TODO implement resolve of remote fields
		if (parent is ScillaLoadStatement && parent.isRemote)
			return null
		if (parent is ScillaMapGetStatement && parent.isRemote)
			return null

		return object: ScillaPsiReferenceBase<ScillaFieldRefElement, ScillaField>(this, null, rangeInElement) {
			override fun processFile(processor: (it: ScillaField) -> Boolean): Boolean {
				if (processor(ScillaBuiltinFieldElement("_balance", element)))
					return true

				return processElements(element.containingFile?.contract?.fields, processor)
			}
		}
	}
}
