package com.kai.common.eventBusEntity

class BaseEntity<T> {
    var code = -1
    var data :T ?= null
    var message :String = ""
}