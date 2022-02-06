package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.util.parentsOfType


interface ScillaType {
	val typeName: String
}

data class ScillaByStrXType(override val typeName: String) : ScillaType

enum class ScillaPrimitiveType(override val typeName: String) : ScillaType {
	Int32("Int32"),
	Int64("Int64"),
	Int128("Int128"),
	Int256("Int256"),
	Uint32("Uint32"),
	Uint64("Uint64"),
	Uint128("Uint128"),
	Uint256("Uint256"),
	StringType("String"),
	BNum("BNum"),
	Message("Message"),
	Event("Event"),
	ByStr("ByStr");
	
	companion object {
		fun processBuiltinTypes(processor: (type: ScillaPrimitiveType) -> Boolean): Boolean {
			for (type in ScillaPrimitiveType.values()) {
				if (processor(type))
					return true
			}
			return false
		}
		
		fun lookupType(name: String): ScillaPrimitiveType? {
			return ScillaPrimitiveType.values().find { it.typeName == name }
		}
	}
}

//TODO: Support type parameters
class ScillaAlgebraicType(override val typeName: String, vararg val constructors: Constr): ScillaType {
	
	class Constr(val name: String, vararg val types: ScillaType) {
	}
	companion object {
		private val BOOL = ScillaAlgebraicType("Bool", Constr("True"), Constr("False"))
		private val OPTION = ScillaAlgebraicType("Option", Constr("Some"), Constr("None") )
		private val LIST = ScillaAlgebraicType("List", Constr("Cons"), Constr("Nil") )
		private val PAIR = ScillaAlgebraicType("Pair", Constr("Pair"))
		private val NAT = ScillaAlgebraicType("Nat", Constr("Zero"), Constr("Succ"))
		
		private val TYPES = listOf(BOOL, OPTION, LIST, PAIR, PAIR, NAT)
		
		fun processBuiltinTypes(processor: (type: ScillaAlgebraicType) -> Boolean): Boolean {
			for (type in TYPES) {
				if (processor(type))
					return true
			}
			return false
		}

		fun processBuiltinTypeConstructors(processor: (type: ScillaAlgebraicType, constructor: Constr) -> Boolean): Boolean {
			for (type in TYPES) {
				for (constructor in type.constructors)
				if (processor(type, constructor))
					return true
			}
			return false
		}
	}
}

class ScillaBuiltinTypeElement(private val type: ScillaType, private val element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement, ScillaNamedTypeElement {

	override fun getName(): String = type.typeName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinTypeElement && another.type == type
	}

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}

class ScillaBuiltinTypeConstructorElement(private val type: ScillaType, private val name: String, private val element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement, ScillaTypeConstructorElement {

	override fun getName(): String = name
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinTypeConstructorElement && another.type == type && another.name == name
	}

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}



interface ScillaTypeVarBindingElement : ScillaNamedElement {
}

interface ScillaTypeElement : PsiElement
interface ScillaNamedTypeElement : ScillaTypeElement, ScillaNamedElement

class ScillaRefTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement {
	val name: ScillaName? get() = findChildByType(ScillaElementType.REFS)

	override fun getReference(): PsiReferenceBase<ScillaRefTypeElement>? {
		val ref = name ?: return null
		val token = ref.nameIdentifier ?: return null
		val name = ref.name
		
		val rangeInElement = token.textRangeInParent.shiftRight(ref.startOffsetInParent)
		return object: ScillaPsiReferenceBase<ScillaRefTypeElement, ScillaNamedTypeElement>(this, ref, rangeInElement) {
			
			override fun processFile(processor: (it: ScillaNamedTypeElement) -> Boolean): Boolean {
				if (ScillaPrimitiveType.processBuiltinTypes { processor(ScillaBuiltinTypeElement(it, element)) })
					return true
				
				if (ScillaAlgebraicType.processBuiltinTypes { processor(ScillaBuiltinTypeElement(it, element)) })
					return true
				
				if (name.startsWith("ByStr") && name != "ByStr") {
					if (processor(ScillaBuiltinTypeElement(ScillaByStrXType(name), element)))
						return true
				}
				else {
					if (processor(ScillaBuiltinTypeElement(ScillaByStrXType("ByStr20"), element)))
						return true
					if (processor(ScillaBuiltinTypeElement(ScillaByStrXType("ByStr32"), element)))
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

class ScillaMapTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement
class ScillaFunTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement
class ScillaPolyTypeElement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaTypeElement, ScillaTypeVarBindingElement
class ScillaAddressTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement
class ScillaAddressTypeField(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement
class ScillaParenTypeElement(node: ASTNode) : ScillaPsiElement(node), ScillaTypeElement