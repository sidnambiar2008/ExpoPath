package org.communityday.navigation.events.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Conference(
    @SerialName("objectID") val objectID: String = "",
    val joinCode: String = "",
    val name: String = "",
    val isPublic: Boolean = true,
    // If these aren't in the Algolia JSON yet, they MUST have default values
    val organization: String = "",
    val description: String = "",
    val isPublished: Boolean = false,
    val ownerId: String = "",
    @SerialName("location") val address: String = "",
    //val dateMillis: Long = 0L,
    val dateString: String = ""
)

