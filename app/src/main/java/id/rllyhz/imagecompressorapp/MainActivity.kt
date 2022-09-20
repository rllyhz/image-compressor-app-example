package id.rllyhz.imagecompressorapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var ivImagePreview: ImageView

    private var tempImageUri: Uri? = null
    private var tempImageFilePath: String = ""

    private var cameraResultLauncher: ActivityResultLauncher<Uri>? = null
    private var openAlbumResultLauncher: ActivityResultLauncher<String>? = null

    private fun initTempUri(): Uri {
        //gets the temp_images dir
        val tempImagesDir = File(
            applicationContext.filesDir, //this function gets the external cache dir
            "temp_images_dir"
        )
        tempImagesDir.mkdir()

        //Creates the temp_image.jpg file
        val tempImage = File(
            tempImagesDir, //prefix the new abstract path with the temporary images dir path
            getString(R.string.temp_image_name)
        ) //gets the abstract temp_image file name

        return FileProvider.getUriForFile(
            applicationContext,
            getString(R.string.authorities),
            tempImage
        )
    }

    private fun registerTakePictureLauncher(path: Uri) {
        //Creates the ActivityResultLauncher
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && this::ivImagePreview.isInitialized) {
                    ivImagePreview.setImageURI(null)
                    ivImagePreview.setImageURI(path)

                    MyFileUtil.uriToFile(this, path, "captured_from_camera")
                        ?.let {
                            lifecycleScope.launchWhenCreated {
                                compressImage(it)
                            }
                        }
                }
            }
    }

    private fun registerOpenAlbumLauncher() {
        openAlbumResultLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { selectedPhotoUri ->
                if (selectedPhotoUri != null) {
                    if (this::ivImagePreview.isInitialized)
                        ivImagePreview.setImageURI(selectedPhotoUri)

                    lifecycleScope.launchWhenCreated {
                        compressImage(selectedPhotoUri)
                    }
                }
            }
    }

    private fun compressImage(file: File) {
        val initialSize = MyFileUtil.getFileSizeInMB(file.absolutePath)
        println("Initial Size: $initialSize")

        val result = MyImageCompressor.compressImage(file.absolutePath, 0.5)
        val beforeCompression = result[0]
        val afterCompression = result[1]

        println("Before Compression: $beforeCompression")
        println("After Compression: $afterCompression")

        val afterAll = MyFileUtil.getFileSizeInMB(file.absolutePath)
        println("After All: $afterAll")
    }

    private fun compressImage(uri: Uri) {
        MyFileUtil.uriToFile(this, uri, "selected_image_from_gallery")
            ?.let {
                val initialSize = MyFileUtil.getFileSizeInMB(it.absolutePath)
                println("Initial Size: $initialSize")

                val result = MyImageCompressor.compressImage(it.absolutePath, 0.5)
                val beforeCompression = result[0]
                val afterCompression = result[1]

                println("Before Compression: $beforeCompression")
                println("After Compression: $afterCompression")

                val afterAll = MyFileUtil.getFileSizeInMB(it.absolutePath)
                println("After All: $afterAll")
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tempImageUri = initTempUri()
        registerTakePictureLauncher(tempImageUri!!)
        registerOpenAlbumLauncher()

        ivImagePreview = findViewById(R.id.iv_image_preview)

        findViewById<Button>(R.id.btn_open_camera).setOnClickListener {
            cameraResultLauncher?.launch(tempImageUri!!)
        }

        findViewById<Button>(R.id.btn_open_album).setOnClickListener {
            openAlbumResultLauncher?.launch("images/*")
        }

        requestAllPermission()
    }

    private fun requestAllPermission() {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        return super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}