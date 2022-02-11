package com.zloyrobot.scilla.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.tree.TokenSet

interface ScillaNavigatableElement : NavigatablePsiElement {
}


class ScillaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ScillaLanguage), ScillaNavigatableElement {
	override fun getFileType(): FileType = ScillaFileType
	
	val version: ScillaVersion? get() = findChildByClass(ScillaVersion::class.java)
	val imports: ScillaImports? get() = findChildByClass(ScillaImports::class.java)
	val library: ScillaLibrary? get() = findChildByClass(ScillaLibrary::class.java)
	val contract: ScillaContract? get() = findChildByClass(ScillaContract::class.java)


	private val factory = lazy { ScillaElementFactory(project) }
	
	val list_foldl = lazy {
		val type = factory.value.createType("forall 'A. forall 'B. ('B -> 'A -> 'B) -> 'B -> (List 'A) -> 'B")
		 ScillaBuiltinValueElement("list_foldl", type, this)
	}
	val list_foldr = lazy {
		val type = factory.value.createType("forall 'A. forall 'B. ('A -> 'B -> 'B) -> 'B -> (List 'A) -> 'B")
		ScillaBuiltinValueElement("list_foldr", type, this)
	}
	val list_foldk = lazy {
		val type = factory.value.createType("forall 'A. forall 'B. ('B -> 'A -> ('B -> 'B) -> 'B) -> 'B -> (List 'A) -> 'B")
		ScillaBuiltinValueElement("list_foldk", type, this)
	}
	val nat_fold = lazy {
		val type = factory.value.createType("forall 'T. ('T -> Nat -> 'T) -> 'T -> Nat -> 'T")
		ScillaBuiltinValueElement("nat_fold", type, this)
	}
	val nat_foldk = lazy {
		val type = factory.value.createType("forall 'T. (T -> Nat -> ('T -> 'T) -> 'T) -> 'T -> Nat -> 'T")
		ScillaBuiltinValueElement("nat_foldk", type, this)
	}
}

abstract class ScillaPsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {
	override fun getContainingFile(): ScillaFile? = super.getContainingFile() as? ScillaFile
}

abstract class ScillaStubElement<S : StubElement<*>> : StubBasedPsiElementBase<S>, StubBasedPsiElement<S> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

	override fun toString(): String {
		return javaClass.simpleName + "(" + elementType.toString() + ")"
	}
}


interface ScillaNamedElement : PsiNameIdentifierOwner

abstract class ScillaNamedPsiElement(node: ASTNode) : ScillaPsiElement(node), ScillaNamedElement {
	override fun getNameIdentifier(): PsiElement? = findChildByType(ScillaTokenType.IDENTS)
	override fun getName(): String = nameIdentifier?.text.orEmpty()
	override fun setName(name: String): PsiElement = TODO("Not yet implemented")

	override fun getNavigationElement(): PsiElement = nameIdentifier ?: this
	override fun getTextOffset() = nameIdentifier?.textOffset ?: super.getTextOffset()
}

abstract class ScillaNamedStub<P: PsiElement>(parent: StubElement<*>?, val elementType: IStubElementType<*, *>, val name: String?) 
	: StubBase<P>(parent, elementType) {
}

abstract class ScillaNamedStubElement<S: ScillaNamedStub<P>, P: PsiElement> : ScillaStubElement<S>, ScillaNamedElement, ScillaNavigatableElement {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub, stub.elementType)

	override fun getNavigationElement(): PsiElement = nameIdentifier ?: this
	override fun getTextOffset(): Int = nameIdentifier?.textOffset ?: super.getTextOffset()

	override fun getNameIdentifier(): PsiElement? = findChildByType(ScillaTokenType.IDENTS)
	override fun getName(): String = stub?.name ?: nameIdentifier?.text.orEmpty()
	override fun setName(name: String): PsiElement = TODO("Not yet implemented")
}

