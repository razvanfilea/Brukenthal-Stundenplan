package net.theluckycoder.stundenplan.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
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
import net.theluckycoder.brukplan.grades.model.Subject
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.ui.VerticallyAnimatedNumber
import net.theluckycoder.stundenplan.viewmodel.SubjectsViewModel

class GradesScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<SubjectsViewModel>()
        val subjectsState = viewModel.subjectsFlow.collectAsState(null)
        val totalAverage by viewModel.totalAveragesStateFlow.collectAsState()

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    viewModel.showCreateSubjectDialog.value = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            topBar = { TopBar(totalAverage) },
        ) { contentPadding ->
            Box(Modifier.padding(contentPadding)) {
                // Don't draw anything while it's loading
                val subjects = subjectsState.value ?: return@Box

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
                    SubjectsList(
                        subjects = subjects,
                        onClickSubject = { viewModel.showEditSubjectDialog.value = it },
                        onLongClickSubject = { viewModel.showDeleteSubjectDialog.value = it },
                    )
                }
            }
        }

        if (viewModel.showCreateSubjectDialog.value) {
            CreateSubjectDialog(
                onDismiss = { viewModel.showCreateSubjectDialog.value = false },
                onSave = { viewModel.insert(it) }
            )
        }

        val subjectToEdit = viewModel.showEditSubjectDialog.value
        if (subjectToEdit != null) {
            EditSubjectDialog(
                subjectToEdit,
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
    private fun TopBar(totalAverage: Float) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(16.dp),
        ) {
            Text(stringResource(R.string.grades_average))

            Spacer(Modifier.width(2.dp))

            VerticallyAnimatedNumber(targetState = totalAverage) { target ->
                Text(target.toString())
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SubjectsList(
        subjects: List<Subject>,
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
            items(subjects, key = { it.id }) {
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
                        SubjectItem(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun SubjectItem(subject: Subject) {
        Column(modifier = Modifier.padding(12.dp)) {

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

                if (subject.gradesAverage != 0) {
                    Text(
                        stringResource(R.string.grades_average, subject.gradesAverage),
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
                val gradesString = subject.grades.filter { it != 0 }.joinToString(", ")
                Text(stringResource(R.string.grades_list, gradesString))

                Spacer(Modifier.width(4.dp))
            }
        }
    }

    @Composable
    private fun CreateSubjectDialog(onDismiss: () -> Unit, onSave: (Subject) -> Unit) {
        var name by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.grades_create_subject)) },
            text = {
                Column {
                    Spacer(Modifier.height(16.dp))

                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.grades_hint_subject)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSave(Subject(name))
                    onDismiss()
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    @Composable
    private fun EditSubjectDialog(
        subject: Subject,
        onDismiss: () -> Unit,
        onSave: (Subject) -> Unit
    ) {
        val gradesList = remember { subject.grades.toMutableStateList() }

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

                    Spacer(Modifier.height(8.dp))

                    Text(
                        subject.name,
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
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
                                    label = {
                                        Text(
                                            stringResource(
                                                R.string.grades_hint_grade,
                                                index + 1
                                            )
                                        )
                                    },
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
                            val selectedGrades = gradesList.filterNot { it == 0 }

                            onSave(subject.copy(grades = selectedGrades))
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
