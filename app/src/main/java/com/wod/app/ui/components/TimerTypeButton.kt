package com.wod.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wod.app.ui.theme.WodTheme

/**
 * Full-width pill button for a timer type.
 *
 * @param label Human-readable timer type name (e.g. "AMRAP").
 * @param background Pill background color from [WodColors].
 * @param onClick Invoked when the button is tapped.
 */
@Composable
fun TimerTypeButton(
    label: String,
    background: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = Color.White,
        ),
    ) {
        Text(label, style = WodTheme.typography.titleMedium)
    }
}
