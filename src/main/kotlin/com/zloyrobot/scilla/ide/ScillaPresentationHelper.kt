package com.zloyrobot.scilla.ide

import com.intellij.icons.AllIcons
import com.intellij.ide.IconProvider
import com.intellij.navigation.ColoredItemPresentation
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.psi.PsiElement
import com.zloyrobot.scilla.lang.*
import javax.swing.Icon


class ScillaIconProvider : IconProvider() {
	override fun getIcon(element: PsiElement, flags: Int): Icon? {
		return when (element) {
			is ScillaFile -> PlainTextFileType.INSTANCE.icon
			is ScillaLibrary -> AllIcons.Nodes.PpLib
			is ScillaLibraryType -> AllIcons.Nodes.Type
			is ScillaContract -> AllIcons.Nodes.Class
			is ScillaField -> AllIcons.Nodes.Field
			is ScillaTransition -> AllIcons.Nodes.Test
			is ScillaProcedure -> AllIcons.Nodes.Property
			is ScillaLibraryLet ->
				when (element.expression) {
					is ScillaFunExpression, is ScillaTFunExpression -> AllIcons.Nodes.Function
					else -> AllIcons.Nodes.Constant
				}
			else -> null
		}
	}
}
