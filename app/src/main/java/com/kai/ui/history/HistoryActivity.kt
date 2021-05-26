package com.kai.ui.history

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.kai.base.application.BaseInit
import com.kai.bookpage.model.BookRecommend
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.merge_toolbar.*
import java.lang.Exception

@Route(path = BaseInit.HISTORY)
class HistoryActivity : BaseMvpActivity<HistoryContract.View, HistoryPresenter>(),
    HistoryContract.View {
    companion object {
        const val LIKE = 0
        const val READ = 1
    }

    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )

    override fun initView() {
        initImmersionBar(view = toolbar, fitSystem = false)
        val intExtra = intent.getIntExtra("type", 0)
        val searchAdapter = HistoryAdapter()
        searchAdapter.setOnItemClickListener { adapter, _, position ->
            try {
                val bookRecommend = adapter.data[position] as BookRecommend
                val searchBook = bookRecommend.toSearchBook()
                val url = searchBook.sources.first().link
                ARouter.getInstance()
                    .build(BaseInit.BOOKINFO)
                    .withString("url", url)
                    .navigation()
            } catch (e: Exception) {

            }
        }


        back.setOnClickListener {
            finish()
        }

        toolbar_title.text = if(intExtra == LIKE) resources.getString(R.string.like) else resources.getString(R.string.read_history)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = searchAdapter
        showEmpty()
        mPresenter?.getHistoryByType(intExtra)
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_history
    }

    override fun onHistory(datas: ArrayList<BookRecommend>) {
        if (datas.size > 0) {
            (list.adapter as HistoryAdapter).setNewInstance(datas)
            (list.adapter as HistoryAdapter).notifyDataSetChanged()
            multiply.showContent()
        } else {
            showEmpty()
        }
    }

    override fun createPresenter(): HistoryPresenter? {
        return HistoryPresenter()
    }


    inner class HistoryAdapter :
        BaseQuickAdapter<BookRecommend, BaseViewHolder>(R.layout.item_main_test) {
        override fun convert(holder: BaseViewHolder, item: BookRecommend) {
            holder.getView<TextView>(R.id.title).text = item.bookName.replace(" ", "").trim()
            holder.getView<TextView>(R.id.des).text = item.bookDescriptor.replace(" ", "").trim()
            holder.getView<TextView>(R.id.author).text = item.authorName.replace(" ", "").trim()
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
            Glide.with(cover).load(item.bookCoverUrl).apply(options).into(cover)
        }
    }

    private fun showEmpty() {
        val inflate = View.inflate(this, R.layout.layout_empty, null)
        multiply.showEmpty(inflate, layoutParams)
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}