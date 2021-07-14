package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.dao
 * @ClassName: TimeCardPayDetailDao
 * @Description: 次卡付款明细数据访问
 * @Author: wyc
 * @CreateDate: 2021-07-09 15:14
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 15:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Dao
public interface TimeCardPayDetailDao {
    @Query("select * from timeCardPayDetail")
    List<TimeCardPayDetail> getAll();
    @Query("select * from timeCardPayDetail where order_no=:id")
    List<TimeCardPayDetail> getAllById(String id);
    @Insert
    void insertAll(TimeCardPayDetail ...details);
    @Insert
    void insertAll(List<TimeCardPayDetail> details);
    @Update
    void updateAll(List<TimeCardPayDetail> details);
}