abstract class ScillaName(node: ASTNode) : ScillaNamedPsiElement(node) {
	abstract val qualifiedName: String
}

class ScillaQualifiedName(node: ASTNode) : ScillaName(node) {
	val qualifierElement: PsiElement = findChildByType(ScillaTokenType.IDENTS)!!
	val qualifier: String = qualifierElement.text.orEmpty()

	override fun getNameIdentifier(): PsiElement? = findChildrenByType<PsiElement>(ScillaTokenType.IDENTS).dropWhile { it == qualifierElement }.firstOrNull()
	
	override val qualifiedName: String get() = "${qualifier}.${name}"
	
	override fun getReference(): PsiReference {
		val range = qualifierElement.textRangeInParent
		return object : PsiPolyVariantReferenceBase<ScillaQualifiedName>(this, range), PsiReference {

			override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
				val imports = element.containingFile?.imports?.imports ?: return arrayOf()
				return PsiElementResolveResult.createResults(imports.filter { it.namespaceName == qualifierElement.text })
			}
		}
	}
}

class ScillaHexQualifiedName(node: ASTNode) : ScillaName(node) {
	override val qualifiedName: String get() {
		val qualifier = findChildByType<PsiElement>(ScillaTokenType.HEX)?.text ?: ""
		val name = findChildByType<PsiElement>(ScillaTokenType.IDENTS)?.text ?: ""
		return "${qualifier}.${name}"
	}
}
class ScillaSimpleName(node: ASTNode) : ScillaName(node) {
	override val qualifiedName: String get() = name
}


class ScillaMapKey(node: ASTNode) : ScillaPsiElement(node)
class ScillaMapValue(node: ASTNode) : ScillaPsiElement(node)
class ScillaMapAccess(node: ASTNode) : ScillaPsiElement(node)

class ScillaIdWithType(node: ASTNode) : ScillaNamedPsiElement(node), ScillaTypeOwner {
	val typeAnnotation: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)
	
	override fun calculateOwnType(): ScillaType = typeAnnotation?.ownType ?: ScillaUnknownType
	
}

interface ScillaPatternMatchClause : PsiElement {
	val pattern: ScillaPattern?
}

class ScillaStatementPatternMatchClause(node: ASTNode) : ScillaPsiElement(node), ScillaPatternMatchClause {
	override val pattern: ScillaPattern? get() = findChildByType(ScillaElementType.PATTERNS)
}

class ScillaExpressionPatternMatchClause(node: ASTNode) : ScillaPsiElement(node), ScillaPatternMatchClause {
	override val pattern: ScillaPattern? get() = findChildByType(ScillaElementType.PATTERNS)
	val body: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
}

interface ScillaMatchElement : PsiElement {
	val matchKeyword: PsiElement
	val subject: ScillaExpression?
	val withKeyword: PsiElement?
	val endKeyword: PsiElement?
}

abstract class ScillaParameters(node: ASTNode) : ScillaPsiElement(node) {
	val parameters : List<ScillaIdWithType> get() = findChildrenByType(ScillaElementType.ID_WITH_TYPE)
}

class ScillaContractParameters(node: ASTNode) : ScillaParameters(node)
class ScillaComponentParameters(node: ASTNode) : ScillaParameters(node)
class ScillaContractRefParameters(node: ASTNode) : ScillaParameters(node)
class ScillaFunctionParameters(node: ASTNode) : ScillaParameters(node)

interface ScillaParametersOwner : PsiElement {
	val parameterList: ScillaParameters?
}

class ScillaContractStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaContract>(parent, ScillaElementType.CONTRACT_DEFINITION, name)

