package cn.leizy.rtmpdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.leizy.rtmpdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var mediaProjectionManager: MediaProjectionManager
    lateinit var screenLive: ScreenLive
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main) as ActivityMainBinding
        binding.code.setText("?streamname=live_8379703_1896767&key=167ddc29b2d089fe2e43f58fe3b8c36d&schedule=rtmp&pflag=1")
        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ), 1
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            screenLive = ScreenLive()
            screenLive.startLive(
                binding.url.text.toString().trim() + binding.code.text.toString().trim(),
                mediaProjection
            )
        }
    }

    fun startLive(view: View) {
        if (binding.url.text.toString().trim().isEmpty()
            /*|| binding.code.text.toString().trim().isEmpty()*/
        ) return
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, 100)
    }

    fun stopLive(view: View) {}
/*
    */
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     *//*
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }*/
}