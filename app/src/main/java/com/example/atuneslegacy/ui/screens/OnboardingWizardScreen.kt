package com.example.atuneslegacy.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.atuneslegacy.ui.theme.AppTheme
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun OnboardingWizardScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current

    var currentStep by remember { mutableIntStateOf(1) } // 1 to 7

    // Profile State
    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()

    var nameInput by remember { mutableStateOf(userName) }
    var bioInput by remember { mutableStateOf(userBio) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setProfilePictureUri(it.toString()) }
    }

    // Selected Theme
    val currentTheme by viewModel.currentTheme.collectAsState()
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    // Permission check functions
    fun isAudioGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPhotosVideosGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isMicGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun isNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Automatically advance if current step permission is already granted
    LaunchedEffect(currentStep) {
        when (currentStep) {
            3 -> if (isAudioGranted()) currentStep = 4
            4 -> if (isPhotosVideosGranted()) currentStep = 5
            5 -> if (isMicGranted()) currentStep = 6
            6 -> if (isNotificationsGranted()) currentStep = 7
        }
    }

    // Permission Request Launchers
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (isAudioGranted() && currentStep == 3) currentStep = 4
    }

    val photosVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (isPhotosVideosGranted() && currentStep == 4) currentStep = 5
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (isMicGranted() && currentStep == 5) currentStep = 6
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (isNotificationsGranted() && currentStep == 6) currentStep = 7
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (currentStep == 7) themeColors.background else Color(0xFFD8DCE5))
    ) {
        // Classic iOS 6 Glossy Top Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(
                    brush = if (currentStep == 7 && themeColors.isAppleMusic) {
                        Brush.verticalGradient(listOf(themeColors.surface, themeColors.surface))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF83A1C0), Color(0xFF436993)))
                    }
                )
                .border(0.5.dp, if (currentStep == 7 && themeColors.isAppleMusic) themeColors.surfaceBorder else Color(0xFF2C4563))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $currentStep of 7",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentStep == 7 && themeColors.isAppleMusic) themeColors.textPrimary else Color.White
                )

                Text(
                    text = "aTunes Setup",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentStep == 7 && themeColors.isAppleMusic) themeColors.textPrimary else Color.White
                )

                // Classic Glossy iOS 6 Next Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(themeColors.primaryAccent, themeColors.primaryAccent)
                            )
                        )
                        .border(0.5.dp, themeColors.primaryAccent, RoundedCornerShape(6.dp))
                        .clickable {
                            if (currentStep == 2) {
                                viewModel.updateUserProfile(nameInput, bioInput)
                            }
                            if (currentStep == 7) {
                                viewModel.setTheme(selectedTheme)
                                onComplete()
                            } else {
                                currentStep++
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentStep == 7) "Done" else "Next",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Onboarding Pages Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {
                // STEP 1: WELCOME
                1 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0C4CB)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFF5394ED), Color(0xFF1D6CE5))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Welcome to aTunes Legacy",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1D21),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Experience classic iTunes 11 & Apple Music on your Android device. Tap Next in the top right to set up your app.",
                                fontSize = 13.sp,
                                color = Color(0xFF636A78),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // STEP 2: PROFILE SETUP
                2 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0C4CB)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Profile Details",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1D21)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E6EE))
                                    .border(1.dp, Color(0xFFC0C4CB), CircleShape)
                                    .clickable { avatarPicker.launch("image/*") },
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
                                        tint = Color(0xFF1D6CE5),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap to select photo", fontSize = 11.sp, color = Color(0xFF1D6CE5), fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Display Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = bioInput,
                                onValueChange = { bioInput = it },
                                label = { Text("Bio / Status") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // STEP 3: MUSIC & AUDIO PERMISSIONS
                3 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0C4CB)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = Color(0xFF1D6CE5), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Music & Audio Access", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("aTunes Legacy needs permission to discover and play music files.", fontSize = 13.sp, color = Color(0xFF636A78), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))

                            if (isAudioGranted()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Permissions Granted :)", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 15.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            audioLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
                                        } else {
                                            audioLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6CE5)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Allow Music & Audio Permissions")
                                }
                            }
                        }
                    }
                }

                // STEP 4: PHOTOS & VIDEOS PERMISSIONS
                4 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0C4CB)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF1D6CE5), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Photos & Videos Access", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("aTunes Legacy needs permission to display your video library and gallery.", fontSize = 13.sp, color = Color(0xFF636A78), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))

                            if (isPhotosVideosGranted()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Permissions Granted :)", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 15.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            photosVideosLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO))
                                        } else {
                                            photosVideosLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6CE5)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Allow Photos & Videos Permissions")
                                }
                            }
                        }
                    }
                }

                // STEP 5: MICROPHONE PERMISSIONS
                5 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0C4CB)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF1D6CE5), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Microphone Access", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("aTunes Legacy needs microphone permission for recording audio and voice memos.", fontSize = 13.sp, color = Color(0xFF636A78), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))

                            if (isMicGranted()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Permissions Granted :)", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 15.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6CE5)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Allow Microphone Permission")
                                }
                            }
                        }
                    }
                }

                // STEP 6: NOTIFICATION PERMISSIONS
                6 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0C4CB)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF1D6CE5), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Notification Access", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("aTunes Legacy needs notification permission to show now-playing playback controls in your notification shade.", fontSize = 13.sp, color = Color(0xFF636A78), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))

                            if (isNotificationsGranted()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Permissions Granted :)", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 15.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            currentStep = 7
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6CE5)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Allow Notification Permissions")
                                }
                            }
                        }
                    }
                }

                // STEP 7: THEME SELECTION (Live Preview)
                7 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.surfaceBorder),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = themeColors.primaryAccent, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select Your Theme", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap any theme to preview its look in real-time:", fontSize = 12.sp, color = themeColors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))

                            AppTheme.entries.forEach { theme ->
                                val isSelected = theme == selectedTheme
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) themeColors.primaryAccent.copy(alpha = 0.2f) else themeColors.background)
                                        .border(1.dp, if (isSelected) themeColors.primaryAccent else themeColors.surfaceBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedTheme = theme
                                            viewModel.setTheme(theme)
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(theme.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = themeColors.textPrimary)
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = themeColors.primaryAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
