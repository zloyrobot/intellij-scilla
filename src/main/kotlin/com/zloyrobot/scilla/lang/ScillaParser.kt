package com.zloyrobot.scilla.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 *
 * Sid:
 *   | ID
 *   | SPID
 *   | CID '.' ID
 *
 * SCid:
 *   | CID
 *   | CID '.' CID
 *   | HEX '.' CID
 *
 *
 * Type:
 *   | '(' Type ')'
 *   | SCid TypeArgList
 *   | TID
 *   | AddressType
 *   | 'Map' MapKey MapValue
 *   | Type '->' Type
 *   | 'forall' TID '.' Type
 *
 * TypeArg:
 *   | '(' Type ')'
 *   | SCid
 *   | TID
 *   | AddressType
 *   | 'Map' MapKey MapValue
 *
 * MapKey:
 *   | SCid
 *   | '(' SCid ')'
 *   | AddressType
 *   | '(' AddressType ')'
 *
 * MapValue:
 *   | SCid
 *   | 'Map' MapKey MapValue
 *   | '(' MapValueAllowTypeArgs ')'
 *   | AddressType
 *
 * MapValueAllowTypeArgs:
 *   | SCid MapValueArgNonEmptyList // We only allow type args when the type is surrounded by parentheses
 *   | MapValue
 *
 * MapValueArg:
 *   | SCid
 *   | 'Map' MapKey MapValue
 *   | '(' MapValueAllowTypeArgs ')'
 *
 * AddressType:
 *  | CID 'with' 'end'
 *  | CID 'with' 'contract' AddressTypeFieldList 'end'
 *  | CID 'with' 'contract' '(' ParameterList ')' AddressTypeFieldList 'end'
 *
 * AddressTypeFieldList:
 *  | AddressTypeField
 *  | AddressTypeField ',' AddressTypeFieldList
 *
 * AddressTypeField:
 *  | 'field' IdWithType
 *
 * ParameterList:
 *  | IdWithType
 *  | IdWithType ',' ParameterList
 *
 * TypeAnnotation:
 *   | ':' Type
 *
 * IdWithType:
 *   | ID TypeAnnotation
 *
 *
 * Expression:
 *   | Sid
 *   | Literal
 *   | LetExpression
 *   | FunctionExpression
 *   | Application
 *   | BuiltinCall
 *   | MessageConstruction
 *   | DataConstructorApplication
 *   | MatchExpression
 *   | TypeFunctionExpression
 *   | TypeApplication
 *
 * Literal:
 *   | CID INT
 *   | HEX
 *   | STRING
 *   | 'Emp' MapKey MapValue
 *
 * LetExpression:
 *   | 'let' ID TypeAnnotation? '=' Expression 'in' Expression
 *
 * FunctionExpression:
 *   | 'fun' '(' IdWithType ')' '=>' Expression
 *
 * Application:
 *   | Sid SidList
 *
 * BuiltinCall:
 *   | 'builtin' ID ('{' TypeArgList '}')? BuiltinArgs
 *
 * BuiltinArgs:
 *   | SidList
 *   | '(' ')'
 *
 * MessageConstruction:
 *   | '{' MsgEntryList '}'
 *
 * MsgEntryList:
 *   | MsgEntry
 *   | MsgEntry ';' MsgEntryList
 *
 * MsgEntry:
 *   | Sid ':' Literal
 *   | Sid ':' Sid
 *
 * DataConstructorApplication:
 *   | SCid  ('{' TypeArgList '}')? SidList?
 *
 * MatchExpression:
 *   | 'match' Sid 'with' ExpressionPatternMatchingClauseList 'end'
 *
 * ExpressionPatternMatchingClause:
 *   | '|' Pattern '=>' Expression
 *
 * Pattern:
 *   | '_'
 *   | ID
 *   | SCid ArgPatternList
 *
 * ArgPattern:
 *   | '_'
 *   | ID
 *   | SCid
 *   | '(' Pattern ')'
 *
 * TypeFunctionExpression:
 *   | 'tfun' TID '=>' Expression
 *
 * TypeApplication:
 *   | '@' Sid TypeArgList
 *
 *
 *
 * StatementList:
 *   | Statement
 *   | Statement ';' StatementList
 *
 * Statement:
 *   | ID ':=' Sid
 *   | ID MapAccessNonEmptyList ':=' Sid
 *   | ID '=' Expression
 *   | ID '<-' Sid
 *   | ID '<-' ID MapAccessNonEmptyList
 *   | ID '<-' 'exists' ID MapAccessNonEmptyList
 *   | ID '<-' '&' CID
 *   | ID '<-' '&' SPID '.' SPID
 *   | ID '<-' '&' ID '.' Sid
 *   | ID '<-' '&' ID '.' '(' Sid ')' //Remote fetch of contract parameters not yet supported
 *   | ID '<-' '&' ID '.' ID MapAccessNonEmptyList
 *   | ID '<-' '&' 'exists' ID '.' ID MapAccessNonEmptyList
 *   | ID '<-' '&' Sid 'as' AddressType
 *   | ComponentId SidList
 *   | 'delete' ID MapAccessNonEmptyList
 *   | 'accept'
 *   | 'send' Sid
 *   | 'event' Sid
 *   | 'throw' Sid?
 *   | 'match' Sid 'with' StatementPatternMatchingClauseList 'end'
 *   | 'forall' Sid ComponentId
 *
 * StatementPatternMatchingClause:
 *   | '|' Pattern '=>' StatementList
 *
 * MapAccess:
 *   | '[' Sid ']'
 *
 *
 *
 * ContractModule:
 *   | 'scilla_version' INT Imports Library? Contract EOF
 *
 * Imports:
 *   | 'import' ImportNameList
 *
 * ImportName:
 *   | CID
 *   | CID 'as' CID
 *
 * Library:
 *   | 'library' CID LibraryEntryList
 *
 * LibraryEntry:
 *   | 'let' ID TypeAnnotation? '=' Expression
 *   | Type CID ('=' TypeConstructorNonEmptyList)?
 *
 * TypeConstructor:
 *   | '|' CID ('of' TypeArgNonEmptyList)?
 *
 * Contract:
 *   | 'contract' CID '(' ParameterList ')' ('with' Expression '->')? FieldsList ComponentList
 *
 * Field:
 *   | 'field' IdWithType '=' Expression
 *
 * Component:
 *   | 'transition' ComponentId '(' ParameterList ')' StatementList 'end'
 *   | 'procedure'  ComponentId '(' ParameterList ')' StatementList 'end'
 *
 * ComponentId:
 *   | CID
 *   | ID
 *
 */
class ScillaParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        InnerParser(builder).parseContractModule()
        return builder.treeBuilt
    }

    class InnerParser(private val builder: PsiBuilder) {

        private val identBeginningWithLowerCaseLetter = "(identifier beginning with a lowercase letter)"
        private val identCapitalized = "(capitalized identifier)"
        private val identBeginningWithUnderscore = "(identifier beginning with '_')"
        private val identBeginningWithQuote = "(identifier beginning with ')"

        /**
        * ContractModule:
        *   | 'scilla_version' INT ImportList Library? Contract EOF
        */
        fun parseContractModule() {
            val mark = builder.mark()

            parseScillaVersion()
            if (builder.tokenType == ScillaTokenType.IMPORT) {
                parseImports()
            }

            if (builder.tokenType == ScillaTokenType.LIBRARY)
                parseLibrary()

            parseContract()

            if (builder.tokenType != null)
                parseGarbage()

            mark.done(ScillaElementType.SCILLA_CONTRACT_FILE)
        }

        /**
         * Imports:
         *   | 'import' ImportNameList
         *
         * ImportName:
         *   | CID
         *   | CID 'as' CID
         */
        private fun parseImports() {
            val mark = builder.mark()

            assertAdvance(ScillaTokenType.IMPORT)
            while (builder.tokenType == ScillaTokenType.CID) {
                assertAdvance(ScillaTokenType.CID)
                if (builder.tokenType == ScillaTokenType.AS) {
                    assertAdvance(ScillaTokenType.AS)
                    expectAdvance(ScillaTokenType.CID, "namespace name")
                }
            }
            mark.done(ScillaElementType.IMPORTS)

        }

        private fun parseGarbage() {
            val mark = builder.mark()
            builder.error("Expected library or contract definition")

            while (builder.tokenType != null) {
                when(builder.tokenType) {
                    ScillaTokenType.LET, ScillaTokenType.TYPE -> parseLibraryEntry()
                    ScillaTokenType.FIELD -> parseField()
                    ScillaTokenType.TRANSITION, ScillaTokenType.PROCEDURE -> tryParseComponent()
                    else -> advance()
                }
            }
            mark.done(ScillaElementType.GARBAGE_AT_THE_END_OF_FILE)
        }

        private fun parseScillaVersion() {
            val mark = builder.mark()

            if (expectAdvance(ScillaTokenType.SCILLA_VERSION, "'scilla_version'"))
                expectAdvance(ScillaTokenType.INT, "version number")

            mark.done(ScillaElementType.SCILLA_VERSION)
        }

        /**
        * Library:
        *   | 'library' CID LibEntryList
        */
        private fun parseLibrary() {
            val mark = builder.mark()

            assertAdvance(ScillaTokenType.LIBRARY)
            expectAdvance(ScillaTokenType.CID, "library name")

            while (builder.tokenType != null && builder.tokenType != ScillaTokenType.CONTRACT) {
                if (builder.tokenType == ScillaTokenType.LET || builder.tokenType == ScillaTokenType.TYPE)
                    parseLibraryEntry()
				else if (builder.tokenType == ScillaTokenType.TRANSITION || builder.tokenType == ScillaTokenType.PROCEDURE) {
					//Parser error will be reported by parseContract method
					break
				}
                else {
                    errorAdvance("let binding, type declaration or contract definition")
                }
            }
            mark.done(ScillaElementType.LIBRARY_DEFINITION)
        }

        /**
        * LibraryEntry:
        *   | 'let' ID TypeAnnotation? '=' Expression
        *   | Type CID ('=' TypeConstructorNonEmptyList)?
        */
        private fun parseLibraryEntry() {
            assert(builder.tokenType == ScillaTokenType.LET || builder.tokenType == ScillaTokenType.TYPE)

            when (builder.tokenType) {
                ScillaTokenType.LET -> parseLetExpression(true) //Syntactically it's LetExpression
                ScillaTokenType.TYPE -> {
                    val mark = builder.mark()
                    assertAdvance(ScillaTokenType.TYPE)
                    expectAdvance(ScillaTokenType.CID, "type name")
                    if (builder.tokenType == ScillaTokenType.EQ) {
                        assertAdvance(ScillaTokenType.EQ)
                        while (builder.tokenType == ScillaTokenType.BAR || detectSCid() || detectSid())
                            parseTypeConstructor()
                    }
                    mark.done(ScillaElementType.LIBRARY_TYPE_DEFINITION)
                }
            }
        }

        /**
        * TypeConstructor:
        *   | '|' CID ('of' TypeArgNonEmptyList)?
        */
        private fun parseTypeConstructor() {
            val mark = builder.mark()
            expectAdvance(ScillaTokenType.BAR, "'|' before type constructor name")
            expectAdvance(ScillaTokenType.CID, "type constructor name")
            if (builder.tokenType == ScillaTokenType.OF) {
                assertAdvance(ScillaTokenType.OF)
				if (!tryParseTypeArg()) {
					builder.error("Expected type parameter")
				}
				else while (builder.tokenType != null) {
                    if (!tryParseTypeArg())
                        break
                }
            }
            mark.done(ScillaElementType.LIBRARY_TYPE_CONSTRUCTOR)
        }

        /**
         * Statement:
         *   | ID ':=' Sid
         *   | ID MapAccessNonEmptyList ':=' Sid
         *   | ID '=' Expression
         *   | ID '<-' Sid
         *   | ID '<-' ID MapAccessNonEmptyList
         *   | ID '<-' 'exists' ID MapAccessNonEmptyList
         *   | ID '<-' '&' CID
         *   | ID '<-' '&' SPID '.' SPID
         *   | ID '<-' '&' ID '.' Sid
         *   | ID '<-' '&' ID '.' '(' Sid ')' //Remote fetch of contract parameters not yet supported
         *   | ID '<-' '&' ID '.' ID MapAccessNonEmptyList
         *   | ID '<-' '&' 'exists' ID '.' ID MapAccessNonEmptyList
         *   | ID '<-' '&' Sid 'as' AddressType
         *   | ComponentId SidList
         *   | 'delete' ID MapAccessNonEmptyList
         *   | 'accept'
         *   | 'send' Sid
         *   | 'event' Sid
         *   | 'throw' Sid?
         *   | 'match' Sid 'with' StatementPatternMatchingClauseList 'end'
         *   | 'forall' Sid ComponentId
         *
         * ComponentId:
         *   | CID
         *   | ID
         */
        private fun parseStatement() {
            when (builder.tokenType) {
                ScillaTokenType.ID -> {
                    when(builder.lookAhead(1)) {
                        ScillaTokenType.FETCH -> parseFetchStatement()
                        ScillaTokenType.ASSIGN, ScillaTokenType.LBRACKET -> parseAssignStatement()
                        ScillaTokenType.EQ -> parseLocalBinding()
                        ScillaTokenType.ID,ScillaTokenType.SPID,ScillaTokenType.CID -> parseProcedureCallStatement()
                        else -> errorAdvance("Expected statement") //TODO: create InvalidStatement?
                    }
                }
                ScillaTokenType.CID -> parseProcedureCallStatement()

                ScillaTokenType.ACCEPT -> {
                    val mark = builder.mark()
                    assertAdvance(ScillaTokenType.ACCEPT)
                    mark.done(ScillaElementType.ACCEPT_STATEMENT)
                }
                ScillaTokenType.DELETE -> parseDeleteStatement()
                ScillaTokenType.SEND -> parseSendStatement()
                ScillaTokenType.EVENT -> parseEventStatement()
                ScillaTokenType.THROW -> parseThrowStatement()
                ScillaTokenType.MATCH -> parseMatchStatement()
                ScillaTokenType.FORALL -> parseForallStatement()
                else -> errorAdvance("Expected statement")
            }
        }

        /**
         *   | ID '<-' Sid
         *   | ID '<-' ID MapAccessNonEmptyList
         *   | ID '<-' 'exists' ID MapAccessNonEmptyList
         *   | ID '<-' '&' CID                                      //Fetch the value of the blockchain state variable
         *   | ID '<-' '&' SPID '.' SPID
         *   | ID '<-' '&' ID '.' Sid
         *   | ID '<-' '&' ID '.' '(' Sid ')'                   //Remote fetch of contract parameters not yet supported
         *   | ID '<-' '&' ID '.' ID MapAccessNonEmptyList
         *   | ID '<-' '&' 'exists' ID '.' ID MapAccessNonEmptyList
         *   | ID '<-' '&' Sid 'as' AddressType
         *
         * Sid:
         *   | ID
         *   | SPID
         *   | CID '.' ID
         */
        private fun parseFetchStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.ID)
            assertAdvance(ScillaTokenType.FETCH)

            val isRemote = if (builder.tokenType == ScillaTokenType.AMP) {
                advance(); true
            }
            else false

            if (detectSid()) {
                parseSidOrSCid(false, "contract field") //TODO: disallow 'Sid MapAccessNonEmptyList'
                if (isRemote) {
					if (builder.tokenType == ScillaTokenType.AS) {
						assertAdvance(ScillaTokenType.AS)
						parseAddressType()
					}
					else {
						expectAdvance(ScillaTokenType.DOT, "'.'")
						if (builder.tokenType == ScillaTokenType.LPAREN) {
							assertAdvance(ScillaTokenType.LPAREN)
							parseSidOrSCid(false, "remote contract parameter")
							expectAdvance(ScillaTokenType.RPAREN, "')'")
						} else 
							parseSidOrSCid(false, "remote contract field")
						
					}
                }
                while (builder.tokenType == ScillaTokenType.LBRACKET) {
                    parseMapAccess()
                }
            }
            else if (builder.tokenType == ScillaTokenType.CID) {
                assertAdvance(ScillaTokenType.CID)
            }
            else if (builder.tokenType == ScillaTokenType.EXISTS) {
                assertAdvance(ScillaTokenType.EXISTS)
                if (isRemote) {
                    expectAdvance(ScillaTokenType.ID, "address of contract")
                    expectAdvance(ScillaTokenType.DOT, "'.'")
                }
                expectAdvance(ScillaTokenType.ID, "contract field")
                while (builder.tokenType == ScillaTokenType.LBRACKET) {
                    parseMapAccess()
                }
            }
            mark.done(ScillaElementType.FETCH_STATEMENT)
        }


        private fun tryParseStatement(): Boolean {
            if (builder.tokenType == ScillaTokenType.CID ||
                builder.tokenType == ScillaTokenType.ID ||
                builder.tokenType == ScillaTokenType.ACCEPT ||
                builder.tokenType == ScillaTokenType.DELETE ||
                builder.tokenType == ScillaTokenType.SEND ||
                builder.tokenType == ScillaTokenType.EVENT ||
                builder.tokenType == ScillaTokenType.THROW ||
                builder.tokenType == ScillaTokenType.MATCH ||
                builder.tokenType == ScillaTokenType.FORALL) {
                parseStatement()
                return true
            }
            return false
        }

        private fun parseStatementList() {
            val statementsMark = builder.mark()
			parseLoop("statement", ScillaTokenType.SEMICOLON, "';'", listOf(
				ScillaTokenType.END,
				ScillaTokenType.BAR,
				ScillaTokenType.CONTRACT, 
				ScillaTokenType.LIBRARY,
				ScillaTokenType.TRANSITION,
				ScillaTokenType.PROCEDURE)) {
				
				tryParseStatement()
			}
            statementsMark.done(ScillaElementType.STATEMENT_LIST)
        }

        /**
         *   | ID '=' Expression
         */
        private fun parseLocalBinding() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.ID)
            expectAdvance(ScillaTokenType.EQ, "'='")
            parseExpression()
            mark.done(ScillaElementType.LOCAL_BINDING_STATEMENT)
        }

        /**
         *   | ID ':=' Sid
         *   | ID MapAccessNonEmptyList ':=' Sid
         */
        private fun parseAssignStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.ID)
            while (builder.tokenType == ScillaTokenType.LBRACKET) {
                parseMapAccess()
            }

            expectAdvance(ScillaTokenType.ASSIGN, "':='")
            parseSidOrSCid(false, "value")
            mark.done(ScillaElementType.ASSIGN_STATEMENT)

        }

        /**
         *   | ComponentId SidList
         */
        private fun parseProcedureCallStatement() {
            assert(builder.tokenType == ScillaTokenType.CID || builder.tokenType == ScillaTokenType.ID)

            val mark = builder.mark()
            advance()

            while (detectSid()) {
                parseSidOrSCid(false, "constructor application argument")
            }
            mark.done(ScillaElementType.CALL_STATEMENT)
        }

        /**
         *   | 'forall' Sid ComponentId
         */
        private fun parseForallStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.FORALL)
            parseSidOrSCid(false, "list")
            if (builder.tokenType == ScillaTokenType.ID || builder.tokenType == ScillaTokenType.CID) {
                advance()
            }
            else {
                builder.error("Expected component id")
            }

            mark.done(ScillaElementType.FORALL_STATEMENT)
        }

        /**
         *   | 'match' Sid 'with' StatementPatternMatchingClauseList 'end'
         */
        private fun parseMatchStatement() {
            val mark = builder.mark()

            assertAdvance(ScillaTokenType.MATCH)
            parseSidOrSCid(false, "value")
            expectAdvance(ScillaTokenType.WITH, "'with'")

            while (tryParseExpressionPatternMatchingClause(true)) {
                continue
            }
            expectAdvance(ScillaTokenType.END, "'end'")
            mark.done(ScillaElementType.MATCH_STATEMENT)

        }

        /**
         *   | 'throw' Sid?
         */
        private fun parseThrowStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.THROW)
            if (detectSid())
                parseSidOrSCid(false, "exception")
            mark.done(ScillaElementType.THROW_STATEMENT)

        }

        /**
         *   | 'event' Sid
         */
        private fun parseEventStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.EVENT)
            parseSidOrSCid(false, "message")
            mark.done(ScillaElementType.EVENT_STATEMENT)
        }

        /**
         *   | 'send' Sid
         */
        private fun parseSendStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.SEND)
            parseSidOrSCid(false, "list of messages")
            mark.done(ScillaElementType.SEND_STATEMENT)
        }

        /**
         * | 'delete' ID MapAccessNonEmptyList
         */
        private fun parseDeleteStatement() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.DELETE)
            expectAdvance(ScillaTokenType.ID, "field")
            if (builder.tokenType != ScillaTokenType.LBRACKET) {
                builder.error("Expected '['")
            }
            else while (builder.tokenType == ScillaTokenType.LBRACKET) {
                parseMapAccess()
            }
            mark.done(ScillaElementType.DELETE_STATEMENT)
        }

        /**
         * MapAccess:
         *   | '[' Sid ']'
         */
        private fun parseMapAccess() {
            val accessMark = builder.mark()
            assertAdvance(ScillaTokenType.LBRACKET)
            parseSidOrSCid(false, "key")
            expectAdvance(ScillaTokenType.RBRACKET, "']'")
            accessMark.done(ScillaElementType.MAP_ACCESS)
        }

        /**
        * Contract:
        *   | 'contract' CID '(' ParameterList ')' ('with' Expression '->')? FieldsList ComponentsList
        */
        private fun parseContract() {
            val marker = builder.mark()
            if (expectAdvance(ScillaTokenType.CONTRACT, "contract definition")) {
                expectAdvance(ScillaTokenType.CID, "contract name")
                if (builder.tokenType == ScillaTokenType.LPAREN)
                    parseParameterList(ScillaElementType.CONTRACT_PARAMETERS)
                else builder.error("Expected contract parameter list")

                if (builder.tokenType == ScillaTokenType.WITH) {
					parseContractConstraint()
				}
                while (builder.tokenType == ScillaTokenType.FIELD) {
                    parseField()
                }
                while (builder.tokenType != null) {
                    if (!tryParseComponent()) {
                        errorAdvance("transition or procedure declaration")
                    }

                }
            }
            marker.done(ScillaElementType.CONTRACT_DEFINITION)
        }

		/**
		 *   | 'with' Expression '->'
		 */
		private fun parseContractConstraint() {
			val mark = builder.mark()
			
			assertAdvance(ScillaTokenType.WITH)
			parseExpression()
			expectAdvance(ScillaTokenType.ARROW, "'->'")
			
			mark.done(ScillaElementType.CONTRACT_CONSTRAINT)
		}

		private fun parseParameterList(parametersKind: ScillaElementType) {
            val mark = builder.mark()

            assertAdvance(ScillaTokenType.LPAREN)
            parseLoop("parameter", ScillaTokenType.COMMA, "','", listOf(
				ScillaTokenType.RPAREN,
				ScillaTokenType.LET,
				ScillaTokenType.TYPE,
				ScillaTokenType.END,
				ScillaTokenType.CONTRACT,
				ScillaTokenType.LIBRARY,
				ScillaTokenType.TRANSITION,
				ScillaTokenType.PROCEDURE)) {
                parseIdWithType("parameter")
            }
            expectAdvance(ScillaTokenType.RPAREN, "')'")

            mark.done(parametersKind)
        }

        /** Field:
        *   | 'field' IdWithType '=' Expression
        */
        private fun parseField() {
            val marker = builder.mark()

            assertAdvance(ScillaTokenType.FIELD)
			
			expectAdvance(ScillaTokenType.ID, "field name")
			parseTypeAnnotation()
			
            if (expectAdvance(ScillaTokenType.EQ, "'='"))
                parseExpression()

            marker.done(ScillaElementType.FIELD_DEFINITION)
        }

        /**
        * Component:
        *   | 'transition' ComponentId '(' ParameterList ')' StatementsList 'end'
        *   | 'procedure'  ComponentId '(' ParameterList ')' StatementsList 'end'
        */
        private fun tryParseComponent(): Boolean {
            when(builder.tokenType) {
                ScillaTokenType.TRANSITION -> {
                    parseTransitionOrProcedure(true)
                    return true
                }
                ScillaTokenType.PROCEDURE -> {
                    parseTransitionOrProcedure(false)
                    return true
                }
            }
            return false
        }

        /**
         * Component:
         *   | 'transition' ComponentId '(' ParameterList ')' StatementsList 'end'
         *   | 'procedure'  ComponentId '(' ParameterList ')' StatementsList 'end'
         */
        private fun parseTransitionOrProcedure(transition: Boolean) {
            assert(builder.tokenType == ScillaTokenType.TRANSITION || builder.tokenType == ScillaTokenType.PROCEDURE)

            val mark = builder.mark()
            advance()
            if (builder.tokenType != ScillaTokenType.CID && builder.tokenType != ScillaTokenType.ID)
                builder.error("Expected ${if (transition) "transition" else "procedure"} name")
            else advance()

            if (builder.tokenType == ScillaTokenType.LPAREN)
                parseParameterList(ScillaElementType.COMPONENT_PARAMETERS)
            else
                builder.error("Expected parameter list in parens")

            parseStatementList()
            expectAdvance(ScillaTokenType.END, "'end'")

			if (transition)
            	mark.done(ScillaElementType.TRANSITION_DEFINITION)
			else
				mark.done(ScillaElementType.PROCEDURE_DEFINITION)
        }


        /**
         * Sid:
         *   | ID
         *   | SPID
         *   | CID '.' ID
         *
         * SCid:
         *   | CID
         *   | CID '.' CID
         *   | HEX '.' CID
         */
        private fun parseSidOrSCid(expectedSCid: Boolean, expectedName: String) {
            val mark = builder.mark()
            when (builder.tokenType) {
                ScillaTokenType.CID -> {
                    assertAdvance(ScillaTokenType.CID)
                    if (builder.tokenType == ScillaTokenType.DOT) {
                        assertAdvance(ScillaTokenType.DOT)
                        if (expectedSCid)
                            expectAdvance(ScillaTokenType.CID, expectedName)
                        else
                            expectAdvance(ScillaTokenType.ID, expectedName)
                        mark.done(ScillaElementType.QUALIFIED_REF)
                    }
                    else {
                        mark.done(ScillaElementType.SIMPLE_REF)
                        if (!expectedSCid)
                            mark.precede().error("Expected $expectedName $identBeginningWithLowerCaseLetter")
                    }
                }
                ScillaTokenType.ID, ScillaTokenType.SPID -> {
                    advance()
                    mark.done(ScillaElementType.SIMPLE_REF)
                    if (expectedSCid)
                        mark.precede().error("Expected $expectedName $identCapitalized")
                }
                ScillaTokenType.HEX -> {
                    assertAdvance(ScillaTokenType.HEX)
                    if (expectAdvance(ScillaTokenType.DOT, "'.'")) {
                        if (expectedSCid) {
                            expectAdvance(ScillaTokenType.CID, expectedName)
                            mark.done(ScillaElementType.HEX_QUALIFIED_REF)
                        }
                        else {
                            if (builder.tokenType in ScillaTokenType.IDENTS)
                                advance()

                            mark.done(ScillaElementType.HEX_QUALIFIED_REF)
                            mark.precede().error("Expected $expectedName $identBeginningWithLowerCaseLetter")
                        }
                    }
                    else {
                        mark.done(ScillaElementType.HEX_QUALIFIED_REF)
                        mark.precede().error("Expected $expectedName $identBeginningWithLowerCaseLetter")
                    }
                }
                else -> {
                    mark.done(ScillaElementType.SIMPLE_REF) //TODO: INVALID_REF
                    if (expectedSCid)
                        mark.precede().error("Expected $expectedName $identCapitalized")
                    else
                        mark.precede().error("Expected $expectedName $identBeginningWithLowerCaseLetter")
                }
            }
        }

        /**
         * Sid:
         *   | ID
         *   | SPID
         *   | CID '.' ID
         */
        private fun detectSid(): Boolean {
            return builder.tokenType == ScillaTokenType.ID ||
                    builder.tokenType == ScillaTokenType.SPID ||
                        builder.tokenType == ScillaTokenType.CID &&
                        builder.lookAhead(1) == ScillaTokenType.DOT &&
                        builder.lookAhead(2) == ScillaTokenType.ID
        }

        /**
         * SCid:
         *   | CID
         *   | CID '.' CID
         *   | HEX '.' CID
         */
        private fun detectSCid(): Boolean {
            return builder.tokenType == ScillaTokenType.CID ||
                    builder.tokenType == ScillaTokenType.HEX &&
                    builder.lookAhead(1) == ScillaTokenType.DOT &&
                    builder.lookAhead(2) == ScillaTokenType.CID
        }

        /**
        * TypeAnnotation:
        *   | ':' Type
        */
        private fun tryParseTypeAnnotation() {
            if (builder.tokenType == ScillaTokenType.COLON) {
                assertAdvance(ScillaTokenType.COLON)
                parseType()
            }
        }
        private fun parseTypeAnnotation() {
            expectAdvance(ScillaTokenType.COLON, "':'")
            parseType()
        }

        /**
        * IdWithType:
        *   | ID TypeAnnotation
        */
        private fun parseIdWithType(idKind: String) {
            val mark = builder.mark()

            expectAdvance(ScillaTokenType.ID, idKind)
            parseTypeAnnotation()

            mark.done(ScillaElementType.ID_WITH_TYPE)
        }

        /**
         * AddressType:
         *  | CID 'with' 'end'
         *  | CID 'with' 'contract' AddressTypeFieldList 'end'
         *  | CID 'with' 'contract' '(' ParameterList ')' AddressTypeFieldList 'end'
         */
        private fun parseAddressType() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.CID)
            expectAdvance(ScillaTokenType.WITH, "'with'")
            if (builder.tokenType == ScillaTokenType.END) {
                assertAdvance(ScillaTokenType.END)
            }
            else {
                if (expectAdvance(ScillaTokenType.CONTRACT, "'contract'")) {
                    if (builder.tokenType == ScillaTokenType.LPAREN) {
                        parseParameterList(ScillaElementType.CONTRACT_REF_PARAMETERS)
                    }
                    while (builder.tokenType == ScillaTokenType.FIELD) {
                        parseAddressTypeField()
                        if (builder.tokenType == ScillaTokenType.END)
                            break

                        expectAdvance(ScillaTokenType.COMMA, "','")
                    }
                    expectAdvance(ScillaTokenType.END, "'end'")
                }
                else builder.error("Expected 'contract' or 'end'")
            }
            mark.done(ScillaElementType.ADDRESS_TYPE)
        }

        /**
         * AddressTypeField:
         *  | 'field' IdWithType
         */
        private fun parseAddressTypeField() {
            val marker = builder.mark()

            assertAdvance(ScillaTokenType.FIELD)
            parseIdWithType("field name")

            marker.done(ScillaElementType.ADDRESS_TYPE_FIELD)
        }

        /**
        * Type:
        *   | SCid TypeArgList
        *   | 'Map' MapKey MapValue
        *   | Type '->' Type
        *   | '(' Type ')'
        *   | AddressType
        *   | 'forall' TID '.' Type
        *   | TID
        */
        private fun parseType() {
            val mark = builder.mark()
            when(builder.tokenType) {
                ScillaTokenType.TID -> {
                    assertAdvance(ScillaTokenType.TID)
                    mark.done(ScillaElementType.TYPE_VAR_TYPE)
                }
                ScillaTokenType.MAP -> {
                    assertAdvance(ScillaTokenType.MAP)
                    parseMapKey()
                    parseMapValue()
                    mark.done(ScillaElementType.MAP_TYPE)
                }
                ScillaTokenType.LPAREN -> {
                    assertAdvance(ScillaTokenType.LPAREN)
                    parseType()
                    expectAdvance(ScillaTokenType.RPAREN, "')'")
                    mark.done(ScillaElementType.PAREN_TYPE)
                }
                ScillaTokenType.CID, ScillaTokenType.ID, ScillaTokenType.SPID -> {
                    if (builder.lookAhead(1) == ScillaTokenType.WITH) {
                        mark.drop() // parseAddressType creates marker
                        parseAddressType()
                    }
                    else {
                        parseSidOrSCid(true, "type")
                        while (builder.tokenType != null) {
                            if (!tryParseTypeArg())
                                break
                        }
                        mark.done(ScillaElementType.REF_TYPE)
                    }
                }
                ScillaTokenType.FORALL -> {
                    assertAdvance(ScillaTokenType.FORALL)
                    if (expectAdvance(ScillaTokenType.TID, "type variable")) {
                        expectAdvance(ScillaTokenType.DOT, "'.'")
                        parseType()
                    }
                    mark.done(ScillaElementType.POLY_TYPE)
                }
                else -> mark.error("Expected type")
            }

            if (builder.tokenType == ScillaTokenType.TARROW) {
                assertAdvance(ScillaTokenType.TARROW)
                val precede = mark.precede()
                parseType()
                precede.done(ScillaElementType.FUN_TYPE)
            }
        }

        /**
        * TypeArg:
        *   | '(' Type ')'
        *   | SCid
        *   | TID
        *   | AddressType
        *   | 'Map' MapKey MapValue
        */
        private fun tryParseTypeArg() : Boolean {
            when (builder.tokenType) {
                ScillaTokenType.TID,
                ScillaTokenType.MAP,
                ScillaTokenType.LPAREN,
                ScillaTokenType.CID -> {
                    parseType()
                    return true
                }
            }
            return false
        }

        /**
         * LibraryEntry:
         *   | 'let' ID TypeAnnotation? '=' Expression
         * LetExpression:
         *   | 'let' ID TypeAnnotation? '=' Expression 'in' Expression
         */
        private fun parseLetExpression(libraryEntry: Boolean) {
            val marker = builder.mark()
            assertAdvance(ScillaTokenType.LET)
            expectAdvance(ScillaTokenType.ID, "let binding name")
            tryParseTypeAnnotation()
            expectAdvance(ScillaTokenType.EQ, "'='")
            parseExpression()
            if (libraryEntry) {
                marker.done(ScillaElementType.LIBRARY_LET_DEFINITION)
            } else {
                expectAdvance(ScillaTokenType.IN, "'in' expression")
                parseExpression()
                marker.done(ScillaElementType.LET_EXPRESSION)
            }
        }

        /**
         * FunctionExpression:
         *   | 'fun' '(' IdWithType ')' '=>' Expression
         */
        private fun parseFunExpression() {
            val marker = builder.mark()
            assertAdvance(ScillaTokenType.FUN)
            if (builder.tokenType == ScillaTokenType.LPAREN) {
				parseParameterList(ScillaElementType.FUNCTION_PARAMETERS) // according to grammar only one parameter is allowed, check it in daemon
                expectAdvance(ScillaTokenType.ARROW, "function arrow ('=>')")
                parseExpression()
            }
			else 
				builder.error("Expected function parameter")
			
            marker.done(ScillaElementType.FUN_EXPRESSION)
        }

        /**
         * TypeFunctionExpression:
         *   | 'tfun' TID '=>' Expression
         */
        private fun parseTypeFunctionExpression() {
            val marker = builder.mark()
            assertAdvance(ScillaTokenType.TFUN)
            if (expectAdvance(ScillaTokenType.TID, "type function parameter")) {
                expectAdvance(ScillaTokenType.ARROW, "type function arrow ('=>')")
                parseExpression()
            }
            marker.done(ScillaElementType.TYPE_FUN_EXPRESSION)
        }


        /**
         * BuiltinCall:
         *   | 'builtin' ID ('{' TypeArgList '}')? BuiltinArgs
         *
         * BuiltinArgs:
         *   | SidList
         *   | '(' ')'
         */
        private fun parseBuiltinCallExpression() {
            val marker = builder.mark()
            assertAdvance(ScillaTokenType.BUILTIN)
            expectAdvance(ScillaTokenType.ID, "builtin function")
            if (builder.tokenType == ScillaTokenType.LBRACE) {
                assertAdvance(ScillaTokenType.LBRACE)
                parseLoop("type argument",null, null, listOf(ScillaTokenType.RBRACE)) {
                    tryParseTypeArg()
                }
                expectAdvance(ScillaTokenType.RBRACE, "'}'")
            }

            if (builder.tokenType == ScillaTokenType.LPAREN) {
                assertAdvance(ScillaTokenType.LPAREN)
                expectAdvance(ScillaTokenType.RPAREN, "')")
            }
            else if (!detectSid()) {
                builder.error("Expected builtin function argument")
            }
            else while (detectSid()) {
                parseSidOrSCid(false, "constructor application argument")
            }
            marker.done(ScillaElementType.BUILTIN_EXPRESSION)
        }

        /**
         * Literal:
         *   | CID INT
         *   | HEX
         *   | STRING
         *   | 'Emp' MapKey MapValue
         */
        private fun tryParseIntLiteral() : Boolean {
            val mark = builder.mark()
            if (builder.tokenType == ScillaTokenType.CID) {
                assertAdvance(ScillaTokenType.CID)
                if (builder.tokenType == ScillaTokenType.INT) {
                    assertAdvance(ScillaTokenType.INT)
                    mark.done(ScillaElementType.LITERAL_EXPRESSION)
                    return true
                }
            }
            mark.rollbackTo()
            return false
        }

        private fun tryParseLiteral(): Boolean {
            if (tryParseIntLiteral())
                return true

            val mark = builder.mark()
            when (builder.tokenType) {
                //case ScillaTokenType.CID is already processed by tryParseIntLiteral()
                ScillaTokenType.HEX, ScillaTokenType.STRING -> {
                    advance()
                    mark.done(ScillaElementType.LITERAL_EXPRESSION)
                    return true
                }
                ScillaTokenType.EMP -> {
                    assertAdvance(ScillaTokenType.EMP)
                    parseMapKey()
                    parseMapValue()
                    mark.done(ScillaElementType.LITERAL_EXPRESSION)
                    return true
                }
                else -> {
                    mark.rollbackTo()
                    return false
                }
            }
        }
        /**
         * MapKey:
         *   | SCid
         *   | '(' SCid ')'
         *   | AddressType
         *   | '(' AddressType ')'
         */
        private fun parseMapKey() {
            val mark = builder.mark()
            if (builder.tokenType == ScillaTokenType.LPAREN) {
                assertAdvance(ScillaTokenType.LPAREN)
                parseSCidTypeOrAddressType(allowTypeArgs = false)
                expectAdvance(ScillaTokenType.RPAREN, "')'")
            }
            else
                parseSCidTypeOrAddressType(allowTypeArgs = false)

            mark.done(ScillaElementType.MAP_KEY)
        }

        /**
         * MapValue:
         *   | SCid
         *   | 'Map' MapKey MapValue
         *   | '(' MapValueAllowTypeArgs ')'
         *   | AddressType
         *
         * MapValueAllowTypeArgs:
         *   | SCid MapValueArgNonEmptyList // We only allow type args when the type is surrounded by parentheses
         *   | MapValue
         *
         * MapValueArg:
         *   | SCid
         *   | 'Map' MapKey MapValue
         *   | '(' MapValueAllowTypeArgs ')'
         */
        private fun parseMapValue(allowTypeArgs: Boolean = false) {
            val mark = builder.mark()

            when (builder.tokenType) {
                ScillaTokenType.MAP -> {
                    assertAdvance(ScillaTokenType.MAP)
                    parseMapKey()
                    parseMapValue(allowTypeArgs = false)
                }
                ScillaTokenType.LPAREN -> {
                    assertAdvance(ScillaTokenType.LPAREN)
                    parseMapValue(true)
                    expectAdvance(ScillaTokenType.RPAREN, "')'")
                }
                else -> parseSCidTypeOrAddressType(allowTypeArgs)
            }

            mark.done(ScillaElementType.MAP_VALUE)
        }

        /**
         *   | SCid MapValueArgNonEmptyList?
         *   | AddressType
         */
        private fun parseSCidTypeOrAddressType(allowTypeArgs: Boolean) {
            if (builder.tokenType == ScillaTokenType.HEX) {
                parseSCidAndMapValueArgList(allowTypeArgs)
            }
            else if (builder.tokenType in ScillaTokenType.IDENTS && builder.lookAhead(1) == ScillaTokenType.WITH) {
                val mark = builder.mark()
                parseAddressType()
                mark.done(ScillaElementType.ADDRESS_TYPE)
            } else {
                parseSCidAndMapValueArgList(allowTypeArgs)
            }
        }

        /**
         *   | SCid MapValueArgNonEmptyList?
         */
        private fun parseSCidAndMapValueArgList(allowTypeArgs: Boolean) {
            parseSidOrSCid(true, "type")
            if (allowTypeArgs) {
                parseLoop("map argument", null, null, listOf(ScillaTokenType.RPAREN)) {
                    //We should parse 'MapValueArg' here, but it's identical to 'MapValue' except allowing AddressType
                    parseMapValue(allowTypeArgs = false)
                }
            }
        }

        /**
        * Expression:
        *   | Sid
        *   | Literal
        *   | LetExpression
        *   | FunctionExpression
        *   | Application
        *   | BuiltinCall
        *   | MessageConstruction
        *   | DataConstructorApplication
        *   | MatchExpression
        *   | TypeFunctionExpression
        *   | TypeApplication
        */
        private fun parseExpression() {
            /* Expression:
            *   | Sid
            *   | Literal => | CID INT | HEX | STRING | 'Emp' MapKey MapValue
            *   | LetExpression => 'let' ID TypeAnnotation? '=' Expression 'in' Expression
            *   | FunctionExpression => 'fun' '(' IdWithType ')' '=>' Expression
            *   | Application  => Sid SidList
            *   | BuiltinCall  => 'builtin' ID ('{' TypeArgList '}')? BuiltinArgs
            *   | MessageConstruction => '{' MsgEntryList '}'
            *   | DataConstructorApplication => SCid  ('{' TypeArgList '}')? SidList?
            *   | MatchExpression => 'match' ...
            *   | TypeFunctionExpression => 'tfun' TID '=>' Expression
            *   | TypeApplication => '@' Sid TypeArgList
            */

            if (tryParseIntLiteral()) return
            if (tryParseConstructorApplication()) return
            if (tryParseLiteral()) return

            when (builder.tokenType) {
                ScillaTokenType.LET -> parseLetExpression(false)
                ScillaTokenType.FUN -> parseFunExpression()
                ScillaTokenType.BUILTIN -> parseBuiltinCallExpression()
                ScillaTokenType.LBRACE -> parseMessageConstruction()
                ScillaTokenType.MATCH -> parseMatchExpression()
                ScillaTokenType.TFUN -> parseTypeFunctionExpression()
                ScillaTokenType.AT -> parseTypeApplication()
                ScillaTokenType.CID, ScillaTokenType.ID, ScillaTokenType.SPID  -> parseSidAndApplication()
            }
        }

        /**
         * MatchExpression:
         *   | 'match' Sid 'with' ExpressionPatternMatchingClauseList 'end'
         */
        private fun parseMatchExpression() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.MATCH)
            parseSidOrSCid(false, "value")
            expectAdvance(ScillaTokenType.WITH, "'with'")

            while (tryParseExpressionPatternMatchingClause(false)) {
                continue
            }
            expectAdvance(ScillaTokenType.END, "'end'")
            mark.done(ScillaElementType.MATCH_EXPRESSION)
        }

        /**
         * ExpressionPatternMatchingClause:
         *   | '|' Pattern '=>' Expression
         *
         * StatementPatternMatchingClause:
         *   | '|' Pattern '=>' StatementList
         */
        private fun tryParseExpressionPatternMatchingClause(isStatement: Boolean): Boolean {
            if (builder.tokenType != ScillaTokenType.BAR)
                return false

            val mark = builder.mark()
            assertAdvance(ScillaTokenType.BAR)
            if (!tryParsePattern(false)) {
                builder.error("Expected pattern ('_', ADT constructor or variable)")
            }
            if (expectAdvance(ScillaTokenType.ARROW, "'=>'")) {
                if (isStatement) {
                    parseStatementList()
                }
                else
                    parseExpression()
            }
            mark.done(ScillaElementType.PATTERN_MATCH_CLAUSE)
            return true
        }

        /** Pattern:
         *   | '_'
         *   | ID
         *   | SCid ArgPatternList
         *
         * ArgPattern:
         *   | '_'
         *   | ID
         *   | SCid
         *   | '(' Pattern ')'
         */
        private fun tryParsePattern(allowParens: Boolean): Boolean {
            if (detectSCid()) {
                val mark = builder.mark()
                parseSidOrSCid(true, "ADT constructor")
                while (tryParsePattern(true)) {
                    continue
                }

                mark.done(ScillaElementType.CONSTRUCTOR_PATTERN)
                return true
            }
            when (builder.tokenType) {
                ScillaTokenType.UNDERSCORE -> {
                    val mark = builder.mark()
                    assertAdvance(ScillaTokenType.UNDERSCORE)
                    mark.done(ScillaElementType.WILDCARD_PATTERN)
                    return true
                }
                ScillaTokenType.ID -> {
                    val mark = builder.mark()
                    assertAdvance(ScillaTokenType.ID)
                    mark.done(ScillaElementType.BINDER_PATTERN)
                    return true
                }
                ScillaTokenType.LPAREN -> {
                    val mark = builder.mark()
                    if (allowParens)
                        assertAdvance(ScillaTokenType.LPAREN)
                    else
                        errorAdvance("pattern without parentheses")

                    tryParsePattern(false)
                    expectAdvance(ScillaTokenType.RPAREN, "')'")
                    mark.done(ScillaElementType.PAREN_PATTERN)
                    return true
                }
                else -> return false
            }
        }

        /**
         * MessageConstruction:
         *   | '{' MsgEntryList? '}'
         *
         * MsgEntryList:
         *   | MsgEntry
         *   | MsgEntry MsgEntryList
         *
         * MsgEntry:
         *   | Sid ':' Literal
         *   | Sid ':' Sid
         */
        private fun parseMessageConstruction() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.LBRACE)
            parseLoop("message entry", ScillaTokenType.SEMICOLON, "';'", listOf(ScillaTokenType.RBRACE)) {
                val msgMark = builder.mark()
                parseSidOrSCid(false, "message field")
                expectAdvance(ScillaTokenType.COLON, "':'")
				
				val fieldValueMark = builder.mark()
                if (!tryParseLiteral())
                    parseSidOrSCid(false, "literal or value $identBeginningWithLowerCaseLetter")
				fieldValueMark.done(ScillaElementType.MESSAGE_ENTRY_VALUE)
				
                msgMark.done(ScillaElementType.MESSAGE_ENTRY)
            }
            expectAdvance(ScillaTokenType.RBRACE, "'}'")
            mark.done(ScillaElementType.MESSAGE_EXPRESSION)
        }

        /**
         * DataConstructorApplication:
         *   | SCid  ('{' TypeArgList '}')? SidList?
         */
        private fun tryParseConstructorApplication(): Boolean {
            if (!detectSCid())
                return false
            val mark = builder.mark()
            parseSidOrSCid(true, "constructor")

            if (builder.tokenType == ScillaTokenType.LBRACE) {
                assertAdvance(ScillaTokenType.LBRACE)
                parseLoop("type argument",null, null, listOf(ScillaTokenType.RBRACE), ) {
                    tryParseTypeArg()
                }
                expectAdvance(ScillaTokenType.RBRACE, "'}'")
            }
            while (detectSid()) {
                parseSidOrSCid(false, "constructor application argument")
            }

            mark.done(ScillaElementType.CONSTR_EXPRESSION)
            return true
        }

        /**
         *   | Sid
         *   | Application  => Sid SidList
         */
        private fun parseSidAndApplication() {
            val mark = builder.mark()
            parseSidOrSCid(false, "function")

            if (detectSid()) {
                while (detectSid()) {
                    parseSidOrSCid(false, "application argument")
                }
                mark.done(ScillaElementType.APP_EXPRESSION)
            }
            else
                mark.drop()
        }

        /**
         * TypeApplication:
         *   | '@' Sid TypeArgList
         */
        private fun parseTypeApplication() {
            val mark = builder.mark()
            assertAdvance(ScillaTokenType.AT)
            parseSidOrSCid(false, "type function")
            while (builder.tokenType != null) {
                if (!tryParseTypeArg())
                    break
            }
            mark.done(ScillaElementType.TYPE_APP_EXPRESSION)
        }

        private fun parseLoop(itemName: String, separator: ScillaTokenType?, separatorName: String?, stopper: List<ScillaTokenType>, itemParser: () -> Unit) {
			assert(separator == null && separatorName == null || separator != null && separatorName != null)
			
            while (builder.tokenType != null && builder.tokenType !in stopper) {
                val offsetBeforeBody = builder.currentOffset
                itemParser()

                if (builder.currentOffset == offsetBeforeBody) {
                    errorAdvance(itemName)
                    continue
                }

                if (separator == null)
                    continue
                else if (builder.tokenType == separator) {
                    assertAdvance(separator)
                    continue
                }
                else if (builder.tokenType in stopper) {
                    break
                }
                else {
                    builder.error("Expected $separatorName")
                    continue
                }
            }
        }

        private fun advance(): IElementType? {
            val result = builder.tokenType
            builder.advanceLexer()
            return result
        }

        private fun errorAdvance(expectedName: String) {
            val mark = builder.mark()
            advance()
            mark.error("Expected $expectedName")
        }

        private fun expectAdvance(expectedTt: ScillaTokenType, expectedName: String): Boolean {
            val suffix = when(expectedTt) {
                ScillaTokenType.CID -> " $identCapitalized"
                ScillaTokenType.ID -> " $identBeginningWithLowerCaseLetter"
                ScillaTokenType.SPID -> " $identBeginningWithUnderscore"
                ScillaTokenType.TID -> " $identBeginningWithQuote"
                else -> ""
            }
            if (builder.tokenType == expectedTt) {
                advance()
                return true
            }
            else if (  expectedTt in ScillaTokenType.IDENTS && builder.tokenType in ScillaTokenType.IDENTS
                    || expectedTt in ScillaTokenType.ARROWS && builder.tokenType in ScillaTokenType.ARROWS) {
                val mark = builder.mark()
                advance()
                mark.error("Expected $expectedName$suffix")
                return true
            }
            else {
                builder.error("Expected $expectedName$suffix")
                return false
            }
        }

        private fun assertAdvance(tt: IElementType) {
            assert(builder.tokenType == tt)
            advance()
        }
    }
}

class ScillaParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer {
        return ScillaLexer()
    }

    override fun createParser(project: Project?): PsiParser {
        return ScillaParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return ScillaElementType.SCILLA_CONTRACT_STUB_FILE
    }

    override fun getCommentTokens(): TokenSet {
        return ScillaTokenType.COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return ScillaTokenType.STRINGS
    }

    override fun createElement(node: ASTNode): PsiElement {
        return ScillaElementType.createElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return ScillaFile(viewProvider)
    }
}

