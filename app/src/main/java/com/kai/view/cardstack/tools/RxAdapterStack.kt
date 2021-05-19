package com.kai.view.cardstack.tools

import android.content.Context
import android.view.LayoutInflater
import com.kai.view.cardstack.RxCardStackView
import java.util.*

/**
 * @author vondear
 * @date 2018/6/11 11:36:40 整合修改
 */
abstract class RxAdapterStack<T>(val context: Context) :
    RxCardStackView.Adapter<RxCardStackView.ViewHolder?>() {
    val layoutInflater: LayoutInflater
    private val mData: MutableList<T>
    fun updateData(data: List<T>?) {
        setData(data)
        notifyDataSetChanged()
    }

    fun setData(data: List<T>?) {
        mData.clear()
        if (data != null) {
            mData.addAll(data)
        }
    }

    override fun onBindViewHolder(holder: RxCardStackView.ViewHolder?, position: Int) {
        val data = getItem(position)
        bindView(data, position, holder)
    }



    abstract fun bindView(data: T, position: Int, holder: RxCardStackView.ViewHolder?)
    fun getCount(): Int {
        return mData.size
    }



    fun getItem(position: Int): T {
        return mData[position]
    }

    init {
        layoutInflater = LayoutInflater.from(context)
        mData = ArrayList<T>()
    }
}