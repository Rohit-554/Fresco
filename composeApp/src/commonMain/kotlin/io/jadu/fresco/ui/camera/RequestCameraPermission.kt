package io.jadu.fresco.ui.camera

import androidx.compose.runtime.Composable

@Composable
expect fun RequestCameraPermission(
    onGranted: () -> Unit,
    onDenied: (isPermanent: Boolean) -> Unit
)