class ScillaContract : ScillaNamedStubElement<ScillaContractStub, ScillaContract>, ScillaParametersOwner {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaContractStub) : super(stub)

	override val parameterList: ScillaParameters? get() = findChildByType(ScillaElementType.PARAMETERS)
	
	val fields: List<ScillaUserField> get() = findChildrenByType(ScillaElementType.FIELD_DEFINITION)
	val procedures: List<ScillaProcedure> get() = findChildrenByType(ScillaElementType.PROCEDURE_DEFINITION)


	val _this_address = lazy {
		ScillaBuiltinValueElement("_this_address", ScillaByStrType.ByStr20, this) 
	}
	val _creation_block = lazy {
		ScillaBuiltinValueElement("_creation_block", ScillaPrimitiveType.BNum, this)
	}
	val _scilla_version = lazy {
		ScillaBuiltinValueElement("_scilla_version", ScillaPrimitiveType.Uint32, this)
	}
}

class ScillaContractConstraint(node: ASTNode) : ScillaPsiElement(node)

abstract class ScillaContractEntry<S: ScillaNamedStub<P>, P: PsiElement> : ScillaNamedStubElement<S, P> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub)
	
	val qualifiedName: String get() = "${(parentByStub as ScillaContract).name}.$name"
}

abstract class ScillaComponent<S: ScillaNamedStub<P>, P: PsiElement> : ScillaContractEntry<S, P>, ScillaParametersOwner {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub)
	
	companion object {
		val COMPONENT_KEYWORDS = TokenSet.create(ScillaTokenType.TRANSITION, ScillaTokenType.PROCEDURE) 
	}
	
	val definitionKeyword: PsiElement get() = findChildByType(COMPONENT_KEYWORDS)!!
	override val parameterList: ScillaParameters? get() = findChildByType(ScillaElementType.PARAMETERS)
	val statementList: ScillaStatementList? get() = findChildByType(ScillaElementType.STATEMENT_LIST)
	val endKeyword: PsiElement? get() = findChildByType(ScillaTokenType.END)


	val _sender = lazy {
		ScillaBuiltinValueElement("_sender", ScillaAddressType(), this)
	}
	val _origin = lazy {
		ScillaBuiltinValueElement("_origin", ScillaAddressType(), this)
	}
	val _amount = lazy {
		ScillaBuiltinValueElement("_amount", ScillaPrimitiveType.Uint128, this)
	}
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

class ScillaLibrary : ScillaNamedStubElement<ScillaLibraryStub, ScillaLibrary> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryStub) : super(stub)
	
	val types : List<ScillaLibraryType> get() = findChildrenByType(ScillaElementType.LIBRARY_TYPE_DEFINITION)
	val vars : List<ScillaLibraryLet> get() = findChildrenByType(ScillaElementType.LIBRARY_LET_DEFINITION)
}

abstract class ScillaLibraryEntry<S: ScillaNamedStub<P>, P: PsiElement> : ScillaNamedStubElement<S, P> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: S) : super(stub)

	val qualifiedName: String get() = "${(parentByStub as ScillaLibrary).name}.$name"
}


class ScillaLibraryLetStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaLibraryLet>(parent, ScillaElementType.LIBRARY_LET_DEFINITION, name)

class ScillaLibraryLet : ScillaLibraryEntry<ScillaLibraryLetStub, ScillaLibraryLet>, ScillaLetElement {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryLetStub) : super(stub)
	
	val letKeyword: PsiElement get() = findChildByType(ScillaTokenType.LET)!!
	val eqToken: PsiElement? get() = findChildByType(ScillaTokenType.EQ)

	override val type: ScillaTypeElement? get() = findChildByType(ScillaElementType.TYPES)
	override val initializer: ScillaExpression? get() = findChildByType(ScillaElementType.EXPRESSIONS)
	
	override fun calculateOwnType(): ScillaType {
		if (type != null)
			return type!!.ownType
		
		return initializer?.expressionType ?: ScillaUnknownType
	}
}

class ScillaLibraryTypeStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaLibraryType>(parent, ScillaElementType.LIBRARY_TYPE_DEFINITION, name)

