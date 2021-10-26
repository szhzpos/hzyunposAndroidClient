package com.wyc.cloudapp.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wyc.cloudapp.application.CustomApplication;
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
    @Query("select *  from pay_method where status = 1 and is_check = 2 and support like '%' || :support_code || '%' order by sort")
    List<PayMethod> getOfflinePayMethodBySupport(String support_code);

    @Query("select *  from pay_method where status = 1 and support like '%' || :support_code || '%' order by sort")
    List<PayMethod> getNotCheckPayMethodBySupport(String support_code);

    @Query("select *  from pay_method where status = 1 and (is_check = 2 or pay_method_id = 560) and support like '%' || :support_code || '%' order by sort")
    List<PayMethod> getPracticeModePayMethodBySupport(String support_code);

    default List<PayMethod> getPayMethodBySupport(String support_code){
        if (!CustomApplication.self().isConnection()){
            return getOfflinePayMethodBySupport(support_code);
        }else if (CustomApplication.isPracticeMode()){
            return getPracticeModePayMethodBySupport(support_code);
        }
        return getNotCheckPayMethodBySupport(support_code);
    }
}
