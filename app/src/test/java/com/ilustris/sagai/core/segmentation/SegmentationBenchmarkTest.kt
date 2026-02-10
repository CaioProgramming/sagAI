package com.ilustris.sagai.core.segmentation

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class SegmentationBenchmarkTest {

    class PixelGrid(val width: Int, val height: Int) {
        private val data = IntArray(width * height)

        init {
            // Fill with random "pixels"
            val r = Random(0)
            for (i in data.indices) {
                // RGBA
                val alpha = if (r.nextFloat() > 0.5f) 255 else 0
                val rgb = r.nextInt() and 0x00FFFFFF
                data[i] = (alpha shl 24) or rgb
            }
        }

        fun getPixel(x: Int, y: Int): Int {
            // Simulate bounds check and calculation
            if (x < 0 || x >= width || y < 0 || y >= height) throw IllegalArgumentException()
            return data[y * width + x]
        }

        fun getPixels(pixels: IntArray, offset: Int, stride: Int, x: Int, y: Int, width: Int, height: Int) {
            // Simulate copy
            for (r in 0 until height) {
                System.arraycopy(data, (y + r) * this.width + x, pixels, offset + r * stride, width)
            }
        }
    }

    @Test
    fun benchmarkPixelAccess() {
        val width = 1000
        val height = 1000
        val grid = PixelGrid(width, height)

        // Warm up
        runSlow(grid)
        runFast(grid)

        val startSlow = System.nanoTime()
        val resultSlow = runSlow(grid)
        val endSlow = System.nanoTime()

        val startFast = System.nanoTime()
        val resultFast = runFast(grid)
        val endFast = System.nanoTime()

        val durationSlow = (endSlow - startSlow) / 1_000_000.0 // ms
        val durationFast = (endFast - startFast) / 1_000_000.0 // ms

        println("Slow method (getPixel): ${durationSlow}ms")
        println("Fast method (getPixels): ${durationFast}ms")
        println("Speedup: ${durationSlow / durationFast}x")

        assertEquals(resultSlow, resultFast)
    }

    data class Bounds(val minX: Float, val minY: Float, val maxX: Float, val maxY: Float)

    private fun runSlow(grid: PixelGrid): Bounds {
        var minX = grid.width.toFloat()
        var minY = grid.height.toFloat()
        var maxX = 0f
        var maxY = 0f

        // Original logic iterates x then y
        for (x in 0 until grid.width) {
            for (y in 0 until grid.height) {
                val pixel = grid.getPixel(x, y)
                if ((pixel ushr 24) > 0) { // If pixel is not transparent
                    minX = min(minX, x.toFloat())
                    minY = min(minY, y.toFloat())
                    maxX = max(maxX, x.toFloat())
                    maxY = max(maxY, y.toFloat())
                }
            }
        }
        return Bounds(minX, minY, maxX, maxY)
    }

    private fun runFast(grid: PixelGrid): Bounds {
        var minX = grid.width.toFloat()
        var minY = grid.height.toFloat()
        var maxX = 0f
        var maxY = 0f

        val width = grid.width
        val height = grid.height
        val rowPixels = IntArray(width)

        // Optimized logic iterates y then x (scanline)
        for (y in 0 until height) {
            grid.getPixels(rowPixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val pixel = rowPixels[x]
                if ((pixel ushr 24) > 0) {
                    minX = min(minX, x.toFloat())
                    minY = min(minY, y.toFloat())
                    maxX = max(maxX, x.toFloat())
                    maxY = max(maxY, y.toFloat())
                }
            }
        }
        return Bounds(minX, minY, maxX, maxY)
    }
}
