package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.annotations.NonNls


class ScillaElementType(@NonNls debugName: String) : IElementType(debugName, ScillaLanguage) {
    companion object {
        val SCILLA_CONTRACT_FILE = IFileElementType(ScillaLanguage)
        val SCILLA_CONTRACT_STUB_FILE = ScillaStubFileElementType()

        val SIMPLE_REF = ScillaElementType("SIMPLE_REF")
        val QUALIFIED_REF = ScillaElementType("QUALIFIED_REF")
        val HEX_QUALIFIED_REF = ScillaElementType("HEX_QUALIFIED_REF")
		
		val REFS = TokenSet.create(SIMPLE_REF, QUALIFIED_REF, HEX_QUALIFIED_REF)

        val MAP_KEY = ScillaElementType("MAP_KEY")
        val MAP_VALUE = ScillaElementType("MAP_VALUE")
        val MAP_ACCESS = ScillaElementType("MAP_ACCESS")


        val REF_TYPE = ScillaElementType("REF_TYPE")
        val MAP_TYPE = ScillaElementType("MAP_TYPE")
        val FUN_TYPE = ScillaElementType("FUN_TYPE")
        val POLY_TYPE = ScillaElementType("POLY_TYPE")
        val ADDRESS_TYPE = ScillaElementType("ADDRESS_TYPE")
        val ADDRESS_TYPE_FIELD = ScillaElementType("ADDRESS_TYPE_FIELD")
        val TYPE_VAR_TYPE = ScillaElementType("TYPE_VAR_TYPE")
        val PAREN_TYPE = ScillaElementType("PAREN_TYPE")
		
		val TYPES = TokenSet.create(REF_TYPE, MAP_TYPE, FUN_TYPE, POLY_TYPE, ADDRESS_TYPE, TYPE_VAR_TYPE, PAREN_TYPE)

		val CONTRACT_PARAMETERS = ScillaElementType("CONTRACT_PARAMETER_LIST")
		val COMPONENT_PARAMETERS = ScillaElementType("COMPONENT_PARAMETERS")
		val CONTRACT_REF_PARAMETERS = ScillaElementType("CONTRACT_REF_PARAMETER_LIST")
		val FUNCTION_PARAMETERS = ScillaElementType("FUNCTION_PARAMETER_LIST")

		val CONTRACT_OR_COMPONENT_PARAMETERS = TokenSet.create(CONTRACT_PARAMETERS, COMPONENT_PARAMETERS)
		val PARAMETERS = TokenSet.create(CONTRACT_PARAMETERS, COMPONENT_PARAMETERS, CONTRACT_REF_PARAMETERS,
			FUNCTION_PARAMETERS)
		
        val WILDCARD_PATTERN = ScillaElementType("LITERAL_EXPRESSION")
        val BINDER_PATTERN = ScillaElementType("LITERAL_EXPRESSION")
        val CONSTRUCTOR_PATTERN = ScillaElementType("LITERAL_EXPRESSION")
        val PAREN_PATTERN = ScillaElementType("PAREN_EXPRESSION")
        val PATTERN_MATCH_CLAUSE = ScillaElementType("PATTERN_MATCH_CLAUSE")

		val PATTERNS = TokenSet.create(WILDCARD_PATTERN, BINDER_PATTERN, CONSTRUCTOR_PATTERN, PAREN_PATTERN)

        val LITERAL_EXPRESSION = ScillaElementType("LITERAL_EXPRESSION")
        val REF_EXPRESSION = ScillaElementType("VAR_EXPRESSION")
        val LET_EXPRESSION = ScillaElementType("LET_EXPRESSION")
        val MESSAGE_EXPRESSION = ScillaElementType("MESSAGE_EXPRESSION")
        val MESSAGE_ENTRY = ScillaElementType("MESSAGE_ENTRY")
        val MESSAGE_ENTRY_VALUE = ScillaElementType("MESSAGE_ENTRY_VALUE")
        val FUN_EXPRESSION = ScillaElementType("FUN_EXPRESSION")
        val APP_EXPRESSION = ScillaElementType("APP_EXPRESSION")
        val CONSTR_EXPRESSION = ScillaElementType("CONSTR_EXPRESSION")
        val MATCH_EXPRESSION = ScillaElementType("MATCH_EXPRESSION")
        val BUILTIN_EXPRESSION = ScillaElementType("BUILTIN_EXPRESSION")
        val TYPE_FUN_EXPRESSION = ScillaElementType("TYPE_FUN_EXPRESSION")
        val TYPE_APP_EXPRESSION = ScillaElementType("TYPE_APP_EXPRESSION")
		
		val FUN_EXPRESSIONS = TokenSet.create(FUN_EXPRESSION, TYPE_FUN_EXPRESSION) 
		val EXPRESSIONS = TokenSet.create(LITERAL_EXPRESSION, REF_EXPRESSION, LET_EXPRESSION, MESSAGE_EXPRESSION,
		 MESSAGE_ENTRY, FUN_EXPRESSION, APP_EXPRESSION, CONSTR_EXPRESSION, MATCH_EXPRESSION, BUILTIN_EXPRESSION,
		 TYPE_FUN_EXPRESSION, TYPE_APP_EXPRESSION)


        val STATEMENT_LIST = ScillaElementType("STATEMENT_LIST")
		
        val LOAD_STATEMENT = ScillaElementType("LOAD_STATEMENT")
        val REMOTE_LOAD_STATEMENT = ScillaElementType("REMOTE_LOAD_STATEMENT")
        val STORE_STATEMENT = ScillaElementType("STORE_STATEMENT")
        val BIND_STATEMENT = ScillaElementType("BIND_STATEMENT")
        val MAP_UPDATE_STATEMENT = ScillaElementType("MAP_UPDATE_STATEMENT")
		val MAP_DELETE_STATEMENT = ScillaElementType("DELETE_STATEMENT")
        val MAP_GET_STATEMENT = ScillaElementType("MAP_GET_STATEMENT")
        val REMOTE_MAP_GET_STATEMENT = ScillaElementType("REMOTE_MAP_GET_STATEMENT")
		val MATCH_STATEMENT = ScillaElementType("MATCH_STATEMENT")
		val READ_FROM_BC_STATEMENT = ScillaElementType("READ_FROM_BC_STATEMENT")
		val TYPE_CAST_STATEMENT = ScillaElementType("TYPE_CAST_STATEMENT")
		val ACCEPT_STATEMENT = ScillaElementType("ACCEPT_STATEMENT")
		val ITERATE_STATEMENT = ScillaElementType("ITERATE_STATEMENT")
		val SEND_STATEMENT = ScillaElementType("SEND_STATEMENT")
		val EVENT_STATEMENT = ScillaElementType("EVENT_STATEMENT")
		val CALL_STATEMENT = ScillaElementType("CALL_STATEMENT")
		val THROW_STATEMENT = ScillaElementType("THROW_STATEMENT")

		val STATEMENTS = TokenSet.create(LOAD_STATEMENT, REMOTE_LOAD_STATEMENT, STORE_STATEMENT, BIND_STATEMENT,  
			MAP_UPDATE_STATEMENT, MAP_DELETE_STATEMENT, MAP_GET_STATEMENT, REMOTE_MAP_GET_STATEMENT, MATCH_STATEMENT,
			READ_FROM_BC_STATEMENT, TYPE_CAST_STATEMENT, ACCEPT_STATEMENT,ITERATE_STATEMENT, SEND_STATEMENT,
			EVENT_STATEMENT, CALL_STATEMENT, THROW_STATEMENT)

        val CONTRACT_DEFINITION = ScillaContractStubElementType("CONTRACT_DEFINITION")
        val CONTRACT_CONSTRAINT = ScillaElementType("CONTRACT_CONSTRAINT")
        val ID_WITH_TYPE = ScillaElementType("ID_WITH_TYPE")
        val FIELD_DEFINITION = ScillaFieldStubElementType("FIELD_DEFINITION")
        val FIELD_REF = ScillaElementType("FIELD_REF")
        val TRANSITION_DEFINITION = ScillaTransitionStubElementType("TRANSITION_DEFINITION")
        val PROCEDURE_DEFINITION = ScillaProcedureStubElementType("PROCEDURE_DEFINITION")

        val LIBRARY_DEFINITION = ScillaLibraryStubElementType("LIBRARY")
        val LIBRARY_LET_DEFINITION = ScillaLibraryLetStubElementType("LIBRARY_LET_DEFINITION")
        val LIBRARY_TYPE_DEFINITION = ScillaLibraryTypeStubElementType("LIBRARY_TYPE_DEFINITION")
        val LIBRARY_TYPE_CONSTRUCTOR = ScillaLibraryTypeConstructorStubElementType("LIBRARY_TYPE_CONSTRUCTOR")
		
		val LIBRARY_ENTRIES = TokenSet.create(LIBRARY_LET_DEFINITION, LIBRARY_TYPE_DEFINITION)

        val SCILLA_VERSION = ScillaElementType("SCILLA_VERSION")
        val IMPORTS = ScillaImportsStubElementType("IMPORTS")
        val IMPORT_ENTRY = ScillaImportStubElementType("IMPORT")

        val GARBAGE_AT_THE_END_OF_FILE = ScillaElementType("GARBAGE_AT_THE_END_OF_FILE")

        fun createElement(node: ASTNode): PsiElement {
            return when (node.elementType) {
                SIMPLE_REF -> ScillaSimpleName(node)
                QUALIFIED_REF -> ScillaQualifiedName(node)
                HEX_QUALIFIED_REF -> ScillaHexQualifiedName(node)

                MAP_KEY -> ScillaMapKey(node)
                MAP_VALUE -> ScillaMapValue(node)
                MAP_ACCESS -> ScillaMapAccess(node)

                REF_TYPE -> ScillaRefTypeElement(node)
                MAP_TYPE -> ScillaMapTypeElement(node)
                FUN_TYPE -> ScillaFunTypeElement(node)
                POLY_TYPE -> ScillaPolyTypeElement(node)
                ADDRESS_TYPE -> ScillaAddressTypeElement(node)
                ADDRESS_TYPE_FIELD -> ScillaAddressTypeField(node)
                TYPE_VAR_TYPE -> ScillaTypeVarTypeElement(node)
                PAREN_TYPE -> ScillaParenTypeElement(node)

                ID_WITH_TYPE -> ScillaIdWithType(node)
				CONTRACT_PARAMETERS -> ScillaContractParameters(node)
				COMPONENT_PARAMETERS -> ScillaComponentParameters(node)
				CONTRACT_REF_PARAMETERS -> ScillaContractRefParameters(node)
				FUNCTION_PARAMETERS -> ScillaFunctionParameters(node)

                WILDCARD_PATTERN -> ScillaWildcardPattern(node)
                BINDER_PATTERN -> ScillaBinderPattern(node)
                CONSTRUCTOR_PATTERN -> ScillaConstructorPattern(node)
                PAREN_PATTERN -> ScillaParenPattern(node)
                PATTERN_MATCH_CLAUSE -> ScillaPatternMatchClause(node)

                LITERAL_EXPRESSION -> ScillaLiteralExpression(node)
                REF_EXPRESSION -> ScillaRefExpression(node)
                LET_EXPRESSION -> ScillaLetExpression(node)
                MESSAGE_EXPRESSION -> ScillaMessageExpression(node)
                MESSAGE_ENTRY -> ScillaMessageEntry(node)
                MESSAGE_ENTRY_VALUE -> ScillaMessageEntryValue(node)
                FUN_EXPRESSION -> ScillaFunExpression(node)
                APP_EXPRESSION -> ScillaAppExpression(node)
                CONSTR_EXPRESSION -> ScillaConstructorExpression(node)
                MATCH_EXPRESSION -> ScillaMatchExpression(node)
                BUILTIN_EXPRESSION -> ScillaBuiltinExpression(node)
                TYPE_FUN_EXPRESSION -> ScillaTFunExpression(node)
                TYPE_APP_EXPRESSION -> ScillaTAppExpression(node)

                STATEMENT_LIST -> ScillaStatementList(node)
				LOAD_STATEMENT -> ScillaLoadStatement(node)
				REMOTE_LOAD_STATEMENT -> ScillaLoadStatement(node)
				STORE_STATEMENT -> ScillaStoreStatement(node)
				BIND_STATEMENT -> ScillaBindStatement(node)
				MAP_UPDATE_STATEMENT -> ScillaMapUpdateStatement(node)
				MAP_DELETE_STATEMENT -> ScillaMapDeleteStatement(node)
				MAP_GET_STATEMENT -> ScillaMapGetStatement(node)
				REMOTE_MAP_GET_STATEMENT -> ScillaMapGetStatement(node)
				MATCH_STATEMENT -> ScillaMatchStatement(node)
				READ_FROM_BC_STATEMENT -> ScillaReadFromBCStatement(node)
				TYPE_CAST_STATEMENT -> ScillaTypeCastStatement(node)
				ACCEPT_STATEMENT -> ScillaAcceptStatement(node)
				ITERATE_STATEMENT -> ScillaIterateStatement(node)
				SEND_STATEMENT -> ScillaSendStatement(node)
                EVENT_STATEMENT -> ScillaEventStatement(node)
                CALL_STATEMENT -> ScillaCallStatement(node)
                THROW_STATEMENT -> ScillaThrowStatement(node)
				

                CONTRACT_DEFINITION -> ScillaContract(node)
                CONTRACT_CONSTRAINT -> ScillaContractConstraint(node)
                FIELD_DEFINITION -> ScillaUserField(node)
				FIELD_REF -> ScillaFieldRefElement(node)
                TRANSITION_DEFINITION -> ScillaTransition(node)
                PROCEDURE_DEFINITION -> ScillaProcedure(node)

                LIBRARY_DEFINITION -> ScillaLibrary(node)
                LIBRARY_LET_DEFINITION -> ScillaLibraryLet(node)
                LIBRARY_TYPE_DEFINITION -> ScillaLibraryType(node)
                LIBRARY_TYPE_CONSTRUCTOR -> ScillaLibraryTypeConstructor(node)

                SCILLA_VERSION -> ScillaVersion(node)
                IMPORTS ->  ScillaImports(node)
                IMPORT_ENTRY ->  ScillaImport(node)

                GARBAGE_AT_THE_END_OF_FILE -> ScillaGarbageAtTheEndOfFile(node)

                else -> throw IllegalArgumentException("Unknown elementType: " + node.elementType)
            }
        }

    }

}
