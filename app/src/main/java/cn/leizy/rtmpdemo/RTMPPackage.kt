package cn.leizy.rtmpdemo

/**
 * @author wulei
 * @date 3/28/21
 * @description
 */
data class RTMPPackage(
    val buffer: ByteArray,
    val tms: Long = 0,
    val type: Int = RTMP_PACKET_TYPE_VIDEO
) {
    companion object {
        val RTMP_PACKET_TYPE_AUDIO_DATA = 2
        val RTMP_PACKET_TYPE_AUDIO_HEAD = 1
        val RTMP_PACKET_TYPE_VIDEO = 0
    }
}