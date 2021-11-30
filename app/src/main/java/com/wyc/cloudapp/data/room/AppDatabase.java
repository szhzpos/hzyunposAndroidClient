package com.wyc.cloudapp.data.room;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.dao.GiftCardPayDetailDao;
import com.wyc.cloudapp.data.room.dao.GiftCardSaleDetailDao;
import com.wyc.cloudapp.data.room.dao.GiftCardSaleOrderDao;
import com.wyc.cloudapp.data.room.dao.GoodsPracticeDao;
import com.wyc.cloudapp.data.room.dao.PayMethodDao;
import com.wyc.cloudapp.data.room.dao.PracticeAssociatedDao;
import com.wyc.cloudapp.data.room.dao.TimeCardPayDetailDao;
import com.wyc.cloudapp.data.room.dao.TimeCardSaleDetailDao;
import com.wyc.cloudapp.data.room.dao.TimeCardSaleOrderDao;
import com.wyc.cloudapp.data.room.entity.GiftCardPayDetail;
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail;
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder;
import com.wyc.cloudapp.data.room.entity.GoodsPractice;
import com.wyc.cloudapp.data.room.entity.PayMethod;
import com.wyc.cloudapp.data.room.entity.PracticeAssociated;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;

@Database(entities = {PayMethod.class, TimeCardSaleOrder.class,TimeCardSaleInfo.class, TimeCardPayDetail.class,
        GiftCardSaleOrder.class,GiftCardSaleDetail.class, GiftCardPayDetail.class, GoodsPractice.class, PracticeAssociated.class},
        version = SQLiteHelper.DATABASE_VERSION)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PayMethodDao PayMethodDao();
    public abstract TimeCardSaleOrderDao TimeCardSaleOrderDao();
    public abstract TimeCardPayDetailDao TimeCardPayDetailDao();
    public abstract TimeCardSaleDetailDao TimeCardSaleDetailDao();

    public abstract GiftCardSaleOrderDao GiftCardSaleOrderDao();
    public abstract GiftCardSaleDetailDao GiftCardSaleDetailDao();
    public abstract GiftCardPayDetailDao GiftCardPayDetailDao();

    public abstract GoodsPracticeDao GoodsPracticeDao();
    public abstract PracticeAssociatedDao PracticeAssociatedDao();

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