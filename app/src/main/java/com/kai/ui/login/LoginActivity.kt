package com.kai.ui.login

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.merge_toolbar.*

/**
 *
 * @ProjectName:    app-page
 * @Description:    登陆页面
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/7 15:45
 */
@Route(path = "/app/login")
class LoginActivity : BaseMvpActivity<LoginContract.View, LoginPresenter>() {
    override fun initView() {
        initImmersionBar(fitSystem = false)
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
}