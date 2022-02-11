package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentsOfType


interface ScillaStatement : PsiElement

abstract class ScillaVarBindingStatement(node: ASTNode) : ScillaVarBindingPsiElement(node), ScillaStatement {
}

class ScillaStatementList(node: ASTNode) : ScillaPsiElement(node) {
	val statements : List<ScillaStatement> get() = findChildrenByType(ScillaElementType.STATEMENTS)
}

class ScillaBindStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
	}
}

class ScillaLoadStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	val address: PsiElement? get() = findChildByType(ScillaTokenType.DOT)
	val isRemote: Boolean get() = address != null
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
	}
}

class ScillaMapGetStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	val address: PsiElement? get() = findChildByType(ScillaTokenType.DOT)
	val isRemote: Boolean get() = address != null
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
	}
}
class ScillaReadFromBCStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
	}
}

class ScillaTypeCastStatement(node: ASTNode) : ScillaVarBindingStatement(node) {
	override fun calculateOwnType(): ScillaType {
		TODO("Not yet implemented")
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
