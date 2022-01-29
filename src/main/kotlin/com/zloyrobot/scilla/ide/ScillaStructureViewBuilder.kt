package com.zloyrobot.scilla.ide

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.zloyrobot.scilla.lang.*

class ScillaStructureViewBuilderFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
		return object : TreeBasedStructureViewBuilder() {
			override fun createStructureViewModel(editor: Editor?): StructureViewModel {
				return ScillaStructureViewBuilder(editor, psiFile as ScillaFile)
			}
		}
	}
}

class ScillaStructureViewBuilder(editor: Editor?, file: ScillaFile) : TextEditorBasedStructureViewModel(editor, file),
	StructureViewModel.ElementInfoProvider {

	companion object {
		private fun canHaveChildren(element: Any) = when (element) {
			is ScillaFile,
			is ScillaLibrary,
			is ScillaLibraryEntry<*, *>,
			is ScillaContract -> true
			else -> false
		}
		private fun isPresentable(element: Any) = when (element) {
			is ScillaFile,
			is ScillaLibrary,
			is ScillaLibraryEntry<*, *>,
			is ScillaContract,
			is ScillaField,
			is ScillaComponent<*, *> -> true
			else -> false
		}
	}
	override fun getRoot() = TreeElement(psiFile)
	
	override fun isAlwaysShowsPlus(node: StructureViewTreeElement) = node.value is ScillaFile
	override fun isAlwaysLeaf(node: StructureViewTreeElement): Boolean = !canHaveChildren(node.value)


	class TreeElement(item: PsiElement) : PsiTreeElementBase<PsiElement>(item) {
		override fun getPresentableText(): String? {
			return (element as ScillaNavigatableElement).name
		}

		override fun getChildrenBase(): Collection<StructureViewTreeElement> {
			val node = element ?: return emptyList()
			return node.children.filter { isPresentable(it)}.sortedBy { it.textOffset }.map { TreeElement(it) }
		}
	}

}

