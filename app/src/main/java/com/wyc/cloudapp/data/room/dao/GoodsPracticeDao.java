package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wyc.cloudapp.data.room.entity.GoodsPractice;

import java.util.List;

@Dao
public interface GoodsPracticeDao {
    @Query("select *from goodsPractice")
    List<GoodsPractice> getAll();
    @Query("select * from goodsPractice where kw_code =:code")
    GoodsPractice getPracticeById(int code);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<GoodsPractice> goodsPractices);
}
