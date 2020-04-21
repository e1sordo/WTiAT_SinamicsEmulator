package com.e1sordo.sinamicsemulator

import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "driveparameters", path = "driveparameters")
interface DriveParameterRepository : CrudRepository<DriveParameter, Long> {

    fun findByParamIdAndParamIndex(paramId: Int, paramIndex: Int) : DriveParameter
}