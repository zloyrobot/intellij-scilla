package com.zloyrobot.scilla.ide

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.zloyrobot.scilla.lang.*

class ScillaRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
		return PsiSearchHelper.getInstance(element.project).getUseScope(element) is LocalSearchScope
	}
}

class ScillaVetoRenameCondition : Condition<PsiElement> {
	override fun value(element: PsiElement?): Boolean {
		if (element is ScillaNamedElement && element.parent is ScillaMessageEntry)
			return true
		if (element is ScillaContract)
			return true
		
		return false
	}
}

class ScillaNamesValidator : NamesValidator {
	override fun isKeyword(name: String, project: Project): Boolean {
		val lexer = ScillaLexer()
		lexer.start(name)
		return (lexer.tokenType in ScillaTokenType.KEYWORDS && lexer.tokenText == name)
	}

	override fun isIdentifier(name: String, project: Project): Boolean {
		val lexer = ScillaLexer()
		lexer.start(name)
		return (lexer.tokenType in ScillaTokenType.IDENTS && lexer.tokenText == name)
	}
}

class ScillaRenameTypeVarElementProcessor : RenamePsiElementProcessor() {
	override fun canProcessElement(element: PsiElement): Boolean {
		return element is ScillaTFunExpression
	}
} 