package org.communityday.navigation.events.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json

// Move this outside the class so it's only created once for the whole app
private val sharedClient = HttpClient {
    install(ContentNegotiation) {
        // This lets Ktor handle the Json parsing for you automatically
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
}
actual class ConferenceSearcher {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    actual suspend fun search(query: String): List<Conference> {
        return try {
            // 1. Properly encode the query for a URL string
            val queryString = "query=${query.encodeURLParameter()}"
            val response =
                sharedClient.post("https://QZPCMXM01D-dsn.algolia.net/1/indexes/conferences_index/query") {

                    header("X-Algolia-Application-Id", "QZPCMXM01D")
                    header("X-Algolia-API-Key", "6cc7d0551bd8fb59c75313135385a396")
                    contentType(ContentType.Application.Json)

                    // 2. Algolia expects the body to look like: {"params": "query=searchterm"}
                    setBody(buildJsonObject {
                        put("params", queryString)
                    })
                }

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = response.body<JsonObject>()
                val hits = jsonResponse["hits"]?.jsonArray ?: JsonArray(emptyList())

                hits.map {
                    // Use our specific jsonParser instance here
                    jsonParser.decodeFromJsonElement<Conference>(it)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Algolia Search Failed: ${e.message}")
            emptyList()
        }
    }
}