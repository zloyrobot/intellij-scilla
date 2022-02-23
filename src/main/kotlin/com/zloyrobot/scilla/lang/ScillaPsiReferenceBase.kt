package com.zloyrobot.scilla.lang

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase

abstract class ScillaPsiReferenceBase<P: ScillaPsiElement, T: ScillaNamedElement>(
    element: P, private val nameElement: ScillaName?, range: TextRange
) : PsiReferenceBase<P>(element, range) {
	
	private val name: String = nameElement?.name ?: element.name!!
	
	override fun resolve(): PsiElement? {
		var result: T? = null
		processNameElement {
			if (it.name == name) {
				result = it
				true
			}
			else false
		}
		return result
	}

	override fun getVariants(): Array<Any> {
		val collector = mutableListOf<T>()
		processNameElement {
			collector.add(it)
			false
		}
		return collector.toTypedArray()
	}

	override fun handleElementRename(newElementName: String): PsiElement {
		return if (nameElement != null)
			nameElement.setName(newElementName)
		else {
			val element = element
			if (element is ScillaNamedElement)
				element.setName(newElementName)
			
			else throw UnsupportedOperationException()
		}
	}

	private fun processNameElement(processor: (it: T) -> Boolean) {
		when (nameElement) {
			is ScillaSimpleName -> processFile(processor)
			is ScillaQualifiedName -> {
				val multiReference = nameElement.reference as PsiPolyVariantReference
				for (resolve in multiReference.multiResolve(false)){
					val import = resolve.element as? ScillaImport ?: continue
					val library = import.reference.resolve() as? ScillaLibrary
					if (library != null && processLibrary(library, processor))
						return
				}
			}
			is ScillaHexQualifiedName -> TODO()
			else -> processFile(processor)
		}
	}
	
	protected fun processElements(elements: Iterable<T>?, processor: (target: T) -> Boolean): Boolean {
		if (elements == null)
			return false

		for (element in elements) {
			if (processor(element)) return true
		}
		return false
	}
	
	protected fun processElements(elements: Sequence<T>?, processor: (target: T) -> Boolean): Boolean {
		if (elements == null)
			return false
		
		for (element in elements) {
			if (processor(element)) return true
		}
		return false
	}
	
	protected fun processCurrentAndImportedLibraries(processor: (target: T) -> Boolean): Boolean {
		val file = element.containingFile ?: return false
		val currentLibrary = file.library
		if (currentLibrary != null && processLibrary(currentLibrary, processor))
			return true

		val imports = file.imports?.imports ?: return false
		for (import in imports) {
			val library = import.reference.resolve() as? ScillaLibrary
			if (library != null && processLibrary(library, processor))
				return true
		}
		return false
	}
	
	protected open fun processLibrary(library: ScillaLibrary, processor: (it: T) -> Boolean): Boolean = false
	
	protected abstract fun processFile(processor: (it: T) -> Boolean): Boolean
}