package com.kai.ui.fragments.shelf

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.fragment.BaseMvpFragment
import com.kai.bookpage.model.BookRecommend
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import com.kai.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_book_shelf.*

class BookShelfFragment:BaseMvpFragment<ShelfContract.View,ShelfPresenter>(),ShelfContract.View{
    companion object{
        fun newInstance(): BookShelfFragment {
            val bookRackFragment =
                BookShelfFragment()
            val bundle = Bundle()
            bookRackFragment.arguments = bundle
            return bookRackFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            toolbar_layout.post {
                val marginLayoutParams = toolbar_layout.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.topMargin =
                    ScreenUtils.getStatusBarHeight() + (toolbar_layout.height - ScreenUtils.getStatusBarHeight()) / 5
                toolbar_layout.layoutParams = marginLayoutParams
            }
        } catch (e: Exception) {
            LogUtils.e("BookRecommendFragment", e.toString())
        }


        search_layout.setOnClickListener {
            ARouter.getInstance().build("/app/search").navigation()
        }
        draw.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }
        val bookAdapter = BookAdapter()
        bookAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
            if(baseQuickAdapter.data.size - 1 == i){
                ARouter.getInstance().build("/app/search").navigation()
            } else {
                val bookRecommend = baseQuickAdapter.data[i] as BookRecommend
                val searchBook = bookRecommend.toSearchBook()
                val url = searchBook.sources.first().link
                ARouter.getInstance()
                    .build("/app/bookinfo")
                    .withString("url", url)
                    .navigation()
            }
        }
        activity?.let {
            list.layoutManager = LinearLayoutManager(it)
            bookAdapter.setHasStableIds(true)
            list.adapter = bookAdapter
        }


        mPresenter?.shelf()


    }
    override fun createPresenter(): ShelfPresenter? {
       return ShelfPresenter()
    }

    override fun setLayoutId(): Int {
        return R.layout.fragment_book_shelf
    }

    override fun lazyInit(view: View, savedInstanceState: Bundle?) {
    }

    override fun initImmersionBar() {

    }


    inner class BookAdapter:BaseQuickAdapter<BookRecommend,BaseViewHolder>(R.layout.item_recyclerview_shelf){
        override fun convert(holder: BaseViewHolder, item: BookRecommend) {
            try {
                val contentLayout = holder.getView<LinearLayout>(R.id.content_layout)
                val bookName = holder.getView<TextView>(R.id.book_name)
                val bookAuthor = holder.getView<TextView>(R.id.book_author)
                val cover = holder.getView<ImageView>(R.id.cover)
                val addLayout = holder.getView<ConstraintLayout>(R.id.add_layout)


                if((getItemPosition(item) == data.size - 1)){
                    addLayout.visibility = View.VISIBLE
                    contentLayout.visibility = View.INVISIBLE
                } else {
                    contentLayout.visibility = View.VISIBLE
                    addLayout.visibility = View.INVISIBLE
                    Glide.with(cover).load(item.bookCoverUrl).into(cover)
                    bookName.text = item.bookName
                    bookAuthor.text = item.authorName
                }
            }catch (e:java.lang.Exception){
                LogUtils.e("BookShelfFragment","error is $e")
            }



        }

        override fun getItemId(position: Int): Long {
            var id = position.toLong()
            try {
                val item = getItem(position)
                id =  item.bookUrl.hashCode().toLong()
            }catch (e:java.lang.Exception){

            }
            return id
        }


    }

    override fun onShelf(recommends: ArrayList<BookRecommend>) {
            recommends.add(BookRecommend())
            (list.adapter as BookAdapter).setNewInstance(recommends)
            (list.adapter as BookAdapter).notifyDataSetChanged()
        }

}