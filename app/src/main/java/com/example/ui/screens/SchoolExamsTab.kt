package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyTopicEntity
import com.example.data.repository.ExamItem
import com.example.ui.components.HubCheckRow
import com.example.ui.components.HubDatePickerField
import com.example.ui.components.HubDropdownSelector
import com.example.ui.components.HubProgressBar
import com.example.ui.components.HubSectionCard
import com.example.ui.components.HubTagChip
import com.example.ui.theme.BricolageFontFamily
import com.example.ui.theme.LocalHubColors
import com.example.ui.viewmodel.HubViewModel
import com.example.ui.viewmodel.TimerUiState
import com.example.util.DateUtils
import com.example.util.DueUrgency
import java.util.Locale
import kotlin.math.ceil

private val STUDY_METHODS = listOf(
    "Flashcards",
    "Practice questions",
    "Summary notes",
    "Past papers",
    "Teach it out loud"
)

private val STUDY_TIPS = mapOf(
    "Flashcards" to "Write a question on one side and the answer on the other. Say answers out loud, and put the ones you miss in a pile to repeat.",
    "Practice questions" to "Try questions before looking at your notes. Mark the ones you got wrong and redo only those.",
    "Summary notes" to "Shrink each topic to one page from memory, then check your notes to see what you missed.",
    "Past papers" to "Do one paper with a timer and no notes, mark it, then study only the gaps.",
    "Teach it out loud" to "Explain each topic as if teaching a friend. Wherever you get stuck is what to study next."
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchoolExamsTab(
    exams: List<ExamItem>,
    studyTopics: List<StudyTopicEntity>,
    timer: TimerUiState,
    viewModel: HubViewModel
) {
    val hubColors = LocalHubColors.current
    var examSubject by rememberSaveable { mutableStateOf("") }
    var examDate by rememberSaveable { mutableStateOf(DateUtils.tomorrowKey()) }
    var examTopics by rememberSaveable { mutableStateOf("") }
    var examMethod by rememberSaveable { mutableStateOf(STUDY_METHODS.first()) }

    var studyInput by rememberSaveable { mutableStateOf("") }
    var studyDate by rememberSaveable { mutableStateOf("") }

    HubSectionCard(title = "Exams") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = examSubject,
                onValueChange = { if (it.length <= 60) examSubject = it },
                label = { Text("Exam subject, like Biology") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("exam_subject_input")
            )
            HubDatePickerField(
                label = "Exam date",
                dateKey = examDate,
                onDateSelected = { examDate = it },
                testTag = "exam_date_picker"
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = examTopics,
            onValueChange = { if (it.length <= 300) examTopics = it },
            label = { Text("Topics to study, separated by commas") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exam_topics_input")
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubDropdownSelector(
                label = "Study method",
                options = STUDY_METHODS,
                selectedOption = examMethod,
                onSelect = { examMethod = it },
                testTag = "exam_method_selector",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.addExam(examSubject, examDate, examTopics, examMethod)
                    examSubject = ""
                    examTopics = ""
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_exam_button")
            ) {
                Text("Add exam")
            }
        }

        Spacer(Modifier.height(14.dp))
        if (exams.isEmpty()) {
            Text(
                text = "No exams yet. Add one with its date and topics to get a study plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = hubColors.muted
            )
        } else {
            exams.forEachIndexed { idx, exam ->
                if (idx > 0) HorizontalDivider(color = hubColors.line)
                val n = DateUtils.daysUntil(exam.date) ?: 0
                val tot = exam.topics.size
                val doneCount = exam.topics.count { it.done }
                val remaining = tot - doneCount
                val tag = when {
                    n < 0 -> "Passed"
                    n == 0 -> "Today"
                    n == 1 -> "Tomorrow"
                    else -> "In $n days"
                }
                val perDay = if (n >= 2) ceil(remaining.toDouble() / (n - 1)).toInt() else 0
                val planText = when {
                    n < 0 -> "This exam has passed. You can delete it."
                    n == 0 -> "Exam today. Trust your preparation."
                    n == 1 -> "Exam is tomorrow. Review your weakest topics, pack your bag, and sleep early."
                    tot == 0 -> "Add topics next time to get a daily plan."
                    remaining == 0 -> "Every topic is covered. Use the days left for practice."
                    else -> "Plan: cover about $perDay topic${if (perDay == 1) "" else "s"} a day to finish the day before."
                }

                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = exam.subject,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        HubTagChip(
                            text = tag,
                            urgency = if (n in 0..3) DueUrgency.SOON else DueUrgency.NORMAL
                        )
                        IconButton(
                            onClick = { viewModel.deleteExam(exam.id) },
                            modifier = Modifier.testTag("delete_exam_${exam.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Delete exam",
                                tint = hubColors.muted
                            )
                        }
                    }

                    Text(
                        text = DateUtils.formatLongDate(exam.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = hubColors.muted
                    )

                    if (tot > 0) {
                        Spacer(Modifier.height(8.dp))
                        HubProgressBar(
                            progress = doneCount.toFloat() / tot.toFloat(),
                            heightDp = 6
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$doneCount of $tot topics covered. Tap a topic when you finish it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = hubColors.muted
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            exam.topics.forEachIndexed { tIdx, topic ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            if (topic.done) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.background
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (topic.done) MaterialTheme.colorScheme.primary else hubColors.line,
                                            RoundedCornerShape(999.dp)
                                        )
                                        .clickable { viewModel.toggleExamTopic(exam, tIdx) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("exam_${exam.id}_topic_$tIdx")
                                ) {
                                    Text(
                                        text = (if (topic.done) "✓ " else "") + topic.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (topic.done) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                append("How to study: ")
                            }
                            append(STUDY_TIPS[exam.method].orEmpty())
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = hubColors.muted
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = planText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = hubColors.muted
                    )
                }
            }
        }
    }

    HubSectionCard(title = "Study list") {
        OutlinedTextField(
            value = studyInput,
            onValueChange = { if (it.length <= 120) studyInput = it },
            label = { Text("What should you study?") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_topic_input")
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubDatePickerField(
                label = "Test date (optional)",
                dateKey = studyDate,
                onDateSelected = { studyDate = it },
                allowClear = true,
                testTag = "study_date_picker",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.addStudyTopic(studyInput, studyDate)
                    studyInput = ""
                    studyDate = ""
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_study_button")
            ) {
                Text("Add topic")
            }
        }

        Spacer(Modifier.height(12.dp))
        if (studyTopics.isEmpty()) {
            Text(
                text = "Nothing to study yet. Add a topic or an upcoming test.",
                style = MaterialTheme.typography.bodyMedium,
                color = hubColors.muted
            )
        } else {
            studyTopics.forEachIndexed { idx, item ->
                if (idx > 0) HorizontalDivider(color = hubColors.line)
                val n = if (item.testDate.isNotBlank()) DateUtils.daysUntil(item.testDate) else null
                val tagText = when {
                    n == null -> null
                    n < 0 -> "Past"
                    n == 0 -> "Test today"
                    n == 1 -> "Test tomorrow"
                    else -> "Test in $n days"
                }
                val urgency = if (n != null && n in 0..1) DueUrgency.SOON else DueUrgency.NORMAL
                HubCheckRow(
                    text = item.text,
                    checked = item.done,
                    onToggle = { viewModel.toggleStudyTopic(item) },
                    onDelete = { viewModel.deleteStudyTopic(item.id) },
                    tagText = tagText,
                    tagUrgency = urgency,
                    toggleTestTag = "toggle_study_${item.id}",
                    deleteTestTag = "delete_study_${item.id}"
                )
            }
        }
    }

    HubSectionCard(title = "Study timer") {
        val mins = timer.remainingSeconds / 60
        val secs = timer.remainingSeconds % 60
        val clockStr = String.format(Locale.US, "%02d:%02d", mins, secs)

        Text(
            text = timer.statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = hubColors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = clockStr,
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 52.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("study_timer_clock")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { viewModel.toggleTimer() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("timer_start_pause_button")
            ) {
                Text(if (timer.isRunning) "Pause" else "Start")
            }
            OutlinedButton(
                onClick = { viewModel.resetTimer() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, hubColors.line),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("timer_reset_button")
            ) {
                Text("Reset")
            }
            OutlinedButton(
                onClick = { viewModel.swapTimerMode() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, hubColors.line),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("timer_swap_button")
            ) {
                Text(if (timer.mode == "Focus") "Switch to break" else "Switch to focus")
            }
        }
    }
}
