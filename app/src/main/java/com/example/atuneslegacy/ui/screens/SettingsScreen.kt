package com.example.atuneslegacy.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.ui.theme.AppTheme
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    val isLoadingAuth by viewModel.isLoadingAuth.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()
    val songs by viewModel.allSongs.collectAsState()

    var enableCrossfade by remember { mutableStateOf(true) }
    var enableSoundEnhancer by remember { mutableStateOf(true) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (themeColors.isDark) themeColors.background else Color(0xFFD8DCE5))
            .verticalScroll(rememberScrollState())
    ) {
        // Classic iOS Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (themeColors.isDark) {
                            listOf(Color(0xFF32363C), Color(0xFF1E2125))
                        } else {
                            listOf(Color(0xFF9EBCD0), Color(0xFF5B82A8))
                        }
                    )
                )
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group 1: User Profile Card (Clicking takes you to ProfileScreen)
            IOSGroupCard(themeColors = themeColors) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setShowProfilePage(true) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(themeColors.background)
                            .border(1.dp, themeColors.surfaceBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePictureUri != null) {
                            AsyncImage(
                                model = profilePictureUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userBio,
                            fontSize = 11.sp,
                            color = themeColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Group 2: Preferences & Theme
            IOSGroupCard(themeColors = themeColors) {
                IOSCell(
                    icon = Icons.Default.Palette,
                    iconBg = Color(0xFF8B5CF6),
                    title = "Theme & Appearance",
                    value = currentTheme.displayName,
                    onClick = { showThemeDialog = true }
                )
                IOSDivider(themeColors = themeColors)
                IOSToggleCell(
                    icon = Icons.Default.VolumeUp,
                    iconBg = Color(0xFF10B981),
                    title = "Crossfade Songs",
                    checked = enableCrossfade,
                    onCheckedChange = { enableCrossfade = it }
                )
                IOSDivider(themeColors = themeColors)
                IOSToggleCell(
                    icon = Icons.Default.Speaker,
                    iconBg = Color(0xFFEC4899),
                    title = "Sound Enhancer",
                    checked = enableSoundEnhancer,
                    onCheckedChange = { enableSoundEnhancer = it }
                )
            }

            // Group 3: Account & Cloud
            IOSGroupCard(themeColors = themeColors) {
                IOSCell(
                    icon = Icons.Default.Cloud,
                    iconBg = Color(0xFF0EA5E9),
                    title = "Supabase Cloud Account",
                    value = if (currentUser != null) "Signed In" else "Sign In",
                    onClick = { showAccountDialog = true }
                )
            }

            // Group 4: Library Management
            IOSGroupCard(themeColors = themeColors) {
                IOSCell(
                    icon = Icons.Default.AddCircle,
                    iconBg = Color(0xFF22C55E),
                    title = "Add Media to Library",
                    value = "${songs.size} items",
                    onClick = { viewModel.setShowAddMediaDialog(true) }
                )
            }
        }
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold, color = themeColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentTheme == theme) themeColors.primaryAccent.copy(alpha = 0.2f) else themeColors.background)
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(theme.displayName, fontWeight = if (currentTheme == theme) FontWeight.Bold else FontWeight.Normal, color = themeColors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showThemeDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)) {
                    Text("Done")
                }
            },
            containerColor = themeColors.surface
        )
    }

    // Account Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Supabase Account", fontWeight = FontWeight.Bold, color = themeColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentUser == null) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email") },
                            singleLine = true,
                            textStyle = TextStyle(color = themeColors.inputText),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = themeColors.surface, unfocusedContainerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            singleLine = true,
                            textStyle = TextStyle(color = themeColors.inputText),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = themeColors.surface, unfocusedContainerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.signInWithEmail(emailInput, passwordInput) }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent), modifier = Modifier.weight(1f)) {
                                Text("Sign In")
                            }
                            OutlinedButton(onClick = { viewModel.signUpWithEmail(emailInput, passwordInput) }, modifier = Modifier.weight(1f)) {
                                Text("Sign Up")
                            }
                        }
                    } else {
                        Text("Signed in as ${currentUser!!.email}", color = themeColors.textPrimary)
                        Button(onClick = { viewModel.signOut() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
                            Text("Sign Out")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAccountDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)) {
                    Text("Done")
                }
            },
            containerColor = themeColors.surface
        )
    }
}

@Composable
fun IOSGroupCard(
    themeColors: com.example.atuneslegacy.ui.theme.ThemeColors,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = themeColors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.surfaceBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun IOSCell(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    val themeColors = LocalThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = themeColors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun IOSToggleCell(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val themeColors = LocalThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themeColors.primaryAccent
            )
        )
    }
}

@Composable
fun IOSDivider(themeColors: com.example.atuneslegacy.ui.theme.ThemeColors) {
    HorizontalDivider(
        color = themeColors.surfaceBorder,
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 52.dp)
    )
}
