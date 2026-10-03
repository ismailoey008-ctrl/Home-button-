package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CircleSettings
import com.example.ui.HomeUiState
import com.example.ui.HomeViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.HomeCircleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HomeCircleTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check permissions when returning to app from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launcher for Android 13+ notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Continue
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshState() },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh status"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Master Activation Card
                MasterToggleCard(
                    uiState = uiState,
                    onToggle = { isChecked ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.toggleService(context, isChecked)
                    },
                    onRequestOverlayPermission = {
                        openOverlaySettings(context)
                    }
                )
            }

            // Overlay Permission Warning if missing
            if (!uiState.isOverlayPermissionGranted) {
                item {
                    OverlayPermissionAlertCard(
                        onOpenSettings = { openOverlaySettings(context) }
                    )
                }
            }

            // Interactive Live Preview Card
            item {
                CircleLivePreviewCard(
                    settings = uiState.settings,
                    onTestTap = {
                        // Feedback preview
                    }
                )
            }

            // Transparency Section
            item {
                TransparencySettingsCard(
                    settings = uiState.settings,
                    onActiveOpacityChange = { viewModel.updateOpacityActive(it) },
                    onIdleOpacityChange = { viewModel.updateOpacityIdle(it) },
                    onAutoFadeChange = { viewModel.updateAutoFade(it) }
                )
            }

            // Shape, Size & Color Section
            item {
                AppearanceSettingsCard(
                    settings = uiState.settings,
                    onSizeChange = { viewModel.updateSize(it) },
                    onColorChange = { viewModel.updateColor(it) },
                    onStyleChange = { viewModel.updateStyle(it) }
                )
            }

            // Responsiveness & Behavior Section
            item {
                BehaviorSettingsCard(
                    settings = uiState.settings,
                    onSnapChange = { viewModel.updateSnapToEdge(it) },
                    onHapticChange = { viewModel.updateHaptic(it) },
                    onDoubleTapActionChange = { viewModel.updateDoubleTapAction(it) },
                    onLongPressActionChange = { viewModel.updateLongPressAction(it) }
                )
            }

            // Optional Accessibility Card for Advanced Gestures
            item {
                AccessibilityStatusCard(
                    isGranted = uiState.isAccessibilityPermissionGranted,
                    onOpenAccessibility = { openAccessibilitySettings(context) }
                )
            }

            // Quick Test Button & Stats
            item {
                QuickTestSection(
                    totalClicks = uiState.settings.totalHomeClicks,
                    onTestHome = {
                        viewModel.triggerTestHome(context)
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MasterToggleCard(
    uiState: HomeUiState,
    onToggle: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    val isRunning = uiState.isServiceRunning
    val isPermissionGranted = uiState.isOverlayPermissionGranted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("master_toggle_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) Color(0xFF00E676) else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning)
                            stringResource(R.string.service_running)
                        else
                            stringResource(R.string.service_stopped),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning)
                        "Tombol lingkaran sedang aktif di layar."
                    else
                        "Aktifkan untuk menampilkan lingkaran tombol home.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRunning)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isRunning,
                onCheckedChange = { checked ->
                    if (checked && !isPermissionGranted) {
                        onRequestOverlayPermission()
                    } else {
                        onToggle(checked)
                    }
                },
                modifier = Modifier.testTag("service_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun OverlayPermissionAlertCard(
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.overlay_permission_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = stringResource(R.string.overlay_permission_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grant_overlay_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.grant_overlay_permission))
            }
        }
    }
}

