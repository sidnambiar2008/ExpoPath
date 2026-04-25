package org.communityday.navigation.events.data

import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchParamsObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.encodeToJsonElement

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
        try {
            // 2. FIX: Use SearchParamsObject (not SearchMethodParams)
            val response = client.searchSingleIndex(
                indexName = "conferences_index",
                searchParams = SearchParamsObject(
                    query = query
                )
            )

            // 3. FIX: Convert the Hit map to your Conference class
            return response.hits.map { hit ->
                // 1. Convert the properties to a JsonObject
                val hitJson = jsonParser.encodeToJsonElement(hit.additionalProperties).jsonObject.toMutableMap()

                // 2. Manually inject the objectID into the map before decoding
                // This ensures your @SerialName("objectID") in Conference matches up
                hitJson["objectID"] = jsonParser.encodeToJsonElement(hit.objectID)

                val finalJson = JsonObject(hitJson)
                jsonParser.decodeFromJsonElement<Conference>(finalJson)
            }
        } catch (e: Exception) {
            println("Search Error: ${e.message}")
            return emptyList()
        }
    }
}
