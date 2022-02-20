package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.stubs.StubElement


interface ScillaField : ScillaNamedElement, ScillaTypeOwner

class ScillaBuiltinFieldElement(private val fieldName: String, private val type: ScillaType, private val element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaField {

	override fun getName(): String = fieldName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null
	
	override fun calculateOwnType(): ScillaType = type

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
	
	val type: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)

	override fun calculateOwnType(): ScillaType = type?.ownType ?: ScillaUnknownType
}

class ScillaFieldRefElement(node: ASTNode): ScillaNamedPsiElement(node), ScillaExpression {
	
	override fun calculateExpressionType(): ScillaType {
		val referencedElement = reference?.resolve() as? ScillaTypeOwner
		return referencedElement?.ownType ?: return ScillaUnknownType
	}

	override fun getReference(): PsiReferenceBase<ScillaFieldRefElement>? {
		val token = nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent
		
		return object: ScillaPsiReferenceBase<ScillaFieldRefElement, ScillaField>(this, null, rangeInElement) {
			override fun processFile(processor: (it: ScillaField) -> Boolean): Boolean {
				val parent = parent
				
				if (processor(ScillaBuiltinFieldElement("_balance", ScillaPrimitiveType.UINT128, element)))
					return true
				
				if (parent is ScillaLoadStatement && parent.isRemote) {
					val qualifierType = parent.qualifier?.expressionType ?: ScillaUnknownType
					if (qualifierType is ScillaAddressType) {
						val element = qualifierType.element
						if (element != null)
							return processElements(element.fields, processor)
					}
					return false
				}
				if (parent is ScillaMapGetStatement && parent.isRemote) {
					val qualifierType = parent.qualifier?.expressionType ?: ScillaUnknownType
					if (qualifierType is ScillaAddressType) {
						val element = qualifierType.element
						if (element != null)
							return processElements(element.fields, processor)
					}
					return false
				}
				else {
					return processElements(element.containingFile?.contract?.fields, processor)
				}
			}
		}
	}
}
