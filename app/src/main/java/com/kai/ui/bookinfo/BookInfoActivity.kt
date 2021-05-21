package com.kai.ui.bookinfo

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.appbar.AppBarLayout
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.bookpage.model.BookRecommend
import com.kai.common.extension.customToast
import com.kai.common.extension.getScreenHeight
import com.kai.common.extension.getScreenWidth
import com.kai.common.extension.measureView
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import com.kai.ui.bookdetail.BookDetailActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_book_info.*
import kotlinx.android.synthetic.main.activity_book_info.appBar
import kotlinx.android.synthetic.main.activity_book_info.info_layout
import kotlinx.android.synthetic.main.activity_main.*
import skin.support.SkinCompatManager
import skin.support.widget.SkinCompatSupportable
import java.lang.Exception
import kotlin.math.abs


@Route(path = "/app/bookinfo")
class BookInfoActivity : BaseMvpActivity<BookInfoContract.View, BookInfoPresenter>(),
    BookInfoContract.View, SkinCompatSupportable {

    private var bitmapWidth = 0
    private var bitmapHeight = 0
    private var mRecommend: BookRecommend? = null
    override fun initView() {
        initImmersionBar(view = toolbar, fitSystem = false)
        val height = getScreenHeight() / 6 * 2
        val layoutParams = sector.layoutParams
        layoutParams.height = height
        sector.layoutParams = layoutParams
        if (SkinCompatManager.getInstance().curSkinName == "night") {
            sector.post {
                sector.changeBackgroundColor(R.color.app_background_night)
            }

        } else {
            sector.post {
                sector.changeBackgroundColor(R.color.app_background)
            }

        }
        bitmapWidth = getScreenWidth()
        bitmapHeight = height
        setMargin(height, 1f)
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val alpha = if (verticalOffset != 0) {
                abs(verticalOffset).toFloat() / (appBar.height - toolbar_layout.height).toFloat()
            } else {
                0f
            }
            real_toolbar_layout.alpha = (alpha + alpha / 2)
            sector.post {
                sector.setChangeHeight(1 - (alpha + alpha / 2))
            }
            setMargin(height, alpha)
        })

        val url = intent.getStringExtra("url")
        url?.let {
            if (it.isNotEmpty()) {
                mPresenter?.getBookDetail(it)
                mPresenter?.recommendList(it)
            } else {
                finish()
            }
        }


        back.setOnClickListener {
            finish()
        }


        add_like_layout.setOnClickListener {
            mRecommend?.let {
                if (it.updateLikeState()) {

                    if (!it.getCurrentLikeState()) {
                        add_like.text = "收藏"
                        customToast("取消收藏成功")
                    } else {
                        add_like.text = "已收藏"
                        customToast("收藏成功")
                    }

                }
            }
        }
        add_shelf_layout.setOnClickListener {
            mRecommend?.let {
                if (it.updateShelfState()) {
                    if (!it.getCurrentShelfState()) {
                        add_shelf_layout.isEnabled = true
                        add_shelf.text = "加书架"
                    } else {
                        add_shelf_layout.isEnabled = false
                        add_shelf.text = "已加入"
                        customToast("加入书架成功")
                    }
                } else {
                    it.save()
                    if(it.updateShelfState()){
                        if (!it.getCurrentShelfState()) {
                            add_shelf_layout.isEnabled = true
                            add_shelf.text = "加书架"
                        } else {
                            add_shelf_layout.isEnabled = false
                            add_shelf.text = "已加入"
                            customToast("加入书架成功")
                        }
                    }
                }
            }
        }
        read.setOnClickListener {
            mRecommend?.let {
                val searchBook = it.toSearchBook()
                ARouter.getInstance().build("/app/book").navigation()
                postStickyEvent(
                    searchBook,
                    BookDetailActivity.BOOK_DETAIL,
                    BookDetailActivity::class.java.name
                )
            }
        }

    }

    override fun createPresenter(): BookInfoPresenter? {
        return BookInfoPresenter()
    }


    override fun setLayoutId(): Int {
        return R.layout.activity_book_info
    }


    private fun setMargin(height: Int, alpha: Float) {
        val layoutHeight = info_layout.measureView()[1]
        real_toolbar_layout.layoutParams.width = getScreenWidth()
        val realToolbarLayoutHeight = real_toolbar_layout.measureView()[1]
        val marginLayoutParams = info_layout.layoutParams as ViewGroup.MarginLayoutParams
        marginLayoutParams.leftMargin = (ScreenUtils.dpToPx(18) * (1 - alpha)).toInt()
        marginLayoutParams.rightMargin = (ScreenUtils.dpToPx(18) * (1 - alpha)).toInt()
        var topMargin = ((height - (layoutHeight / 3 * 2)) * (1 - alpha)).toInt()
        if (topMargin < realToolbarLayoutHeight) {
            topMargin = realToolbarLayoutHeight
        }
        marginLayoutParams.topMargin = topMargin
        info_layout.layoutParams = marginLayoutParams


        val bookLayoutParams = book_intro.layoutParams as ViewGroup.MarginLayoutParams
        bookLayoutParams.topMargin =
            ((layoutHeight - (layoutHeight / 3 * 2) * (1 - alpha)) + ScreenUtils.dpToPx(50)).toInt()
        book_intro.layoutParams = bookLayoutParams
    }

    override fun onBookDetail(recommend: BookRecommend) {
        runOnUiThread {
            try {
                mRecommend = recommend
                book_name.text = recommend.bookName
                toolbar_title.text = recommend.bookName
                book_author.text = recommend.authorName
                update_chapter.text = recommend.newChapterName
                update_time.text = recommend.updateTime
                book_descriptor.text = recommend.bookDescriptor
                val options = RequestOptions()
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(16)
                        )
                    )
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                Glide.with(cover)
                    .load(recommend.bookCoverUrl)
                    .apply(options)
                    .dontAnimate()
                    .into(cover)
                loadBlur(recommend.bookCoverUrl)


                if (!recommend.getCurrentLikeState()) {
                    add_like_layout.isEnabled = true
                    add_like.text = "收藏"
                } else {
                    add_like_layout.isEnabled = false
                    add_like.text = "已收藏"
                }

                if (!recommend.getCurrentShelfState()) {
                    add_shelf_layout.isEnabled = true
                    add_shelf.text = "加书架"
                } else {
                    add_shelf_layout.isEnabled = false
                    add_shelf.text = "已加入"
                }


                recommend.updateReadState()
            } catch (e: Exception) {
                LogUtils.e("BookInfoActivity", "onBookDetail error is $e")
            }

        }
    }

    override fun onRecommendList(list: ArrayList<BookRecommend>) {
        val gridLayoutManager = GridLayoutManager(this, 3)

        gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
        recommend_list.layoutManager = gridLayoutManager
        val recommendItemListAdapter = RecommendItemListAdapter()
        recommendItemListAdapter.setHasStableIds(true)
        recommendItemListAdapter.setOnItemClickListener { _, _, i ->
            try {
                val bookRecommend = list[i]
                ARouter.getInstance()
                    .build("/app/bookinfo")
                    .withString("url", bookRecommend.bookUrl)
                    .navigation()
            } catch (e: java.lang.Exception) {

            }
        }
        recommend_list.adapter = recommendItemListAdapter
        recommendItemListAdapter.setNewInstance(list)
    }


    private fun loadBlur(url: String) {
        Thread {
            val blur = RequestOptions()
                .skipMemoryCache(true)
                .transform(BlurTransformation(25, 20))
                .diskCacheStrategy(DiskCacheStrategy.ALL)

            val into = Glide.with(sector)
                .asBitmap()
                .load(url)
                .apply(blur)
                .override(bitmapWidth, bitmapHeight)
                .into(bitmapWidth, bitmapHeight)
            val drawable = into.get()
            runOnUiThread {
                if (drawable.width < bitmapWidth) {
                    loadBlur(url)
                } else {
                    sector.setBitmap(drawable)
                }
            }
        }.start()
    }

    inner class RecommendItemListAdapter :
        BaseQuickAdapter<BookRecommend, BaseViewHolder>(R.layout.item_recyclerview_info_item) {
        override fun convert(holder: BaseViewHolder, item: BookRecommend) {
            val cover = holder.getView<ImageView>(R.id.cover)
            val name = holder.getView<TextView>(R.id.book_name)
            val descriptor = holder.getView<TextView>(R.id.book_descriptor)
            val layout = holder.getView<ConstraintLayout>(R.id.layout)
            val width = (getScreenWidth()) / 2 + 10
            layout.layoutParams.width = width
            val coverWidth = (width / 2) - ScreenUtils.dpToPx(16)
            cover.layoutParams.width = coverWidth
            cover.layoutParams.height = (coverWidth / 0.75).toInt()
            layout.layoutParams.height = (coverWidth / 0.75).toInt() + ScreenUtils.dpToPx(16)
            name.layoutParams.width = width / 2
            descriptor.layoutParams.width = width / 2
            mPresenter?.let {
                val localBookDetail = it.localBookDetail(item.bookUrl)
                if (localBookDetail == null || localBookDetail.checkIsEmpty()) {
                    it.bookDetail(item, object : BookInfoPresenter.OnBookDetail {
                        override fun onBookDetail(bookRecommend: BookRecommend) {
                            try {
                                if (item.bookUrl == bookRecommend.bookUrl) {
                                    if (bookRecommend.bookCoverUrl.isNotEmpty()) {
                                        val options = RequestOptions()
                                            .transform(
                                                MultiTransformation(
                                                    CenterCrop(),
                                                    RoundedCorners(16)
                                                )
                                            )
                                            .skipMemoryCache(true)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        Glide.with(cover)
                                            .load(bookRecommend.bookCoverUrl)
                                            .apply(options)
                                            .dontAnimate()
                                            .into(cover)
                                    }

                                    name.text = bookRecommend.bookName
                                    descriptor.text = bookRecommend.bookDescriptor
                                }
                            } catch (e: java.lang.Exception) {
                                LogUtils.e("BookRecommendFragment", "error is $e")
                            }
                        }
                    })
                } else {
                    if (localBookDetail != null && localBookDetail.bookCoverUrl.isNotEmpty()) {
                        val options = RequestOptions()
                            .transform(
                                MultiTransformation(
                                    CenterCrop(),
                                    RoundedCorners(16)
                                )
                            )
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                        Glide.with(cover)
                            .load(localBookDetail.bookCoverUrl)
                            .apply(options)
                            .dontAnimate()
                            .into(cover)
                    }

                    name.text = localBookDetail.bookName
                    descriptor.text = localBookDetail.bookDescriptor
                }
            }


        }

        override fun getItemId(position: Int): Long {
            var id = position.toLong()
            try {
                val item = getItem(position)
                id = item.bookUrl.hashCode().toLong()
            } catch (e: java.lang.Exception) {

            }
            return id
        }
    }


    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @SuppressLint("ResourceAsColor")
    override fun applySkin() {

    }

}