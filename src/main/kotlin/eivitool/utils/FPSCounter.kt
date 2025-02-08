package eivitool.utils

class FPSCounter {
    var fps = 0
    private var lastTime = System.nanoTime()
    private var frameCount = 0

    fun update() {
        frameCount++
        val currentTime = System.nanoTime()
        val elapsedTime = currentTime - lastTime

        if (elapsedTime >= 1_000_000_000) {
            fps = frameCount
            frameCount = 0
            lastTime = currentTime
        }
    }
}