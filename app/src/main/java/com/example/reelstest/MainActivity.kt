package com.example.reelstest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val pickLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { videoUri ->
                    shareVideoToFacebookReels(videoUri)
                } ?: run {
                    Toast.makeText(this, "Video could not be picked", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted)
            startPickVideo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.shareBtn).setOnClickListener { pick() }
    }

    private fun startPickVideo() {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val intent = Intent(Intent.ACTION_PICK, uri)
        intent.setTypeAndNormalize("video/*")

        pickLauncher.launch(Intent.createChooser(intent, "Pick video"))
    }

    private fun pick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            startPickVideo()
        else permissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun shareVideoToFacebookReels(uri: Uri) {
        val intent = Intent("com.facebook.reels.SHARE_TO_REEL")

        val appId = getString(R.string.facebook_app_id)
        intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", appId)

        intent.setDataAndType(uri, "video/mp4")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        if (packageManager.resolveActivity(intent, 0) != null)
            startActivity(intent)
        else Toast.makeText(this, "Intent not resolved", Toast.LENGTH_SHORT).show()
    }

}