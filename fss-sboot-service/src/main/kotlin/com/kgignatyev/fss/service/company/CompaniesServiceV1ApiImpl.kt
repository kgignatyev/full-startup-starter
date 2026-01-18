package com.kgignatyev.fss.service.company

import com.kgignatyev.fss_svc.api.fsssvc.CompaniesServiceV1Api
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1Company
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/api"])
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"], methods = [RequestMethod.PATCH, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.PUT])
@RestController
class CompaniesServiceV1ApiImpl(val companiesSvc: CompaniesService,
                                @Qualifier("mvcConversionService") val conv:ConversionService): CompaniesServiceV1Api {
    

    override fun getAllCompanies( accountId:String): ResponseEntity<List<V1Company>> {
       return ok( companiesSvc.findAll().map { conv.convert(it, V1Company::class.java)!! })
    }

    override fun createCompany( accountId: String, v1Company: V1Company?): ResponseEntity<V1Company> {
        val company = companiesSvc.save(conv.convert(v1Company, Company::class.java)!!)
        return ok( conv.convert(company, V1Company::class.java)!! )
    }

    override fun getCompanyById(id: String?): ResponseEntity<V1Company> {
        val company = companiesSvc.findById(id!!).get()
        return ok( conv.convert(company, V1Company::class.java)!! )
    }
}
