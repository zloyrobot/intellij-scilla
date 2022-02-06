package com.zloyrobot.scilla.ide

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.ElementDescriptionUtil
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import com.zloyrobot.scilla.lang.*

class ScillaFindUsagesProvider : FindUsagesProvider {
	override fun getWordsScanner(): WordsScanner {
		return DefaultWordsScanner(ScillaLexer(), ScillaTokenType.IDENTS, ScillaTokenType.COMMENTS, ScillaTokenType.LITERALS)
	}

	override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
		return when (psiElement) {
			is ScillaIdWithType,
			is ScillaNamedTypeElement,
			is ScillaBuiltinValueElement,
			is ScillaBuiltinTypeConstructorElement,
			is ScillaLibraryTypeConstructor,
			is ScillaVarBindingElement,
			is ScillaProcedure, 
			is ScillaField, 
			is ScillaLibrary -> true
			else -> false
		}
		 
	}

	override fun getHelpId(psiElement: PsiElement): String? = null

	override fun getType(element: PsiElement): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
	}

	override fun getDescriptiveName(element: PsiElement): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE);
	}

	override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE);
	}
}