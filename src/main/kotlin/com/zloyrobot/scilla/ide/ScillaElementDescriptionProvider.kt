package com.zloyrobot.scilla.ide

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewShortNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import com.zloyrobot.scilla.lang.*

class ScillaElementDescriptionProvider : ElementDescriptionProvider {
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		when (location) {
			is UsageViewTypeLocation -> {
				val parent = if (element is ScillaName) element.parent else element
				return when(parent) {
					is ScillaLibraryType -> "user type"
					is ScillaBuiltinTypeElement -> "builtin type"
					is ScillaTypeElement -> "type"
					is ScillaBuiltinTypeConstructorElement -> "builtin type constructor"
					is ScillaRefExpression -> "value"
					is ScillaConstructorRefElement -> "type constructor"
					is ScillaLibraryTypeConstructor -> "user type constructor"
					is ScillaVarBindingElement -> "value"
					is ScillaProcedure -> "procedure"
					is ScillaTransition -> "transition"
					is ScillaUserField -> "field"
					is ScillaBuiltinFieldElement -> "builtin field"
					is ScillaAddressTypeField -> "remote field"
					is ScillaLibrary -> "library"
					is ScillaIdWithType -> {
						when(parent.parent) {
							is ScillaComponentParameters -> "component parameter"
							is ScillaContractParameters -> "contract parameter"
							is ScillaFunctionParameters -> "function parameter"
							else -> "parameter"
						}
					}
					is ScillaBuiltinValueElement -> "builtin value"
					else -> ""
				}
			}
			is UsageViewLongNameLocation,
			is UsageViewShortNameLocation -> {
				if (element is ScillaName)
					element.qualifiedName
				else {
					val name = element.children.find { it is ScillaName } as? ScillaName
					name?.qualifiedName
				}
			}
		}
		return null
	}
}