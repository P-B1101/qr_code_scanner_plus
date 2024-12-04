package net.touchcapture.qr.flutterqrplus

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.util.AttributeSet
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.Size
import kotlin.math.abs
import kotlin.math.min

class CustomFramingRectBarcodeView : BarcodeView {
    private var bottomOffset = BOTTOM_OFFSET_NOT_SET_VALUE

    private val minZoomIndex: Int = 0

    private var maxZoomIndex: Int? = null
    private var zoomLevels: List<Int>? = null
    private var minZoom: Double? = null
    private var maxZoom: Double? = null

    constructor(context: Context?) : super(context) {
        initializeZoom()

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun calculateFramingRect(container: Rect, surface: Rect): Rect {
        val containerArea = Rect(container)
        val intersects =
            containerArea.intersect(surface) //adjusts the containerArea (code from super.calculateFramingRect)
        val scanAreaRect = super.calculateFramingRect(container, surface)
        if (bottomOffset != BOTTOM_OFFSET_NOT_SET_VALUE) { //if the setFramingRect function was called, then we shift the scan area by Y
            val scanAreaRectWithOffset = Rect(scanAreaRect)
            scanAreaRectWithOffset.bottom -= bottomOffset
            scanAreaRectWithOffset.top -= bottomOffset
            val belongsToContainer = scanAreaRectWithOffset.intersect(containerArea)
            if (belongsToContainer) {
                return scanAreaRectWithOffset
            }
        }
        return scanAreaRect
    }

    fun setFramingRect(rectWidth: Int, rectHeight: Int, bottomOffset: Int) {
        this.bottomOffset = bottomOffset
        framingRectSize = Size(rectWidth, rectHeight)
    }

    companion object {
        private const val BOTTOM_OFFSET_NOT_SET_VALUE = -1
    }

    fun setZoomLevel(zoomLevel: Double) {
        val camera = cameraInstance ?: return
        camera.changeCameraParameters { params: Camera.Parameters ->
            try {
                if (params.isZoomSupported) {
                    val zoom = findIndex(zoomLevel)
                    if (zoom != -1) {
                        val maxZoom = params.maxZoom
                        val newZoomLevel = min(maxZoom, zoom)
                        params.zoom = newZoomLevel
                    }
                }
            } catch (e: Exception) {
                print(e)
            }
            params
        }
    }

    fun getMaxZoomLevel(): Double? {
        if (zoomLevels == null) {
            initializeZoom()
            return null
        }
        if (maxZoom != null) return maxZoom as Double
        maxZoom = (maxZoomIndex?.let { zoomLevels?.get(it) } ?: 100) / 100.0
        return maxZoom as Double
    }

    fun getMinZoomLevel(): Double? {
        if (zoomLevels == null) {
            initializeZoom()
            return null
        }
        if (minZoom != null) return minZoom as Double
        minZoom = (zoomLevels?.get(minZoomIndex) ?: 100) / 100.0
        return minZoom as Double
    }

    private fun initializeZoom() {
        if (zoomLevels != null) return
        cameraInstance?.changeCameraParameters { params: Camera.Parameters ->
            maxZoomIndex = params.maxZoom
            zoomLevels = params.zoomRatios
            params
        }
    }

    private fun findIndex(zoomLevel: Double): Int {
        if (zoomLevels == null) {
            initializeZoom()
            return -1
        }
        val realValue = (zoomLevel * 100).toInt()
        val nearValue = (zoomLevels as List<Int>).minByOrNull { abs(it - realValue) } ?: -1
        val result = (zoomLevels as List<Int>).indexOf(nearValue)
        return result
    }

}