@Composable
fun CircleLivePreviewCard(
    settings: CircleSettings,
    onTestTap: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isTapped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.preview_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "${settings.sizeDp}dp • ${(settings.opacityActive * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.preview_instruction),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas sandbox resembling phone screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141923))
                    .border(1.dp, Color(0xFF283244), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background grid pattern simulation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 24.dp.toPx()
                    for (x in 0..(size.width / step).toInt()) {
                        drawLine(
                            color = Color(0x15FFFFFF),
                            start = Offset(x * step, 0f),
                            end = Offset(x * step, size.height),
                            strokeWidth = 1f
                        )
                    }
                }

                // Interactive preview circle
                val scale by animateFloatAsState(
                    targetValue = if (isTapped) 0.88f else 1.0f,
                    label = "preview_scale"
                )

                Box(
                    modifier = Modifier
                        .size(settings.sizeDp.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isTapped = true
                                    tryAwaitRelease()
                                    isTapped = false
                                },
                                onTap = {
                                    onTestTap()
                                    coroutineScope.launch {
                                        isTapped = true
                                        delay(120)
                                        isTapped = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f
                        val baseRadius = (minOf(w, h) / 2f - 4.dp.toPx()) * scale
                        val baseColor = Color(settings.colorHex)
                        val activeAlpha = settings.opacityActive

                        // Shadow
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.35f),
                            radius = baseRadius + 1.dp.toPx(),
                            center = Offset(cx, cy + 2.dp.toPx())
                        )

                        when (settings.circleStyle) {
                            CircleSettings.STYLE_SOLID -> {
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha),
                                    radius = baseRadius,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha * 0.8f),
                                    radius = baseRadius,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                            CircleSettings.STYLE_RING -> {
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha * 0.25f),
                                    radius = baseRadius,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha),
                                    radius = baseRadius - 1.5.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 3.5.dp.toPx())
                                )
                            }
                            CircleSettings.STYLE_DOT -> {
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha * 0.25f),
                                    radius = baseRadius,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha),
                                    radius = baseRadius - 1.5.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha),
                                    radius = baseRadius * 0.38f,
                                    center = Offset(cx, cy)
                                )
                            }
                            CircleSettings.STYLE_DOUBLE_RING -> {
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha * 0.2f),
                                    radius = baseRadius,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha),
                                    radius = baseRadius - 1.5.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                                drawCircle(
                                    color = baseColor.copy(alpha = activeAlpha),
                                    radius = baseRadius * 0.55f,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }
                }

                // Temporary tap indicator
                if (isTapped) {
                    Text(
                        text = stringResource(R.string.preview_tapped_msg),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransparencySettingsCard(
    settings: CircleSettings,
    onActiveOpacityChange: (Float) -> Unit,
    onIdleOpacityChange: (Float) -> Unit,
    onAutoFadeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Opacity,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.appearance_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Active Opacity
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.opacity_active_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(settings.opacityActive * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = settings.opacityActive,
                    onValueChange = onActiveOpacityChange,
                    valueRange = 0.10f..1.0f,
                    modifier = Modifier.testTag("slider_active_opacity"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Idle Opacity
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.opacity_idle_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(settings.opacityIdle * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Slider(
                    value = settings.opacityIdle,
                    onValueChange = onIdleOpacityChange,
                    valueRange = 0.10f..1.0f,
                    modifier = Modifier.testTag("slider_idle_opacity"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Text(
                    text = stringResource(R.string.opacity_idle_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Auto Fade Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.auto_fade_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Memudar setelah 3 detik tidak disentuh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.autoFade,
                    onCheckedChange = onAutoFadeChange,
                    modifier = Modifier.testTag("switch_auto_fade")
                )
            }
        }
    }
}

@Composable
fun AppearanceSettingsCard(
    settings: CircleSettings,
    onSizeChange: (Int) -> Unit,
    onColorChange: (Long) -> Unit,
    onStyleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bentuk, Ukuran & Warna Lingkaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Size Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.circle_size_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${settings.sizeDp} dp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = settings.sizeDp.toFloat(),
                    onValueChange = { onSizeChange(it.roundToInt()) },
                    valueRange = 40f..80f,
                    steps = 8,
                    modifier = Modifier.testTag("slider_size")
                )
            }

            // Style Selection Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.circle_style_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip(
                        title = "Cincin (Ring)",
                        isSelected = settings.circleStyle == CircleSettings.STYLE_RING,
                        onClick = { onStyleChange(CircleSettings.STYLE_RING) }
                    )
                    StyleChip(
                        title = "Titik Pusat (Dot)",
                        isSelected = settings.circleStyle == CircleSettings.STYLE_DOT,
                        onClick = { onStyleChange(CircleSettings.STYLE_DOT) }
                    )
                    StyleChip(
                        title = "Penuh (Solid)",
                        isSelected = settings.circleStyle == CircleSettings.STYLE_SOLID,
                        onClick = { onStyleChange(CircleSettings.STYLE_SOLID) }
                    )
                    StyleChip(
                        title = "Cincin Ganda",
                        isSelected = settings.circleStyle == CircleSettings.STYLE_DOUBLE_RING,
                        onClick = { onStyleChange(CircleSettings.STYLE_DOUBLE_RING) }
                    )
                }
            }

            // Color Palette Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.circle_color_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                val colors = listOf(
                    0xFFFFFFFF to "Putih",
                    0xFF00E5FF to "Cyan",
                    0xFF00E676 to "Hijau",
                    0xFF2979FF to "Biru",
                    0xFFFFB300 to "Amber",
                    0xFFFF5252 to "Merah",
                    0xFFD500F9 to "Ungu",
                    0xFF212529 to "Hitam"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { (colorLong, name) ->
                        val isSelected = settings.colorHex == colorLong
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { onColorChange(colorLong) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = if (colorLong == 0xFFFFFFFF || colorLong == 0xFF00E5FF || colorLong == 0xFF00E676)
                                        Color.Black
                                    else
                                        Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StyleChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(title) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun BehaviorSettingsCard(
    settings: CircleSettings,
    onSnapChange: (Boolean) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onDoubleTapActionChange: (String) -> Unit,
    onLongPressActionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.behavior_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Snap to Edge Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.snap_to_edge_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.snap_to_edge_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.snapToEdge,
                    onCheckedChange = onSnapChange,
                    modifier = Modifier.testTag("switch_snap")
                )
            }

            // Haptic Feedback Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.haptic_feedback_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.haptic_feedback_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.hapticEnabled,
                    onCheckedChange = onHapticChange,
                    modifier = Modifier.testTag("switch_haptic")
                )
            }

            // Single Tap
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.single_tap_action_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.action_home),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Double Tap Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.double_tap_action_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip(
                        title = "Tidak Ada",
                        isSelected = settings.doubleTapAction == CircleSettings.ACTION_NONE,
                        onClick = { onDoubleTapActionChange(CircleSettings.ACTION_NONE) }
                    )
                    StyleChip(
                        title = "Recent Apps",
                        isSelected = settings.doubleTapAction == CircleSettings.ACTION_RECENTS,
                        onClick = { onDoubleTapActionChange(CircleSettings.ACTION_RECENTS) }
                    )
                    StyleChip(
                        title = "Tarik Notifikasi",
                        isSelected = settings.doubleTapAction == CircleSettings.ACTION_NOTIFICATIONS,
                        onClick = { onDoubleTapActionChange(CircleSettings.ACTION_NOTIFICATIONS) }
                    )
                    StyleChip(
                        title = "Kunci Layar",
                        isSelected = settings.doubleTapAction == CircleSettings.ACTION_LOCK,
                        onClick = { onDoubleTapActionChange(CircleSettings.ACTION_LOCK) }
                    )
                }
            }

            // Long Press Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.long_press_action_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip(
                        title = "Buka Pengaturan",
                        isSelected = settings.longPressAction == CircleSettings.ACTION_OPEN_SETTINGS,
                        onClick = { onLongPressActionChange(CircleSettings.ACTION_OPEN_SETTINGS) }
                    )
                    StyleChip(
                        title = "Recent Apps",
                        isSelected = settings.longPressAction == CircleSettings.ACTION_RECENTS,
                        onClick = { onLongPressActionChange(CircleSettings.ACTION_RECENTS) }
                    )
                    StyleChip(
                        title = "Tarik Notifikasi",
                        isSelected = settings.longPressAction == CircleSettings.ACTION_NOTIFICATIONS,
                        onClick = { onLongPressActionChange(CircleSettings.ACTION_NOTIFICATIONS) }
                    )
                    StyleChip(
                        title = "Tidak Ada",
                        isSelected = settings.longPressAction == CircleSettings.ACTION_NONE,
                        onClick = { onLongPressActionChange(CircleSettings.ACTION_NONE) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccessibilityStatusCard(
    isGranted: Boolean,
    onOpenAccessibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.accessibility_permission_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (isGranted) "Aktif" else "Opsional",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isGranted) Color(0xFF00E676) else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.accessibility_permission_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) {
                OutlinedButton(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.grant_accessibility_permission))
                }
            }
        }
    }
}

@Composable
fun QuickTestSection(
    totalClicks: Long,
    onTestHome: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onTestHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("test_home_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.test_home_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Total Ketukan Home: $totalClicks kali",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun openOverlaySettings(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}

private fun openAccessibilitySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
