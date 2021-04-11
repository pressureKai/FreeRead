package com.kai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User")
class User {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id",typeAffinity = ColumnInfo.INTEGER)
    var id: Int = 0


    @ColumnInfo(name = "account",typeAffinity = ColumnInfo.TEXT)
    var account: String = ""



    @ColumnInfo(name = "password",typeAffinity = ColumnInfo.TEXT)
    var password: String = ""


    @ColumnInfo(name = "question",typeAffinity = ColumnInfo.TEXT)
    var question: String = ""



    @ColumnInfo(name = "answer",typeAffinity = ColumnInfo.TEXT)
    var answer: String = ""


    @ColumnInfo(name = "onLine")
    var onLine = false

}