package com.yourapp.aikeyboard.ai

import com.yourapp.aikeyboard.data.model.AiReplyRequest
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

interface AiApiService {
    @Throws(Exception::class)
    fun requestReplies(request: AiReplyRequest): String
}

class HttpAiApiService(
    private val endpointUrl: String,
    private val apiKey: String?
) : AiApiService {

    override fun requestReplies(request: AiReplyRequest): String {
        val url = URL(endpointUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            apiKey?.takeIf { it.isNotBlank() }?.let {
                setRequestProperty("Authorization", "Bearer $it")
            }
            doOutput = true
            connectTimeout = 30000
            readTimeout = 30000
        }

        connection.outputStream.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8)).use { writer ->
                writer.write(request.toJson().toString())
                writer.flush()
            }
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        val responseText = stream.bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
        if (status !in 200..299) {
            throw AiApiException("API request failed with status $status: $responseText")
        }

        return responseText
    }
}

class AiApiException(message: String) : Exception(message)
