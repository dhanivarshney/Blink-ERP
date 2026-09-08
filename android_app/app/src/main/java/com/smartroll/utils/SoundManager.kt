package com.smartroll.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import android.view.View

object SoundManager {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

    fun init(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun playClick(view: View) {
        // Haptic is more reliable than custom audio for clicks in Android
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}
