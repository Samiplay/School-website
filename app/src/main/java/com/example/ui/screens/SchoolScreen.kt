package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExamEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.PackItemEntity
import com.example.data.local.StudyTopicEntity
import com.example.ui.components.HubAltButton
import com.example.ui.components.HubCard
import com.example.ui.components.HubCheckboxButton
import com.example.ui.components.HubDatePickerButton
import com.example.ui.components.HubDeleteButton
import com.example.ui.components.HubDropdownField
import com.example.ui.components.HubPrimaryButton
import com.example.ui.components.HubProgressBar
import com.example.ui.components.HubRowDivider
import com.example.ui.components.HubSectionHeader
import com.example.ui.components.HubSegmentedTabs
import com.example.ui.components.HubTag
import com.example.ui.components.HubTextField
import com.example.ui.components.HubTimePickerButton
import com.example.ui.components.SummaryPillData
import com.example.ui.components.SummaryPillRow
import com.example.ui.theme.BricolageGrotesqueFamily
import com.example.ui.theme.LocalHubPalette
import com.example.ui.viewmodel.ExamDisplayItem
import com.example.ui.viewmodel.HubFormatters
import com.example.ui.viewmodel.HubUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchoolScreen(
    state: HubUiState,
    onSelectTab: (String) -> Unit,
    onAddClass: (Int, String, String) -> Unit,
    onDeleteClass: (Long) -> Unit,
    onAddPackItem: (String) -> Unit,
    onTogglePackItem: (PackItemEntity) -> Unit,
    onDeletePackItem: (Long) -> Unit,
    onAddHomework: (String, String) -> Unit,
    onToggleHomework: (HomeworkEntity) -> Unit,
    onDeleteHomework: (Long) -> Unit,
    onAddExam: (String, String, String, String) -> Unit,
    onToggleExamTopic: (ExamEntity, Int) -> Unit,
    onDeleteExam: (Long) -> Unit,
    onAddStudyTopic: (String, String) -> Unit,
    onToggleStudyTopic: (StudyTopicEntity) -> Unit,
    onDeleteStudyTopic: (Long) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSwapTimerMode: () -> Unit
) {
    val palette = LocalHubPalette.current
    val tomorrowDayName = HubFormatters.DAY_NAMES[state.tomorrowDayOfWeek]

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Header
        Column(modifier = Modifier.padding(top = 4.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tomorrow",
                    style = MaterialTheme.typography.displayLarge,
                    color = palette.ink
                )
                Text(
                    text = tomorrowDayName,
                    style = MaterialTheme.typography.displayLarge,
                    color = palette.ink,
                    modifier = Modifier.drawBehind {
                        val topY = size.height * 0.58f
                        val bottomY = size.height * 0.92f
                        drawRect(
                            color = palette.highlight,
                            topLeft = Offset(-4f, topY),
                            size = Size(size.width + 8f, bottomY - topY)
                        )
                    }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${HubFormatters.formatMonthDay(state.tomorrowDate)}. Here is what to get ready tonight.",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted
            )
        }

        // Summary Pills
        SummaryPillRow(
            pills = listOf(
                SummaryPillData(state.tomorrowClasses.size.toString(), "classes"),
                SummaryPillData(state.dueTomorrowCount.toString(), "due tomorrow"),
                SummaryPillData(state.toStudyCount.toString(), "to study")
            )
        )

        // Sub-Tabs
        HubSegmentedTabs(
            tabs = listOf(
                "tomorrow" to "Tomorrow",
                "work" to "Homework",
                "exams" to "Exams & Study"
            ),
            selectedKey = state.schoolTab,
            onSelect = onSelectTab,
            testTagPrefix = "school_tab"
        )

        when (state.schoolTab) {
            "tomorrow" -> TomorrowTabContent(
                state = state,
                tomorrowDayName = tomorrowDayName,
                onAddClass = onAddClass,
                onDeleteClass = onDeleteClass,
                onAddPackItem = onAddPackItem,
                onTogglePackItem = onTogglePackItem,
                onDeletePackItem = onDeletePackItem
            )
            "work" -> HomeworkTabContent(
                state = state,
                onAddHomework = onAddHomework,
                onToggleHomework = onToggleHomework,
                onDeleteHomework = onDeleteHomework
            )
            else -> ExamsAndStudyTabContent(
                state = state,
                onAddExam = onAddExam,
                onToggleExamTopic = onToggleExamTopic,
                onDeleteExam = onDeleteExam,
                onAddStudyTopic = onAddStudyTopic,
                onToggleStudyTopic = onToggleStudyTopic,
                onDeleteStudyTopic = onDeleteStudyTopic,
                onToggleTimer = onToggleTimer,
                onResetTimer = onResetTimer,
                onSwapTimerMode = onSwapTimerMode
            )
        }
    }
}

