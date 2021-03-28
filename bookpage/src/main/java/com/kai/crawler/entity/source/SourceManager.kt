package com.kai.crawler.entity.source

import android.util.SparseArray

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:32
 */
class SourceManager {
   companion object{
      var CONFIGS: SparseArray<SourceConfig> = SparseArray()

      init {
         init()
      }


      private fun init(){
         CONFIGS.clear()
      }
   }




}