package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentsOfType


interface ScillaTypeElement : ScillaTypeOwner

interface ScillaNamedTypeElement : ScillaTypeElement, ScillaNamedElement

interface ScillaTypeVarBindingElement : ScillaNamedElement {
	val typeVar: ScillaTypeVarType?
}

interface ScillaTypeOwner : PsiElement {
	fun calculateOwnType(): ScillaType

	val ownType: ScillaType get() {
		return CachedValuesManager.getCachedValue(this) {
			CachedValueProvider.Result.create(this.calculateOwnType(), PsiModificationTracker.MODIFICATION_COUNT)
		}
	}
}

class ScillaBuiltinTypeElement(private val _type: ScillaNamedType, private val element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement, ScillaNamedTypeElement {

	override fun getName(): String = _type.typeName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinTypeElement && another._type == _type
	}

	override fun calculateOwnType(): ScillaType = _type

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}

class ScillaBuiltinTypeConstructorElement(private val _type: ScillaType, private val name: String, private val element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement, ScillaTypeConstructorElement {

	override fun getName(): String = name
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null
	
	override fun calculateOwnType(): ScillaType = _type

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinTypeConstructorElement && another._type == _type && another.name == name
	}

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}



class ScillaRefTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement {
	val name: ScillaName? get() = findChildByType(ScillaElementType.REFS)
	val typeArguments: List<ScillaTypeElement> get() = findChildrenByType(ScillaElementType.TYPES)
	
	override fun calculateOwnType(): ScillaType {
		val referencedTypeElement = reference?.resolve() as? ScillaTypeElement
		val type = referencedTypeElement?.ownType ?: return ScillaUnknownType
		
		if (typeArguments.isEmpty())
			return type
		
		if (type is ScillaPolyAlgebraicType) {
			return ScillaPolyAlgebraicTypeApplication(type, typeArguments.map { it.ownType })
		}
		
		return ScillaUnknownType
	}

	override fun getReference(): PsiReferenceBase<ScillaRefTypeElement>? {
		val ref = name ?: return null
		val token = ref.nameIdentifier ?: return null
		val name = ref.name
		
		val rangeInElement = token.textRangeInParent.shiftRight(ref.startOffsetInParent)
		return object: ScillaPsiReferenceBase<ScillaRefTypeElement, ScillaNamedTypeElement>(this, ref, rangeInElement) {
			
			override fun processFile(processor: (it: ScillaNamedTypeElement) -> Boolean): Boolean {
				if (ScillaPrimitiveType.processBuiltinTypes { processor(ScillaBuiltinTypeElement(it, element)) })
					return true
				
				if (ScillaSimpleAlgebraicType.processBuiltinTypes { processor(ScillaBuiltinTypeElement(it, element)) })
					return true
				
				if (name.startsWith("ByStr") && name != "ByStr") {
					try {
						val size = name.substring(5).toInt()
						if (processor(ScillaBuiltinTypeElement(ScillaByStrType(size), element)))
							return true
					} catch (_: NumberFormatException) {
					}
				}
				else {
					if (processor(ScillaBuiltinTypeElement(ScillaByStrType.ByStr20, element)))
						return true
					if (processor(ScillaBuiltinTypeElement(ScillaByStrType.ByStr32, element)))
						return true
				}

				return processCurrentAndImportedLibraries(processor)
			}

			override fun processLibrary(library: ScillaLibrary, processor: (it: ScillaNamedTypeElement) -> Boolean): Boolean {
				return processElements(library.types, processor)
			}
		}
	}
}

class ScillaTypeVarTypeElement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaNamedTypeElement {
	override fun calculateOwnType(): ScillaType {
		val referencedTypeElement = reference?.resolve() as? ScillaTypeVarBindingElement
		return referencedTypeElement?.typeVar ?: return ScillaTypeVarType(name)
	}

	override fun getReference(): PsiReferenceBase<ScillaTypeVarTypeElement>? {
		val name = name
		val token = nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent
		
		return object: ScillaPsiReferenceBase<ScillaTypeVarTypeElement, ScillaTypeVarBindingElement>(this, null, rangeInElement) {
			override fun resolve(): PsiElement? {
				return parentsOfType<ScillaTypeVarBindingElement>().find { it.name == name }
			}

			override fun processFile(processor: (it: ScillaTypeVarBindingElement) -> Boolean): Boolean {
				return processElements(parentsOfType<ScillaTypeVarBindingElement>(), processor)
			}
		}
	}

}

class ScillaMapTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement {
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
	}
}

class ScillaFunTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement {
	val paramType: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)
	val resultType: ScillaTypeElement? get() {
		return findChildrenByType<ScillaTypeElement>(ScillaElementType.TYPES).dropWhile { it == paramType }.firstOrNull()
	}	
		
	override fun calculateOwnType(): ScillaType {
		val paramType = paramType?.ownType ?: ScillaUnknownType
		val resultType = resultType?.ownType ?: ScillaUnknownType
		return ScillaFunType(paramType, resultType)
	}
}

class ScillaPolyTypeElement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaTypeElement, ScillaTypeVarBindingElement {
	val body: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)

	override val typeVar: ScillaTypeVarType get() = ScillaTypeVarType(name)

	override fun calculateOwnType(): ScillaType {
		return ScillaPolyFunType(typeVar, body?.ownType ?: ScillaUnknownType)
	}
}


class ScillaParenTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement {
	val innerType: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)
	
	override fun calculateOwnType(): ScillaType = innerType?.ownType ?: ScillaUnknownType  
}

class ScillaAddressTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement {
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
	}
}

class ScillaAddressTypeField(node: ASTNode) : ScillaPsiElement(node)
