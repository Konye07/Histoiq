package com.konye.historiq.Features.Play.BlindMapBattle

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsGamesFlagTerritoryBlindmap
import com.konye.historiq.Core.Theme.AppColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * The bundled map (world_map.png) is a black-land/white-ocean silhouette rather than a colored
 * physical map, so it's tinted here via a duotone ColorMatrix: black -> continent color (ForestDark),
 * white -> water color (SlateBlue), linearly in between. Real coastlines from Natural Earth data,
 * Wikimedia Commons, CC BY-SA 4.0.
 */
private val MAP_COLOR_MATRIX = ColorMatrix(
    floatArrayOf(
        (120f - 30f) / 255f, 0f, 0f, 0f, 30f,
        0f, (140f - 42f) / 255f, 0f, 0f, 42f,
        0f, 0f, (158f - 32f) / 255f, 0f, 32f,
        0f, 0f, 0f, 1f, 0f
    )
)

internal const val MAP_MIN_SCALE = 1f

/** The map image is only 1280 px wide, so past a point more zoom just softens the coastline; it still places a pin more exactly. */
internal const val MAP_MAX_SCALE = 40f

internal val GuessPinColor = Color(0xFFFFC107)
internal val AnswerPinColor = Color(0xFFE53935)

private const val BUTTON_ZOOM_STEP = 2f
private const val ZOOM_ANIMATION_MILLIS = 320

/** Where a place sits on the map's own canvas (the map fills a [size] box, equirectangular). */
internal fun latLngToMapPoint(lat: Double, lng: Double, size: IntSize): Offset {
    val x = ((lng + 180.0) / 360.0 * size.width).toFloat()
    val y = ((90.0 - lat) / 180.0 * size.height).toFloat()
    return Offset(x, y)
}

internal fun mapPointToLatLng(point: Offset, size: IntSize): Pair<Double, Double> {
    val lng = (point.x / size.width) * 360.0 - 180.0
    val lat = 90.0 - (point.y / size.height) * 180.0
    return lat to lng
}

/**
 * Zoom and pan of a [ZoomableWorldMap]. "Map points" are positions on the unzoomed map canvas and
 * "screen points" are positions in the visible box; [toScreen]/[toMap] convert between the two, so
 * pins can be drawn at a constant size however far the map is zoomed.
 */
@Stable
internal class MapViewport(private val scope: CoroutineScope) {
    var scale by mutableFloatStateOf(MAP_MIN_SCALE)
        private set
    var pan by mutableStateOf(Offset.Zero)
        private set
    var size by mutableStateOf(IntSize.Zero)

    val canZoomIn: Boolean get() = scale < MAP_MAX_SCALE - 0.01f
    val canZoomOut: Boolean get() = scale > MAP_MIN_SCALE + 0.01f

    private var animation: Job? = null
    private var goalScale = MAP_MIN_SCALE
    private var goalPan = Offset.Zero

    fun toScreen(mapPoint: Offset): Offset = pan + mapPoint * scale

    fun toMap(screenPoint: Offset): Offset = (screenPoint - pan) / scale

    private fun clampPan(pan: Offset, scale: Float): Offset {
        val minX = size.width * (1f - scale)
        val minY = size.height * (1f - scale)
        return Offset(pan.x.coerceIn(min(minX, 0f), 0f), pan.y.coerceIn(min(minY, 0f), 0f))
    }

    /** A finger gesture: keeps the point under [centroid] where it is, so zooming follows the fingers. */
    fun transformBy(centroid: Offset, panChange: Offset, zoomChange: Float) {
        animation?.cancel()
        val newScale = (scale * zoomChange).coerceIn(MAP_MIN_SCALE, MAP_MAX_SCALE)
        val mapPointUnderCentroid = toMap(centroid)
        pan = clampPan(centroid + panChange - mapPointUnderCentroid * newScale, newScale)
        scale = newScale
        goalScale = newScale
        goalPan = pan
    }

