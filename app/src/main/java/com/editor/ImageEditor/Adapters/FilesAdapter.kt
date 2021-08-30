package com.editor.ImageEditor.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.editor.ImageEditor.R
import com.editor.ImageEditor.Support.AppFileManager
import java.io.File


class FilesAdapter(private var mContext: Context, private var filesData: Array<File>): RecyclerView.Adapter<FilesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImgFilePreview: ImageView? = null
        var ivShare: ImageView? = null
        var tvImgFileName: TextView? = null

        init {
            ivImgFilePreview = itemView.findViewById(R.id.iv_img_file_preview)
            ivShare = itemView.findViewById(R.id.iv_share)
            tvImgFileName = itemView.findViewById(R.id.tv_img_file_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(mContext).inflate(R.layout.img_file_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file:File = filesData[position]
        holder.ivImgFilePreview?.setImageURI(Uri.parse(file.absolutePath.toString()))
        holder.tvImgFileName?.text = file.name.split(".")[0]
        holder.ivShare?.setOnClickListener(View.OnClickListener {
            AppFileManager(mContext).shareFile(file)
        })

        // Open image on clicked
        holder.itemView.setOnClickListener(View.OnClickListener {
            val filePath = Uri.parse(AppFileManager(mContext).getContentUriFromPath(file).toString())
            val openImageIntent =Intent(Intent.ACTION_VIEW, filePath)
            openImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            mContext.startActivity(openImageIntent)
        })
    }

    override fun getItemCount(): Int { return filesData.size }
}