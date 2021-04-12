package com.kai.ui.login

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.core.widget.addTextChangedListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.customToast
import com.kai.common.extension.formatPhone
import com.kai.common.extension.getScreenWidth
import com.kai.common.keyboard.KeyboardHeightObserver
import com.kai.common.keyboard.KeyboardHeightProvider
import com.kai.common.listener.CustomTextWatcher
import com.kai.common.utils.StringUtils
import com.kai.entity.User
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.account
import kotlinx.android.synthetic.main.activity_login.account_layout
import kotlinx.android.synthetic.main.activity_login.register
import kotlinx.android.synthetic.main.activity_login.root
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

/**
 *
 * @ProjectName:    app-page
 * @Description:    登陆页面
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/7 15:45
 */
@Route(path = "/app/login")
class LoginActivity : BaseMvpActivity<LoginContract.View, LoginPresenter>(),
    KeyboardHeightObserver,LoginContract.View {

    companion object{
        const val REGISTER_CALLBACK = 0
        const val LOGIN_FAIL_NO_ACCOUNT = 1
        const val LOGIN_FAIL_ERROR_PASSWORD = 2
        const val LOGIN_SUCCESS= 3
    }

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider
    private var loginCardOriginBottom = 0
    override fun initView() {
        keyboardHeightProvider = KeyboardHeightProvider(this)
        keyboardHeightProvider.setKeyboardHeightObserver(this)
        initImmersionBar(fitSystem = false)
        login_lottie.layoutParams.height = getScreenWidth()
        login_content.post {
            keyboardHeightProvider.start()
        }

        forget_password.setOnClickListener {
            ARouter.getInstance().build("/app/forgetPassword").navigation()
        }
        register.setOnClickListener {
            ARouter.getInstance().build("/app/register").navigation()
        }


        account.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                account.formatPhone(start,count>0)
                if(account_layout.error != null
                        || account.text.toString().replace(" ","").length >= 11){
                    if(StringUtils.verifyPhone(account.text.toString())){
                        val userByAccount = mPresenter?.getUserByAccount(StringUtils.trim(account.text.toString()))
                        userByAccount?.let { observable ->
                            observable.subscribe {
                                if(it.isEmpty()){
                                    account_layout.error = resources.getString(R.string.login_no_account)
                                } else {
                                    account_layout.error = null
                                }
                            }
                        }?: kotlin.run {
                            account_layout.error = null
                        }

                    } else {
                        account_layout.error = resources.getString(R.string.phone_format_error)
                    }
                }
            }
        })
        password.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(password_layout.error != null
                        || password.text.toString().replace(" ","").length >= 6){
                    if(password.text.toString().length >= 6){
                        password_layout.error = null
                    } else {
                        password_layout.error = resources.getString(R.string.password_length_no_enough)
                    }
                }
            }

        })

        login.setOnClickListener {
            if(verifyAll()){
                mPresenter?.login(account.text.toString(),password.text.toString())
            }
        }
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }



    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        login_card.post {
            if(loginCardOriginBottom == 0){
                loginCardOriginBottom = login_card.bottom
            }
            val rootHeight = root.height

            val changeHeight = if (height == 0) {
                0
            } else {
                if (rootHeight - loginCardOriginBottom < height) {
                    height - (rootHeight - loginCardOriginBottom)
                } else {
                    height
                }
            }
            beginAnimation(login_card,-changeHeight.toFloat())

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

    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        if(baseEntity.code == REGISTER_CALLBACK){
            runOnUiThread {
                try {
                    val user = baseEntity.data as User
                    account.setText(user.account)
                }catch (e: Exception){

                }
            }
            EventBus.getDefault().removeStickyEvent(baseEntity)
        }
    }

    override fun onLogin(entity:BaseEntity<User>) {
        when(entity.code){
            LOGIN_FAIL_ERROR_PASSWORD -> {

            }
            LOGIN_SUCCESS -> {
                customToast(resources.getString(R.string.login_success))
                finish()
            }
            LOGIN_FAIL_NO_ACCOUNT -> {

            }
        }
    }

    override fun createPresenter(): LoginPresenter {
        return LoginPresenter()
    }

    private fun verifyAll(): Boolean{
        if(password.text.toString().length < 6){
            password_layout.error = resources.getString(R.string.password_length_no_enough)
        }
        return account_layout.error == null && password_layout.error == null
    }
}