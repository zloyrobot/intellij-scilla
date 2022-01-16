package com.zloyrobot.scilla.lang

import com.intellij.lexer.FlexAdapter
import com.zloyrobot.scilla.lexer._ScillaLexer


class ScillaLexer : FlexAdapter(_ScillaLexer(null))