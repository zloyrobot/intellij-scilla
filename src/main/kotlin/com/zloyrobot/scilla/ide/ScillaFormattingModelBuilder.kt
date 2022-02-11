package com.zloyrobot.scilla.ide

import com.intellij.application.options.*
import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.tree.TokenSet
import com.zloyrobot.scilla.lang.ScillaElementType
import com.zloyrobot.scilla.lang.ScillaLanguage
import com.zloyrobot.scilla.lang.ScillaTokenType

const val WRAP_ALWAYS =CommonCodeStyleSettings.WRAP_ALWAYS
const val CHOP_IF_LONG = CommonCodeStyleSettings.WRAP_AS_NEEDED or CommonCodeStyleSettings.WRAP_ON_EVERY_ITEM

@Suppress("PropertyName")
class ScillaCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings("ScillaSettings", container) {
	
	// Wrapping & Align
	@JvmField
	var DECLARATION_PARAMETERS: Int = CHOP_IF_LONG

	@JvmField
	var ALIGN_PARAMETER_NAMES: Boolean = false

	@JvmField
	var ALIGN_PARAMETER_TYPES: Boolean = false

	@JvmField
	var PARAMETERS_LPAR_ON_NEW_LINE: Boolean = false

	@JvmField
	var PARAMETERS_RPAR_ON_NEW_LINE: Boolean = true

	@JvmField
	var MESSAGE_EXPRESSION: Int = CHOP_IF_LONG

	@JvmField
	var ALIGN_MESSAGE_NAMES: Boolean = false

	@JvmField
	var ALIGN_MESSAGE_VALUES: Boolean = false

	@JvmField
	var MESSAGE_LBRACE_ON_NEW_LINE: Boolean = false

	@JvmField
	var MESSAGE_RBRACE_ON_NEW_LINE: Boolean = true

	@JvmField
	var TYPE_DECLARATION: Int = WRAP_ALWAYS
	
	
	// Spacing
	@JvmField
	var SPACE_PARAMETER_BEFORE_COLON: Boolean = false

	@JvmField
	var SPACE_PARAMETER_AFTER_COLON: Boolean = true

	@JvmField
	var SPACE_WITHIN_PARAMETERS: Boolean = false
	
	@JvmField
	var SPACE_WITHIN_TYPE_ARGUMENT_BRACES: Boolean = false

	@JvmField
	var SPACE_WITHIN_MESSAGE_EXPR_BRACES: Boolean = true

	@JvmField
	var SPACE_AROUND_ARROW_IN_MATCH: Boolean = true
	
	@JvmField
	var SPACE_AROUND_ARROW_IN_FUNCTION: Boolean = true
	
	@JvmField
	var SPACE_AROUND_TARROW_IN_TYPE: Boolean = true

	@JvmField
	var SPACE_AROUND_FETCH_OPERATOR: Boolean = true

	@JvmField
	var SPACE_AROUND_ASSIGN_OPERATOR: Boolean = true

	@JvmField
	var SPACE_AROUND_EQ_OPERATOR: Boolean = true
}


class ScillaFormattingModelBuilder : FormattingModelBuilder {
	override fun createModel(element: PsiElement, commonSettings: CodeStyleSettings): FormattingModel {
		val spacingBuilder = createSpacingBuilder(commonSettings)

		val settings = commonSettings.getCustomSettings(ScillaCodeStyleSettings::class.java)
		val containingFile = element.containingFile
		val solidityBlock = ScillaFormattingBlock(element.node, null, spacingBuilder, settings)

		return FormattingModelProvider.createFormattingModelForPsiFile(containingFile, solidityBlock, commonSettings)
	}

