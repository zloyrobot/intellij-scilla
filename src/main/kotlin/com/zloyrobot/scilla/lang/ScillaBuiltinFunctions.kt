package com.zloyrobot.scilla.lang

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.light.LightElement
import com.zloyrobot.scilla.lang.ScillaPrimitiveType.*
import com.zloyrobot.scilla.lang.ScillaPrimitiveType.Companion.INTEGER_TYPES
import com.zloyrobot.scilla.lang.ScillaPrimitiveType.Companion.UINTEGER_TYPES
import com.zloyrobot.scilla.lang.ScillaSimpleAlgebraicType.Companion.BOOL
import com.zloyrobot.scilla.lang.ScillaSimpleAlgebraicType.Companion.LIST
import com.zloyrobot.scilla.lang.ScillaSimpleAlgebraicType.Companion.NAT
import com.zloyrobot.scilla.lang.ScillaSimpleAlgebraicType.Companion.OPTION
import com.zloyrobot.scilla.lang.ScillaSimpleAlgebraicType.Companion.PAIR
import com.zloyrobot.scilla.lang.ScillaTypeVarType.Companion._K
import com.zloyrobot.scilla.lang.ScillaTypeVarType.Companion._V

class ScillaFunctionSignature(val typeParams: List<ScillaTypeVarType>, val returnType: ScillaType, vararg val paramTypes: ScillaType) {
	constructor(returnType: ScillaType, vararg paramTypes: ScillaType) : this(emptyList(), returnType, *paramTypes)
}

enum class ScillaBuiltinFunction(val functionName: String, val functionSignatures: List<ScillaFunctionSignature>) {
	EQ("eq", (INTEGER_TYPES + STRING + BNUM).map { ScillaFunctionSignature(BOOL, it, it) }),

	//Integer operations
	LT("lt", INTEGER_TYPES.map { ScillaFunctionSignature(BOOL, it, it) }),
	ADD("add", INTEGER_TYPES.map { ScillaFunctionSignature(it, it, it) }),
	SUB("sub", INTEGER_TYPES.map { ScillaFunctionSignature(it, it, it) }),
	MUL("mul", INTEGER_TYPES.map { ScillaFunctionSignature(it, it, it) }),
	DIV("div", INTEGER_TYPES.map { ScillaFunctionSignature(it, it, it) }),
	REM("rem", INTEGER_TYPES.map { ScillaFunctionSignature(it, it, it) }),
	POW("pow", INTEGER_TYPES.map { ScillaFunctionSignature(it, it, it) }),
	SQRT("sqrt", INTEGER_TYPES.map { ScillaFunctionSignature(it, it) }),
	TO_INT32("to_int32", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(INT32)), it) }),
	TO_INT64("to_int64", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(INT64)), it) }),
	TO_INT128("to_int128", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(INT128)), it) }),
	TO_INT256("to_int128", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(INT256)), it) }),
	TO_UINT32("to_uint32", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(UINT32)), it) }),
	TO_UINT64("to_uint64", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(UINT64)), it) }),
	TO_UINT128("to_uint128", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(UINT128)), it) }),
	TO_UINT256("to_uint256", (INTEGER_TYPES + STRING).map { ScillaFunctionSignature(ScillaPolyTypeApplication(OPTION, listOf(UINT256)), it) }),
	TO_NAT("to_nat", ScillaFunctionSignature(NAT, UINT32)),

	//String operations
	TO_STRING("to_string", (INTEGER_TYPES + BYSTR).map { ScillaFunctionSignature(STRING, it) }),
	CONCAT("concat", ScillaFunctionSignature(STRING, STRING, STRING)),
	SUBSTR("substr", ScillaFunctionSignature(STRING, STRING, UINT32, UINT32)),
	STRLEN("strlen", ScillaFunctionSignature(UINT32, STRING)),
	STRREV("strrev", ScillaFunctionSignature(STRING, STRING)),
	TO_ASCII("to_ascii", ScillaFunctionSignature(STRING, BYSTR)),

	//Block number operations
	BADD("badd", UINTEGER_TYPES.map { ScillaFunctionSignature(BNUM, BNUM, it) }),
	BLT("blt", ScillaFunctionSignature(BOOL, BNUM, BNUM)),
	BSUB("bsub", ScillaFunctionSignature(INT256, BNUM, BNUM)),

	//Map
	PUT("put", ScillaFunctionSignature(listOf(_K, _V), ScillaMapType(_K, _V), ScillaMapType(_K, _V), _K, _V)),
	GET("get", ScillaFunctionSignature(listOf(_K, _V), _V, ScillaMapType(_K, _V), _K)),
	CONTAINS("contains", ScillaFunctionSignature(listOf(_K, _V), BOOL, ScillaMapType(_K, _V), _K)),
	REMOVE("remove", ScillaFunctionSignature(listOf(_K, _V), ScillaMapType(_K, _V), ScillaMapType(_K, _V), _K)),
	SIZE("size", ScillaFunctionSignature(listOf(_K, _V), UINT32, ScillaMapType(_K, _V))),
	TO_LIST("to_list", ScillaFunctionSignature(listOf(_K, _V),
		ScillaPolyTypeApplication(LIST, listOf(ScillaPolyTypeApplication(PAIR, listOf(_K, _V)))),
		ScillaMapType(_K, _V)));
	

	constructor(functionName: String, functionSignature: ScillaFunctionSignature) : this(functionName, listOf(functionSignature))
	
	companion object {
		fun processBuiltinFunctions(processor: (function: ScillaBuiltinFunction) -> Boolean): Boolean {
			for (type in ScillaBuiltinFunction.values()) {
				if (processor(type))
					return true
			}
			return false
		}
	}
}

class ScillaBuiltinFunctionElement(val function: ScillaBuiltinFunction, element: PsiElement)
	: LightElement(element.manager, ScillaLanguage), ScillaNamedElement {

	override fun getName(): String = function.functionName
	override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
	override fun getNameIdentifier(): PsiElement? = null

	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ScillaBuiltinFunctionElement && another.function == function
	}

	override fun toString(): String = javaClass.simpleName + "(" + name + ")"
}
