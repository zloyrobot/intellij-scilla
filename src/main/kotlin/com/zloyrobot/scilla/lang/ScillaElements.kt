package com.zloyrobot.scilla.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.tree.TokenSet


class ScillaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ScillaLanguage) {
	override fun getFileType(): FileType = ScillaFileType
}


abstract class ScillaElement(node: ASTNode) : ASTWrapperPsiElement(node)
abstract class ScillaNamedElement(node: ASTNode) : ScillaElement(node), PsiNameIdentifierOwner {
	override fun getNameIdentifier(): PsiElement? = findChildByType(ScillaTokenType.IDENTS)
	override fun getName(): String = nameIdentifier?.text.orEmpty()
	override fun setName(name: String): PsiElement = TODO("Not yet implemented")
}

abstract class ScillaRef(node: ASTNode) : ScillaElement(node)
class ScillaQualifiedRef(node: ASTNode) : ScillaRef(node)
class ScillaHexQualifiedRef(node: ASTNode) : ScillaRef(node)
class ScillaSimpleRef(node: ASTNode) : ScillaRef(node)

class ScillaMapKey(node: ASTNode) : ScillaElement(node)
class ScillaMapValue(node: ASTNode) : ScillaElement(node)
class ScillaMapAccess(node: ASTNode) : ScillaElement(node)

class ScillaIdWithType(node: ASTNode) : ScillaElement(node)

abstract class ScillaPattern(node: ASTNode) : ScillaElement(node)
class ScillaWildcardPattern(node: ASTNode) : ScillaPattern(node)
class ScillaBinderPattern(node: ASTNode) : ScillaPattern(node)
class ScillaConstructorPattern(node: ASTNode) : ScillaPattern(node)
class ScillaParenPattern(node: ASTNode) : ScillaPattern(node)
class ScillaPatternMatchClause(node: ASTNode) : ScillaElement(node)

interface ScillaMatchElement : PsiElement {
	val matchKeyword: PsiElement
	val subject: ScillaRef?
	val withKeyword: PsiElement?
	val endKeyword: PsiElement?
}

abstract class ScillaExpression(node: ASTNode) : ScillaElement(node)
class ScillaLiteralExpression(node: ASTNode) : ScillaExpression(node)
class ScillaVarExpression(node: ASTNode) : ScillaExpression(node)
class ScillaLetExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMessageExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMessageEntry(node: ASTNode) : ScillaElement(node)
class ScillaMessageEntryValue(node: ASTNode) : ScillaElement(node)
class ScillaFunExpression(node: ASTNode) : ScillaExpression(node)
class ScillaAppExpression(node: ASTNode) : ScillaExpression(node)
class ScillaConstrExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMatchExpression(node: ASTNode) : ScillaExpression(node), ScillaMatchElement {
	override val matchKeyword: PsiElement get() = findChildByType(ScillaTokenType.MATCH)!!
	override val subject: ScillaRef? get() = findChildByType(ScillaElementType.REFS)
	override val withKeyword: PsiElement? get() = findChildByType(ScillaTokenType.WITH)
	override val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
}

class ScillaBuiltinExpression(node: ASTNode) : ScillaExpression(node)
class ScillaTFunExpression(node: ASTNode) : ScillaExpression(node)
class ScillaTAppExpression(node: ASTNode) : ScillaExpression(node)
class ScillaFixpointExpression(node: ASTNode) : ScillaExpression(node)
class ScillaGasExpression(node: ASTNode) : ScillaExpression(node)


abstract class ScillaStatement(node: ASTNode) : ScillaElement(node)
class ScillaStatementList(node: ASTNode) : ScillaStatement(node)
class ScillaForallStatement(node: ASTNode) : ScillaStatement(node)
class ScillaAcceptStatement(node: ASTNode) : ScillaStatement(node)
class ScillaEventStatement(node: ASTNode) : ScillaStatement(node)

class ScillaMatchStatement(node: ASTNode) : ScillaStatement(node), ScillaMatchElement {
	override val matchKeyword: PsiElement get() = findChildByType(ScillaTokenType.MATCH)!!
	override val subject: ScillaRef? get() = findChildByType(ScillaElementType.REFS)
	override val withKeyword: PsiElement? get() = findChildByType(ScillaTokenType.WITH)
	override val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
}

