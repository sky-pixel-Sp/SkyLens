package id.sky.skylens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.FocusMeteringAction
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import id.sky.skylens.camera.CameraCapabilities
import id.sky.skylens.camera.CameraCapabilitySummary
import id.sky.skylens.camera.GalleryPhoto
import id.sky.skylens.camera.MediaStoreRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

private val Ink = Color(0xFF101319)
private val Panel = Color(0xFF1A2028)
private val Cream = Color(0xFFE8D5B5)
private val Muted = Color(0xFF9AA3AF)

private enum class CaptureMode {
    PHOTO, LIVE, VIDEO, PRO
}

private enum class AppLanguage { ENGLISH, INDONESIAN }

private fun tr(language: AppLanguage, english: String, indonesian: String): String =
    if (language == AppLanguage.INDONESIAN) indonesian else english

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 19, 25)
        window.navigationBarColor = android.graphics.Color.rgb(16, 19, 25)
        setContent {
            MaterialTheme {
                SkyLensApp()
            }
        }
    }
}

@Composable
private fun SkyLensApp() {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showPermissionMessage by remember { mutableStateOf(false) }
    val languagePreferences = remember {
        context.getSharedPreferences("skylens_preferences", android.content.Context.MODE_PRIVATE)
    }
    var language by remember {
        mutableStateOf(
            if (languagePreferences.getString(
                    "language",
                    if (Locale.getDefault().language in setOf("id", "in")) "ID" else "EN"
                ) == "ID") {
                AppLanguage.INDONESIAN
            } else {
                AppLanguage.ENGLISH
            }
        )
    }
    LaunchedEffect(language) {
        languagePreferences.edit()
            .putString("language", if (language == AppLanguage.INDONESIAN) "ID" else "EN")
            .apply()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        showPermissionMessage = !granted
    }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        if (hasCameraPermission) {
            CameraHome(language = language, onLanguageChange = { language = it })
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        language = if (language == AppLanguage.ENGLISH) AppLanguage.INDONESIAN else AppLanguage.ENGLISH
                    }) {
                        Text(if (language == AppLanguage.ENGLISH) "EN · English" else "ID · Indonesia", color = Cream)
                    }
                }
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Cream, modifier = Modifier.size(54.dp))
                Spacer(Modifier.height(18.dp))
                Text(tr(language, "SkyLens needs camera access", "SkyLens memerlukan akses kamera"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(tr(language, "Camera access is used to show the live preview and capture photos or videos.", "Akses kamera digunakan untuk menampilkan pratinjau langsung dan mengambil foto atau video."), color = Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(22.dp))
                Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                    Text(tr(language, "Allow camera", "Izinkan kamera"))
                }
                if (showPermissionMessage) {
                    Spacer(Modifier.height(10.dp))
                    Text(tr(language, "Permission was not granted. Enable Camera in Android Settings to use SkyLens.", "Izin belum diberikan. Aktifkan akses Kamera melalui Pengaturan Android untuk menggunakan SkyLens."), color = Cream, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun CameraHome(language: AppLanguage, onLanguageChange: (AppLanguage) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { MediaStoreRepository(context) }
    val mainExecutor: Executor = remember { ContextCompat.getMainExecutor(context) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var liveTempFile by remember { mutableStateOf<File?>(null) }
    var liveStem by remember { mutableStateOf<String?>(null) }
    var livePhotoSaved by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(CaptureMode.PHOTO) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashOn by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var exposure by remember { mutableFloatStateOf(0f) }
    var isRecordingVideo by remember { mutableStateOf(false) }
    var isMotionArmed by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(tr(language, "Ready", "Siap")) }
    LaunchedEffect(language) {
        status = tr(language, "Ready", "Siap")
    }
    var showGallery by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var gallery by remember { mutableStateOf(repository.recentPhotos()) }
    var capabilities by remember { mutableStateOf<CameraCapabilitySummary?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    fun refreshGallery() { gallery = repository.recentPhotos() }

    LaunchedEffect(previewView, lensFacing) {
        val view = previewView ?: return@LaunchedEffect
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.fromOrderedList(
                            listOf(Quality.FHD, Quality.HD),
                            androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                        )
                    )
                    .build()
                val video = VideoCapture.withOutput(recorder)
                val selector = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                provider.unbindAll()
                val boundCamera = provider.bindToLifecycle(
                    lifecycleOwner, selector, preview, capture, video
                )
                camera = boundCamera
                imageCapture = capture
                videoCapture = video
                boundCamera.cameraControl.setZoomRatio(zoom.coerceAtLeast(1f))
                capabilities = CameraCapabilities.scanRearCamera(context)
                status = tr(language, "Camera ready", "Kamera siap")
            } catch (error: Exception) {
                status = tr(language, "Camera error", "Kamera bermasalah") + ": ${error.localizedMessage ?: error.javaClass.simpleName}"
            }
        }, mainExecutor)
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            liveTempFile?.delete()
            cameraProvider?.unbindAll()
        }
    }

    fun stopLiveCapture(save: Boolean) {
        val recording = activeRecording
        if (recording == null) {
            isMotionArmed = false
            return
        }
        if (!save) livePhotoSaved = false
        recording.stop()
        isMotionArmed = false
    }

    fun armLiveCapture() {
        val video = videoCapture
        if (video == null || activeRecording != null) return
        val stem = makeFileStem()
        val file = File(context.cacheDir, "$stem.mp4")
        liveStem = stem
        liveTempFile = file
        livePhotoSaved = false
        status = tr(language, "Motion capture armed", "Pengambilan gerak diaktifkan")
        val options = FileOutputOptions.Builder(file).build()
        activeRecording = video.output.prepareRecording(context, options)
            .start(mainExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isMotionArmed = true
                        status = tr(language, "LIVE ready · press shutter", "LIVE siap · tekan tombol rana")
                    }
                    is VideoRecordEvent.Finalize -> {
                        isMotionArmed = false
                        val savedStill = livePhotoSaved
                        if (!event.hasError() && savedStill) {
                            runCatching { repository.publishMotionVideo(file, stem) }
                                .onSuccess {
                                    status = tr(language, "Motion pair saved", "Foto dan video gerak tersimpan")
                                    refreshGallery()
                                }
                                .onFailure { status = tr(language, "Motion clip not saved", "Video gerak gagal disimpan") + ": ${it.localizedMessage}" }
                        } else {
                            file.delete()
                            if (event.hasError()) status = tr(language, "Motion capture stopped with an error", "Pengambilan gerak berhenti karena kesalahan")
                        }
                        liveTempFile = null
                        liveStem = null
                        livePhotoSaved = false
                        activeRecording = null
                    }
                }
            }
    }

    LaunchedEffect(mode, videoCapture, activeRecording) {
        if (mode == CaptureMode.LIVE && videoCapture != null && activeRecording == null) {
            armLiveCapture()
        }
    }

    fun capturePhoto() {
        val capture = imageCapture ?: return
        if (isBusy || isRecordingVideo) return
        isBusy = true
        val stem = if (mode == CaptureMode.LIVE) liveStem ?: makeFileStem() else makeFileStem()
        val output = repository.photoOutputOptions(stem)
        capture.flashMode = if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        capture.takePicture(output, mainExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                isBusy = false
                status = tr(language, "Photo saved", "Foto tersimpan")
                refreshGallery()
                if (mode == CaptureMode.LIVE && activeRecording != null) {
                    livePhotoSaved = true
                    handler.postDelayed({ stopLiveCapture(save = true) }, 1_100L)
                }
            }
            override fun onError(exception: ImageCaptureException) {
                isBusy = false
                status = tr(language, "Photo failed", "Foto gagal diambil") + ": ${exception.message ?: tr(language, "capture error", "kesalahan pengambilan")}"
                if (mode == CaptureMode.LIVE) stopLiveCapture(save = false)
            }
        })
    }

    fun startVideo() {
        val video = videoCapture ?: return
        if (activeRecording != null) return
        val stem = makeFileStem()
        val options = repository.videoOutputOptions(stem)
        activeRecording = video.output.prepareRecording(context, options)
            .start(mainExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecordingVideo = true
                        status = tr(language, "Recording video", "Merekam video")
                    }
                    is VideoRecordEvent.Finalize -> {
                        isRecordingVideo = false
                        activeRecording = null
                        status = if (event.hasError()) tr(language, "Video save failed", "Video gagal disimpan") else tr(language, "Video saved", "Video tersimpan")
                        refreshGallery()
                    }
                }
            }
    }

    fun stopVideo() {
        activeRecording?.stop()
        isRecordingVideo = false
    }

    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("SKYLENS", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(tr(language, "CAMERA SYSTEM", "SISTEM KAMERA"), color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    onLanguageChange(if (language == AppLanguage.ENGLISH) AppLanguage.INDONESIAN else AppLanguage.ENGLISH)
                }) {
                    Text(if (language == AppLanguage.ENGLISH) "EN" else "ID", color = Cream, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = tr(language, "Camera capability information", "Informasi kemampuan kamera"), tint = Cream)
                }
                IconButton(onClick = { showGallery = !showGallery; refreshGallery() }) {
                    Icon(Icons.Default.Collections, contentDescription = tr(language, "Open gallery", "Buka galeri"), tint = Color.White)
                }
                IconButton(onClick = { flashOn = !flashOn }) {
                    Icon(if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = tr(language, "Toggle flash", "Aktif/nonaktifkan lampu kilat"), tint = if (flashOn) Cream else Color.White)
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp).clip(RoundedCornerShape(28.dp))) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).also {
                        it.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = it
                    }
                },
                update = { previewView = it }
            )
            Box(
                modifier = Modifier.fillMaxSize().pointerInput(camera, zoom) {
                    detectTapGestures { offset ->
                        val view = previewView ?: return@detectTapGestures
                        val point: MeteringPointFactory = view.meteringPointFactory
                        val action = FocusMeteringAction.Builder(point.createPoint(offset.x, offset.y))
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        camera?.cameraControl?.startFocusAndMetering(action)
                        status = tr(language, "Focus adjusted", "Fokus disesuaikan")
                    }
                }
            )
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mode == CaptureMode.LIVE) StatusPill("LIVE", Cream, Ink)
                if (mode == CaptureMode.PRO) StatusPill("PRO", Cream, Ink)
                StatusPill(
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) tr(language, "REAR", "BELAKANG") else tr(language, "FRONT", "DEPAN"),
                    Color.White.copy(alpha = .18f),
                    Color.White
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(1f, 2f, 3f).forEach { value ->
                        val selected = zoom == value
                        Text(
                            text = "${value.toInt()}×",
                            color = if (selected) Cream else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.clip(CircleShape)
                                .background(if (selected) Ink.copy(alpha = .82f) else Ink.copy(alpha = .38f))
                                .clickable {
                                    val maxZoom = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
                                    val appliedZoom = value.coerceAtMost(maxZoom).coerceAtLeast(1f)
                                    zoom = appliedZoom
                                    camera?.cameraControl?.setZoomRatio(appliedZoom)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showGallery) {
            GalleryPanel(gallery = gallery, language = language, onOpen = { uri -> openMedia(context, uri) }, onClose = { showGallery = false })
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Ink).navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(status, color = Muted, fontSize = 11.sp, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            if (mode == CaptureMode.PRO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Cream, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(tr(language, "EXPOSURE", "PENCAHAYAAN"), color = Muted, fontSize = 10.sp)
                    Spacer(Modifier.width(10.dp))
                    Slider(
                        value = exposure,
                        onValueChange = { value ->
                            exposure = value
                            val range = camera?.cameraInfo?.exposureState?.exposureCompensationRange
                            val index = value.toInt().coerceIn(range?.lower ?: -2, range?.upper ?: 2)
                            camera?.cameraControl?.setExposureCompensationIndex(index)
                        },
                        valueRange = -2f..2f,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = Cream,
                            activeTrackColor = Cream,
                            inactiveTrackColor = Panel
                        )
                    )
                    Text("${exposure.toInt()}.0", color = Cream, fontSize = 11.sp)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaptureMode.values().forEach { item ->
                    Text(
                        when (item) {
                            CaptureMode.PHOTO -> tr(language, "PHOTO", "FOTO")
                            CaptureMode.LIVE -> "LIVE"
                            CaptureMode.VIDEO -> tr(language, "VIDEO", "VIDEO")
                            CaptureMode.PRO -> "PRO"
                        },
                        color = if (mode == item) Cream else Muted,
                        fontSize = 11.sp,
                        fontWeight = if (mode == item) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.clip(CircleShape)
                            .background(if (mode == item) Panel else Color.Transparent)
                            .clickable {
                                if (activeRecording != null) {
                                    if (mode == CaptureMode.LIVE) stopLiveCapture(save = false) else stopVideo()
                                }
                                mode = item
                            }
                            .padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { refreshGallery(); showGallery = true }, modifier = Modifier.size(52.dp)) {
                    if (gallery.isNotEmpty()) {
                        AsyncImage(model = gallery.first().uri, contentDescription = tr(language, "Latest photo", "Foto terbaru"), modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)))
                    } else {
                        Icon(Icons.Default.Collections, contentDescription = tr(language, "Gallery", "Galeri"), tint = Color.White)
                    }
                }
                when (mode) {
                    CaptureMode.VIDEO -> {
                        IconButton(onClick = { if (isRecordingVideo) stopVideo() else startVideo() }, modifier = Modifier.size(76.dp)) {
                            Box(Modifier.size(66.dp).background(Color(0xFFD94C4C), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(if (isRecordingVideo) Icons.Default.Stop else Icons.Default.Videocam, contentDescription = if (isRecordingVideo) tr(language, "Stop recording", "Hentikan rekaman") else tr(language, "Start recording", "Mulai merekam"), tint = Color.White, modifier = Modifier.size(31.dp))
                            }
                        }
                    }
                    else -> {
                        IconButton(onClick = { capturePhoto() }, enabled = !isBusy && (mode != CaptureMode.LIVE || isMotionArmed), modifier = Modifier.size(78.dp)) {
                            Box(Modifier.size(68.dp).background(Cream, CircleShape).padding(5.dp).background(Ink, CircleShape).padding(4.dp).background(Cream, CircleShape))
                        }
                    }
                }
                IconButton(
                    onClick = {
                        if (activeRecording != null) {
                            if (mode == CaptureMode.LIVE) {
                                stopLiveCapture(save = false)
                                mode = CaptureMode.PHOTO
                            } else stopVideo()
                        }
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = tr(language, "Switch camera", "Ganti kamera"), tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }
        }
    }

    if (showInfo) {
        CapabilityDialog(capabilities = capabilities, language = language, onDismiss = { showInfo = false })
    }
}

@Composable
private fun StatusPill(text: String, background: Color, foreground: Color) {
    Text(text, color = foreground, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        modifier = Modifier.background(background, CircleShape).padding(horizontal = 10.dp, vertical = 6.dp))
}

@Composable
private fun GalleryPanel(gallery: List<GalleryPhoto>, language: AppLanguage, onOpen: (Uri) -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Panel).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(tr(language, "YOUR CAPTURES", "HASIL FOTO & VIDEO"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                Text(
                    if (language == AppLanguage.ENGLISH) "${gallery.size} photos · Motion pairs are marked" else "${gallery.size} foto · Pasangan video gerak ditandai",
                    color = Muted,
                    fontSize = 10.sp
                )
            }
            TextButton(onClick = onClose) { Text(tr(language, "CLOSE", "TUTUP"), color = Cream, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(10.dp))
        if (gallery.isEmpty()) {
            Text(tr(language, "No SkyLens photos yet. Take your first shot.", "Belum ada foto SkyLens. Ambil foto pertamamu."), color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 20.dp))
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(184.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(gallery, key = { it.uri.toString() }) { photo ->
                    Box {
                        AsyncImage(model = photo.uri, contentDescription = photo.displayName, modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(9.dp)).clickable { onOpen(photo.uri) })
                        if (photo.motionVideoUri != null) {
                            Text("LIVE", color = Ink, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Cream, CircleShape).padding(horizontal = 5.dp, vertical = 3.dp).clickable { onOpen(photo.motionVideoUri) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityDialog(capabilities: CameraCapabilitySummary?, language: AppLanguage, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text(tr(language, "DEVICE CAMERA CAPABILITIES", "KEMAMPUAN KAMERA PERANGKAT"), color = Color.White, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                if (capabilities == null) {
                    Text(tr(language, "Camera capability information is not available yet.", "Informasi kemampuan kamera belum tersedia."), color = Muted)
                } else {
                    val reported = tr(language, "Not reported", "Tidak dilaporkan")
                    CapabilityRow(tr(language, "Maximum JPEG stream", "Resolusi JPEG maksimum"), capabilities.largestJpeg?.let { "${it.width} × ${it.height}" } ?: reported)
                    CapabilityRow("RAW / DNG", supported(capabilities.supportsRaw, language))
                    CapabilityRow(tr(language, "Manual sensor", "Sensor manual"), supported(capabilities.supportsManualSensor, language))
                    CapabilityRow(tr(language, "Optical stabilization", "Stabilisasi optik"), supported(capabilities.supportsOis, language))
                    CapabilityRow(tr(language, "Video stabilization", "Stabilisasi video"), supported(capabilities.supportsVideoStabilization, language))
                    CapabilityRow("120 fps", supported(capabilities.supports120Fps, language))
                    CapabilityRow(tr(language, "High-speed ranges", "Rentang kecepatan tinggi"), capabilities.highSpeedFpsRanges.joinToString { "${it.lower}–${it.upper} fps" }.ifBlank { reported })
                }
                Text(
                    tr(language,
                        "These are Camera2 hardware reports. A supported capability does not mean every capture mode is already enabled in this app.",
                        "Informasi ini berasal dari laporan perangkat keras Camera2. Kemampuan yang terdeteksi belum tentu sudah tersedia sebagai mode pengambilan gambar di aplikasi."
                    ),
                    color = Muted,
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr(language, "DONE", "SELESAI"), color = Cream) } }
    )
}

@Composable
private fun CapabilityRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun supported(value: Boolean, language: AppLanguage) =
    if (value) tr(language, "Supported", "Didukung") else tr(language, "Not reported", "Tidak dilaporkan")

private fun makeFileStem(): String = "SKY_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())

private fun openMedia(context: android.content.Context, uri: Uri) {
    val mime = context.contentResolver.getType(uri) ?: "*/*"
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }
}
