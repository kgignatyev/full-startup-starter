package com.kgignatyev.fss.service.common.events

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.NamedInterface
import org.springframework.modulith.PackageInfo


@ApplicationModule(
    type = ApplicationModule.Type.OPEN
)
@PackageInfo
@NamedInterface
class ModuleMetadata {}
