     package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.annotations.NonNls

class ScillaElementType(@NonNls debugName: String) : IElementType(debugName, ScillaLanguage) {
    override fun toString(): String {
        return "ScillaElementType." + super.toString()
    }

    companion object {
        val SCILLA_CONTRACT_FILE = IFileElementType(ScillaLanguage)

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

        val LITERAL_EXPRESSION = ScillaElementType("LITERAL_EXPRESSION")
        val VAR_EXPRESSION = ScillaElementType("VAR_EXPRESSION")
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
        val FIXPOINT_EXPRESSION = ScillaElementType("FIXPOINT_EXPRESSION")
        val GAS_EXPRESSION = ScillaElementType("GAS_EXPRESSION")
		
		val FUN_EXPRESSIONS = TokenSet.create(FUN_EXPRESSION, TYPE_FUN_EXPRESSION) 
		val EXPRESSIONS = TokenSet.create(LITERAL_EXPRESSION, VAR_EXPRESSION, LET_EXPRESSION, MESSAGE_EXPRESSION,
		 MESSAGE_ENTRY, FUN_EXPRESSION, APP_EXPRESSION, CONSTR_EXPRESSION, MATCH_EXPRESSION, BUILTIN_EXPRESSION,
		 TYPE_FUN_EXPRESSION, TYPE_APP_EXPRESSION, FIXPOINT_EXPRESSION, GAS_EXPRESSION)


        val STATEMENT_LIST = ScillaElementType("STATEMENT_LIST")
        val FORALL_STATEMENT = ScillaElementType("FORALL_STATEMENT")
        val ACCEPT_STATEMENT = ScillaElementType("ACCEPT_STATEMENT")
        val EVENT_STATEMENT = ScillaElementType("EVENT_STATEMENT")
        val MATCH_STATEMENT = ScillaElementType("MATCH_STATEMENT")
        val THROW_STATEMENT = ScillaElementType("THROW_STATEMENT")
        val SEND_STATEMENT = ScillaElementType("SEND_STATEMENT")
        val DELETE_STATEMENT = ScillaElementType("DELETE_STATEMENT")
        val FETCH_STATEMENT = ScillaElementType("FETCH_STATEMENT")
        val LOCAL_BINDING_STATEMENT = ScillaElementType("LOCAL_BINDING_STATEMENT")
        val ASSIGN_STATEMENT = ScillaElementType("ASSIGN_STATEMENT")
        val CALL_STATEMENT = ScillaElementType("CALL_STATEMENT")

		val STATEMENTS = TokenSet.create(FORALL_STATEMENT, ACCEPT_STATEMENT, EVENT_STATEMENT, MATCH_STATEMENT,
			THROW_STATEMENT, SEND_STATEMENT, DELETE_STATEMENT, FETCH_STATEMENT, LOCAL_BINDING_STATEMENT, 
			ASSIGN_STATEMENT, CALL_STATEMENT)

        val CONTRACT_DEFINITION = ScillaElementType("CONTRACT_DEFINITION")
        val CONTRACT_CONSTRAINT = ScillaElementType("CONTRACT_CONSTRAINT")
        val ID_WITH_TYPE = ScillaElementType("ID_WITH_TYPE")
        val FIELD_DEFINITION = ScillaElementType("FIELD_DEFINITION")
        val TRANSITION_DEFINITION = ScillaElementType("TRANSITION_DEFINITION")
        val PROCEDURE_DEFINITION = ScillaElementType("PROCEDURE_DEFINITION")

        val LIBRARY_DEFINITION = ScillaElementType("LIBRARY")
        val LIBRARY_LET_DEFINITION = ScillaElementType("LIBRARY_LET_DEFINITION")
        val LIBRARY_TYPE_DEFINITION = ScillaElementType("LIBRARY_TYPE_DEFINITION")
        val LIBRARY_TYPE_CONSTRUCTOR = ScillaElementType("LIBRARY_TYPE_CONSTRUCTOR")
		
		val LIBRARY_ENTRIES = TokenSet.create(LIBRARY_LET_DEFINITION, LIBRARY_TYPE_DEFINITION)

        val SCILLA_VERSION = ScillaElementType("SCILLA_VERSION")
        val IMPORTS = ScillaElementType("IMPORTS")
        val IMPORT_NAME = ScillaElementType("IMPORT_NAME")

        val GARBAGE_AT_THE_END_OF_FILE = ScillaElementType("GARBAGE_AT_THE_END_OF_FILE")

        fun createElement(node: ASTNode): PsiElement {
            return when (node.elementType) {
                SIMPLE_REF -> ScillaSimpleRef(node)
                QUALIFIED_REF -> ScillaQualifiedRef(node)
                HEX_QUALIFIED_REF -> ScillaHexQualifiedRef(node)

                MAP_KEY -> ScillaMapKey(node)
                MAP_VALUE -> ScillaMapValue(node)
                MAP_ACCESS -> ScillaMapAccess(node)

                REF_TYPE -> ScillaRefType(node)
                MAP_TYPE -> ScillaMapType(node)
                FUN_TYPE -> ScillaFunType(node)
                POLY_TYPE -> ScillaPolyType(node)
                ADDRESS_TYPE -> ScillaAddressType(node)
                ADDRESS_TYPE_FIELD -> ScillaAddressTypeField(node)
                TYPE_VAR_TYPE -> ScillaTypeVarType(node)
                PAREN_TYPE -> ScillaParenType(node)

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
                VAR_EXPRESSION -> ScillaVarExpression(node)
                LET_EXPRESSION -> ScillaLetExpression(node)
                MESSAGE_EXPRESSION -> ScillaMessageExpression(node)
                MESSAGE_ENTRY -> ScillaMessageEntry(node)
                MESSAGE_ENTRY_VALUE -> ScillaMessageEntryValue(node)
                FUN_EXPRESSION -> ScillaFunExpression(node)
                APP_EXPRESSION -> ScillaAppExpression(node)
                CONSTR_EXPRESSION -> ScillaConstrExpression(node)
                MATCH_EXPRESSION -> ScillaMatchExpression(node)
                BUILTIN_EXPRESSION -> ScillaBuiltinExpression(node)
                TYPE_FUN_EXPRESSION -> ScillaTFunExpression(node)
                TYPE_APP_EXPRESSION -> ScillaTAppExpression(node)
                FIXPOINT_EXPRESSION -> ScillaFixpointExpression(node)
                GAS_EXPRESSION -> ScillaGasExpression(node)

                STATEMENT_LIST -> ScillaStatementList(node)
                FORALL_STATEMENT -> ScillaForallStatement(node)
                ACCEPT_STATEMENT -> ScillaAcceptStatement(node)
                EVENT_STATEMENT -> ScillaEventStatement(node)
                MATCH_STATEMENT -> ScillaMatchStatement(node)
                THROW_STATEMENT -> ScillaThrowStatement(node)
                SEND_STATEMENT -> ScillaSendStatement(node)
                DELETE_STATEMENT -> ScillaDeleteStatement(node)
                FETCH_STATEMENT -> ScillaFetchStatement(node)
                LOCAL_BINDING_STATEMENT -> ScillaLocalBindingStatement(node)
                ASSIGN_STATEMENT -> ScillaAssignStatement(node)
                CALL_STATEMENT -> ScillaCallStatement(node)

                CONTRACT_DEFINITION -> ScillaContractDefinition(node)
                CONTRACT_CONSTRAINT -> ScillaContractConstraint(node)
                FIELD_DEFINITION -> ScillaFieldDefinition(node)
                TRANSITION_DEFINITION -> ScillaTransitionDefinition(node)
                PROCEDURE_DEFINITION -> ScillaProcedureDefinition(node)

                LIBRARY_DEFINITION -> ScillaLibraryDefinition(node)
                LIBRARY_LET_DEFINITION -> ScillaLibraryLetDefinition(node)
                LIBRARY_TYPE_DEFINITION -> ScillaLibraryTypeDefinition(node)
                LIBRARY_TYPE_CONSTRUCTOR -> ScillaLibraryTypeConstructor(node)

                SCILLA_VERSION -> ScillaVersion(node)
                IMPORTS ->  ScillaImports(node)
                IMPORT_NAME ->  ScillaImportName(node)

                GARBAGE_AT_THE_END_OF_FILE -> ScillaGarbageAtTheEndOfFile(node)

                else -> throw IllegalArgumentException("Unknown elementType: " + node.elementType)
            }
        }

    }

}
