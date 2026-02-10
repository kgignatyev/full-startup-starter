package com.kgignatyev.fss.service.data_grid

import org.springframework.cache.Cache


interface DataGridSvc {

    fun getEnforcersCache(): Cache
}
