package com.kai.ui.search

import android.content.Context
import android.text.Editable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.widget.load.ChargeLoadMoreListener
import com.kai.base.widget.load.ListPageLoader
import com.kai.base.widget.load.RefreshDataListener
import com.kai.common.listener.CustomTextWatcher
import com.kai.common.utils.LogUtils
import com.kai.crawler.entity.book.SearchBook
import com.kai.entity.SearchHistory
import com.kai.ui.bookdetail.BookDetailActivity
import com.kai.util.DialogHelper
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.recycler
import kotlinx.android.synthetic.main.activity_search.refresh
import kotlinx.android.synthetic.main.activity_search.status
import kotlinx.android.synthetic.main.dialog_loading.*
import java.lang.Exception


@Route(path = "/app/search")
class SearchActivity: BaseMvpActivity<SearchContract.View, SearchPresenter>(),
    SearchContract.View, RefreshDataListener, ChargeLoadMoreListener {
    private lateinit var listPageLoader: ListPageLoader<SearchBook>
    override fun initView() {
        initImmersionBar(fitSystem = false, color = R.color.app_background,view = search_edit_layout)
        back.setOnClickListener {
            finish()
        }

        search_edit.addTextChangedListener(object : CustomTextWatcher(){
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                val searchString = s.toString()
                if(searchString.isNotEmpty()){
                    listPageLoader.setLoadState(ListPageLoader.SMART_LOAD_REFRESH)
                    mPresenter?.search(searchString)
                    DialogHelper.instance?.showLoadingDialog(activity = this@SearchActivity)
                }
            }
        })


        val searchAdapter = SearchAdapter()
        searchAdapter.setOnItemClickListener { adapter, _, position ->
            try {
                val searchBook = adapter.data[position] as SearchBook
                val url = searchBook.sources.first().link
                ARouter.getInstance()
                    .build("/app/bookinfo")
                    .withString("url", url)
                    .navigation()
            }catch (e:Exception){

            }
        }
        listPageLoader = ListPageLoader(
            recycler,
            refreshDataDelegate = this,
            chargeLoadMoreListener = this,
            mSmartRefreshLayout = refresh,
            mMultipleStatusView = status,
            mAdapter = searchAdapter
        )

        history_list.layoutManager = LinearLayoutManager(this)
        history_list.adapter = HistoryAdapter()
        mPresenter?.recommend()
        mPresenter?.history()



        delete.setOnClickListener {
            DialogHelper.instance?.showRemindDialog(activity = this,resources.getString(R.string.delete_all_history),remindDialogClickListener = object :DialogHelper.RemindDialogClickListener{
                override fun onRemindDialogClickListener(positive: Boolean) {
                    if(positive){
                        mPresenter?.deleteAll()
                    }
                    DialogHelper.instance?.hintRemindDialog()
                }
            })

        }
    }


    override fun createPresenter(): SearchPresenter? {
        return SearchPresenter()
    }

    override fun setLayoutId(): Int {
       return R.layout.activity_search
    }

    override fun onSearch(searchBooks: ArrayList<SearchBook>) {
        descriptor_layout.visibility = View.GONE
        refresh_content.visibility = View.VISIBLE
        listPageLoader.loadData(searchBooks)
        DialogHelper.instance?.hintLoadingDialog()
    }

    override fun onRecommend(recommends: ArrayList<SearchHistory>) {
        if(recommends.isNotEmpty()){
            recommend_layout.visibility = View.VISIBLE
        }
        addFlow(recommends)
    }

    override fun onHistory(historys: ArrayList<SearchHistory>) {
        (history_list.adapter as HistoryAdapter).setNewInstance(historys)
        (history_list.adapter as HistoryAdapter).notifyDataSetChanged()
        if(historys.size > 0){
            delete.visibility =View.VISIBLE
        } else {
            delete.visibility = View.INVISIBLE
        }
    }

    override fun onDelete() {
        mPresenter?.history()
    }

    private fun addFlow(recommends: ArrayList<SearchHistory>){
        recommend.removeAllViews()
        for(value in recommends){
            val inflate = View.inflate(this, R.layout.item_flow_recommend, null)
            val recommendTextView = inflate.findViewById<TextView>(R.id.recommend)
            recommendTextView.text = value.searchName
            recommendTextView.setOnClickListener {
                val textView = it as TextView
                search_edit.setText(textView.text.toString())
            }
            recommend.addView(inflate)
        }

    }

    override fun onLoadMore(pageIndex: Int, pageSize: Int) {

    }

    override fun onRefresh() {
        val toString = search_edit.text.toString()
        if(toString.isNotEmpty()){
            mPresenter?.search(toString)
        } else {
            listPageLoader?.finishAll()
        }
    }

    override fun couldLoadMore(pageIndex: Int, totalPage: Int): Boolean {
        return false
    }

    inner class SearchAdapter :
        BaseQuickAdapter<SearchBook, BaseViewHolder>(R.layout.item_main_test) {
        override fun convert(holder: BaseViewHolder, item: SearchBook) {
            holder.getView<TextView>(R.id.title).text = item.title.replace(" ", "").trim()
            holder.getView<TextView>(R.id.des).text = item.descriptor.replace(" ", "").trim()
            holder.getView<TextView>(R.id.author).text = item.author.replace(" ", "").trim()
            val view = holder.getView<View>(R.id.divider)

            val itemPosition = getItemPosition(item)
            if (itemPosition == data.size - 1) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
            }

            val options = RequestOptions()
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(16)))
                .diskCacheStrategy(DiskCacheStrategy.ALL);
            val cover = holder.getView<ImageView>(R.id.cover)
            Glide.with(cover).load(item.cover).apply(options).into(cover)
        }
    }



    inner class HistoryAdapter:
        BaseQuickAdapter<SearchHistory, BaseViewHolder>(R.layout.item_recyclerview_history) {
        override fun convert(holder: BaseViewHolder, item: SearchHistory) {
            val view = holder.getView<TextView>(R.id.history_name)

            val delete = holder.getView<ImageView>(R.id.delete)
            view.text = item.searchName
            view.setOnClickListener {
                val textView = it as TextView
                search_edit.setText(textView.text.toString())
            }

            delete.setOnClickListener {
                mPresenter?.deleteByName(item.searchName)
            }
        }
    }


    override fun onBackPressed() {
        if(descriptor_layout.visibility == View.GONE){
            refresh_content.visibility = View.GONE
            descriptor_layout.visibility = View.VISIBLE
            mPresenter?.history()
        } else {
            super.onBackPressed()
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