package com.kai.bookpage.model.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.BookRecordBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.dao.BookDao
import com.kai.common.application.BaseApplication

@Database(
    entities = [
        BookChapterBean::class,
        CoolBookBean::class,
        BookRecordBean::class,
        BookRecommend::class,
    ], version = 5
)
abstract class BookDatabase : RoomDatabase() {
    companion object {
        private const val DATABASE_NAME = "app_book"
        private val migration: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                // database.execSQL("ALTER TABLE users ADD COLUMN last_update INTEGER");
                // 修改表名为 users 的表 增加一个新的字段 名为 last_update 数据类型为 INTEGER
            }
        }
        private var instance: BookDatabase? = null
            get() {
                if (field == null) {
                    field = Room.databaseBuilder(
                        BaseApplication.getContext()!!,
                        BookDatabase::class.java,
                        DATABASE_NAME
                    )
                        .allowMainThreadQueries()
                        .addMigrations(migration)
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return field
            }


        @Synchronized
        fun get(): BookDatabase {
            return instance!!
        }
    }

    abstract fun bookDao(): BookDao
}