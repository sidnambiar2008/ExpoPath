package org.communityday.navigation.events.data

import kotlinx.serialization.Serializable


@Serializable
data class Conference(
    val id: String = "",
    val name: String = "",
    val organization: String = "",
    val description: String = ""
)



