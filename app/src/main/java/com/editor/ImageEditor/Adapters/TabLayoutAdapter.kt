package com.editor.ImageEditor.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.editor.ImageEditor.Fragments.CapturedImages
import com.editor.ImageEditor.Fragments.EditedImages

class TabLayoutAdapter(private val context: Context, fm: FragmentManager?, behavior: Int, var totalTabs: Int) : FragmentPagerAdapter(fm!!, behavior) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return CapturedImages()
            1 -> return EditedImages()
        }
        return CapturedImages()
    }
    override fun getCount(): Int {
        return totalTabs
    }
}
