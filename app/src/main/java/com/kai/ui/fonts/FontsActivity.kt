package com.kai.ui.fonts

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.utils.SharedPreferenceUtils
import com.kai.ui.main.MainActivity
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_fonts.*
import kotlinx.android.synthetic.main.merge_toolbar.*

/**
 *
 * @ProjectName:    app-bookpage
 * @Description:    全局字体
 *                   使用自定义字体在android29上报错
 *                   https://blog.csdn.net/bigsmart12/article/details/103291014
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/6 10:58
 */
@Route(path = "/app/fonts")
class FontsActivity : BaseMvpActivity<IView, BasePresenter<IView>>() {
    companion object {
        const val CURRENT_FONT = "current_font"
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        initImmersionBar(view = toolbar, fitSystem = false)
        toolbar_title.text = "字体"
        back.setOnClickListener {
            finish()
        }
        SharedPreferenceUtils.getInstance()?.let {
            current_font.text = "当前字体 - ${
                it.getString(CURRENT_FONT, "fonts/方正准圆.ttf")
                    .replace("fonts/", "")
                    .replace(".ttf", "")
                    .replace(".TTF", "").trim()
            }字体"
        }

        pop_font.setOnClickListener {
            reLoadFont("fonts/pop字体.ttf")
        }
        hkzh_font.setOnClickListener {
            reLoadFont("fonts/华康中黑字体.TTF")
        }

        hksn_font.setOnClickListener {
            reLoadFont("fonts/华康少女字体.ttf")
        }

        hwxs_font.setOnClickListener {
            reLoadFont("fonts/华文新宋.ttf")
        }

        hwxk_font.setOnClickListener {
            reLoadFont("fonts/华文行楷.ttf")
        }

        wqywmh_font.setOnClickListener {
            reLoadFont("fonts/文泉驿微米黑.ttf")
        }
        fzzy_font.setOnClickListener {
            reLoadFont("fonts/方正准圆.TTF")
        }

        fzzyuan_font.setOnClickListener {
            reLoadFont("fonts/方正正圆.ttf")
        }


        ml_font.setOnClickListener {
            reLoadFont("fonts/明兰.ttf")
        }
        kt_font.setOnClickListener {
            reLoadFont("fonts/楷体.ttf")
        }


        pglh_font.setOnClickListener {
            reLoadFont("fonts/苹果丽黑.ttf")
        }

    }

    override fun setLayoutId(): Int {
        return R.layout.activity_fonts
    }


    private fun reLoadFont(fontPath: String) {
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath(fontPath)
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )
        SharedPreferenceUtils.getInstance()?.let {
            it.putString(CURRENT_FONT, fontPath)
        }
        postStickyEvent(0, MainActivity.CODE_FROM_FONTS, MainActivity::class.java.name)
        finish()
    }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }


}