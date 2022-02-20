package com.zloyrobot.scilla.ide

import com.intellij.codeInsight.hints.*
import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentsOfType
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
						if ((!settings.showForLibraryLetBindings || (element !is ScillaLibraryLet)) && 
							(!settings.showForLocalLetBindings || (element !is ScillaLetExpression)))
							return true

						if (element.type == null) 
							buildAnnotation(element.ownType, element)
					}
					is ScillaBinderPattern -> {
						if (!settings.showForPatterns)
							return true

						buildAnnotation(element.ownType, element)
					}
					is ScillaVarBindingStatement -> {
						if (!settings.showForLocalVariables)
							return true

						buildAnnotation(element.ownType, element)
					}
				}
				return true
			}

			private fun buildAnnotation(type: ScillaType, element: PsiNameIdentifierOwner) {
				val nameIdentifier = element.nameIdentifier
				if (nameIdentifier != null) {
					val presentation = factory.smallText(": ${type.presentation}")
					val wrapped = factory.roundWithBackground(presentation)
					sink.addInlineElement(nameIdentifier.endOffset, true, wrapped, false)
				}
			}
		}
	}

	override val key: SettingsKey<Settings>
		get() = KEY

	override fun createSettings() = Settings()

	override val name: String
		get() = "Type annotations"
	
	override val previewText: String?
		get() = """scilla_version 0
library Sample
let greetings = fun (name: String) => "Hello" 
"""

	override fun createConfigurable(settings: Settings): ImmediateConfigurable = Configurable(settings)

	class Configurable(private val settings: Settings) : ImmediateConfigurable {
		override val cases: List<ImmediateConfigurable.Case> = listOf(
			ImmediateConfigurable.Case("Library let binging", "LIBRARY_LET_BINDING", settings::showForLibraryLetBindings),
			ImmediateConfigurable.Case("Local let binging", "LOCAL_LET_BINDING", settings::showForLocalLetBindings),
			ImmediateConfigurable.Case("Local variable", "LOCAL_VARIABLE", settings::showForLocalLetBindings),
			ImmediateConfigurable.Case("Pattern binging", "PATTERN_BINDING", settings::showForPatterns)

		)

		override fun createComponent(listener: ChangeListener): JPanel = panel {}

		override val mainCheckboxText: String
			get() = "Show type hints for:"
	}

	data class Settings(
		var showForLibraryLetBindings: Boolean = true,
		var showForLocalLetBindings: Boolean = true,
		var showForLocalVariables: Boolean = true,
		var showForPatterns: Boolean = true)
}
