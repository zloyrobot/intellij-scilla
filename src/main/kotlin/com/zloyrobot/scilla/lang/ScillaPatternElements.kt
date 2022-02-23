package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.parentOfType


interface ScillaPattern : PsiElement {
	fun calculateMatchType(): ScillaType? {
		when (val element = parent) {
			is ScillaParenPattern -> {
				return element.calculateMatchType()
			}
			is ScillaConstructorPattern -> {
				val type = element.calculateMatchType()
				if (type is ScillaAlgebraicType) {
					val index = element.argumentPatterns.indexOf(this)
					val constructorElement = element.reference?.resolve() as? ScillaTypeConstructorElement ?: return null 
					val constructor = type.constructors.find { it.name == constructorElement.name } ?: return null
					return constructor.types.getOrNull(index)
				}
			}
			is ScillaPatternMatchClause -> {
				val match = element.parent
				if (match is ScillaMatchElement) {
					return match.subject?.expressionType
				}
			}
		}
		return null
	}
}

class ScillaConstructorPattern(node: ASTNode) : ScillaConstructorRefElement(node), ScillaPattern {
	val argumentPatterns: List<ScillaPattern> get() = findChildrenByType(ScillaElementType.PATTERNS)
}

class ScillaWildcardPattern(node: ASTNode) : ScillaPsiElement(node), ScillaPattern
class ScillaParenPattern(node: ASTNode) : ScillaPsiElement(node), ScillaPattern

class ScillaBinderPattern(node: ASTNode) : ScillaVarBindingPsiElement(node), ScillaPattern {
	override fun calculateOwnType(): ScillaType = calculateMatchType() ?: ScillaUnknownType
	
	override fun getUseScope(): SearchScope {
		return LocalSearchScope(parentOfType<ScillaPatternMatchClause>() ?: containingFile!!)
	}
}