    /**
     * The zoom buttons: one step in or out, from where a running zoom is heading. The point under [focus] (in map
     * coordinates) stays where it is on screen while it is visible, so a pin being placed does not drift away;
     * otherwise the middle of the box stays put.
     */
    fun zoomBy(factor: Float, focus: Offset? = null) {
        if (size == IntSize.Zero) return
        val animating = animation?.isActive == true
        val baseScale = if (animating) goalScale else scale
        val basePan = if (animating) goalPan else pan
        val target = (baseScale * factor).coerceIn(MAP_MIN_SCALE, MAP_MAX_SCALE)
        val focusOnScreen = focus
            ?.let { basePan + it * baseScale }
            ?.takeIf { it.x in 0f..size.width.toFloat() && it.y in 0f..size.height.toFloat() }
        val fixed = focusOnScreen ?: Offset(size.width / 2f, size.height / 2f)
        val mapPointAtFixed = (fixed - basePan) / baseScale
        animateTo(target, fixed - mapPointAtFixed * target, animated = true)
    }

    /**
     * Zooms so every one of [mapPoints] is on screen, with a pin's width of room round the outermost ones, but
     * no closer than [maxScale]. Points that sit on top of each other just zoom to [maxScale].
     */
    fun frame(mapPoints: List<Offset>, maxScale: Float, animated: Boolean) {
        if (size == IntSize.Zero || mapPoints.isEmpty()) return
        val minX = mapPoints.minOf { it.x }
        val maxX = mapPoints.maxOf { it.x }
        val minY = mapPoints.minOf { it.y }
        val maxY = mapPoints.maxOf { it.y }
        val roomOnScreen = size.width * 0.07f
        val spreadX = (maxX - minX) * 1.3f
        val spreadY = (maxY - minY) * 1.3f
        val fitX = if (spreadX > 0f) (size.width - 2f * roomOnScreen) / spreadX else MAP_MAX_SCALE
        val fitY = if (spreadY > 0f) (size.height - 2f * roomOnScreen) / spreadY else MAP_MAX_SCALE
        val target = min(fitX, fitY).coerceIn(MAP_MIN_SCALE, min(maxScale, MAP_MAX_SCALE))
        val center = Offset((minX + maxX) / 2f, (minY + maxY) / 2f)
        animateTo(target, Offset(size.width / 2f, size.height / 2f) - center * target, animated)
    }

    fun reset(animated: Boolean) = animateTo(MAP_MIN_SCALE, Offset.Zero, animated)

    private fun animateTo(targetScale: Float, targetPan: Offset, animated: Boolean) {
        animation?.cancel()
        val startScale = scale
        val startPan = pan
        val endPan = clampPan(targetPan, targetScale)
        goalScale = targetScale
        goalPan = endPan
        if (!animated) {
            scale = targetScale
            pan = endPan
            return
        }
        animation = scope.launch {
            animate(0f, 1f, animationSpec = tween(ZOOM_ANIMATION_MILLIS)) { progress, _ ->
                val s = startScale * (targetScale / startScale).pow(progress)
                scale = s
                pan = clampPan(startPan + (endPan - startPan) * progress, s)
            }
        }
    }
}

/**
 * Pinch and drag, like Compose's own transform detector, except that a single finger on a map that is not
 * zoomed in is left alone: there is nothing to pan, so the page the map sits in can scroll under it.
 */
private suspend fun PointerInputScope.detectMapGestures(
    panWithOneFinger: () -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
    awaitEachGesture {
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled && (event.changes.size > 1 || panWithOneFinger())) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange
                    val zoomMotion = abs(1f - zoom) * event.calculateCentroidSize(useCurrent = false)
                    if (zoomMotion > touchSlop || pan.getDistance() > touchSlop) pastTouchSlop = true
                }

                if (pastTouchSlop) {
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(event.calculateCentroid(useCurrent = false), panChange, zoomChange)
                    }
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
    }
}

/**
 * The world map in a 2:1 box that can be pinched, dragged and stepped in and out with the two buttons in its
 * corner. [onTap] gets a tapped point in map coordinates (null = the map is only for looking at), [zoomFocus]
 * is the map point the buttons zoom around when it is on screen (the middle of the box otherwise), and
 * [drawOverlay] draws on top in screen coordinates, so pins keep their size at every zoom.
 */
