package com.kai.ui.bookinfo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.kai.base.application.BaseInit
import com.kai.bookpage.model.BookRecommend
import com.kai.common.extension.customToast
import com.kai.common.extension.getScreenHeight
import com.kai.common.extension.getScreenWidth
import com.kai.common.extension.measureView
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import com.kai.ui.bookdetail.BookDetailActivity
import com.kai.util.DialogHelper
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_book_info.*
import kotlinx.android.synthetic.main.activity_book_info.appBar
import kotlinx.android.synthetic.main.activity_book_info.info_layout
import skin.support.SkinCompatManager
import skin.support.widget.SkinCompatSupportable
import java.lang.Exception
import kotlin.math.abs


@Route(path = BaseInit.BOOKINFO)
class BookInfoActivity : BaseMvpActivity<BookInfoContract.View, BookInfoPresenter>(),
    BookInfoContract.View, SkinCompatSupportable {
    private var bitmapWidth = 0
    private var bitmapHeight = 0
    private var mRecommend: BookRecommend? = null
    override fun initView() {
        initImmersionBar(view = real_toolbar_layout, fitSystem = false)
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
                DialogHelper.instance?.showLoadingDialog(activity = this)


                Thread{
                    Thread.sleep(200)
                    mPresenter?.getBookDetail(it)
                    mPresenter?.recommendList(it)
                }.start()


            } else {
                finish()
            }
        }


        back.setOnClickListener {
            finish()
        }


        add_like_layout.setOnClickListener {
            mRecommend?.let {
               val addLike = add_like.text == resources.getString(R.string.like)
               mPresenter?.likeOption(it.bookUrl,addLike)
            }
        }
        add_shelf_layout.setOnClickListener {
            mRecommend?.let {
                val addShelf = add_shelf.text == resources.getString(R.string.add_book_shelf)
                mPresenter?.shelfOption(it.bookUrl,addShelf)
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

//        val marginLayoutParams1 = toolbar.layoutParams as ViewGroup.MarginLayoutParams
//        marginLayoutParams1.topMargin = ScreenUtils.getStatusBarHeight()
//        toolbar.layoutParams = marginLayoutParams1
    }

    override fun onBookDetail(recommend: BookRecommend) {
        DialogHelper.instance?.hintLoadingDialog()
        runOnUiThread {
            try {
                mRecommend = recommend
                book_name.text = recommend.bookName
                toolbar_title.text = recommend.bookName
                book_author.text = recommend.authorName
                update_chapter.text = recommend.newChapterName
                update_time.text = recommend.updateTime
                book_descriptor.text = recommend.bookDescriptor
                loadBlur(recommend.bookCoverUrl)
                mPresenter?.getBookLike(recommend.bookUrl)
                mPresenter?.getBookShelf(recommend.bookUrl)

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
                    .build(BaseInit.BOOKINFO)
                    .withString("url", bookRecommend.bookUrl)
                    .navigation()
            } catch (e: java.lang.Exception) {

            }
        }
        recommend_list.adapter = recommendItemListAdapter
        recommendItemListAdapter.setNewInstance(list)
    }

    override fun onLikeOption(code:Int) {
        when (code) {
            0 -> {
                customToast("请登录")
                ARouter.getInstance().build(BaseInit.LOGIN)
                    .navigation()
            }
            1 -> {
                mRecommend?.let {
                    customToast("已收藏")
                    mPresenter?.getBookLike(it.bookUrl)
                }
            }
            else -> {
                mRecommend?.let {
                    customToast("已取消")
                    mPresenter?.getBookLike(it.bookUrl)
                }
            }
        }
    }

    override fun onShelfOption(code: Int) {
        when (code) {
            0 -> {
                customToast("请登录")
                ARouter.getInstance().build(BaseInit.LOGIN)
                    .navigation()
            }
            1 -> {
                mRecommend?.let {
                    customToast("已加入")
                    mPresenter?.getBookShelf(it.bookUrl)
                }
            }
            else -> {
                mRecommend?.let {
                    customToast("已移除")
                    mPresenter?.getBookShelf(it.bookUrl)
                }
            }
        }
    }

    override fun onGetBookLike(like:Boolean) {
        if (!like) {
            add_like_layout.isEnabled = true
            Glide.with(like_image).load(R.drawable.add_like_night).into(like_image)
            add_like.text = resources.getString(R.string.like)
        } else {
            add_like_layout.isEnabled = true
            Glide.with(like_image).load(R.drawable.like_selected).into(like_image)
            add_like.text = resources.getString(R.string.liked)
        }
    }

    override fun onGetBookShelf(shelf:Boolean) {
        if (!shelf) {
            add_shelf_layout.isEnabled = true
            Glide.with(shelf_image).load(R.drawable.book_shelf_night).into(shelf_image)
            add_shelf.text =  resources.getString(R.string.add_book_shelf)
        } else {
            add_shelf_layout.isEnabled = true
            Glide.with(shelf_image).load(R.drawable.book_shelf_select).into(shelf_image)
            add_shelf.text = resources.getString(R.string.shelfed)
        }

    }


    private fun loadBlur(url: String) {
        Observable.create<Bitmap> {
            var isMatch = false
            while(!isMatch){
                val blur = RequestOptions()
                    .skipMemoryCache(true)
                    .error(R.drawable.default_loading)
                    .placeholder(R.drawable.default_loading)
                    .transform(BlurTransformation(25, 20))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                val into = Glide.with(sector)
                    .asBitmap()
                    .load(url)
                    .apply(blur)
                    .override(bitmapWidth, bitmapHeight)
                    .into(bitmapWidth, bitmapHeight)
                val drawable = into.get()
                if(drawable.width >= bitmapWidth){
                    isMatch = true
                    it.onNext(drawable)
                }
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                sector.setBitmap(it)
                val options = RequestOptions()
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(16)
                        )
                    )
                    .error(R.drawable.default_loading)
                    .placeholder(R.drawable.default_loading)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

                Glide.with(book_cover)
                    .load(url)
                    .apply(options)
                    .into(book_cover)
            }
    }

    inner class RecommendItemListAdapter :
        BaseQuickAdapter<BookRecommend, BaseViewHolder>(R.layout.item_recyclerview_info_item) {
        override fun convert(holder: BaseViewHolder, item: BookRecommend) {
            val cover = holder.getView<ImageView>(R.id.cover)
            val name = holder.getView<TextView>(R.id.book_name)
            val descriptor = holder.getView<TextView>(R.id.book_descriptor_item)
            val layout = holder.getView<ConstraintLayout>(R.id.layout)
            val nameLayout = holder.getView<LinearLayout>(R.id.name_layout)
            val width = (getScreenWidth()) / 2 + name.textSize.toInt() * 2
            layout.layoutParams.width = width
            val coverWidth = (width / 2) - ScreenUtils.dpToPx(16)
            cover.layoutParams.width = coverWidth
            cover.layoutParams.height = (coverWidth / 0.75).toInt()
            layout.layoutParams.height = (coverWidth / 0.75).toInt()
            name.layoutParams.width = width / 2
            descriptor.layoutParams.width = width / 2
            nameLayout.layoutParams.height = (coverWidth / 0.75).toInt()

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
                                            .error(R.drawable.default_loading)
                                            .placeholder(R.drawable.default_loading)
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
                            .error(R.drawable.default_loading)
                            .placeholder(R.drawable.default_loading)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                        Glide.with(cover)
                            .load(localBookDetail.bookCoverUrl)
                            .apply(options)
                            .dontAnimate()
                            .into(cover)
                    }
                    name.text = localBookDetail.bookName
                    name.postDelayed({
                        val fl = (((coverWidth / 0.75).toInt() - name.measureView()[1]) / name.measureView()[1]).toInt()
                        descriptor.setLines(fl-1)
                        descriptor.text = localBookDetail.bookDescriptor
                        descriptor.lineCount
                    },10)
                    descriptor.postDelayed({
                        val fl = (((coverWidth / 0.75).toInt() - name.measureView()[1]) / name.measureView()[1]).toInt()
                        descriptor.setLines(fl)
                        descriptor.text = localBookDetail.bookDescriptor
                    },30)

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