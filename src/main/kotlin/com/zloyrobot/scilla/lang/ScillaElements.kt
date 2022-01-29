package com.zloyrobot.scilla.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.*
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.TokenSet

interface ScillaNavigatableElement : NavigatablePsiElement {
	val qualifiedName: String
}


class ScillaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ScillaLanguage), ScillaNavigatableElement {
	override fun getFileType(): FileType = ScillaFileType
	override val qualifiedName: String get() = name
}

abstract class ScillaPsiElement(node: ASTNode) : ASTWrapperPsiElement(node)

abstract class ScillaStubElement<S : StubElement<*>> : StubBasedPsiElementBase<S>, StubBasedPsiElement<S> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

	override fun toString(): String {
		return javaClass.simpleName + "(" + elementType.toString() + ")"
	}
}


abstract class ScillaNamedStub<P: PsiElement>(parent: StubElement<*>?, val elementType: IStubElementType<*, *>, val name: String?) 
	: StubBase<P>(parent, elementType) {
		
}



abstract class ScillaNamedElement<S: ScillaNamedStub<P>, P: PsiElement> : ScillaStubElement<S>, PsiNameIdentifierOwner, ScillaNavigatableElement {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub, stub.elementType)
	
	override fun getNameIdentifier(): PsiElement? = findChildByType(ScillaTokenType.IDENTS)
	override fun getName(): String = stub?.name ?: nameIdentifier?.text.orEmpty()
	override fun setName(name: String): PsiElement = TODO("Not yet implemented")
}

abstract class ScillaRef(node: ASTNode) : ScillaPsiElement(node)
class ScillaQualifiedRef(node: ASTNode) : ScillaRef(node)
class ScillaHexQualifiedRef(node: ASTNode) : ScillaRef(node)
class ScillaSimpleRef(node: ASTNode) : ScillaRef(node)

class ScillaMapKey(node: ASTNode) : ScillaPsiElement(node)
class ScillaMapValue(node: ASTNode) : ScillaPsiElement(node)
class ScillaMapAccess(node: ASTNode) : ScillaPsiElement(node)

class ScillaIdWithType(node: ASTNode) : ScillaPsiElement(node)

abstract class ScillaPattern(node: ASTNode) : ScillaPsiElement(node)
class ScillaWildcardPattern(node: ASTNode) : ScillaPattern(node)
class ScillaBinderPattern(node: ASTNode) : ScillaPattern(node)
class ScillaConstructorPattern(node: ASTNode) : ScillaPattern(node)
class ScillaParenPattern(node: ASTNode) : ScillaPattern(node)
class ScillaPatternMatchClause(node: ASTNode) : ScillaPsiElement(node)

interface ScillaMatchElement : PsiElement {
	val matchKeyword: PsiElement
	val subject: ScillaRef?
	val withKeyword: PsiElement?
	val endKeyword: PsiElement?
}

abstract class ScillaExpression(node: ASTNode) : ScillaPsiElement(node)
class ScillaLiteralExpression(node: ASTNode) : ScillaExpression(node)
class ScillaVarExpression(node: ASTNode) : ScillaExpression(node)
class ScillaLetExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMessageExpression(node: ASTNode) : ScillaExpression(node)
class ScillaMessageEntry(node: ASTNode) : ScillaPsiElement(node)
class ScillaMessageEntryValue(node: ASTNode) : ScillaPsiElement(node)
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


abstract class ScillaStatement(node: ASTNode) : ScillaPsiElement(node)
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


abstract class ScillaType(node: ASTNode) : ScillaPsiElement(node)
class ScillaRefType(node: ASTNode) : ScillaType(node)
class ScillaMapType(node: ASTNode) : ScillaType(node)
class ScillaFunType(node: ASTNode) : ScillaType(node)
class ScillaPolyType(node: ASTNode) : ScillaType(node)
class ScillaAddressType(node: ASTNode) : ScillaType(node)
class ScillaAddressTypeField(node: ASTNode) : ScillaPsiElement(node)
class ScillaTypeVarType(node: ASTNode) : ScillaType(node)
class ScillaParenType(node: ASTNode) : ScillaType(node)


class ScillaContractStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaContract>(parent, ScillaElementType.CONTRACT_DEFINITION, name)

class ScillaContract : ScillaNamedElement<ScillaContractStub, ScillaContract> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaContractStub) : super(stub)

	override val qualifiedName: String get() = name
}

