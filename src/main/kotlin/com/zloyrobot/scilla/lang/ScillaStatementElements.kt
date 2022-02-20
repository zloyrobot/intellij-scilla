package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase


interface ScillaStatement : PsiElement

abstract class ScillaVarBindingStatement(node: ASTNode) : ScillaVarBindingPsiElement(node), ScillaStatement {
}

class ScillaStatementList(node: ASTNode) : ScillaPsiElement(node) {
	val statements : List<ScillaStatement> get() = findChildrenByType(ScillaElementType.STATEMENTS)
}

class ScillaBindStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	val initializer: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
	
	override fun calculateOwnType(): ScillaType = initializer?.expressionType ?: ScillaUnknownType
}

class ScillaLoadStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	val isRemote: Boolean get() = findChildByType<PsiElement>(ScillaTokenType.DOT) != null
	val isExist: Boolean get() =  findChildByType<PsiElement>(ScillaTokenType.EXISTS) != null
	val field: ScillaFieldRefElement? get() = findChildByType(ScillaElementType.FIELD_REF)
	val qualifier get() = findChildrenByType<ScillaExpression>(ScillaElementType.EXPRESSIONS).find { it != field }
	
	override fun calculateOwnType(): ScillaType {
		if (isExist)
			return ScillaSimpleAlgebraicType.BOOL
		
		return field?.expressionType ?: ScillaUnknownType
	}
}

class ScillaMapGetStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	val isRemote: Boolean get() = findChildByType<PsiElement>(ScillaTokenType.DOT) != null
	val isExist: Boolean get() =  findChildByType<PsiElement>(ScillaTokenType.EXISTS) != null
	val field: ScillaExpression? get() = findChildByType(ScillaElementType.FIELD_REF)
	val qualifier get() = findChildrenByType<ScillaExpression>(ScillaElementType.EXPRESSIONS).find { it != field }
	val arguments: List<ScillaMapAccess> get() = findChildrenByType(ScillaElementType.MAP_ACCESS)
	
	override fun calculateOwnType(): ScillaType {
		if (isExist)
			return ScillaSimpleAlgebraicType.BOOL
		
		var mapType = field?.expressionType ?: ScillaUnknownType
		for (argument in arguments) {
			if (mapType is ScillaMapType) {
				mapType = mapType.valueType
			}
			else return ScillaUnknownType 
		}
		return ScillaPolyTypeApplication(ScillaSimpleAlgebraicType.OPTION, listOf(mapType))
	}
}

class ScillaReadFromBCStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	override fun calculateOwnType(): ScillaType = ScillaPrimitiveType.BNUM
}

class ScillaTypeCastStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	val type: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)
	
	override fun calculateOwnType(): ScillaType {
		return ScillaPolyTypeApplication(ScillaSimpleAlgebraicType.OPTION, listOf(type?.ownType ?: ScillaUnknownType))
	}
}

class ScillaIterateStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaAcceptStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaEventStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaThrowStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaSendStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaMapDeleteStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaMapUpdateStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement
class ScillaStoreStatement(node: ASTNode) : ScillaPsiElement(node), ScillaStatement

class ScillaCallStatement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaStatement {
	override fun getReference(): PsiReferenceBase<ScillaCallStatement>? {
		val token = nameIdentifier ?: return null
		val rangeInElement = token.textRangeInParent
		
		return object: ScillaPsiReferenceBase<ScillaCallStatement, ScillaProcedure>(this, null, rangeInElement) {
			override fun processFile(processor: (it: ScillaProcedure) -> Boolean): Boolean {
				return processElements(element.containingFile?.contract?.procedures, processor)
			}
		}
	}
}

class ScillaMatchStatement(node: ASTNode) : ScillaNamedPsiElement(node), ScillaStatement, ScillaMatchElement {
	override val matchKeyword: PsiElement get() = findChildByType(ScillaTokenType.MATCH)!!
	override val subject: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
	override val withKeyword: PsiElement? get() = findChildByType(ScillaTokenType.WITH)
	override val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
}
