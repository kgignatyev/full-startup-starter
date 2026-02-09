package com.kgignatyev.fss.service.automation

import io.temporal.activity.ActivityInterface

@ActivityInterface
interface JobActivities {

    fun recordCompanyResponse( jobId:String, responseCode:String, responseMessage:String )
}
