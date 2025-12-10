package com.dev.jakob.watermanager.data.model

/**
 * Datenklasse, die einen Wasserbehälter repräsentiert.
 *
 * @param name Der Name des Behälters (z.B. "Glas", "Flasche").
 * @param size Die Füllmenge des Behälters in Millilitern.
 */
data class Container(val name: String, val size: Int)
