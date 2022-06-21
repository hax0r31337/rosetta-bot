package test.rosetta

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val cacheDir = File("./.cache").also {
    if (!it.exists()) it.mkdir()
}

fun loadJsonFromWeb(url: String, name: String): JsonElement {
    val file = File(cacheDir, name)
    if (!file.exists()) {
        println("Downloading $url")
        val conn = URL(url).openConnection() as HttpURLConnection

        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
        conn.setRequestProperty("Accept", "*/*")
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9")

        conn.connect()

        file.writeBytes(conn.inputStream.readBytes())
    }

    return JsonParser.parseReader(file.reader())
}