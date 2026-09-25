package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketplaceViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val initialRoute = intent?.data?.path
    setContent {
      MyApplicationTheme(darkTheme = true) {
        val viewModel: MarketplaceViewModel = viewModel()
        MainApp(viewModel = viewModel, initialRoute = initialRoute)
      }
    }
  }
}

