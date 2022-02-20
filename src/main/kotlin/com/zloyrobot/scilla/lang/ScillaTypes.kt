package com.zloyrobot.scilla.lang

import java.util.concurrent.atomic.AtomicInteger


interface ScillaType {
	val presentation: String

	val presentationInParentsIfNeeded: String get() {
		return when (this) {
			is ScillaPolyAlgebraicType -> presentation
			is ScillaPolyTypeApplication,
			is ScillaFunType,
			is ScillaMapType,
			is ScillaTypeFunType -> "(${presentation})"
			else -> presentation
		}
	}
}

interface ScillaNamedType: ScillaType {
	val typeName: String

	override val presentation: String get() = typeName
}

object ScillaUnknownType : ScillaNamedType {
	override val typeName: String = "?"

	override fun toString(): String = presentation
}


class ScillaTypeVarType private constructor(override val typeName: String, val counter: Int) : ScillaNamedType {
	constructor(name: String) : this(name, 0)

	fun increment(): ScillaTypeVarType {
		return ScillaTypeVarType(typeName, globalCounter.incrementAndGet())
	}

	override val presentation: String get() {
		return if (counter == 0) typeName
		else "$typeName$counter"
	}

	override fun equals(other: Any?): Boolean {
		return other is ScillaTypeVarType && typeName == other.typeName && counter == other.counter
	}
	override fun hashCode(): Int = typeName.hashCode()

	override fun toString(): String = presentation

	companion object {
		var globalCounter = AtomicInteger(1)

		val _A = ScillaTypeVarType("'A")
		val _B = ScillaTypeVarType("'A")
		val _K = ScillaTypeVarType("'K")
		val _V = ScillaTypeVarType("'V")
	}
}


class ScillaAddressType(val fields : Map<String, ScillaType>? = null, val element: ScillaAddressTypeElement? = null) : ScillaType {
	override val presentation: String get() {
		val suffix = if (fields != null) " with...end" else ""
		return "ByStr20$suffix"
	} 
	
	override fun equals(other: Any?): Boolean {
		return other is ScillaAddressType
	}

	override fun hashCode(): Int {
		return 67861
	}

}

data class ScillaByStrType(val size: Int) : ScillaNamedType {
	override val typeName: String get() = "ByStr$size"

	companion object {
		val BYSTR20 = ScillaByStrType(20)
		val BUSTR32 = ScillaByStrType(32)
		val BYSTR = ScillaPrimitiveType.BYSTR
	}

	override fun toString(): String = presentation
	
	override fun equals(other: Any?): Boolean {
		return other is ScillaByStrType && other.size == size
	}

	override fun hashCode(): Int {
		return size
	}


}

enum class ScillaPrimitiveType(override val typeName: String) : ScillaNamedType {
	INT32("Int32"),
	INT64("Int64"),
	INT128("Int128"),
	INT256("Int256"),
	UINT32("Uint32"),
	UINT64("Uint64"),
	UINT128("Uint128"),
	UINT256("Uint256"),
	STRING("String"),
	BNUM("BNum"),
	MESSAGE("Message"),
	EVENT("Event"),
	BYSTR("ByStr");

	companion object {
		val INTEGER_TYPES = listOf(INT32, INT64, INT128, INT256, UINT32, UINT64, UINT128, UINT256)
		val UINTEGER_TYPES = listOf(UINT32, UINT64, UINT128, UINT256)

		fun processBuiltinTypes(processor: (type: ScillaPrimitiveType) -> Boolean): Boolean {
			for (type in ScillaPrimitiveType.values()) {
				if (processor(type))
					return true
			}
			return false
		}

		fun lookupType(name: String): ScillaPrimitiveType? {
			return ScillaPrimitiveType.values().find { it.typeName == name }
		}
	}

	override fun toString(): String = presentation
}

class ScillaTypeConstructor(val name: String, vararg var types: ScillaType) {

	fun substitute(typeVar: ScillaTypeVarType, value: ScillaType): ScillaTypeConstructor {
		val substitutor = ScillaTypeSubstitution(typeVar, value)
		return ScillaTypeConstructor(name, *types.map { substitutor.substitute(it) }.toTypedArray())
	}
}

interface ScillaAlgebraicType : ScillaNamedType {
	val constructors: Array<out ScillaTypeConstructor>
}

