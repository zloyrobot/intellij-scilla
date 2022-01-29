package com.zloyrobot.scilla.ide

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.zloyrobot.scilla.lang.*

class ScillaFoldingBuilder : FoldingBuilderEx(), DumbAware {
	
	class ElementProcessor : PsiElementVisitor() {
		val accumulator = mutableListOf<FoldingDescriptor>()
		
		override fun visitElement(element: PsiElement) {
			super.visitElement(element)
			if (element.node.textContains('\n'))
				tryBuildFoldingForElement(element)

			element.acceptChildren(this)
		}

		private fun tryBuildFoldingForElement(element: PsiElement) {
			val range = when (element) {
				is ScillaComponent<*, *> -> TextRange(
					/* startOffset = */ element.parameterList?.endOffset
						?: element.nameIdentifier?.endOffset
						?: element.definitionKeyword.endOffset,
					/* endOffset = */ element.endKeyword?.startOffset ?: element.endOffset)

				is ScillaLibraryType -> TextRange(
					/* startOffset = */ element.eqToken?.endOffset
						?: element.nameIdentifier?.endOffset
						?: element.typeKeyword.endOffset,
					/* endOffset = */ element.endOffset)

				is ScillaLibraryLet -> TextRange(
					/* startOffset = */ element.eqToken?.endOffset
						?: element.nameIdentifier?.endOffset
						?: element.letKeyword.endOffset,
					/* endOffset = */ element.endOffset)

				is ScillaMatchElement -> TextRange(
					/* startOffset = */ element.withKeyword?.endOffset
						?: element.subject?.endOffset
						?: element.matchKeyword.endOffset,
					/* endOffset = */ element.endKeyword?.startOffset ?: element.endOffset)
				
				else -> if (element.elementType in ScillaTokenType.COMMENTS) element.textRange else null
			}

			if (range != null)
				accumulator.add(FoldingDescriptor(element.node, range))
		}
	}
	override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
		val processor = ElementProcessor()
		root.accept(processor)
		
		return processor.accumulator.toTypedArray()
	}

	override fun getPlaceholderText(node: ASTNode): String {
		return when (node.elementType) {
			ScillaTokenType.COMMENT -> getCommentPlaceholder(node)
			else -> "..."
		}
	}

	private fun getCommentPlaceholder(node: ASTNode): String {
		return node.text.split("\n").firstNotNullOfOrNull {
			val line = it.trim().removePrefix("*").
						  trim().removePrefix("(*").
						  trim().removeSuffix("*)").
			 			  trimEnd('.', ' ')

			if (line.isNotEmpty()) "(* $line...*)" else null
		} ?: "(**)"
	}

	override fun isCollapsedByDefault(node: ASTNode): Boolean {
		return false
	}
}