	override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode): TextRange? {
		return null
	}

	companion object {
		fun createSpacingBuilder(commonSettings: CodeStyleSettings): SpacingBuilder {
			val barParents = TokenSet.create(
				ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR,
				ScillaElementType.MATCH_EXPRESSION,
				ScillaElementType.MATCH_STATEMENT
			)

			val identsLikeTokens = TokenSet.orSet(
				ScillaTokenType.KEYWORDS,
				ScillaTokenType.IDENTS,
				TokenSet.create(ScillaTokenType.UNDERSCORE))
			
			val settings = commonSettings.getCustomSettings(ScillaCodeStyleSettings::class.java)
			return SpacingBuilder(commonSettings, ScillaLanguage)
				.between(identsLikeTokens, identsLikeTokens).spaces(1)
				
				//Contract and Components Parameters 
				.afterInside(ScillaTokenType.LPAREN, ScillaElementType.CONTRACT_OR_COMPONENT_PARAMETERS)
					.spaceIf(settings.SPACE_WITHIN_PARAMETERS, settings.PARAMETERS_LPAR_ON_NEW_LINE)
				.beforeInside(ScillaTokenType.RPAREN, ScillaElementType.CONTRACT_OR_COMPONENT_PARAMETERS)
					.spaceIf(settings.SPACE_WITHIN_PARAMETERS, settings.PARAMETERS_RPAR_ON_NEW_LINE)

				//Message Expression
				.afterInside(ScillaTokenType.LBRACE, ScillaElementType.MESSAGE_EXPRESSION)
					.spaceIf(settings.SPACE_WITHIN_MESSAGE_EXPR_BRACES, settings.MESSAGE_LBRACE_ON_NEW_LINE)
				.beforeInside(ScillaTokenType.RBRACE, ScillaElementType.MESSAGE_EXPRESSION)
					.spaceIf(settings.SPACE_WITHIN_MESSAGE_EXPR_BRACES, settings.MESSAGE_RBRACE_ON_NEW_LINE)
				
				//Other Parameters
				.afterInside(ScillaTokenType.LPAREN, ScillaElementType.PARAMETERS)
					.spaceIf(settings.SPACE_WITHIN_PARAMETERS)
				.beforeInside(ScillaTokenType.RPAREN, ScillaElementType.PARAMETERS)
					.spaceIf(settings.SPACE_WITHIN_PARAMETERS)
				
				//All Parameters
				.beforeInside(ScillaTokenType.COLON, ScillaElementType.ID_WITH_TYPE)
					.spaceIf(settings.SPACE_PARAMETER_BEFORE_COLON)
				.afterInside(ScillaTokenType.COLON, ScillaElementType.ID_WITH_TYPE)
					.spaceIf(settings.SPACE_PARAMETER_AFTER_COLON)

				//Constructor Application
				.afterInside(ScillaTokenType.LBRACE, ScillaElementType.CONSTR_EXPRESSION)
					.spaceIf(settings.SPACE_WITHIN_TYPE_ARGUMENT_BRACES)
				.beforeInside(ScillaTokenType.RBRACE, ScillaElementType.CONSTR_EXPRESSION)
					.spaceIf(settings.SPACE_WITHIN_TYPE_ARGUMENT_BRACES)
				
				//Around
				.aroundInside(ScillaTokenType.ARROW, ScillaElementType.PATTERN_MATCH_CLAUSE)
					.spaceIf(settings.SPACE_AROUND_ARROW_IN_MATCH)
				.aroundInside(ScillaTokenType.ARROW, ScillaElementType.EXPRESSION_PATTERN_MATCH_CLAUSE)
				.spaceIf(settings.SPACE_AROUND_ARROW_IN_MATCH)
				.aroundInside(ScillaTokenType.ARROW, ScillaElementType.FUN_EXPRESSIONS)
					.spaceIf(settings.SPACE_AROUND_ARROW_IN_FUNCTION)
				.aroundInside(ScillaTokenType.TARROW, ScillaElementType.FUN_TYPE)
					.spaceIf(settings.SPACE_AROUND_TARROW_IN_TYPE)
				.aroundInside(ScillaTokenType.FETCH, ScillaElementType.STATEMENTS)
					.spaceIf(settings.SPACE_AROUND_FETCH_OPERATOR)
				.aroundInside(ScillaTokenType.ASSIGN, ScillaElementType.STATEMENTS)
					.spaceIf(settings.SPACE_AROUND_ASSIGN_OPERATOR)
				.aroundInside(ScillaTokenType.EQ, TokenSet.orSet(
						ScillaElementType.STATEMENTS, 
						ScillaElementType.EXPRESSIONS,
						ScillaElementType.LIBRARY_ENTRIES))
					.spaceIf(settings.SPACE_AROUND_EQ_OPERATOR)
				
				//General
				.after(TokenSet.create(ScillaTokenType.LPAREN, ScillaTokenType.LBRACKET)).spaces(0)//, true)
				.before(TokenSet.create(ScillaTokenType.RPAREN, ScillaTokenType.RBRACKET)).spaces(0)//, true)
				
				.before(ScillaTokenType.LBRACKET).none() //my_map[key]
				
				.before(TokenSet.create(ScillaTokenType.COMMA)).none() //param1: Int32, param2: Int32 
				.after(TokenSet.create(ScillaTokenType.COMMA)).spaces(1)//, true)
				
				.before(ScillaTokenType.SEMICOLON).none() // stmt;
				.afterInside(ScillaTokenType.SEMICOLON, ScillaElementType.MESSAGE_EXPRESSION).spaces(1, true)

				.beforeInside(ScillaTokenType.BAR, barParents).lineBreakInCode()
				.afterInside(ScillaTokenType.BAR, barParents).spaces(1)

				.before(ScillaTokenType.COLON).none()
				.after(ScillaTokenType.COLON).spaces(1)
				.between(ScillaTokenType.KEYWORDS, ScillaTokenType.KEYWORDS).spaces(1)
		}
	}
}

