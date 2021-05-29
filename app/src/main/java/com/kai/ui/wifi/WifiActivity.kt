package com.kai.ui.wifi

import android.content.Context
import android.text.TextUtils
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.appbar.AppBarLayout
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.application.BaseInit
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.getScreenWidth
import com.kai.crawler.entity.book.SearchBook
import com.kai.crawler.entity.source.Source
import com.kai.ui.bookdetail.BookDetailActivity
import com.kai.ui.main.MainActivity
import com.kai.util.DialogHelper
import com.kai.util.NetworkUtils
import com.kai.wifitransfer.Defaults
import com.kai.wifitransfer.ServerRunner
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlin.math.abs

@Route(path = BaseInit.WIFI)
class WifiActivity : BaseMvpActivity<IView, BasePresenter<IView>>() {
    override fun initView() {
        initImmersionBar(fitSystem = false,view = back_layout)
        val wifiIp = NetworkUtils.getConnectWifiIp(this@WifiActivity)
        if (wifiIp != null && !TextUtils.isEmpty(wifiIp)) {
            address.text = "http://" + NetworkUtils.getConnectWifiIp(this@WifiActivity)
                .toString() + ":" + Defaults.getPort()
            // 启动wifi传书服务器
            ServerRunner.startServer()
        }


        wifi_animation.layoutParams.height = getScreenWidth()
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->

            val alpha = if (verticalOffset != 0) {
                abs(verticalOffset).toFloat() / (appBar.height - toolbar.height).toFloat()
            } else {
                0f
            }
            back_layout.alpha = alpha
            toolbar.alpha = alpha
        })
        back.setOnClickListener {
            finish()
        }
        toolbar_title.text = resources.getString(R.string.wifi_transmission)
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_wifi
    }


    override fun onDestroy() {
        super.onDestroy()
        ServerRunner.stopServer()
    }

    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        val path = baseEntity.data.toString()
        if (path.isNotEmpty()) {
            try {
                var name = ""
                try {
                    name = path.substring(path.lastIndexOf("/") + 1, path.length)
                } catch (e: java.lang.Exception) {

                }

                val format = String.format(resources.getString(R.string.wifi_read), name)

                val bookRecommend = BookRecommend()
                bookRecommend.bookType = BookRecommend.LOCAL_RECOMMEND
                bookRecommend.bookUrl = path
                bookRecommend.authorName = "wifi"
                bookRecommend.bookDescriptor = name
                bookRecommend.bookName = name
                bookRecommend.bookCoverUrl = R.mipmap.ic_launcher.toString()
                bookRecommend.newChapterUrl = path
                bookRecommend.newChapterName = name
                bookRecommend.isLocal = true
                bookRecommend.save()


                runOnUiThread {
                    DialogHelper.instance?.hintRemindDialog()
                    DialogHelper.instance?.showRemindDialog(
                        this,
                        format,
                        remindDialogClickListener = object :
                            DialogHelper.RemindDialogClickListener {
                            override fun onRemindDialogClickListener(positive: Boolean) {
                                if (positive) {
                                    val searchBook = SearchBook()
                                    val sl = SearchBook.SL(path, Source(0, "", "", ""))
                                    searchBook.sources = arrayListOf(sl)
                                    searchBook.title = name
                                    searchBook.descriptor = name
                                    searchBook.author = "wifi"
                                    searchBook.cover = ""

                                    DialogHelper.instance?.hintRemindDialog()
                                    ARouter.getInstance().build(BaseInit.BOOK).navigation()
                                    this@WifiActivity.postStickyEvent(
                                        searchBook,
                                        BookDetailActivity.BOOK_DETAIL,
                                        BookDetailActivity::class.java.name
                                    )
                                }
                                DialogHelper.instance?.hintRemindDialog()
                            }
                        })
                }

            } catch (e: Exception) {

            }

        }
    }


    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


}