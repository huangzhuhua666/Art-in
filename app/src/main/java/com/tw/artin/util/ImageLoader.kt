package com.tw.artin.util

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.lxj.xpopup.interfaces.XPopupImageLoader
import java.io.File


class ImageLoader : XPopupImageLoader {

    override fun loadImage(position: Int, uri: Any, imageView: ImageView) {
        Glide.with(imageView).load(uri)
            .into(imageView)
    }

    override fun getImageFile(context: Context, uri: Any): File {
        return Glide.with(context).downloadOnly().load(uri).submit().get()
    }
}