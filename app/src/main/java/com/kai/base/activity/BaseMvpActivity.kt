package com.kai.base.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import com.classic.common.MultipleStatusView
import com.gyf.immersionbar.ImmersionBar
import com.kai.base.R
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.eventBusEntity.EventBusEntity
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import skin.support.SkinCompatManager

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


    fun <T> postStickyEvent(data: T, code: Int? = 0, message: String = "") {
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


    fun initImmersionBar(
        view: View? = null,
        fitSystem: Boolean = false,
        color: Int? = 0
    ) {
        var immersionColor = R.color.app_background
        color?.let {
            if (it != 0) {
                immersionColor = it
            }
        }

        var fontIsDark = true
        try {
            val curSkinName = SkinCompatManager.getInstance().curSkinName
            if(curSkinName.isEmpty()){
                fontIsDark = true
            } else if(curSkinName == "night"){
                fontIsDark = false
            }
        }catch (e :java.lang.Exception){
            LogUtils.e("BaseMvpActivity","getCurrentSkinName error is $e")
        }

        if (view != null) {
            ImmersionBar
                .with(this)
                .statusBarDarkFont(fontIsDark, 0.7f)
                .fitsSystemWindows(fitSystem)
                .navigationBarColor(immersionColor)
                .titleBar(view)
                .autoNavigationBarDarkModeEnable(fontIsDark, 0.8f)
                .init()
        } else {
            ImmersionBar
                .with(this)
                .statusBarDarkFont(fontIsDark, 0.7f)
                .fitsSystemWindows(fitSystem)
                .navigationBarColor(immersionColor)
                .autoNavigationBarDarkModeEnable(fontIsDark, 0.8f)
                .init()
        }
    }
}