package com.kai.ui.fonts

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.ui.main.MainActivity
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_fonts.*

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
class FontsActivity : BaseMvpActivity<FontsContract.View, FontsPresenter>(), FontsContract.View {
    override fun initView() {
        initImmersionBar(fitSystem = false, color = R.color.app_background)

        test_gtw.setOnClickListener {
            reLoadFont("fonts/gtw.ttf")
        }


        test_roboto.setOnClickListener {
            reLoadFont("fonts/RobotoCondensed-Regular.ttf")
        }
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_fonts
    }


    override fun onLoadFonts(list: List<String>) {

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
        postStickyEvent(0, MainActivity.CODE_FROM_FONTS, MainActivity::class.java.name)
        finish()
    }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}