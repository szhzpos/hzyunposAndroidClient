package com.wyc.cloudapp.data;

import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.RoomDatabase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FileUtils;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class SQLiteHelper extends SQLiteOpenHelper {
    /**
     * DATABASE_VERSION从14开始由AppDatabase负责升级
     * */
    private static final int DATABASE_VERSION = AppDatabase.DATABASE_VERSION;
    private static volatile SQLiteDatabase mDb;

    private SQLiteHelper(Context context,final String databaseName,int ver){
        super(context, databaseName, null, ver);
        Logger.d("DATABASE_NAME:%s",databaseName);
    }

    private static String DATABASE_NAME(String storesId){
        //数据库名称order_门店编号
        if (!Utils.isNotEmpty(storesId)){
            Logger.e("init DATABASE_NAME failure because the storesId is empty...");
            throw new IllegalArgumentException("storesId must not be empty...");
        }
        return CustomApplication.isPracticeMode() ? String.format(Locale.CHINA,"%sTemp_order_%s",Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/",storesId) : NORMAL_DATABASE_NAME(storesId);
    }
    private static String NORMAL_DATABASE_NAME(String storesId){
        return String.format(Locale.CHINA,"%sorder_%s",Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/",storesId);
    }

    /**
    * 练习收银模式下要复制基本资料
     * @param src 正常模式下本地库文件的绝对路径
    * */
    private static void copy(final String src) {
        final JEventLoop loop = new JEventLoop();
        CustomApplication.execute(()->{
            final ContentValues values = new ContentValues();
            final SQLiteDatabase src_sqLiteHelper = new SQLiteHelper(CustomApplication.self(), src,DATABASE_VERSION).getWritableDatabase();
            try{
                final List<String> tables = getCopyTable();

                src_sqLiteHelper.beginTransaction();
                mDb.beginTransaction();
                for (String name : tables) {
                    mDb.delete(name,null,null);
                    try (Cursor src_cursor = src_sqLiteHelper.query(name, null, null, null, null, null, null)) {
                        int count = src_cursor.getColumnCount();
                        while (src_cursor.moveToNext()) {
                            values.clear();
                            for (int i = 0; i < count; i++) {
                                values.put(src_cursor.getColumnName(i), src_cursor.getString(i));
                            }
                            mDb.insert(name, null, values);
                        }
                    }
                }
                src_sqLiteHelper.setTransactionSuccessful();
                mDb.setTransactionSuccessful();
            }finally {
                src_sqLiteHelper.endTransaction();
                mDb.endTransaction();

                src_sqLiteHelper.close();
            }
            loop.done(1);
        });
        loop.exec();
    }

    public static boolean isNotInit(){
        return mDb == null && AppDatabase.getInstance() != null;
    }

    public static void initDb(Context context, final String storesId){
        final String name = DATABASE_NAME(storesId);
        final boolean exist = new File(name).exists();

        try {
            if (exist){
                final SQLiteHelper helper = new SQLiteHelper(context,name,13);
                helper.setWriteAheadLoggingEnabled(true);
                helper.getWritableDatabase();
                helper.close();

                AppDatabase.initDataBase(name);
            }
            if (mDb == null){
                synchronized (SQLiteHelper.class){
                    if (mDb == null){
                        int ver = DATABASE_VERSION;
                        if (!exist){
                            ver = 13;
                        }

                        final SQLiteHelper helper = new SQLiteHelper(context,name,ver);
                        helper.setWriteAheadLoggingEnabled(true);
                        mDb = helper.getWritableDatabase();

                        if (!exist){
                            AppDatabase.initDataBase(name);
                        }

                        if (CustomApplication.isPracticeMode())copy(NORMAL_DATABASE_NAME(storesId));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            MyDialog.toastMessage("打开数据库错误：" + e.getLocalizedMessage());
            SystemClock.sleep(3000);
            CustomApplication.self().exit();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initTables(db);//初始化数据库
        onUpgrade(db,0,0);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion) {
        final List<String> update_list = new ArrayList<>(),modify_list = new ArrayList<>();
        final String sales_info_sql = "CREATE TABLE IF NOT EXISTS sales_info (\n" +
                "    sc_id      VARCHAR PRIMARY KEY,\n" +
                "    sc_name    VARCHAR,\n" +
                "    sc_phone   VARCHAR,\n" +
                "    stores_id  INTEGER,\n" +
                "    tc_mode    INTEGER,\n" +
                "    is_tc      CHAR,\n" +
                "    tc_rate    NUMERIC,\n" +
                "    sc_status  INTEGER,\n" +
                "    appids     VARCHAR,\n" +
                "    sc_addtime INTEGER\n" +
                ");",
            sale_operator_info_sql = "CREATE TABLE IF NOT EXISTS sale_operator_info (\n" +//经办人
                "    sales_id VARCHAR PRIMARY KEY\n" +
                "                     NOT NULL,\n" +
                "    name,\n" +
                "    sort     VARCHAR\n" +
                ");",auxiliary_barcode_sql = "CREATE TABLE  IF NOT EXISTS auxiliary_barcode_info (\n" +
                "    id            INTEGER PRIMARY KEY,\n" +
                "    g_m_id        INTEGER,\n" +
                "    barcode_id    TEXT,\n" +
                "    fuzhu_barcode TEXT,\n" +
                "    status        INTEGER\n" +
                ");\n",sql_transfer_gift_info = "CREATE TABLE IF NOT EXISTS transfer_gift_money (\n" +//交班购物卡信息
                "    order_num  INTEGER DEFAULT (0),\n" +
                "    pay_money  REAL,\n" +
                "    pay_method INTEGER,\n" +
                "    ti_code    TEXT,\n" +
                "    ti_id      INTEGER PRIMARY KEY AUTOINCREMENT);" +
        update_list.add(sales_info_sql);
        update_list.add(sale_operator_info_sql);
        update_list.add(auxiliary_barcode_sql);
        update_list.add(sql_transfer_gift_info);

        update_list.add("CREATE TABLE IF NOT EXISTS `timeCardSaleOrder` (`order_no` TEXT NOT NULL, `online_order_no` TEXT, `vip_openid` TEXT, `vip_card_no` TEXT, `vip_mobile` TEXT, `vip_name` TEXT, `amt` REAL NOT NULL, `status` INTEGER NOT NULL DEFAULT 0, `saleman` TEXT, `cas_id` TEXT,`transfer_status` INTEGER NOT NULL DEFAULT 0, `time` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`order_no`))");
        update_list.add("CREATE TABLE IF NOT EXISTS `timeCardSaleDetails` (`rowId` INTEGER NOT NULL, `num` INTEGER NOT NULL, `amt` REAL NOT NULL, `price` REAL NOT NULL, `once_card_id` INTEGER NOT NULL, `name` TEXT, `discountAmt` REAL NOT NULL, `order_no` TEXT NOT NULL, PRIMARY KEY(`rowId`, `order_no`))");
        update_list.add("CREATE TABLE IF NOT EXISTS `timeCardPayDetail` (`rowId` INTEGER NOT NULL, `order_no` TEXT NOT NULL, `pay_method_id` INTEGER NOT NULL, `amt` REAL NOT NULL, `zl_amt` REAL NOT NULL, `online_pay_no` TEXT, `remark` TEXT, `status` INTEGER NOT NULL, `cas_id` TEXT, `pay_time` TEXT, PRIMARY KEY(`rowId`, `order_no`))");

        update_list.add("CREATE TABLE IF NOT EXISTS `GiftCardSaleOrder` (`order_no` TEXT NOT NULL, `online_order_no` TEXT NOT NULL, `amt` REAL NOT NULL, `status` INTEGER NOT NULL DEFAULT 0, `saleId` TEXT NOT NULL, `cas_id` TEXT NOT NULL, `store_id` TEXT DEFAULT '-1', `time` INTEGER NOT NULL DEFAULT 0, `transfer_status` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`order_no`))");
        update_list.add("CREATE TABLE IF NOT EXISTS `GiftCardSaleDetail` (`rowId` INTEGER NOT NULL, `num` INTEGER NOT NULL, `amt` REAL NOT NULL, `price` REAL NOT NULL, `face_value` REAL NOT NULL, `gift_card_code` TEXT NOT NULL, `card_chip_no` TEXT NOT NULL, `name` TEXT, `discountAmt` REAL NOT NULL, `order_no` TEXT NOT NULL, PRIMARY KEY(`rowId`, `order_no`))");
        update_list.add("CREATE TABLE IF NOT EXISTS `GiftCardPayDetail` (`rowId` INTEGER NOT NULL, `order_no` TEXT NOT NULL, `pay_method_id` INTEGER NOT NULL, `amt` REAL NOT NULL, `zl_amt` REAL NOT NULL, `online_pay_no` TEXT, `remark` TEXT, `status` INTEGER NOT NULL, `cas_id` TEXT, `pay_time` TEXT, PRIMARY KEY(`rowId`, `order_no`))");

        if (oldVersion <= 10){
            update_list.add("delete from barcode_info;");
            update_list.add("DROP TABLE pay_method;");
            update_list.add("CREATE TABLE `pay_method` (`pay_method_id` INTEGER NOT NULL, `name` TEXT, `status` INTEGER, `remark` TEXT, `is_check` INTEGER, `shortcut_key` TEXT, `sort` INTEGER, `xtype` TEXT, `pay_img` TEXT, `master_img` TEXT, `is_show_client` INTEGER, `is_cardno` INTEGER DEFAULT 1, `is_scan` INTEGER DEFAULT 2, `wr_btn_img` TEXT, `unified_pay_order` TEXT, `unified_pay_query` TEXT, `rule` TEXT, `is_open` INTEGER DEFAULT 1, `support` TEXT, `is_enable` INTEGER DEFAULT 1, PRIMARY KEY(`pay_method_id`));");
        }

        //修改
        if(checkColumnNotExists(db, "member_order_info", "sc_id")){
            modify_list.add("ALTER TABLE member_order_info ADD COLUMN sc_id  VARCHAR");
        }
        if(checkColumnNotExists(db, "member_order_info", "order_type")){//1 充值单  2 会员退款单
            modify_list.add("ALTER TABLE member_order_info ADD COLUMN order_type  INTEGER DEFAULT (1)");
        }
        if(checkColumnNotExists(db, "member_order_info", "origin_order_code")){
            modify_list.add("ALTER TABLE member_order_info ADD COLUMN origin_order_code  VARCHAR");
        }
        if(checkColumnNotExists(db, "pay_method", "is_moling")){
            modify_list.add("ALTER TABLE pay_method ADD COLUMN is_moling INTEGER DEFAULT (1)");
        }
        if(checkColumnNotExists(db, "shop_stores", "wh_id")){
            modify_list.add("ALTER TABLE shop_stores ADD COLUMN wh_id VARCHAR");
        }

        if(checkColumnNotExists(db, "shop_category", "category_code")){
            modify_list.add("ALTER TABLE shop_category ADD COLUMN category_code VARCHAR");
        }
        if(checkColumnNotExists(db, "barcode_info", "current_goods")){
            modify_list.add("ALTER TABLE barcode_info ADD COLUMN current_goods INTEGER DEFAULT (1)");
        }
        if(checkColumnNotExists(db, "barcode_info", "spec_str")){
            modify_list.add("ALTER TABLE barcode_info ADD COLUMN spec_str VARCHAR");
        }
        if(checkColumnNotExists(db, "barcode_info", "cash_flow_ratio")){
            modify_list.add("ALTER TABLE barcode_info ADD COLUMN cash_flow_ratio REAL DEFAULT (0.00)");
        }

        try {
            db.beginTransaction();
            for (String sql : update_list) {
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        try {
            db.beginTransaction();
            for (String sql : modify_list) {
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onConfigure (SQLiteDatabase db){
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    public static boolean isNew(){
        final StringBuilder err = new StringBuilder();
        final String sql = "select count(1) from barcode_info";
        final String szCount = getString(sql,err);
        if (szCount != null){
            return "0".equals(szCount);
        }
        return false;
    }

    public static boolean backupDBPublicDir(Context context, final String new_name, final StringBuilder err){
        final String file_absolute_path = Environment.getExternalStorageDirectory().getAbsolutePath(),backup_name = new_name + ".zip",
                file_name = file_absolute_path + "/hzYunPos/";

        final File db = new File(file_name);
        boolean code = false;

        OutputStream outputStream = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, backup_name);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, FileUtils.getMIMEType(backup_name));
                contentValues.put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis());
                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                outputStream = context.getContentResolver().openOutputStream(uri);
            }else {
                String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                outputStream = new FileOutputStream(dir + backup_name);
            }
            FileUtils.zipFile(db,outputStream);
            Utils.deleteFile(db);
            closeDB();
            code = true;
        }catch (IOException e){
            e.printStackTrace();
            if (err != null){
                err.append(e.getMessage());
            }else MyDialog.toastMessage(e.getMessage());
        }finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
       return code;
    }

    private static boolean checkColumnNotExists(SQLiteDatabase db, String tableName, String columnName) throws SQLiteException {
        boolean result;
        Cursor cursor = null ;
        try{
            cursor = db.rawQuery( "select 1 from sqlite_master where name = '"+ tableName +"' and sql like '%" + columnName + "%'", null);
            result = null != cursor && cursor.moveToFirst() ;
        }finally{
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
        return !result;
    }

    private static boolean execSQLByBatchFromJson(@NonNull JSONArray jsonArray, String table, StringBuilder err, int type) {
        //type 0 insert 1 replace

        boolean isTrue = true;
        JSONObject jsonObject;
        SQLiteStatement statement = null;
        int columnN0;
        if (jsonArray.isEmpty()) {
            return false;
        }

        jsonObject = jsonArray.getJSONObject(0);
        final String sql = generateSql(jsonObject,table,null,type);

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                statement = mDb.compileStatement(sql);
                for (int i = 0,len = jsonArray.size();i < len; i ++){
                    columnN0 = 0;
                    jsonObject = jsonArray.getJSONObject(i);
                    String value;
                    for (String key : jsonObject.keySet()){
                        value = jsonObject.getString(key);
                        if (null == value || "".equals(value)){
                            statement.bindNull(++columnN0);
                        }else
                            statement.bindString(++columnN0,value);
                    }
                    statement.execute();
                }
                mDb.setTransactionSuccessful();
            } catch (SQLException e) {
                isTrue = false;
                if (err != null)err.append(e.getMessage());
                e.printStackTrace();
            } finally {
                if (statement != null){
                    statement.close();
                }
                if (mDb != null){
                    mDb.endTransaction();
                }
            }
        }
        return isTrue;
    }
    private static boolean saveFormJson(@NonNull final JSONObject json, @NonNull final String table_name,int action,final StringBuilder err){
        boolean isTrue = true;
        SQLiteStatement statement = null;
        int columnN0 = 0;
        final String sql = generateSql(json,table_name,null,action);

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                statement = mDb.compileStatement(sql);
                String value;
                for (String key : json.keySet()){
                    value = json.getString(key);
                    if (null == value || "".equals(value)){
                        statement.bindNull(++columnN0);
                    }else
                        statement.bindString(++columnN0,value);
                }
                statement.execute();

                mDb.setTransactionSuccessful();
            } catch (SQLException | JSONException e) {
                isTrue = false;
                if (err != null)err.append(e.getMessage());
                e.printStackTrace();
            } finally {
                if (statement != null){
                    statement.close();
                }
                if (mDb != null){
                    mDb.endTransaction();
                }
            }
        }
        return isTrue;
    }

    public static boolean execSQLByBatchFromJson(@NonNull final JSONArray jsonArray,final String table,final String[] cls,final StringBuilder err,int type) {
        //type 0 insert 1 replace

        boolean isTrue = true;
        JSONObject jsonObject;
        SQLiteStatement statement = null;
        int columnN0;
        if (jsonArray.isEmpty()) {
            return false;
        }

        if (cls == null || cls.length == 0){
            return execSQLByBatchFromJson(jsonArray,table,err,type);
        }

        final String sql = generateSql(null,table,cls,type);

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                statement = mDb.compileStatement(sql);
                String value;
                for (int i = 0,len = jsonArray.size();i < len; i ++){
                    columnN0 = 0;
                    jsonObject = jsonArray.getJSONObject(i);
                    for (String cl:cls){
                        value = jsonObject.getString(cl);
                        if (null == value){
                            statement.bindNull(++columnN0);
                        }else {
                            statement.bindString(++columnN0,value);
                        }
                    }
                    statement.execute();
                }
                mDb.setTransactionSuccessful();
            } catch (SQLException e) {
                isTrue = false;
                if (err != null)err.append(e.getMessage());
                e.printStackTrace();
            } finally {
                if (statement != null){
                    statement.close();
                }
                if (mDb != null){
                    mDb.endTransaction();
                }
            }
        }
        return isTrue;
    }

    public static boolean getLocalParameter(final String parameter_id,@NonNull final JSONObject param){
        boolean isTrue = true;
        try (Cursor cursor = mDb.query("local_parameter",new String[]{"parameter_content"},"parameter_id = ?",new String[]{parameter_id},
                null,null,null)){
            if (!cursor.moveToNext()){
                return true;
            }
            final JSONObject json = JSON.parseObject(cursor.getString(0));
            for (String key : json.keySet()){
                param.put(key,json.get(key));
            }
        } catch (SQLiteException e) {
            isTrue = false;
            param.put("info",e.getMessage());
            e.printStackTrace();
        }
        return isTrue;
    }

    public static boolean saveLocalParameter(final String parameter_id,JSONObject content,final String desc,final StringBuilder err){
        ContentValues values = new ContentValues();
        values.put("parameter_id",parameter_id);
        values.put("parameter_content",content.toString());
        values.put("parameter_desc",desc);
        return execInsertSql("local_parameter",null,values,err) > 0;
    }

    public static void closeDB(){
        if (mDb != null)
        {
            synchronized (SQLiteHelper.class){
                if (mDb.inTransaction())mDb.endTransaction();
                mDb.close();
                mDb = null;
                Logger.i("mDb closed...");
            }
        }
    }

    public static JSONArray getListToJson(@NonNull String sql, Integer minrow, Integer maxrow, boolean row, StringBuilder err){
        JSONArray array;
        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                array = rs2Json(cursor,minrow,maxrow,row);
            } catch (JSONException | SQLiteException e) {
                if (err != null)err.append("查询错误：").append(e.getMessage());
                e.printStackTrace();
                array = null;
            }
        }
        return array;
    }
    public static JSONArray getListToJson(@NonNull String sql,StringBuilder err){
        JSONArray array;
        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                array = rs2Json(cursor,0,0,false);
            } catch (JSONException | SQLiteException e) {
                if (err != null)
                    err.append(e.getMessage());
                else
                    MyDialog.toastMessage(e.getMessage());

                e.printStackTrace();
                array = null;
            }
        }
        return array;
    }
    public static JSONArray getListToValue(@NonNull String sql,StringBuilder err){
        JSONArray array;
        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                array = rs2Values(cursor,0,0,false);
            } catch (JSONException | SQLiteException e) {
                if (err !=  null)err.append(e.getMessage());
                e.printStackTrace();
                array = null;
            }
        }
        return array;
    }
    public static List<Map<String,Object>> getList(@NonNull String sql,StringBuilder err){
        List<Map<String,Object>> maps;
        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                maps = rs2List(cursor);
            } catch (SQLiteException e) {
                if (err != null)err.append("查询出错：" ).append(e.getLocalizedMessage());
                e.printStackTrace();
                maps = null;
            }
        }
        return maps;
    }
    public static String getString(@NonNull String sql,StringBuilder err){
        String result;
        synchronized(SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null)){
                result = rs2Txt(cursor);
            } catch (SQLiteException e) {
                if (err != null)err.append(e.getMessage());
                result = null;
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean saveFormJson(@NonNull final JSONObject json, @NonNull final String table_name, String[] cls,int action, StringBuilder err){
        boolean isTrue = true;
        SQLiteStatement statement = null;
        int columnN0 = 0;

        if (cls == null || cls.length == 0){
            return saveFormJson(json,table_name,action,err);
        }

        final String sql = generateSql(null,table_name,cls,action);

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                statement = mDb.compileStatement(sql);
                String value;
                for (String cl:cls){
                    value = json.getString(cl);
                    if (null == value || "".equals(value)){
                        statement.bindNull(++columnN0);
                    }else
                        statement.bindString(++columnN0,value);
                }
                statement.execute();

                mDb.setTransactionSuccessful();
            } catch (SQLException | JSONException e) {
                isTrue = false;
                if (err != null)err.append(e.getMessage());
                e.printStackTrace();
            } finally {
                if (statement != null){
                    statement.close();
                }
                if (mDb != null){
                    mDb.endTransaction();
                }
            }
        }
        return isTrue;
    }
    public static boolean execSql(@NonNull JSONObject json,@NonNull final String sql){
        boolean isTrue = true;
        //执行select语句 json只返回一条记录，如果出错json包含错误信息 sql要执行的数据库查询语句

        ArrayList<String> colNames= new ArrayList<>();
        ArrayList<Integer> coltypes= new ArrayList<>();

        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null)){
                // 获取列数
                int columnCount = cursor.getColumnCount();
                if(!cursor.moveToNext()) return true;

               //获取列名及相关信息
                for (int i = 0; i < columnCount; i++) {
                    colNames.add(cursor.getColumnName(i));
                    coltypes.add(cursor.getType(i));
                }
                // 遍历ResultSet中的每条数据
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        json.put(colNames.get(i),cursor.getDouble(i));  /////i = 0;rs里面的是从1开始
                    } else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {
                        json.put(colNames.get(i), cursor.getInt(i));
                    } else {
                        if (cursor.getString(i) == null) {
                            json.put(colNames.get(i), "");
                        } else {
                            json.put(colNames.get(i), cursor.getString(i).trim());
                        }
                    }
                }
            } catch (JSONException | SQLiteException e) {
                isTrue = false;
                try {
                    json.put("info",e.getMessage());
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return isTrue;
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TableName {
        String value() default "";
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Ignore{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Where{
        int index() default 0;
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OrderBy{
        int order() default 0;
        boolean desc() default false;
    }

    public static <T> List<T> getBeans(Class<T> tClass,final String ...args)  {
        String tableName;
        TableName annotation = tClass.getAnnotation(TableName.class);
        if (annotation != null){
            tableName = annotation.value();
        }else {
            tableName = tClass.getSimpleName();
        }

        final String[] arguments = args.clone();
        final String where = createWhereClause(tClass);
        Logger.d("where:%s,arguments:%s",where, Arrays.toString(args));

        final String orderBy = createOrderByClause(tClass);
        Logger.d("oderBy:%s",orderBy);

        final Map<String, Method> fieldToMethod = createCols(tClass);
        final String[] cols = fieldToMethod.keySet().toArray(new String[0]);
        List<T> results = new ArrayList<>();
        try (Cursor cursor = mDb.query(tableName,cols,where, arguments,null,null,orderBy)){
            int index;
            Method method;
            T obj;
            Constructor<T> constructor = tClass.getConstructor();
            while (cursor.moveToNext()){
                obj = constructor.newInstance();
                for (String colKey : cols){
                    index = cursor.getColumnIndex(colKey);
                    int type = cursor.getType(index);
                    method = fieldToMethod.get(colKey);
                    if (null == method)continue;
                    switch (type){
                        case FIELD_TYPE_FLOAT:
                            method.invoke(obj,cursor.getFloat(index));
                            break;
                        case FIELD_TYPE_INTEGER:
                            method.invoke(obj,cursor.getInt(index));
                            break;
                        case FIELD_TYPE_NULL:
                            method.invoke(obj, (Object) null);
                            break;
                        case FIELD_TYPE_STRING:
                            method.invoke(obj,cursor.getString(index));
                            break;
                    }
                }
                results.add(obj);
            }
        } catch (SQLiteException | NoSuchMethodException | IllegalAccessException |
                IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            MyDialog.toastMessage("getBeans:" + e.getMessage());
        }
        return results;
    }
    private static String combinationSetMethod(final String fieldName){
        return "set" + captureName(fieldName);
    }
    private static String captureName(String str) {
        char[] cs= str.toCharArray();
        if (cs[0] >= 65 && cs[0] <= 90)return str;
        cs[0]-= 32;
        return String.valueOf(cs);
    }
    private static <T> Map<String,Method> createCols(final Class<T> tClass){
        String fieldName,methodName;
        Field[] fields = tClass.getDeclaredFields();
        Method[] methods = tClass.getMethods();
        final Map<String,Method> fieldToMethod = new HashMap<>();
        for (Field field : fields){
            if (field.getAnnotation(Ignore.class) != null)continue;

            fieldName = field.getName();
            methodName = combinationSetMethod(fieldName);
            for (Method method : methods){
                if (methodName.equals(method.getName())){
                    fieldToMethod.put(fieldName,method);
                }
            }
        }
        return fieldToMethod;
    }
    private static <T> String createWhereClause(final Class<T> tClass){
        final StringBuilder sb  = new StringBuilder();
        final SortedMap<Integer,String> map = new TreeMap<>();
        final Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields){
            final Where where = field.getAnnotation(Where.class);
            if (where == null)continue;
            map.put(where.index(),field.getName());
        }
        for (Integer integer : map.keySet()) {
            final String name = map.get(integer);
            if (sb.lastIndexOf("?") == -1) {
                sb.append(name).append("=").append("?");
            } else {
                sb.append(" and ").append(name).append("=").append("?");
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
    private static <T> String createOrderByClause(final Class<T> tClass){
        boolean desc = false;
        final StringBuilder sb  = new StringBuilder();
        final SortedMap<Integer,String> map = new TreeMap<>();
        final Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields){
            final OrderBy orderBy = field.getAnnotation(OrderBy.class);
            if (orderBy == null)continue;
            map.put(orderBy.order(),field.getName());
            desc = orderBy.desc();
        }
        for (Integer integer : map.keySet()) {
            final String name = map.get(integer);
            if (sb.length() == 0) {
                sb.append(name);
            } else {
                sb.append(",").append(name);
            }
        }
        if (desc) sb.append(" desc");

        return sb.length() > 0 ? sb.toString() : null;
    }

    public static int execUpdateSql(@NonNull final String table,final ContentValues values,final String whereClause,final String[] whereArgs,StringBuilder err){
        int code = -1;
        try {
            synchronized (SQLiteHelper.class){
                code = mDb.update(table,values,whereClause,whereArgs);
            }
        }catch (SQLiteException e){
            e.printStackTrace();
            if (err != null){
                err.append(e.getMessage());
            }else MyDialog.toastMessage(e.getMessage());
        }
        return code;
    }

    public static int verifyUpdateResult(@NonNull int[] rows){
        int index = 0;
        for (int row : rows){
            if (row == 0)return index;
            index ++;
        }
        Logger.d("批量更新返回值验证：" + Arrays.toString(rows));
        return -1;
    }

    public static int[] execBatchUpdateSql(@NonNull final List<String> tables,final List<ContentValues> values,final List<String> whereClauses,final List<String[]> whereArgs,StringBuilder err){
        if (tables.size() == values.size()){
            int[] codes = new int[tables.size()];
            synchronized (SQLiteHelper.class){
                mDb.beginTransaction();
                try {
                    for (int i = 0,size = tables.size();i < size;i ++){
                        codes[i] = mDb.update(tables.get(i),values.get(i),whereClauses.get(i),whereArgs.get(i));
                    }
                    mDb.setTransactionSuccessful();
                }catch (SQLiteException e){
                    e.printStackTrace();
                    if (err != null)err.append(e.getMessage());
                    codes = null;
                }finally {
                    mDb.endTransaction();
                }
                return codes;
            }
        }else {
            final String mess = String.format(Locale.CHINA,"更新表数量tables：%d 不等于 更新值数量values：%d",tables.size(),values.size());
            if (err != null){err.append(mess);}else MyDialog.toastMessage(mess);
        }
        return null;
    }

    private static long execInsertSql(@NonNull final String table,final String nullColumnHack,final ContentValues values,StringBuilder err){
        long code = -1;
        try {
            synchronized (SQLiteHelper.class){
                code = mDb.replaceOrThrow(table,nullColumnHack,values);
            }
        }catch (SQLiteException e){
            e.printStackTrace();
            if (err != null){
                err.append(e.getMessage());
            }else MyDialog.toastMessage(e.getMessage());
        }
        return code;
    }
    public static int execDelete(String table, String whereClause, String[] whereArgs,StringBuilder err){
        int code = -1;
        try {
            synchronized (SQLiteHelper.class){
                code =  mDb.delete(table,whereClause,whereArgs);
            }
        }catch (SQLiteException e){
            e.printStackTrace();
            if (err != null){
                err.append(e.getMessage());
            }else MyDialog.toastMessage(e.getMessage());
        }
        return code;
    }
    public static boolean execBatchUpdateSql(@NonNull final List<String> list,@Nullable StringBuilder err){
        boolean code = true;
        try {
            synchronized (SQLiteHelper.class){
                mDb.beginTransaction();
                try {
                    for (String sql : list) {
                        mDb.execSQL(sql);
                    }
                    mDb.setTransactionSuccessful();
                }finally {
                    mDb.endTransaction();
                }
            }
        }catch (SQLiteException e){
            e.printStackTrace();
            code = false;
            if (err != null){
                err.append(e.getMessage());
            }else MyDialog.toastMessage(e.getMessage());
        }
        return code;
    }

    public static boolean execSQLByBatchFromJson(@NonNull JSONObject json, List<String> tables, List<List<String>> table_cols,@Nullable StringBuilder err, int type ){//type 0 insert 1 replace
        boolean code = true;
        JSONObject jsonObject;
        String table,value,sql;
        SQLiteStatement statement = null;
        int columnN0 = 0;
        List<String> cls;
        JSONArray arrays;

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                for (int k = 0,size = tables.size();k < size;k++) {
                    table = tables.get(k);
                    arrays = json.getJSONArray(table);
                    if (arrays == null){
                        if (err != null)err.append(table).append("不能为null");
                        return false;
                    }

                    if (table_cols != null){
                        cls = table_cols.get(k);
                        sql = generateSql(table,cls,type);
                    }else{
                        jsonObject = arrays.getJSONObject(0);
                        sql = generateSql(jsonObject,table,null,type);
                    }

                    statement = mDb.compileStatement(sql);
                    for (int i = 0, len = arrays.size(); i < len; i++) {
                        columnN0 = 0;
                        jsonObject = arrays.getJSONObject(i);
                        if (table_cols != null){
                            cls = table_cols.get(k);
                            for (String cl : cls) {
                                value = jsonObject.getString(cl);
                                if (null == value || "".equals(value)) {
                                    statement.bindNull(++columnN0);
                                } else
                                    statement.bindString(++columnN0,value);
                            }
                        }else{
                            for (String key:jsonObject.keySet()){
                                value = jsonObject.getString(key);
                                if (null == value || "".equals(value)){
                                    statement.bindNull(++columnN0);
                                }else
                                    statement.bindString(++columnN0,value);
                            }
                        }
                        statement.execute();
                    }
                }
                mDb.setTransactionSuccessful();
            } catch (SQLException | JSONException e) {
                code = false;
                if (err != null){
                    err.append(e.getMessage());
                }else MyDialog.toastMessage(e.getMessage());
                e.printStackTrace();
            } finally {
                if (statement != null){
                    statement.close();
                }
                if (mDb != null){
                    mDb.endTransaction();
                }
            }
        }

         return code;
    }

    public static boolean execBatchInsertAndUpdate(@NonNull JSONObject json, List<String> tables, List<List<String>> table_cols,@NonNull final List<String> list,@Nullable StringBuilder err, int type ){
        boolean code = true;
        JSONObject jsonObject;
        String table,value,sql;
        SQLiteStatement statement = null;
        int columnN0 = 0;
        List<String> cls;
        JSONArray arrays;

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();

                for (int k = 0,size = tables.size();k < size;k++) {
                    table = tables.get(k);
                    arrays = json.getJSONArray(table);

                    if (arrays == null){
                        if (err != null)err.append(table).append("不能为null");
                        return false;
                    }

                    if (table_cols != null){
                        cls = table_cols.get(k);
                        sql = generateSql(table,cls,type);
                        statement = mDb.compileStatement(sql);
                        for (int i = 0, len = arrays.size(); i < len; i++) {
                            columnN0 = 0;
                            jsonObject = arrays.getJSONObject(i);
                            cls = table_cols.get(k);
                            for (String cl : cls) {
                                value = jsonObject.getString(cl);
                                if (null == value || "".equals(value)) {
                                    statement.bindNull(++columnN0);
                                } else
                                    statement.bindString(++columnN0,value);
                            }
                            statement.execute();
                        }
                    }else{
                        jsonObject = arrays.getJSONObject(0);
                        sql = generateSql(jsonObject,table,null,type);
                        statement = mDb.compileStatement(sql);
                        for (int i = 0, len = arrays.size(); i < len; i++) {
                            columnN0 = 0;
                            jsonObject = arrays.getJSONObject(i);
                            for (String key:jsonObject.keySet()){
                                value = jsonObject.getString(key);
                                if (null == value || "".equals(value)){
                                    statement.bindNull(++columnN0);
                                }else
                                    statement.bindString(++columnN0,value);
                            }
                            statement.execute();
                        }
                    }
                }

                for (String sz : list) {
                    mDb.execSQL(sz);
                }

                mDb.setTransactionSuccessful();
            } catch (SQLException | JSONException e) {
                code = false;
                if (err != null)err.append(e.getMessage());
                e.printStackTrace();
            } finally {
                if (statement != null){
                    statement.close();
                }
                if (mDb != null){
                    mDb.endTransaction();
                }
            }
        }
        return code;
    }
    public static Cursor getCursor(final String sql,final String[] selectArgs){
        synchronized(SQLiteHelper.class){
            return mDb.rawQuery(sql,selectArgs);
        }
    }

    private static String generateSql(@NonNull final String table_name, @Nullable List<String> cls, int type){
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();

        if (type == 0)
            stringBuilderHead.append(" INSERT INTO ");
        else
            stringBuilderHead.append(" REPLACE INTO ");

        stringBuilderHead.append(table_name);
        stringBuilderHead.append(" (");
        stringBuilderfoot.append("VALUES (");

        if (cls != null && !cls.isEmpty()){
            for (String cl:cls){
                stringBuilderHead.append(cl);
                stringBuilderHead.append(",");

                stringBuilderfoot.append("?");
                stringBuilderfoot.append(",");
            }
        }
        stringBuilderHead.replace(stringBuilderHead.length() - 1,stringBuilderHead.length(),")");
        stringBuilderfoot.replace(stringBuilderfoot.length() - 1,stringBuilderfoot.length(),")");
        stringBuilderHead.append(stringBuilderfoot);

        return stringBuilderHead.toString();
    }
    private static String generateSql(@Nullable JSONObject json, @NonNull final String table_name, @Nullable String[] cls, int type){
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();

        if (type == 0)
            stringBuilderHead.append(" INSERT INTO ");
        else
            stringBuilderHead.append(" REPLACE INTO ");

        stringBuilderHead.append(table_name);
        stringBuilderHead.append(" (");
        stringBuilderfoot.append("VALUES (");

        if (cls != null && cls.length != 0){
            for (String cl:cls){
                stringBuilderHead.append(cl);
                stringBuilderHead.append(",");

                stringBuilderfoot.append("?");
                stringBuilderfoot.append(",");
            }
        }else {
            if (json != null)
                for (String key : json.keySet()){
                    stringBuilderHead.append(key);
                    stringBuilderHead.append(",");

                    stringBuilderfoot.append("?");
                    stringBuilderfoot.append(",");
                }
        }

        stringBuilderHead.replace(stringBuilderHead.length() - 1,stringBuilderHead.length(),")");
        stringBuilderfoot.replace(stringBuilderfoot.length() - 1,stringBuilderfoot.length(),")");
        stringBuilderHead.append(stringBuilderfoot);

        return stringBuilderHead.toString();
    }
    private static List<Map<String,Object>> rs2List(Cursor cursor) {

        final List<Map<String,Object>> list = new ArrayList<>();
        final ArrayList<String> colNames= new ArrayList<>();
        final ArrayList<Integer> coltypes= new ArrayList<>();
        Map<String,Object> map;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        //if(!rs.next()) return "";
        if(!cursor.moveToNext()) return list;

        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            colNames.add(cursor.getColumnName(i));
            coltypes.add(cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据

            do {
                map = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        map.put(colNames.get(i),cursor.getDouble(i));
                    } else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {//发现有小数的也返回这个数据类型，所以改用取double
                        map.put(colNames.get(i),cursor.getDouble(i));
                    } else {
                        if (cursor.getString(i) == null) {
                            map.put(colNames.get(i), "");
                        } else {
                            map.put(colNames.get(i), cursor.getString(i));
                        }
                    }
                }
                list.add(map);
            } while (cursor.moveToNext());
        return list;
    }
    private static JSONArray rs2Json(Cursor cursor, Integer minRow, Integer maxRow, boolean row) throws JSONException {
        // json数组
        final JSONArray array = new JSONArray();
        final ArrayList<String> colNames= new ArrayList<>();
        final ArrayList<Integer> coltypes= new ArrayList<>();
        JSONObject jsonObj;
        int row_count = 0;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        if(!cursor.moveToNext()) return array;
        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            colNames.add(cursor.getColumnName(i));
            coltypes.add(cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据

        if(row) {
            do {
                row_count++;
                if (row_count > minRow && row_count <= maxRow){
                    jsonObj= new JSONObject();
                    for (int i = 0; i < columnCount; i++) {
                        if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                            jsonObj.put(colNames.get(i) ,cursor.getDouble(i));
                        }/*else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            jsonObj.put(colNames.get(i) , cursor.getDouble(i));
                        }*/else {
                            if(cursor.getString(i)==null){
                                jsonObj.put(colNames.get(i) , "");
                            }else{
                                jsonObj.put(colNames.get(i) , cursor.getString(i).trim());
                            }
                        }
                    }
                    array.add(jsonObj);
                }
            }while(cursor.moveToNext());
        }else {
            do {
                jsonObj = new JSONObject();
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        jsonObj.put(colNames.get(i),cursor.getDouble(i));
                    } /*else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {
                        jsonObj.put(colNames.get(i), cursor.getDouble(i));
                    } */else {
                        if (cursor.getString(i) == null) {
                            jsonObj.put(colNames.get(i), "");
                        } else {
                            jsonObj.put(colNames.get(i), cursor.getString(i).trim());
                        }
                    }
                }
                array.add(jsonObj);
            } while (cursor.moveToNext());
        }
        return array;
    }
    private static JSONArray rs2Values(Cursor cursor, Integer minRow, Integer maxRow, boolean row) throws JSONException {
        // json数组
        JSONArray array = new JSONArray();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
        int row_count = 0;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        if(!cursor.moveToNext()) return array;
        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            coltypes.add(cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据

        if(row) {
            do {
                row_count++;
                if (row_count > minRow && row_count <= 0){
                    for (int i = 0; i < columnCount; i++) {
                        if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                            array.add(cursor.getDouble(i));
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            array.add(cursor.getInt(i));
                        }else {
                            if(cursor.getString(i)==null){
                                array.add( "");
                            }else{
                                array.add(cursor.getString(i).trim());
                            }
                        }
                    }
                }
            }while(cursor.moveToNext());
        }else {
            do {
                for (int i = 0; i < columnCount; i++) {
                    if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                        array.add(cursor.getDouble(i));
                    }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                        array.add(cursor.getInt(i));
                    }else {
                        if(cursor.getString(i)==null){
                            array.add( "");
                        }else{
                            array.add(cursor.getString(i).trim());
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        return array;
    }
    private static JSONArray rs2ContentValues(Cursor cursor, Integer minRow, Integer maxRow, boolean row) {
        // json数组
        JSONArray array = new JSONArray();
        ArrayList<String> colNames= new ArrayList<>();
        ArrayList<Integer> coltypes= new ArrayList<>();
        ContentValues values;
        int  row_count = 0;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        if(!cursor.moveToNext()) return array;

        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            colNames.add(cursor.getColumnName(i));
            coltypes.add(cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据
        if(row) {
            do {
                row_count++;
                if (row_count > minRow && row_count <= maxRow){
                    values = new ContentValues();
                    for (int i = 0; i < columnCount; i++) {
                        if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                            values.put(colNames.get(i) ,cursor.getDouble(i));
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            values.put(colNames.get(i) ,cursor.getInt(i));
                        }else {
                            if(cursor.getString(i) == null){
                                values.put(colNames.get(i) , "");
                            }else{
                                values.put(colNames.get(i) , cursor.getString(i).trim());
                            }
                        }
                    }
                    array.add(values);
                }
            }while(cursor.moveToNext());
        }else {
            do {
                values = new ContentValues();
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        values.put(colNames.get(i),cursor.getDouble(i));
                    } else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {
                        values.put(colNames.get(i), cursor.getInt(i));
                    } else {
                        if (cursor.getString(i) == null) {
                            values.put(colNames.get(i), "");
                        } else {
                            values.put(colNames.get(i), cursor.getString(i).trim());
                        }
                    }
                }
                array.add(values);
            } while (cursor.moveToNext());
        }
        return array;
    }
    private static String rs2Txt(Cursor cursor) throws SQLiteException {
        // json数组

        ArrayList<Integer> coltypes=new ArrayList<>();
        StringBuilder dataList = new StringBuilder();
        String data;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        //获取列名及相关信息
        if(!cursor.moveToNext()) {
            return "";
        }

        for (int i = 0; i < columnCount; i++) {
            coltypes.add(cursor.getType(i));
        }
        // 遍历ResultSet中的每条数据
        do {
            if(dataList.length()>0){
                dataList.append("\r\n");
            }
            for (int i = 0; i < columnCount; i++) {
                data = cursor.getString(i);
                if(data == null){
                    if(coltypes.get(i) == FIELD_TYPE_STRING || coltypes.get(i) == FIELD_TYPE_NULL){
                        data="";
                    }else if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                        data="0.00";
                    }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                        data="0";
                    }
                }
                if(i>0){
                    dataList.append("\t");
                }
                dataList.append(data);
            }
        }while(cursor.moveToNext());
        return dataList.toString();
    }

    public static  @Nullable String getCashierNameById(String cas_id){
        return SQLiteHelper.getString("select cas_name from cashier_info where cas_id = '"+ cas_id + "'",null);
    }

    public static @Nullable String getShopAssistantById(String sc_id){
        return SQLiteHelper.getString("select sc_name from sales_info where sc_id = '"+ sc_id + "'",null);
    }

    public static @Nullable String getStoreNameById(String s_id){
        return SQLiteHelper.getString("select stores_name from shop_stores where stores_id = '"+ s_id + "'",null);
    }


    public static @NonNull List<String> getSyncDataTableName(){
        return Arrays.asList("shop_category","shop_stores","barcode_info","pay_method","cashier_info","fullreduce_info","sales_info",
                "promotion_info","sale_operator_info","goods_group", "goods_group_info","buyfull_give_x","buy_x_give_x","step_promotion_info","auxiliary_barcode_info","goodsPractice","practiceAssociated");
    }
    private static @NonNull List<String> getCopyTable(){
        return Arrays.asList("local_parameter","shop_category","shop_stores","barcode_info","pay_method","cashier_info","fullreduce_info","sales_info",
                "promotion_info","sale_operator_info","goods_group", "goods_group_info","buyfull_give_x","buy_x_give_x","step_promotion_info","auxiliary_barcode_info","goodsPractice","practiceAssociated");
    }

    public static String[] getGoodsCols(){
        return new String[]{"goods_id","barcode_id","barcode","goods_title","only_coding","retail_price","buying_price","trade_price","cost_price","ps_price",
                "unit_id","unit_name","specifi","category_name","metering_id","current_goods","shelf_life","goods_status","origin","type","goods_tare","barcode_status","category_id",
                "tax_rate","tc_mode","tc_rate","yh_mode","yh_price","mnemonic_code","image","attr_id","attr_name","attr_code","brand_id","brand","brand_code","gs_id","gs_code","gs_name",
                "conversion","update_price","stock_unit_id","stock_unit_name","goods_img","img_url","spec_str","cash_flow_ratio","updtime"};
    }

    private void initTables(SQLiteDatabase db) throws SQLiteException {

        List<String> list = new ArrayList<>();
        final String sql_shop_stores = "CREATE TABLE IF NOT EXISTS  shop_stores (\n" +//商店仓库
                "    nature      INT     DEFAULT (1),\n" +
                "    status      INT     DEFAULT (1),\n" +
                "    region      VARCHAR,\n" +
                "    telphone    CHAR,\n" +
                "    manager     VARCHAR,\n" +
                "    stores_name VARCHAR,\n" +
                "    stores_id   INTEGER PRIMARY KEY\n" +
                ");\n",sql_shop_category = "CREATE TABLE IF NOT EXISTS shop_category (\n" +//商品类别
                "    sort        INTEGER DEFAULT (0),\n" +
                "    status      INTEGER DEFAULT (1),\n" +
                "    path        CHAR,\n" +
                "    depth       INT,\n" +
                "    parent_id   CHAR    DEFAULT (0),\n" +
                "    name        CHAR,\n" +
                "    category_id INTEGER UNIQUE,\n" +
                "    _id INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_barcode_info = "CREATE TABLE IF NOT EXISTS barcode_info (\n" +//商品档案
                "    points_max_money REAL    DEFAULT (0),\n" +
                "    img_url  VARCHAR,\n" +
                "    stock_unit_name  VARCHAR,\n" +
                "    stock_unit_id    INTEGER,\n" +
                "    update_price     INTEGER DEFAULT (0),\n" +
                "    conversion       INTEGER DEFAULT (1),\n" +
                "    attr_code        VARCHAR,\n" +
                "    attr_name        VARCHAR,\n" +
                "    attr_id          INTEGER,\n" +
                "    brand_code        VARCHAR,\n" +
                "    brand        VARCHAR,\n" +
                "    brand_id          INTEGER,\n" +
                "    gs_name        VARCHAR,\n" +
                "    gs_code        VARCHAR,\n" +
                "    gs_id          INTEGER,\n" +
                "    image            VARCHAR,\n" +
                "    mnemonic_code    VARCHAR,\n" +
                "    yh_price         REAL    DEFAULT (0.0),\n" +
                "    yh_mode          INTEGER DEFAULT (0),\n" +
                "    tc_rate          REAL    DEFAULT (0),\n" +
                "    tc_mode          INTEGER,\n" +
                "    tax_rate         INTEGER,\n" +
                "    category_id      INTEGER,\n" +
                "    barcode_status   INTEGER DEFAULT (1),\n" +
                "    goods_tare       INTEGER,\n" +
                "    type             INTEGER,\n" +
                "    origin           VARCHAR,\n" +
                "    goods_status     INTEGER,\n" +
                "    shelf_life       VARCHAR,\n" +
                "    metering_id      INTEGER,\n" +
                "    category_name    VARCHAR,\n" +
                "    specifi          VARCHAR,\n" +
                "    unit_name        VARCHAR,\n" +
                "    unit_id          INTEGER,\n" +
                "    ps_price         REAL,\n" +
                "    cost_price       REAL,\n" +
                "    trade_price      REAL,\n" +
                "    buying_price     REAL,\n" +
                "    retail_price     REAL,\n" +
                "    only_coding      VARCHAR,\n" +
                "    goods_title      VARCHAR,\n" +
                "    barcode          VARCHAR,\n" +
                "    barcode_id       INTEGER UNIQUE,\n" +
                "    goods_id         INTEGER,\n" +
                "    bi_id            INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                "                             UNIQUE\n" +
                ");\n",sql_pay_method = "CREATE TABLE IF NOT EXISTS pay_method (\n" +//付款方式
                "    is_enable         INTEGER DEFAULT (1),\n" +
                "    support           TEXT  ,\n" +
                "    is_open           INTEGER DEFAULT (1),\n" +
                "    rule              TEXT,\n" +
                "    unified_pay_query TEXT,\n" +
                "    unified_pay_order TEXT,\n" +
                "    wr_btn_img        TEXT,\n" +
                "    is_scan           INTEGER DEFAULT (2),\n" +
                "    is_cardno         INTEGER     DEFAULT (1),\n" +
                "    is_show_client    INTEGER,\n" +
                "    master_img        TEXT,\n" +
                "    pay_img           TEXT,\n" +
                "    xtype             TEXT,\n" +
                "    sort              INTEGER,\n" +
                "    shortcut_key      TEXT,\n" +
                "    is_check          INTEGER,\n" +
                "    remark            TEXT,\n" +
                "    status            INTEGER,\n" +
                "    name              TEXT,\n" +
                "    pay_method_id     INTEGER PRIMARY KEY NOT NULL\n" +
                ");\n",sql_local_parameter= "CREATE TABLE IF NOT EXISTS local_parameter (\n" +//本地参数
                "    parameter_id      VARCHAR (20) NOT NULL,\n" +
                "    parameter_content TEXT,\n" +
                "    parameter_desc VARCHAR(50),\n" +
                "    PRIMARY KEY (\n" +
                "        parameter_id\n" +
                "    )\n" +
                ");",sql_cashier_info = "CREATE TABLE IF NOT EXISTS cashier_info (\n" +//收银员
                "    authority     VARCHAR,\n" +
                "    remark     VARCHAR,\n" +
                "    pt_user_cname VARCHAR,\n" +
                "    pt_user_id    VARCHAR,\n" +
                "    is_put        INTEGER DEFAULT (0),\n" +
                "    is_give       INTEGER DEFAULT (1),\n" +
                "    is_refund     INT     DEFAULT (2),\n" +
                "    min_discount  REAL,\n" +
                "    cas_status    INTEGER,\n" +
                "    cas_phone     CHAR,\n" +
                "    cas_code      CHAR,\n" +
                "    cas_addtime   INT,\n" +
                "    cas_pwd       VARCHAR,\n" +
                "    cas_account   VARCHAR,\n" +
                "    cas_name      VARCHAR,\n" +
                "    stores_name   VARCHAR,\n" +
                "    stores_id     INT,\n" +
                "    cas_id        INTEGER PRIMARY KEY\n" +
                "                          UNIQUE\n" +
                ");",sql_refund_order = "CREATE TABLE IF NOT EXISTS refund_order (\n" +//退单
                "    refund_total    REAL    DEFAULT (0),\n" +
                "    ok_cashier_name VARCHAR,\n" +
                "    ok_cashier_id   INT,\n" +
                "    cashier_name    VARCHAR,\n" +
                "    member_id       INT,\n" +
                "    transfer_time   INT,\n" +
                "    transfer_status INT     DEFAULT (1),\n" +
                "    remark          VARCHAR,\n" +
                "    card_code       CHAR,\n" +
                "    name            CHAR,\n" +
                "    mobile          CHAR,\n" +
                "    refund_ment     INT,\n" +
                "    is_rk           INT     DEFAULT (1),\n" +
                "    upload_time     INT,\n" +
                "    upload_status   INT     DEFAULT (1),\n" +
                "    order_status    INT     DEFAULT (1),\n" +
                "    pos_code        VARCHAR,\n" +
                "    addtime         INT,\n" +
                "    cashier_id      INT,\n" +
                "    member_card     CHAR,\n" +
                "    type            INT,\n" +
                "    total           REAL,\n" +
                "    order_code      VARCHAR,\n" +
                "    ro_code         VARCHAR,\n" +
                "    stores_id       INT,\n" +
                "    ro_id           INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_refund_order_goods = "CREATE TABLE IF NOT EXISTS refund_order_goods (\n" +//退单商品明细
                "    produce_date INT,\n" +
                "    conversion   REAL,\n" +
                "    is_rk        INT     DEFAULT (2),\n" +
                "    unit_name    CHAR,\n" +
                "    barcode      VARCHAR,\n" +
                "    goods_title  VARCHAR,\n" +
                "    rog_id       INT,\n" +
                "    refund_price REAL,\n" +
                "    refund_num  REAL,\n" +
                "    price        REAL,\n" +
                "    xnum         REAL,\n" +
                "    barcode_id   INT,\n" +
                "    ro_code      VARCHAR,\n" +
                "    ro_id        INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_refund_order_pays = "CREATE TABLE IF NOT EXISTS refund_order_pays (\n" +//退单付款明细
                "    road_pay_status INT     DEFAULT (1),\n" +
                "    pay_method_name CHAR,\n" +
                "    is_check        INT,\n" +
                "    remark          VARCHAR,\n" +
                "    pay_code        VARCHAR,\n" +
                "    pay_serial_no   VARCHAR,\n" +
                "    pay_status      INT     DEFAULT (1),\n" +
                "    pay_time        INT,\n" +
                "    pay_money       REAL,\n" +
                "    pay_method      INT,\n" +
                "    ro_code         CHAR,\n" +
                "    pay_id          INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_retail_order = "CREATE TABLE IF NOT EXISTS retail_order (\n" +//销售单
                "    zk_cashier_id   INTEGER,\n" +
                "    remark          VARCHAR,\n" +
                "    ss_money        REAL    DEFAULT (0),\n" +
                "    zl_money        REAL    DEFAULT (0),\n" +
                "    spare_param2    VARCHAR,\n" +
                "    spare_param1    VARCHAR,\n" +
                "    discount_money  REAL    DEFAULT (0),\n" +
                "    member_id       INT,\n" +
                "    sc_tc_money     REAL,\n" +
                "    sc_ids          VARCHAR,\n" +
                "    card_code       CHAR,\n" +
                "    name            CHAR,\n" +
                "    mobile          CHAR,\n" +
                "    integral_info          VARCHAR,\n" +
                "    is_rk           INT     DEFAULT (2),\n" +
                "    transfer_time   INT,\n" +
                "    transfer_status INT     DEFAULT (1),\n" +
                "    upload_time     INT,\n" +
                "    upload_status   INT     DEFAULT (1),\n" +
                "    pay_time        INT,\n" +
                "    pay_status      INT     DEFAULT (1),\n" +
                "    order_status    INT     DEFAULT (1),\n" +
                "    pos_code        VARCHAR,\n" +
                "    addtime         INT,\n" +
                "    cashier_id      INT,\n" +
                "    total           REAL    DEFAULT (0),\n" +
                "    discount_price  REAL    DEFAULT (0),\n" +
                "    discount  REAL    DEFAULT (0),\n" +
                "    order_code      VARCHAR,\n" +
                "    stores_id       INT,\n" +
                "    order_id        INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_retail_order_goods = "CREATE TABLE IF NOT EXISTS retail_order_goods (\n" +//销售商品明细
                "    y_price       REAL,\n" +
                "    barcode       VARCHAR,\n" +
                "    "+ GoodsInfoViewAdapter.W_G_MARK +"       VARCHAR,\n" +
                "    conversion    INT     DEFAULT (1),\n" +
                "    total_money   REAL,\n" +
                "    zk_cashier_id INT,\n" +
                "    gp_id         INT,\n" +
                "    tc_rate       REAL,\n" +
                "    tc_mode       INT,\n" +
                "    tax_rate      REAL,\n" +
                "    ps_price      REAL,\n" +
                "    cost_price    REAL,\n" +
                "    trade_price   REAL,\n" +
                "    retail_price  REAL,\n" +
                "    buying_price  REAL,\n" +
                "    price         REAL,\n" +
                "    xnum          REAL,\n" +
                "    barcode_id    INTEGER,\n" +
                "    order_code    VARCHAR,\n" +
                "    rog_id        INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_retail_order_pays = "CREATE TABLE IF NOT EXISTS retail_order_pays (\n" +//销售付款明细
                "    print_info        VARCHAR,\n" +
                "    v_num        VARCHAR,\n" +//支付方式凭证号
                "    return_code       VARCHAR,\n" +
                "    card_no           VARCHAR,\n" +
                "    xnote             VARCHAR,\n" +
                "    discount_money    REAL    DEFAULT (0),\n" +
                "    give_change_money REAL    DEFAULT (0),\n" +
                "    pre_sale_money    REAL    DEFAULT (0),\n" +
                "    zk_money          REAL    DEFAULT (0),\n" +
                "    is_check          INT,\n" +
                "    remark            VARCHAR,\n" +
                "    pay_code          CHAR,\n" +
                "    pay_serial_no     CHAR,\n" +
                "    pay_status        INT     DEFAULT (1),\n" +
                "    pay_time          INTEGER,\n" +
                "    pay_money         REAL    DEFAULT (0),\n" +
                "    pay_method        INT,\n" +
                "    order_code        CHAR,\n" +
                "    pay_id            INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_goods_group = "CREATE TABLE IF NOT EXISTS goods_group (\n" +
                "    img_url         VARCHAR,\n" +
                "    stores_id     INT,\n" +
                "    type          INT,\n" +
                "    unit_name     VARCHAR,\n" +
                "    addtime       INT,\n" +
                "    status        INT,\n" +
                "    gp_price      REAL,\n" +
                "    gp_title      VARCHAR,\n" +
                "    gp_code       CHAR,\n" +
                "    gp_id         INTEGER PRIMARY KEY\n" +
                "                          UNIQUE,\n" +
                "    mnemonic_code VARCHAR\n" +
                ");",sql_goods_group_info = "CREATE TABLE IF NOT EXISTS goods_group_info (\n" +
                "    xnum       INT,\n" +
                "    barcode_id INT,\n" +
                "    gp_id      INT,\n" +
                "    _id        INT UNIQUE\n" +
                ");",goods_group_view = "CREATE VIEW IF NOT EXISTS vi_goods_group_info AS\n" +//组合商品视图
                "    SELECT b.*,\n" +
                "           a.xnum,\n" +
                "           c.*\n" +
                "      FROM goods_group_info a\n" +
                "           LEFT JOIN\n" +
                "           barcode_info b\n" +
                "           LEFT JOIN\n" +
                "           goods_group c\n" +
                "     WHERE a.barcode_id = b.barcode_id AND \n" +
                "           c.gp_id = a.gp_id AND \n" +
                "           c.status = 1;\n",
                sql_hangbill = "CREATE TABLE IF NOT EXISTS hangbill (\n" +
                "_id   INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "hang_id   INT  NOT NULL UNIQUE,\n" +
                "amt      NUMERIC (18, 4),\n" +
                "stores_id     INT,\n" +
                "cas_id     INT,\n" +
                "cas_name     VARCHAR,\n" +
                "pos_code     VARCHAR,\n" +
                "card_code     VARCHAR,\n" +
                "vip_name     VARCHAR,\n" +
                "vip_mobile     VARCHAR,\n" +
                "oper_date   DATETIME NOT NULL DEFAULT ( (datetime('now', 'localtime'))));",
                sql_hangbill_detail = "CREATE TABLE IF NOT EXISTS hangbill_detail (\n" +
                "_id     INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "hang_id  INT NOT NULL,\n" +
                "row_id  INT  NOT NULL,\n" +
                "stores_id       VARCHAR (4)     NOT NULL,\n" +
                "gp_id  INT     NOT NULL,\n" +
                "barcode_id  INT     NOT NULL,\n" +
                "barcode  VARCHAR     NOT NULL,\n" +
                "only_coding  INT     NOT NULL,\n" +
                "    "+ GoodsInfoViewAdapter.W_G_MARK +"       VARCHAR,\n" +
                "goods_title  VARCHAR      ,\n" +
                "original_price   NUMERIC (18, 4) NOT NULL,\n" +
                "sale_price     NUMERIC (18, 4) NOT NULL,\n" +
                "xnum      NUMERIC (18, 4) NOT NULL,\n" +
                "unit_name      VARCHAR NOT NULL,\n" +
                "sale_amt     NUMERIC (18, 4) NOT NULL,\n" +
                "discount  NUMERIC (18, 4) NOT NULL,\n" +
                "discount_amt NUMERIC (18, 4) NOT NULL,\n" +
                "" + GoodsInfoViewAdapter.SALE_TYPE +"  INT     NOT NULL,\n" +
                "sale_man          VARCHAR (4),\n" +
                "cas_id        VARCHAR (4)     NOT NULL,\n" +
                "remark         VARCHAR (50),\n" +
                "oper_date      DATETIME        NOT NULL DEFAULT ( (datetime('now', 'localtime') )) );",
                sql_barcode_scale_info = "CREATE TABLE IF NOT EXISTS barcode_scalse_info (\n" +
                "    _id         INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                "                        NOT NULL,\n" +
                "    s_manufacturer VARCHAR NOT NULL,\n" +
                "    s_class_id VARCHAR NOT NULL,\n" +
                "    s_product_t VARCHAR NOT NULL,\n" +
                "    scale_ip    VARCHAR NOT NULL,\n" +
                "    scale_port  INTEGER NOT NULL,\n" +
                "    g_c_id      VARCHAR NOT NULL,\n" +
                "    g_c_name    VARCHAR NOT NULL,\n" +
                "    remark      VARCHAR\n" +
                ");",sql_discount_record = "CREATE TABLE IF NOT EXISTS discount_record (\n" +
                "    order_code     VARCHAR,\n" +
                "    discount_type  INTEGER  DEFAULT (0),\n" +
                "    type           INTEGER  DEFAULT (1),\n" +
                "    stores_id      INTEGER,\n" +
                "    relevant_id    INTEGER,\n" +
                "    discount_money REAL     DEFAULT (0.0),\n" +
                "    details        VARCHAR,\n" +
                "    oper_date      DATETIME DEFAULT ( (datetime('now', 'localtime') ) ),\n" +
                "    PRIMARY KEY (\n" +
                "        order_code,\n" +
                "        discount_type\n" +
                "    )\n" +
                ");",sql_member_order_info = "CREATE TABLE IF NOT EXISTS member_order_info (\n" +
                "    xnote           VARCHAR,\n" +
                "    addtime         INTEGER,\n" +
                "    transfer_status INTEGER DEFAULT (1),\n" +
                "    status          INTEGER,\n" +
                "    cashier_id      INTEGER,\n" +
                "    stores_id       INTEGER,\n" +
                "    name            VARCHAR,\n" +
                "    mobile          VARCHAR,\n" +
                "    card_code       VARCHAR,\n" +
                "    pay_method_id   INTEGER,\n" +
                "    order_money     REAL,\n" +
                "    give_money     REAL DEFAULT (0.0),\n" +
                "    order_code      VARCHAR,\n" +
                "    third_order_id  VARCHAR,\n" +
                "    member_id       INTEGER NOT NULL,\n" +
                "    _id             INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    UNIQUE (\n" +
                "        third_order_id,\n" +
                "        member_id\n" +
                "    )\n" +
                ");",sql_transfer_info = "CREATE TABLE IF NOT EXISTS transfer_info (\n" + //交班总信息
                "    sj_money       REAL    DEFAULT (0),\n" +
                "    cards_num      INTEGER,\n" +
                "    cards_money    REAL,\n" +
                "    order_money    REAL    DEFAULT (0),\n" +
                "    order_e_date   INTEGER DEFAULT (0),\n" +
                "    order_b_date   INTEGER DEFAULT (0),\n" +
                "    recharge_num   INTEGER DEFAULT (0),\n" +
                "    recharge_money REAL    DEFAULT (0),\n" +
                "    refund_num     INTEGER DEFAULT (0),\n" +
                "    refund_money   REAL    DEFAULT (0),\n" +
                "    cashbox_money       REAL    DEFAULT (0),\n" +
                "    sum_money      REAL    DEFAULT (0),\n" +
                "    ti_code        VARCHAR,\n" +
                "    upload_time    INTEGER DEFAULT (0),\n" +
                "    upload_status  INT     DEFAULT (1),\n" +
                "    transfer_time  INTEGER DEFAULT (0),\n" +
                "    order_num      REAL,\n" +
                "    cas_id         INTEGER,\n" +
                "    stores_id      INTEGER,\n" +
                "    ti_id          INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ")",sql_transfer_money_info = "CREATE TABLE IF NOT EXISTS transfer_money_info (\n" +//交班零售信息
                "    order_num  INTEGER DEFAULT (0),\n" +
                "    pay_money  REAL,\n" +
                "    pay_method INTEGER,\n" +
                "    ti_code    VARCHAR,\n" +
                "    ti_id      INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ");\n",sql_transfer_once_cardsc = "CREATE TABLE IF NOT EXISTS transfer_once_cardsc (\n" +//交班次卡信息
                "    ti_id      INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                "                       UNIQUE,\n" +
                "    ti_code    INTEGER,\n" +
                "    pay_method INTEGER,\n" +
                "    pay_money  REAL,\n" +
                "    order_num  INTEGER\n" +
                ")",sql_transfer_order = "CREATE TABLE IF NOT EXISTS transfer_order (\n" +//交班各类型单号信息
                "    cas_id     INTEGER,\n" +
                "    status     INTEGER DEFAULT (1),\n" +
                "    order_code VARCHAR,\n" +
                "    ti_code    VARCHAR,\n" +
                "    ti_id      INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                ")",sql_transfer_recharge_money= "CREATE TABLE IF NOT EXISTS transfer_recharge_money (\n" +//交班充值信息
                "    order_num  INTEGER DEFAULT (0),\n" +
                "    _id        INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                "                       NOT NULL\n" +
                "                       UNIQUE,\n" +
                "    ti_code    INTEGER,\n" +
                "    pay_method INTEGER,\n" +
                "    pay_money  REAL\n" +
                ");\n",sql_transfer_refund_money = "CREATE TABLE IF NOT EXISTS transfer_refund_money (\n" +
                "    order_num  INTEGER DEFAULT (0),\n" +
                "    _id        INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                "                       NOT NULL\n" +
                "                       UNIQUE,\n" +
                "    ti_code    INTEGER,\n" +
                "    pay_method INTEGER,\n" +
                "    pay_money  REAL\n" +
                ");\n",sql_once_cards = "CREATE TABLE IF NOT EXISTS once_cards (\n" +
                "    member_grade_name VARCHAR,\n" +
                "    member_card       VARCHAR,\n" +
                "    member_name       VARCHAR,\n" +
                "    member_mobile     VARCHAR,\n" +
                "    cards_num         INTEGER,\n" +
                "    order_money       REAL,\n" +
                "    sales_id          INTEGER,\n" +
                "    order_status      INTEGER DEFAULT (1),\n" +
                "    xnote             TEXT,\n" +
                "    transfer_time     INTEGER DEFAULT (0),\n" +
                "    transfer_status   INTEGER DEFAULT (1),\n" +
                "    addtime           INTEGER DEFAULT (0),\n" +
                "    stores_id         INTEGER,\n" +
                "    cashier_id        INTEGER,\n" +
                "    pay_status        INTEGER DEFAULT (1),\n" +
                "    order_code        VARCHAR,\n" +
                "    cards_id          INTEGER PRIMARY KEY AUTOINCREMENT\n" +
                "                              UNIQUE\n" +
                ")",sql_fullreduce_info = "CREATE TABLE IF NOT EXISTS fullreduce_info (\n" +//阶梯满减
                "    full_id    VARCHAR PRIMARY KEY\n" +
                "                       NOT NULL,\n" +
                "    title      VARCHAR,\n" +
                "    modes      INTEGER,\n" +
                "    fold       INTEGER,\n" +
                "    rule       TEXT,\n" +
                "    start_time VARCHAR,\n" +
                "    end_time   VARCHAR,\n" +
                "    starttime  NUMERIC,\n" +
                "    endtime    NUMERIC\n" +
                ");",sql_fullreduce_info_new = "CREATE TABLE fullreduce_info_new (\n" +//新满减
                "    tlp_id             INTEGER PRIMARY KEY\n" +
                "                               NOT NULL,\n" +
                "    tlpb_id             INTEGER ," +
                "    title           VARCHAR,\n" +
                "    promotion_type     INTEGER,\n" +
                "    type_detail_id     INTEGER,\n" +
                "    promotion_object   INTEGER,\n" +
                "    promotion_grade_id INTEGER,\n" +
                "    cumulation_give    INTEGER,\n" +
                "    buyfull_money      REAL,\n" +
                "    reduce_money       REAL,\n" +
                "    start_date         INTEGER,\n" +
                "    end_date           INTEGER,\n" +
                "    promotion_week     VARCHAR,\n" +
                "    begin_time         VARCHAR,\n" +
                "    end_time           VARCHAR,\n" +
                "    status             INTEGER,\n" +
                "    xtype              INTEGER\n" +
                ");\n",sql_promotion_info = "CREATE TABLE IF NOT EXISTS promotion_info (\n" +//零售特价促销
                "    tlp_id        INTEGER PRIMARY KEY\n" +
                "                            NOT NULL,\n" +
                "    tlpb_id      INTEGER,\n" +
                "    barcode_id      INTEGER,\n" +
                "    type_detail_id      INTEGER,\n" +
                "    status          INTEGER,\n" +
                "    way             INTEGER,\n" +
                "    limit_xnum      REAL,\n" +
                "    promotion_object INTEGER,\n" +
                "    promotion_grade_id INTEGER,\n" +
                "    promotion_type INTEGER,\n" +
                "    promotion_price REAL,\n" +
                "    stores_id       INTEGER,\n" +
                "    start_date      VARCHAR,\n" +
                "    end_date        VARCHAR,\n" +
                "    promotion_week  VARCHAR,\n" +
                "    begin_time      VARCHAR,\n" +
                "    end_time        VARCHAR,\n" +
                "    xtype           INTEGER\n" +
                ");\n",sql_step_promotion_info = "CREATE TABLE IF NOT EXISTS step_promotion_info (\n" +//零售阶梯促销
                "    tlp_id        INTEGER PRIMARY KEY\n" +
                "                            NOT NULL,\n" +
                "    tlpb_id      INTEGER,\n" +
                "    type_detail_id      INTEGER,\n" +
                "    status          INTEGER,\n" +
                "    way             INTEGER,\n" +
                "    promotion_object INTEGER,\n" +
                "    promotion_grade_id INTEGER,\n" +
                "    promotion_type INTEGER,\n" +
                "    xnum_one REAL,\n" +
                "    promotion_price_one REAL,\n" +
                "    xnum_two REAL,\n" +
                "    promotion_price_two REAL,\n" +
                "    xnum_three REAL,\n" +
                "    promotion_price_three REAL,\n" +
                "    xnum_four REAL,\n" +
                "    promotion_price_four REAL,\n" +
                "    xnum_five REAL,\n" +
                "    promotion_price_five REAL,\n" +
                "    stores_id       INTEGER,\n" +
                "    start_date      VARCHAR,\n" +
                "    end_date        VARCHAR,\n" +
                "    promotion_week  VARCHAR,\n" +
                "    begin_time      VARCHAR,\n" +
                "    end_time        VARCHAR,\n" +
                "    xtype           INTEGER\n" +
                ");\n",sql_buy_x_give_x = "CREATE TABLE IF NOT EXISTS buy_x_give_x (\n" +
                "    tlp_id             INTEGER PRIMARY KEY\n" +
                "                               NOT NULL,\n" +
                "    tlpb_id     INTEGER," +
                "    promotion_type     INTEGER,\n" +
                "    promotion_object   INTEGER,\n" +
                "    promotion_grade_id INTEGER,\n" +
                "    cumulation_give    INTEGER,\n" +
                "    xnum_buy    REAL,\n" +
                "    xnum_give    REAL,\n" +
                "    markup_price    REAL,\n" +
                "    barcode_id   INTEGER,\n" +
                "    barcode_id_give   INTEGER,\n" +
                "    start_date         INTEGER,\n" +
                "    end_date           INTEGER,\n" +
                "    promotion_week     VARCHAR,\n" +
                "    begin_time         VARCHAR,\n" +
                "    end_time           VARCHAR,\n" +
                "    status             INTEGER,\n" +
                "    xtype              INTEGER\n" +
                ");\n",sql_buyfull_give_x = "CREATE TABLE IF NOT EXISTS buyfull_give_x (\n" +
                "    tlp_id             INTEGER PRIMARY KEY\n" +
                "                               NOT NULL,\n" +
                "    tlpb_id     INTEGER,                  \n" +
                "    title     VARCHAR,\n" +
                "    type_detail_id     INTEGER,\n" +
                "    promotion_type     INTEGER,\n" +
                "    promotion_object   INTEGER,\n" +
                "    promotion_grade_id INTEGER,\n" +
                "    cumulation_give    INTEGER,\n" +
                "    fullgive_way    INTEGER,\n" +
                "    give_way    INTEGER,\n" +
                "    item_discount    INTEGER,\n" +
                "    buyfull_money    REAL,\n" +
                "    givex_goods_info  VARCHAR,\n" +
                "    start_date         INTEGER,\n" +
                "    end_date           INTEGER,\n" +
                "    promotion_week     VARCHAR,\n" +
                "    begin_time         VARCHAR,\n" +
                "    end_time           VARCHAR,\n" +
                "    status             INTEGER,\n" +
                "    xtype              INTEGER\n" +
                ");";
        list.add(sql_shop_stores);
        list.add(sql_shop_category);
        list.add(sql_barcode_info);
        list.add(sql_pay_method);
        list.add(sql_local_parameter);
        list.add(sql_cashier_info);
        list.add(sql_refund_order);
        list.add(sql_refund_order_goods);
        list.add(sql_refund_order_pays);
        list.add(sql_retail_order);
        list.add(sql_retail_order_goods);
        list.add(sql_retail_order_pays);
        list.add(sql_goods_group);
        list.add(sql_goods_group_info);
        list.add(goods_group_view);
        list.add(sql_hangbill);
        list.add(sql_hangbill_detail);
        list.add(sql_barcode_scale_info);
        list.add(sql_discount_record);
        list.add(sql_member_order_info);

        list.add(sql_transfer_info);
        list.add(sql_transfer_money_info);
        list.add(sql_transfer_once_cardsc);
        list.add(sql_transfer_order);
        list.add(sql_transfer_recharge_money);
        list.add(sql_transfer_refund_money);
        list.add(sql_once_cards);
        list.add(sql_fullreduce_info);
        list.add(sql_promotion_info);
        list.add(sql_step_promotion_info);
        list.add(sql_fullreduce_info_new);
        list.add(sql_buy_x_give_x);
        list.add(sql_buyfull_give_x);

        try {
            db.beginTransaction();
            for (String sql : list) {
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
