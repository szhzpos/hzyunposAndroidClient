package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wyc.cloudapp.bean.TimeCardSaleInfo;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.dao
 * @ClassName: TimeCardSaleDetailDao
 * @Description: 次卡销售明细数据访问
 * @Author: wyc
 * @CreateDate: 2021-07-09 15:12
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 15:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Dao
public interface TimeCardSaleDetailDao {
    @Query("select * from timeCardSaleDetails")
    List<TimeCardSaleInfo> getAll();
    @Insert
    void insertAll(TimeCardSaleInfo ...saleInfo);
    @Insert
    void insertAll(List<TimeCardSaleInfo> saleInfo);
}
