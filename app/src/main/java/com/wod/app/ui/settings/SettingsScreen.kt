package com.wod.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wod.app.domain.model.CueCategory
import com.wod.app.ui.theme.ThemeMode
import com.wod.app.ui.theme.WodColors
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme
import kotlinx.coroutines.launch

/** Settings screen: audio controls + theme selection (T45-T46). */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val audioSettings by vm.audioSettings.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val importResult by vm.importResult.collectAsStateWithLifecycle()
    val exportResult by vm.exportResult.collectAsStateWithLifecycle()
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) vm.exportToUri(context, uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) vm.importWods(context, uri)
    }

    LaunchedEffect(exportResult) {
        exportResult?.let { count ->
            val msg = if (count >= 0) "Esportati $count WOD con successo." else "Errore durante l'esportazione."
            scope.launch { snackbarHost.showSnackbar(msg) }
            vm.clearExportResult()
        }
    }

    LaunchedEffect(importResult) {
        importResult?.let { count ->
            val msg = if (count >= 0) "Importati $count WOD con successo." else "Errore durante l'importazione."
            scope.launch { snackbarHost.showSnackbar(msg) }
            vm.clearImportResult()
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = colors.bgPrimary,
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .padding(innerPadding),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.s, vertical = spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro", tint = colors.textPrimary)
            }
            Text("Impostazioni", style = typography.headlineMedium, color = colors.textPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.m),
        ) {
            // ── Audio ───────────────────────────────────────────────────────────
            SectionHeader("Audio", colors, typography)
            SettingsRow(
                label = "Audio attivo",
                subtitle = "Attiva/disattiva tutti i suoni",
                colors = colors,
                typography = typography,
            ) {
                Switch(
                    checked = audioSettings.masterEnabled,
                    onCheckedChange = vm::setMasterEnabled,
                    colors = switchColors(colors),
                )
            }
            if (audioSettings.masterEnabled) {
                Spacer(Modifier.height(spacing.s))
                // Volume slider — local state gives immediate visual feedback
                // while DataStore update propagates in the background.
                var sliderVolume by remember { mutableFloatStateOf(audioSettings.masterVolume) }
                LaunchedEffect(audioSettings.masterVolume) {
                    // Sync when an external change arrives (e.g. fresh app start).
                    sliderVolume = audioSettings.masterVolume
                }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Volume segnali",
                            style = typography.bodyLarge,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${(sliderVolume * 100).toInt()}%",
                            style = typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                    Slider(
                        value = sliderVolume,
                        onValueChange = { v ->
                            sliderVolume = v
                            vm.setMasterVolume(v)
                        },
                        onValueChangeFinished = { vm.playVolumePreview(sliderVolume) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accentTabata,
                            activeTrackColor = colors.accentTabata,
                            inactiveTrackColor = colors.bgElevated,
                        ),
                    )
                }
                SettingsRow(
                    label = "Conto alla rovescia",
                    subtitle = "Bip negli ultimi 3 secondi",
                    colors = colors,
                    typography = typography,
                ) {
                    Switch(
                        checked = audioSettings.countdownEnabled,
                        onCheckedChange = { vm.setCategoryEnabled(CueCategory.COUNTDOWN, it) },
                        colors = switchColors(colors),
                    )
                }
                SettingsRow(
                    label = "A metà fase",
                    subtitle = "Segnale a metà del tempo di lavoro",
                    colors = colors,
                    typography = typography,
                ) {
                    Switch(
                        checked = audioSettings.halfwayEnabled,
                        onCheckedChange = { vm.setCategoryEnabled(CueCategory.HALFWAY, it) },
                        colors = switchColors(colors),
                    )
                }
                SettingsRow(
                    label = "Cambio fase",
                    subtitle = "Segnale a ogni cambio di fase",
                    colors = colors,
                    typography = typography,
                ) {
                    Switch(
                        checked = audioSettings.phaseTransitionEnabled,
                        onCheckedChange = { vm.setCategoryEnabled(CueCategory.PHASE_TRANSITION, it) },
                        colors = switchColors(colors),
                    )
                }
                SettingsRow(
                    label = "Completamento",
                    subtitle = "Segnale a fine allenamento",
                    colors = colors,
                    typography = typography,
                ) {
                    Switch(
                        checked = audioSettings.completionEnabled,
                        onCheckedChange = { vm.setCategoryEnabled(CueCategory.COMPLETION, it) },
                        colors = switchColors(colors),
                    )
                }
            }

            Spacer(Modifier.height(spacing.l))
            HorizontalDivider(color = colors.divider)
            Spacer(Modifier.height(spacing.l))

            // ── Tema ────────────────────────────────────────────────────────────
            SectionHeader("Tema", colors, typography)
            ThemeOption(ThemeMode.DARK, "Scuro", themeMode, colors, typography) { vm.setThemeMode(ThemeMode.DARK) }
            ThemeOption(ThemeMode.LIGHT, "Chiaro", themeMode, colors, typography) { vm.setThemeMode(ThemeMode.LIGHT) }
            ThemeOption(ThemeMode.AUTO, "Automatico", themeMode, colors, typography) { vm.setThemeMode(ThemeMode.AUTO) }

            Spacer(Modifier.height(spacing.l))
            HorizontalDivider(color = colors.divider)
            Spacer(Modifier.height(spacing.l))

            // ── Dati ────────────────────────────────────────────────────────────
            SectionHeader("Dati", colors, typography)
            DataRow(
                label = "Esporta WOD",
                subtitle = "Salva tutti i WOD in un file JSON",
                icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                colors = colors,
                typography = typography,
            ) {
                exportLauncher.launch(vm.suggestedExportFilename())
            }
            Spacer(Modifier.height(spacing.s))
            DataRow(
                label = "Importa WOD",
                subtitle = "Carica WOD da un file JSON (sovrascrive se esistenti)",
                icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                colors = colors,
                typography = typography,
            ) {
                importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
            }

            Spacer(Modifier.height(spacing.xl))
        }
    }
    } // Scaffold
}

