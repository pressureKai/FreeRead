package com.kai.ui.fragments.ranking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.fragment.BaseMvpFragment
import com.kai.bookpage.model.BookRecommend
import com.kai.common.extension.getScreenWidth
import com.kai.common.extension.measureView
import com.kai.common.utils.GlideUtils
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import kotlinx.android.synthetic.main.fragment_book_ranking.*
import java.lang.Exception
import java.lang.ref.WeakReference

class BookRankingFragment : BaseMvpFragment<RankingContract.View, RankingPresenter>(),
    RankingContract.View {
    companion object {
        fun newInstance(): BookRankingFragment {
            val bookRackFragment =
                BookRankingFragment()
            val bundle = Bundle()
            bookRackFragment.arguments = bundle
            return bookRackFragment
        }
    }

    var map: HashMap<Int, String> = HashMap()
    override fun createPresenter(): RankingPresenter? {
        return RankingPresenter()
    }

    override fun setLayoutId(): Int {
        return R.layout.fragment_book_ranking
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridLayoutManager = GridLayoutManager(activity, 2)
        val layoutParams = list.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = ScreenUtils.getStatusBarHeight()
        list.layoutParams = layoutParams
        list.layoutManager = gridLayoutManager
        val rankingAdapter = RankingAdapter()
        rankingAdapter.setHasStableIds(true)
        list.adapter = rankingAdapter
        mPresenter?.ranking()
    }

    override fun lazyInit(view: View, savedInstanceState: Bundle?) {
    }

    override fun initImmersionBar() {

    }

    override fun onRanking(hashMap: HashMap<Int, String>) {
        val keys = hashMap.keys
        val arrayList = ArrayList<Int>()
        for (value in keys) {
            if (value != BookRecommend.INDEX_RECOMMEND && value > 0) {
                arrayList.add(value)
            }
        }
        map = hashMap
        (list.adapter as RankingAdapter).setNewInstance(arrayList)
    }

    inner class RankingAdapter :
        BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_recyclerview_ranking) {
        override fun convert(holder: BaseViewHolder, item: Int) {
            val view = holder.getView<ConstraintLayout>(R.id.layout)
            val cover = holder.getView<ImageView>(R.id.cover)
            val typeName = holder.getView<TextView>(R.id.type_name)
            val typeLayout = holder.getView<ConstraintLayout>(R.id.type_layout)
            val back = holder.getView<View>(R.id.back)

            var topMargin = 0
            topMargin = if (item % 2 == 0) {
                ScreenUtils.getStatusBarHeight()
            } else {
                0
            }

            val marginLayoutParams = cover.layoutParams as ViewGroup.MarginLayoutParams
            marginLayoutParams.topMargin = topMargin
            map[item]?.let {
                mPresenter?.getCover(item, it, object : RankingPresenter.GetCoverListener {
                    override fun onCover(path: String) {
                        Handler(Looper.getMainLooper()).post {
                            GlideUtils.loadCornersTop(
                                WeakReference(activity),
                                path,
                                cover,
                                8,
                                object : GlideUtils.ResourceWidthAndHeightListener {
                                    override fun resourceWidthAndHeight(width: Int, height: Int) {
                                        try {
                                            val fl = width.toFloat() / height.toFloat()
                                            val height1 = cover.layoutParams.height
                                            typeLayout.layoutParams.width =
                                                (fl * height1).toInt() + ScreenUtils.dpToPx(1)
                                            typeLayout.layoutParams.height =  typeName.measureView()[1]
                                            back.layoutParams.height = typeName.measureView()[1]
                                            back.alpha = 0.85f
                                        }catch (e:Exception){
                                            LogUtils.e("BookRankingFragment","error is $e")
                                        }
                                    }
                                })
                        }
                    }
                })


                view.setOnClickListener { _ ->
                    ARouter.getInstance()
                        .build("/app/ranking")
                        .withInt("type", item)
                        .withString("url", it)
                        .navigation()
                }
            }

            typeName.text = BookRecommend.typeToName(item)
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).toLong()
        }
    }
}