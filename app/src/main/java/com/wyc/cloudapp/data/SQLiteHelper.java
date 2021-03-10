package com.wyc.cloudapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Administrator on 2018-03-27.
 */

public final class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 6;
    private static volatile SQLiteDatabase mDb;

    private SQLiteHelper(Context context,final String databaseName){
        super(context, databaseName, null, DATABASE_VERSION);
        Logger.d("DATABASE_NAME:%s",databaseName);
    }

    public static void initDb(Context context, final String storesId){
        if (mDb == null){
            synchronized (SQLiteHelper.class){
                if (mDb == null){
                    try {
                        //数据库名称order_门店编号
                        final String databaseName = String.format(Locale.CHINA,"%sorder_%s",Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/"
                                ,storesId);
                        final SQLiteHelper sqLiteHelper = new SQLiteHelper(context,databaseName);
                        mDb = sqLiteHelper.getWritableDatabase();
                    }catch (SQLiteCantOpenDatabaseException e){
                        CustomApplication.execute(()-> {
                            Looper.prepare();
                            MyDialog.ToastMessage("打开数据库错误：" + e.getLocalizedMessage(),context,null);
                            Looper.loop();
                        });
                        SystemClock.sleep(3000);
                        CustomApplication.self().exit();
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initTables(db);//初始化数据库
        onUpgrade(db,0,0);
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
                ");";
        update_list.add(sales_info_sql);
        update_list.add(sale_operator_info_sql);

        //修改
        if(!checkColumnExists(db,"member_order_info","sc_id")){
            modify_list.add("ALTER TABLE member_order_info ADD COLUMN sc_id  VARCHAR");
        }
        if(!checkColumnExists(db,"member_order_info","order_type")){//1 充值单  2 会员退款单
            modify_list.add("ALTER TABLE member_order_info ADD COLUMN order_type  INTEGER DEFAULT (1)");
        }
        if(!checkColumnExists(db,"member_order_info","origin_order_code")){
            modify_list.add("ALTER TABLE member_order_info ADD COLUMN origin_order_code  VARCHAR");
        }
        if(!checkColumnExists(db,"pay_method","is_moling")){
            modify_list.add("ALTER TABLE pay_method ADD COLUMN is_moling INTEGER DEFAULT (1)");
        }
        if(!checkColumnExists(db,"shop_stores","wh_id")){
            modify_list.add("ALTER TABLE shop_stores ADD COLUMN wh_id");
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

    public static boolean backupDB(final String new_name,final StringBuilder err) {
        final String file_absolute_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File db = new File(file_absolute_path + "/hzYunPos/");
        boolean code = false;
        try {
            zipFile(db,file_absolute_path + File.separator + new_name + ".zip");
            Utils.deleteFile(db);
            closeDB();
            code = true;
        } catch (IOException e) {
            e.printStackTrace();
            if (err != null)err.append(e.getMessage());
        }
        return code;
    }

    private static void zipFile(File dbFile, final String backup_name) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream( new FileOutputStream(backup_name));) {
            zip(dbFile.listFiles(),"",out);
        }
    }
    private static void zip(File[] files, String baseFolder, ZipOutputStream zos)throws IOException {
        if (files != null){
            ZipEntry entry = null;
            int count = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    zip(file.listFiles(), file.getName() + File.separator, zos);
                    continue;
                }
                entry = new ZipEntry(baseFolder + file.getName());

                zos.putNextEntry(entry);

                try( FileInputStream fis = new FileInputStream(file);) {
                    final byte[] buffer = new byte[1024];
                    while ((count = fis.read(buffer, 0, buffer.length)) != -1)
                        zos.write(buffer, 0, count);
                }
            }
        }
    }


    private static boolean checkColumnExists(SQLiteDatabase db,String tableName, String columnName) throws SQLiteException {
        boolean result = false ;
        Cursor cursor = null ;
        try{
            cursor = db.rawQuery( "select 1 from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName , "%" + columnName + "%"} );
            result = null != cursor && cursor.moveToFirst() ;
        }finally{
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
        return result ;
    }

    private static boolean execSQLByBatchFromJson(@NonNull JSONArray jsonArray, String table, StringBuilder err, int type) {
        //type 0 insert 1 replace

        boolean isTrue = true;
        JSONObject jsonObject;
        SQLiteStatement statement = null;
        int columnN0 = 0;
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
        JSONObject jsonObject = null;
        SQLiteStatement statement = null;
        int columnN0 = 0;
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
                        if (null == value || "".equals(value)){
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
                mDb.close();
                mDb = null;
                Logger.d("mDb closed...");
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
                if (err != null)err.append(e.getMessage());
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
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                result = rs2Txt(cursor);
            } catch (SQLiteException e) {
                if (err != null)err.append(e.getMessage());
                result = null;
                e.printStackTrace();
            }
        }
        return result;
    }
    public static JSONArray getListContentValues(@NonNull String sql,Integer minrow,Integer maxrow,boolean row,StringBuilder err){
        JSONArray array;
        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                array = rs2ContentValues(cursor,minrow,maxrow,row);
            } catch (SQLiteException e) {
                if (err != null)err.append(e.getMessage());
                e.printStackTrace();
                array=null;
            }
        }
        return array;
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

        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();

        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
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
    public static int execUpdateSql(@NonNull final String table,final ContentValues values,final String whereClause,final String[] whereArgs,StringBuilder err){
        int code = -1;
        try {
            synchronized (SQLiteHelper.class){
                code = mDb.update(table,values,whereClause,whereArgs);
            }
        }catch (SQLiteException e){
            e.printStackTrace();
            code = -1;
            if (err != null)err.append(e.getMessage());
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
            if (err != null)err.append(String.format(Locale.CHINA,"更新表数量tables：%d 不等于 更新值数量values：%d",tables.size(),values.size()));
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
            if (err != null)err.append(e.getMessage());
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
            if (err != null)err.append(e.getMessage());
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
            if (err != null)err.append(e.getMessage());
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
    };
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
        final ArrayList<String> colNames=new ArrayList<String>();
        final ArrayList<Integer> coltypes=new ArrayList<Integer>();
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
        final ArrayList<String> colNames=new ArrayList<String>();
        final ArrayList<Integer> coltypes=new ArrayList<Integer>();
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
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            jsonObj.put(colNames.get(i) , cursor.getInt(i));
                        }else {
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
                    } else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {
                        jsonObj.put(colNames.get(i), cursor.getInt(i));
                    } else {
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
                if (row_count > minRow && row_count <= maxRow){
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
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
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
                "    is_enable         VARCHAR DEFAULT (1),\n" +
                "    support           VARCHAR  ,\n" +
                "    is_open           INTEGER DEFAULT (1),\n" +
                "    rule              VARCHAR,\n" +
                "    unified_pay_query VARCHAR,\n" +
                "    unified_pay_order VARCHAR,\n" +
                "    wr_btn_img        VARCHAR,\n" +
                "    is_scan           INTEGER DEFAULT (2),\n" +
                "    is_cardno         INT     DEFAULT (1),\n" +
                "    is_show_client    INT,\n" +
                "    master_img        VARCHAR,\n" +
                "    pay_img           VARCHAR,\n" +
                "    xtype             CHAR,\n" +
                "    sort              INT,\n" +
                "    shortcut_key      CHAR,\n" +
                "    is_check          INTEGER,\n" +
                "    remark            VARCHAR,\n" +
                "    status            INT,\n" +
                "    name              CHAR,\n" +
                "    pay_method_id     INTEGER PRIMARY KEY\n" +
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
                ")",sql_fullreduce_info = "CREATE TABLE IF NOT EXISTS fullreduce_info (\n" +
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
                ");",sql_promotion_info = "CREATE TABLE IF NOT EXISTS promotion_info (\n" +//零售特价促销
                "    tlp_id        INTEGER PRIMARY KEY\n" +
                "                            NOT NULL,\n" +
                "    tlpb_id      INTEGER,\n" +
                "    barcode_id      INTEGER,\n" +
                "    type_detail_id      INTEGER,\n" +
                "    status          INTEGER,\n" +
                "    way             INTEGER,\n" +
                "    limit_xnum      INTEGER,\n" +
                "    promotion_object INTEGER,\n" +
                "    promotion_grade_id INTEGER,\n" +
                "    promotion_type INTEGER,\n" +
                "    promotion_price NUMERIC,\n" +
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
                "    xnum_one NUMERIC,\n" +
                "    promotion_price_one NUMERIC,\n" +
                "    xnum_two NUMERIC,\n" +
                "    promotion_price_two NUMERIC,\n" +
                "    xnum_three NUMERIC,\n" +
                "    promotion_price_three NUMERIC,\n" +
                "    xnum_four NUMERIC,\n" +
                "    promotion_price_four NUMERIC,\n" +
                "    xnum_five NUMERIC,\n" +
                "    promotion_price_five NUMERIC,\n" +
                "    stores_id       INTEGER,\n" +
                "    start_date      VARCHAR,\n" +
                "    end_date        VARCHAR,\n" +
                "    promotion_week  VARCHAR,\n" +
                "    begin_time      VARCHAR,\n" +
                "    end_time        VARCHAR,\n" +
                "    xtype           INTEGER\n" +
                ");\n";

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
