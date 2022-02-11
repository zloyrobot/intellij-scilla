package com.zloyrobot.scilla.ide

import com.intellij.codeInsight.hints.*
import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.util.skipTokens
import com.intellij.refactoring.suggested.endOffset
import com.intellij.ui.layout.panel
import com.zloyrobot.scilla.lang.*
import javax.swing.JPanel

class ScillaTypeInfoProvider : ExpressionTypeProvider<ScillaExpression>() {
	override fun getExpressionsAt(pivot: PsiElement): List<ScillaExpression> {
		val expression = pivot.parentsOfType<ScillaExpression>(true).toList()
		
		if (expression.isEmpty())
			return expression
		
		val index = expression.indexOfFirst { it is ScillaAppExpression || it is ScillaTAppExpression}
		if (index != -1) 
			return expression.take(index + 1).toList()
		
		return listOf(expression.first())
	}

	override fun getInformationHint(element: ScillaExpression): String {
		return StringUtil.escapeXmlEntities(element.expressionType.presentation)
	}

	override fun getErrorHint() = "Select an expression"
}

open class ScillaTypeHintsInlayProvider : InlayHintsProvider<ScillaTypeHintsInlayProvider.Settings> {
	companion object {
		val KEY: SettingsKey<Settings> = SettingsKey("SCILLA_TYPE_HINTS")
	}

	
	override fun getCollectorFor(file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): FactoryInlayHintsCollector {
		return object : FactoryInlayHintsCollector(editor) {
			
			override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
				when (element) {
					is ScillaLetElement -> {
						if ((!settings.showForLibraryBindings || (element !is ScillaLibraryLet)) && 
							(!settings.showForLocalBindings || (element !is ScillaLetExpression)))
							return true
						
						val nameIdentifier = element.nameIdentifier
						if (nameIdentifier != null && element.type == null && element.ownType !is ScillaUnknownType) {
							val presentation = factory.smallText(": ${element.ownType.presentation}")
							val wrapped = factory.roundWithBackground(presentation)
							sink.addInlineElement(nameIdentifier.endOffset, true, wrapped, false)
						}
					}
				}
				return true
			}
		}
	}

	override val key: SettingsKey<Settings>
		get() = KEY

	override fun createSettings() = Settings()

	override val name: String
		get() = "Type annotations"

	override val group: InlayGroup
		get() = InlayGroup.TYPES_GROUP

	override val previewText: String?
		get() = """scilla_version 0
library Sample
let greetings = fun (name: String) => "Hello" 
"""
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable = Configurable(settings)

	class Configurable(private val settings: Settings) : ImmediateConfigurable {
		override val cases: List<ImmediateConfigurable.Case> = listOf(
			ImmediateConfigurable.Case("Library let binging", "LIBRARY_LET_BINDINGS", settings::showForLibraryBindings),
			ImmediateConfigurable.Case("Local let binging", "LOCAL_LET_BINDINGS", settings::showForLocalBindings)
		)

		override fun createComponent(listener: ChangeListener): JPanel = panel {}

		override val mainCheckboxText: String
			get() = "Show type hints for:"
	}

	data class Settings(
		var showForLibraryBindings: Boolean = true,
		var showForLocalBindings: Boolean = true)
}
