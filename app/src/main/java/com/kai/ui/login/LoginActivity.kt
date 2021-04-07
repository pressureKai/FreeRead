package com.kai.ui.login

import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity

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

    }

    override fun setLayoutId(): Int {
        return R.layout.activity_login
    }
}