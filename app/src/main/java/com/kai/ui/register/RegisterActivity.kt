package com.kai.ui.register

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.extension.formatPhone
import com.kai.common.extension.measureView
import com.kai.common.keyboard.KeyboardHeightObserver
import com.kai.common.keyboard.KeyboardHeightProvider
import com.kai.common.listener.CustomTextWatcher
import com.kai.common.utils.StringUtils
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.merge_toolbar.*

/**
 *
 * @ProjectName:    app-page
 * @Description:    注册页面
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/7 15:45
 */
@Route(path = "/app/register")
class RegisterActivity : BaseMvpActivity<RegisterContract.View, RegisterPresenter>()
    ,KeyboardHeightObserver ,RegisterContract.View{

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider
    private var registerOriginBottom = 0
    override fun initView() {
        keyboardHeightProvider = KeyboardHeightProvider(this)
        keyboardHeightProvider.setKeyboardHeightObserver(this)
        initImmersionBar(view = toolbar,fitSystem = false)
        toolbar_title.text = resources.getString(R.string.register)
        back.setOnClickListener {
            finish()
        }

        register.post {
            keyboardHeightProvider.start()
        }
        register.setOnClickListener {
            verifyAll()
            mPresenter?.register("15816522608","123456","who am i","me")
        }

        account.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                account.formatPhone(start,count>0)
            }
        })

    }

    override fun setLayoutId(): Int {
        return R.layout.activity_register
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        register.post {
            if(registerOriginBottom == 0){
                registerOriginBottom = content.bottom
            }
            val rootHeight = root.height

            var changeHeight = if (height == 0) {
                0
            } else {
                if (rootHeight - registerOriginBottom < height) {
                    height - (rootHeight - registerOriginBottom)
                } else {
                    height
                }
            }
            if(changeHeight != 0){
               if(changeHeight < toolbar.measureView()[1]){
                   changeHeight = toolbar.measureView()[1]
               }
            }
            beginAnimation(root,-changeHeight.toFloat())
        }

    }


    override fun onPause() {
        super.onPause()
        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }


    override fun onResume() {
        super.onResume()
        if(::keyboardHeightProvider.isInitialized){
            keyboardHeightProvider.setKeyboardHeightObserver(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }

    override fun onRegister(code: Int) {

    }

    override fun createPresenter(): RegisterPresenter? {
        return RegisterPresenter()
    }



    private fun verifyAll():Boolean{
        var verify = false
        verify = StringUtils.verifyPhone(account.text.toString())

        if(!verify){
            account_layout.errorContentDescription = "手机格式错误"
        }
        return verify
    }
}