package com.kai.ui.register

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.application.BaseInit
import com.kai.common.extension.customToast
import com.kai.common.extension.formatPhone
import com.kai.common.extension.getScreenHeight
import com.kai.common.extension.measureView
import com.kai.common.keyboard.KeyboardHeightObserver
import com.kai.common.keyboard.KeyboardHeightProvider
import com.kai.common.listener.CustomTextWatcher
import com.kai.common.utils.LogUtils
import com.kai.common.utils.StringUtils
import com.kai.entity.User
import com.kai.ui.login.LoginActivity
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
@Route(path = BaseInit.REGISTER)
class RegisterActivity : BaseMvpActivity<RegisterContract.View, RegisterPresenter>()
    ,KeyboardHeightObserver ,RegisterContract.View{

    companion object{
        const val REGISTER_SUCCESS = 0
        const val REGISTER_REPEAT = 1
        const val REGISTER_ERROR = 2
    }
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

        register.postDelayed({
            keyboardHeightProvider.start()
        },100)
        register.setOnClickListener {
            if(verifyAll()){
                mPresenter?.register(StringUtils.trim(account.text.toString()),
                        password.text.toString(),
                        security_question.text.toString(),
                        security_question_answer.text.toString())
            }
        }

        account.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                account.formatPhone(start,count>0)
                if(account_layout.error != null || account.text.toString().replace(" ","").length >= 11){
                    if(StringUtils.verifyPhone(account.text.toString())){
                        account_layout.error = null
                    } else {
                        account_layout.error = resources.getString(R.string.phone_format_error)
                    }
                }
            }
        })

        password.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(password_layout.error != null){
                    if(password.text.toString().length >= 6){
                        password_layout.error = null
                    } else {
                        password_layout.error = resources.getString(R.string.password_length_no_enough)
                    }
                }
            }
        })


        repeat_password.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(repeat_password_layout.error != null){
                    if(repeat_password.text.toString().length >= 6){
                        if(repeat_password.text.toString() == password.text.toString()){
                            repeat_password_layout.error = null
                        } else {
                            repeat_password_layout.error = resources.getString(R.string.password_repeat_no_same)
                        }
                    } else {
                        repeat_password_layout.error = resources.getString(R.string.password_length_no_enough)
                    }
                }
            }
        })

        security_question.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(security_question_layout.error != null){
                    if(security_question.text.toString().isNotEmpty()){
                        security_question_layout.error = null
                    } else {
                        security_question_layout.error = resources.getString(R.string.security_question_empty)
                    }
                }
            }
        })



        security_question_answer.addTextChangedListener(object: CustomTextWatcher(){
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(security_question_answer_layout.error != null){
                    if(security_question_answer.text.toString().isNotEmpty()){
                        security_question_answer_layout.error = null
                    } else {
                        security_question_answer_layout.error = resources.getString(R.string.security_answer_empty)
                    }
                }
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
        LogUtils.e("RegisterActivity","height is $height")
        root.post {
            if(registerOriginBottom == 0){
                registerOriginBottom = content.bottom
            }
            val rootHeight = getScreenHeight()

            var changeHeight = if (height < 0) {
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
            }else{
               changeHeight = if(toolbar.top < 0){
                    -toolbar.measureView()[1]
                } else {
                    0
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

    override fun onRegister(code: Int,user: User?) {
        when(code){
            REGISTER_SUCCESS ->{
                postStickyEvent(user,LoginActivity.REGISTER_CALLBACK,LoginActivity::class.java.name)
                finish()
                customToast(resources.getString(R.string.register_success))
            }
            REGISTER_ERROR -> {
                customToast(resources.getString(R.string.register_fail))
            }
            REGISTER_REPEAT ->{
                customToast(resources.getString(R.string.register_repeat))
            }
        }
    }

    override fun createPresenter(): RegisterPresenter {
        return RegisterPresenter()
    }



    private fun verifyAll():Boolean{
        if(!StringUtils.verifyPhone(account.text.toString())){
            account_layout.error = resources.getString(R.string.phone_format_error)
        }

        if(password.text.toString().isEmpty() || password.text.toString().length < 6){
            password_layout.error = resources.getString(R.string.password_length_no_enough)
        }


        if(repeat_password.text.toString().isEmpty() || repeat_password.text.toString().length < 6){
            repeat_password_layout.error = resources.getString(R.string.password_length_no_enough)
        } else {
            if(repeat_password.text.toString() != password.text.toString()){
                repeat_password_layout.error = resources.getString(R.string.password_repeat_no_same)
            }
        }
        if(security_question.text.toString().isEmpty() ){
            security_question_layout.error = resources.getString(R.string.security_question_empty)
        }


        if(security_question_answer.text.toString().isEmpty() ){
            security_question_answer_layout.error = resources.getString(R.string.security_answer_empty)
        }

        return account_layout.error == null
                && password_layout.error == null
                && repeat_password_layout.error == null
                && security_question_layout.error == null
                &&  security_question_answer_layout.error == null
    }
}