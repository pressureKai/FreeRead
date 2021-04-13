package com.kai.ui.forgetpassword

import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.customToast
import com.kai.common.listener.CustomTextWatcher
import com.kai.common.utils.LogUtils
import com.kai.entity.User
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_forget_password.account
import kotlinx.android.synthetic.main.activity_forget_password.password
import kotlinx.android.synthetic.main.activity_forget_password.password_layout
import kotlinx.android.synthetic.main.activity_forget_password.repeat_password
import kotlinx.android.synthetic.main.activity_forget_password.repeat_password_layout
import kotlinx.android.synthetic.main.merge_toolbar.*
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

/**
 *
 * @ProjectName:    app-page
 * @Description:    注册页面
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/7 15:45
 */
@Route(path = "/app/forgetPassword")
class ForgetPasswordActivity : BaseMvpActivity<ForgetPasswordContract.View,ForgetPasswordPresenter>(),ForgetPasswordContract.View {
    companion object{
        const val FORGET_PASSWORD_CODE = 0x11
    }
    private var user: User ?= null
    private var unRegister = false
    override fun initView() {
        initImmersionBar(view = toolbar,fitSystem = false)

        back.setOnClickListener {
            finish()
        }
        unRegister = intent
                .getBooleanExtra("unRegister", false)
        if(!unRegister){
            toolbar_title.text = resources.getString(R.string.reset_password)
        } else {
            toolbar_title.text = resources.getString(R.string.un_register)
            commit.text = "注销"
        }

        commit.setOnClickListener {
            if(question_content_layout.visibility == View.VISIBLE){
                if(verifyAnswer()){
                    user?.let {
                       if(it.answer == answer.text.toString()){
                           if(!unRegister){
                               question_content_layout.visibility = View.INVISIBLE
                               new_password_layout.visibility = View.VISIBLE
                           } else {
                               mPresenter?.deleteAccount(account.text.toString())
                           }

                       } else {
                           customToast(resources.getString(R.string.security_answer_error))
                       }
                    }
                }
            } else {
                if(verifyPassword()){
                    mPresenter?.updatePassword(account = account.text.toString(),password = password.text.toString())
                }
            }
        }

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
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_forget_password
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        if(baseEntity.code == FORGET_PASSWORD_CODE){
            try {
                val account = baseEntity.data as String
                mPresenter?.getUserByAccount(account)
            }catch (e: Exception){

            }
            EventBus.getDefault().removeStickyEvent(baseEntity)
        }
    }

    override fun createPresenter(): ForgetPasswordPresenter {
        return ForgetPasswordPresenter()
    }

    override fun onGetUserByAccount(entity: BaseEntity<User>) {
        runOnUiThread {
            if(entity.code == BaseEntity.ENTITY_SUCCESS_CODE){
                try {
                    val user = entity.data as User
                    account.text = user.account
                    question.text = user.question
                    this.user = user
                }catch (e: Exception){
                    LogUtils.e("ForgetPasswordActivity",e.toString())
                }
            } else {
                customToast(resources.getString(R.string.login_no_account))
                finish()
            }
        }
    }

    override fun onUpdatePassword(entity: BaseEntity<User>) {
        runOnUiThread {
            if(entity.code == BaseEntity.ENTITY_SUCCESS_CODE){
                customToast(resources.getString(R.string.update_password_success))
                finish()
            } else {
                customToast(resources.getString(R.string.update_password_fail))
            }
        }
    }

    override fun onDeleteAccount(entity: BaseEntity<String>) {
        runOnUiThread {
            if(entity.code == BaseEntity.ENTITY_SUCCESS_CODE){
                customToast(resources.getString(R.string.un_register_success))
                finish()
            } else {
                customToast(resources.getString(R.string.un_register_fail))
            }
        }
    }

    /**
      * #  验证密保问题答案是否通过验证
      * @return 返回值描述
      * @date 2021/4/13
      */
    private fun verifyAnswer(): Boolean{
        val answerString = answer.text.toString()
        if(answerString.isEmpty()){
            answer_layout.error = resources.getString(R.string.security_answer_empty)
        } else{
            answer_layout.error = null
        }
        return answerString.isNotEmpty()
    }


    private fun verifyPassword(): Boolean{
        if(password.text.toString().isEmpty()){
            password_layout.error = resources.getString(R.string.password_length_no_enough)
        }
        if(repeat_password.text.toString().isEmpty()){
            repeat_password_layout.error = resources.getString(R.string.password_length_no_enough)
        }
        return password_layout.error == null && repeat_password_layout.error == null
    }
}