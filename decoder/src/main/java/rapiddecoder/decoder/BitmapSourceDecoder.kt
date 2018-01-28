package rapiddecoder.decoder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import rapiddecoder.LoadBitmapOptions
import rapiddecoder.source.BitmapSource

internal abstract class BitmapSourceDecoder(
        protected val source: BitmapSource) : BitmapDecoder() {
    protected var bitmapWidth = INVALID_SIZE
    protected var bitmapHeight = INVALID_SIZE
    protected var imageMimeType: String? = null

    protected var bitmapDensityScale = if (source.densityScaleSupported) Float.NaN else 1f

    override val sourceWidth: Int
        get() {
            if (bitmapWidth == INVALID_SIZE) {
                synchronized(decodeLock) {
                    decodeBounds()
                }
            }
            return bitmapWidth
        }

    override val sourceHeight: Int
        get() {
            if (bitmapHeight == INVALID_SIZE) {
                synchronized(decodeLock) {
                    decodeBounds()
                }
            }
            return bitmapHeight
        }

    override val mimeType: String?
        get() {
            if (imageMimeType == null) {
                synchronized(decodeLock) {
                    decodeBounds()
                }
            }
            return imageMimeType
        }

    override val densityScale: Float
        get() {
            if (bitmapDensityScale.isNaN()) {
                synchronized(decodeLock) {
                    decodeBounds()
                }
            }
            return bitmapDensityScale
        }

    private fun decodeBounds() {
        if (bitmapWidth != INVALID_SIZE &&
                imageMimeType != null &&
                !bitmapDensityScale.isNaN()) {
            return
        }

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        opts.inJustDecodeBounds = true
        source.decode(opts)

        imageMimeType = opts.outMimeType
        bitmapWidth = opts.outWidth
        bitmapHeight = opts.outHeight
        if (bitmapDensityScale.isNaN()) {
            bitmapDensityScale =
                    if (opts.inTargetDensity != 0 && opts.inDensity != 0) {
                        opts.inTargetDensity.toFloat() / opts.inDensity
                    } else {
                        1f
                    }
        }
    }

    private fun prepareDecode(options: LoadBitmapOptions,
                              input: BitmapDecodeInput,
                              output: BitmapDecodeOutput) {
        val opts = output.options
        opts.inPreferredConfig = options.config
        opts.inMutable = options.mutable

        output.remainScaleX = input.scaleX
        output.remainScaleY = input.scaleY

        opts.inSampleSize = 1
        while (output.remainScaleX <= 0.5f && output.remainScaleY <= 0.5f) {
            opts.inSampleSize *= 2
            output.remainScaleX *= 2f
            output.remainScaleY *= 2f
        }
    }

    protected open fun finalizeDecode(options: LoadBitmapOptions,
                                      input: BitmapDecodeInput,
                                      output: BitmapDecodeOutput,
                                      bitmap: Bitmap): Bitmap {
        val scaledBitmap = if (!input.finalScale ||
                output.remainScaleX == 1f && output.remainScaleY == 1f) {
            bitmap
        } else {
            val m = Matrix()
            m.setScale(output.remainScaleX, output.remainScaleY)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
                    m, true).also {
                if (bitmap !== it) {
                    bitmap.recycle()
                }
            }
        }

        return if (!options.mutable && scaledBitmap.isMutable) {
            val immutableBitmap = scaledBitmap.copy(scaledBitmap.config, false)
            if (immutableBitmap !== scaledBitmap) {
                scaledBitmap.recycle()
            }
            immutableBitmap
        } else {
            scaledBitmap
        }
    }

    override fun decode(options: LoadBitmapOptions,
                        input: BitmapDecodeInput,
                        output: BitmapDecodeOutput): Bitmap {
        prepareDecode(options, input, output)
        val bitmap = decodeResource(options, input, output)
        return finalizeDecode(options, input, output, bitmap)
    }

    protected abstract fun decodeResource(options: LoadBitmapOptions,
                                          input: BitmapDecodeInput,
                                          output: BitmapDecodeOutput): Bitmap

    companion object {
        internal const val INVALID_SIZE = -1
    }
}