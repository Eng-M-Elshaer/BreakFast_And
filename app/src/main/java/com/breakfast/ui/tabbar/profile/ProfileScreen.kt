
import androidx.navigation.NavController
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.breakfast.designsystem.BreakfastOutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.breakfast.ui.components.ErrorDialog
import com.breakfast.ui.components.ConfirmDialog
import com.breakfast.BreakfastApplication
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.models.User
import com.breakfast.models.UserModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.utils.Validator
import com.breakfast.viewmodel.ProfileViewModel
import java.io.ByteArrayOutputStream
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.res.stringResource
import com.breakfast.R
import com.breakfast.models.ApiResponse
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.breakfast.ui.components.BreakfastEmptyState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController? = null, isPreview: Boolean = false) {

    // Obtain dependencies
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = ApiClient.apiService
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(preferenceManager, apiService, context))
    val profileState = viewModel.profileState.collectAsState(null)
    val updateState = viewModel.updateState.collectAsState(null)
    val logoutState = viewModel.logoutState.collectAsState(null)
    val showLogoutError = remember { mutableStateOf(false) }
    val logoutErrorMsg = remember { mutableStateOf("") }
    val avatarState = viewModel.avatarState.collectAsState(null)
    val showAvatarError = remember { mutableStateOf(false) }
    val avatarErrorMsg = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = remember { mutableStateOf(false) }
    val showUpdate = remember { mutableStateOf(false) }
    val latestAvatarBytes = remember { mutableStateOf<ByteArray?>(null) }

    fun uploadAvatar(bytes: ByteArray) {
        latestAvatarBytes.value = bytes
        viewModel.uploadAvatar(bytes)
    }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            viewModel.loadProfile()
        }
    }

    LaunchedEffect(logoutState.value) {
        when (val s = logoutState.value) {
            is Result.Success<*> -> {
                navController?.tryNavigateToLogin()
                viewModel.clearLogoutState()
            }
            is Result.Error -> {
                logoutErrorMsg.value = s.message ?: context.getString(R.string.failed_logout)
                showLogoutError.value = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(avatarState.value) {
        when (val s = avatarState.value) {
            is Result.Success<*> -> {
                viewModel.loadProfile()
                viewModel.clearAvatarState()
                snackbarHostState.showSnackbar(context.getString(R.string.profile_updated))
            }
            is Result.Error -> {
                avatarErrorMsg.value = s.message ?: context.getString(R.string.failed_update)
                showAvatarError.value = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(updateState.value) {
        when (val s = updateState.value) {
            is Result.Success<*> -> {
                // Collapse edit mode and hide Update button
                isEditing.value = false
                showUpdate.value = false
                // Show confirmation and clear state so it doesn't stick when revisiting the screen
                snackbarHostState.showSnackbar(context.getString(R.string.profile_updated))
                viewModel.clearUpdateState()
            }
            is Result.Error -> {
                // Optionally handle error via dialog/snackbar
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.personal_details)) },
                actions = {
                    IconButton(onClick = {
                        val next = !isEditing.value
                        isEditing.value = next
                        showUpdate.value = next
                    }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
            when (val state = profileState.value) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    BreakfastEmptyState(
                        iconRes = com.breakfast.R.drawable.no_internet,
                        title = state.message ?: stringResource(id = com.breakfast.R.string.something_went_wrong),
                        showButton = true,
                        buttonText = stringResource(id = com.breakfast.R.string.retry),
                        onButtonClick = { viewModel.loadProfile() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                is Result.Success<*> -> {
                    val user: User = when (val payload = state.data) {
                        is User -> payload
                        is UserModel -> payload.user ?: User(null, null, null, null, null, null)
                        is ApiResponse<*> -> (payload.data as? UserModel)?.user ?: User(null, null, null, null, null, null)
                        else -> User(null, null, null, null, null, null)
                    }
                    ProfileContent(
                        user = user,
                        isEditing = isEditing.value,
                        showUpdate = showUpdate.value,
                        updateState = updateState.value,
                        avatarState = avatarState.value,
                        onUpdate = { n, p, e -> viewModel.updateProfile(n, p, e) },
                        onChangePassword = { navController?.navigate("change_password") },
                        onLogout = { viewModel.logout() },
                        onPickOrCapture = { bytes -> uploadAvatar(bytes) },
                        latestAvatarBytes = latestAvatarBytes.value
                    )
                    ErrorDialog(
                        visible = showLogoutError.value,
                        title = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.error),
                        message = logoutErrorMsg.value,
                        buttonText = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.ok),
                        onDismiss = {
                            showLogoutError.value = false
                            viewModel.clearLogoutState()
                        }
                    )
                    ErrorDialog(
                        visible = showAvatarError.value,
                        title = stringResource(id = R.string.something_went_wrong),
                        message = avatarErrorMsg.value,
                        buttonText = stringResource(id = R.string.submit),
                        onDismiss = {
                            showAvatarError.value = false
                            viewModel.clearAvatarState()
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

private fun NavController.tryNavigateToLogin() {
    val candidates = listOf("login", "sign_in", "auth", "LOGIN", "Auth")
    for (route in candidates) {
        try {
            this.navigate(route) {
                popUpTo(this@tryNavigateToLogin.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            return
        } catch (_: IllegalArgumentException) { /* try next */ }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    isEditing: Boolean,
    showUpdate: Boolean,
    updateState: Result<*>?,
    avatarState: Result<*>?,
    onUpdate: (name: String, phone: String, email: String) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onPickOrCapture: (ByteArray) -> Unit,
    latestAvatarBytes: ByteArray?
) {
    val nameState = remember { mutableStateOf(user.name ?: "") }
    val phoneState = remember { mutableStateOf(user.phone ?: "") }
    val emailState = remember { mutableStateOf(user.email ?: "") }

    // Touched flags for showing errors only after user interaction
    val nameTouched = remember { mutableStateOf(false) }
    val phoneTouched = remember { mutableStateOf(false) }
    val emailTouched = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val showPicker = remember { mutableStateOf(false) }
    val avatarBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val showLogoutConfirm = remember { mutableStateOf(false) }

    // Launchers
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    avatarBitmap.value = bmp
                    onPickOrCapture(bytes)
                }
            } catch (_: Exception) { }
        }
    }
    val pickLegacy = rememberLauncherForActivityResult(GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    avatarBitmap.value = bmp
                    onPickOrCapture(bytes)
                }
            } catch (_: Exception) { }
        }
    }
    val takePreview = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            val bytes = out.toByteArray()
            avatarBitmap.value = bmp
            onPickOrCapture(bytes)
        }
    }
    val requestCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) takePreview.launch(null) else { /* optionally show a toast/snackbar */ }
    }

    // Baseline values to detect changes
    val originalName = remember(user) { user.name ?: "" }
    val originalPhone = remember(user) { user.phone ?: "" }
    val originalEmail = remember(user) { user.email ?: "" }
    val isDirty = (nameState.value != originalName) || (phoneState.value != originalPhone) || (emailState.value != originalEmail)

    // Avatar
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val bmp = avatarBitmap.value ?: latestAvatarBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { showPicker.value = true }
            )
        } else {
            val avatarUrl = user.profilePic?.let {
                if (it.startsWith("http://")) it.replace("http://", "https://") else it
            }
            if (avatarUrl.isNullOrBlank()) {
                // No URL → show local placeholder drawable
                Icon(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "avatar",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showPicker.value = true }
                )
            } else {
                // URL exists → load with Coil and still provide placeholder/error
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showPicker.value = true },
                    placeholder = painterResource(id = R.drawable.avatar),
                    error = painterResource(id = R.drawable.avatar)
                )
            }
        }
        if (avatarState is Result.Loading) {
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
    Spacer(modifier = Modifier.padding(top = 16.dp))

    if (showPicker.value) {
        AlertDialog(
            onDismissRequest = { showPicker.value = false },
            title = { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.change_profile_picture)) },
            text = { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.choose_source)) },
            confirmButton = {
                TextButton(onClick = {
                    showPicker.value = false
                    pickLegacy.launch("image/*")
                }) { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.gallery)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPicker.value = false
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }) { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.camera)) }
            }
        )
    }

    BreakfastOutlinedTextField(
        value = nameState.value,
        onValueChange = {
            if (!nameTouched.value) nameTouched.value = true
            nameState.value = it
        },
        label = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.name),
        isError = nameTouched.value && !Validator.isValidFullName(nameState.value),
        errorText = if (nameTouched.value && !Validator.isValidFullName(nameState.value)) {
            androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.invalid_name)
        } else null,
        enabled = isEditing,
        modifier = Modifier.fillMaxWidth()
    )
    BreakfastOutlinedTextField(
        value = phoneState.value,
        onValueChange = {
            if (!phoneTouched.value) phoneTouched.value = true
            phoneState.value = it
        },
        label = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.phone),
        isError = phoneTouched.value && !Validator.isValidEgyptianPhoneNumber(phoneState.value),
        errorText = if (phoneTouched.value && !Validator.isValidEgyptianPhoneNumber(phoneState.value)) {
            androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.invalid_phone)
        } else null,
        enabled = isEditing,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
    BreakfastOutlinedTextField(
        value = emailState.value,
        onValueChange = {
            if (!emailTouched.value) emailTouched.value = true
            emailState.value = it
        },
        label = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.email),
        isError = emailTouched.value && !Validator.isValidEmail(emailState.value),
        errorText = if (emailTouched.value && !Validator.isValidEmail(emailState.value)) {
            androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.invalid_email)
        } else null,
        enabled = isEditing,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )

    if (updateState is Result.Loading) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
    }

    // Change Password button (gradient)
    BreakfastButtonRes(
        onClick = onChangePassword,
        enabled = true,
        isHasObserver = false,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.change_password_button))
    }

    val allValid = Validator.isValidFullName(nameState.value) &&
        Validator.isValidEgyptianPhoneNumber(phoneState.value) &&
        Validator.isValidEmail(emailState.value)
    if (isEditing && showUpdate) {
        val canUpdate = allValid && isDirty
        BreakfastButtonRes(
            onClick = { onUpdate(nameState.value.trim(), phoneState.value.trim(), emailState.value.trim()) },
            enabled = canUpdate,
            isHasObserver = !canUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(text = stringResource(R.string.update))
        }
    }

    // Logout button – danger style (solid red, using isHasObserver = true then custom background inside component if available)
    BreakfastButtonRes(
        onClick = { showLogoutConfirm.value = true },
        enabled = true,
        isHasObserver = false,
        isDanger = true,
        iconRes = com.breakfast.R.drawable.rectangle_portrait_and_arrow_forward,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(text = stringResource(R.string.log_out))
    }

    ConfirmDialog(
        visible = showLogoutConfirm.value,
        title = stringResource(id = R.string.log_out),
        message = stringResource(id = R.string.are_you_sure_to_logout),
        onCancel = { showLogoutConfirm.value = false },
        onConfirm = {
            showLogoutConfirm.value = false
            onLogout()
        },
        cancelText = stringResource(id = R.string.cancel),
        confirmText = stringResource(id = R.string.confirm),
        isRed = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Profile – Preview")
@Composable
private fun ProfileScreenPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.personal_details)) },
                actions = {
                    IconButton(onClick = { /* simulate toggle */ }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
            ProfileContent(
                user = User(id = 64, name = "iOS Tester", email = "mail@mail.com", status = null, phone = "01000000000", profilePic = null),
                isEditing = true,
                showUpdate = true,
                updateState = null,
                avatarState = null,
                onUpdate = { _, _, _ -> },
                onChangePassword = {},
                onLogout = {},
                onPickOrCapture = {},
                latestAvatarBytes = null
            )
        }
    }
}