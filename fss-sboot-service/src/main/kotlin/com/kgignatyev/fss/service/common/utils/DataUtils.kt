package com.kgignatyev.fss.service.common.utils


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

}
