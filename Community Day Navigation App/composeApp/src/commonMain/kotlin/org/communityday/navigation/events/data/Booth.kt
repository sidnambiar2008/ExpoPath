package org.communityday.navigation.events.data

import kotlinx.serialization.Serializable

@Serializable
data class Booth(val id: String = "",
                  val name: String = "",
                  val description: String = "",
                  val organization: String = "",
                  val latitude: Double = 0.0,
                  val longitude: Double = 0.0,
                  val category: String = "Exhibitor",
                  val imageUrl: String? = null)
{

}

