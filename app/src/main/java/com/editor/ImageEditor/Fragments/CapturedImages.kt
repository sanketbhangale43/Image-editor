package com.editor.ImageEditor.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.ImageEditor.Adapters.FilesAdapter
import com.editor.ImageEditor.R
import com.editor.ImageEditor.Support.AppFileManager
import java.io.File

class CapturedImages : Fragment() {
    lateinit var filesData: Array<File>
    lateinit var recyclerView: RecyclerView
    lateinit var tvRvPlaceholder: TextView


    override fun onStart() {
        super.onStart()
        setData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_captured_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_captured_images)
        tvRvPlaceholder = view.findViewById(R.id.tv_placeholder_txt)

        setData()
    }

    fun setData(){
        filesData = context?.let { AppFileManager(it).getAllCapturedImages() }!!

        if (filesData.isNotEmpty()){
            tvRvPlaceholder.visibility = View.GONE
            val filesAdapter: FilesAdapter? = filesData.let {
                context?.let { it1 ->
                    FilesAdapter(
                        it1,
                        it
                    )
                }
            }
            recyclerView.setHasFixedSize(true);
            recyclerView.adapter = filesAdapter

            val filesLayoutManager: RecyclerView.LayoutManager?
            filesLayoutManager = GridLayoutManager(context, 2)
            recyclerView.layoutManager = filesLayoutManager
        }else{
            // Show placeholder if images are not found
            tvRvPlaceholder.visibility = View.VISIBLE
        }
    }
}