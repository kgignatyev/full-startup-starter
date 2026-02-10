package com.kgignatyev.fss.service.common.data.impl

import com.kgignatyev.fss.service.common.data.Updater
import com.kgignatyev.fss.service.common.data.UpdaterService
import org.springframework.core.GenericTypeResolver
import org.springframework.stereotype.Service

@Service
class UpdaterSvcImpl(updaters: List<Updater<*, *>>) : UpdaterService {

    private val registry = mutableMapOf<Class<*>, MutableMap<Class<*>, Updater<*, *>>>()

    init {
        for (updater in updaters) {
            val typeArgs = GenericTypeResolver.resolveTypeArguments(updater::class.java, Updater::class.java)
            if (typeArgs != null && typeArgs.size == 2) {
                val sourceClass = typeArgs[0]
                val targetClass = typeArgs[1]
                (registry.getOrPut(targetClass) { mutableMapOf() })[sourceClass] = updater
            } else {
                throw IllegalStateException(
                    "Could not resolve type arguments for Updater: ${updater::class.java.name}"
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> getUpdater(sourceClass: Class<S>, targetClass: Class<T>): Updater<S, T>? {
        return registry[targetClass]?.get(sourceClass) as? Updater<S, T>
    }

}
