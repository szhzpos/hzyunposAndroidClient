package com.wyc.cloudapp.data.room;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
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
import com.wyc.cloudapp.logger.Logger;

@Database(entities = {PayMethod.class, TimeCardSaleOrder.class,TimeCardSaleInfo.class, TimeCardPayDetail.class,
        GiftCardSaleOrder.class,GiftCardSaleDetail.class, GiftCardPayDetail.class, GoodsPractice.class, PracticeAssociated.class},
        version = AppDatabase.DATABASE_VERSION,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public static final int DATABASE_VERSION = 15;

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
        return DB;
    }
    public static void initDataBase( final String name){
        if (DB == null){
            synchronized (AppDatabase.class){
                if (DB == null){
                    DB = Room.databaseBuilder(CustomApplication.self(),AppDatabase.class,name)
                            .addMigrations(migration_13_14).addMigrations(migration_14_15)
                            .allowMainThreadQueries()
                            .build();
                    Logger.d("roomVersion:%d",DB.getOpenHelper().getWritableDatabase().getVersion());
                }
            }
        }
    }

    private static boolean checkColumnNotExists(SupportSQLiteDatabase db, String tableName, String columnName) throws SQLiteException {
        boolean result;
        Cursor cursor = null ;
        try{
            cursor = db.query( "select 1 from sqlite_master where name = '"+ tableName +"' and sql like '%" + columnName + "%'", null);
            result = null != cursor && cursor.moveToFirst() ;
        }finally{
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
        return !result;
    }

    private static final Migration migration_14_15 = new Migration(14,DATABASE_VERSION){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `labelTemplate` (`templateId` INTEGER NOT NULL, `templateName` TEXT NOT NULL, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `realWidth` INTEGER NOT NULL, `realHeight` INTEGER NOT NULL, `itemList` TEXT NOT NULL, `backgroundImg` TEXT NOT NULL, PRIMARY KEY(`templateId`))");
        }
    };

    private static final Migration migration_13_14 = new Migration(13,14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            try {
                db.beginTransaction();

                db.execSQL("CREATE TABLE IF NOT EXISTS `goodsPractice` (`kw_id` INTEGER NOT NULL, `kw_code` TEXT NOT NULL, `kw_name` TEXT, `kw_price` REAL, `status` INTEGER, PRIMARY KEY(`kw_id`, `kw_code`))");
                db.execSQL("CREATE TABLE IF NOT EXISTS `practiceAssociated` (`id` INTEGER, `barcode_id` TEXT, `kw_id` INTEGER, `kw_code` TEXT DEFAULT '', `kw_name` TEXT DEFAULT '', `kw_price` REAL DEFAULT 0.0, `status` INTEGER, PRIMARY KEY(`id`))");

                if(checkColumnNotExists(db, "transfer_info", "shopping_num") && checkColumnNotExists(db, "transfer_info", "shopping_money")){
                    db.execSQL("ALTER TABLE transfer_info ADD COLUMN shopping_num INTEGER DEFAULT (0)");
                    db.execSQL("ALTER TABLE transfer_info ADD COLUMN shopping_money REAL DEFAULT (0.00)");
                }

                if(checkColumnNotExists(db, "barcode_info", "goods_img")){
                    db.execSQL("ALTER TABLE barcode_info ADD COLUMN goods_img INTEGER DEFAULT (-1)");
                }
                if(checkColumnNotExists(db, "barcode_info", "updtime")){
                    db.execSQL("ALTER TABLE barcode_info ADD COLUMN updtime INTEGER DEFAULT (0)");
                }

                /*
                 * 20211201 增加做法支持
                 * */
                if(checkColumnNotExists(db, "retail_order_goods", "goodsPractice")){
                    db.execSQL("ALTER TABLE retail_order_goods ADD COLUMN goodsPractice TEXT DEFAULT '[]'");
                }
                if(checkColumnNotExists(db, "refund_order_goods", "goodsPractice")){
                    db.execSQL("ALTER TABLE refund_order_goods ADD COLUMN goodsPractice TEXT DEFAULT '[]'");
                }
                if(checkColumnNotExists(db, "hangbill_detail", "goodsPractice")){
                    db.execSQL("ALTER TABLE hangbill_detail ADD COLUMN goodsPractice TEXT DEFAULT '[]'");
                }
                /* 20211201 end */

                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }
        }
    };

    public static void closeDB(){
        if (DB != null){
            DB.getOpenHelper().close();;
            DB = null;
        }
    }
}