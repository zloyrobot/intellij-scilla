package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement


interface ScillaPattern : PsiElement

class ScillaConstructorPattern(node: ASTNode) : ScillaConstructorRefElement(node), ScillaPattern
class ScillaWildcardPattern(node: ASTNode) : ScillaPsiElement(node), ScillaPattern
class ScillaParenPattern(node: ASTNode) : ScillaPsiElement(node), ScillaPattern

class ScillaBinderPattern(node: ASTNode) : ScillaVarBindingPsiElement(node), ScillaPattern