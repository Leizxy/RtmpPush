package cn.leizy.rtmpdemo

import android.media.projection.MediaProjection
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author wulei
 * @date 3/28/21
 * @description
 */
class ScreenLive : Thread() {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private lateinit var mediaProjection: MediaProjection
    private lateinit var url: String
    private val queue: LinkedBlockingQueue<RTMPPackage> = LinkedBlockingQueue()

    private var isLiving: Boolean = false

    fun startLive(url: String, mediaProjection: MediaProjection) {
        this.url = url
        this.mediaProjection = mediaProjection
        start()
    }

    fun addPackage(rtmpPackage: RTMPPackage) {
        if (!isLiving) return
        queue.add(rtmpPackage)
    }

    override fun run() {
        if (!connect(url)) {
            Log.i("ScreenLive", "run: fail")
            return
        }
        val videoCodec = VideoCodec(this)
        videoCodec.startLive(mediaProjection)
        isLiving = true
        while (isLiving) {
            var rtmpPackage: RTMPPackage? = null
            try {
                rtmpPackage = queue.take()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            rtmpPackage?.apply {
                if (buffer != null && buffer.size != 0) {
                    Log.i("ScreenLive", "run: send -> ${buffer.size}")
                    sendData(buffer, buffer.size, tms,0)
                }
            }
        }

    }

    private external fun sendData(data: ByteArray, len: Int, tms: Long, type: Int): Boolean

    private external fun connect(url: String): Boolean
}