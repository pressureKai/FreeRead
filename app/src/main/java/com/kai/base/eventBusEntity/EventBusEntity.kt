package com.kai.base.eventBusEntity

class EventBusEntity<T> {
    var code = -1
    var data :T ?= null
    var message :String = ""
}