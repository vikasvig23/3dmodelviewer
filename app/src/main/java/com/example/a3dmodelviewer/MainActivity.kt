package com.example.a3dmodelviewer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position

private const val TAG = "MainActivity"

private val ButtonBase       = Color(0xFF1E2A4A)
private val ButtonTop        = Color(0xFF2E4080)
private val ButtonHighlight  = Color(0xFF5B7FFF)
private val ButtonShadow     = Color(0xFF08101F)
private val ButtonDisabled   = Color(0xFF1A1A2E)
private val ButtonTextActive = Color(0xFFE8EEFF)
private val ButtonTextOff    = Color(0xFF444466)
private val AccentGlow       = Color(0x664466FF)
private val PanelBg          = Color(0xF0090912)


private const val PICKER_BAR_HEIGHT_DP = 68f


private const val CONTAINER_SIZE_DP = 220f

class MainActivity : ComponentActivity() {

    private val vm by viewModels<ModelViewModel>()

    private val modelAssets = listOf(
        "models/chair.glb",
        "models/car.glb",
        "models/robot.glb",
        "models/table.glb",
        "models/dice.glb"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var sceneView   by remember { mutableStateOf<SceneView?>(null) }
            var engineError by remember { mutableStateOf<String?>(null) }


            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0D0D))
            ) {

                val canvasW = maxWidth.value
                val canvasH = maxHeight.value


                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory  = { ctx ->
                        try {
                            SceneView(ctx).also { sv ->
                                sv.cameraNode.position = Position(0f, 0f, 5f)
                                sceneView = sv
                                Log.d(TAG, "SceneView OK — canvas ${canvasW}×${canvasH}dp")
                            }
                        } catch (e: IllegalStateException) {
                            Log.e(TAG, "Filament engine creation failed", e)
                            engineError = buildString {
                                appendLine("Filament engine failed to start.")
                                appendLine()
                                appendLine("• android:hardwareAccelerated=\"true\" in Manifest")
                                appendLine("• ABI filters in build.gradle")
                                appendLine("• Emulator: AVD → Graphics → Hardware - GLES 2.0")
                                appendLine()
                                appendLine("ABI: ${System.getProperty("os.arch")}")
                                appendLine("Error: ${e.message}")
                            }
                            android.view.View(ctx)
                        }
                    },
                    update = {}
                )

                engineError?.let { msg ->
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(Color(0xEE0D0D0D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(msg, color = Color(0xFFEF5350), fontSize = 12.sp,
                            modifier = Modifier.padding(24.dp))
                    }
                }


                sceneView?.let { sv ->
                    for (index in vm.models.indices) {
                        val item = vm.models[index]
                        key(item.id) {
                            ModelContainer(
                                item           = item,
                                sharedSceneView = sv,
                                spawnIndex     = index,
                                canvasWidthDp  = canvasW,

                                canvasHeightDp = canvasH - PICKER_BAR_HEIGHT_DP,
                                onClose        = { vm.removeModel(item.id) }
                            )
                        }
                    }
                }

                ModelPickerBar(
                    assets     = modelAssets,
                    canAddMore = vm.models.size < 5 && engineError == null,
                    onAdd      = { vm.addModel(it) },
                    modifier   = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}


@Composable
fun TactileButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    depth: Dp = 5.dp,
    cornerRadius: Dp = 12.dp
) {
    var pressed by remember { mutableStateOf(false) }

    val sinkPx    by animateDpAsState(
        targetValue   = if (pressed && enabled) depth else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f), label = "sink")
    val scale     by animateFloatAsState(
        targetValue   = if (pressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f), label = "scale")
    val glowAlpha by animateFloatAsState(
        targetValue   = if (enabled) 1f else 0f,
        animationSpec = tween(300), label = "glow")

    val cr = cornerRadius
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .drawBehind {
                val dp = depth.toPx(); val sf = sinkPx.toPx()
                val crPx = cr.toPx(); val w = size.width; val h = size.height
                if (enabled) {
                    drawRoundRect(color = ButtonShadow.copy(alpha = 0.7f * (1f - sf / dp.coerceAtLeast(1f))),
                        topLeft = Offset(4f, dp + 4f - sf), size = Size(w, h), cornerRadius = CornerRadius(crPx))
                    drawRoundRect(color = AccentGlow.copy(alpha = 0.35f * glowAlpha),
                        topLeft = Offset(-3f, -3f), size = Size(w + 6f, h + dp + 6f), cornerRadius = CornerRadius(crPx + 3f))
                }
                drawRoundRect(color = if (enabled) ButtonShadow else Color(0xFF0A0A14),
                    topLeft = Offset(0f, sf), size = Size(w, h + dp - sf), cornerRadius = CornerRadius(crPx))
                drawRoundRect(
                    brush = if (enabled) Brush.verticalGradient(listOf(ButtonHighlight, ButtonTop, ButtonBase), sf, sf + h)
                    else Brush.verticalGradient(listOf(ButtonDisabled, ButtonDisabled.copy(alpha = 0.7f)), sf, sf + h),
                    topLeft = Offset(0f, sf), size = Size(w, h), cornerRadius = CornerRadius(crPx))
                if (enabled) drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.25f), Color.White.copy(0.18f), Color.Transparent)),
                    topLeft = Offset(0f, sf), size = Size(w, h * 0.35f), cornerRadius = CornerRadius(crPx))
            }
            .clip(RoundedCornerShape(cornerRadius))
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = { _ -> if (enabled) { pressed = true; tryAwaitRelease(); pressed = false } },
                    onTap   = { if (enabled) onClick() }
                )
            }
            .padding(bottom = depth)
    ) {
        Box(modifier = Modifier.offset(y = sinkPx).padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center) {
            Text(label, color = if (enabled) ButtonTextActive else ButtonTextOff,
                fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}


@Composable
fun ModelPickerBar(
    assets: List<String>,
    canAddMore: Boolean,
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .drawBehind {
            drawRect(brush = Brush.horizontalGradient(
                listOf(Color.Transparent, ButtonHighlight.copy(0.6f), Color.Transparent)),
                size = Size(size.width, 1.5f))
        }
        .background(PanelBg)
        .padding(vertical = 10.dp)
    ) {
        LazyRow(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            item {
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canAddMore) Color(0x33334499) else Color(0x33EF5350))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(if (canAddMore) "" else "Max 5",
                        color = if (canAddMore) Color(0xFF8899FF) else Color(0xFFEF5350),
                        fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp)
                }
            }
            items(assets) { asset ->
                TactileButton(
                    label   = asset.substringAfterLast("/").removeSuffix(".glb").replaceFirstChar { it.uppercase() },
                    onClick = { onAdd(asset) },
                    enabled = canAddMore
                )
            }
        }
    }
}