@Composable
private fun TomorrowTabContent(
    state: HubUiState,
    tomorrowDayName: String,
    onAddClass: (Int, String, String) -> Unit,
    onDeleteClass: (Long) -> Unit,
    onAddPackItem: (String) -> Unit,
    onTogglePackItem: (PackItemEntity) -> Unit,
    onDeletePackItem: (Long) -> Unit
) {
    val palette = LocalHubPalette.current
    var showTimetableEditor by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableIntStateOf(1) }
    var selectedTime by remember { mutableStateOf("08:30") }
    var className by remember { mutableStateOf("") }
    var packInput by remember { mutableStateOf("") }

    // Classes Card
    HubCard {
        HubSectionHeader(title = "Classes", subtitle = "Tomorrow's schedule")

        if (state.tomorrowClasses.isEmpty()) {
            Text(
                text = "No classes added for $tomorrowDayName. Open the timetable below to add them.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            state.tomorrowClasses.forEachIndexed { idx, item ->
                if (idx > 0) HubRowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = HubFormatters.formatTime12h(item.time),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.accent,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.ink,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { showTimetableEditor = !showTimetableEditor }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (showTimetableEditor) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = palette.accent
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Edit weekly timetable",
                style = MaterialTheme.typography.labelLarge,
                color = palette.accent
            )
        }

        AnimatedVisibility(visible = showTimetableEditor) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dayOptions = HubFormatters.DAY_ORDER.map { HubFormatters.DAY_NAMES[it] }
                    HubDropdownField(
                        label = "Day",
                        options = dayOptions,
                        selectedOption = HubFormatters.DAY_NAMES[selectedDay],
                        onSelect = { name ->
                            val idx = HubFormatters.DAY_NAMES.indexOf(name)
                            if (idx >= 0) selectedDay = idx
                        },
                        modifier = Modifier.weight(1f)
                    )
                    HubTimePickerButton(
                        time24 = selectedTime,
                        onTimeSelected = { selectedTime = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubTextField(
                        value = className,
                        onValueChange = { className = it },
                        placeholder = "Subject (and room)",
                        maxLength = 60,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("class_name_input")
                    )
                    HubPrimaryButton(
                        text = "Add class",
                        onClick = {
                            if (className.isNotBlank()) {
                                onAddClass(selectedDay, selectedTime, className)
                                className = ""
                            }
                        },
                        modifier = Modifier.testTag("add_class_button")
                    )
                }

                state.classesByDay.forEach { (day, list) ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = HubFormatters.DAY_NAMES[day],
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.ink
                    )
                    list.forEachIndexed { idx, cls ->
                        if (idx > 0) HubRowDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = HubFormatters.formatTime12h(cls.time),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.accent,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = cls.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.ink,
                                modifier = Modifier.weight(1f)
                            )
                            HubDeleteButton(
                                onClick = { onDeleteClass(cls.id) },
                                contentDesc = "Delete class ${cls.name}"
                            )
                        }
                    }
                }
            }
        }
    }

    // Pack Your Bag Card
    HubCard {
        HubSectionHeader(title = "Pack your bag", subtitle = "Resets daily")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubTextField(
                value = packInput,
                onValueChange = { packInput = it },
                placeholder = "Add an item, like PE kit",
                maxLength = 60,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pack_input")
            )
            HubPrimaryButton(
                text = "Add item",
                onClick = {
                    if (packInput.isNotBlank()) {
                        onAddPackItem(packInput)
                        packInput = ""
                    }
                },
                modifier = Modifier.testTag("add_pack_button")
            )
        }

        if (state.packItems.isEmpty()) {
            Text(
                text = "List what goes in your bag. Checks reset every day.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            state.packItems.forEachIndexed { idx, item ->
                if (idx > 0) HubRowDivider()
                val checked = item.doneDate == state.tomorrowKey
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubCheckboxButton(
                        checked = checked,
                        contentDesc = "Mark ${item.text} packed",
                        onClick = { onTogglePackItem(item) }
                    )
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (checked) palette.done else palette.ink,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                    HubDeleteButton(
                        onClick = { onDeletePackItem(item.id) },
                        contentDesc = "Delete ${item.text}"
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeworkTabContent(
    state: HubUiState,
    onAddHomework: (String, String) -> Unit,
    onToggleHomework: (HomeworkEntity) -> Unit,
    onDeleteHomework: (Long) -> Unit
) {
    val palette = LocalHubPalette.current
    var hwText by remember { mutableStateOf("") }
    var hwDue by remember(state.tomorrowKey) { mutableStateOf(state.tomorrowKey) }

    HubCard {
        HubSectionHeader(title = "Homework and to do")
        Column(
            modifier = Modifier.padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HubTextField(
                value = hwText,
                onValueChange = { hwText = it },
                placeholder = "What do you need to do?",
                maxLength = 120,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("homework_input")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubDatePickerButton(
                    dateKey = hwDue,
                    placeholder = "Due date",
                    onDateSelected = { hwDue = it },
                    allowClear = true,
                    modifier = Modifier.weight(1f)
                )
                HubPrimaryButton(
                    text = "Add task",
                    onClick = {
                        if (hwText.isNotBlank()) {
                            onAddHomework(hwText, hwDue)
                            hwText = ""
                        }
                    },
                    modifier = Modifier.testTag("add_homework_button")
                )
            }
        }

        if (state.homework.isEmpty()) {
            Text(
                text = "All clear. Add anything you need to hand in.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            state.homework.forEachIndexed { idx, item ->
                if (idx > 0) HubRowDivider()
                val (tagLabel, tagStyle) = HubFormatters.formatDueLabel(item.due, state.todayDate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubCheckboxButton(
                        checked = item.done,
                        contentDesc = "Mark ${item.text} done",
                        onClick = { onToggleHomework(item) }
                    )
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.done) palette.done else palette.ink,
                        textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                    HubTag(text = tagLabel, style = if (item.done) "" else tagStyle)
                    HubDeleteButton(
                        onClick = { onDeleteHomework(item.id) },
                        contentDesc = "Delete task ${item.text}"
                    )
                }
            }
        }
    }

    // Week Ahead Card
    HubCard {
        HubSectionHeader(title = "Week ahead", subtitle = "Next 7 days")
        state.weekAhead.forEachIndexed { idx, day ->
            if (idx > 0) HubRowDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.ink,
                    modifier = Modifier.width(74.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${day.classCount} class${if (day.classCount == 1) "" else "es"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.ink
                    )
                    if (day.dueTasks.isNotEmpty()) {
                        Text(
                            text = "Due: ${day.dueTasks.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                    }
                    if (day.tests.isNotEmpty()) {
                        Text(
                            text = "Test: ${day.tests.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                    }
                }
                if (day.tests.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    HubTag(text = "Test", style = "soon")
                }
            }
        }
    }
}

@Composable
private fun ExamsAndStudyTabContent(
    state: HubUiState,
    onAddExam: (String, String, String, String) -> Unit,
    onToggleExamTopic: (ExamEntity, Int) -> Unit,
    onDeleteExam: (Long) -> Unit,
    onAddStudyTopic: (String, String) -> Unit,
    onToggleStudyTopic: (StudyTopicEntity) -> Unit,
    onDeleteStudyTopic: (Long) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSwapTimerMode: () -> Unit
) {
    val palette = LocalHubPalette.current
    var examSubject by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("") }
    var examTopics by remember { mutableStateOf("") }
    var examMethod by remember { mutableStateOf(HubFormatters.STUDY_METHODS.first()) }

    var studyText by remember { mutableStateOf("") }
    var studyDate by remember { mutableStateOf("") }

    // Exams Card
    HubCard {
        HubSectionHeader(title = "Exams")
        Column(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HubTextField(
                    value = examSubject,
                    onValueChange = { examSubject = it },
                    placeholder = "Exam subject, like Biology",
                    maxLength = 60,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("exam_subject_input")
                )
                HubDatePickerButton(
                    dateKey = examDate,
                    placeholder = "Date",
                    onDateSelected = { examDate = it },
                    modifier = Modifier.weight(1f)
                )
            }
            HubTextField(
                value = examTopics,
                onValueChange = { examTopics = it },
                placeholder = "Topics to study, separated by commas",
                maxLength = 300,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_topics_input")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubDropdownField(
                    label = "Study method",
                    options = HubFormatters.STUDY_METHODS,
                    selectedOption = examMethod,
                    onSelect = { examMethod = it },
                    modifier = Modifier.weight(1f)
                )
                HubPrimaryButton(
                    text = "Add exam",
                    onClick = {
                        if (examSubject.isNotBlank() && examDate.isNotBlank()) {
                            onAddExam(examSubject, examDate, examTopics, examMethod)
                            examSubject = ""
                            examDate = ""
                            examTopics = ""
                        }
                    },
                    modifier = Modifier.testTag("add_exam_button")
                )
            }
        }

        if (state.exams.isEmpty()) {
            Text(
                text = "No exams yet. Add one with its date and topics to get a study plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            state.exams.forEachIndexed { idx, examItem ->
                if (idx > 0) HubRowDivider()
                ExamItemCard(
                    item = examItem,
                    onToggleTopic = { topicIdx -> onToggleExamTopic(examItem.entity, topicIdx) },
                    onDelete = { onDeleteExam(examItem.entity.id) }
                )
            }
        }
    }

    // Study List Card
    HubCard {
        HubSectionHeader(title = "Study list")
        Column(
            modifier = Modifier.padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HubTextField(
                value = studyText,
                onValueChange = { studyText = it },
                placeholder = "What should you study?",
                maxLength = 120,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_topic_input")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubDatePickerButton(
                    dateKey = studyDate,
                    placeholder = "Test date (optional)",
                    onDateSelected = { studyDate = it },
                    allowClear = true,
                    modifier = Modifier.weight(1f)
                )
                HubPrimaryButton(
                    text = "Add topic",
                    onClick = {
                        if (studyText.isNotBlank()) {
                            onAddStudyTopic(studyText, studyDate)
                            studyText = ""
                            studyDate = ""
                        }
                    },
                    modifier = Modifier.testTag("add_study_button")
                )
            }
        }

        if (state.studyTopics.isEmpty()) {
            Text(
                text = "Nothing to study yet. Add a topic or an upcoming test.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            state.studyTopics.forEachIndexed { idx, topic ->
                if (idx > 0) HubRowDivider()
                val n = if (topic.date.isNotBlank()) HubFormatters.daysUntil(topic.date, state.todayDate) else null
                val tag = when {
                    n == null -> ""
                    n < 0L -> "Past"
                    n == 0L -> "Test today"
                    n == 1L -> "Test tomorrow"
                    else -> "Test in $n days"
                }
                val isSoon = !topic.done && n != null && n in 0L..1L
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubCheckboxButton(
                        checked = topic.done,
                        contentDesc = "Mark ${topic.text} studied",
                        onClick = { onToggleStudyTopic(topic) }
                    )
                    Text(
                        text = topic.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (topic.done) palette.done else palette.ink,
                        textDecoration = if (topic.done) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                    if (tag.isNotEmpty()) {
                        HubTag(text = tag, style = if (isSoon) "soon" else "")
                    }
                    HubDeleteButton(
                        onClick = { onDeleteStudyTopic(topic.id) },
                        contentDesc = "Delete ${topic.text}"
                    )
                }
            }
        }
    }

    // Study Timer Card
    HubCard {
        HubSectionHeader(title = "Study timer")
        Text(
            text = state.timerStatusLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = HubFormatters.formatTimer(state.timerSeconds),
            fontFamily = BricolageGrotesqueFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 54.sp,
            lineHeight = 60.sp,
            color = palette.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("study_timer_clock")
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            HubPrimaryButton(
                text = if (state.timerRunning) "Pause" else "Start",
                onClick = onToggleTimer,
                modifier = Modifier.testTag("timer_toggle_button")
            )
            HubAltButton(
                text = "Reset",
                onClick = onResetTimer,
                modifier = Modifier.testTag("timer_reset_button")
            )
            HubAltButton(
                text = if (state.timerMode == "Focus") "Switch to break" else "Switch to focus",
                onClick = onSwapTimerMode,
                modifier = Modifier.testTag("timer_swap_button")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExamItemCard(
    item: ExamDisplayItem,
    onToggleTopic: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val palette = LocalHubPalette.current
    val total = item.topics.size
    val doneCount = item.topics.count { it.done }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.entity.subject,
                fontFamily = BricolageGrotesqueFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = palette.ink,
                modifier = Modifier.weight(1f)
            )
            HubTag(text = item.tagText, style = if (item.isSoon) "soon" else "")
            HubDeleteButton(
                onClick = onDelete,
                contentDesc = "Delete exam ${item.entity.subject}"
            )
        }

        Text(
            text = item.formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )

        if (total > 0) {
            HubProgressBar(
                progress = doneCount.toFloat() / total.toFloat(),
                barColor = palette.accent,
                heightDp = 6,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "$doneCount of $total topics covered. Tap a topic when you finish it.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item.topics.forEachIndexed { idx, topic ->
                    Surface(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onToggleTopic(idx) },
                        shape = RoundedCornerShape(999.dp),
                        color = if (topic.done) palette.accent else palette.bg,
                        border = BorderStroke(1.dp, if (topic.done) palette.accent else palette.line)
                    ) {
                        Text(
                            text = (if (topic.done) "✓ " else "") + topic.t,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (topic.done) palette.accentInk else palette.ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = palette.ink)) {
                    append("How to study: ")
                }
                append(item.studyTip)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
        Text(
            text = item.planText,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
    }
}