class ScillaThrowStatement(node: ASTNode) : ScillaStatement(node)
class ScillaSendStatement(node: ASTNode) : ScillaStatement(node)
class ScillaDeleteStatement(node: ASTNode) : ScillaStatement(node)
class ScillaFetchStatement(node: ASTNode) : ScillaStatement(node)
class ScillaLocalBindingStatement(node: ASTNode) : ScillaStatement(node)
class ScillaAssignStatement(node: ASTNode) : ScillaStatement(node)
class ScillaCallStatement(node: ASTNode) : ScillaStatement(node)


abstract class ScillaType(node: ASTNode) : ScillaElement(node)
class ScillaRefType(node: ASTNode) : ScillaType(node)
class ScillaMapType(node: ASTNode) : ScillaType(node)
class ScillaFunType(node: ASTNode) : ScillaType(node)
class ScillaPolyType(node: ASTNode) : ScillaType(node)
class ScillaAddressType(node: ASTNode) : ScillaType(node)
class ScillaAddressTypeField(node: ASTNode) : ScillaElement(node)
class ScillaTypeVarType(node: ASTNode) : ScillaType(node)
class ScillaParenType(node: ASTNode) : ScillaType(node)


class ScillaContractDefinition(node: ASTNode) : ScillaNamedElement(node)
class ScillaContractConstraint(node: ASTNode) : ScillaNamedElement(node)

abstract class ScillaParameters(node: ASTNode) : ScillaElement(node)
class ScillaContractParameters(node: ASTNode) : ScillaParameters(node)
class ScillaComponentParameters(node: ASTNode) : ScillaParameters(node)
class ScillaContractRefParameters(node: ASTNode) : ScillaParameters(node)
class ScillaFunctionParameters(node: ASTNode) : ScillaParameters(node)

class ScillaFieldDefinition(node: ASTNode) : ScillaElement(node)

abstract class ScillaComponentDefinition(node: ASTNode) : ScillaNamedElement(node) {
	companion object {
		val COMPONENT_KEYWORDS = TokenSet.create(ScillaTokenType.TRANSITION, ScillaTokenType.PROCEDURE) 
	}
	
	val definitionKeyword: PsiElement = findChildByType(COMPONENT_KEYWORDS)!!
	val parameterList: ScillaComponentParameters? get() = findChildByType(ScillaElementType.COMPONENT_PARAMETERS)
	val statementList: ScillaStatementList? get() = findChildByType(ScillaElementType.STATEMENT_LIST)
	val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
}
class ScillaTransitionDefinition(node: ASTNode) : ScillaComponentDefinition(node) 
class ScillaProcedureDefinition(node: ASTNode) : ScillaComponentDefinition(node) 

class ScillaLibraryDefinition(node: ASTNode) : ScillaNamedElement(node)

abstract class ScillaLibraryEntry(node: ASTNode) : ScillaNamedElement(node)


class ScillaLibraryLetDefinition(node: ASTNode) : ScillaLibraryEntry(node) {
	val letKeyword: PsiElement get() = findChildByType(ScillaTokenType.LET)!!
	val eqToken: PsiElement? get() = findChildByType(ScillaTokenType.EQ)
	val expression: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
}

class ScillaLibraryTypeDefinition(node: ASTNode) : ScillaLibraryEntry(node) {
	val typeKeyword: PsiElement get() = findChildByType(ScillaTokenType.TYPE)!!
	val eqToken: PsiElement? get() = findChildByType(ScillaTokenType.EQ)
	val constructors: List<ScillaLibraryTypeConstructor> get() = findChildrenByType(ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR)
}

class ScillaLibraryTypeConstructor(node: ASTNode) : ScillaElement(node)

class ScillaVersion(node: ASTNode) : ScillaElement(node)

class ScillaImports(node: ASTNode) : ScillaElement(node)
class ScillaImportName(node: ASTNode) : ScillaElement(node)

class ScillaGarbageAtTheEndOfFile(node: ASTNode) : ScillaElement(node)