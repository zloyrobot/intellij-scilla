package com.zloyrobot.scilla.lang

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

object   ScillaLanguage : Language("Scilla")


object ScillaFileType : LanguageFileType(ScillaLanguage) {
    override fun getDisplayName() = "Scilla"
    override fun getName() = "Scilla"
    override fun getDescription() = "Scilla"
    override fun getDefaultExtension() = "scilla"
    override fun getIcon() = null
    override fun getCharset(file: VirtualFile, content: ByteArray): String = StandardCharsets.UTF_8.name()
}
