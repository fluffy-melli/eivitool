package eivitool.utils

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.Logger

fun KeyListener(key: List<Pair<Int, () -> Unit>>) {
    try {
        Logger.getLogger(GlobalScreen::class.java.name).level = Level.OFF
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
            override fun nativeKeyPressed(event: NativeKeyEvent) {
                key.find { it.first == event.keyCode }?.second?.invoke()
            }
        })
    } catch (e: NativeHookException) {
        e.printStackTrace()
    }
}