package com.kai.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kai.base.eventBusEntity.EventBusEntity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseActivity<P :BasePresenter<in AppCompatActivity>> : AppCompatActivity(),IView {
    private var startEventBus = true
    private var mPresenter :P ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initView()
    }


    private fun init(){
        this.setContentView(setLayoutId())
        startEventBus = startEventBus()
        if(startEventBus){
            if(!EventBus.getDefault().isRegistered(this)){
                EventBus.getDefault().register(this)
            }
        }
    }
    open fun initView(){

    }


    abstract fun setLayoutId() :Int
    abstract fun createPresenter() :P?
    open fun startEventBus():Boolean{
        return true
    }


    override fun onResume() {
        super.onResume()
        if(startEventBus){
            if(!EventBus.getDefault().isRegistered(this)){
                EventBus.getDefault().register(this)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if(startEventBus){
            if(EventBus.getDefault().isRegistered(this)){
                EventBus.getDefault().unregister(this)
            }
        }
    }


    /**
     *
     * */
    @Subscribe(threadMode = ThreadMode.ASYNC,sticky = true)
    open fun <T> onMessageEvent(eventBusEntity :EventBusEntity<T>){
        if(eventBusEntity.message == this::class.java.name){
            EventBus.getDefault().removeStickyEvent(eventBusEntity)
            onMessageReceiver(eventBusEntity)
        }
    }

    open fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>){

    }


    fun <T> postStickyEvent(data :T,code :Int ?= 0,message :String ){
        val eventBusEntity = EventBusEntity<T>()
        eventBusEntity.data = data
        eventBusEntity.code = code!!
        if(message.isNotEmpty()){
            eventBusEntity.message = message
        }
        EventBus.getDefault().postSticky(eventBusEntity)
    }


    override fun onPause() {
        super.onPause()
        if(startEventBus){
            if(EventBus.getDefault().isRegistered(this)){
                EventBus.getDefault().unregister(this)
            }
        }
    }

    override fun bindView() {
        mPresenter = createPresenter()
        mPresenter?.register(this)
    }


    override fun unBindView() {
        mPresenter?.unRegister()
    }
}