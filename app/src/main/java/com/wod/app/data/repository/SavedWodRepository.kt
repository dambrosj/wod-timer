package com.wod.app.data.repository

import com.wod.app.data.db.SavedWodDao
import com.wod.app.data.db.toDomain
import com.wod.app.data.db.toEntity
import com.wod.app.domain.model.CustomInterval
import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SavedWodRepository(private val dao: SavedWodDao) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val prettyJson = Json { ignoreUnknownKeys = true; prettyPrint = true }

    val allFlow: Flow<List<SavedWod>> = dao.getAllFlow().map { it.map(::toDomain) }

    val favouritesFlow: Flow<List<SavedWod>> = dao.getFavouritesFlow().map { it.map(::toDomain) }

    fun byTypeFlow(type: TimerType): Flow<List<SavedWod>> =
        dao.getByTypeFlow(type.name).map { it.map(::toDomain) }

    suspend fun getById(id: String): SavedWod? = dao.getById(id)?.toDomain()

    suspend fun save(wod: SavedWod) = dao.upsert(wod.toEntity())

    /** Creates a new user WOD from the current config screen state and persists it. */
    suspend fun saveFromConfig(name: String, description: String, config: TimerConfig) {
        val type = when (config) {
            is TimerConfig.Tabata  -> TimerType.TABATA
            is TimerConfig.Amrap   -> TimerType.AMRAP
            is TimerConfig.ForTime -> TimerType.FOR_TIME
            is TimerConfig.Emom    -> TimerType.EMOM
            is TimerConfig.Mix     -> TimerType.MIX
            is TimerConfig.Custom  -> TimerType.CUSTOM
        }
        save(SavedWod(
            id          = java.util.UUID.randomUUID().toString(),
            name        = name,
            type        = type,
            configJson  = json.encodeToString<TimerConfig>(config),
            description = description,
            isBuiltIn   = false,
            createdAt   = System.currentTimeMillis(),
        ))
    }

    suspend fun setFavourite(id: String, isFavourite: Boolean) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(isFavourite = isFavourite))
    }

    suspend fun incrementUsage(id: String) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(
            timesUsed = entity.timesUsed + 1,
            lastUsedAt = System.currentTimeMillis(),
        ))
    }

    suspend fun delete(id: String) = dao.deleteById(id)

    suspend fun countAll(): Int = dao.count()

    /** Serializes all WODs to a pretty-printed JSON string (iOS-compatible format). */
    suspend fun exportJson(): String {
        val dtos = dao.getAll().map { it.toDomain().toExportDto() }
        return prettyJson.encodeToString<List<SavedWodExportDto>>(dtos)
    }

    /**
     * Decodes [jsonString] and upserts each WOD (overwrite if ID exists, insert if new).
     * Returns the number of WODs successfully processed.
     */
    suspend fun importJson(jsonString: String): Int {
        val dtos = runCatching {
            json.decodeFromString<List<SavedWodExportDto>>(jsonString)
        }.getOrElse { return 0 }
        var count = 0
        dtos.forEach { dto ->
            dto.toDomain()?.let { wod ->
                dao.upsert(wod.toEntity())
                count++
            }
        }
        return count
    }

    /** Idempotent: inserts only the built-in WODs that don't already exist in the DB. */
    suspend fun seedBuiltIns() {
        BuiltInWods.all(json).forEach { wod -> dao.insertIfAbsent(wod.toEntity()) }
    }

    private fun toDomain(entity: com.wod.app.data.db.SavedWodEntity) = entity.toDomain()
}

// T08b — factory WODs bundled with the app.
// Personal WODs (isBuiltIn = false) are seeded with stable UUIDs so
// insertIfAbsent skips them if already present; they survive reinstall.
private object BuiltInWods {

