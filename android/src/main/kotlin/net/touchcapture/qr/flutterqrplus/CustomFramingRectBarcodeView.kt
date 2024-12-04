package net.touchcapture.qr.flutterqrplus

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.util.AttributeSet
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.Size
import kotlin.math.min

class CustomFramingRectBarcodeView : BarcodeView {
    private var bottomOffset = BOTTOM_OFFSET_NOT_SET_VALUE

    val minZoomLevel: Double = 1.0
    private var maxZoomLevel: Int = 1
    private var isMaxLevelSet: Boolean = false

    constructor(context: Context?) : super(context)
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
            maxZoomLevel = params.maxZoom
            isMaxLevelSet = true
            if (params.isZoomSupported) {
                val maxZoom = params.maxZoom
                val newZoomLevel = min(maxZoom, zoomLevel.toInt())
                params.zoom = newZoomLevel
            }
            params
        }
    }

    fun getMaxZoomLevel(): Double {
        if (!isMaxLevelSet) {
            setZoomLevel(1.0)
        }
        return maxZoomLevel.toDouble()
    }


}