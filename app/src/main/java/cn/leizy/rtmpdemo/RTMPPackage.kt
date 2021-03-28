package cn.leizy.rtmpdemo

/**
 * @author wulei
 * @date 3/28/21
 * @description
 */
data class RTMPPackage(
    val buffer: ByteArray,
    val tms: Long
)