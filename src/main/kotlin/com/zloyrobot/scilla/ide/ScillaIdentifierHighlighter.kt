package com.zloyrobot.scilla.ide

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.ElementDescriptionUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.refactoring.suggested.startOffset
import com.intellij.usageView.UsageViewTypeLocation
import com.zloyrobot.scilla.lang.*


class ScillaIdentifierHighlighter : Annotator {
	override fun annotate(element: PsiElement, holder: AnnotationHolder) {
		val reference = element.reference
		
		val (range, element) = if (reference != null) 
			reference.rangeInElement.shiftRight(element.startOffset) to reference.resolve()
		else
			(element as? ScillaNamedElement)?.nameIdentifier?.textRange to element
		
		if (range == null)
			return
		
		val tokenAndAttribute = when (element) {
			is ScillaTypeVarBindingElement -> ScillaTextAttributeKeys.TYPE_VAR
			is ScillaTypeVarTypeElement -> ScillaTextAttributeKeys.TYPE_VAR
			is ScillaLibraryType -> ScillaTextAttributeKeys.USER_TYPE
			is ScillaBuiltinTypeElement -> ScillaTextAttributeKeys.BUILTIN_TYPE
			is ScillaLetExpression -> ScillaTextAttributeKeys.LOCAL_LET_BINDING
			is ScillaVarBindingStatement -> ScillaTextAttributeKeys.LOCAL_LET_BINDING
			is ScillaLibraryLet -> ScillaTextAttributeKeys.LIBRARY_LET_BINDING
			is ScillaLibraryTypeConstructor -> ScillaTextAttributeKeys.USER_TYPE_CONSTRUCTOR
			is ScillaBuiltinTypeConstructorElement -> ScillaTextAttributeKeys.BUILTIN_TYPE_CONSTRUCTOR
			is ScillaProcedure -> ScillaTextAttributeKeys.PROCEDURE_DECLARATION
			is ScillaCallStatement -> ScillaTextAttributeKeys.PROCEDURE_CALL
			is ScillaField -> ScillaTextAttributeKeys.FIELD
			is ScillaLibrary -> ScillaTextAttributeKeys.LIBRARY
			is ScillaContract -> ScillaTextAttributeKeys.CONTRACT
			else -> null
		}
		
		if (tokenAndAttribute != null) {
			val annotation = holder.newAnnotation(HighlightSeverity.INFORMATION, "")
			annotation.range(range)
			annotation.textAttributes(tokenAndAttribute.key)
			annotation.create()
		}
	}
}


class ScillaUnresolvedReferenceHighlighter : LocalInspectionTool() {
	override fun getDisplayName(): String = "Unresolved symbol"
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return object : PsiElementVisitor() {
			override fun visitElement(element: PsiElement) {
				val reference = element.reference ?: return
				if (reference is PsiPolyVariantReference && reference.multiResolve(false).isEmpty() ||
					reference !is PsiPolyVariantReference && reference.resolve() == null) {
					
					val kind = ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE)
					holder.registerProblem(reference, "Unresolved $kind '${reference.canonicalText}'", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
				}
			}
		}
	}
}

