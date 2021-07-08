package com.wyc.cloudapp.data.room;

import android.content.Context;
import android.os.Environment;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.dao.PayMethodDao;
import com.wyc.cloudapp.data.room.entity.PayMethod;

@Database(entities = {PayMethod.class},version = SQLiteHelper.DATABASE_VERSION)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PayMethodDao PayMethodDao();

    private static AppDatabase DB;
    public static AppDatabase getInstance(){
        if (DB == null){
            synchronized (AppDatabase.class){
                if (DB == null){
                    DB = Room.databaseBuilder(CustomApplication.self(),AppDatabase.class, SQLiteHelper.DATABASE_NAME(CustomApplication.self().getStoreId()))
                            .addMigrations()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return DB;
    }
    public static void closeDB(){
        if (DB != null){
            DB.getOpenHelper().close();;
            DB = null;
        }
    }
}