package com.eggmonsters.app.util

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Sonidos simples generados por ToneGenerator (no necesita archivos de audio).
 */
object SoundHelper {

    private var toneGen: ToneGenerator? = null

    private fun ensure() {
        if (toneGen == null) {
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (_: Exception) {
                // Algunos dispositivos fallan si el volumen es 0
            }
        }
    }

    fun playClick() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
    }

    fun playSuccess() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    fun playHatch() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
    }

    fun playEvolve() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 300)
    }

    fun playFeed() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_DTMF_1, 80)
    }

    fun playPlay() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_DTMF_5, 100)
    }

    fun playClean() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_DTMF_7, 90)
    }

    fun playSleep() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
    }

    fun playPet() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
    }

    fun playMiniGameHit() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_DTMF_A, 50)
    }

    fun playMiniGameMiss() {
        ensure()
        toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 80)
    }

    fun release() {
        toneGen?.release()
        toneGen = null
    }
}
