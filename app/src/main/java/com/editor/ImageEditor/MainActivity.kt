package com.editor.ImageEditor

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.editor.ImageEditor.Adapters.TabLayoutAdapter
import com.editor.ImageEditor.Support.AppFileManager
import com.editor.ImageEditor.Support.AppPermissions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder
import java.io.File


class MainActivity : AppCompatActivity() {
    // views
    private lateinit var btnCaptureImage: ImageButton

    // Variables and constants
    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            outputImagePath?.let { detectObjectsInImage(it) }
        }
    }
    private  val imageEditorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Done! Check now", Toast.LENGTH_SHORT).show()
        }
    }
    private val STORAGE_PERMISSION_CODE = 1
    private val appFileManager: AppFileManager = AppFileManager(this)
    private var outputImagePath: File? = null

    // Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ask for permissions
        AppPermissions(this).checkPermissions()

        // Set tab UI
        setTabUi()

        // Capture image button is clicked
        btnCaptureImage = findViewById(R.id.btn_capture_image)
        btnCaptureImage.setOnClickListener{
            dispatchTakePictureIntent()
        }
    }

    // Set tab UI for captured and edited images
    private fun setTabUi(){
        val tablayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)

        val tabLayoutAdapter = TabLayoutAdapter(
            this,
            this.supportFragmentManager,
            1,
            tablayout.tabCount
        )
        viewPager.adapter = tabLayoutAdapter
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tablayout))
        tablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // Dispatch intent to take picture
    private fun dispatchTakePictureIntent() {
        val appFileManagerInstance = AppFileManager(this)

        // Output path
        outputImagePath = File(
            appFileManagerInstance.getCapturedImagesFolderPath()
                .toString() + File.separator + appFileManagerInstance.getNewFileName()
        )

        // Set intent
        val contentUriOutputPath = appFileManagerInstance.getContentUriFromPath(outputImagePath!!)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUriOutputPath)
        captureImageLauncher.launch(takePictureIntent)
    }

    // Launch photo editor with image 
    private fun launchPhotoEditor(sourceImagePath: String, outputFilePath: String){
        try {
            val intent = ImageEditorIntentBuilder(this, sourceImagePath, outputFilePath)
                .withAddText() // Add the features you need
                .withPaintFeature()
                .withFilterFeature()
                .withRotateFeature()
                .withCropFeature()
                .withBrightnessFeature()
                .withSaturationFeature()
                .withBeautyFeature()
                .withStickerFeature()
                .forcePortrait(true)
                .setSupportActionBarVisibility(false)
                .build()
            EditImageActivity.start(imageEditorLauncher, intent, this)
        } catch (e: java.lang.Exception) {
            println(e.printStackTrace())
        }
    }

    // Object detection
    private fun detectObjectsInImage(imgFile: File){
        val image = InputImage.fromFilePath(this, Uri.fromFile(imgFile))
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        val objectDetector = ObjectDetection.getClient(options)

        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects ->
                drawBoundingBoxes(imgFile, detectedObjects)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Failed to Detect")
            }
    }
    private fun drawBoundingBoxes(imgFile: File, objs: List<DetectedObject>){
        // Create a mutable bitmap
        val mutableBitmap = BitmapFactory.decodeFile(imgFile.toString()).copy(Bitmap.Config.ARGB_8888, true)

        // Set to canvas
        val canvas = Canvas(mutableBitmap)

        // Set color array
        val colorArr: IntArray = intArrayOf(Color.RED, Color.BLUE, Color.CYAN,Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.YELLOW, Color.DKGRAY)

        if(objs.size == 0){
            Toast.makeText(this, "Sorry!... Can not detect any objects", Toast.LENGTH_LONG).show()
        }else{
            objs.forEachIndexed{ index, obj ->
                // Define paint class object
                val paint = Paint()
                paint.alpha = 0xA0
                paint.color = colorArr[index]
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 20F

                // Draw the rectangle
                val box = obj.boundingBox
                val r = Rect(box.left, box.top, box.right, box.bottom)
                canvas.drawRect(r, paint)

                // Draw the text
                var txt = ""
                obj.labels.forEachIndexed { index2, label ->
                    println(label.text)
                    txt += label.text + " " + label.confidence.toString().split(".")[0]
                }
                if (txt.equals("")){
                    txt = "Can not detect"
                }
                    paint.strokeWidth = 10F
                    paint.textSize=100F
                    canvas.drawText(txt, box.left.toFloat(), box.top.toFloat()-40F, paint)
                }
        }

        // Save the edited image
        AppFileManager(this).saveBitMapFile(mutableBitmap, outputImagePath.toString())

        // Generate unique file name
        val fileName = appFileManager.getNewFileName()

        // Set source and destination
        val source = outputImagePath.toString()
        val destination = appFileManager.getEditedImagesFolderPath().toString() + File.separator.toString() + fileName

        // Launch the Image editor with detected objects image
        launchPhotoEditor(source, destination)
    }


    // After app permissions are granted create folders to save data
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                appFileManager.createFolders()
                Toast.makeText(this, "Thank you", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You denied permission", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

