package com.aquapetszone.kmp.domain.repository

import com.aquapetszone.kmp.data.database.MongoFactory

open class ServerBaseRepository {

    protected val db = MongoFactory.database

}