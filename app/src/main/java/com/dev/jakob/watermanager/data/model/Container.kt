package com.dev.jakob.watermanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "containers")
data class Container(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val size: Int
)
