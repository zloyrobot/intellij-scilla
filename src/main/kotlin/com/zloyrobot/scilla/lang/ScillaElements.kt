package com.zloyrobot.scilla.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider


open class ScillaElement(node: ASTNode) : ASTWrapperPsiElement(node)

class ScillaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ScillaLanguage) {
    override fun getFileType(): FileType {
        return ScillaFileType
    }
}

open class ScillaRef(node: ASTNode) : ScillaElement(node)
class ScillaQualifiedRef(node: ASTNode) : ScillaRef(node)
class ScillaHexQualifiedRef(node: ASTNode) : ScillaRef(node)
class ScillaSimpleRef(node: ASTNode) : ScillaRef(node)

class ScillaMapKey(node: ASTNode) : ScillaElement(node)
class ScillaMapValue(node: ASTNode) : ScillaElement(node)
class ScillaMapAccess(node: ASTNode) : ScillaElement(node)

class ScillaIdWithType(node: ASTNode) : ScillaElement(node)

open class ScillaPattern(node: ASTNode) : ScillaElement(node)
open class ScillaWildcardPattern(node: ASTNode) : ScillaPattern(node)
open class ScillaBinderPattern(node: ASTNode) : ScillaPattern(node)
open class ScillaConstructorPattern(node: ASTNode) : ScillaPattern(node)
open class ScillaParenPattern(node: ASTNode) : ScillaPattern(node)
open class ScillaPatternMatchClause(node: ASTNode) : ScillaElement(node)

open class ScillaExpression(node: ASTNode) : ScillaElement(node)
class ScillaLiteralExpression(node: ASTNode) : ScillaExpression(node)
class ScillaVarExpression(node: ASTNode) : ScillaExpression(node)
class ScillaLetExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMessageExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMessageEntry(node: ASTNode) : ScillaElement(node)
class ScillaFunExpression(node: ASTNode) : ScillaExpression(node)
class ScillaAppExpression(node: ASTNode) : ScillaExpression(node)
class ScillaConstrExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMatchExpression(node: ASTNode) : ScillaExpression(node)
class ScillaBuiltinExpression(node: ASTNode) : ScillaExpression(node)
class ScillaTFunExpression(node: ASTNode) : ScillaExpression(node)
class ScillaTAppExpression(node: ASTNode) : ScillaExpression(node)
class ScillaFixpointExpression(node: ASTNode) : ScillaExpression(node)
class ScillaGasExpression(node: ASTNode) : ScillaExpression(node)


open class ScillaStatement(node: ASTNode) : ScillaElement(node)
class ScillaStatementList(node: ASTNode) : ScillaStatement(node)
class ScillaForallStatement(node: ASTNode) : ScillaStatement(node)
class ScillaAcceptStatement(node: ASTNode) : ScillaStatement(node)
class ScillaEventStatement(node: ASTNode) : ScillaStatement(node)
class ScillaMatchStatement(node: ASTNode) : ScillaStatement(node)
class ScillaThrowStatement(node: ASTNode) : ScillaStatement(node)
class ScillaSendStatement(node: ASTNode) : ScillaStatement(node)
class ScillaDeleteStatement(node: ASTNode) : ScillaStatement(node)
class ScillaFetchStatement(node: ASTNode) : ScillaStatement(node)
class ScillaLocalBindingStatement(node: ASTNode) : ScillaStatement(node)
class ScillaAssignStatement(node: ASTNode) : ScillaStatement(node)
class ScillaCallStatement(node: ASTNode) : ScillaStatement(node)


open class ScillaType(node: ASTNode) : ScillaElement(node)
class ScillaRefType(node: ASTNode) : ScillaType(node)
class ScillaMapType(node: ASTNode) : ScillaType(node)
class ScillaFunType(node: ASTNode) : ScillaType(node)
class ScillaPolyType(node: ASTNode) : ScillaType(node)
class ScillaAddressType(node: ASTNode) : ScillaType(node)
class ScillaAddressTypeField(node: ASTNode) : ScillaElement(node)
class ScillaTypeVarType(node: ASTNode) : ScillaType(node)
class ScillaParenType(node: ASTNode) : ScillaType(node)


class ScillaContractDefinition(node: ASTNode) : ScillaElement(node)
class ScillaParameterList(node: ASTNode) : ScillaElement(node)
class ScillaFieldDefinition(node: ASTNode) : ScillaElement(node)
class ScillaComponentDefinition(node: ASTNode) : ScillaElement(node)

class ScillaLibraryDefinition(node: ASTNode) : ScillaElement(node)

open class ScillaLibraryEntry(node: ASTNode) : ScillaElement(node)
class ScillaLibraryLetDefinition(node: ASTNode) : ScillaLibraryEntry(node)
class ScillaLibraryTypeDefinition(node: ASTNode) : ScillaLibraryEntry(node)
class ScillaLibraryTypeConstructor(node: ASTNode) : ScillaElement(node)

class ScillaVersion(node: ASTNode) : ScillaElement(node)

class ScillaImports(node: ASTNode) : ScillaElement(node)
class ScillaImportName(node: ASTNode) : ScillaElement(node)

class ScillaGarbageAtTheEndOfFile(node: ASTNode) : ScillaElement(node)