    fun all(json: Json): List<SavedWod> {
        val now = System.currentTimeMillis()
        return listOf(
            tabataClassico(json, now),
            bodyWeight(json, now),
            cindy(json, now),
            fran(json, now),
            murph(json, now),
            // ── Personal WODs (Nicola) ────────────────────────────────────────
            wodSolito(json, now),
            wormUp(json, now),
            fullBodyResistanceBand(json, now),
            defaticamentoStretching(json, now),
        )
    }

    private fun encode(json: Json, config: TimerConfig): String =
        json.encodeToString<TimerConfig>(config)

    private fun tabataClassico(json: Json, now: Long) = SavedWod(
        id = "built_in_tabata_classico",
        name = "Tabata Classico",
        type = TimerType.TABATA,
        configJson = encode(json, TimerConfig.Tabata(series = 8, workSeconds = 20, restSeconds = 10)),
        description = "Il protocollo Tabata originale: 8 serie da 20s lavoro / 10s riposo.",
        tags = listOf("tabata", "classico"),
        isBuiltIn = true,
        createdAt = now,
    )

    private fun bodyWeight(json: Json, now: Long) = SavedWod(
        id = "built_in_body_weight",
        name = "Body Weight",
        type = TimerType.TABATA,
        configJson = encode(json, TimerConfig.Tabata(series = 20, workSeconds = 30, restSeconds = 15)),
        description = "Tabata esteso a corpo libero: 20 serie da 30s lavoro / 15s riposo.",
        tags = listOf("tabata", "corpo libero"),
        isBuiltIn = true,
        createdAt = now,
    )

    private fun cindy(json: Json, now: Long) = SavedWod(
        id = "built_in_cindy",
        name = "Cindy",
        type = TimerType.AMRAP,
        configJson = encode(json, TimerConfig.Amrap(
            durationSeconds = 20 * 60,
            exercises = listOf("5 trazioni", "10 piegamenti", "15 squat"),
        )),
        description = "AMRAP 20 min: 5 trazioni, 10 piegamenti, 15 squat.",
        tags = listOf("amrap", "girls", "crossfit"),
        isBuiltIn = true,
        createdAt = now,
    )

    private fun fran(json: Json, now: Long) = SavedWod(
        id = "built_in_fran",
        name = "Fran",
        type = TimerType.FOR_TIME,
        configJson = encode(json, TimerConfig.ForTime(
            timecapSeconds = 10 * 60,
            rounds = 1,
            exercises = listOf("21 thruster", "21 trazioni", "15 thruster", "15 trazioni", "9 thruster", "9 trazioni"),
        )),
        description = "For Time (timecap 10 min): 21-15-9 thruster + trazioni.",
        tags = listOf("for time", "girls", "crossfit"),
        isBuiltIn = true,
        createdAt = now,
    )

    private fun murph(json: Json, now: Long) = SavedWod(
        id = "built_in_murph",
        name = "Murph",
        type = TimerType.FOR_TIME,
        configJson = encode(json, TimerConfig.ForTime(
            timecapSeconds = 60 * 60,
            rounds = 1,
            exercises = listOf("1 miglio corsa", "100 trazioni", "200 piegamenti", "300 squat", "1 miglio corsa"),
        )),
        description = "For Time (timecap 60 min): 1 miglio corsa, 100 trazioni, 200 piegamenti, 300 squat, 1 miglio corsa.",
        tags = listOf("for time", "hero", "crossfit"),
        isBuiltIn = true,
        createdAt = now,
    )

    // ── Personal WODs ────────────────────────────────────────────────────────
    // Stable UUIDs match the device DB so insertIfAbsent avoids duplicates.

    private fun wodSolito(json: Json, now: Long) = SavedWod(
        id = "a17bee12-1753-4e92-ad7c-4d433a7d6121",
        name = "WOD SOLITO",
        type = TimerType.TABATA,
        configJson = encode(json, TimerConfig.Tabata(
            series = 8,
            workSeconds = 33,
            restSeconds = 15,
            exercises = listOf(
                "Push-up", "Clean & Jerk", "Pull-up", "GTH kettleball",
                "Dips", "Russian crunch dumbell", "Curl", "Core Superman hollow",
            ),
        )),
        description = "3×8 — 33s lavoro / 15s riposo",
        isBuiltIn = false,
        isFavourite = true,
        createdAt = now,
    )

