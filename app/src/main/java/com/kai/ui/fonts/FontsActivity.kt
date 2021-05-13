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
    companion object{
        const val CURRENT_FONT = "current_font"
    }
    @SuppressLint("SetTextI18n")
    override fun initView() {
        initImmersionBar(view = toolbar,fitSystem = false)
        toolbar_title.text = "字体"
        back.setOnClickListener {
            finish()
        }
        SharedPreferenceUtils.getInstance()?.let {
            current_font.text = "当前字体 - ${
                it.getString(CURRENT_FONT,"fonts/方正准圆.ttf")
                        .replace("fonts/","")
                        .replace(".ttf","")
                        .replace(".TTF","").trim()
            }字体"
        }

        current_font.setOnClickListener {
            reLoadFont("fonts/gtw.ttf")
        }
        pop_font.setOnClickListener {
            reLoadFont("fonts/pop字体.ttf")
        }
        hkzh_font.setOnClickListener {
            reLoadFont("fonts/华康中黑字体.TTF")
        }
        hkww_font.setOnClickListener {
            reLoadFont("fonts/华康娃娃体.TTF")
        }
        hksn_font.setOnClickListener {
            reLoadFont("fonts/华康少女字体.ttf")
        }
        hwcy_font.setOnClickListener {
            reLoadFont("fonts/华文彩云.TTF")
        }
        hwxs_font.setOnClickListener {
            reLoadFont("fonts/华文新宋.ttf")
        }
        hwxw_font.setOnClickListener {
            reLoadFont("fonts/华文新魏.TTF")
        }
        hwxk_font.setOnClickListener {
            reLoadFont("fonts/华文行楷.ttf")
        }
        yy_font.setOnClickListener {
            reLoadFont("fonts/幼圆.ttf")
        }
        wryh_font.setOnClickListener {
            reLoadFont("fonts/微软雅黑14M.ttf")
        }
        wqywmh_font.setOnClickListener {
            reLoadFont("fonts/文泉驿微米黑.ttf")
        }
        fzzy_font.setOnClickListener {
            reLoadFont("fonts/方正准圆.TTF")
        }
        fzhl_font.setOnClickListener {
            reLoadFont("fonts/方正华隶.ttf")
        }
        fzktjt_font.setOnClickListener {
            reLoadFont("fonts/方正卡通简体.ttf")
        }
        fzgl_font.setOnClickListener {
            reLoadFont("fonts/方正古隶.ttf")
        }

        fzqtjt_font.setOnClickListener {
            reLoadFont("fonts/方正启体简体.ttf")
        }
        fzxz_font.setOnClickListener {
            reLoadFont("fonts/方正小篆.TTF")
        }
        fzzyuan_font.setOnClickListener {
            reLoadFont("fonts/方正正圆.ttf")
        }
        fzlxtjt_font.setOnClickListener {
            reLoadFont("fonts/方正流行体简体.ttf")
        }
        fzybxs_font.setOnClickListener {
            reLoadFont("fonts/方正硬笔行书.TTF")
        }

        fzcy_font.setOnClickListener {
            reLoadFont("fonts/方正粗圆.ttf")
        }
        fzpty_font.setOnClickListener {
            reLoadFont("fonts/方正胖头鱼.TTF")
        }

        fzjljt_font.setOnClickListener {
            reLoadFont("fonts/方正静蕾简体.TTF")
        }
        ml_font.setOnClickListener {
            reLoadFont("fonts/明兰.ttf")
        }
        kt_font.setOnClickListener {
            reLoadFont("fonts/楷体.ttf")
        }
        sjt_font.setOnClickListener {
            reLoadFont("fonts/瘦金体.ttf")
        }

        pglh_font.setOnClickListener {
            reLoadFont("fonts/苹果丽黑.ttf")
        }
        njygy_font.setOnClickListener {
            reLoadFont("fonts/诺基亚古印.ttf")
        }
        ls_font.setOnClickListener {
            reLoadFont("fonts/隶书.ttf")
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
                    .build())
            )
            .build())
        SharedPreferenceUtils.getInstance()?.let {
            it.putString(CURRENT_FONT,fontPath)
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