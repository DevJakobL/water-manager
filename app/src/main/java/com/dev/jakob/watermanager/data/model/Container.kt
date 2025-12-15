package com.dev.jakob.watermanager.data.model

import java.util.*

/**
 * Data class representing a water container.
 *
 * @property id A unique identifier for the container, crucial for list operations and database persistence.
 *              It's nullable to handle deserialization of older data that might not have an ID.
 * @property name The name of the container (e.g., "Glass", "Bottle").
 * @property size The capacity of the container in milliliters.
 */
data class Container(
    val id: String = UUID.randomUUID().toString(), // Make nullable, default for new containers
    val name: String,
    val size: Int
)
