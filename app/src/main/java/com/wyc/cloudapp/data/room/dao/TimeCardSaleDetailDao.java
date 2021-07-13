package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;

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
public abstract class TimeCardSaleDetailDao {
    @Query("select * from timeCardSaleDetails")
    public abstract  List<TimeCardSaleInfo> getAll();
    @Insert
    public abstract  void insertAll(TimeCardSaleInfo ...saleInfo);
    @Insert
    public abstract  void insertAll(List<TimeCardSaleInfo> saleInfo);

    public int getCountsById(String id){
        return getDetailById(id).size();
    }
    @Query("select * from timeCardSaleDetails where order_no=:id")
    public abstract  List<TimeCardSaleInfo> getDetailById(String id);
}
