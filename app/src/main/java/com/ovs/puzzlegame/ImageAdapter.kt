package com.ovs.puzzlegame

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import java.io.IOException
import java.io.InputStream


class ImageAdapter(c: Context) : BaseAdapter() {

    private var mContext: Context? = c
    private var am: AssetManager? = c.assets
    private var files: Array<String>? = am!!.list("img")!!

    override fun getCount(): Int {
        return files!!.size;
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertViewAux: View? = null
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(mContext)
            convertViewAux = layoutInflater.inflate(R.layout.grid_element, null)
        } else {
            convertViewAux = convertView
        }

        val imageView: ImageView = convertViewAux!!.findViewById(R.id.gridImageview)
        imageView.setImageBitmap(null)
        // run image related code after the view was laid out
        imageView.post(Runnable {
            object : AsyncTask<Void?, Void?, Void?>() {
                private var bitmap: Bitmap? = null

                override fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    imageView.setImageBitmap(bitmap)
                }

                override fun doInBackground(vararg p0: Void?): Void? {
                    bitmap = getPicFromAsset(imageView, files!!.get(position))
                    return null
                }
            }.execute()
        })

        return convertViewAux
    }

    private fun getPicFromAsset(imageView: ImageView, assetName: String): Bitmap? {
        // Get the dimensions of the View
        val targetW = imageView.width
        val targetH = imageView.height
        return if (targetW == 0 || targetH == 0) {
            // view has no dimensions set
            null
        } else try {
            val `is`: InputStream = am!!.open("img/$assetName")
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
            BitmapFactory.decodeStream(`is`, Rect(-1, -1, -1, -1), bmOptions)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}