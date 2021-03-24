package com.kai.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.eventBusEntity.EventBusEntity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *des  mvp
 */
abstract class  BaseMvpActivity<V: IView,P : BasePresenter<V>> : AppCompatActivity(), IView {
    private var enableEventBus = true
    protected var mPresenter: P? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initView()
    }


    private fun init() {
        this.setContentView(setLayoutId())
        bindView()
        if (enableEventBus) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        }
    }


    abstract fun initView()

    /**
     * 用户第一次触发事件流中的MotionEvent.ACTION_DOWN时，Activity源码中调用的方法
     */
    override fun onUserInteraction() {
        super.onUserInteraction()
    }

    abstract fun setLayoutId(): Int
    open fun createPresenter(): P? {
        return null
    }

    open fun enableEventBus(enable: Boolean){
        enableEventBus = enable
    }


    override fun onResume() {
        super.onResume()
        if (enableEventBus) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (enableEventBus) {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        }
        unBindView()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    open fun <T> onMessageEvent(eventBusEntity: EventBusEntity<T>) {
        if (eventBusEntity.message == this::class.java.name) {
            EventBus.getDefault().removeStickyEvent(eventBusEntity)
            onMessageReceiver(eventBusEntity)
        }
    }

    open fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>) {

    }


    fun <T> postStickyEvent(data: T, code: Int? = 0, message: String) {
        val eventBusEntity = EventBusEntity<T>()
        eventBusEntity.data = data
        eventBusEntity.code = code!!
        if (message.isNotEmpty()) {
            eventBusEntity.message = message
        }
        EventBus.getDefault().postSticky(eventBusEntity)
    }


    override fun onPause() {
        super.onPause()
        if (enableEventBus) {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        }
    }

    override fun bindView() {
        mPresenter = createPresenter()
        mPresenter?.register(this as V)
    }


    override fun unBindView() {
        mPresenter?.unRegister()
    }


}