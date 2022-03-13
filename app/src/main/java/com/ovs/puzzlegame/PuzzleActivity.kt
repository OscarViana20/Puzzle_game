package com.ovs.puzzlegame

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.roundToInt


class PuzzleActivity : AppCompatActivity() {

    private var pieces: ArrayList<PuzzlePiece>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        val layout: RelativeLayout = findViewById(R.id.layout)
        val imageView: ImageView = findViewById(R.id.imageView)

        val intent = intent
        val assetName = intent.getStringExtra("assetName")

        imageView.post {
            if (assetName != null) {
                setPicFromAsset(assetName, imageView);
            }
            pieces = splitImage()
            pieces!!.shuffle();
            val touchListener = TouchListener(this)
            for (piece in pieces!!) {
                piece.setOnTouchListener(touchListener)
                layout.addView(piece)
                val lParams = piece.layoutParams as RelativeLayout.LayoutParams
                lParams.leftMargin = Random().nextInt(layout.width - piece.pieceWidth)
                lParams.topMargin = layout.height - piece.pieceHeight
                piece.layoutParams = lParams
            }
        }
    }

    private fun splitImage(): ArrayList<PuzzlePiece> {
        val piecesNumber = 12
        val rows = 4
        val cols = 3
        val imageView: ImageView = findViewById(R.id.imageView)
        val localPieces: ArrayList<PuzzlePiece> = ArrayList(piecesNumber)

        // Get the bitmap of the source image
        val drawable: BitmapDrawable = imageView.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap

        val dimensions = getBitmapPositionInsideImageView(imageView)
        val scaledBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaledBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]

        val croppedImageWidth: Int = scaledBitmapWidth - 2 * abs(scaledBitmapLeft)
        val croppedImageHeight: Int = scaledBitmapHeight - 2 * abs(scaledBitmapTop)

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            scaledBitmapWidth,
            scaledBitmapHeight,
            true
        )

        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            abs(scaledBitmapLeft),
            abs(scaledBitmapTop),
            croppedImageWidth,
            croppedImageHeight
        )

        // Calculate the with and height of the pieces
        val pieceWidth = croppedImageWidth / cols
        val pieceHeight = croppedImageHeight / rows

        var yCord = 0
        for (row in 0 until rows) {

            var xCord = 0
            for (col in 0 until cols) {

                // calculate offset for each piece
                // calculate offset for each piece
                var offsetX = 0
                var offsetY = 0
                if (col > 0) {
                    offsetX = pieceWidth / 3
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3
                }

                // apply the offset to each piece
                val pieceBitmap: Bitmap = Bitmap.createBitmap(croppedBitmap, xCord - offsetX, yCord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY)
                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)
                piece.xCord = xCord - offsetX + imageView.left
                piece.yCord = yCord - offsetY + imageView.top
                piece.pieceWidth = pieceWidth + offsetX
                piece.pieceHeight = pieceHeight + offsetY

                // this bitmap will hold our final puzzle piece image
                val puzzlePiece = Bitmap.createBitmap(
                    pieceWidth + offsetX,
                    pieceHeight + offsetY,
                    Bitmap.Config.ARGB_8888
                )

                // draw path
                val bumpSize = pieceHeight / 4
                val canvas = Canvas(puzzlePiece)
                val path = Path()
                path.moveTo(offsetX.toFloat(), offsetY.toFloat())
                if (row === 0) {
                    // top side piece
                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                } else {
                    // top bump
                    path.lineTo((offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        offsetY.toFloat()
                    )
                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (offsetY - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (offsetY - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        offsetY.toFloat()
                    )
                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                }

                if (col === cols - 1) {
                    // right side piece
                    path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
                } else {
                    // right bump
                    path.lineTo(pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.cubicTo(
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )
                    path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
                }

                if (row === rows - 1) {
                    // bottom side piece
                    path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
                } else {
                    // bottom bump
                    path.lineTo((offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                    path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
                }

                if (col === 0) {
                    // left side piece
                    path.close()
                } else {
                    // left bump
                    path.lineTo(offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )
                    path.cubicTo(
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.close()
                }

                // mask the piece
                // mask the piece
                val paint = Paint()
                paint.color = -0x1000000
                paint.style = Paint.Style.FILL

                canvas.drawPath(path, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

                // draw a white border
                // draw a white border
                var border = Paint()
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 8.0f
                canvas.drawPath(path, border)

                // draw a black border
                border = Paint()
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 3.0f
                canvas.drawPath(path, border)

                // set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece)

                localPieces.add(piece)

                xCord += pieceWidth
            }
            yCord += pieceHeight
        }
        return localPieces
    }

    private fun getBitmapPositionInsideImageView(imageView: ImageView): IntArray {
        val ret = IntArray(4)

        if (imageView.drawable == null)
            return ret

        // Get image dimensions
        val f = FloatArray(9)
        imageView.imageMatrix.getValues(f)

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        val scaleX: Float = f[Matrix.MSCALE_X]
        val scaleY: Float = f[Matrix.MSCALE_Y]

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        val d: Drawable = imageView.drawable
        val origW: Int = d.intrinsicWidth
        val origH: Int = d.intrinsicHeight

        // Calculate the actual dimensions
        val actW = (origW * scaleX).roundToInt()
        val actH = (origH * scaleY).roundToInt()

        ret[2] = actW
        ret[3] = actH

        // Get image position
        // We assume that the image is centered into ImageView
        val imgViewW = imageView.width
        val imgViewH = imageView.height

        val top = (imgViewH - actH) / 2
        val left = (imgViewW - actW) / 2

        ret[0] = left
        ret[1] = top

        return ret
    }

    private fun setPicFromAsset(assetName: String, imageView: ImageView) {
        // Get the dimensions of the View
        val targetW = imageView.width
        val targetH = imageView.height
        val am = assets
        try {
            val `is`: InputStream = am.open("img/$assetName")
            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`is`, Rect(-1, -1, -1, -1), bmOptions)
            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            // Determine how much to scale down the image
            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
            `is`.reset()

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor
            bmOptions.inPurgeable = true
            val bitmap = BitmapFactory.decodeStream(`is`, Rect(-1, -1, -1, -1), bmOptions)
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun checkGameOver() {
        if (isGameOver()) {
            finish()
        }
    }

    private fun isGameOver(): Boolean {
        for (piece in pieces!!) {
            if (piece.canMove) {
                return false
            }
        }
        return true
    }
}