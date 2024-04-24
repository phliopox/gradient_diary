package com.example.gradientdiary.presentation.ui.component

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageBlock(
    override var seq: Long = 0,
    override var content: Uri?
): ContentBlock<Uri?>() ,Parcelable{
    @Composable
    override fun drawOnlyReadContent(modifier: Modifier) {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun addNextBlock() {
        TODO("Not yet implemented")
    }

    @Composable
    override fun drawEditableContent() {
        TODO("Not yet implemented")
    }

}