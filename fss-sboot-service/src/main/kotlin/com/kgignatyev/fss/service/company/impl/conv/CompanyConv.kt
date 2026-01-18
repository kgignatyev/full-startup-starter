package com.kgignatyev.fss.service.company.impl.conv

import com.kgignatyev.fss.service.company.Company
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1Company
import org.springframework.beans.BeanUtils
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component


@Component
class CompanyToApi: Converter<Company, V1Company> {
    override fun convert(from: Company): V1Company {
        val r = V1Company()
        BeanUtils.copyProperties(from, r)
        return r
    }
}

@Component
class ApiToCompany: Converter<V1Company, Company> {
    override fun convert(from: V1Company): Company {
        val r = Company()
        BeanUtils.copyProperties(from, r)
        return r
    }
}
