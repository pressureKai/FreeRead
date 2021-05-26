package com.kai.bookpage.model.dao

import androidx.room.*
import com.kai.bookpage.model.*
import java.util.stream.LongStream


@Dao
interface BookDao {
    @Insert
    fun insertCoolBook(coolBookBean: CoolBookBean)

    @Delete
    fun deleteCoolBook(coolBookBean: CoolBookBean)

    @Query("SELECT * FROM coolBook WHERE bookId = :id")
    fun getCoolBookById(id: Int):CoolBookBean


    @Query("SELECT * FROM coolBook WHERE isLocal = :isLocal")
    fun getCoolBookByIsLocal(isLocal: Boolean):List<CoolBookBean>

    @Update
    fun updateCoolBook(coolBookBean: CoolBookBean)


    @Query("SELECT * FROM coolBook")
    fun getCoolBookList(): List<CoolBookBean>

    @Insert
    fun insertBookRecord(bookRecordBean: BookRecordBean)

    @Update
    fun updateBookRecord(bookRecordBean: BookRecordBean)

    @Delete
    fun deleteBookRecord(bookRecordBean: BookRecordBean)

    @Query("SELECT * FROM bookRecord WHERE bookId = :bookId")
    fun getBookRecord(bookId: String): BookRecordBean


    @Insert
    fun insertBookChapter(bookChapterBean: BookChapterBean)

    @Query("SELECT * FROM bookChapter WHERE id = :id")
    fun getBookChapterById(id: Int): BookChapterBean


    @Update
    fun updateBookChapter(bookChapterBean: BookChapterBean)


    @Delete
    fun deleteBookChapter(bookChapterBean: BookChapterBean)


    @Query("SELECT * FROM coolBook WHERE bookId = :bookId")
    fun getChapterList(bookId: String): BookChapterListBean

    @Query("SELECT * FROM bookRecommend WHERE bookType = :bookType")
    fun getBookRecommendByType(bookType:Int):List<BookRecommend>


    @Query("SELECT * FROM bookRecommend WHERE isLocal = :isLocal")
    fun getBookRecommendByLocal(isLocal:Boolean):List<BookRecommend>


    @Insert
    fun insertBookRecommend(bookRecommend: BookRecommend)

    @Update
    fun updateBookRecommend(bookRecommend: BookRecommend)

    @Delete
    fun deleteBookRecommend(bookRecommend: BookRecommend)

    @Query("SELECT * FROM bookRecommend WHERE bookUrl = :bookUrl")
    fun getBookRecommendByBookUrl(bookUrl:String):BookRecommend


    @Query("SELECT * FROM bookRecommend WHERE isRead = :isRead")
    fun getBookRecommendByBookRead(isRead:Boolean):List<BookRecommend>

    @Query("SELECT * FROM bookRecommend WHERE bookType = :type AND isRanking = :isRanking")
    fun getRankingBookRecommendByType(type:Int,isRanking:Boolean):List<BookRecommend>

}