class ScillaFormattingBlock(
	private val node: ASTNode,
	private val parent: ScillaFormattingBlock?,
	private val spacing: SpacingBuilder,
	private val settings: ScillaCodeStyleSettings
) : ASTBlock {

	companion object {
		val PARAMETERS_WRAP = Key<Wrap>("PARAMETER_WRAP")
		val PARAMETER_NAME_ALIGN = Key<Alignment>("PARAMETER_NAME_ALIGN")
		val PARAMETER_TYPE_ALIGN = Key<Alignment>("PARAMETER_TYPE_ALIGN")

		val MESSAGE_EXPRESSION_WRAP = Key<Wrap>("MESSAGE_WRAP")
		val MESSAGE_FIELD_NAME_ALIGN = Key<Alignment>("MESSAGE_FIELD_NAME_ALIGN")
		val MESSAGE_FIELD_VALUE_ALIGN = Key<Alignment>("MESSAGE_FIELD_VALUE_ALIGN")

		val TYPE_DECLARATION_WRAP = Key<Wrap>("TYPE_DECLARATION_WRAP")
	}

	private val userDate = UserDataHolderBase()
	private val nodeSubBlocks = lazy {
		val blocks = mutableListOf<Block>()
		var child = node.firstChildNode
		while (child != null) {
			if (child.elementType != TokenType.WHITE_SPACE) {
				val block = ScillaFormattingBlock(child, this, spacing, settings)
				processChildBlock(child, block)
				blocks.add(block)
			}
			child = child.treeNext
		}
		return@lazy blocks
	}
	
	override fun getNode(): ASTNode = node
	override fun getTextRange(): TextRange = node.textRange
	override fun getSubBlocks(): MutableList<Block> = nodeSubBlocks.value 
	
	private fun processChildBlock(child: ASTNode, block: ScillaFormattingBlock) {
		when (child.elementType) {
			ScillaElementType.CONTRACT_PARAMETERS,
			ScillaElementType.COMPONENT_PARAMETERS -> {
				block.userDate.putUserData(PARAMETERS_WRAP, Wrap.createWrap(settings.DECLARATION_PARAMETERS, true))
				if (settings.ALIGN_PARAMETER_NAMES)
					block.userDate.putUserData(PARAMETER_NAME_ALIGN, Alignment.createAlignment(true))
				if (settings.ALIGN_PARAMETER_TYPES)
					block.userDate.putUserData(PARAMETER_TYPE_ALIGN, Alignment.createAlignment(true))
			}
			ScillaElementType.MESSAGE_EXPRESSION -> {
				block.userDate.putUserData(MESSAGE_EXPRESSION_WRAP, Wrap.createWrap(settings.MESSAGE_EXPRESSION, true))
				if (settings.ALIGN_MESSAGE_NAMES)
					block.userDate.putUserData(MESSAGE_FIELD_NAME_ALIGN, Alignment.createAlignment(true))
				if (settings.ALIGN_MESSAGE_VALUES)
					block.userDate.putUserData(MESSAGE_FIELD_VALUE_ALIGN, Alignment.createAlignment(true))
			}
			ScillaElementType.LIBRARY_TYPE_DEFINITION -> {
				block.userDate.putUserData(TYPE_DECLARATION_WRAP, Wrap.createWrap(settings.TYPE_DECLARATION, true))
			}
		}
	}

	override fun getSpacing(left: Block?, right: Block): Spacing? {
		return spacing.getSpacing(this, left, right)
	}

	override fun getAlignment(): Alignment? {
		return when (node.elementType) {
			in ScillaElementType.TYPES -> {
				val parameterListBlock = parent?.parent
				if (parameterListBlock?.node?.elementType in ScillaElementType.CONTRACT_OR_COMPONENT_PARAMETERS) {
					parameterListBlock?.userDate?.getUserData(PARAMETER_TYPE_ALIGN)
				}
				else null
			}
			ScillaElementType.ID_WITH_TYPE -> {
				val parameterListBlock = parent
				if (parameterListBlock?.node?.elementType in ScillaElementType.CONTRACT_OR_COMPONENT_PARAMETERS) {
					parameterListBlock?.userDate?.getUserData(PARAMETER_NAME_ALIGN)
				}
				else null
			}
			ScillaElementType.MESSAGE_ENTRY -> {
				val messageExpressionBlock = parent
				messageExpressionBlock?.userDate?.getUserData(MESSAGE_FIELD_NAME_ALIGN)
			}
			in ScillaElementType.EXPRESSIONS -> {
				val messageExpressionBlock = parent?.parent
				messageExpressionBlock?.userDate?.getUserData(MESSAGE_FIELD_VALUE_ALIGN)
			}
			else -> null
		}
	}

	override fun getWrap(): Wrap?  {
		return when (node.elementType) {
			ScillaElementType.ID_WITH_TYPE -> {
				val parameterListBlock = parent
				if (parameterListBlock?.node?.elementType in ScillaElementType.CONTRACT_OR_COMPONENT_PARAMETERS) {
					 parameterListBlock?.userDate?.getUserData(PARAMETERS_WRAP)
				}
				else null
			}
			ScillaElementType.MESSAGE_ENTRY -> {
				val messageExpressionBlock = parent
				messageExpressionBlock?.userDate?.getUserData(MESSAGE_EXPRESSION_WRAP)
			}
			ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR -> {
				val typeDeclarationBlock = parent
				typeDeclarationBlock?.userDate?.getUserData(TYPE_DECLARATION_WRAP)
			}
			else -> null
		}
		
	}
	override fun getIndent(): Indent {
		if (node.firstChildNode != null || ScillaTokenType.COMMENTS.contains(node.elementType)) 
			return getChildIndent(node.treeParent)
		else 
			return Indent.getNoneIndent()
	}

	private fun getChildIndent(parent: ASTNode?): Indent {
		if (parent == null)
			return Indent.getNoneIndent()
		
		return when (parent.elementType) {
			ScillaElementType.LIBRARY_LET_DEFINITION,
			ScillaElementType.CONTRACT_CONSTRAINT -> {
				Indent.getNormalIndent()
			}
			ScillaElementType.PATTERN_MATCH_CLAUSE, 
			ScillaElementType.EXPRESSION_PATTERN_MATCH_CLAUSE -> {
				Indent.getNormalIndent()
			}
			
			ScillaElementType.STATEMENT_LIST -> {
				if (parent.treeParent.elementType != ScillaElementType.PATTERN_MATCH_CLAUSE)
					Indent.getNormalIndent()
				else
					Indent.getNoneIndent()
			}
			
			ScillaElementType.CONTRACT_PARAMETERS,
			ScillaElementType.COMPONENT_PARAMETERS,
			ScillaElementType.FUNCTION_PARAMETERS,
			ScillaElementType.CONTRACT_REF_PARAMETERS,
			ScillaElementType.MESSAGE_EXPRESSION -> {
				Indent.getNormalIndent()
			}
			ScillaElementType.FUN_EXPRESSION,
			ScillaElementType.TYPE_FUN_EXPRESSION -> {
				val child = node.findChildByType(ScillaElementType.EXPRESSIONS)?.elementType
				if (child != ScillaElementType.FUN_EXPRESSION && child != ScillaElementType.TYPE_FUN_EXPRESSION) {
					Indent.getNormalIndent()
				} else Indent.getNoneIndent()
			}
			else -> Indent.getNoneIndent()
		}
	}

	override fun isIncomplete(): Boolean = FormatterUtil.isIncomplete(node)
	override fun isLeaf(): Boolean = node.firstChildNode == null

	override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
		return ChildAttributes(getChildIndent(node), null)
	}
}


class ScillaCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun createCustomSettings(settings: CodeStyleSettings) = ScillaCodeStyleSettings(settings)

	override fun getConfigurableDisplayName() = ScillaLanguage.displayName

	override fun createSettingsPage(settings: CodeStyleSettings, originalSettings: CodeStyleSettings) =
		object : CodeStyleAbstractConfigurable(settings, originalSettings, configurableDisplayName) {
			override fun createPanel(settings: CodeStyleSettings) = ScillaCodeStyleMainPanel(currentSettings, settings)
			override fun getHelpTopic() = null
		}

	private class ScillaCodeStyleMainPanel(currentSettings: CodeStyleSettings, settings: CodeStyleSettings) :
		TabbedLanguageCodeStylePanel(ScillaLanguage, currentSettings, settings) {

		override fun initTabs(settings: CodeStyleSettings?) {
			addIndentOptionsTab(settings)
			addSpacesTab(settings)
			addWrappingAndBracesTab(settings)
		}
	}
}



class ScillaLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	companion object {
		const val MESSAGE_EXPRESSION_TITLE = "Message expression"
		const val PARAMETER_OR_FILED_TITLE = "Parameter or field declaration"
		const val CONTRACT_OR_COMPONENT_PARAMETERS = "Contract or component parameters"
		const val TYPE_DECLARATION = "Type declaration"
		const val AROUND_TITLE = "Around"
		const val OTHER_TITLE = "Other"
	}
	override fun getLanguage(): Language = ScillaLanguage

	override fun getCodeSample(settingsType: SettingsType): String {
		return CodeStyleAbstractPanel.readFromFile(ScillaLanguage::class.java, "Sample.scilla")
	}

	override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
		if (settingsType === SettingsType.SPACING_SETTINGS) {
			
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_PARAMETER_BEFORE_COLON",
				"Before colon, after declaration name",
				PARAMETER_OR_FILED_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_PARAMETER_AFTER_COLON",
				"After colon, before declaration type",
				PARAMETER_OR_FILED_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_WITHIN_PARAMETERS",
				"Within parameters parentheses",
				PARAMETER_OR_FILED_TITLE
			)
			
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_AROUND_ARROW_IN_MATCH",
				"Arrow in match clause ('=>')",
				AROUND_TITLE
			)

			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_AROUND_ARROW_IN_FUNCTION",
				"Arrow in function definition ('=>')",
				AROUND_TITLE
			)

			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_AROUND_TARROW_IN_TYPE",
				"Type Arrow ('->')",
				AROUND_TITLE
			)

			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_AROUND_FETCH_OPERATOR",
				"Fetch operator ('<-')",
				AROUND_TITLE
			)
			
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_AROUND_ASSIGN_OPERATOR",
				"Assignment operator (':=')",
				AROUND_TITLE
			)

			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_AROUND_EQ_OPERATOR",
				"Eq operator in definition ('=')",
				AROUND_TITLE
			)
			
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_WITHIN_TYPE_ARGUMENT_BRACES",
				"Within type argument braces",
				OTHER_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"SPACE_WITHIN_MESSAGE_EXPR_BRACES",
				"Within message expression braces",
				OTHER_TITLE
			)
			
		}
		if (settingsType === SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
			consumer.showStandardOptions("RIGHT_MARGIN")
			
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"DECLARATION_PARAMETERS",
				CONTRACT_OR_COMPONENT_PARAMETERS,
				null, CodeStyleSettingsCustomizable.WRAP_OPTIONS, CodeStyleSettingsCustomizable.WRAP_VALUES
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"PARAMETERS_LPAR_ON_NEW_LINE",
				"New line after '('",
				CONTRACT_OR_COMPONENT_PARAMETERS
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"PARAMETERS_RPAR_ON_NEW_LINE",
				"Place ')' on new line",
				CONTRACT_OR_COMPONENT_PARAMETERS
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java, 
				"ALIGN_PARAMETER_NAMES",
				"Align names when multiline",
				CONTRACT_OR_COMPONENT_PARAMETERS
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"ALIGN_PARAMETER_TYPES",
				"Align types when multiline",
				CONTRACT_OR_COMPONENT_PARAMETERS
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"MESSAGE_EXPRESSION",
				MESSAGE_EXPRESSION_TITLE,
				null, CodeStyleSettingsCustomizable.WRAP_OPTIONS, CodeStyleSettingsCustomizable.WRAP_VALUES
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"MESSAGE_LBRACE_ON_NEW_LINE",
				"New line after '{'",
				MESSAGE_EXPRESSION_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"MESSAGE_RBRACE_ON_NEW_LINE",
				"Place '}' on new line",
				MESSAGE_EXPRESSION_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"ALIGN_MESSAGE_NAMES",
				"Align field names when multiline",
				MESSAGE_EXPRESSION_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"ALIGN_MESSAGE_VALUES",
				"Align field values when multiline",
				MESSAGE_EXPRESSION_TITLE
			)
			consumer.showCustomOption(
				ScillaCodeStyleSettings::class.java,
				"TYPE_DECLARATION",
				TYPE_DECLARATION,
				null, CodeStyleSettingsCustomizable.WRAP_OPTIONS, CodeStyleSettingsCustomizable.WRAP_VALUES
			)
			
		}
	}

	override fun getRightMargin(settingsType: SettingsType): Int {
		return if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) 50 else -1
	}

	override fun getDefaultCommonSettings(): CommonCodeStyleSettings {
		val settings = CommonCodeStyleSettings(ScillaLanguage)
		settings.initIndentOptions()
		return settings
	}
	
	override fun getIndentOptionsEditor(): IndentOptionsEditor = SmartIndentOptionsEditor()
}
