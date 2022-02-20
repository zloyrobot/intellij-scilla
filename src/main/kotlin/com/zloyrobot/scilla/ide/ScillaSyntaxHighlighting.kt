package com.zloyrobot.scilla.ide

import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType
import com.zloyrobot.scilla.lang.ScillaFileType
import com.zloyrobot.scilla.lang.ScillaLanguage
import com.zloyrobot.scilla.lang.ScillaLexer
import com.zloyrobot.scilla.lang.ScillaTokenType

enum class ScillaTextAttributeKeys(humanName: String, fallback: TextAttributesKey) {
    COMMENT("Comment", DefaultLanguageHighlighterColors.DOC_COMMENT),
    STRING("String//String text", DefaultLanguageHighlighterColors.STRING),
    INT("Number//Decimal", DefaultLanguageHighlighterColors.NUMBER),
    HEX("Number//Hexadecimal", DefaultLanguageHighlighterColors.NUMBER),
    KEYWORD("Keyword", DefaultLanguageHighlighterColors.KEYWORD),

    DOT("Braces and Operators//Dot", DefaultLanguageHighlighterColors.DOT),
    COMMA("Braces and Operators//Comma", DefaultLanguageHighlighterColors.COMMA),
    SEMICOLON("Braces and Operators//Semicolon", DefaultLanguageHighlighterColors.SEMICOLON),

    PARENTHESES("Braces and Operators//Parentheses", DefaultLanguageHighlighterColors.PARENTHESES),
    BRACKETS("Braces and Operators//Brackets", DefaultLanguageHighlighterColors.BRACKETS),
    BRACES("Braces and Operators//Braces", DefaultLanguageHighlighterColors.BRACES),

    COLON("Braces and Operators//Colon", DefaultLanguageHighlighterColors.DOT),
    ARROW("Braces and Operators//Arrow", DefaultLanguageHighlighterColors.DOT),
    TARROW("Braces and Operators//Type Arrow", DefaultLanguageHighlighterColors.DOT),
    EQ("Braces and Operators//Equal Sign", DefaultLanguageHighlighterColors.DOT),
    AMP("Braces and Operators//Ampersand", DefaultLanguageHighlighterColors.DOT),
    FETCH("Braces and Operators//Fetch", DefaultLanguageHighlighterColors.DOT),
    ASSIGN("Braces and Operators//Assign", DefaultLanguageHighlighterColors.DOT),
    AT("Braces and Operators//At", DefaultLanguageHighlighterColors.DOT),

    VALID_STRING_ESCAPE("String//Escape Sequence//Valid", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    INVALID_STRING_ESCAPE("String//Escape Sequence//Invalid", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE),

	BUILTIN_TYPE("Types//Builtin Type", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
	BUILTIN_TYPE_CONSTRUCTOR("Types//Builtin Type Constructor", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE),
	USER_TYPE("Types//Library User Type", DefaultLanguageHighlighterColors.CONSTANT),
	USER_TYPE_CONSTRUCTOR("Types//Library User Type Constructor", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE),
	TYPE_VAR("Types//Type Parameter", DefaultLanguageHighlighterColors.PARAMETER),
	
	LIBRARY("Library", DefaultLanguageHighlighterColors.IDENTIFIER),
	CONTRACT("Contract", DefaultLanguageHighlighterColors.IDENTIFIER),
	CONTRACT_PARAMETER("Contract", DefaultLanguageHighlighterColors.PARAMETER),
	LIBRARY_LET_BINDING("Library Named Value", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE),
	LOCAL_LET_BINDING("Local Named Value", DefaultLanguageHighlighterColors.LOCAL_VARIABLE),
	PROCEDURE("Contract Procedure", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION),
	TRANSITION("Contract Transition", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION),
	COMPONENT_PARAMETER("Contract Component Parameter", DefaultLanguageHighlighterColors.PARAMETER),
	FIELD("Contract Field", DefaultLanguageHighlighterColors.INSTANCE_FIELD),
	MESSAGE_TAG("Message Tag", DefaultLanguageHighlighterColors.METADATA);
	
	val key = TextAttributesKey.createTextAttributesKey("Scilla.$name", fallback)
    val descriptor = AttributesDescriptor(humanName, key)
}

class ScillaColorSettingsPage : ColorSettingsPage {
	override fun getDisplayName() = ScillaLanguage.displayName
	override fun getIcon() = ScillaFileType.icon
	override fun getAttributeDescriptors() = ScillaTextAttributeKeys.values().map { it.descriptor }.toTypedArray()
	override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
	override fun getHighlighter() = ScillaSyntaxHighlighter()
	
	override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
		return ScillaTextAttributeKeys.values().associateBy({ it.name }, { it.key })
	}

	private val DEMO_TEXT = CodeStyleAbstractPanel.readFromFile(ScillaLanguage::class.java, "Sample.scilla")
	override fun getDemoText(): String = DEMO_TEXT
}


class ScillaSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val keys1 = HashMap<IElementType, TextAttributesKey>()

