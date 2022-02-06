package com.zloyrobot.scilla.ide

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.zloyrobot.scilla.lang.*
import javax.swing.Icon

abstract class ScillaGotoContributor(private val key: StubIndexKey<String, ScillaNavigatableElement>) 
	: ChooseByNameContributor, GotoClassContributor {

	override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<out String> {
		return when (project) {
			null -> emptyArray()
			else -> StubIndex.getInstance().getAllKeys(key, project).toTypedArray()
		}
	}

	override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<out NavigationItem> {
		if (project == null || name == null) 
			return emptyArray()
		
		val scope = if (includeNonProjectItems) GlobalSearchScope.allScope(project)
		else GlobalSearchScope.projectScope(project)

		return StubIndex.getElements(key, name, project, scope, ScillaNavigatableElement::class.java).toTypedArray()
	}

	override fun getQualifiedName(item: NavigationItem?): String? {
		return when(item) {
			is ScillaLibraryEntry<*, *> -> item.qualifiedName
			is ScillaContractEntry<*, *> -> item.qualifiedName
			else -> item?.name
		}
	}

	override fun getQualifiedNameSeparator(): String = "."
}


class ScillaGotoClassContributor : ScillaGotoContributor(ScillaContractLibraryAndTypeIndex.KEY)
class ScillaGotoSymbolContributor : ScillaGotoContributor(ScillaSymbolIndex.KEY)
