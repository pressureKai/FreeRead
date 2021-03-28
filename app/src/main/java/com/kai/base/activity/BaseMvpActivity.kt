package com.kai.base.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.classic.common.MultipleStatusView
import com.kai.base.R
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.eventBusEntity.EventBusEntity
import com.kai.common.utils.LogUtils
import com.kai.common.utils.RxNetworkObserver
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseMvpActivity<V : IView, P : BasePresenter<V>> : AppCompatActivity(), IView {
    private var enableEventBus = true
    protected var mPresenter: P? = null
    private var mEventBusTarget = this::class.java.name
    private var mMultipleStatusView: MultipleStatusView? = null
    private val DEFAULT_LAYOUT_PARAMS = ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mEventBusTarget = resetEventBusTarget()
        init()
        initView()
        RxNetworkObserver.register(this)
    }

    fun checkNetworkState(){
        RxNetworkObserver.subscribe {
             when(it){
                 RxNetworkObserver.NET_STATE_WIFI -> {
                     onWifi()
                 }
                 RxNetworkObserver.NET_STATE_DISCONNECT -> {
                     disConnect()
                 }
                 RxNetworkObserver.NET_STATE_MOBILE -> {
                     onMobile()
                 }
             }
        }
    }

    open fun disConnect(){
        Toast.makeText(this,"disConnect",Toast.LENGTH_SHORT).show()
    }

    open fun onWifi(){
        Toast.makeText(this,"onWifi",Toast.LENGTH_SHORT).show()
    }

    open fun onMobile(){
        Toast.makeText(this,"onMobile",Toast.LENGTH_SHORT).show()
    }

    open fun resetEventBusTarget(): String {
        return this::class.java.name
    }

    open fun getStatusView(): MultipleStatusView? {
        var statusView: MultipleStatusView? = null
        try {
            statusView = findViewById(R.id.status)
        } catch (e: Exception) {
            LogUtils.e("BaseMvpActivity", e.toString())
        }
        return statusView
    }

    private fun init() {
        this.setContentView(setLayoutId())
        bindView()
        mMultipleStatusView = getStatusView()
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

    open fun enableEventBus(enable: Boolean) {
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
        RxNetworkObserver.unregister()
        unBindView()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    open fun <T> onMessageEvent(eventBusEntity: EventBusEntity<T>) {
        if (eventBusEntity.target == mEventBusTarget) {
            EventBus.getDefault().removeStickyEvent(eventBusEntity)
            onMessageReceiver(eventBusEntity)
        }
    }

    /**
     * des EventBus接收数据的方法通过基类EventBusEntity包装数据,
     *      在项目中进行数据交换
     * @param eventBusEntity  被传输的数据
     */
    open fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>) {

    }


    fun <T> postStickyEvent(data: T, code: Int? = 0, message: String) {
        val eventBusEntity = EventBusEntity<T>()
        eventBusEntity.data = data
        eventBusEntity.code = code!!
        if (message.isNotEmpty()) {
            eventBusEntity.target = message
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


    fun showErrorView(layoutId: Int? = R.layout.layout_error) {
        mMultipleStatusView?.let {
            runOnUiThread {
                it.showError(layoutId!!,DEFAULT_LAYOUT_PARAMS)
            }
        }

    }


    fun showEmptyView() {
        mMultipleStatusView?.showEmpty()
    }


    fun showLoadingView(layoutId: Int? = R.layout.layout_loading) {
        mMultipleStatusView?.let {
            runOnUiThread {
                it.showLoading(layoutId!!,DEFAULT_LAYOUT_PARAMS)
            }
        }
    }

    fun showNoNetView() {
        mMultipleStatusView?.showNoNetwork()
    }

    fun showContent(){
        runOnUiThread {
            mMultipleStatusView?.showContent()
        }
    }
}