class ScillaSimpleAlgebraicType(override val typeName: String, override vararg val constructors: ScillaTypeConstructor)
	: ScillaAlgebraicType {

	companion object {
		val BOOL =
			ScillaSimpleAlgebraicType("Bool",
				ScillaTypeConstructor("True"),
				ScillaTypeConstructor("False"))

		val NAT =
			ScillaSimpleAlgebraicType("Nat",
				ScillaTypeConstructor("Zero"),
				ScillaTypeConstructor("Succ"))

		val OPTION =
			ScillaPolyAlgebraicType(listOf(ScillaTypeVarType._A),
				ScillaSimpleAlgebraicType("Option",
					ScillaTypeConstructor("Some", ScillaTypeVarType._A),
					ScillaTypeConstructor("None")))

		val PAIR =
			ScillaPolyAlgebraicType(listOf(ScillaTypeVarType._A, ScillaTypeVarType._B),
				ScillaSimpleAlgebraicType("Pair",
					ScillaTypeConstructor("Pair", ScillaTypeVarType._A, ScillaTypeVarType._B)))

		private val CONS_CONSTRUCTOR_INCOMPLETE = ScillaTypeConstructor("Cons")
		val LIST: ScillaPolyAlgebraicType =
			ScillaPolyAlgebraicType(listOf(ScillaTypeVarType._A),
				ScillaSimpleAlgebraicType("List",
					CONS_CONSTRUCTOR_INCOMPLETE,
					ScillaTypeConstructor("Nil")))
	
		private val TYPES = listOf(BOOL, OPTION, LIST, PAIR, PAIR, NAT)
	
		
		init {
			CONS_CONSTRUCTOR_INCOMPLETE.types = arrayOf(
				ScillaTypeVarType._A,
				ScillaPolyTypeApplication(LIST, listOf(ScillaTypeVarType._A)))
		}

		fun processBuiltinTypes(processor: (type: ScillaAlgebraicType) -> Boolean): Boolean {
			for (type in TYPES) {
				if (processor(type))
					return true
			}
			return false
		}

		fun processBuiltinTypeConstructors(processor: (type: ScillaAlgebraicType, constructor: ScillaTypeConstructor) -> Boolean): Boolean {
			for (type in TYPES) {
				for (constructor in type.constructors)
					if (processor(type, constructor))
						return true
			}
			return false
		}
	}

	override fun toString(): String = presentation
	
	override fun equals(other: Any?): Boolean {
		return other is ScillaSimpleAlgebraicType && typeName == other.typeName && constructors.contentEquals(other.constructors)
	}

	override fun hashCode(): Int {
		return 59 *typeName.hashCode() + constructors.contentHashCode()
	}
}

class ScillaMapType(val keyType: ScillaType, val valueType: ScillaType) : ScillaType {
	override val presentation: String get() {
		return "Map ${keyType.presentationInParentsIfNeeded} ${valueType.presentationInParentsIfNeeded}"
	}

	override fun toString(): String = presentation

	override fun equals(other: Any?): Boolean {
		return other is ScillaMapType && other.keyType == keyType && other.valueType == valueType
	}

	override fun hashCode(): Int {
		return 31 * keyType.hashCode() + valueType.hashCode() 
	}
}

class ScillaFunType(val paramType: ScillaType, val resultType: ScillaType) : ScillaType {
	override val presentation: String get() {
		return "${paramType.presentationInParentsIfNeeded} -> ${resultType.presentation}"
	}

	override fun toString(): String = presentation

	override fun equals(other: Any?): Boolean {
		return other is ScillaFunType && other.paramType == paramType && other.resultType == resultType
	}

	override fun hashCode(): Int {
		return 19 * paramType.hashCode() + resultType.hashCode()
	}
}

open class ScillaTypeFunType(val typeParameter: ScillaTypeVarType, open val body: ScillaType) : ScillaType {
	override val presentation: String get() = "forall ${typeParameter.presentation}. ${body.presentation}"

	override fun toString(): String = presentation

	override fun equals(other: Any?): Boolean {
		return other is ScillaTypeFunType && other.typeParameter == typeParameter && other.body == body
	}

	override fun hashCode(): Int {
		return 29 * typeParameter.hashCode() + body.hashCode()
	}
}

class ScillaPolyAlgebraicType(val typeParameters: List<ScillaTypeVarType>, val body: ScillaAlgebraicType) : ScillaAlgebraicType {
	override val typeName: String get() = body.typeName

	override val presentation: String get() {
		return typeParameters.joinToString(" ", "", body.presentation) { "forall ${it.typeName}. "}
	}

	override val constructors: Array<out ScillaTypeConstructor> get() = body.constructors

	override fun equals(other: Any?): Boolean {
		return other is ScillaPolyAlgebraicType && typeParameters == other.typeParameters && body == other.body
	}

	override fun hashCode(): Int {
		return 59 * typeParameters.hashCode() + body.hashCode()
	}
}

