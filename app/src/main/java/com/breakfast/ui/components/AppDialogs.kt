package com.breakfast.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.BreakfastOutlinedTextField
import com.breakfast.models.StoreModel

@Composable
fun ErrorDialog(
    visible: Boolean,
    title: String = stringResource(id = R.string.something_went_wrong),
    message: String,
    onDismiss: () -> Unit,
    buttonText: String = stringResource(R.string.submit),
    iconRes: Int = R.drawable.ic_error_round
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.woodsmoke)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = R.color.dove_gray)
                )

                Spacer(Modifier.height(16.dp))

                BreakfastButtonRes(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.confirm),
    iconRes: Int = R.drawable.ic_info_round,
    isRed: Boolean = false
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.woodsmoke)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = R.color.dove_gray)
                )

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Cancel: just dismiss
                    BreakfastButtonRes(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(cancelText)
                    }

                    // Confirm: perform action
                    BreakfastButtonRes(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        isDanger = isRed
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectStoreDialog(
    visible: Boolean,
    stores: List<StoreModel>,
    onDismiss: () -> Unit,
    onConfirm: (StoreModel) -> Unit,
    iconRes: Int = R.drawable.ic_info_round,
    title: String = stringResource(R.string.select_store)
) {
    if (!visible) return

    val expanded = remember { mutableStateOf(false) }
    val selectedStore = remember(stores) { mutableStateOf(stores.firstOrNull()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(16.dp))

//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    color = colorResource(id = R.color.woodsmoke),
//                    modifier = Modifier.fillMaxWidth()
//                )

                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    BreakfastOutlinedTextField(
                        value = selectedStore.value?.name ?: "",
                        onValueChange = {},
                        label = stringResource(id = R.string.store),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    if (stores.isNotEmpty()) {
                                        expanded.value = !expanded.value
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        enabled = false,
                        isError = false,
                        trailing = {
                            Icon(
                                imageVector = if (expanded.value) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    if (stores.isNotEmpty()) {
                                        expanded.value = !expanded.value
                                    }
                                }
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f) // عشان يبقى بنفس عرض الـ TextField تقريباً
                    ) {
                        stores.forEach { store ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = store.name ?: "",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    selectedStore.value = store
                                    expanded.value = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BreakfastButtonRes(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.no))
                    }
                    BreakfastButtonRes(
                        onClick = {
                            selectedStore.value?.let { store ->
                                onConfirm(store)
                            }
                        },
                        enabled = selectedStore.value != null, // 🔥 disable لو مفيش حاجة مختارة
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.yes))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "ConfirmDialog Preview")
@Composable
private fun ConfirmDialogPreview() {
    ConfirmDialog(
        visible = true,
        title = "Delete item",
        message = "Are you sure you want to delete this item? This action cannot be undone.",
        onCancel = {},
        onConfirm = {},
        cancelText = "Cancel",
        confirmText = "Delete",
        iconRes = android.R.drawable.ic_dialog_info,
        isRed = true
    )
}

@Preview(showBackground = true, name = "ErrorDialog Preview")
@Composable
private fun ErrorDialogPreview() {
    // Use a platform icon to avoid missing project drawable at design time
    ErrorDialog(
        visible = true,
        title = "Heading",
        message = "Something went wrong. Please try again.",
        onDismiss = {},
        buttonText = "Submit",
        iconRes = android.R.drawable.ic_delete
    )
}

@Preview(showBackground = true, name = "SelectStoreDialog Preview")
@Composable
private fun SelectStoreDialogPreview() {
    val sample = listOf(
        StoreModel(1, "Store A", phone = "1234567890", image = ""),
        StoreModel(2, "Store B", phone = "1234567890", image = ""),
        StoreModel(3, "Store C", phone = "1234567890", image = ""),
    )
    SelectStoreDialog(
        visible = true,
        stores = sample,
        onDismiss = {},
        onConfirm = {}
    )
}