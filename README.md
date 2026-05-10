# WOD

Android-native interval timer for CrossFit / HIIT workouts (AMRAP, FOR TIME, EMOM, TABATA, MIX) with WOD repetitions, per-series exercises, saved WODs and decoupled in-app audio cues.

> **Continuing the project from a fresh chat or onboarding a new dev?** Start with **[agent/HANDOFF.md](agent/HANDOFF.md)** — it covers what's already built, what's pending, the conventions to follow and the recommended next 5 tasks.
>
> Full product specification: [agent/PRD.md](agent/PRD.md)

## Stack

- Kotlin 2.0.21
- Jetpack Compose (BOM 2024.10.01)
- Navigation Compose, Material 3 + WindowSizeClass
- Room 2.6.1 (KSP)
- DataStore Preferences 1.1.1
- kotlinx.coroutines 1.9.0, kotlinx.serialization 1.7.3
- Min SDK 26 · Target SDK 35 · Java 17

## Module layout

```
app/src/main/java/com/wod/app/
├── MainActivity.kt                  # single Activity, hosts WodTheme + NavGraph
├── WodApp.kt                        # Application — service-locator style
├── ui/
│   ├── WodNavGraph.kt               # all routes
│   ├── Routes.kt
│   ├── theme/                       # Color, Typography, Shapes, Theme
│   ├── home/HomeScreen.kt           # 5 timer-type buttons
│   ├── config/ConfigScreen.kt       # stub (T21–T27)
│   ├── timer/TimerRunningScreen.kt  # stub (T28+)
│   ├── completion/CompletionScreen.kt
│   ├── diary/DiaryScreen.kt
│   ├── wods/WodsLibraryScreen.kt    # I miei WOD
│   ├── settings/SettingsScreen.kt
│   └── components/Placeholder.kt
├── domain/
│   ├── model/                       # TimerType, TimerConfig, TimerPhase, AudioCue, SavedWod, WorkoutLog
│   └── engine/                      # TimerEngine + TabataEngine scaffold
├── data/
│   ├── audio/                       # AudioCueManager + SoundPool impl
│   ├── db/                          # Room (to come)
│   ├── datastore/                   # DataStore (to come)
│   └── repository/                  # Repos (to come)
└── service/TimerForegroundService.kt
```

## Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug   # device or emulator connected
```

If Android Studio asks for an SDK location, edit `local.properties` (already present) and point `sdk.dir` to your Android SDK.

## Status

This is **scaffold-stage**: project structure, design system, domain models, navigation graph and entry-point screens are in place. Engine ticking, real screens, persistence and audio samples are tracked as the T-tasks in [agent/PRD.md §8](agent/PRD.md).

## Next up

Pick the next unchecked task in PRD §8. The recommended order is the Phase numbering — Phase 2 (data layer) → Phase 3 (engines) → Phase 5–7 (UI) → Phase 8–11.
