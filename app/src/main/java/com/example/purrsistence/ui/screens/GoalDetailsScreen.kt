package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.GoalViewModel
import java.util.Locale

@Composable
fun GoalDetailsScreen(
    goalId: Int?,
    goalViewModel: GoalViewModel,
    onEditClick: (Int) -> Unit,
    setTopBar: (TopBarState) -> Unit
) {

    val goal by goalViewModel
        .getGoal(goalId)
        .collectAsState(initial = null)

    val currentGoal = goal ?: return

    val formattedType =
        currentGoal.type
            .name
            .lowercase(Locale.ROOT)
            .replaceFirstChar {
                it.titlecase(Locale.ROOT)
            }

    setTopBar(
        TopBarState(
            title = "Goal Details"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {

        ElevatedCard(
            shape = Shapes.cards,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = Elevation.Lvl2
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = currentGoal.title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(
                            modifier = Modifier.height(Spacing.xs)
                        )

                        Text(
                            text = "${currentGoal.targetDuration.toHours()}h $formattedType",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentGoal.deepFocus) {

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Deep Focus",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(Spacing.sm)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {

                    Text(
                        text = "Goal Information",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Target Duration: ${currentGoal.targetDuration.toHours()}h",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Cycle: $formattedType",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = if (currentGoal.deepFocus) {
                            "Deep Focus enabled"
                        } else {
                            "Deep Focus disabled"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Button(
            onClick = {
                onEditClick(currentGoal.id)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.buttons,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = Elevation.Lvl1
            )
        ) {

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(Spacing.sm)
            )

            Text("Edit Goal")
        }
    }
}