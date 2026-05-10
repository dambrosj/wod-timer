package com.wod.app.ui.wods

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Saved WODs library with search, type filter, and quick-start (T44a-T44e). */
@Composable
fun WodsLibraryScreen(
    onBack: () -> Unit,
    onWodClick: (String) -> Unit,
    onEditWod: (String) -> Unit = {},
    onStartTimer: (typeName: String) -> Unit = {},
) {
    val vm: WodsViewModel = viewModel()
    val filteredWods by vm.filteredWods.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val selectedType by vm.selectedType.collectAsStateWithLifecycle()
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing

    var wodToDelete by remember { mutableStateOf<SavedWod?>(null) }

    if (wodToDelete != null) {
        AlertDialog(
            onDismissRequest = { wodToDelete = null },
            title = { Text("Elimina WOD", style = typography.titleMedium, color = colors.textPrimary) },
            text = { Text("Sei sicuro di voler eliminare «${wodToDelete?.name}»?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = { vm.delete(wodToDelete!!); wodToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
                ) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { wodToDelete = null }) { Text("Annulla", color = colors.textSecondary) }
            },
            containerColor = colors.bgSurface,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
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
            Text(
                text = "I miei WOD",
                style = typography.headlineMedium,
                color = colors.textPrimary,
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = vm::setSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.m),
            placeholder = { Text("Cerca WOD…", color = colors.textDisabled) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.textSecondary) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { vm.setSearchQuery("") }) { Icon(Icons.Default.Close, null, tint = colors.textSecondary) } }
            } else null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedBorderColor = colors.accentTabata,
                unfocusedBorderColor = colors.divider,
                cursorColor = colors.accentTabata,
            ),
        )
        Spacer(Modifier.height(spacing.s))

        // Type filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.m),
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { vm.setTypeFilter(null) },
                    label = { Text("Tutti") },
                    colors = chipColors(selectedType == null, colors),
                )
            }
            items(TimerType.entries.filter { it != TimerType.MIX }) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { vm.setTypeFilter(if (selectedType == type) null else type) },
                    label = { Text(type.displayName()) },
                    colors = chipColors(selectedType == type, colors),
                )
            }
        }
        Spacer(Modifier.height(spacing.s))

        if (filteredWods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Bookmarks,
                        contentDescription = null,
                        tint = colors.textDisabled,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(spacing.m))
                    Text(
                        text = "Nessun WOD trovato",
                        style = typography.bodyLarge,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(spacing.m),
                verticalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                items(filteredWods, key = { it.id }) { wod ->
                    SavedWodCard(
                        wod = wod,
                        onClick = { vm.selectWod(wod); onWodClick(wod.id) },
                        onAvvia = { vm.startWod(wod) { typeName -> onStartTimer(typeName) } },
                        onToggleFavourite = { vm.toggleFavourite(wod) },
                        onDuplicate = { vm.duplicate(wod) },
                        onEdit = { vm.startEditWod(wod) { id -> onEditWod(id) } },
                        onDelete = { if (!wod.isBuiltIn) wodToDelete = wod },
                    )
                }
            }
        }
    }
}

/** Full WOD detail view with stats and AVVIA CTA (T44d). */
@Composable
fun WodDetailScreen(
    id: String,
    onBack: () -> Unit,
    onStartTimer: (typeName: String) -> Unit = {},
) {
    val vm: WodsViewModel = viewModel()
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing

    // Use the wod selected before navigating here (set by WodsLibraryScreen)
    val wod = remember(id) {
        // fallback: fetch from allFlow is async; use cached selectedWod
        null as SavedWod?
    }.let {
        // Read from WodApp.selectedWod (set synchronously before navigation)
        val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.wod.app.WodApp
        app.selectedWod
    } ?: run { onBack(); return }

    val accent = wod.type.accentColor(colors)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
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
            Text(
                text = wod.name,
                style = typography.headlineMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
        }

        Column(modifier = Modifier.padding(horizontal = spacing.m)) {
            TypeBadge(wod.type, colors)
            Spacer(Modifier.height(spacing.m))

            if (wod.description.isNotBlank()) {
                Text(wod.description, style = typography.bodyLarge, color = colors.textSecondary)
                Spacer(Modifier.height(spacing.m))
            }

            // Stats
            if (wod.timesUsed > 0) {
                Text(
                    text = "Usato ${wod.timesUsed}× · Ultimo uso: ${wod.lastUsedAt?.let { formatDate(it) } ?: "—"}",
                    style = typography.bodyMedium,
                    color = colors.textDisabled,
                )
                Spacer(Modifier.height(spacing.m))
            }

            // AVVIA CTA
            Button(
                onClick = { vm.startWod(wod) { typeName -> onStartTimer(typeName) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = colors.bgPrimary,
                ),
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(spacing.s))
                Text("AVVIA", style = typography.titleMedium)
            }
        }
    }
}

@Composable
private fun chipColors(selected: Boolean, colors: com.wod.app.ui.theme.WodColors) =
    FilterChipDefaults.filterChipColors(
        selectedContainerColor  = colors.accentTabata.copy(alpha = 0.15f),
        selectedLabelColor      = colors.accentTabata,
        containerColor          = colors.bgSurface,
        labelColor              = colors.textSecondary,
    )

private fun formatDate(ms: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(ms))

// T04d / T56b — 4-variant previews

@WodPreview
@androidx.compose.runtime.Composable
private fun WodsLibraryScreenPreview() {
    WodTheme { WodsLibraryScreen(onBack = {}, onWodClick = {}, onStartTimer = {}) }
}

