package com.zloyrobot.scilla.ide

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.zloyrobot.scilla.lang.ScillaLanguage
import com.zloyrobot.scilla.lang.ScillaTokenType

class ScillaBraceMatcher: PairedBraceMatcherAdapter(object : PairedBraceMatcher{
	val PAIRS = arrayOf(
		BracePair(ScillaTokenType.LPAREN, ScillaTokenType.RPAREN, false),
		BracePair(ScillaTokenType.LBRACE, ScillaTokenType.RBRACE, false),
		BracePair(ScillaTokenType.LBRACKET, ScillaTokenType.RBRACKET, false),
		BracePair(ScillaTokenType.MATCH, ScillaTokenType.END, true),
		BracePair(ScillaTokenType.PROCEDURE, ScillaTokenType.END, true),
		BracePair(ScillaTokenType.TRANSITION, ScillaTokenType.END, true)
	)

	override fun getPairs(): Array<BracePair> {
		return PAIRS
	}

	override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
		return true
	}

	override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
		return openingBraceOffset
	}
}, ScillaLanguage) 