@Composable
private fun SectionHeader(
    title: String,
    colors: WodColors,
    typography: com.wod.app.ui.theme.WodTypography,
) {
    Text(
        text = title.uppercase(),
        style = typography.labelSmall,
        color = colors.textDisabled,
        modifier = Modifier.padding(vertical = WodTheme.spacing.s),
    )
}

@Composable
private fun SettingsRow(
    label: String,
    subtitle: String,
    colors: WodColors,
    typography: com.wod.app.ui.theme.WodTypography,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = typography.bodyLarge, color = colors.textPrimary)
            Text(subtitle, style = typography.bodyMedium, color = colors.textSecondary)
        }
        control()
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    label: String,
    current: ThemeMode,
    colors: WodColors,
    typography: com.wod.app.ui.theme.WodTypography,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = current == mode,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accentTabata,
                unselectedColor = colors.textDisabled,
            ),
        )
        Text(label, style = typography.bodyLarge, color = colors.textPrimary)
    }
}

@Composable
private fun DataRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    colors: WodColors,
    typography: com.wod.app.ui.theme.WodTypography,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = typography.bodyLarge, color = colors.textPrimary)
            Text(subtitle, style = typography.bodyMedium, color = colors.textSecondary)
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textDisabled,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun switchColors(colors: WodColors) = SwitchDefaults.colors(
    checkedThumbColor = colors.bgPrimary,
    checkedTrackColor = colors.accentTabata,
    uncheckedThumbColor = colors.textDisabled,
    uncheckedTrackColor = colors.bgElevated,
)

// T04d / T56b — 4-variant previews

@WodPreview
@androidx.compose.runtime.Composable
private fun SettingsScreenPreview() {
    WodTheme { SettingsScreen(onBack = {}) }
}

