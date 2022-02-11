package com.zloyrobot.scilla.lang

import java.util.concurrent.atomic.AtomicInteger


interface ScillaType {
	val presentation: String
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
	}
}


class ScillaAddressType : ScillaType {
	override val presentation: String get() = "ByStr20"
}

data class ScillaByStrType(val size: Int) : ScillaNamedType {
	override val typeName: String get() = "ByStr$size"
	
	companion object {
		val ByStr20 = ScillaByStrType(20)
		val ByStr32 = ScillaByStrType(32)
		val ByStr = ScillaPrimitiveType.ByStr
	}

	override fun toString(): String = presentation

}

enum class ScillaPrimitiveType(override val typeName: String) : ScillaNamedType {
	Int32("Int32"),
	Int64("Int64"),
	Int128("Int128"),
	Int256("Int256"),
	Uint32("Uint32"),
	Uint64("Uint64"),
	Uint128("Uint128"),
	Uint256("Uint256"),
	StringType("String"),
	BNum("BNum"),
	Message("Message"),
	Event("Event"),
	ByStr("ByStr");

	companion object {
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
		private val BOOL =
			ScillaSimpleAlgebraicType("Bool",
				ScillaTypeConstructor("True"),
				ScillaTypeConstructor("False"))
		
		private val NAT =
			ScillaSimpleAlgebraicType("Nat",
				ScillaTypeConstructor("Zero"),
				ScillaTypeConstructor("Succ"))

		private val OPTION =
			ScillaPolyAlgebraicType(listOf(ScillaTypeVarType("'A")),
			ScillaSimpleAlgebraicType("Option",
				ScillaTypeConstructor("Some", ScillaTypeVarType("'A")),
				ScillaTypeConstructor("None")))
				
		private val PAIR =
			ScillaPolyAlgebraicType(listOf(ScillaTypeVarType("'A"), ScillaTypeVarType("'B")),
			ScillaSimpleAlgebraicType("Pair", 
				ScillaTypeConstructor("Pair", ScillaTypeVarType("'A"), ScillaTypeVarType("'B"))))

		private val CONS_CONSTRUCTOR_INCOMPLETE = ScillaTypeConstructor("Cons")
		private val LIST: ScillaPolyAlgebraicType =
			ScillaPolyAlgebraicType(listOf(ScillaTypeVarType("'A")),
			ScillaSimpleAlgebraicType("List",
				CONS_CONSTRUCTOR_INCOMPLETE,
				ScillaTypeConstructor("Nil")))

		private val TYPES = listOf(BOOL, OPTION, LIST, PAIR, PAIR, NAT)
		
		init {
			CONS_CONSTRUCTOR_INCOMPLETE.types = arrayOf(
				ScillaTypeVarType("'A"),
				ScillaPolyAlgebraicTypeApplication(LIST, listOf(ScillaTypeVarType("'A"))))
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
}


class ScillaFunType(val paramType: ScillaType, val resultType: ScillaType) : ScillaType {
	override val presentation: String get() {
		val paramPresentation = when (paramType) {
			is ScillaPolyAlgebraicType -> paramType.presentation 
			is ScillaPolyAlgebraicTypeApplication -> "(${paramType.presentation})" 
			is ScillaFunType,
			is ScillaPolyFunType -> "(${paramType.presentation})"
			else -> paramType.presentation
		}
		return "$paramPresentation -> ${resultType.presentation}"
	}
	
	override fun toString(): String = presentation
}

open class ScillaPolyFunType(val typeParameter: ScillaTypeVarType, open val body: ScillaType) : ScillaType {
	override val presentation: String get() = "forall ${typeParameter.presentation}. ${body.presentation}"
	
	override fun toString(): String = presentation
}

class ScillaPolyAlgebraicType(val typeParameters: List<ScillaTypeVarType>, val body: ScillaAlgebraicType) : ScillaAlgebraicType {
	override val typeName: String get() = body.typeName
	
	override val presentation: String get() {
		return typeParameters.joinToString(" ", "", body.presentation) { "forall ${it.typeName}. "}
	}

	override val constructors: Array<out ScillaTypeConstructor> get() = body.constructors
}

class ScillaPolyAlgebraicTypeApplication(val origin: ScillaPolyAlgebraicType, val typeArguments: List<ScillaType>) 
	: ScillaAlgebraicType {
	
	override val typeName: String get() = origin.typeName
	override val presentation: String get() {
		val arguments = typeArguments + origin.typeParameters.drop(typeArguments.size)
		return arguments.joinToString(" ", "$typeName ") {
			if (it is ScillaPolyAlgebraicType || it is ScillaPolyAlgebraicTypeApplication)
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
			is ScillaPolyFunType -> {
				val inner = mutableSetOf<ScillaTypeVarType>()
				buildFreeSet(type.body, inner)
				inner.remove(type.typeParameter)

				acc.addAll(inner)
			}
			is ScillaPolyAlgebraicTypeApplication -> {
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
			is ScillaPolyFunType -> {
				if (typeVar == type.typeParameter)
					type
				else if (type.typeParameter in freeSet) {
					val newVar = type.typeParameter.increment()
					val newBody = ScillaTypeSubstitution(type.typeParameter, newVar).substitute(type.body)
					ScillaPolyFunType(newVar, substitute(newBody))
				}
				else ScillaPolyFunType(type.typeParameter, substitute(type.body))
			}
			is ScillaPolyAlgebraicTypeApplication -> {
				ScillaPolyAlgebraicTypeApplication(type.origin, type.typeArguments.map { substitute(it) })
			}
			else -> ScillaUnknownType
		}
	}
}


