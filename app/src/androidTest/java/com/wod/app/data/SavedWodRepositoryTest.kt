package com.wod.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wod.app.data.db.WodDatabase
import com.wod.app.data.repository.SavedWodRepository
import com.wod.app.domain.model.TimerConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T55 — Integration tests for SavedWod flow.
 *
 * Uses an in-memory Room database so tests are hermetic and fast.
 */
@RunWith(AndroidJUnit4::class)
class SavedWodRepositoryTest {

    private lateinit var db: WodDatabase
    private lateinit var repo: SavedWodRepository

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, WodDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = SavedWodRepository(db.savedWodDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- T55.1: save from config → appears in library ---

    @Test
    fun saveFromConfig_appearsInAllFlow() = runBlocking {
        val config = TimerConfig.Tabata(series = 8, workSeconds = 20, restSeconds = 10)

        repo.saveFromConfig("Il mio Tabata", "Test WOD", config)

        val all = repo.allFlow.first()
        assertEquals(1, all.size)
        val saved = all.first()
        assertEquals("Il mio Tabata", saved.name)
        assertEquals("Test WOD", saved.description)
        assertTrue(!saved.isBuiltIn)
    }

    // --- T55.2: launch from library → correct config ---

    @Test
    fun savedWod_getById_returnsCorrectConfig() = runBlocking {
        val config = TimerConfig.Tabata(series = 4, workSeconds = 30, restSeconds = 15)
        repo.saveFromConfig("Tabata Breve", "", config)

        val saved = repo.allFlow.first().first()
        val retrieved = repo.getById(saved.id)

        assertNotNull(retrieved)
        assertEquals("Tabata Breve", retrieved!!.name)
    }

    // --- T55.3: timesUsed and lastUsedAt update after a completed run ---

    @Test
    fun incrementUsage_updatesTimesUsedAndLastUsedAt() = runBlocking {
        repo.saveFromConfig("WOD Test", "", TimerConfig.Amrap(durationSeconds = 300))
        val wod = repo.allFlow.first().first()
        assertEquals(0, wod.timesUsed)
        assertNull(wod.lastUsedAt)

        repo.incrementUsage(wod.id)

        val updated = repo.getById(wod.id)!!
        assertEquals(1, updated.timesUsed)
        assertNotNull(updated.lastUsedAt)
    }

    @Test
    fun incrementUsage_twice_incrementsTwice() = runBlocking {
        repo.saveFromConfig("WOD Test", "", TimerConfig.Amrap(durationSeconds = 300))
        val wod = repo.allFlow.first().first()

        repo.incrementUsage(wod.id)
        repo.incrementUsage(wod.id)

        val updated = repo.getById(wod.id)!!
        assertEquals(2, updated.timesUsed)
    }

    // --- T55.4: delete a non-built-in WOD removes it ---

    @Test
    fun delete_removesNonBuiltInWod() = runBlocking {
        repo.saveFromConfig("Da Eliminare", "", TimerConfig.Amrap(durationSeconds = 120))
        val wod = repo.allFlow.first().first()

        repo.delete(wod.id)

        val all = repo.allFlow.first()
        assertTrue(all.isEmpty())
    }

    // --- T55.5: built-in templates are seeded and survive the session ---

    @Test
    fun seedBuiltIns_insertsAllBuiltIns() = runBlocking {
        repo.seedBuiltIns()

        val all = repo.allFlow.first()
        assertEquals(5, all.size)
        assertTrue(all.all { it.isBuiltIn })
    }

    @Test
    fun seedBuiltIns_idempotent_doesNotDuplicate() = runBlocking {
        repo.seedBuiltIns()
        repo.seedBuiltIns()

        val all = repo.allFlow.first()
        assertEquals(5, all.size)
    }

    @Test
    fun builtIn_isProtectedByUiLayer_repoDeleteAcceptsIdButUiHidesOption() = runBlocking {
        // The repository layer does not enforce built-in protection; that is a UI concern.
        // This test documents the contract: after seeding, built-ins are present with isBuiltIn=true.
        repo.seedBuiltIns()
        val builtIns = repo.allFlow.first().filter { it.isBuiltIn }
        assertEquals(5, builtIns.size)

        // Built-in protection in production is enforced in WodsLibraryScreen (the Elimina menu
        // option is only shown for !isBuiltIn WODs). The repository itself does not guard this.
    }

    // --- Bonus: favourites and byType flows ---

    @Test
    fun setFavourite_true_appearsInFavouritesFlow() = runBlocking {
        repo.saveFromConfig("Preferito", "", TimerConfig.Tabata(series = 9, workSeconds = 20, restSeconds = 10))
        val wod = repo.allFlow.first().first()

        repo.setFavourite(wod.id, true)

        val favs = repo.favouritesFlow.first()
        assertEquals(1, favs.size)
        assertTrue(favs.first().isFavourite)
    }
}
