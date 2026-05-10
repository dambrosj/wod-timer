package com.wod.app.domain.model

/**
 * Domain representation of a SavedWod. The Room entity lives in
 * [com.wod.app.data.db.SavedWodEntity] and maps 1:1 to this class via the
 * repository. PRD §4.
 */
data class SavedWod(
    val id: String,
    val name: String,
    val type: TimerType,
    val configJson: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val lastUsedAt: Long? = null,
    val timesUsed: Int = 0,
    val isFavourite: Boolean = false,
    val isBuiltIn: Boolean = false,
)