class ScillaLibraryType : ScillaLibraryEntry<ScillaLibraryTypeStub, ScillaLibraryType>, ScillaNamedTypeElement {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryTypeStub) : super(stub)

	val typeKeyword: PsiElement get() = findChildByType(ScillaTokenType.TYPE)!!
	val eqToken: PsiElement? get() = findChildByType(ScillaTokenType.EQ)
	val constructors: List<ScillaLibraryTypeConstructor> get() = findChildrenByType(ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR)
	
	override fun calculateOwnType(): ScillaType {
		val constructors = constructors.map {
			val constructorTypes = it.params.map { it.ownType }.toTypedArray()
			ScillaTypeConstructor(it.name, *constructorTypes)
		}.toTypedArray()
		
		return ScillaSimpleAlgebraicType(name, *constructors)
	} 
}

class ScillaLibraryTypeConstructorStub(parent: StubElement<*>?, name: String?) :
	ScillaNamedStub<ScillaLibraryTypeConstructor>(parent, ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR, name)

interface ScillaTypeConstructorElement : ScillaNamedElement, ScillaTypeOwner

class ScillaLibraryTypeConstructor : ScillaNamedStubElement<ScillaLibraryTypeConstructorStub, ScillaLibraryTypeConstructor>,
	ScillaTypeConstructorElement {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaLibraryTypeConstructorStub) : super(stub)
	
	val params: List<ScillaTypeElement> get() = findChildrenByType(ScillaElementType.TYPES)
	
	override fun calculateOwnType(): ScillaType {
		return (parent as? ScillaLibraryType)?.ownType ?: ScillaUnknownType
	}
}

class ScillaVersion(node: ASTNode) : ScillaPsiElement(node)


class ScillaImportsStub(parent: StubElement<*>?)
	: StubBase<ScillaImport>(parent, ScillaElementType.IMPORTS) {
}

class ScillaImports : ScillaStubElement<ScillaImportsStub> {
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaImportsStub) : super(stub, ScillaElementType.IMPORTS)
	
	val imports: Array<ScillaImport> get() = getStubOrPsiChildren(ScillaElementType.IMPORT_ENTRY, ScillaImport.EMPTY_ARRAY) 
}

class ScillaImportStub(parent: StubElement<*>?, val name: String?)
	: StubBase<ScillaImport>(parent, ScillaElementType.IMPORT_ENTRY) {
}

class ScillaImport : ScillaStubElement<ScillaImportStub>, ScillaNamedElement {
	companion object{
		var EMPTY_ARRAY: Array<ScillaImport> = arrayOf()
	}
	
	constructor(node: ASTNode) : super(node)
	constructor(stub: ScillaImportStub) : super(stub, ScillaElementType.IMPORT_ENTRY)

	val namespaceIdentifier: PsiElement? get() {
		return findChildrenByType<PsiElement>(ScillaTokenType.IDENTS).dropWhile { it == nameIdentifier }.firstOrNull()	
	}
	
	val namespaceName: String? get() = namespaceIdentifier?.text 
	
	override fun getNameIdentifier(): PsiElement? = findChildByType(ScillaTokenType.IDENTS)
	override fun getName(): String = stub?.name ?: nameIdentifier?.text.orEmpty()
	override fun setName(name: String): PsiElement = TODO("Not yet implemented")
	
	override fun getReference(): PsiReferenceBase<ScillaImport> {
		val rangeInElement = nameIdentifier?.textRangeInParent
		return object: PsiReferenceBase<ScillaImport>(this, rangeInElement) {
			override fun resolve(): PsiElement? {
				val scope = GlobalSearchScope.projectScope(project)
				val key = ScillaLibraryIndex.KEY
				return StubIndex.getElements(key, name, project, scope, ScillaLibrary::class.java).firstOrNull()
			}
		}
	}
}

class ScillaGarbageAtTheEndOfFile(node: ASTNode) : ScillaPsiElement(node)

