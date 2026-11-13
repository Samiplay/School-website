package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalHubColors
import com.example.ui.viewmodel.MainSection
import com.example.util.DateUtils
import com.example.util.DueUrgency
import java.util.Locale

@Composable
fun HubTopBar(
    activeSection: MainSection,
    isDark: Boolean,
    onSelectSection: (MainSection) -> Unit,
    onToggleTheme: () -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, hubColors.line),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 6.dp)
            )

            // Segmented section switcher
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, hubColors.line)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MainSection.entries.forEach { section ->
                        val selected = section == activeSection
                        val label = if (section == MainSection.SCHOOL) {
                            stringResource(R.string.nav_school)
                        } else {
                            stringResource(R.string.nav_money)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { onSelectSection(section) }
                                .minimumInteractiveComponentSize()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("nav_${section.key}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    hubColors.muted
                                }
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(R.string.action_toggle_theme),
                        tint = hubColors.muted
                    )
                }
                IconButton(
                    onClick = onLock,
                    modifier = Modifier.testTag("lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = stringResource(R.string.action_lock),
                        tint = hubColors.muted
                    )
                }
            }
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
    val hubColors = LocalHubColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pills.forEach { pill ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, hubColors.line)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = pill.value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pill.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = hubColors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun <T> SubTabBar(
    items: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, hubColors.line)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { (item, label) ->
                val isSelected = item == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onSelect(item) }
                        .minimumInteractiveComponentSize()
                        .padding(horizontal = 6.dp, vertical = 10.dp)
                        .testTag("${testTagPrefix}_${label.lowercase().replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            hubColors.muted
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun HubSectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val hubColors = LocalHubColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, hubColors.line)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            color = hubColors.muted
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun HubProgressBar(
    progress: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    heightDp: Int = 10,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 250),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(hubColors.line)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(99.dp))
                .background(color)
        )
    }
}

@Composable
fun HubTagChip(
    text: String,
    urgency: DueUrgency,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    val bgColor = when (urgency) {
        DueUrgency.SOON -> hubColors.highlight
        DueUrgency.LATE -> MaterialTheme.colorScheme.background
        DueUrgency.NORMAL -> MaterialTheme.colorScheme.background
    }
    val borderColor = when (urgency) {
        DueUrgency.SOON -> hubColors.highlight
        DueUrgency.LATE -> hubColors.warn
        DueUrgency.NORMAL -> hubColors.line
    }
    val textColor = when (urgency) {
        DueUrgency.SOON -> hubColors.highlightInk
        DueUrgency.LATE -> hubColors.warn
        DueUrgency.NORMAL -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun HubCheckRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    tagText: String? = null,
    tagUrgency: DueUrgency = DueUrgency.NORMAL,
    toggleTestTag: String,
    deleteTestTag: String,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clickable(onClick = onToggle)
                .testTag(toggleTestTag),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (checked) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (checked) hubColors.done else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (!tagText.isNullOrBlank()) {
            HubTagChip(
                text = tagText,
                urgency = if (checked) DueUrgency.NORMAL else tagUrgency
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag(deleteTestTag)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Delete",
                tint = hubColors.muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubDropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag(testTag)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubDatePickerField(
    label: String,
    dateKey: String,
    onDateSelected: (String) -> Unit,
    allowClear: Boolean = false,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val hubColors = LocalHubColors.current
    val display = if (dateKey.isBlank()) label else DateUtils.formatShortDate(dateKey)

    OutlinedButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, hubColors.line),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = hubColors.muted
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = display,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (showDialog) {
        val initialMillis = DateUtils.parseKey(dateKey)?.let {
            // Align UTC millis for DatePicker
            val utc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            utc.set(
                it.get(java.util.Calendar.YEAR),
                it.get(java.util.Calendar.MONTH),
                it.get(java.util.Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
            utc.set(java.util.Calendar.MILLISECOND, 0)
            utc.timeInMillis
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onDateSelected(DateUtils.dateKeyFromMillis(millis))
                        }
                        showDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (allowClear && dateKey.isNotBlank()) {
                        TextButton(
                            onClick = {
                                onDateSelected("")
                                showDialog = false
                            }
                        ) { Text("Clear") }
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
fun HubTimePickerField(
    time24: String,
    onTimeSelected: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val hubColors = LocalHubColors.current
    OutlinedButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, hubColors.line),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = "Select time",
            modifier = Modifier.size(16.dp),
            tint = hubColors.muted
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = DateUtils.format12Hour(time24),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (showDialog) {
        val parts = time24.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
        val timeState = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Class start time", style = MaterialTheme.typography.titleMedium) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formatted = String.format(
                            Locale.US,
                            "%02d:%02d",
                            timeState.hour,
                            timeState.minute
                        )
                        onTimeSelected(formatted)
                        showDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
