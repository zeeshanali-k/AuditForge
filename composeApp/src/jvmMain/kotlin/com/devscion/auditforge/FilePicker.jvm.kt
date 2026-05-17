package com.devscion.auditforge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FilePicker actual constructor() {
    actual suspend fun pickFile(allowedExtensions: List<String>): PickedFile? {
        val file = withContext(Dispatchers.Main) {
            val chooser = JFileChooser()
            if (allowedExtensions.isNotEmpty()) {
                val exts = allowedExtensions.map { it.trimStart('.') }.toTypedArray()
                chooser.addChoosableFileFilter(
                    FileNameExtensionFilter(
                        "Allowed files (${exts.joinToString(", ") { "*.$it" }})",
                        *exts,
                    )
                )
                chooser.isAcceptAllFileFilterUsed = false
            }
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
        } ?: return null

        return withContext(Dispatchers.Default) {
            PickedFile(
                name = file.name,
                bytes = file.readBytes(),
                mimeType = guessMimeType(file.name),
            )
        }
    }

    private fun guessMimeType(filename: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "zip" -> "application/zip"
            "json" -> "application/json"
            "yaml", "yml" -> "application/yaml"
            "sql" -> "application/sql"
            "pdf" -> "application/pdf"
            "env" -> "text/plain"
            "toml" -> "application/toml"
            "ini" -> "text/plain"
            "tf" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
