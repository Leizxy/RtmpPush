package cn.leizy.rtmpdemo

import android.media.*
import android.util.Log
import java.lang.Exception

/**
 * @author wulei
 * @date 3/28/21
 * @description
 */
class AudioCodec(private val screenLive: ScreenLive) : Thread() {

    private var startTime: Long = 0
    private var isRecording: Boolean = false
    private lateinit var audioRecord: AudioRecord
    private var minBufferSize: Int = 0
    private lateinit var mediaCodec: MediaCodec

    fun startLive() {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
        //录音质量
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        //1s的码率 aac
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64_000)
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec.start()

            minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
            )
        } catch (e: Exception) {
            Log.e("AudioCodec", "startLive: ", e)
        }
        LiveManager.getInstance().execute(this)
    }

    override fun run() {
        isRecording = true
        val bufferInfo = MediaCodec.BufferInfo()
        //告诉另外一段，准备好接收音频
        val audioDecoderSpecificInfo = byteArrayOf(0x12, 0x08)
        var rtmpPackage =
            RTMPPackage(audioDecoderSpecificInfo, type = RTMPPackage.RTMP_PACKET_TYPE_AUDIO_HEAD)
        screenLive.addPackage(rtmpPackage)
        Log.i("AudioCodec", "run: start record audio, size: $minBufferSize")
        audioRecord.startRecording()
        //固定容器
        val buffer = ByteArray(minBufferSize)
        while (isRecording) {
            //pcm
            val len = audioRecord.read(buffer, 0, buffer.size)
            if (len <= 0) continue
            //输入到缓冲区
            var index = mediaCodec.dequeueInputBuffer(0)
            if (index >= 0) {
                val inputBuffer = mediaCodec.getInputBuffer(index)
                inputBuffer.clear()
                inputBuffer.put(buffer, 0, len)
                mediaCodec.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0)
            }
            index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            while (index >= 0 && isRecording) {
                val outputBuffer = mediaCodec.getOutputBuffer(index)
                val outData = ByteArray(bufferInfo.size)
                outputBuffer.get(outData)
                if (startTime == 0L) {
                    startTime = bufferInfo.presentationTimeUs / 1000
                }
                val tms = (bufferInfo.presentationTimeUs / 1000) - startTime
                rtmpPackage = RTMPPackage(outData, tms, RTMPPackage.RTMP_PACKET_TYPE_AUDIO_DATA)
                screenLive.addPackage(rtmpPackage)
                mediaCodec.releaseOutputBuffer(index, false)
                index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
        if (this::audioRecord.isInitialized) {
            audioRecord.stop()
            audioRecord.release()
        }
        if (this::mediaCodec.isInitialized) {
            mediaCodec.stop()
            mediaCodec.release()
        }
        startTime = 0
        isRecording = false
    }
}