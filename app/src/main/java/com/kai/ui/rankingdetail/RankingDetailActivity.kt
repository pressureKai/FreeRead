package com.kai.ui.rankingdetail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.bookpage.model.BookRecommend
import com.kai.common.utils.GlideUtils
import com.kai.common.utils.LogUtils
import com.kai.view.cardstack.RxCardStackView
import com.kai.view.cardstack.tools.RxAdapterStack
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_ranking_detail.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_stack_ranking.*
import kotlinx.android.synthetic.main.merge_toolbar.*
import kotlinx.android.synthetic.main.merge_toolbar.back
import java.lang.Exception

@Route(path = "/app/ranking")
class RankingDetailActivity : BaseMvpActivity<RankingDetailContract.View, RankingDetailPresenter>(),
    RankingDetailContract.View, RxCardStackView.ItemExpendListener {
    private lateinit var mRankingAdapter: RankingAdapter
    private var type = 0
    private var url = ""
    override fun initView() {
        initImmersionBar(view = toolbar, fitSystem = false)
        type = intent.getIntExtra("type", 0)
        intent.getStringExtra("url")?.let {
            url = it
        }

        toolbar_title.text = BookRecommend.typeToName(type)
        back.setOnClickListener {
            finish()
        }
        ranking_stack.itemExpendListener = this
        mRankingAdapter = RankingAdapter(this)
        ranking_stack.setAdapter(mRankingAdapter)
        url?.let {
            mPresenter?.ranking(type,url)
        }

        pre.setOnClickListener {
            ranking_stack.pre()
        }
        next.setOnClickListener {
            ranking_stack.next()
        }
        info.setOnClickListener {

            try {
                val currentSelect = ranking_stack.getCurrentSelect()
                val bookRecommend = mRankingAdapter.mData[currentSelect]
                ARouter.getInstance()
                    .build("/app/bookinfo")
                    .withString("url", bookRecommend.bookUrl)
                    .navigation()

            }catch (e:Exception){

            }

        }


    }

    override fun createPresenter(): RankingDetailPresenter? {
        return RankingDetailPresenter()
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_ranking_detail
    }

    override fun onItemExpend(expend: Boolean) {
        if(!expend){
            pre.visibility = View.GONE
            next.visibility = View.GONE
            info.visibility = View.GONE
        } else {
            if(ranking_stack.getCurrentSelect() != mRankingAdapter.mData.size - 1){
                next.visibility =View.VISIBLE
            }else{
                next.visibility =View.GONE
            }

            if(ranking_stack.getCurrentSelect() != 0){
                pre.visibility = View.VISIBLE
            } else {
                pre.visibility = View.GONE
            }
            info.visibility =View.VISIBLE
        }
    }


    inner class RankingAdapter(context: Context) : RxAdapterStack<BookRecommend>(context) {
        override fun bindView(
            data: BookRecommend,
            position: Int,
            holder: RxCardStackView.ViewHolder?
        ) {
            (holder as RankingViewHolder).onBind(data)
        }

        override fun onCreateView(parent: ViewGroup?, viewType: Int): RxCardStackView.ViewHolder? {
            val view = getLayoutInflater().inflate(R.layout.item_stack_ranking, parent, false)
            return RankingViewHolder(view)
        }

        override val itemCount: Int
            get() = mData.size
    }


    inner class RankingViewHolder(view: View) : RxCardStackView.ViewHolder(view) {
        private var mDescriptorLayout: LinearLayout = view.findViewById(R.id.descriptor_layout)
        private var mDescriptor: TextView = view.findViewById(R.id.descriptor)
        private var mBookName: TextView = view.findViewById(R.id.book_name)
        private var mBookAuthor: TextView = view.findViewById(R.id.book_author)
        private var mUpdateChapter: TextView = view.findViewById(R.id.update_chapter)
        private var mUpdateTime: TextView = view.findViewById(R.id.update_time)
        private var mBackLayout:ConstraintLayout = view.findViewById(R.id.back_layout)
        private var cover: ImageView = view.findViewById<ImageView>(R.id.cover)
        override fun onItemExpand(b: Boolean) {
            mDescriptorLayout.visibility = if (b) View.VISIBLE else View.GONE
        }

        override fun onAnimationStateChange(state: Int, willBeSelect: Boolean) {
            super.onAnimationStateChange(state, willBeSelect)
            if (state == RxCardStackView.ANIMATION_STATE_START && willBeSelect) {
                onItemExpand(true)
            }
            if (state == RxCardStackView.ANIMATION_STATE_END && !willBeSelect) {
                onItemExpand(false)
            }
        }


        fun onBind(bookRecommend: BookRecommend) {
            if(!bookRecommend.checkIsEmpty()){
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
                Thread {
                    GlideUtils.loadBlur(
                        this@RankingDetailActivity,
                        bookRecommend.bookCoverUrl,
                        mBackLayout
                    )
                }.start()
                mDescriptor.text = bookRecommend.bookDescriptor
                mBookName.text = bookRecommend.bookName
                mBookAuthor.text = bookRecommend.authorName
                mUpdateChapter.text = bookRecommend.newChapterName
                mUpdateTime.text = bookRecommend.updateTime
            } else {
                mPresenter?.getBookDetail(type,bookRecommend.bookUrl,object :RankingDetailPresenter.GetBookDetailListener{
                    override fun onBookDetailListener(bookRecommend: BookRecommend) {
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
                        Thread {
                            GlideUtils.loadBlur(
                                this@RankingDetailActivity,
                                bookRecommend.bookCoverUrl,
                                mBackLayout
                            )
                        }.start()
                        mDescriptor.text = bookRecommend.bookDescriptor
                        mBookName.text = bookRecommend.bookName
                        mBookAuthor.text = bookRecommend.authorName
                        mUpdateChapter.text = bookRecommend.newChapterName
                        mUpdateTime.text = bookRecommend.updateTime
                    }
                })
            }

        }


    }

    override fun onRanking(recommends: ArrayList<BookRecommend>) {
        runOnUiThread {
            mRankingAdapter.updateData(recommends)
        }
    }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }
}