        init
        {
            fillMap(keys1, ScillaTokenType.KEYWORDS, ScillaTextAttributeKeys.KEYWORD.key)

            keys1[StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN] = ScillaTextAttributeKeys.VALID_STRING_ESCAPE.key
            keys1[StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN] = ScillaTextAttributeKeys.INVALID_STRING_ESCAPE.key
            keys1[StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN] = ScillaTextAttributeKeys.INVALID_STRING_ESCAPE.key

            keys1[ScillaTokenType.INT] = ScillaTextAttributeKeys.INT.key
            keys1[ScillaTokenType.HEX] = ScillaTextAttributeKeys.HEX.key
            keys1[ScillaTokenType.STRING] = ScillaTextAttributeKeys.STRING.key

            keys1[ScillaTokenType.COMMENT] = ScillaTextAttributeKeys.COMMENT.key

            keys1[ScillaTokenType.LPAREN] = ScillaTextAttributeKeys.PARENTHESES.key
            keys1[ScillaTokenType.RPAREN] = ScillaTextAttributeKeys.PARENTHESES.key

            keys1[ScillaTokenType.LBRACE] = ScillaTextAttributeKeys.BRACES.key
            keys1[ScillaTokenType.RBRACE] = ScillaTextAttributeKeys.BRACES.key

            keys1[ScillaTokenType.LBRACKET] = ScillaTextAttributeKeys.BRACKETS.key
            keys1[ScillaTokenType.RBRACKET] = ScillaTextAttributeKeys.BRACKETS.key

            keys1[ScillaTokenType.COMMA] = ScillaTextAttributeKeys.COMMA.key
            keys1[ScillaTokenType.DOT] = ScillaTextAttributeKeys.DOT.key
            keys1[ScillaTokenType.SEMICOLON] = ScillaTextAttributeKeys.SEMICOLON.key

            keys1[ScillaTokenType.COLON] = ScillaTextAttributeKeys.COLON.key
            keys1[ScillaTokenType.ARROW] = ScillaTextAttributeKeys.ARROW.key
            keys1[ScillaTokenType.TARROW] = ScillaTextAttributeKeys.TARROW.key
            keys1[ScillaTokenType.EQ] = ScillaTextAttributeKeys.EQ.key
            keys1[ScillaTokenType.AMP] = ScillaTextAttributeKeys.AMP.key
            keys1[ScillaTokenType.FETCH] = ScillaTextAttributeKeys.FETCH.key
            keys1[ScillaTokenType.ASSIGN] = ScillaTextAttributeKeys.ASSIGN.key
            keys1[ScillaTokenType.AT] = ScillaTextAttributeKeys.AT.key
        }
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return pack(keys1[tokenType])
    }

    override fun getHighlightingLexer(): Lexer {
        return ScillaLexer()
    }
}

class ScillaSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        return ScillaSyntaxHighlighter()
    }
}