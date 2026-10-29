package com.example.atuneslegacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.atuneslegacy.ui.MainScreen
import com.example.atuneslegacy.ui.theme.ATunesLegacyTheme
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissions = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            val currentTheme by mainViewModel.currentTheme.collectAsState()
            ATunesLegacyTheme(appTheme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}
