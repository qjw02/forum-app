package com.qjw.forum

data class BuyFieldResponse(
    val code:Int,
    val message:String?,
    val data:BuyFieldData?
)

data class BuyFieldData(
    val contact:String?
)
