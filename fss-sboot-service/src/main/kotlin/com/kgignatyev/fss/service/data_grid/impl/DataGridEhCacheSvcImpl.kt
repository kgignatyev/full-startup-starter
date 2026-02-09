package com.kgignatyev.fss.service.data_grid.impl

import com.kgignatyev.fss.service.data_grid.DataGridSvc
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service


@Service
class DataGridEhCacheSvcImpl( val cacheManager: CacheManager): DataGridSvc {


    override fun getEnforcersCache(): Cache {
        return cacheManager.getCache("enforcers-global")!!
    }
}
