package com.kai.ui.forgetpassword

import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.extension.getScreenWidth
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.merge_toolbar.*
import me.jessyan.autosize.utils.ScreenUtils

/**
 *
 * @ProjectName:    app-page
 * @Description:    注册页面
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/7 15:45
 */
@Route(path = "/app/forgetPassword")
class ForgetPasswordActivity : BaseMvpActivity<ForgetPasswordContract.View,ForgetPasswordPresenter>() {
    override fun initView() {
        initImmersionBar(view = toolbar,fitSystem = false)
        toolbar_title.text = resources.getString(R.string.reset_password)
        back.setOnClickListener {
            finish()
        }
        commit.setOnClickListener {
            question_content_layout.visibility = View.INVISIBLE
            new_password_layout.visibility = View.VISIBLE
        }
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
}