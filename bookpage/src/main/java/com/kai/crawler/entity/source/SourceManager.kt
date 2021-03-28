package com.kai.crawler.entity.source

import android.util.SparseArray
import android.util.SparseBooleanArray
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kai.common.application.BaseApplication
import com.kai.common.utils.AssetsUtils
import com.kai.common.utils.SharedPreferenceUtils
import java.util.*

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:32
 */
class SourceManager {
   companion object{
      val CONFIGS: SparseArray<SourceConfig> = SparseArray()

      /**
       * 初始化所有书源
       */
      val SOURCES: SparseArray<Source> = object : SparseArray<Source>() {
         init {
            put(
               SourceID.LIEWEN,
               Source(SourceID.LIEWEN, "猎文网", "https://www.liewen.cc/search.php?keyword=%s")
            )
         }
      }
      init {
         init()
      }


      private fun init(){
         CONFIGS.clear()
      }

      /**
       * 保存可用状态的书源
       */
      fun saveSourceEnable(booleanArray: SparseBooleanArray?) {
         if (booleanArray != null) {
            val list: MutableList<SourceEnable> = ArrayList()
            for (i in 0 until booleanArray.size()) {
               val key = booleanArray.keyAt(i)
               val value = booleanArray.valueAt(i)
               val sourceEnable = SourceEnable(key, value)
               list.add(sourceEnable)
            }
            SharedPreferenceUtils.getInstance()?.putString(
               "source_setting_list",
               Gson().toJson(list)
            )
         }
      }


      /**
       * 获取可用状态的书源
       */
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