package com.zloyrobot.scilla.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.zloyrobot.scilla.lang.ScillaTokenType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
%%
%{
  public _ScillaLexer() {
    this((java.io.Reader)null);
  }

  private IElementType parseComment() {
      int level = 1;
      while (zzMarkedPos < zzBuffer.length() - 1) {
          char c = zzBuffer.charAt(zzMarkedPos);
          char nc = zzBuffer.charAt(zzMarkedPos + 1);
          if (c == '*' && nc == ')') {
              ++zzMarkedPos;
              --level;
              if (level == 0) {
                  ++zzMarkedPos;
                  return ScillaTokenType.Companion.getCOMMENT();
              }
          }
          else if (c == '(' && nc == '*') {
              ++zzMarkedPos;
              ++level;
          }
          ++zzMarkedPos;
      }
      return ScillaTokenType.Companion.getCOMMENT();
  }
%}

%public
%class _ScillaLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode


DEC_DIGIT = [0-9]
DEC_INTEGER = "-"? {DEC_DIGIT}+
HEX_DIGIT = [a-fA-F0-9]
HEX_INTEGER = "0x" ({HEX_DIGIT})+

LINE_WS = [\ \t\f]+
EOL = "\r"|"\n"|"\r\n"
WHITE_SPACE=({LINE_WS}|{EOL})+

ALPHA_NUM = [a-zA-Z0-9_]
ID = [a-z] {ALPHA_NUM}*
SPID = "_" {ALPHA_NUM}+
CID =   [A-Z] {ALPHA_NUM}*
TID =  "'"{ALPHA_NUM}* //allow non-capitalized type parameter in our lexer, because why not?

INTTY = "Int32" | "Int64" | "Int128" | "Int256" | "Uint32" | "Uint64" | "Uint128" | "Uint256"

STRING=(\"([^\"\r\n\\]|\\.)*\")
%%

"(*"             { return parseComment(); }
{WHITE_SPACE}    { return WHITE_SPACE; }
{STRING}         { return ScillaTokenType.Companion.getSTRING(); }
{HEX_INTEGER}    { return ScillaTokenType.Companion.getHEX(); }
{DEC_INTEGER}    { return ScillaTokenType.Companion.getINT(); }
{INTTY}          { return ScillaTokenType.Companion.getCID(); }
"forall"         { return ScillaTokenType.Companion.getFORALL(); }
"builtin"        { return ScillaTokenType.Companion.getBUILTIN(); }
"library"        { return ScillaTokenType.Companion.getLIBRARY(); }
"import"         { return ScillaTokenType.Companion.getIMPORT(); }
"let"            { return ScillaTokenType.Companion.getLET(); }
"in"             { return ScillaTokenType.Companion.getIN(); }
"match"          { return ScillaTokenType.Companion.getMATCH();}
"with"           { return ScillaTokenType.Companion.getWITH();}
"end"            { return ScillaTokenType.Companion.getEND(); }
"fun"            { return ScillaTokenType.Companion.getFUN(); }
"tfun"           { return ScillaTokenType.Companion.getTFUN(); }
"contract"       { return ScillaTokenType.Companion.getCONTRACT(); }
"transition"     { return ScillaTokenType.Companion.getTRANSITION(); }
"send"           { return ScillaTokenType.Companion.getSEND(); }
"event"          { return ScillaTokenType.Companion.getEVENT(); }
"field"          { return ScillaTokenType.Companion.getFIELD(); }
"accept"         { return ScillaTokenType.Companion.getACCEPT(); }
"exists"         { return ScillaTokenType.Companion.getEXISTS(); }
"delete"         { return ScillaTokenType.Companion.getDELETE(); }
"Emp"            { return ScillaTokenType.Companion.getEMP(); }
"Map"            { return ScillaTokenType.Companion.getMAP(); }
"scilla_version" { return ScillaTokenType.Companion.getSCILLA_VERSION(); }
"type"           { return ScillaTokenType.Companion.getTYPE(); }
"of"             { return ScillaTokenType.Companion.getOF(); }
"try"            { return ScillaTokenType.Companion.getTRY(); }
"catch"          { return ScillaTokenType.Companion.getCATCH(); }
"as"             { return ScillaTokenType.Companion.getAS(); }
"procedure"      { return ScillaTokenType.Companion.getPROCEDURE(); }
"throw"          { return ScillaTokenType.Companion.getTHROW(); }


{ID}             { return ScillaTokenType.Companion.getID(); }
{CID}            { return ScillaTokenType.Companion.getCID(); }
{TID}            { return ScillaTokenType.Companion.getTID(); }
{SPID}           { return ScillaTokenType.Companion.getSPID(); }

";"           { return ScillaTokenType.Companion.getSEMICOLON(); }
":"           { return ScillaTokenType.Companion.getCOLON(); }
"."           { return ScillaTokenType.Companion.getDOT(); }
"|"           { return ScillaTokenType.Companion.getBAR(); }
"["           { return ScillaTokenType.Companion.getLBRACKET(); }
"]"           { return ScillaTokenType.Companion.getRBRACKET(); }
"("           { return ScillaTokenType.Companion.getLPAREN(); }
")"           { return ScillaTokenType.Companion.getRPAREN(); }
"{"           { return ScillaTokenType.Companion.getLBRACE(); }
"}"           { return ScillaTokenType.Companion.getRBRACE(); }
","           { return ScillaTokenType.Companion.getCOMMA(); }
"=>"          { return ScillaTokenType.Companion.getARROW(); }
"->"          { return ScillaTokenType.Companion.getTARROW(); }
"="           { return ScillaTokenType.Companion.getEQ(); }
"&"           { return ScillaTokenType.Companion.getAMP(); }
"<-"          { return ScillaTokenType.Companion.getFETCH(); }
":="          { return ScillaTokenType.Companion.getASSIGN(); }
"@"           { return ScillaTokenType.Companion.getAT(); }
"_"           { return ScillaTokenType.Companion.getUNDERSCORE(); }

[^] { return BAD_CHARACTER; }
