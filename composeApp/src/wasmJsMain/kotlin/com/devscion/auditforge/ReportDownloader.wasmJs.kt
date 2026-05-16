package com.devscion.auditforge

@JsFun(
    "(bytes, mimeType, fileName) => {" +
        " const blob = new Blob([new Uint8Array(bytes)], {type: mimeType});" +
        " const url = URL.createObjectURL(blob);" +
        " const a = document.createElement('a');" +
        " a.href = url;" +
        " a.download = fileName;" +
        " document.body.appendChild(a);" +
        " a.click();" +
        " document.body.removeChild(a);" +
        " setTimeout(() => URL.revokeObjectURL(url), 100);" +
        "}"
)
private external fun jsTriggerDownload(bytes: JsArray<JsNumber>, mimeType: JsString, fileName: JsString)

actual class ReportDownloader actual constructor() {
    actual suspend fun downloadReport(fileName: String, bytes: ByteArray, mimeType: String) {
        val jsArray = JsArray<JsNumber>()
        bytes.forEachIndexed { i, byte -> jsArray[i] = byte.toInt().and(0xFF).toJsNumber() }
        jsTriggerDownload(jsArray, mimeType.toJsString(), fileName.toJsString())
    }
}
