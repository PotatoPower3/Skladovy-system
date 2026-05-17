package cz.petrschopp.skladovysystem.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.math.max

private const val DETECTED_CODE_HOLD_MS = 500L

private data class DetectedBarcodeOverlay(
    val value: String,
    val boundingBox: Rect,
    val imageWidth: Int,
    val imageHeight: Int
)

@Composable
fun BarcodeCameraPreview(
    modifier: Modifier = Modifier,
    onCodeDetected: (String?) -> Unit
) {
    val context = LocalContext.current

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission.value = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission.value) {
        CameraPreviewContent(
            modifier = modifier,
            onCodeDetected = onCodeDetected
        )
    } else {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pro skenování povol kameru",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    onCodeDetected: (String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val lastDetectedCode = remember { mutableStateOf<String?>(null) }
    val lastDetectedAt = remember { mutableLongStateOf(0L) }
    val detectedOverlay = remember { mutableStateOf<DetectedBarcodeOverlay?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also { previewUseCase ->
                            previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val scanner = BarcodeScanning.getClient()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                analyzeImageProxy(
                                    imageProxy = imageProxy,
                                    scannerProcess = { image ->
                                        scanner.process(image)
                                    },
                                    lastDetectedCode = lastDetectedCode.value,
                                    lastDetectedAt = lastDetectedAt.longValue,
                                    onDetectedCodeChanged = { code, detectedAt ->
                                        lastDetectedCode.value = code
                                        lastDetectedAt.longValue = detectedAt
                                    },
                                    onOverlayChanged = { overlay ->
                                        detectedOverlay.value = overlay
                                    },
                                    onCodeDetected = onCodeDetected
                                )
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.04f))
        )

        BarcodeBoundingBoxOverlay(
            detectedOverlay = detectedOverlay.value,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = if (detectedOverlay.value == null) {
                "Namiř na QR nebo čárový kód"
            } else {
                "Kód rozpoznán"
            },
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeImageProxy(
    imageProxy: ImageProxy,
    scannerProcess: (InputImage) -> Task<List<Barcode>>,
    lastDetectedCode: String?,
    lastDetectedAt: Long,
    onDetectedCodeChanged: (String?, Long) -> Unit,
    onOverlayChanged: (DetectedBarcodeOverlay?) -> Unit,
    onCodeDetected: (String?) -> Unit
) {
    val mediaImage = imageProxy.image

    if (mediaImage == null) {
        emitHeldCodeOrNull(
            lastDetectedCode = lastDetectedCode,
            lastDetectedAt = lastDetectedAt,
            onCodeDetected = onCodeDetected,
            onExpired = {
                onDetectedCodeChanged(null, 0L)
                onOverlayChanged(null)
            }
        )

        imageProxy.close()
        return
    }

    val rotation = imageProxy.imageInfo.rotationDegrees

    val imageWidth = if (rotation == 90 || rotation == 270) {
        imageProxy.height
    } else {
        imageProxy.width
    }

    val imageHeight = if (rotation == 90 || rotation == 270) {
        imageProxy.width
    } else {
        imageProxy.height
    }

    val image = InputImage.fromMediaImage(
        mediaImage,
        rotation
    )

    scannerProcess(image)
        .addOnSuccessListener { barcodes: List<Barcode> ->
            val selectedBarcode = selectBarcodeClosestToCenter(
                barcodes = barcodes,
                imageWidth = imageWidth,
                imageHeight = imageHeight
            )

            val newCode = selectedBarcode?.rawValue
            val boundingBox = selectedBarcode?.boundingBox

            if (!newCode.isNullOrBlank() && boundingBox != null) {
                val now = System.currentTimeMillis()

                onDetectedCodeChanged(newCode, now)
                onOverlayChanged(
                    DetectedBarcodeOverlay(
                        value = newCode,
                        boundingBox = boundingBox,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight
                    )
                )
                onCodeDetected(newCode)
            } else {
                emitHeldCodeOrNull(
                    lastDetectedCode = lastDetectedCode,
                    lastDetectedAt = lastDetectedAt,
                    onCodeDetected = onCodeDetected,
                    onExpired = {
                        onDetectedCodeChanged(null, 0L)
                        onOverlayChanged(null)
                    }
                )
            }
        }
        .addOnFailureListener {
            emitHeldCodeOrNull(
                lastDetectedCode = lastDetectedCode,
                lastDetectedAt = lastDetectedAt,
                onCodeDetected = onCodeDetected,
                onExpired = {
                    onDetectedCodeChanged(null, 0L)
                    onOverlayChanged(null)
                }
            )
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun selectBarcodeClosestToCenter(
    barcodes: List<Barcode>,
    imageWidth: Int,
    imageHeight: Int
): Barcode? {
    val imageCenterX = imageWidth / 2
    val imageCenterY = imageHeight / 2

    val validBarcodes = barcodes.filter { barcode: Barcode ->
        !barcode.rawValue.isNullOrBlank() && barcode.boundingBox != null
    }

    return validBarcodes.minByOrNull { barcode: Barcode ->
        val box = barcode.boundingBox ?: return@minByOrNull Int.MAX_VALUE

        val dx = box.centerX() - imageCenterX
        val dy = box.centerY() - imageCenterY

        dx * dx + dy * dy
    }
}

private fun emitHeldCodeOrNull(
    lastDetectedCode: String?,
    lastDetectedAt: Long,
    onCodeDetected: (String?) -> Unit,
    onExpired: () -> Unit
) {
    val now = System.currentTimeMillis()

    if (!lastDetectedCode.isNullOrBlank() && now - lastDetectedAt <= DETECTED_CODE_HOLD_MS) {
        onCodeDetected(lastDetectedCode)
    } else {
        onExpired()
        onCodeDetected(null)
    }
}

@Composable
private fun BarcodeBoundingBoxOverlay(
    detectedOverlay: DetectedBarcodeOverlay?,
    modifier: Modifier = Modifier
) {
    if (detectedOverlay == null) {
        return
    }

    Canvas(modifier = modifier) {
        val sourceWidth = detectedOverlay.imageWidth.toFloat()
        val sourceHeight = detectedOverlay.imageHeight.toFloat()

        if (sourceWidth <= 0f || sourceHeight <= 0f) {
            return@Canvas
        }

        val scale = max(
            size.width / sourceWidth,
            size.height / sourceHeight
        )

        val displayedWidth = sourceWidth * scale
        val displayedHeight = sourceHeight * scale

        val offsetX = (size.width - displayedWidth) / 2f
        val offsetY = (size.height - displayedHeight) / 2f

        val box = detectedOverlay.boundingBox

        val left = offsetX + box.left * scale
        val top = offsetY + box.top * scale
        val right = offsetX + box.right * scale
        val bottom = offsetY + box.bottom * scale

        val width = right - left
        val height = bottom - top

        if (width <= 0f || height <= 0f) {
            return@Canvas
        }

        drawRoundRect(
            color = Color(0xFF4CAF50).copy(alpha = 0.18f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )

        drawRoundRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}