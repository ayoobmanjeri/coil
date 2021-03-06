package coil.memory

import android.graphics.Bitmap
import coil.annotation.ExperimentalCoilApi
import coil.memory.MemoryCache.Key

@OptIn(ExperimentalCoilApi::class)
internal class RealMemoryCache(
    private val strongMemoryCache: StrongMemoryCache,
    private val weakMemoryCache: WeakMemoryCache,
    private val bitmapReferenceCounter: BitmapReferenceCounter
) : MemoryCache {

    override val size get() = strongMemoryCache.size

    override val maxSize get() = strongMemoryCache.maxSize

    override fun get(key: Key): Bitmap? {
        val value = strongMemoryCache.get(key) ?: weakMemoryCache.get(key)
        return value?.bitmap?.also { bitmapReferenceCounter.invalidate(it) }
    }

    override fun remove(key: Key): Boolean {
        // Do not short circuit.
        val removedStrong = strongMemoryCache.remove(key)
        val removedWeak = weakMemoryCache.remove(key)
        return removedStrong || removedWeak
    }

    override fun clear() {
        strongMemoryCache.clearMemory()
        weakMemoryCache.clearMemory()
    }

    interface Value {
        val bitmap: Bitmap
        val isSampled: Boolean
    }
}
