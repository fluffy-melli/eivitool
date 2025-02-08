package eivitool.utils

import java.nio.ShortBuffer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import org.bytedeco.javacv.Frame

class RecorderClip {
    var FPS: Int = 30
    var frameQueue: BlockingQueue<Frame>? = null
    var audioQueue: BlockingQueue<ShortBuffer>? = null

    fun init(fps: Int) {
        FPS = fps
        frameQueue = LinkedBlockingQueue(fps * 60)
        audioQueue = LinkedBlockingQueue(fps * 1024)
    }

    fun cacheFrame(frame: Frame) {
        if (frameQueue?.size!! >= FPS * 60) {
            frameQueue?.poll()
        }
        frameQueue?.offer(frame)
    }

    fun cacheAudio(audioData: ShortBuffer) {
        if (audioQueue?.size!! >= FPS * 1024) {
            audioQueue?.poll()
        }
        audioQueue?.offer(audioData)
    }
}