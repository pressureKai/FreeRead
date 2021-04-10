package com.kai.ui.register

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.extension.getScreenWidth
import io.github.inflationx.viewpump.ViewPumpContextWrapper
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
@Route(path = "/app/register")
class RegisterActivity : BaseMvpActivity<RegisterContract.View, RegisterPresenter>() {
    override fun initView() {

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
}