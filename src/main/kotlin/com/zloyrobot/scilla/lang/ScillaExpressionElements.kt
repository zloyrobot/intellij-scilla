package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.parentOfTypes


class ScillaBuiltinValueElement(private val valueName: String, private val element: PsiElement) 
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement {

	override fun getName(): String = valueName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinValueElement && another.valueName == valueName
	}

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}

interface ScillaVarBindingElement : ScillaNamedElement

abstract class ScillaVarBindingPsiElement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaVarBindingElement 

interface ScillaExpression : PsiElement

class ScillaLiteralExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression

class ScillaRefExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	val name: ScillaName? get() = findChildByType(ScillaElementType.REFS)
	
	override fun getReference(): PsiReferenceBase<ScillaRefExpression>? {
		val ref = name ?: return null
		val name = ref.name
		val token = ref.nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent.shiftRight(ref.startOffsetInParent)
		
		
		return object : ScillaPsiReferenceBase<ScillaRefExpression, ScillaNamedElement>(this, ref, rangeInElement) {
			override fun processFile(processor: (it: ScillaNamedElement) -> Boolean): Boolean {
				var parent: PsiElement? = element
				while (parent != null) {
					when (parent) {
						is ScillaStatement -> {
							if (parent is ScillaVarBindingStatement)
								if (processor(parent)) return true
							
							var left = parent.prevSibling
							while (left != null) {
								if (left is ScillaVarBindingStatement)
									if (processor(left)) return true									
								
								left = left.prevSibling
							}
						}
						is ScillaVarBindingElement -> {
							if (processor(parent)) return true
						}
						is ScillaPatternMatchClause -> {
							val pattern = parent.pattern
							if (pattern != null)
								if (processElements(pattern.collectDescendantsOfType<ScillaBinderPattern>(), processor))
									return true
						}
						is ScillaParametersOwner -> {
							if (processElements(parent.parameterList?.parameters, processor))
								return true
							
							if (parent is ScillaComponent<*, *>)  {
								if (processor(ScillaBuiltinValueElement("_sender", parent)) 
									|| processor(ScillaBuiltinValueElement("_amount", parent)) 
									|| processor(ScillaBuiltinValueElement("_origin", parent)))
									return true
							}								
							
							if (parent is ScillaContract) {
								if (processor(ScillaBuiltinValueElement("_this_address", parent))
									|| processor(ScillaBuiltinValueElement("_creation_block", parent))
									|| processor(ScillaBuiltinValueElement("_scilla_version", parent)))
									return true
							}
						}
						is ScillaFile -> {
							if (processor(ScillaBuiltinValueElement("list_foldl", parent))
								|| processor(ScillaBuiltinValueElement("list_foldr", parent))
								|| processor(ScillaBuiltinValueElement("list_foldk", parent))
								|| processor(ScillaBuiltinValueElement("nat_fold", parent))
								|| processor(ScillaBuiltinValueElement("nat_foldk", parent)))
								return true
							
							if (processCurrentAndImportedLibraries(processor))
								return true
						}
					}
					parent = parent.parentOfTypes(
						ScillaVarBindingPsiElement::class,
						ScillaParametersOwner::class,
						ScillaStatement::class,
						ScillaPatternMatchClause::class,
						ScillaFile::class
					)
				}
				return false
			}
			
			override fun processLibrary(library: ScillaLibrary, processor: (it: ScillaNamedElement) -> Boolean): Boolean {
				return processElements(library.vars, processor)
			}
		}
	}		
}

class ScillaLetExpression(node: ASTNode) : ScillaVarBindingPsiElement(node), ScillaExpression
class ScillaMessageExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression
class ScillaMessageEntry(node: ASTNode) : ScillaPsiElement(node), ScillaExpression
class ScillaMessageEntryValue(node: ASTNode) : ScillaPsiElement(node), ScillaExpression

class ScillaFunExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression, ScillaParametersOwner {
	override val parameterList: ScillaParameters? get() = findChildByType(ScillaElementType.PARAMETERS)
}

class ScillaAppExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression

abstract class ScillaConstructorRefElement(node: ASTNode) : ScillaPsiElement(node) {
	val name: ScillaName? get() = findChildByType(ScillaElementType.REFS)

	override fun getReference(): PsiReferenceBase<ScillaConstructorRefElement>? {
		val ref = name ?: return null
		val token = ref.nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent.shiftRight(ref.startOffsetInParent)

		return object : ScillaPsiReferenceBase<ScillaConstructorRefElement, ScillaTypeConstructorElement>(this, ref, rangeInElement) {
			
			override fun processFile(processor: (it: ScillaTypeConstructorElement) -> Boolean): Boolean {
				if (ScillaAlgebraicType.processBuiltinTypeConstructors { type, constructor -> 
					processor(ScillaBuiltinTypeConstructorElement(type, constructor.name, element)) }) 
					return true
				
				return processCurrentAndImportedLibraries(processor)
			}
			
			override fun processLibrary(library: ScillaLibrary, processor: (it: ScillaTypeConstructorElement) -> Boolean): Boolean {
				for (type in library.types) {
					for (constructor in type.constructors) {
						if (processor(constructor))
							return true
					}
				}
				return false
			}
		}
	}
}

class ScillaConstructorExpression(node: ASTNode) : ScillaConstructorRefElement(node), ScillaExpression {
}

class ScillaMatchExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression, ScillaMatchElement {
	override val matchKeyword: PsiElement get() = findChildByType(ScillaTokenType.MATCH)!!
	override val subject: ScillaName? get() = findChildByType(ScillaElementType.REFS)
	override val withKeyword: PsiElement? get() = findChildByType(ScillaTokenType.WITH)
	override val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
}

class ScillaBuiltinExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression

class ScillaTFunExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression, ScillaTypeVarBindingElement {
	override fun getNavigationElement(): PsiElement = nameIdentifier ?: this
	override fun getName(): String? = nameIdentifier?.text.orEmpty()
	override fun setName(name: String): PsiElement = TODO("Not yet implemented")

	override fun getNameIdentifier(): PsiElement? =  findChildByType(ScillaTokenType.IDENTS) 
	
}

class ScillaTAppExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression

