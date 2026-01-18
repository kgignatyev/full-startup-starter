package com.kgignatyev.fss.service.automation


interface JobActivities {

    fun recordCompanyResponse( jobId:String, responseCode:String, responseMessage:String )
}
