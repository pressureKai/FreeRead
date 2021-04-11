package com.kai.database
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kai.common.application.BaseApplication
import com.kai.dao.UserDao
import com.kai.entity.User

@Database(entities = [User::class],version = 3)
abstract class CustomDatabase : RoomDatabase() {
    companion object{
        private const val DATABASE_NAME = "app"
        private val migration: Migration =object: Migration(2,3){
            override fun migrate(database: SupportSQLiteDatabase) {

                // database.execSQL("ALTER TABLE users ADD COLUMN last_update INTEGER");
                // 修改表名为 users 的表 增加一个新的字段 名为 last_update 数据类型为 INTEGER
            }
        }
        private var instance: CustomDatabase ?= null
        get(){
            if(field == null){
               field = Room.databaseBuilder(BaseApplication.getContext()!!,
                   CustomDatabase::class.java,
                   DATABASE_NAME)
                   .allowMainThreadQueries()
                   .addMigrations(migration)
                   .fallbackToDestructiveMigration()
                   .build()
            }
            return field
        }


        @Synchronized
        fun get(): CustomDatabase{
           return instance!!
        }
    }

    abstract fun userDao(): UserDao
}