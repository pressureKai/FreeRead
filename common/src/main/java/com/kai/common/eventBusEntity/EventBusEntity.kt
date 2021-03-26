package com.kai.common.eventBusEntity

class EventBusEntity<T> {
    var code = -1
    var data :T ?= null
    var target :String = ""
}