class ScillaPolyTypeApplication(val origin: ScillaPolyAlgebraicType, val typeArguments: List<ScillaType>)
	: ScillaAlgebraicType {

	override val typeName: String get() = origin.typeName
	override val presentation: String get() {
		val arguments = typeArguments + origin.typeParameters.drop(typeArguments.size)
		return arguments.joinToString(" ", "$typeName ") {
			if (it is ScillaPolyAlgebraicType || it is ScillaPolyTypeApplication)
				"(${it.presentation})"
			else
				it.presentation
		}
	}

	override val constructors: Array<out ScillaTypeConstructor> get() {
		return origin.constructors.map {
			val substitutions = origin.typeParameters.zip(typeArguments)
			substitutions.fold(it) { constructor, substitution ->
				constructor.substitute(substitution.first, substitution.second)
			}

		}.toTypedArray()
	}
	
	override fun equals(other: Any?): Boolean {
		return other is ScillaPolyTypeApplication && origin == other.origin && typeArguments == other.typeArguments
	}

	override fun hashCode(): Int {
		return 59 * typeName.hashCode() + typeArguments.hashCode()
	}
}

class ScillaTypeSubstitution(private val typeVar: ScillaTypeVarType, private val substitution: ScillaType) {
	private val freeSet = buildFreeSet(substitution, mutableSetOf())

	private fun buildFreeSet(type: ScillaType, acc: MutableSet<ScillaTypeVarType>): Set<ScillaTypeVarType> {
		when (type) {
			is ScillaUnknownType,
			is ScillaAddressType,
			is ScillaByStrType,
			is ScillaPrimitiveType,
			is ScillaSimpleAlgebraicType,
			is ScillaPolyAlgebraicType -> {}
			is ScillaTypeVarType -> {
				acc.add(type)
			}
			is ScillaFunType -> {
				buildFreeSet(type.paramType, acc)
				buildFreeSet(type.resultType, acc)
			}
			is ScillaMapType -> {
				buildFreeSet(type.keyType, acc)
				buildFreeSet(type.valueType, acc)
			}
			is ScillaTypeFunType -> {
				val inner = mutableSetOf<ScillaTypeVarType>()
				buildFreeSet(type.body, inner)
				inner.remove(type.typeParameter)

				acc.addAll(inner)
			}
			is ScillaPolyTypeApplication -> {
				for (argument in type.typeArguments)
					buildFreeSet(argument, acc)
			}
		}
		return acc
	}

	fun substitute(type: ScillaType): ScillaType {
		return when (type) {
			is ScillaUnknownType,
			is ScillaAddressType,
			is ScillaByStrType,
			is ScillaPrimitiveType,
			is ScillaSimpleAlgebraicType,
			is ScillaPolyAlgebraicType -> type

			is ScillaTypeVarType -> {
				if (type == typeVar) substitution
				else type
			}
			is ScillaFunType -> {
				ScillaFunType(substitute(type.paramType), substitute(type.resultType))
			}
			is ScillaMapType -> {
				ScillaMapType(substitute(type.keyType), substitute(type.valueType))
			}
			is ScillaTypeFunType -> {
				if (typeVar == type.typeParameter)
					type
				else if (type.typeParameter in freeSet) {
					val newVar = type.typeParameter.increment()
					val newBody = ScillaTypeSubstitution(type.typeParameter, newVar).substitute(type.body)
					ScillaTypeFunType(newVar, substitute(newBody))
				}
				else ScillaTypeFunType(type.typeParameter, substitute(type.body))
			}
			is ScillaPolyTypeApplication -> {
				ScillaPolyTypeApplication(type.origin, type.typeArguments.map { substitute(it) })
			}
			else -> ScillaUnknownType
		}
	}
}

class ScillaTypeDeduction {
	val substitutions = mutableMapOf<ScillaTypeVarType, ScillaType>()
	val errors = mutableMapOf<ScillaType, ScillaType>()
	
	fun deduce(polyType: ScillaType, type: ScillaType) {
		when(polyType) {
			is ScillaMapType -> {
				when (type) {
					is ScillaMapType -> {
						deduce(polyType.keyType, type.keyType)
						deduce(polyType.valueType, type.valueType)
					}
					is ScillaUnknownType -> {
						deduce(polyType.keyType, ScillaUnknownType)
						deduce(polyType.valueType, ScillaUnknownType)
					}
					else -> {
						errors[polyType] = type
					}
				}
			}
			is ScillaTypeVarType -> {
				val old = substitutions[polyType]
				if (old != null) {
					if (old != type)
						errors[old] = type
				}
				else substitutions[polyType] = type
			}
			else -> throw NotImplementedError()
		}
	}
	
	fun substitute(polyType: ScillaType): ScillaType {
		var type = polyType
		for (substitution in substitutions) {
			type = ScillaTypeSubstitution(substitution.key, substitution.value).substitute(type)
		}
		return type
	}
}


