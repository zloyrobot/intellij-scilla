package com.zloyrobot.scilla.lang

import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IStubFileElementType


class ScillaStubFileElementType : IStubFileElementType<PsiFileStub<ScillaFile>>(ScillaLanguage) {
	override fun getStubVersion(): Int = 1
}

class ScillaContractStubElementType(val id: String) : IStubElementType<ScillaContractStub, ScillaContract>(id, ScillaLanguage) {
	override fun getExternalId(): String = id

	override fun serialize(stub: ScillaContractStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaContractStub {
		return ScillaContractStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaContractStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
			sink.occurrence(ScillaContractLibraryAndTypeIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaContractStub): ScillaContract {
		return ScillaContract(stub)
	}
	override fun createStub(psi: ScillaContract, parent: StubElement<*>?): ScillaContractStub {
		return ScillaContractStub(parent, psi.name)
	}
}

class ScillaTransitionStubElementType(val id: String) : IStubElementType<ScillaTransitionStub, ScillaTransition>(id, ScillaLanguage) {
	override fun getExternalId(): String = id

	override fun serialize(stub: ScillaTransitionStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaTransitionStub {
		return ScillaTransitionStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaTransitionStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaTransitionStub): ScillaTransition {
		return ScillaTransition(stub)
	}
	override fun createStub(psi: ScillaTransition, parentStub: StubElement<*>?): ScillaTransitionStub {
		return ScillaTransitionStub(parentStub, psi.name)
	}
}

class ScillaProcedureStubElementType(val id: String) : IStubElementType<ScillaProcedureStub, ScillaProcedure>(id, ScillaLanguage) {
	override fun getExternalId(): String = id
	
	override fun serialize(stub: ScillaProcedureStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaProcedureStub {
		return ScillaProcedureStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaProcedureStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaProcedureStub): ScillaProcedure {
		return ScillaProcedure(stub)
	}
	override fun createStub(psi: ScillaProcedure, parentStub: StubElement<*>?): ScillaProcedureStub {
		return ScillaProcedureStub(parentStub, psi.name)
	}
}

class ScillaFieldStubElementType(val id: String) : IStubElementType<ScillaFieldStub, ScillaField>(id, ScillaLanguage) {
	override fun getExternalId(): String = id

	override fun serialize(stub: ScillaFieldStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaFieldStub {
		return ScillaFieldStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaFieldStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaFieldStub): ScillaField {
		return ScillaField(stub)
	}
	override fun createStub(psi: ScillaField, parentStub: StubElement<*>?): ScillaFieldStub {
		return ScillaFieldStub(parentStub, psi.name)
	}
}


class ScillaLibraryStubElementType(val id: String) : IStubElementType<ScillaLibraryStub, ScillaLibrary>(id, ScillaLanguage) {
	override fun getExternalId(): String = id

	override fun serialize(stub: ScillaLibraryStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaLibraryStub {
		return ScillaLibraryStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaLibraryStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
			sink.occurrence(ScillaContractLibraryAndTypeIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaLibraryStub): ScillaLibrary {
		return ScillaLibrary(stub)
	}
	override fun createStub(psi: ScillaLibrary, parentStub: StubElement<*>?): ScillaLibraryStub {
		return ScillaLibraryStub(parentStub, psi.name)
	}
}

class ScillaLibraryLetStubElementType(val id: String) : IStubElementType<ScillaLibraryLetStub, ScillaLibraryLet>(id, ScillaLanguage) {
	override fun getExternalId(): String = id

	override fun serialize(stub: ScillaLibraryLetStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaLibraryLetStub {
		return ScillaLibraryLetStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaLibraryLetStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaLibraryLetStub): ScillaLibraryLet {
		return ScillaLibraryLet(stub)
	}
	override fun createStub(psi: ScillaLibraryLet, parentStub: StubElement<*>?): ScillaLibraryLetStub {
		return ScillaLibraryLetStub(parentStub, psi.name)
	}
}

class ScillaLibraryTypeStubElementType(val id: String) : IStubElementType<ScillaLibraryTypeStub, ScillaLibraryType>(id, ScillaLanguage) {
	override fun getExternalId(): String = id

	override fun serialize(stub: ScillaLibraryTypeStub, dataStream: StubOutputStream) {
		dataStream.writeName(stub.name)
	}
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ScillaLibraryTypeStub {
		return ScillaLibraryTypeStub(parentStub, dataStream.readNameString())
	}
	override fun indexStub(stub: ScillaLibraryTypeStub, sink: IndexSink) {
		if (stub.name != null) {
			sink.occurrence(ScillaSymbolIndex.KEY, stub.name)
			sink.occurrence(ScillaContractLibraryAndTypeIndex.KEY, stub.name)
		}
	}
	override fun createPsi(stub: ScillaLibraryTypeStub): ScillaLibraryType {
		return ScillaLibraryType(stub)
	}
	override fun createStub(psi: ScillaLibraryType, parentStub: StubElement<*>?): ScillaLibraryTypeStub {
		return ScillaLibraryTypeStub(parentStub, psi.name)
	}
}





