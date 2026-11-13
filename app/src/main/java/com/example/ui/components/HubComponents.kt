package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BricolageGrotesqueFamily
import com.example.ui.theme.LocalHubPalette
import com.example.ui.viewmodel.HubFormatters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun HubCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalHubPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.line)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun HubSectionHeader(
    title: String,
    subtitle: String? = null
) {
    val palette = LocalHubPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = palette.ink
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = palette.muted
            )
        }
    }
}

data class SummaryPillData(
    val value: String,
    val label: String
)

@Composable
fun SummaryPillRow(
    pills: List<SummaryPillData>,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pills.forEach { pill ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.line)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = pill.value,
                        fontFamily = BricolageGrotesqueFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        lineHeight = 25.sp,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pill.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = palette.muted,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun HubSegmentedTabs(
    tabs: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "tab"
) {
    val palette = LocalHubPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.line)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEach { (key, label) ->
                val selected = key == selectedKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) palette.accent else Color.Transparent)
                        .clickable(role = Role.Tab) { onSelect(key) }
                        .testTag("${testTagPrefix}_$key")
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) palette.accentInk else palette.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun HubPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalHubPalette.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.accent,
            contentColor = palette.accentInk
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HubAltButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, palette.line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.bg,
            contentColor = palette.ink
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLength: Int = 120,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    val palette = LocalHubPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        modifier = modifier,
        placeholder = { Text(placeholder, color = palette.muted) },
        singleLine = singleLine,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = palette.bg,
            unfocusedContainerColor = palette.bg,
            focusedBorderColor = palette.accent,
            unfocusedBorderColor = palette.line,
            focusedTextColor = palette.ink,
            unfocusedTextColor = palette.ink,
            cursorColor = palette.accent
        )
    )
}

@Composable
fun HubCheckboxButton(
    checked: Boolean,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (checked) palette.accent else Color.Transparent)
                .border(2.dp, palette.accent, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = palette.accentInk,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HubDeleteButton(
    onClick: () -> Unit,
    contentDesc: String = "Delete",
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = contentDesc,
            tint = palette.muted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun HubTag(
    text: String,
    style: String = "" // "soon", "late", ""
) {
    val palette = LocalHubPalette.current
    val bgColor = when (style) {
        "soon" -> palette.highlight
        else -> palette.bg
    }
    val borderColor = when (style) {
        "soon" -> palette.highlight
        "late" -> palette.warn
        else -> palette.line
    }
    val textColor = when (style) {
        "soon" -> palette.highlightInk
        "late" -> palette.warn
        else -> palette.ink
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}

@Composable
fun HubProgressBar(
    progress: Float,
    barColor: Color,
    heightDp: Int = 8,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(palette.line)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(99.dp))
                .background(barColor)
        )
    }
}

@Composable
fun HubDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            color = palette.bg,
            border = BorderStroke(1.dp, palette.line)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedOption.ifBlank { label },
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select $label",
                    tint = palette.muted
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(palette.surface)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, color = palette.ink) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubDatePickerButton(
    dateKey: String,
    placeholder: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false
) {
    val palette = LocalHubPalette.current
    var showDialog by remember { mutableStateOf(false) }
    val displayText = if (dateKey.isBlank()) placeholder else HubFormatters.formatShortDate(dateKey)

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { showDialog = true },
        shape = RoundedCornerShape(10.dp),
        color = palette.bg,
        border = BorderStroke(1.dp, palette.line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = palette.muted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (dateKey.isBlank()) palette.muted else palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showDialog) {
        val initialMillis = (HubFormatters.parseDateOrNull(dateKey) ?: LocalDate.now())
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateSelected(picked.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (allowClear && dateKey.isNotBlank()) {
                        TextButton(onClick = {
                            onDateSelected("")
                            showDialog = false
                        }) { Text("Clear") }
                    }
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubTimePickerButton(
    time24: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { showDialog = true },
        shape = RoundedCornerShape(10.dp),
        color = palette.bg,
        border = BorderStroke(1.dp, palette.line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = palette.muted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = HubFormatters.formatTime12h(time24),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.ink,
                maxLines = 1
            )
        }
    }

    if (showDialog) {
        val parts = time24.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
        val state = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val hh = state.hour.toString().padStart(2, '0')
                    val mm = state.minute.toString().padStart(2, '0')
                    onTimeSelected("$hh:$mm")
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) }
        )
    }
}

@Composable
fun HubRowDivider() {
    HorizontalDivider(color = LocalHubPalette.current.line, thickness = 1.dp)
}
