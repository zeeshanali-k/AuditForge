package com.devscion.auditforge

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume

@JsFun("(buffer) => Array.from(new Uint8Array(buffer))")
private external fun uint8ArrayFromBuffer(buffer: JsAny): JsArray<JsNumber>

actual class FilePicker actual constructor() {
    actual suspend fun pickFile(allowedExtensions: List<String>): PickedFile? =
        suspendCancellableCoroutine { cont ->
            val input = document.createElement("input").unsafeCast<HTMLInputElement>()
            input.type = "file"
            if (allowedExtensions.isNotEmpty()) {
                input.accept = allowedExtensions.joinToString(",") { ".$it" }
            }
            var resumed = false
            input.onchange = {
                if (!resumed) {
                    val file = input.files?.get(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = {
                            if (!resumed) {
                                resumed = true
                                val result = reader.result
                                if (result != null) {
                                    val jsArr = uint8ArrayFromBuffer(result.unsafeCast<JsAny>())
                                    val bytes = ByteArray(jsArr.length) { i ->
                                        jsArr[i].toInt().and(0xFF).toByte()
                                    }
                                    cont.resume(
                                        PickedFile(
                                            name = file.name,
                                            bytes = bytes,
                                            mimeType = file.type.ifEmpty { "application/octet-stream" },
                                        )
                                    )
                                } else {
                                    cont.resume(null)
                                }
                            }
                        }
                        reader.readAsArrayBuffer(file)
                    } else {
                        resumed = true
                        cont.resume(null)
                    }
                }
            }
            document.body?.appendChild(input)
            input.click()
            cont.invokeOnCancellation {
                if (!resumed) {
                    resumed = true
                    document.body?.removeChild(input)
                }
            }
        }
}
