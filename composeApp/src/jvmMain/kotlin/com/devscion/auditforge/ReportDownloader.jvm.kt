package com.devscion.auditforge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser

actual class ReportDownloader actual constructor() {
    actual suspend fun downloadReport(fileName: String, bytes: ByteArray, mimeType: String) {
        val target = withContext(Dispatchers.Main) {
            val chooser = JFileChooser()
            chooser.selectedFile = File(fileName)
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
        } ?: return
        withContext(Dispatchers.Default) {
            target.writeBytes(bytes)
        }
    }
}
