package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.light.LightElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.*
import com.intellij.util.containers.map2Array


class ScillaBuiltinValueElement(private val valueName: String, private val type: ScillaType, element: PsiElement) 
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement, ScillaTypeOwner {

	override fun getName(): String = valueName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinValueElement && another.valueName == valueName
	}

	override fun calculateOwnType(): ScillaType = type

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}

interface ScillaVarBindingElement : ScillaNamedElement, ScillaTypeOwner

abstract class ScillaVarBindingPsiElement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaVarBindingElement {
	override fun getUseScope(): SearchScope {
		return LocalSearchScope(parent)
	}
} 

interface ScillaExpression : PsiElement {
	fun calculateExpressionType(): ScillaType

	val expressionType: ScillaType get() {
		return CachedValuesManager.getCachedValue(this) {
			CachedValueProvider.Result.create(this.calculateExpressionType(), PsiModificationTracker.MODIFICATION_COUNT)
		}
	}
}

class ScillaLiteralExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	override fun calculateExpressionType(): ScillaType {
		val token = firstChild
		val name = token.text
		
		return when (token.elementType) {
			ScillaTokenType.CID -> ScillaPrimitiveType.lookupType(name) ?: ScillaUnknownType
			ScillaTokenType.STRING -> ScillaPrimitiveType.STRING
			ScillaTokenType.HEX -> {
				val text = if (name.startsWith("0x"))
					name.substring(2)
				else 
					throw AssertionError("Invalid hex literal")
				
				ScillaByStrType(text.length / 2)
			}
			ScillaTokenType.EMP -> {
				val typeArguments = findChildrenByType<ScillaTypeElement>(ScillaElementType.TYPES)
				if (typeArguments.size == 2)
					return ScillaMapType(typeArguments[0].ownType, typeArguments[1].ownType)
				else ScillaUnknownType 
			}
			else -> ScillaUnknownType
		} 
	} 

}

class ScillaRefExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	val name: ScillaName? get() = findChildByType(ScillaElementType.REFS)

	override fun calculateExpressionType(): ScillaType {
		val referencedElement = reference?.resolve() as? ScillaTypeOwner
		return referencedElement?.ownType ?: return ScillaUnknownType
	}
	
	override fun getReference(): PsiReferenceBase<ScillaRefExpression>? {
		val ref = name ?: return null
		val token = ref.nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent.shiftRight(ref.startOffsetInParent)
		
		return object : ScillaPsiReferenceBase<ScillaRefExpression, ScillaNamedElement>(this, ref, rangeInElement) {
			override fun processFile(processor: (it: ScillaNamedElement) -> Boolean): Boolean {
				var parent: PsiElement? = element
				while (parent != null) {
					when (parent) {
						is ScillaStatement -> {
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
						is ScillaStatementPatternMatchClause -> {
							val pattern = parent.pattern
							if (pattern != null)
								if (processElements(pattern.descendantsOfType<ScillaBinderPattern>(), processor))
									return true
						}
						is ScillaExpressionPatternMatchClause -> {
							val pattern = parent.pattern
							if (pattern != null)
								if (processElements(pattern.descendantsOfType<ScillaBinderPattern>(), processor))
									return true
						}
						is ScillaParametersOwner -> {
							if (processElements(parent.parameterList?.parameters, processor))
								return true
							
							if (parent is ScillaComponent<*, *>)  {
								if (processor(parent._sender.value) 
									|| processor(parent._amount.value) 
									|| processor(parent._origin.value))
									return true
							}								
							
							if (parent is ScillaContract) {
								if (processor(parent._this_address.value)
									|| processor(parent._creation_block.value)
									|| processor(parent._scilla_version.value))
									return true
							}
						}
						is ScillaFile -> {
							if (processor(parent.list_foldl.value) 
								|| processor(parent.list_foldr.value)
								|| processor(parent.list_foldk.value)
								|| processor(parent.nat_fold.value)
								|| processor(parent.nat_foldk.value))
								return true
							
							if (processCurrentAndImportedLibraries(processor))
								return true
						}
					}
					parent = parent.parentOfTypes(
						ScillaVarBindingPsiElement::class,
						ScillaParametersOwner::class,
						ScillaStatement::class,
						ScillaStatementPatternMatchClause::class,
						ScillaExpressionPatternMatchClause::class,
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
interface ScillaLetElement : ScillaVarBindingElement {
	val type: ScillaTypeElement?
	val initializer: ScillaExpression?
}

class ScillaLetExpression(node: ASTNode) : ScillaVarBindingPsiElement(node), ScillaExpression, ScillaLetElement {
	override val type: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)
	
	override val initializer: ScillaExpression? get() {
		val inToken = findChildByType<PsiElement>(ScillaTokenType.IN) ?: return findChildByType(ScillaElementType.EXPRESSIONS)
		return inToken.siblings(forward = false).find { it is ScillaExpression } as? ScillaExpression
	}
	
	val body: ScillaExpression? get() {
		val inToken = findChildByType<PsiElement>(ScillaTokenType.IN)
		return inToken?.siblings(forward = true)?.find { it is ScillaExpression } as? ScillaExpression
	} 
	
	override fun calculateExpressionType(): ScillaType {
		return body?.expressionType ?: ScillaUnknownType
	}

	override fun calculateOwnType(): ScillaType {
		if (type != null)
			return type!!.ownType

		return initializer?.expressionType ?: ScillaUnknownType
	}
	
	override fun getUseScope(): SearchScope {
		return LocalSearchScope(this)
	}
}

class ScillaMessageExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	override fun calculateExpressionType(): ScillaType = ScillaPrimitiveType.MESSAGE
}

class ScillaMessageEntry(node: ASTNode) : ScillaPsiElement(node) {
	val tag: ScillaName? get() = findChildByType(ScillaElementType.REFS)
	val initializer: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
}

class ScillaFunExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression, ScillaParametersOwner {
	override val parameterList: ScillaParameters? get() = findChildByType(ScillaElementType.PARAMETERS)
	val body: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
	
	override fun calculateExpressionType(): ScillaType {
		val paramElement = parameterList?.parameters?.firstOrNull()
		val paramType = paramElement?.ownType ?: ScillaUnknownType
		val bodyType = body?.expressionType ?: ScillaUnknownType
		
		return ScillaFunType(paramType, bodyType)
	}
}

class ScillaAppExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	val function: ScillaRefExpression? get() = findChildByType(ScillaElementType.REF_EXPRESSION)
	val arguments: List<ScillaRefExpression> get() = findChildrenByType<ScillaRefExpression?>(ScillaElementType.REF_EXPRESSION).dropWhile { it == function }
	
	override fun calculateExpressionType(): ScillaType {
		var type = function?.expressionType 
		for (arg in arguments) {
			val functionType = type as? ScillaFunType ?: return ScillaUnknownType
			type = functionType.resultType
		}
		return type?: ScillaUnknownType 
	}

}

abstract class ScillaConstructorRefElement(node: ASTNode) : ScillaPsiElement(node) {
	val name: ScillaName? get() = findChildByType(ScillaElementType.REFS)

	override fun getReference(): PsiReferenceBase<ScillaConstructorRefElement>? {
		val ref = name ?: return null
		val token = ref.nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent.shiftRight(ref.startOffsetInParent)

		return object : ScillaPsiReferenceBase<ScillaConstructorRefElement, ScillaTypeConstructorElement>(this, ref, rangeInElement) {
			
			override fun processFile(processor: (it: ScillaTypeConstructorElement) -> Boolean): Boolean {
				if (ScillaSimpleAlgebraicType.processBuiltinTypeConstructors { type, constructor -> 
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
	val typeArguments: List<ScillaTypeElement> get() = findChildrenByType(ScillaElementType.TYPES)
	
	override fun calculateExpressionType(): ScillaType {
		val referencedElement = reference?.resolve() as? ScillaTypeOwner
		val type = referencedElement?.ownType ?: return ScillaUnknownType

		if (typeArguments.isEmpty())
			return type
		
		if (type is ScillaPolyAlgebraicType) {
			return ScillaPolyTypeApplication(type, typeArguments.map { it.ownType })
		}
		return ScillaUnknownType
	}
}

class ScillaMatchExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression, ScillaMatchElement {
	override val matchKeyword: PsiElement get() = findChildByType(ScillaTokenType.MATCH)!!
	override val subject: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
	override val withKeyword: PsiElement? get() = findChildByType(ScillaTokenType.WITH)
	override val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
	
	val cases: List<ScillaExpressionPatternMatchClause> get() = findChildrenByType(ScillaElementType.EXPRESSION_PATTERN_MATCH_CLAUSE)
	
	override fun calculateExpressionType(): ScillaType {
		//TODO: unification of all types?
		return cases.firstOrNull()?.body?.expressionType ?: ScillaUnknownType
	}
}

class ScillaBuiltinExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	val builtinId: PsiElement? get() = findChildByType(ScillaTokenType.IDENTS)
	val arguments: List<ScillaExpression> get() = findChildrenByType(ScillaElementType.EXPRESSIONS)
	
	override fun getName(): String? = builtinId?.text

	override fun calculateExpressionType(): ScillaType {
		val referencedElement = reference?.resolve() as? ScillaBuiltinFunctionElement ?: return ScillaUnknownType
		val argumentTypes = arguments.map { it.expressionType }.toTypedArray()
		
		val overload = referencedElement.function.functionSignatures.firstNotNullOfOrNull {
			if (it.typeParams.isEmpty()) {
				if (it.paramTypes.contentEquals(argumentTypes)) it
				else null
			} else if (it.paramTypes.size == argumentTypes.size) {
				val deduction = ScillaTypeDeduction()
				for (p in it.paramTypes.zip(argumentTypes)) {
					deduction.deduce(p.first, p.second)
				}
				if (deduction.errors.isEmpty()) {
					val returnType = deduction.substitute(it.returnType)
					val paramTypes = it.paramTypes.map2Array { deduction.substitute(it) }
					ScillaFunctionSignature(returnType, *paramTypes)
				} else null
			} else null
		}
		return overload?.returnType ?: ScillaUnknownType
	}

	override fun getReference(): PsiReferenceBase<ScillaBuiltinExpression>? {
		val token = builtinId ?: return null
		val rangeInElement = token.textRangeInParent

		return object : ScillaPsiReferenceBase<ScillaBuiltinExpression, ScillaBuiltinFunctionElement>(this, null, rangeInElement) {
			override fun processFile(processor: (it: ScillaBuiltinFunctionElement) -> Boolean): Boolean {
				if (ScillaBuiltinFunction.processBuiltinFunctions {
						processor(ScillaBuiltinFunctionElement(it, element)) 
				}) 
					return true
				

				return false
			}
		}
	}
}

class ScillaTFunExpression(node: ASTNode) : ScillaNamedPsiElement(node), ScillaExpression, ScillaTypeVarBindingElement {
	override val typeVar: ScillaTypeVarType get() = ScillaTypeVarType(name)
	val body: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)

	override fun calculateExpressionType(): ScillaType {
		return ScillaTypeFunType(typeVar, body?.expressionType ?: ScillaUnknownType)
	}

	override fun getUseScope(): SearchScope {
		return LocalSearchScope(this)
	}
}

class ScillaTAppExpression(node: ASTNode) : ScillaPsiElement(node), ScillaExpression {
	val function: ScillaRefExpression? get() = findChildByType(ScillaElementType.REF_EXPRESSION)
	val arguments: List<ScillaTypeElement> get() = findChildrenByType(ScillaElementType.TYPES)
	
	override fun calculateExpressionType(): ScillaType {
		var type = function?.expressionType
		for (arg in arguments) {
			val functionType = type as? ScillaTypeFunType ?: return ScillaUnknownType
			type = ScillaTypeSubstitution(functionType.typeParameter, arg.ownType).substitute(functionType.body)
		}
		
		return type?: ScillaUnknownType

	}
}

