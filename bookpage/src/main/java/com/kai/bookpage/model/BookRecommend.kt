package com.kai.bookpage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kai.bookpage.model.database.BookDatabase
import org.jsoup.select.Evaluator

@Entity(tableName = "bookRecommend")
class BookRecommend {
    companion object {
        const val INDEX_RECOMMEND = 0
        //玄幻
        const val FANTASY_RECOMMEND = 1
        //修真
        const val COMPREHENSION_RECOMMEND = 2
        //都市
        const val CITY_RECOMMEND = 3
        //历史
        const val HISTORY_RECOMMEND = 4
        //网游
        const val GAME_RECOMMEND = 5
        //科幻
        const val SCIENCE_RECOMMEND = 6
        //言情
        const val ROMANS_RECOMMEND = 7
        //全本
        const val ALLBOOK_RECOMMEND = 8
        //其他
        const val ORTHER_RECOMMEND = 9

        val types = arrayListOf(
            FANTASY_RECOMMEND,
            COMPREHENSION_RECOMMEND,
            CITY_RECOMMEND,
            HISTORY_RECOMMEND,
            GAME_RECOMMEND,
            SCIENCE_RECOMMEND,
            ROMANS_RECOMMEND,
            ALLBOOK_RECOMMEND,
            ORTHER_RECOMMEND)


        fun typeToName(type: Int):String{
            var typeName = "首页"
            when(type){
                FANTASY_RECOMMEND ->{
                    typeName = "玄幻"
                }
                COMPREHENSION_RECOMMEND ->{
                    typeName = "修真"
                }
                CITY_RECOMMEND ->{
                    typeName = "都市"
                }
                HISTORY_RECOMMEND ->{
                    typeName = "历史"
                }
                GAME_RECOMMEND ->{
                    typeName = "网游"
                }
                SCIENCE_RECOMMEND ->{
                    typeName = "科幻"
                }
                ROMANS_RECOMMEND ->{
                    typeName = "言情"
                }
                ALLBOOK_RECOMMEND ->{
                    typeName = "全本"
                }
                ORTHER_RECOMMEND ->{
                    typeName = "其他"
                }
            }
            return typeName
        }
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recommendId",typeAffinity = ColumnInfo.INTEGER)
    var recommendId = 0
    var bookName = ""
    var bookUrl = ""
    var bookCoverUrl = ""
    var bookDescriptor = ""
    var bookType = 0
    var authorName = ""
    var newChapterUrl = ""
    var newChapterName = ""
    var updateTime = ""
    var isRanking = false



    fun checkIsEmpty(): Boolean{
        return bookName.isEmpty()
                || bookCoverUrl.isEmpty()
                || bookDescriptor.isEmpty()
                || authorName.isEmpty()
                || newChapterUrl.isEmpty()
                || newChapterName.isEmpty()
    }


    fun save(){
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        if(bookRecommendByBookUrl == null || (bookRecommendByBookUrl!=null
                    && !bookRecommendByBookUrl.checkIsEmpty()
                    && checkIsEmpty())){
            BookDatabase.get().bookDao().insertBookRecommend(this)
        }
    }

}