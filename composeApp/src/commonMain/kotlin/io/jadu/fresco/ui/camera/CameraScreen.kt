package io.jadu.fresco.ui.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.jadu.fresco.domain.classification.ClassificationResult
import io.jadu.fresco.platform.camera.SystemNavigator
import io.jadu.fresco.viewmodel.CameraUiState
import io.jadu.fresco.viewmodel.CameraViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = koinViewModel(),
    systemNavigator: SystemNavigator = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CameraUiState.PermissionPrimer -> CameraPermissionPrimer(
            onAllow = viewModel::onPermissionPrimerAccepted,
            onDismiss = viewModel::onPermissionPrimerDismissed
        )

        is CameraUiState.RequestingPermission -> RequestCameraPermission(
            onGranted = viewModel::onPermissionGranted,
            onDenied = viewModel::onPermissionDenied
        )

        is CameraUiState.PermanentlyDenied -> PermanentlyDeniedPane(
            onOpenSettings = systemNavigator::openAppSettings
        )

        is CameraUiState.Preview -> PreviewPane(
            onCapture = viewModel::onCaptureRequested
        )

        is CameraUiState.Processing -> ProcessingPane("Processing image...")

        is CameraUiState.Classifying -> ProcessingPane("Identifying...")

        is CameraUiState.Classified -> ClassifiedPane(
            results = state.results,
            onRetake = viewModel::onRetry
        )

        is CameraUiState.Error -> ErrorPane(
            message = state.message,
            onRetry = viewModel::onRetry
        )
    }
}

@Composable
private fun PermanentlyDeniedPane(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera Access Blocked",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Camera access was denied. To use Fresco, enable it in your device Settings.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Open Settings")
        }
    }
}

@Composable
private fun PreviewPane(onCapture: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize())
        Button(
            onClick = onCapture,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text("Identify")
        }
    }
}

@Composable
private fun ProcessingPane(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ClassifiedPane(results: List<ClassificationResult>, onRetake: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (results.isEmpty()) {
            Text("No confident predictions", style = MaterialTheme.typography.headlineSmall)
        } else {
            val top = results.first()
            Text(
                text = top.label.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(top.confidence * 100).toInt()}% confidence",
                style = MaterialTheme.typography.titleMedium
            )
            if (top.isFruitOrVegetable) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Fruit / Vegetable",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (results.size > 1) {
                Spacer(Modifier.height(16.dp))
                Text("Other predictions:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                results.drop(1).forEach { result ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = result.label.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetake, modifier = Modifier.fillMaxWidth()) {
            Text("Retake")
        }
    }
}

@Composable
private fun ErrorPane(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Try Again")
        }
    }
}
