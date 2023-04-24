package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 2023-02-15
 * pureum
 */

@Entity(tableName = "MyDataTable")
data class MyEntity(
    @PrimaryKey
    val date : String,
    @ColumnInfo
    val cardName: String,
    @ColumnInfo
    val amount: String,
    @ColumnInfo
    val pictureName: String,
    @ColumnInfo
    val picture: String,
)

//fun MyEntity.toDomainEntity():DomainRoomData = DomainRoomData(
//    date = date,
//    cardName = cardName,
//    amount = amount,
//    storeName = pictureName,
//    file = picture
//)