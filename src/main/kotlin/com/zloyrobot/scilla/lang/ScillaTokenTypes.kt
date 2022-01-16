package com.zloyrobot.scilla.lang

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.annotations.NonNls


class ScillaTokenType(@NonNls debugName: String) : IElementType(debugName, ScillaLanguage) {
    override fun toString(): String {
        return "ScillaTokenType." + super.toString()
    }

    companion object {
        val NEW_LINE = ScillaTokenType("NEW_LINE")
        val COMMENT = ScillaTokenType("COMMENT")
        val STRING = ScillaTokenType("STRING")
        val INT = ScillaTokenType("INT")
        val HEX = ScillaTokenType("HEX")
        val CID = ScillaTokenType("CID")
        val TID = ScillaTokenType("TID")
        val ID = ScillaTokenType("ID")
        val SPID = ScillaTokenType("SPID")
        val FORALL = ScillaTokenType("FORALL")
        val BUILTIN = ScillaTokenType("BUILTIN")
        val LIBRARY = ScillaTokenType("LIBRARY")
        val IMPORT = ScillaTokenType("IMPORT")
        val LET = ScillaTokenType("LET")
        val IN = ScillaTokenType("IN")
        val MATCH = ScillaTokenType("MATCH")
        val WITH = ScillaTokenType("WITH")
        val END = ScillaTokenType("END")
        val FUN = ScillaTokenType("FUN")
        val TFUN = ScillaTokenType("TFUN")
        val CONTRACT = ScillaTokenType("CONTRACT")
        val TRANSITION = ScillaTokenType("TRANSITION")
        val SEND = ScillaTokenType("SEND")
        val EVENT = ScillaTokenType("EVENT")
        val FIELD = ScillaTokenType("FIELD")
        val ACCEPT = ScillaTokenType("ACCEPT")
        val EXISTS = ScillaTokenType("EXISTS")
        val DELETE = ScillaTokenType("DELETE")
        val EMP = ScillaTokenType("EMP")
        val MAP = ScillaTokenType("MAP")
        val SCILLA_VERSION = ScillaTokenType("SCILLA_VERSION")
        val TYPE = ScillaTokenType("TYPE")
        val OF = ScillaTokenType("OF")
        val TRY = ScillaTokenType("TRY")
        val CATCH = ScillaTokenType("CATCH")
        val AS = ScillaTokenType("AS")
        val PROCEDURE = ScillaTokenType("PROCEDURE")
        val THROW = ScillaTokenType("THROW")
        val SEMICOLON = ScillaTokenType("SEMICOLON")
        val COLON = ScillaTokenType("COLON")
        val DOT = ScillaTokenType("DOT")
        val BAR = ScillaTokenType("BAR")
        val LBRACKET = ScillaTokenType("LBRACKET")
        val RBRACKET = ScillaTokenType("RBRACKET")
        val LPAREN = ScillaTokenType("LPAREN")
        val RPAREN = ScillaTokenType("RPAREN")
        val LBRACE = ScillaTokenType("LBRACE")
        val RBRACE = ScillaTokenType("RBRACE")
        val COMMA = ScillaTokenType("COMMA")
        val ARROW = ScillaTokenType("ARROW")
        val TARROW = ScillaTokenType("TARROW")
        val EQ = ScillaTokenType("EQ")
        val AMP = ScillaTokenType("AND")
        val FETCH = ScillaTokenType("FETCH")
        val ASSIGN = ScillaTokenType("ASSIGN")
        val AT = ScillaTokenType("AT")
        val UNDERSCORE = ScillaTokenType("UNDERSCORE")


        val COMMENTS = TokenSet.create(COMMENT)
        val STRINGS = TokenSet.create(STRING)
        val IDENTS = TokenSet.create(TID, SPID, CID, ID)
        val ARROWS = TokenSet.create(ARROW, TARROW)
        val KEYWORDS = TokenSet.create(FORALL, BUILTIN, LIBRARY, IMPORT, LET, IN, MATCH, WITH, END, FUN,
                TFUN, CONTRACT, TRANSITION, SEND, EVENT, FIELD, ACCEPT, EXISTS, DELETE, EMP, MAP,
                SCILLA_VERSION, TYPE, OF, TRY, CATCH, AS, PROCEDURE, THROW)
        
    }

}

