package com.wyc.cloudapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Administrator on 2018-03-27.
 */

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/yunPos/order.db";
    private static final int DATABASE_VERSION = 1;//记得修改软件版本
    private static final int MASTER_SOFTWRAW_VERSION = 1;
    private static SQLiteDatabase mDb;
    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initDatabase(db);//初始化数据库
        onUpgrade(db,0,0);

        //必须最后赋值
        mDb = db;
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure (SQLiteDatabase db){
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    private  boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) throws SQLiteException {
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

    public synchronized boolean execSQLByBatchInsertJson(@NonNull JSONArray jsonArray,String table,StringBuilder err) {
        boolean isTrue = true;
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();
        JSONObject jsonObject;
        String key;
        SQLiteStatement statement = null;
        Iterator iterator;
        int columnN0 = 0;
        if (jsonArray.length() == 0) {
            return false;
        }

        stringBuilderHead.append("INSERT INTO ");
        stringBuilderHead.append(table);
        stringBuilderHead.append(" (");
        stringBuilderfoot.append("VALUES (");

        jsonObject = jsonArray.optJSONObject(0);
        iterator = jsonObject.keys();
        while(iterator.hasNext()){
            key = (String) iterator.next();
            stringBuilderHead.append(key);
            stringBuilderHead.append(",");

            stringBuilderfoot.append("?");
            stringBuilderfoot.append(",");
        }
        stringBuilderHead.replace(stringBuilderHead.length() - 1,stringBuilderHead.length(),")");
        stringBuilderfoot.replace(stringBuilderfoot.length() - 1,stringBuilderfoot.length(),")");
        stringBuilderHead.append(stringBuilderfoot);


        try {
            mDb.beginTransaction();
            statement = mDb.compileStatement(stringBuilderHead.toString());
            for (int i = 0,len = jsonArray.length();i < len; i ++){
                columnN0 = 0;
                jsonObject = jsonArray.optJSONObject(i);
                iterator = jsonObject.keys();
                while(iterator.hasNext()){
                    key = (String) iterator.next();
                    if ("".equals(jsonObject.optString(key))){
                        statement.bindNull(++columnN0);
                    }else
                        statement.bindString(++columnN0,jsonObject.optString(key));
                }
                statement.executeInsert();
            }
            mDb.setTransactionSuccessful();
        } catch (SQLException e) {
            isTrue = false;
            err.append(e.getMessage());
            e.printStackTrace();
        } finally {
            if (statement != null){
                statement.close();
            }
            if (mDb != null){
                mDb.endTransaction();
            }
        }
        return isTrue;
    }

    public synchronized boolean execSQLByBatchReplaceJson(@NonNull JSONArray jsonArray, String table,StringBuilder err) {
        boolean isTrue = true;
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();
        JSONObject jsonObject;
        String key;
        SQLiteStatement statement = null;
        Iterator iterator;
        int columnN0 = 0;
        if (jsonArray.length() == 0) {
            return false;
        }

        stringBuilderHead.append("REPLACE INTO ");
        stringBuilderHead.append(table);
        stringBuilderHead.append(" (");
        stringBuilderfoot.append("VALUES (");

        jsonObject = jsonArray.optJSONObject(0);
        iterator = jsonObject.keys();
        while(iterator.hasNext()){
            key = (String) iterator.next();
            stringBuilderHead.append(key);
            stringBuilderHead.append(",");

            stringBuilderfoot.append("?");
            stringBuilderfoot.append(",");
        }
        stringBuilderHead.replace(stringBuilderHead.length() - 1,stringBuilderHead.length(),")");
        stringBuilderfoot.replace(stringBuilderfoot.length() - 1,stringBuilderfoot.length(),")");
        stringBuilderHead.append(stringBuilderfoot);

        try {
            mDb.beginTransaction();
            statement = mDb.compileStatement(stringBuilderHead.toString());
            for (int i = 0,len = jsonArray.length();i < len; i ++){
                columnN0 = 0;
                jsonObject = jsonArray.optJSONObject(i);
                iterator = jsonObject.keys();
                while(iterator.hasNext()){
                    key = (String) iterator.next();
                    if ("NULL".equals(jsonObject.optString(key))){
                        statement.bindNull(++columnN0);
                    }else
                        statement.bindString(++columnN0,jsonObject.optString(key));
                }
                statement.execute();
            }
            mDb.setTransactionSuccessful();
        } catch (SQLException e) {
            isTrue = false;
            err.append(e.getMessage());
            e.printStackTrace();
        } finally {
            if (statement != null){
                statement.close();
            }
            if (mDb != null){
                mDb.endTransaction();
            }
        }
        return isTrue;
    }

    public synchronized boolean execSQLByBatchReplaceJson(@NonNull JSONArray jsonArray, String table,String[] cls,StringBuilder err) {
        boolean isTrue = true;
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();
        JSONObject jsonObject;
        String key;
        SQLiteStatement statement = null;
        Iterator iterator;
        int columnN0 = 0;
        if (jsonArray.length() == 0) {
            return false;
        }


        stringBuilderHead.append("REPLACE INTO ");
        stringBuilderHead.append(table);
        stringBuilderHead.append(" (");
        stringBuilderfoot.append("VALUES (");

        if (cls != null && cls.length != 0){
            for (String cl:cls){
                stringBuilderHead.append(cl);
                stringBuilderHead.append(",");

                stringBuilderfoot.append("?");
                stringBuilderfoot.append(",");
            }
        }else{
            jsonObject = jsonArray.optJSONObject(0);
            iterator = jsonObject.keys();
            while(iterator.hasNext()){
                key = (String) iterator.next();
                stringBuilderHead.append(key);
                stringBuilderHead.append(",");

                stringBuilderfoot.append("?");
                stringBuilderfoot.append(",");
            }
        }
        stringBuilderHead.replace(stringBuilderHead.length() - 1,stringBuilderHead.length(),")");
        stringBuilderfoot.replace(stringBuilderfoot.length() - 1,stringBuilderfoot.length(),")");
        stringBuilderHead.append(stringBuilderfoot);

        try {
            mDb.beginTransaction();
            statement = mDb.compileStatement(stringBuilderHead.toString());
            for (int i = 0,len = jsonArray.length();i < len; i ++){
                columnN0 = 0;
                jsonObject = jsonArray.optJSONObject(i);
                if(cls != null && cls.length != 0){
                    for (String cl:cls){
                        if ("NULL".equals(jsonObject.optString(cl))){
                            statement.bindNull(++columnN0);
                        }else
                            statement.bindString(++columnN0,jsonObject.optString(cl));
                    }
                }else{
                    iterator = jsonObject.keys();
                    while(iterator.hasNext()){
                        key = (String) iterator.next();
                        if ("".equals(jsonObject.optString(key))){
                            statement.bindNull(++columnN0);
                        }else
                            statement.bindString(++columnN0,jsonObject.optString(key));
                    }
                }
                statement.execute();
            }
            mDb.setTransactionSuccessful();
        } catch (SQLException e) {
            isTrue = false;
            err.append(e.getMessage());
            e.printStackTrace();
        } finally {
            if (statement != null){
                statement.close();
            }
            if (mDb != null){
                mDb.endTransaction();
            }
        }
        return isTrue;
    }

    public synchronized boolean execSQLByBatch(@NonNull List<String> sqls,StringBuilder err) {
        boolean isTrue = true;
        if (sqls.isEmpty()) {
            return false;
        }
        try {
            mDb.beginTransaction();
            for (String sql : sqls) {
                mDb.execSQL(sql);
            }
            mDb.setTransactionSuccessful();
        } catch (SQLException e) {
            isTrue = false;
            err.append(e.getMessage());
            e.printStackTrace();
        } finally {
            if (mDb != null){
                mDb.endTransaction();
            }
        }
        return isTrue;
    }

    public JSONObject getLocalParameter(String paramId){

        return null;
    }

    public synchronized void closeDB(){
        if (mDb != null)
        {
            mDb.close();
        }
    }
    public synchronized SQLiteDatabase getDB() throws SQLiteException{
        if (mDb == null){
            mDb = this.getWritableDatabase();
        }
        return mDb;
    }
    public final synchronized JSONArray getList(@NonNull String sql,Integer minrow,Integer maxrow,boolean row,StringBuilder err){
        Cursor cursor = null;
        JSONArray array = null;
        try{
            cursor = mDb.rawQuery(sql,null);
            array = rs2Json(cursor,minrow,maxrow,row);
        } catch (JSONException | SQLiteException e) {
            err.append("查询错误：" + e.getMessage());
            e.printStackTrace();
            array = null;
        }finally{
            if(cursor != null){
                try{
                    cursor.close();
                }catch(SQLException e){
                    Logger.e("getList关闭cursor错误：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return array;
    }

    public final synchronized List<Map<String,Object>> getList(@NonNull String sql,StringBuilder err){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Map<String,Object>> maps = null;
        try{
            db = getDB();
            cursor = db.rawQuery(sql,null);
            maps = rs2List(cursor);
        } catch (SQLiteException e) {
            err.append("查询出错：" + e.getLocalizedMessage());
            e.printStackTrace();
            maps = null;
        }finally{
            if(cursor!=null){
                try{
                    cursor.close();
                }catch(SQLException e){
                    Logger.e("getList关闭cursor错误：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return maps;
    }


    public final synchronized String getString(@NonNull String sql,StringBuilder err){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String result;
        try{
            db = getDB();
            cursor = db.rawQuery(sql,null);
            result = rs2Txt(cursor);
        } catch (SQLiteException e) {
            err.append("查询错误：" + e.getMessage());
            result = null;
            e.printStackTrace();
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }
        return result;
    }

    public final synchronized JSONArray getListValues(@NonNull String sql,Integer minrow,Integer maxrow,boolean row,StringBuilder err){
        Cursor cursor = null;
        JSONArray array = null;
        try{
            cursor = mDb.rawQuery(sql,null);
            array = rs2Values(cursor,minrow,maxrow,row);
        } catch (JSONException | SQLiteException e) {
            err.append(e.getMessage());
            e.printStackTrace();
            array=null;
        }finally{
            if(cursor!=null){
                cursor.close();
            }
        }
        return array;
    }

    private  List<Map<String,Object>> rs2List(Cursor cursor) {

        List<Map<String,Object>> list = new ArrayList<>();
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();

        // 获取列数
        int columnCount = cursor.getColumnCount();
        //if(!rs.next()) return "";
        if(!cursor.moveToNext()) return list;

        ///System.out.println(rs.getRow());
        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            colNames.add(cursor.getColumnName(i));
            coltypes.add(cursor.getType(i));

            ///System.out.println(i + "-" + cursor.getColumnName(i) + "," + cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据

            do {
                Map<String,Object> map = new HashMap<>();
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

    private  JSONArray rs2Json(Cursor cursor, Integer minRow, Integer maxRow, boolean row) throws JSONException {
        // json数组
        JSONArray array = new JSONArray();
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
        int  mRowcount=0;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        //if(!rs.next()) return "";
        if(!cursor.moveToNext()) return array;

        ///System.out.println(rs.getRow());
        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            colNames.add(cursor.getColumnName(i));
            coltypes.add(cursor.getType(i));

           // Logger.d(i + "-" + cursor.getColumnName(i) + "," + cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据

        if(row) {
            do {
                mRowcount++;
                if (mRowcount > minRow && mRowcount <= maxRow){
                    JSONObject jsonObj = new JSONObject();
                    for (int i = 0; i < columnCount; i++) {
                        if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                            jsonObj.put(colNames.get(i) ,cursor.getFloat(i));  /////i = 0;rs里面的是从1开始
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            jsonObj.put(colNames.get(i) , cursor.getInt(i));
                        }else {
                            if(cursor.getString(i)==null){
                                jsonObj.put(colNames.get(i) , "NULL");
                            }else{
                                jsonObj.put(colNames.get(i) , cursor.getString(i).trim());
                            }
                        }
                    }
                    array.put(jsonObj);
                }
            }while(cursor.moveToNext());
        }else {
            do {
                mRowcount++;
                JSONObject jsonObj = new JSONObject();
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        jsonObj.put(colNames.get(i),BigDecimal.valueOf(cursor.getDouble(i)));  /////i = 0;rs里面的是从1开始
                    } else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {
                        jsonObj.put(colNames.get(i), cursor.getInt(i));
                    } else {
                        if (cursor.getString(i) == null) {
                            jsonObj.put(colNames.get(i), "NULL");
                        } else {
                            jsonObj.put(colNames.get(i), cursor.getString(i).trim());
                        }
                    }
                }
                array.put(jsonObj);
            } while (cursor.moveToNext());
        }
        return array;
    }

    private  JSONArray rs2Values(Cursor cursor, Integer minRow, Integer maxRow, boolean row) throws JSONException {
        // json数组
        JSONArray array = new JSONArray();
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
        int  mRowcount=0;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        //if(!rs.next()) return "";
        if(!cursor.moveToNext()) return array;

        ///System.out.println(rs.getRow());
        //获取列名及相关信息
        for (int i = 0; i < columnCount; i++) {
            colNames.add(cursor.getColumnName(i));
            coltypes.add(cursor.getType(i));

            ///System.out.println(i + "-" + cursor.getColumnName(i) + "," + cursor.getType(i));
        }

        // 遍历ResultSet中的每条数据

        if(row) {
            do {
                mRowcount++;
                if (mRowcount > minRow && mRowcount <= maxRow){
                    //JSONObject jsonObj = new JSONObject();
                    ContentValues jsonObj = new ContentValues();
                    for (int i = 0; i < columnCount; i++) {
                        if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                            jsonObj.put(colNames.get(i) ,cursor.getFloat(i));  /////i = 0;rs里面的是从1开始
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            jsonObj.put(colNames.get(i) , cursor.getInt(i));
                        }else {
                            if(cursor.getString(i)==null){
                                jsonObj.put(colNames.get(i) , "NULL");
                            }else{
                                jsonObj.put(colNames.get(i) , cursor.getString(i).trim());
                            }
                        }
                    }
                    array.put(jsonObj);
                }
            }while(cursor.moveToNext());
        }else {
            do {
                mRowcount++;
                //JSONObject jsonObj = new JSONObject();
                ContentValues jsonObj = new ContentValues();
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        jsonObj.put(colNames.get(i),cursor.getDouble(i));
                    } else if (coltypes.get(i) == FIELD_TYPE_INTEGER) {
                        jsonObj.put(colNames.get(i), cursor.getInt(i));
                    } else {
                        if (cursor.getString(i) == null) {
                            jsonObj.put(colNames.get(i), "NULL");
                        } else {
                            jsonObj.put(colNames.get(i), cursor.getString(i).trim());
                        }
                    }
                }
                array.put(jsonObj);
            } while (cursor.moveToNext());
        }
        return array;
    }
    private  String rs2Txt(Cursor cursor) throws SQLiteException {
        // json数组

        ArrayList<Integer> coltypes=new ArrayList<>();
        StringBuilder dataList = new StringBuilder();
        String data =null;
        // 获取列数
        int columnCount = cursor.getColumnCount();
        //获取列名及相关信息
        if(!cursor.moveToNext()) {
            return "";
        }

        for (int i = 0; i < columnCount; i++) {
            coltypes.add(cursor.getType(i));
            //System.out.println(i + "-" + cursor.getColumnName(i) + "," + cursor.getType(i));
        }
        // 遍历ResultSet中的每条数据
        do {
            if(dataList.length()>0){
                dataList.append("\r\n");
            }
            for (int i = 0; i < columnCount; i++) {
                data = cursor.getString(i);
                if(data==null){
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

    private void initDatabase(SQLiteDatabase db) throws SQLiteException {

        List<String> list = new ArrayList<>();
        String sql_shop_stores = "CREATE TABLE IF NOT EXISTS  shop_stores (\n" +//商店仓库
                "    nature      INT     DEFAULT (1),\n" +
                "    status      INT     DEFAULT (1),\n" +
                "    region      VARCHAR,\n" +
                "    telphone    CHAR,\n" +
                "    manager     VARCHAR,\n" +
                "    stores_name VARCHAR,\n" +
                "    stores_id   INTEGER PRIMARY KEY\n" +
                ");\n",
        sql_shop_category = "CREATE TABLE IF NOT EXISTS shop_category (\n" +//商品类别
                "    sort        INTEGER DEFAULT (0),\n" +
                "    status      INTEGER DEFAULT (1),\n" +
                "    path        CHAR,\n" +
                "    depth       INT,\n" +
                "    parent_id   CHAR    DEFAULT (0),\n" +
                "    name        CHAR,\n" +
                "    category_id INTEGER UNIQUE\n" +
                ");\n",
        sql_barcode_info = "CREATE TABLE IF NOT EXISTS barcode_info (\n" +//商品档案
                "    points_max_money REAL    DEFAULT (0),\n" +
                "    stock_unit_name  VARCHAR,\n" +
                "    stock_unit_id    INTEGER,\n" +
                "    update_price     INTEGER DEFAULT (0),\n" +
                "    conversion       INTEGER DEFAULT (1),\n" +
                "    attr_code        VARCHAR,\n" +
                "    attr_name        VARCHAR,\n" +
                "    attr_id          INTEGER,\n" +
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
                "    brand            VARCHAR,\n" +
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
                ");\n",
        sql_pay_method = "CREATE TABLE IF NOT EXISTS pay_method (\n" +//付款方式
                "    is_enable         VARCHAR DEFAULT (1),\n" +
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
                ");\n",
        sql_local_parameter= "CREATE TABLE IF NOT EXISTS local_parameter (\n" +
                "    parameter_id      VARCHAR (20) NOT NULL,\n" +
                "    parameter_content TEXT,\n" +
                "    PRIMARY KEY (\n" +
                "        parameter_id\n" +
                "    )\n" +
                ");";

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
