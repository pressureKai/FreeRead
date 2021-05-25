package com.kai.ui.setting

import android.content.Context
import android.text.Editable
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.bookpage.page.PageLoader
import com.kai.common.listener.CustomTextWatcher
import com.kai.common.utils.SharedPreferenceUtils
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.merge_toolbar.*

@Route(path = "/app/setting")
class SettingActivity: BaseMvpActivity<IView, BasePresenter<IView>>() {

    override fun initView() {
        initImmersionBar(view = toolbar,fitSystem = false)
        val count = SharedPreferenceUtils.getInstance()?.getInt(PageLoader.CHAPTERCOUNT, 3)
        chapter_count.addTextChangedListener(object :CustomTextWatcher(){
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                SharedPreferenceUtils.getInstance()?.putInt(PageLoader.CHAPTERCOUNT, s.toString().toInt())
            }
        })

        chapter_count.hint = count.toString()
        toolbar_title.text = resources.getString(R.string.setting)
        back.setOnClickListener {
            finish()
        }
    }

    override fun setLayoutId(): Int {
       return R.layout.activity_setting
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}