    private fun wormUp(json: Json, now: Long) = SavedWod(
        id = "7ee9feed-9dff-4f09-9397-0676723182b5",
        name = "WORM-UP",
        type = TimerType.TABATA,
        configJson = encode(json, TimerConfig.Tabata(
            series = 12,
            workSeconds = 20,
            restSeconds = 10,
        )),
        description = "12×20s lavoro / 10s riposo",
        isBuiltIn = false,
        isFavourite = true,
        createdAt = now,
    )

    private fun fullBodyResistanceBand(json: Json, now: Long) = SavedWod(
        id = "bab89276-2632-4207-8678-7e285727eec5",
        name = "Full body resistance band",
        type = TimerType.TABATA,
        configJson = encode(json, TimerConfig.Tabata(
            series = 11,
            workSeconds = 40,
            restSeconds = 15,
            exercises = listOf(
                "Thruster", "Seated row", "Facepull", "Deadlift", "Upright row",
                "Pull aparts", "Squats", "X band shuffle", "Curl sx", "Curl dx",
                "Tricep extension",
            ),
            repeat = com.wod.app.domain.model.WodRepeatConfig(
                wodRounds = 3,
                restBetweenRoundsSeconds = 145,
            ),
        )),
        description = "3×11 — 40s lavoro / 15s riposo / 2:25 riposo tra i round",
        isBuiltIn = false,
        isFavourite = true,
        createdAt = now,
    )

    private fun defaticamentoStretching(json: Json, now: Long) = SavedWod(
        id = "4574c156-3a8e-4781-b367-78cd6a5af0a3",
        name = "D & S",
        type = TimerType.CUSTOM,
        configJson = encode(json, TimerConfig.Custom(
            intervals = listOf(
                CustomInterval("Mobilizzazione colonna",          30),
                CustomInterval("Gamba tallone DX",                30),
                CustomInterval("Gamba tallone SX",                30),
                CustomInterval("Divaricata frontale",             20),
                CustomInterval("Divaricata frontale DX",          30),
                CustomInterval("Divaricata frontale SX",          30),
                CustomInterval("Mobilizzazione colonna",          20),
                CustomInterval("Incrocio Gamba SX",               30),
                CustomInterval("Incrocio Gamba DX",               30),
                CustomInterval("Mobilizzazione colonna",          20),
                CustomInterval("Allungamento Laterale Braccio DX", 30),
                CustomInterval("Allungamento Laterale Braccio SX", 30),
                CustomInterval("Mano su pianta del piede DX",     20),
                CustomInterval("Mano su pianta del piede SX",     20),
                CustomInterval("Mobilizzazione colonna",          20),
                CustomInterval("Spalla DX",                       20),
                CustomInterval("Spalla SX",                       20),
                CustomInterval("Pettorale DX",                    20),
                CustomInterval("Pettorale SX",                    20),
                CustomInterval("Frontale mani a terra",           30),
                CustomInterval("Frontale a V",                    40),
                CustomInterval("Mobilizzazione colonna a terra",  30),
                CustomInterval("Allungamento schiena a terra",    30),
                CustomInterval("Allungamento in affondo DX",      30),
                CustomInterval("Allungamento in affondo SX",      30),
                CustomInterval("Glutei back SX",                  30),
                CustomInterval("Glutei back DX",                  30),
                CustomInterval("Glutei front SX",                 30),
                CustomInterval("Glutei front DX",                 30),
            ),
        )),
        description = "Defaticamento & Streaching",
        isBuiltIn = false,
        isFavourite = true,
        createdAt = now,
    )
}
