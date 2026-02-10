package com.kgignatyev.fss.service.common.data


interface Updater<S, T> {
    fun applyUpdate(source: S, target: T)
    fun <T> applyNotNull(v: T?, applier: (v: T) -> Unit) {
        if (v != null) applier(v)
    }
}

interface UpdaterService {
    fun <S, T> getUpdater(sourceClass: Class<S>, targetClass: Class<T>): Updater<S, T>?

    fun <S : Any, T : Any> applyUpdate(source: S, target: T):T {
        @Suppress("UNCHECKED_CAST")
        val updater = getUpdater(source::class.java as Class<S>, target::class.java as Class<T>)
            ?: throw IllegalArgumentException(
                "No Updater registered for ${source::class.java.name} -> ${target::class.java.name}"
            )
        updater.applyUpdate(source, target)
        return target
    }


}
