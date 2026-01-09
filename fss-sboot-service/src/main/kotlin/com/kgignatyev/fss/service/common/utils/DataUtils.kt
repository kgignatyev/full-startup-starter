package com.kgignatyev.fss.service.common.utils

import org.springframework.core.convert.ConversionService


object DataUtils {

    inline fun <T,reified R> assignIfNotNull(
        obj: T,
        v: R?,
        set: T.(R) -> Unit
    ) {
        if (v != null) {
            obj.set(v)
        }
    }

    inline fun <T,reified R> assignConvertedIfNotNullTyped(
        obj: T,
        v: Any?,
        conversionService: ConversionService,
        set: T.(R) -> Unit
    ) {
        if (v != null) {
            val converted:R = conversionService.convert(v, R::class.java)!!
            obj.set(converted)
        }
    }
}
