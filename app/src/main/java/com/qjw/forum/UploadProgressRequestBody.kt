package com.qjw.forum

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.File

class UploadProgressRequestBody(
    private val file: File,
    private val mediaType: MediaType?,
    private val onProgress: (sent: Long, total: Long) -> Unit
) : RequestBody() {
    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val total = contentLength()
        var sent = 0L
        val buffer = Buffer()
        val source: Source = file.source()

        source.use {
            var read: Long
            while (it.read(buffer, 8 * 1024L).also { count -> read = count } != -1L) {
                sink.write(buffer, read)
                sent += read
                onProgress(sent, total)
            }
        }
    }
}
