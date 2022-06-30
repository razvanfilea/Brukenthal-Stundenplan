package net.theluckycoder.stundenplan.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import net.theluckycoder.brukplan.grades.model.Grades
import net.theluckycoder.brukplan.grades.model.Subject
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.viewmodel.SubjectsViewModel

@OptIn(ExperimentalAnimationApi::class)
class GradesScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<SubjectsViewModel>()
        val subjects by viewModel.subjectsFlow.collectAsState(emptyList())
        val selectedSemester by viewModel.selectedSemesterStateFlow.collectAsState()
        val semesterAverage by viewModel.semesterAveragesStateFlow.collectAsState()

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    viewModel.showEditSubjectDialog.value = Subject()
                }) { Icon(Icons.Default.Add, contentDescription = null) }
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            topBar = { TopBar(selectedSemester, semesterAverage) },
            bottomBar = { BottomBar(selectedSemester, viewModel) }
        ) { contentPadding ->
            Box(Modifier.padding(contentPadding)) {
                if (subjects.isEmpty()) {
                    Card(
                        Modifier.padding(8.dp),
                        elevation = 4.dp,
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.grades_explanation_title),
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(stringResource(R.string.grades_explanation))
                        }
                    }
                } else {
                    Crossfade(targetState = selectedSemester) { semester ->
                        SubjectsList(
                            subjects = subjects,
                            semester = semester,
                            onClickSubject = { viewModel.showEditSubjectDialog.value = it },
                            onLongClickSubject = { viewModel.showDeleteSubjectDialog.value = it },
                        )
                    }
                }
            }
        }

        val subjectToEdit = viewModel.showEditSubjectDialog.value
        if (subjectToEdit != null) {
            EditSubjectDialog(
                subjectToEdit,
                selectedSemester,
                onDismiss = { viewModel.showEditSubjectDialog.value = null },
                onSave = { viewModel.insert(it) }
            )
        }

        val subjectToDelete = viewModel.showDeleteSubjectDialog.value
        if (subjectToDelete != null) {
            DeleteSubjectDialog(
                subject = subjectToDelete,
                onDismiss = { viewModel.showDeleteSubjectDialog.value = null },
                onDelete = {
                    viewModel.showDeleteSubjectDialog.value = null
                    viewModel.delete(subjectToDelete)
                }
            )
        }
    }

    @Composable
    private fun TopBar(
        selectedSemester: Subject.Semester,
        semesterAverage: Pair<Float, Float>
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(16.dp),
        ) {
            AnimatedContent(
                targetState = if (selectedSemester == Subject.Semester.ONE) semesterAverage.first else semesterAverage.second
            ) { target ->
                Text(stringResource(R.string.grades_semester_average, target))
            }

            val annualAverage = remember(semesterAverage) {
                semesterAverage.toList()
                    .filterNot { it == 0f }.average().takeIf { it.isFinite() } ?: 0f
            }

            Spacer(Modifier.height(2.dp))

            AnimatedContent(targetState = annualAverage) { target ->
                Text(stringResource(R.string.grades_general_average, target))
            }
        }
    }

    @Composable
    private fun BottomBar(
        selectedSemester: Subject.Semester,
        viewModel: SubjectsViewModel
    ) {
        BottomAppBar(
            Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
        ) {
            val selected1 = selectedSemester == Subject.Semester.ONE
            BottomNavigationItem(
                selected = selected1,
                onClick = { viewModel.setSelectedSemester(Subject.Semester.ONE) },
                icon = {
                    Text(
                        text = stringResource(R.string.grades_semester_ct, 1),
                        color = if (selected1) MaterialTheme.colors.secondaryVariant else Color.Unspecified,
                        fontWeight = FontWeight.Bold.takeIf { selected1 },
                    )
                }
            )

            val selected2 = selectedSemester == Subject.Semester.TWO
            BottomNavigationItem(
                selected = selected2,
                onClick = { viewModel.setSelectedSemester(Subject.Semester.TWO) },
                icon = {
                    Text(
                        text = stringResource(R.string.grades_semester_ct, 2),
                        color = if (selected2) MaterialTheme.colors.secondaryVariant else Color.Unspecified,
                        fontWeight = FontWeight.Bold.takeIf { selected2 },
                    )
                }
            )
        }
    }

    @OptIn(
        ExperimentalFoundationApi::class,
        ExperimentalMaterialApi::class
    )
    @Composable
    private fun SubjectsList(
        subjects: List<Subject>,
        semester: Subject.Semester,
        onClickSubject: (Subject) -> Unit,
        onLongClickSubject: (Subject) -> Unit,
    ) {
        val count =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2

        LazyVerticalGrid(
            columns = GridCells.Fixed(count),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(subjects) {
                key(it) {
                    Card(
                        Modifier.padding(8.dp),
                        elevation = 4.dp,
                    ) {
                        Box(
                            Modifier.combinedClickable(
                                onClick = { onClickSubject(it) },
                                onLongClick = { onLongClickSubject(it) },
                            )
                        ) {
                            SubjectItem(it, semester)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SubjectItem(subject: Subject, semester: Subject.Semester) {
        Column(modifier = Modifier.padding(12.dp)) {
            val grades = subject[semester]

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    subject.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(8.dp))

                if (grades.average != 0) {
                    Text(
                        stringResource(R.string.grades_average, grades.average),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val gradesString = grades.grades.filter { it != 0 }.joinToString(", ")
                Text(stringResource(R.string.grades_list, gradesString))

                Spacer(Modifier.width(4.dp))

                if (grades.semesterPaper != 0)
                    Text(stringResource(R.string.grades_semester_paper, grades.semesterPaper))
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun EditSubjectDialog(
        subject: Subject,
        selectedSemester: Subject.Semester,
        onDismiss: () -> Unit,
        onSave: (Subject) -> Unit
    ) {
        var name by remember { mutableStateOf(subject.name) }
        val currentGrades = subject[selectedSemester]
        var semesterPaper by remember { mutableStateOf(currentGrades.semesterPaper) }
        val gradesList = remember { currentGrades.grades.toMutableStateList() }

        if (gradesList.size < 10) {
            val zeroCount = gradesList.count { it == 0 }
            LaunchedEffect(zeroCount) {
                // there should always be at least 2 empty fields
                if (zeroCount < 2)
                    gradesList.add(0)
            }
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            val numberKeyboard = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )

            Surface(Modifier.padding(8.dp), shape = RoundedCornerShape(6.dp)) {
                Column(Modifier.padding(8.dp)) {

                    val modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)

                    Spacer(Modifier.height(8.dp))

                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.grades_hint_subject)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)///.padding(top = 16.dp),
                    )

                    TextField(
                        value = if (semesterPaper != 0) semesterPaper.toString() else "",
                        onValueChange = { semesterPaper = it.toGrade() },
                        label = { Text(stringResource(R.string.grades_hint_semester_paper)) },
                        singleLine = true,
                        keyboardOptions = numberKeyboard,
                        modifier = modifier,
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        itemsIndexed(gradesList) { index, grade ->
                            val visibleState = remember {
                                MutableTransitionState(false).apply {
                                    // Start the animation immediately.
                                    targetState = true
                                }
                            }

                            AnimatedVisibility(visibleState = visibleState) {
                                TextField(
                                    value = if (grade != 0) grade.toString() else "",
                                    onValueChange = { gradesList[index] = it.toGrade() },
                                    label = { Text(stringResource(R.string.grades_hint_grade,index + 1)) },
                                    singleLine = true,
                                    keyboardOptions = numberKeyboard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(android.R.string.cancel))
                        }

                        TextButton(onClick = {
                            val selectedGrades = Grades(
                                semesterPaper = semesterPaper,
                                grades = gradesList.filterNot { it == 0 },
                            )

                            onSave(
                                subject.copy(
                                    name = name,
                                    semester1 = if (selectedSemester == Subject.Semester.ONE) selectedGrades else subject.semester1,
                                    semester2 = if (selectedSemester == Subject.Semester.TWO) selectedGrades else subject.semester2,
                                )
                            )
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DeleteSubjectDialog(
        subject: Subject,
        onDismiss: () -> Unit,
        onDelete: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.grades_delete_title, subject.name)) },
            text = { Text(stringResource(R.string.grades_delete_desc)) },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    companion object {
        fun String.toGrade() = (toIntOrNull() ?: 0).coerceIn(0, 10)
    }
}
