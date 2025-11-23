package week11.st185898.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import week11.st185898.finalproject.ui.MainViewModel
import week11.st185898.finalproject.ui.SmartCampusApp
import week11.st185898.finalproject.ui.theme.FinalProjectTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjectTheme {
                SmartCampusApp(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FinalProjectTheme {
        // Preview placeholder
    }
}