class ScillaContractConstraint(node: ASTNode) : ScillaPsiElement(node)

abstract class ScillaParameters(node: ASTNode) : ScillaPsiElement(node)
class ScillaContractParameters(node: ASTNode) : ScillaParameters(node)
class ScillaComponentParameters(node: ASTNode) : ScillaParameters(node)
class ScillaContractRefParameters(node: ASTNode) : ScillaParameters(node)
class ScillaFunctionParameters(node: ASTNode) : ScillaParameters(node)


abstract class ScillaContractEntry<S: ScillaNamedStub<P>, P: PsiElement> : ScillaNamedElement<S, P> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub)

	override val qualifiedName: String get() = "${(parentByStub as ScillaContract).name}.$name"
}

class ScillaFieldStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaField>(parent, ScillaElementType.FIELD_DEFINITION, name)

class ScillaField : ScillaContractEntry<ScillaFieldStub, ScillaField> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaFieldStub) : super(stub)
}

abstract class ScillaComponent<S: ScillaNamedStub<P>, P: PsiElement> : ScillaContractEntry<S, P> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub)
	
	companion object {
		val COMPONENT_KEYWORDS = TokenSet.create(ScillaTokenType.TRANSITION, ScillaTokenType.PROCEDURE) 
	}
	
	val definitionKeyword: PsiElement get() = findChildByType(COMPONENT_KEYWORDS)!!
	val parameterList: ScillaComponentParameters? get() = findChildByType(ScillaElementType.COMPONENT_PARAMETERS)
	val statementList: ScillaStatementList? get() = findChildByType(ScillaElementType.STATEMENT_LIST)
	val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)
}

class ScillaTransitionStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaTransition>(parent, ScillaElementType.TRANSITION_DEFINITION, name)


class ScillaTransition : ScillaComponent<ScillaTransitionStub, ScillaTransition> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaTransitionStub) : super(stub)
}

class ScillaProcedureStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaProcedure>(parent, ScillaElementType.PROCEDURE_DEFINITION, name)

class ScillaProcedure : ScillaComponent<ScillaProcedureStub, ScillaProcedure> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaProcedureStub) : super(stub)
}


class ScillaLibraryStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaLibrary>(parent, ScillaElementType.LIBRARY_DEFINITION, name)

class ScillaLibrary : ScillaNamedElement<ScillaLibraryStub, ScillaLibrary> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryStub) : super(stub)

	override val qualifiedName: String get() = name
}

abstract class ScillaLibraryEntry<S: ScillaNamedStub<P>, P: PsiElement> : ScillaNamedElement<S, P> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub)

	override val qualifiedName: String get() = "${(parentByStub as ScillaLibrary).name}.$name"
}


class ScillaLibraryLetStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaLibraryLet>(parent, ScillaElementType.LIBRARY_LET_DEFINITION, name)

class ScillaLibraryLet : ScillaLibraryEntry<ScillaLibraryLetStub, ScillaLibraryLet> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryLetStub) : super(stub)
	
	val letKeyword: PsiElement get() = findChildByType(ScillaTokenType.LET)!!
	val eqToken: PsiElement? get() = findChildByType(ScillaTokenType.EQ)
	val expression: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
}

class ScillaLibraryTypeStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaLibraryType>(parent, ScillaElementType.LIBRARY_TYPE_DEFINITION, name)

class ScillaLibraryType : ScillaLibraryEntry<ScillaLibraryTypeStub, ScillaLibraryType> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryTypeStub) : super(stub)
	
	val typeKeyword: PsiElement get() = findChildByType(ScillaTokenType.TYPE)!!
	val eqToken: PsiElement? get() = findChildByType(ScillaTokenType.EQ)
	val constructors: List<ScillaLibraryTypeConstructor> get() = findChildrenByType(ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR)
}

class ScillaLibraryTypeConstructor(node: ASTNode) : ScillaPsiElement(node)

class ScillaVersion(node: ASTNode) : ScillaPsiElement(node)

class ScillaImports(node: ASTNode) : ScillaPsiElement(node)
class ScillaImportName(node: ASTNode) : ScillaPsiElement(node)

class ScillaGarbageAtTheEndOfFile(node: ASTNode) : ScillaPsiElement(node)