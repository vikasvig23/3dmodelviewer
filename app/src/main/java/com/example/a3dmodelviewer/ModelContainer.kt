package com.example.a3dmodelviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode

private const val MIN_SIZE_DP  = 120f
private const val MAX_SIZE_DP  = 500f
private const val CONTAINER_SIZE_DP = 220f   // initial container size
private const val ROTATE_SENS  = 0.30f
private const val SCALE_MIN    = 0.10f
private const val SCALE_MAX    = 8f
private const val WORLD_PER_DP = 0.006f

/**
 * Computes the top-left offset (in dp) so the container spawns at the
 * centre of the visible canvas, with a small per-model stagger so multiple
 * models don't pile perfectly on top of each other.
 *
 * Formula:
 *   centre = canvasDimension / 2
 *   topLeft = centre - containerSize / 2          ← puts the box centred
 *   stagger = spawnIndex * 24                     ← slight diagonal offset
 *   clamp ≥ 0 so the box never spawns off-screen
 */
private fun centredOffset(
    canvasDp: Float,
    containerSizeDp: Float = CONTAINER_SIZE_DP,
    spawnIndex: Int = 0
): Float {
    val centre  = canvasDp / 2f
    val topLeft = centre - containerSizeDp / 2f
    val stagger = spawnIndex * 24f          // dp
    return (topLeft + stagger).coerceAtLeast(0f)
}

@Composable
fun ModelContainer(
    item: ModelItem,
    sharedSceneView: SceneView,
    spawnIndex: Int    = 0,
    canvasWidthDp: Float,
    canvasHeightDp: Float,
    onClose: () -> Unit
) {
    val density = LocalDensity.current


    var offsetX by remember {
        mutableStateOf(centredOffset(canvasWidthDp,  spawnIndex = spawnIndex))
    }
    var offsetY by remember {
        mutableStateOf(centredOffset(canvasHeightDp, spawnIndex = spawnIndex))
    }

    var containerSize     by remember { mutableStateOf(CONTAINER_SIZE_DP) }
    var isInteractionMode by remember { mutableStateOf(false) }
    var isLoading         by remember { mutableStateOf(true) }
    var loadFailed        by remember { mutableStateOf(false) }
    var modelNode         by remember { mutableStateOf<ModelNode?>(null) }

    val latestMode by rememberUpdatedState(isInteractionMode)
    val latestNode by rememberUpdatedState(modelNode)


    LaunchedEffect(item.assetFile) {
        isLoading  = true
        loadFailed = false
        runCatching {
            val instance = sharedSceneView.modelLoader.loadModelInstance(item.assetFile)
                ?: error("null instance for ${item.assetFile}")
            val node = ModelNode(modelInstance = instance, scaleToUnits = 1f).also {
                val half = containerSize / 2f
                it.position = Position(
                    x =  (offsetX + half) * WORLD_PER_DP - 2f,
                    y = -((offsetY + half) * WORLD_PER_DP - 2f),
                    z = 0f
                )
            }
            sharedSceneView.addChildNode(node)
            modelNode = node
        }.onFailure { loadFailed = true }
        isLoading = false
    }


    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                modelNode?.let {
                    sharedSceneView.removeChildNode(it)
                    it.destroy()
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(containerSize.dp)
            .border(
                width = if (isInteractionMode) 2.dp else 1.dp,
                color = if (isInteractionMode) Color(0xFF29B6F6) else Color(0x668888AA),
                shape = RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))

            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (!latestMode) {

                        with(density) {
                            val dx = pan.x.toDp().value
                            val dy = pan.y.toDp().value
                            offsetX = (offsetX + dx).coerceAtLeast(0f)
                            offsetY = (offsetY + dy).coerceAtLeast(0f)
                            latestNode?.let { n ->
                                val p = n.position
                                n.position = Position(p.x + dx * WORLD_PER_DP, p.y - dy * WORLD_PER_DP, p.z)
                            }
                        }

                        containerSize = (containerSize * zoom).coerceIn(MIN_SIZE_DP, MAX_SIZE_DP)
                        latestNode?.let { n ->
                            val ns = (n.scale.x * zoom).coerceIn(SCALE_MIN, SCALE_MAX)
                            n.scale = Scale(ns, ns, ns)
                        }
                    } else {

                        val node = latestNode ?: return@detectTransformGestures
                        with(density) {
                            val r = node.rotation
                            node.rotation = Rotation(
                                x = r.x - pan.y.toDp().value * ROTATE_SENS,
                                y = r.y + pan.x.toDp().value * ROTATE_SENS,
                                z = r.z
                            )
                        }
                        val ns = (node.scale.x * zoom).coerceIn(SCALE_MIN, SCALE_MAX)
                        node.scale = Scale(ns, ns, ns)
                    }
                }
            }
    ) {

        Box(modifier = Modifier.fillMaxSize().background(Color(0x22000000)))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(if (isInteractionMode) Color(0xDD0D2A4A) else Color(0xDD13131F))
                .padding(horizontal = 8.dp)
                .align(Alignment.TopCenter),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = item.assetFile.substringAfterLast("/").removeSuffix(".glb"),
                color      = Color(0xFF8888AA),
                fontSize   = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f)
            )


            TextButton(
                onClick        = { isInteractionMode = !isInteractionMode },
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier       = Modifier.height(32.dp)
            ) {
                Text(
                    text     = if (isInteractionMode) "↔ Move" else "↺ Rotate",
                    fontSize = 10.sp,
                    color    = if (isInteractionMode) Color(0xFF29B6F6) else Color(0xFFCCCCCC)
                )
            }


            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Text("✕", color = Color(0xFFEF5350), fontSize = 14.sp)
            }
        }


        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(30.dp).align(Alignment.Center),
                color       = Color.White,
                strokeWidth = 2.dp
            )
        }


        if (loadFailed) {
            Text("Load failed", color = Color(0xFFEF5350), fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Center))
        }


        if (isInteractionMode) {
            Text(
                text     = "ROTATE  •  ZOOM",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .background(Color(0xBB0D47A1), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color         = Color(0xFFBBDEFB),
                fontSize      = 8.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}