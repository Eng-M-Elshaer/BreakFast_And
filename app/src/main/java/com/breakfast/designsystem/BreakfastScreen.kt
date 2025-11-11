package com.breakfast.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable screen container to unify padding and scaffold behavior.
 *
 * - Horizontal padding 16.dp
 * - Optional left action (default: back arrow)
 * - Optional right action (custom composable)
 * - Passes Scaffold paddingValues to content
 */
@Composable
fun BreakfastScreen(
    title: String? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    onLeftAction: (() -> Unit)? = null,
    rightAction: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (bottomBar != null) {
                bottomBar()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (title != null || onLeftAction != null || rightAction != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    if (onLeftAction != null) {
                        IconButton(onClick = onLeftAction) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Back"
                            )
                        }
                    }
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = if (onLeftAction != null) 4.dp else 0.dp)
                        )
                    } else {
                        // still occupy space so rightAction stays at end
                        Text(
                            text = "",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rightAction != null) {
                        rightAction()
                    }
                }
            }

            // screen content
            content(innerPadding)
        }
    }
}