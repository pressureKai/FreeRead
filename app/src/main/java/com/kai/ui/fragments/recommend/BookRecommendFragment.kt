package com.kai.ui.fragments.recommend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.kai.base.application.BaseInit
import com.kai.base.fragment.BaseMvpFragment
import com.kai.bookpage.model.BookRecommend
import com.kai.common.extension.getScreenHeight
import com.kai.common.extension.getScreenWidth
import com.kai.common.extension.measureView
import com.kai.common.utils.GlideUtils
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import com.kai.ui.main.MainActivity
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.adapter.OnPageChangeListenerAdapter
import com.zhpan.bannerview.constants.PageStyle
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.fragment_book_ranking.*
import kotlinx.android.synthetic.main.fragment_book_recommend.*
import kotlinx.android.synthetic.main.fragment_book_recommend.draw
import kotlinx.android.synthetic.main.fragment_book_recommend.list
import kotlinx.android.synthetic.main.fragment_book_recommend.multiply
import kotlinx.android.synthetic.main.fragment_book_recommend.search_layout
import kotlinx.android.synthetic.main.fragment_book_recommend.toolbar_layout
import kotlin.math.abs
import kotlin.math.ceil

class BookRecommendFragment : BaseMvpFragment<RecommendContract.View, RecommendPresenter>(),
    RecommendContract.View {
    private var indexBanner: BannerViewPager<BookRecommend, NetViewHolder>? = null
    private var loadFirst = false
    private var adapterMap: HashMap<Int, RecommendItemListAdapter> = HashMap()

    companion object {
        fun newInstance(): BookRecommendFragment {
            val bookRackFragment =
                BookRecommendFragment()
            val bundle = Bundle()
            bookRackFragment.arguments = bundle
            return bookRackFragment
        }
    }

    override fun createPresenter(): RecommendPresenter? {
        return RecommendPresenter()
    }

    override fun setLayoutId(): Int {
        return R.layout.fragment_book_recommend
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            toolbar_layout.post {
                val marginLayoutParams = toolbar_layout.layoutParams as MarginLayoutParams
                marginLayoutParams.topMargin =
                    ScreenUtils.getStatusBarHeight() + (toolbar_layout.height - ScreenUtils.getStatusBarHeight()) / 5
                toolbar_layout.layoutParams = marginLayoutParams
            }
        } catch (e: Exception) {
            LogUtils.e("BookRecommendFragment", e.toString())
        }


        activity?.let {
            indexBanner = view?.findViewById(R.id.index_banner)
            banner_layout.layoutParams.height = it.getScreenHeight() / 4
            appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                val alpha = if (verticalOffset != 0) {
                    abs(verticalOffset).toFloat() / (appBar.height - toolbar.height).toFloat()
                } else {
                    0f
                }
                toolbar.alpha = alpha
                toolbar_layout.alpha = alpha
                val minMargin = ScreenUtils.dpToPx(15)
                val maxWidth = (it.getScreenWidth() / 3) * 2
                val marginLayoutParams = search_layout.layoutParams as MarginLayoutParams
                marginLayoutParams.leftMargin = (minMargin + maxWidth * (1 - alpha)).toInt()
                search_layout.layoutParams = marginLayoutParams
            })

            list.layoutManager = LinearLayoutManager(it)
            val recommendListAdapter = RecommendListAdapter()
            recommendListAdapter.setHasStableIds(true)
            list.adapter = recommendListAdapter
            search_layout.setOnClickListener {
                ARouter.getInstance().build(BaseInit.SEARCH).navigation()
            }
            draw.setOnClickListener {
                (activity as MainActivity).openDrawer()
            }
        }


        refresh.setEnableAutoLoadMore(false)
        refresh.setRefreshHeader(ClassicsHeader(activity))
        refresh.setRefreshFooter(ClassicsFooter(activity))
        refresh.setOnRefreshListener {
            mPresenter?.getHomePage()
        }

    }

    override fun lazyInit(view: View, savedInstanceState: Bundle?) {
        if (!loadFirst) {
            mPresenter?.getHomePage()
            showMul(0)
        }

    }

    override fun initImmersionBar() {

    }


    private fun loadBanner(banners: ArrayList<BookRecommend>) {
        indexBanner?.let {
            if (banners.size > 0) {
                it.setCanLoop(true)
                    .setHolderCreator { NetViewHolder() }
                    .setPageStyle(PageStyle.MULTI_PAGE_OVERLAP)
                    .create(banners)
                Handler(Looper.getMainLooper()).postDelayed({
                    banner_layout.visibility = View.VISIBLE
                }, 100)
            } else {
                banner_layout.visibility = View.GONE
            }
        }
    }


    inner class NetViewHolder : ViewHolder<BookRecommend> {
        override fun getLayoutId(): Int {
            return R.layout.item_index_banner
        }

        override fun onBind(itemView: View, data: BookRecommend, position: Int, size: Int) {
            val imageView = itemView.findViewById<ImageView>(R.id.imageView)
            val layout = itemView.findViewById<ImageView>(R.id.image_layout)
            activity?.let {
                Thread {
                    GlideUtils.loadBlur(it, data.bookCoverUrl, layout)
                }.start()
            }
            val placeholder = RequestOptions()
                .error(R.drawable.default_loading)
                .placeholder(R.drawable.default_loading)
            Glide.with(imageView).load(data.bookCoverUrl).apply(placeholder).into(imageView)

            itemView.setOnClickListener {
                ARouter.getInstance()
                    .build(BaseInit.BOOKINFO)
                    .withString("url", data.bookUrl)
                    .navigation()
            }
        }
    }

    inner class RecommendListAdapter :
        BaseQuickAdapter<ArrayList<BookRecommend>, BaseViewHolder>(R.layout.item_recyclerview_recommend_list) {
        override fun convert(holder: BaseViewHolder, item: ArrayList<BookRecommend>) {
            val typeImage = holder.getView<ImageView>(R.id.type_image)
            val typeName = holder.getView<TextView>(R.id.type_name)
            val itemList = holder.getView<RecyclerView>(R.id.recommend_item_list)
            var name = "玄幻"
            var count = 2
            if (item.isNotEmpty()) {
                val bookType = item.first().bookType
                val b = bookType % 2 == 0
                when (bookType) {
                    BookRecommend.GAME_RECOMMEND -> {
                        name = "网游小说"
                        typeImage.setImageResource(R.drawable.game)
                    }
                    BookRecommend.FANTASY_RECOMMEND -> {
                        name = "玄幻小说"
                        typeImage.setImageResource(R.drawable.fantasy)
                    }
                    BookRecommend.COMPREHENSION_RECOMMEND -> {
                        name = "修真小说"
                        typeImage.setImageResource(R.drawable.comprehension)
                    }
                    BookRecommend.CITY_RECOMMEND -> {
                        name = "城市小说"
                        typeImage.setImageResource(R.drawable.city)
                    }
                    BookRecommend.HISTORY_RECOMMEND -> {
                        name = "历史小说"
                        typeImage.setImageResource(R.drawable.history)
                    }
                    BookRecommend.SCIENCE_RECOMMEND -> {
                        name = "科幻小说"
                        typeImage.setImageResource(R.drawable.science)
                    }
                }

                typeName.text = name


                if (b) {
                    count = 3
                }
                val gridLayoutManager = GridLayoutManager(context, count)
                if (data.size != 0) {
                    try {
                        gridLayoutManager.spanCount = ceil((data.size / count).toDouble()).toInt()
                    } catch (e: Exception) {
                        LogUtils.e("RecommendListAdapter", "error is $e")
                    }
                }

                gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
                itemList.layoutManager = gridLayoutManager

                var recommendItemListAdapter: RecommendItemListAdapter? = null
                if (adapterMap.size > 0 && item.size > 0 && adapterMap[item.first().bookType] != null) {
                    recommendItemListAdapter = adapterMap[item.first().bookType]
                } else {
                    recommendItemListAdapter = RecommendItemListAdapter()
                    recommendItemListAdapter?.setHasStableIds(true)
                    recommendItemListAdapter?.setOnItemClickListener { _, view, i ->
                        try {
                            val bookRecommend = item[i]
                            ARouter.getInstance()
                                .build(BaseInit.BOOKINFO)
                                .withString("url", bookRecommend.bookUrl)
                                .navigation()
                        } catch (e: java.lang.Exception) {

                        }
                    }
                }


                itemList.adapter = recommendItemListAdapter
                try {
                    if (item.size > 0) {
                        adapterMap[item.first().bookType] = recommendItemListAdapter!!
                    }
                } catch (e: java.lang.Exception) {

                }
                recommendItemListAdapter?.setNewInstance(item)
            }
        }

        override fun getItemId(position: Int): Long {
            var id = position.toLong()
            try {
                val item = getItem(position)
                id = item.first().bookType.toLong()
            } catch (e: java.lang.Exception) {

            }
            return id
        }
    }


    inner class RecommendItemListAdapter :
        BaseQuickAdapter<BookRecommend, BaseViewHolder>(R.layout.item_recyclerview_recommend_item) {
        override fun convert(holder: BaseViewHolder, item: BookRecommend) {
            val cover = holder.getView<ImageView>(R.id.cover)
            val name = holder.getView<TextView>(R.id.book_name)
            val descriptor = holder.getView<TextView>(R.id.book_descriptor_item)
            val layout = holder.getView<ConstraintLayout>(R.id.layout)
            val layoutName = holder.getView<ConstraintLayout>(R.id.layout_name)
            val nameLayout = holder.getView<LinearLayout>(R.id.name_layout)
            var coverWidth = 0
            activity?.let {
                val width = (it.getScreenWidth()) / 2 + name.textSize.toInt() * 2
                layout.layoutParams.width = width
                coverWidth = (width / 2) - ScreenUtils.dpToPx(16)
                cover.layoutParams.width = coverWidth
                cover.layoutParams.height = (coverWidth / 0.75).toInt()
                layout.layoutParams.height = (coverWidth / 0.75).toInt()
                name.layoutParams.width = width / 2
                descriptor.layoutParams.width = width / 2
                nameLayout.layoutParams.height = (coverWidth / 0.75).toInt()
           }
            mPresenter?.let {
                val localBookDetail = it.localBookDetail(item.bookUrl)
                if (localBookDetail == null || localBookDetail.checkIsEmpty()) {
                    it.bookDetail(item, object : RecommendPresenter.OnBookDetail {
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
                                            .skipMemoryCache(false)
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
                            .skipMemoryCache(false)
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

    override fun onBanner(arrayList: ArrayList<BookRecommend>) {
        loadBanner(arrayList)
    }

    override fun onRecommend(arrayList: ArrayList<ArrayList<BookRecommend>>) {
        showMul(2)
        loadFirst = true
        val recommendListAdapter = list.adapter as RecommendListAdapter
        recommendListAdapter.setNewInstance(arrayList)
        recommendListAdapter.notifyDataSetChanged()
        refresh.finishRefresh(300)
    }


    /**
     * @param state 0:loading 1:empty 2: content
     */
    private fun showMul(state: Int) {
        var layout = R.layout.layout_empty
        when (state) {
            0 -> {
                layout = R.layout.layout_loading
            }
            1 -> {
                layout = R.layout.layout_empty
            }
        }
        if (state != 2) {
            val inflate = View.inflate(activity, layout, null)
            multiply.showEmpty(
                inflate, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            )
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                multiply.showContent()
            }, 200)
        }

    }
}