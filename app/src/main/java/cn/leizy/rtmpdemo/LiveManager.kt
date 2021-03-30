package cn.leizy.rtmpdemo

import android.util.TimeUtils
import java.util.concurrent.*

/**
 * @author wulei
 * @date 3/28/21
 * @description
 */
class LiveManager {
    private constructor()

    fun execute(runnable: Runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable)
    }

    companion object {
        val CPU_COUNT: Int = Runtime.getRuntime().availableProcessors()
        val CORE_POOL_SIZE = 2.coerceAtLeast((CPU_COUNT - 1).coerceAtMost(4))
        val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
        val KEEP_ALIVE_SECONDS = 30L
        val sPoolWorkQueue = LinkedBlockingQueue<Runnable>(5)
        private var THREAD_POOL_EXECUTOR: ThreadPoolExecutor

        init {
            THREAD_POOL_EXECUTOR = ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, sPoolWorkQueue, ThreadFactory {
                    val thread = Thread()
                    thread.priority = Thread.NORM_PRIORITY
                    thread.name = "rtmp_push_thread"
                    thread
                }
            )
        }

        @Volatile
        private var instance: LiveManager? = null

        fun getInstance(): LiveManager {
            if (instance == null) {
                synchronized(LiveManager::class.java) {
                    if (instance == null) {
                        instance = LiveManager()
                    }
                }
            }
            return instance!!
        }
    }
}