package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.dao
 * @ClassName: TimeCardSaleOrderDao
 * @Description: 次卡销售订单数据访问
 * @Author: wyc
 * @CreateDate: 2021-07-09 14:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 14:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Dao
public interface TimeCardSaleOrderDao {
    @Query("select * from timeCardSaleOrder")
    List<TimeCardSaleOrder> getAll();
    @Query("select count(rowId) as counts from timeCardSaleOrder")
    int count();

    @Insert
    void insert(TimeCardSaleOrder... saleOrders);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertWithDetails(TimeCardSaleOrder saleOrders, List<TimeCardSaleInfo> saleInfoList, List<TimeCardPayDetail> payDetails);

    @Transaction
    @Delete
    void deleteWithDetails(TimeCardSaleOrder saleOrders, List<TimeCardSaleInfo> saleInfoList, List<TimeCardPayDetail> payDetails);

    @RawQuery
    List<TimeCardSaleOrder> getOrderByCondition(SimpleSQLiteQuery query);
    @Query("update timeCardSaleOrder set status = :status,online_order_no=:online_order_no where order_no=:order_no")
    void updateOrder(String order_no,String online_order_no,int status);
    @Query("update timeCardSaleOrder set status = :status where order_no=:order_no")
    void updateOrder(String order_no,int status);
}