@Composable
internal fun ZoomableWorldMap(
    worldMap: ImageBitmap,
    viewport: MapViewport,
    modifier: Modifier = Modifier,
    onTap: ((Offset) -> Unit)? = null,
    zoomFocus: Offset? = null,
    drawOverlay: DrawScope.() -> Unit = {}
) {
    val currentOnTap by rememberUpdatedState(onTap)
    val tint = remember { ColorFilter.colorMatrix(MAP_COLOR_MATRIX) }

    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clipToBounds()
            .onGloballyPositioned { viewport.size = it.size }
            .pointerInput(viewport) {
                detectMapGestures(
                    panWithOneFinger = { viewport.canZoomOut },
                    onGesture = { centroid, pan, zoom -> viewport.transformBy(centroid, pan, zoom) }
                )
            }
            .pointerInput(viewport) {
                detectTapGestures { position -> currentOnTap?.invoke(viewport.toMap(position)) }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(color = AppColors.SlateBlue)
            val mapSize = viewport.size
            if (mapSize.width > 0) {
                withTransform({
                    translate(left = viewport.pan.x, top = viewport.pan.y)
                    scale(viewport.scale, viewport.scale, pivot = Offset.Zero)
                }) {
                    drawImage(
                        image = worldMap,
                        dstSize = mapSize,
                        colorFilter = tint,
                        // Smoother edges once the 1280 px image is blown up well past its own size.
                        filterQuality = if (viewport.scale > 3f) FilterQuality.High else FilterQuality.Medium
                    )
                }
                drawOverlay()
            }
        }
        Row(
            Modifier.align(Alignment.BottomEnd).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ZoomButton("+", StringsGamesFlagTerritoryBlindmap.blindmap_zoom_in, viewport.canZoomIn) { viewport.zoomBy(BUTTON_ZOOM_STEP, zoomFocus) }
            ZoomButton("−", StringsGamesFlagTerritoryBlindmap.blindmap_zoom_out, viewport.canZoomOut) { viewport.zoomBy(1f / BUTTON_ZOOM_STEP, zoomFocus) }
        }
    }
}

@Composable
private fun ZoomButton(glyph: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = if (enabled) 0.55f else 0.3f))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.5f else 0.25f), CircleShape)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            glyph,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.5f)
        )
    }
}

/**
 * A pin drawn in screen coordinates: a [color] dot with a white rim, so it shows on land and water alike.
 * The real place gets an [extraRadius], so a guess that lands right on it still leaves a ring of red showing.
 */
internal fun DrawScope.drawPin(center: Offset, color: Color, extraRadius: Float = 0f) {
    drawCircle(Color.White, radius = 7.5.dp.toPx() + extraRadius, center = center)
    drawCircle(color, radius = 6.dp.toPx() + extraRadius, center = center)
}

/** A pin that carries a number, sized to fit [label] (already measured with the text style it is drawn in). */
internal fun DrawScope.drawNumberedPin(
    center: Offset,
    color: Color,
    textColor: Color,
    label: TextLayoutResult,
    extraRadius: Float = 0f
) {
    val radius = max(label.size.width, label.size.height) / 2f + 3.dp.toPx() + extraRadius
    drawCircle(Color.White, radius = radius + 1.5.dp.toPx(), center = center)
    drawCircle(color, radius = radius, center = center)
    drawText(
        label,
        color = textColor,
        topLeft = Offset(center.x - label.size.width / 2f, center.y - label.size.height / 2f)
    )
}

/** Says which dot is the player's pin and which is the real place. */
@Composable
internal fun MapLegend(modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendEntry(GuessPinColor, StringsGamesFlagTerritoryBlindmap.blindmap_legend_your_pin)
        LegendEntry(AnswerPinColor, StringsGamesFlagTerritoryBlindmap.blindmap_legend_actual)
    }
}

@Composable
private fun LegendEntry(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
