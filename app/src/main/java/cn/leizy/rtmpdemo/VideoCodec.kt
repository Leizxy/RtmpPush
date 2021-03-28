package cn.leizy.rtmpdemo

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Bundle
import java.io.IOException

/**
 * @author wulei
 * @date 3/28/21
 * @description
 */
class VideoCodec(private val screenLive: ScreenLive) : Thread() {

    //虚拟画布
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var mediaCodec: MediaCodec

    //录屏
    private lateinit var mediaProjection: MediaProjection

    private var timeStamp: Long = 0
    private var startTime: Long = 0
    private var isLiving: Boolean = false

    fun startLive(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection
        val format: MediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, 400_000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc")
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec.createInputSurface()
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "screen-codec",
                720,
                1280,
                1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface,
                null,
                null
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        LiveManager.getInstance().execute(this)
    }

    override fun run() {
        isLiving = true
        mediaCodec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (isLiving) {
            //隔2s触发 I 帧
            if (System.currentTimeMillis() - timeStamp >= 2000) {
                val params = Bundle()
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                mediaCodec.setParameters(params)
                timeStamp = System.currentTimeMillis()
            }
            val index = mediaCodec.dequeueOutputBuffer(bufferInfo, 100_000)
            if (index >= 0) {
                if (startTime == 0L) {
                    startTime = bufferInfo.presentationTimeUs / 1000
                }

                val buffer = mediaCodec.getOutputBuffer(index)
                val mediaFormat = mediaCodec.getOutputFormat(index)
                val outData = ByteArray(bufferInfo.size)
                buffer?.get(outData)

                val rtmpPackage =
                    RTMPPackage(outData, (bufferInfo.presentationTimeUs / 1000) - startTime)
                screenLive.addPackage(rtmpPackage)
                mediaCodec.releaseOutputBuffer(index, false)
            }
        }
        isLiving = false
        if (this::mediaCodec.isInitialized) {
            mediaCodec.stop()
            mediaCodec.release()
        }
        if (this::virtualDisplay.isInitialized) {
            virtualDisplay.release()
        }
        if (this::mediaProjection.isInitialized) {
            mediaProjection.stop()
        }
        startTime = 0
    }
}