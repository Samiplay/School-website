package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BagItemEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.SchoolClassEntity
import com.example.ui.components.HubCheckRow
import com.example.ui.components.HubDatePickerField
import com.example.ui.components.HubDropdownSelector
import com.example.ui.components.HubSectionCard
import com.example.ui.components.HubTagChip
import com.example.ui.components.HubTimePickerField
import com.example.ui.components.SubTabBar
import com.example.ui.components.SummaryPillData
import com.example.ui.components.SummaryPillRow
import com.example.ui.theme.LocalHubColors
import com.example.ui.viewmodel.HubUiState
import com.example.ui.viewmodel.HubViewModel
import com.example.ui.viewmodel.SchoolTab
import com.example.util.DateUtils
import com.example.util.DueUrgency

@Composable
fun SchoolScreen(
    uiState: HubUiState,
    viewModel: HubViewModel,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    val tomorrowKey = remember { DateUtils.tomorrowKey() }
    val tomorrowDayIndex = remember { DateUtils.tomorrowDayIndex() }
    val tomorrowDayName = remember { DateUtils.tomorrowDayName() }
    val tomorrowSubtitle = remember { DateUtils.tomorrowSubtitle() }

    val tomorrowClasses = remember(uiState.classes, tomorrowDayIndex) {
        uiState.classes.filter { it.dayOfWeek == tomorrowDayIndex }.sortedBy { it.time }
    }
    val dueTomorrowCount = remember(uiState.homework, tomorrowKey) {
        uiState.homework.count { !it.done && it.dueDate.isNotBlank() && it.dueDate <= tomorrowKey }
    }
    val toStudyCount = remember(uiState.studyTopics) {
        uiState.studyTopics.count { !it.done }
    }

    val selectedTab = SchoolTab.fromKey(uiState.prefs.schoolTab)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Editorial Hero Header
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Tomorrow ",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = tomorrowDayName,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.drawBehind {
                        val top = size.height * 0.58f
                        val bottom = size.height * 0.92f
                        drawRect(
                            color = hubColors.highlight,
                            topLeft = Offset(-4f, top),
                            size = Size(size.width + 8f, bottom - top)
                        )
                    }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = tomorrowSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = hubColors.muted
            )
        }

        SummaryPillRow(
            pills = listOf(
                SummaryPillData(tomorrowClasses.size.toString(), "classes"),
                SummaryPillData(dueTomorrowCount.toString(), "due tomorrow"),
                SummaryPillData(toStudyCount.toString(), "to study")
            )
        )

        SubTabBar(
            items = listOf(
                SchoolTab.TOMORROW to stringResource(R.string.tab_tomorrow),
                SchoolTab.WORK to stringResource(R.string.tab_homework),
                SchoolTab.EXAMS to stringResource(R.string.tab_exams)
            ),
            selected = selectedTab,
            onSelect = { viewModel.selectSchoolTab(it) },
            testTagPrefix = "school_tab"
        )

        when (selectedTab) {
            SchoolTab.TOMORROW -> TomorrowTabContent(
                tomorrowDayName = tomorrowDayName,
                tomorrowKey = tomorrowKey,
                tomorrowClasses = tomorrowClasses,
                allClasses = uiState.classes,
                bagItems = uiState.bagItems,
                viewModel = viewModel
            )
            SchoolTab.WORK -> HomeworkTabContent(
                homework = uiState.homework,
                classes = uiState.classes,
                studyTopics = uiState.studyTopics,
                exams = uiState.exams,
                tomorrowKey = tomorrowKey,
                viewModel = viewModel
            )
            SchoolTab.EXAMS -> SchoolExamsTab(
                exams = uiState.exams,
                studyTopics = uiState.studyTopics,
                timer = uiState.timer,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun TomorrowTabContent(
    tomorrowDayName: String,
    tomorrowKey: String,
    tomorrowClasses: List<SchoolClassEntity>,
    allClasses: List<SchoolClassEntity>,
    bagItems: List<BagItemEntity>,
    viewModel: HubViewModel
) {
    val hubColors = LocalHubColors.current
    var timetableExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedDayIndex by rememberSaveable { mutableIntStateOf(1) } // Monday default
    var classTime by rememberSaveable { mutableStateOf("08:30") }
    var className by rememberSaveable { mutableStateOf("") }
    var bagInput by rememberSaveable { mutableStateOf("") }

    HubSectionCard(
        title = "Classes",
        subtitle = "Tomorrow's schedule"
    ) {
        if (tomorrowClasses.isEmpty()) {
            Text(
                text = "No classes added for $tomorrowDayName. Open the timetable below to add them.",
                style = MaterialTheme.typography.bodyMedium,
                color = hubColors.muted
            )
        } else {
            tomorrowClasses.forEachIndexed { idx, cls ->
                if (idx > 0) HorizontalDivider(color = hubColors.line)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.format12Hour(cls.time),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(76.dp)
                    )
                    Text(
                        text = cls.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clickable { timetableExpanded = !timetableExpanded }
                .testTag("toggle_timetable_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (timetableExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = "Edit weekly timetable",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Edit weekly timetable",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(visible = timetableExpanded) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubDropdownSelector(
                        label = "Day",
                        options = DateUtils.WEEK_ORDER.map { DateUtils.DAY_NAMES[it] },
                        selectedOption = DateUtils.DAY_NAMES[selectedDayIndex],
                        onSelect = { dayName ->
                            val idx = DateUtils.DAY_NAMES.indexOf(dayName)
                            if (idx >= 0) selectedDayIndex = idx
                        },
                        testTag = "timetable_day_selector",
                        modifier = Modifier.weight(1f)
                    )
                    HubTimePickerField(
                        time24 = classTime,
                        onTimeSelected = { classTime = it },
                        testTag = "timetable_time_picker"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { if (it.length <= 60) className = it },
                        label = { Text("Subject (and room)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("timetable_subject_input")
                    )
                    Button(
                        onClick = {
                            viewModel.addClass(selectedDayIndex, classTime, className)
                            className = ""
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_class_button")
                    ) {
                        Text("Add class")
                    }
                }

                DateUtils.WEEK_ORDER.forEach { dayIdx ->
                    val dayClasses = allClasses.filter { it.dayOfWeek == dayIdx }.sortedBy { it.time }
                    if (dayClasses.isNotEmpty()) {
                        Text(
                            text = DateUtils.DAY_NAMES[dayIdx],
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        dayClasses.forEach { cls ->
                            HorizontalDivider(color = hubColors.line)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.format12Hour(cls.time),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(74.dp)
                                )
                                Text(
                                    text = cls.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteClass(cls.id) },
                                    modifier = Modifier.testTag("delete_class_${cls.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Delete class",
                                        tint = hubColors.muted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    HubSectionCard(
        title = "Pack your bag",
        subtitle = "Resets daily"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = bagInput,
                onValueChange = { if (it.length <= 60) bagInput = it },
                label = { Text("Add an item, like PE kit") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("bag_item_input")
            )
            Button(
                onClick = {
                    viewModel.addBagItem(bagInput)
                    bagInput = ""
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_bag_item_button")
            ) {
                Text("Add item")
            }
        }

        Spacer(Modifier.height(10.dp))
        if (bagItems.isEmpty()) {
            Text(
                text = "List what goes in your bag. Checks reset every day.",
                style = MaterialTheme.typography.bodyMedium,
                color = hubColors.muted
            )
        } else {
            bagItems.forEachIndexed { idx, item ->
                if (idx > 0) HorizontalDivider(color = hubColors.line)
                HubCheckRow(
                    text = item.text,
                    checked = item.doneDate == tomorrowKey,
                    onToggle = { viewModel.toggleBagItem(item, tomorrowKey) },
                    onDelete = { viewModel.deleteBagItem(item.id) },
                    toggleTestTag = "toggle_bag_${item.id}",
                    deleteTestTag = "delete_bag_${item.id}"
                )
            }
        }
    }
}

@Composable
private fun HomeworkTabContent(
    homework: List<HomeworkEntity>,
    classes: List<SchoolClassEntity>,
    studyTopics: List<com.example.data.local.StudyTopicEntity>,
    exams: List<com.example.data.repository.ExamItem>,
    tomorrowKey: String,
    viewModel: HubViewModel
) {
    val hubColors = LocalHubColors.current
    var hwInput by rememberSaveable { mutableStateOf("") }
    var hwDue by rememberSaveable { mutableStateOf(tomorrowKey) }

    HubSectionCard(title = "Homework and to do") {
        OutlinedTextField(
            value = hwInput,
            onValueChange = { if (it.length <= 120) hwInput = it },
            label = { Text("What do you need to do?") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("homework_input")
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubDatePickerField(
                label = "Due date",
                dateKey = hwDue,
                onDateSelected = { hwDue = it },
                allowClear = true,
                testTag = "homework_due_picker",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.addHomework(hwInput, hwDue)
                    hwInput = ""
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_homework_button")
            ) {
                Text("Add task")
            }
        }

        Spacer(Modifier.height(12.dp))
        if (homework.isEmpty()) {
            Text(
                text = "All clear. Add anything you need to hand in.",
                style = MaterialTheme.typography.bodyMedium,
                color = hubColors.muted
            )
        } else {
            homework.forEachIndexed { idx, item ->
                if (idx > 0) HorizontalDivider(color = hubColors.line)
                val (tagText, urgency) = DateUtils.dueLabel(item.dueDate)
                HubCheckRow(
                    text = item.text,
                    checked = item.done,
                    onToggle = { viewModel.toggleHomework(item) },
                    onDelete = { viewModel.deleteHomework(item.id) },
                    tagText = tagText,
                    tagUrgency = urgency,
                    toggleTestTag = "toggle_hw_${item.id}",
                    deleteTestTag = "delete_hw_${item.id}"
                )
            }
        }
    }

    val weekSlots = remember { DateUtils.weekAheadDays() }
    HubSectionCard(
        title = "Week ahead",
        subtitle = "Next 7 days"
    ) {
        weekSlots.forEachIndexed { idx, slot ->
            if (idx > 0) HorizontalDivider(color = hubColors.line)
            val classCount = classes.count { it.dayOfWeek == slot.dayOfWeek }
            val dueItems = homework.filter { !it.done && it.dueDate == slot.dateKey }
            val testNames = studyTopics.filter { !it.done && it.testDate == slot.dateKey }.map { it.text } +
                exams.filter { it.date == slot.dateKey }.map { "${it.subject} exam" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = slot.shortLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(68.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$classCount class${if (classCount == 1) "" else "es"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (dueItems.isNotEmpty()) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Due: ") }
                                append(dueItems.joinToString(", ") { it.text })
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = hubColors.muted
                        )
                    }
                    if (testNames.isNotEmpty()) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Test: ") }
                                append(testNames.joinToString(", "))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = hubColors.muted
                        )
                    }
                }
                if (testNames.isNotEmpty()) {
                    HubTagChip(text = "Test", urgency = DueUrgency.SOON)
                }
            }
        }
    }
}
