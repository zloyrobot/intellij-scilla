package com.zloyrobot.scilla.ide

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.zloyrobot.scilla.lang.*
import javax.swing.Icon

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
			is ScillaLibraryDefinition,
			is ScillaLibraryEntry,
			is ScillaContractDefinition,
			is ScillaComponentDefinition -> true
			else -> false
		}
	}
	override fun getRoot() = TreeElement(psiFile)
	
	override fun isAlwaysShowsPlus(node: StructureViewTreeElement) = node.value is ScillaFile
	override fun isAlwaysLeaf(node: StructureViewTreeElement): Boolean = !canHaveChildren(node.value)


	class TreeElement(item: PsiElement) : PsiTreeElementBase<PsiElement>(item) {
		override fun getPresentableText(): String {
			return when(val element = this.element) {
				is ScillaFile -> element.name
				is ScillaNamedElement -> element.name
				else -> ""
			}
		}
		
		override fun getIcon(open: Boolean): Icon? {
			return when(val element = element) {
				is ScillaFile -> PlainTextFileType.INSTANCE.icon
				is ScillaLibraryDefinition -> AllIcons.Nodes.PpLib
				is ScillaLibraryTypeDefinition -> AllIcons.Nodes.Type
				is ScillaContractDefinition -> AllIcons.Nodes.Class
				is ScillaTransitionDefinition -> AllIcons.Nodes.Test 
				is ScillaProcedureDefinition -> AllIcons.Nodes.Property
				
				is ScillaLibraryLetDefinition -> when(element.expression) {
					is ScillaFunExpression, is ScillaTFunExpression -> AllIcons.Nodes.Function
					else -> AllIcons.Nodes.Constant 
				}
				
				else -> AllIcons.Nodes.Unknown
			}
		} 

		override fun getChildrenBase(): Collection<StructureViewTreeElement> {
			val node = element ?: return emptyList()
			return node.children.filter {canHaveChildren(it)}.sortedBy { it.textOffset }.map { TreeElement(it) }
		}
	}

}

