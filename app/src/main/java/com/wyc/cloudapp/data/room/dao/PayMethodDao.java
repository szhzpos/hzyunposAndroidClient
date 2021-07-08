package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wyc.cloudapp.data.room.entity.PayMethod;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.dao
 * @ClassName: PayMethodDao
 * @Description: 支付方式访问接口
 * @Author: wyc
 * @CreateDate: 2021-07-08 14:00
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-08 14:00
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Dao
public interface PayMethodDao {
    @Query("select *from pay_method")
    List<PayMethod> getAll();
    @Query("select * from pay_method where pay_method_id =:id")
    PayMethod getPayMethodById(int id);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<PayMethod> payMethods);
}
