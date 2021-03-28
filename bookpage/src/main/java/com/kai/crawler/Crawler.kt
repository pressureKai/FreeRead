package com.kai.crawler

import android.util.SparseBooleanArray
import com.kai.crawler.entity.source.SourceManager

class Crawler {
    companion object{
        const val TAG = "Crawler"
        fun search(keyword: String){
            val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
            for(i in 0.until(checkedMap.size()) ){
                val id = SourceManager.CONFIGS.keyAt(i)
                val config = SourceManager.CONFIGS.valueAt(i)
                val source = SourceManager.SOURCES.get(id)
                if(!checkedMap.get(id)){
                    continue
                }

            }
        }
    }
}