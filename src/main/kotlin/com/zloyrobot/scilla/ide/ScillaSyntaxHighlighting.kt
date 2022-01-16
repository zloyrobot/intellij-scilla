package com.zloyrobot.scilla.ide

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType
import com.zloyrobot.scilla.lang.ScillaLexer
import com.zloyrobot.scilla.lang.ScillaTokenType

object ScillaTextAttributeKeys {
    val COMMENT = key("Scilla.COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    val STRING = key("Scilla.STRING", DefaultLanguageHighlighterColors.STRING)
    val INT = key("Scilla.INT", DefaultLanguageHighlighterColors.NUMBER)
    val HEX = key("Scilla.INT", DefaultLanguageHighlighterColors.NUMBER)
    val KEYWORD = key("Scilla.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

    val DOT = key("Scilla.DOT", DefaultLanguageHighlighterColors.DOT)
    val COMMA = key("Scilla.COMMA", DefaultLanguageHighlighterColors.COMMA)
    val SEMICOLON = key("Scilla.SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)

    val PARENTHESES = key("Scilla.PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val BRACKETS = key("Scilla.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val BRACES = key("Scilla.BRACES", DefaultLanguageHighlighterColors.BRACES)

    val COLON = key("Scilla.COLON", DefaultLanguageHighlighterColors.DOT)
    val ARROW = key("Scilla.ARROW", DefaultLanguageHighlighterColors.DOT)
    val TARROW = key("Scilla.TARROW", DefaultLanguageHighlighterColors.DOT)
    val EQ = key("Scilla.EQ", DefaultLanguageHighlighterColors.DOT)
    val AMP = key("Scilla.AND", DefaultLanguageHighlighterColors.DOT)
    val FETCH = key("Scilla.FETCH", DefaultLanguageHighlighterColors.DOT)
    val ASSIGN = key("Scilla.ASSIGN", DefaultLanguageHighlighterColors.DOT)
    val AT = key("Scilla.AT", DefaultLanguageHighlighterColors.DOT)

    val VALID_STRING_ESCAPE = key("Scilla.VALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    val INVALID_STRING_ESCAPE = key("Scilla.INVALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)

    private fun key(name: String, fallback: TextAttributesKey) = TextAttributesKey.createTextAttributesKey(name, fallback)
}

class ScillaSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val keys1 = HashMap<IElementType, TextAttributesKey>()

        init
        {
            fillMap(keys1, ScillaTokenType.KEYWORDS, ScillaTextAttributeKeys.KEYWORD)

            keys1[StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN] = ScillaTextAttributeKeys.VALID_STRING_ESCAPE
            keys1[StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN] = ScillaTextAttributeKeys.INVALID_STRING_ESCAPE
            keys1[StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN] = ScillaTextAttributeKeys.INVALID_STRING_ESCAPE

            keys1[ScillaTokenType.INT] = ScillaTextAttributeKeys.INT
            keys1[ScillaTokenType.HEX] = ScillaTextAttributeKeys.HEX
            keys1[ScillaTokenType.STRING] = ScillaTextAttributeKeys.STRING

            keys1[ScillaTokenType.COMMENT] = ScillaTextAttributeKeys.COMMENT

            keys1[ScillaTokenType.LPAREN] = ScillaTextAttributeKeys.PARENTHESES
            keys1[ScillaTokenType.RPAREN] = ScillaTextAttributeKeys.PARENTHESES

            keys1[ScillaTokenType.LBRACE] = ScillaTextAttributeKeys.BRACES
            keys1[ScillaTokenType.RBRACE] = ScillaTextAttributeKeys.BRACES

            keys1[ScillaTokenType.LBRACKET] = ScillaTextAttributeKeys.BRACKETS
            keys1[ScillaTokenType.RBRACKET] = ScillaTextAttributeKeys.BRACKETS

            keys1[ScillaTokenType.COMMA] = ScillaTextAttributeKeys.COMMA
            keys1[ScillaTokenType.DOT] = ScillaTextAttributeKeys.DOT
            keys1[ScillaTokenType.SEMICOLON] = ScillaTextAttributeKeys.SEMICOLON

            keys1[ScillaTokenType.COLON] = ScillaTextAttributeKeys.COLON
            keys1[ScillaTokenType.ARROW] = ScillaTextAttributeKeys.ARROW
            keys1[ScillaTokenType.TARROW] = ScillaTextAttributeKeys.TARROW
            keys1[ScillaTokenType.EQ] = ScillaTextAttributeKeys.EQ
            keys1[ScillaTokenType.AMP] = ScillaTextAttributeKeys.AMP
            keys1[ScillaTokenType.FETCH] = ScillaTextAttributeKeys.FETCH
            keys1[ScillaTokenType.ASSIGN] = ScillaTextAttributeKeys.ASSIGN
            keys1[ScillaTokenType.AT] = ScillaTextAttributeKeys.AT
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