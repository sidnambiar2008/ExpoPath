package org.communityday.navigation.events.data

import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchParamsObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

actual class ConferenceSearcher {
    // 1. Plain Strings (No AppID or APIKey wrappers!)
    private val client = SearchClient(
        appId = "QZPCMXM01D",
        apiKey = "6cc7d0551bd8fb59c75313135385a396"
    )

    private val jsonParser = Json {
        ignoreUnknownKeys = true
    }

    actual suspend fun search(query: String): List<Conference> {
        return try {
            val response = client.searchSingleIndex(
                indexName = "conferences_index",
                searchParams = SearchParamsObject(query = query)
            )

            response.hits.map { hit ->
                val finalJson = buildJsonObject {
                    // 1. Put all the data fields from Algolia into the object
                    hit.additionalProperties?.forEach { (key, value) ->
                        put(key, value)
                    }
                    // 2. Explicitly add the objectID so the Conference class can find it
                    put("objectID", hit.objectID)                }

                jsonParser.decodeFromJsonElement<Conference>(finalJson)
            }
        } catch (e: Exception) {
            println("Search Error: ${e.message}")
            emptyList()
        }
    }
}
