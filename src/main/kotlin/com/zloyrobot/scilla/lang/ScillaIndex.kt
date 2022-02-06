package com.zloyrobot.scilla.lang

import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class ScillaSymbolIndex : StringStubIndexExtension<ScillaNavigatableElement>() {
	companion object {
		val KEY: StubIndexKey<String, ScillaNavigatableElement> = StubIndexKey.createIndexKey("SCILLA_SYMBOL_INDEX")
	}

	override fun getVersion(): Int = ScillaElementType.SCILLA_CONTRACT_STUB_FILE.stubVersion
	override fun getKey(): StubIndexKey<String, ScillaNavigatableElement> = KEY
}

class ScillaContractLibraryAndTypeIndex : StringStubIndexExtension<ScillaNavigatableElement>() {
	companion object {
		val KEY: StubIndexKey<String, ScillaNavigatableElement> = StubIndexKey.createIndexKey("SCILLA_CONTRACT_INDEX")
	}

	override fun getVersion(): Int = ScillaElementType.SCILLA_CONTRACT_STUB_FILE.stubVersion
	override fun getKey(): StubIndexKey<String, ScillaNavigatableElement> = KEY
}


class ScillaLibraryIndex : StringStubIndexExtension<ScillaLibrary>() {
	companion object {
		val KEY: StubIndexKey<String, ScillaLibrary> = StubIndexKey.createIndexKey("SCILLA_LIBRARY_INDEX")
	}

	override fun getVersion(): Int = ScillaElementType.SCILLA_CONTRACT_STUB_FILE.stubVersion
	override fun getKey(): StubIndexKey<String, ScillaLibrary> = KEY
}
