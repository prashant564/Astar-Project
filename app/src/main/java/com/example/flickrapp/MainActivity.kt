@file:Suppress("DEPRECATION")

package com.example.flickrapp

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.MediaController
import java.io.File


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 112
    private val folderName: String = "astarProject"
    private var playbackPosition = 0
    private lateinit var mediaController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setStoragePermissions()

        button_download_image.setOnClickListener(object: View.OnClickListener{
                override fun onClick(p0: View?) {

                    this@MainActivity.runOnUiThread(object: Runnable{

                        override fun run() {
                            imageView_display_image.visibility = View.VISIBLE
                            Picasso.get().load(editText_enter_url.text.toString()).into(imageView_display_image)
                            saveFileToStorage(editText_enter_url.text.toString())
                        }
                    })

                }
            })

            button_download_video.setOnClickListener(object : View.OnClickListener{

                override fun onClick(p0: View?) {

                    this@MainActivity.runOnUiThread(object : Runnable{
                        override fun run() {
                            displayVideo(editText_enter_video_url.text.toString())
                            saveFileToStorage(editText_enter_video_url.text.toString())
                        }
                    })
                }
            })
    }


    override fun onPause() {
        super.onPause()

        videoView_set_video.pause()
        playbackPosition = videoView_set_video.currentPosition
    }

    override fun onStop() {
        videoView_set_video.stopPlayback()
        super.onStop()
    }

    private fun createFolder(){

        val myFolder = "${Environment.getExternalStorageDirectory()}/${folderName}"
        val f= File(myFolder)
        if(!f.exists())
            if(!f.mkdir()){
                Toast.makeText(this, "$myFolder can't be created.", Toast.LENGTH_SHORT).show()

            }
            else
                Toast.makeText(this, "$myFolder can be created.", Toast.LENGTH_SHORT).show()
        else
            return
    }

    private fun setStoragePermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {


                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {

                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Permission to access the external storage is required for this app to download media.")
                        .setTitle("Permission required")
                    builder.setPositiveButton("OK") { dialog, id ->

                        Log.i("MainActivity", "Clicked")
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            REQUEST_CODE
                        )
                    }

                    val dialog = builder.create()
                    dialog.show()

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE
                    )
                }


            } else {
                createFolder()
            }

        } else {

            createFolder()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == REQUEST_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                return

            } else {

                Toast.makeText(
                    baseContext,
                    "Permission not granted, so image cant be stored in storage",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }


    }

   private fun saveFileToStorage(url: String){

         val request = DownloadManager.Request(Uri.parse(url)).
             setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE).
             setTitle("Download").
             setDescription("The file is downloading").
             setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalPublicDir("/astarProject", "${System.currentTimeMillis()}")


       val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
       manager.enqueue(request)
   }


   private fun displayVideo(video_url: String){

       val uri = Uri.parse(video_url)
       mediaController = MediaController(this)
       videoView_set_video.setVideoURI(uri)
       progress_bar_video.visibility = View.VISIBLE

       videoView_set_video.setOnPreparedListener {

           mediaController.setAnchorView(video_container)
           videoView_set_video.setMediaController(mediaController)
           videoView_set_video.seekTo(playbackPosition)
           videoView_set_video.start()
       }

       videoView_set_video.setOnInfoListener { player, what, extras ->

           if(what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
               progress_bar_video.visibility = View.INVISIBLE
           true
       }

   }

}
