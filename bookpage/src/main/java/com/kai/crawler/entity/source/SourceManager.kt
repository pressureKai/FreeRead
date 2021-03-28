package com.kai.crawler.entity.source

import android.util.SparseArray
import android.util.SparseBooleanArray
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kai.common.application.BaseApplication
import com.kai.common.utils.AssetsUtils
import com.kai.common.utils.SharedPreferenceUtils

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


      fun getSourceEnableSparseArray(): SparseBooleanArray {
         val json = SharedPreferenceUtils.getInstance()?.getString(
            "source_setting_list",
            AssetsUtils.readAssetsTxt(BaseApplication.getContext()!!, "SourceEnable.json")
         )

         val enables = Gson().fromJson<List<SourceEnable>>(
            json,
            object : TypeToken<List<SourceEnable?>?>() {}.type
         )

         val checkedMap = SparseBooleanArray()
         for (sourceEnable in enables) {
            checkedMap.put(sourceEnable.id, sourceEnable.enable)
         }
         return checkedMap
      }
   }
}