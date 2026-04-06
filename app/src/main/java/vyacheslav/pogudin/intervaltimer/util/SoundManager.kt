package vyacheslav.pogudin.intervaltimer.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import androidx.annotation.RawRes
import vyacheslav.pogudin.intervaltimer.R

/**
 * Управление короткими звуковыми сигналами для тренировок.
 * Работает поверх фоновой музыки и воспроизводится на активном аудиоустройстве.
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private val startSoundId: Int
    private val intervalSoundId: Int
    private val endSoundId: Int
    private val mainHandler = Handler(Looper.getMainLooper())
    private val loadedSoundIds = mutableSetOf<Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            // FLAG_AUDIBILITY_ENFORCED помогает сделать сигнал слышимым поверх фоновой музыки
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
            }
        }

        startSoundId = loadSound(context, R.raw.start)
        intervalSoundId = loadSound(context, R.raw.interval)
        endSoundId = loadSound(context, R.raw.end)
    }

    private fun loadSound(context: Context, @RawRes resId: Int): Int {
        return soundPool.load(context, resId, 1)
    }

    fun playStart() {
        playSound(startSoundId)
    }

    fun playInterval() {
        playSound(intervalSoundId)
    }

    fun playEnd() {
        playSound(endSoundId)
    }

    private fun playSound(soundId: Int, attempt: Int = 0) {
        if (loadedSoundIds.contains(soundId)) {
            soundPool.play(soundId, 0.1f, 0.1f, 1, 0, 1f)
            return
        }

        if (attempt >= 10) return
        mainHandler.postDelayed({ playSound(soundId, attempt + 1) }, 50)
    }

    fun release() {
        mainHandler.removeCallbacksAndMessages(null)
        soundPool.release()
    }
}