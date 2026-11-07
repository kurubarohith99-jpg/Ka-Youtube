package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.screens.DjMainScreen
import com.example.ui.theme.RohithDjProTheme
import com.example.viewmodel.DjViewModel

class MainActivity : ComponentActivity() {

    private val djViewModel: DjViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep screen active during DJ performance
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            RohithDjProTheme {
                DjMainScreen(
                    viewModel = djViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
