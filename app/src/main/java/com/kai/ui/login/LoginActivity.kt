package com.kai.ui.login

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.extension.getScreenWidth
import com.kai.common.keyboard.KeyboardHeightObserver
import com.kai.common.keyboard.KeyboardHeightProvider
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_login.*

/**
 *
 * @ProjectName:    app-page
 * @Description:    登陆页面
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/7 15:45
 */
@Route(path = "/app/login")
class LoginActivity : BaseMvpActivity<LoginContract.View, LoginPresenter>(),
    KeyboardHeightObserver {

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
}