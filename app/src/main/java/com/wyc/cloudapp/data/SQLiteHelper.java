package com.wyc.cloudapp.data;

import android.app.Activity;
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
import androidx.annotation.NonNull;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Administrator on 2018-03-27.
 */

public final class SQLiteHelper extends SQLiteOpenHelper {
    public static String IMG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/img/";
    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/order.db";
    private static final int DATABASE_VERSION = 1;//记得修改软件版本
    private static final int MASTER_SOFTWRAW_VERSION = 1;
    private static SQLiteDatabase mDb;
    private Context mContext;
    private SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Logger.d("DATABASE_NAME:%s",DATABASE_NAME);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            initTables(db);//初始化数据库
        }catch (SQLiteException e){
            MyDialog.displayErrorMessage(null,"初始化本地数据库错误：" + e.getMessage(),mContext);
        }
        onUpgrade(db,0,0);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure (SQLiteDatabase db){
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    public static boolean initDb(Context context){
        boolean code = true;
        if (mDb == null){
            SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
            synchronized (SQLiteHelper.class){
                if (mDb == null)
                    try {
                        mDb = sqLiteHelper.getWritableDatabase();
                    }catch (SQLiteCantOpenDatabaseException e){
                        MyDialog.displayErrorMessage(null,"打开数据库错误：" + e.getLocalizedMessage(),context,(MyDialog myDialog)->{
                            myDialog.dismiss();
                           if (context instanceof Activity){
                               ((Activity)context).finish();
                           }else{
                               System.exit(0);
                           }
                        });
                        code = false;
                    }
            }
        }
        return code;
    }

    public static void initGoodsImgDirectory(Context context){
        File file = new File(IMG_PATH);
        if (!file.exists()){
            if (!file.mkdir()){
                MyDialog.ToastMessage("初始化商品图片目录错误！",context,null);
            }
        }
    }

    private static boolean checkColumnExists(String tableName, String columnName) throws SQLiteException {
        boolean result = false ;
        Cursor cursor = null ;
        try{
            cursor = mDb.rawQuery( "select 1 from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName , "%" + columnName + "%"} );
            result = null != cursor && cursor.moveToFirst() ;
        }finally{
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
        return result ;
    }

    public static boolean execSQLByBatchInsertJson(@NonNull JSONArray jsonArray,String table,StringBuilder err) {
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

        synchronized(SQLiteHelper.class){
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
        }
        return isTrue;
    }

    private static boolean execSQLByBatchFromJson(@NonNull JSONArray jsonArray, String table, StringBuilder err) {
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

        synchronized (SQLiteHelper.class){
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
        }
        return isTrue;
    }

    public static boolean execSQLByBatchFromJson(@NonNull JSONArray jsonArray, String table, String[] cls, StringBuilder err) {
        boolean isTrue = true;
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();
        JSONObject jsonObject = null;
        SQLiteStatement statement = null;
        int columnN0 = 0;
        if (jsonArray.length() == 0) {
            return false;
        }

        if (cls == null || cls.length == 0){
            return execSQLByBatchFromJson(jsonArray,table,err);
        }

        stringBuilderHead.append("REPLACE INTO ");
        stringBuilderHead.append(table);
        stringBuilderHead.append(" (");
        stringBuilderfoot.append("VALUES (");

        for (String cl:cls){
            stringBuilderHead.append(cl);
            stringBuilderHead.append(",");

            stringBuilderfoot.append("?");
            stringBuilderfoot.append(",");
        }

        stringBuilderHead.replace(stringBuilderHead.length() - 1,stringBuilderHead.length(),")");
        stringBuilderfoot.replace(stringBuilderfoot.length() - 1,stringBuilderfoot.length(),")");
        stringBuilderHead.append(stringBuilderfoot);

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                statement = mDb.compileStatement(stringBuilderHead.toString());
                for (int i = 0,len = jsonArray.length();i < len; i ++){
                    columnN0 = 0;
                    jsonObject = jsonArray.optJSONObject(i);
                    for (String cl:cls){
                        if ("".equals(jsonObject.optString(cl))){
                            statement.bindNull(++columnN0);
                        }else
                            statement.bindString(++columnN0,jsonObject.optString(cl));
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
        }
        return isTrue;
    }

    public static boolean getLocalParameter(String parameter_id,@NonNull JSONObject param){
        boolean isTrue = true;
        try (Cursor cursor = mDb.query("local_parameter",new String[]{"parameter_content"},"parameter_id = ?",new String[]{parameter_id},
                null,null,null)){
            if (!cursor.moveToNext()){
                return true;
            }
            JSONObject json = new JSONObject(cursor.getString(0));
            Iterator<String> iterator = json.keys();
            String key;
            while(iterator.hasNext()){
                key = iterator.next();
                param.put(key,json.getString(key));
            }
        } catch (JSONException | SQLiteException e) {
            isTrue = false;
            try {
                param.put("info","查询参数错误：" + e.getMessage());
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return isTrue;
    }

    public static void closeDB(){
        if (mDb != null)
        {
            synchronized (SQLiteHelper.class){
                mDb.close();
                mDb = null;
            }
        }
    }

    public static JSONArray getListToJson(@NonNull String sql, Integer minrow, Integer maxrow, boolean row, StringBuilder err){
        JSONArray array;
        synchronized (SQLiteHelper.class){
            try(Cursor cursor = mDb.rawQuery(sql,null);){
                array = rs2Json(cursor,minrow,maxrow,row);
            } catch (JSONException | SQLiteException e) {
                err.append("查询错误：").append(e.getMessage());
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
                err.append("查询错误：").append(e.getMessage());
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
                if (err !=  null)err.append("查询错误：").append(e.getMessage());
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
                err.append("查询出错：" ).append(e.getLocalizedMessage());
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
                err.append("查询错误：").append(e.getMessage());
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
                err.append(e.getMessage());
                e.printStackTrace();
                array=null;
            }
        }
        return array;
    }
    public static boolean saveToDatabaseFormJson(@NonNull final JSONObject json,@NonNull final String table_name,String[] cls,final String save_type,StringBuilder err){
        boolean isTrue = true;
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();
        String key;
        SQLiteStatement statement = null;
        Iterator iterator;
        int columnN0 = 0;

        stringBuilderHead.append(save_type).append(" INTO ");
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
        }else{
            iterator = json.keys();
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

        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                statement = mDb.compileStatement(stringBuilderHead.toString());

                if(cls != null && cls.length != 0){
                    for (String cl:cls){
                        if ("".equals(json.getString(cl))){
                            statement.bindNull(++columnN0);
                        }else
                            statement.bindString(++columnN0,json.getString(cl));
                    }
                }else{
                    iterator = json.keys();
                    while(iterator.hasNext()){
                        key = (String) iterator.next();
                        if ("".equals(json.getString(key))){
                            statement.bindNull(++columnN0);
                        }else
                            statement.bindString(++columnN0,json.getString(key));
                    }
                }
                statement.execute();

                mDb.setTransactionSuccessful();
            } catch (SQLException | JSONException e) {
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
                        json.put(colNames.get(i),BigDecimal.valueOf(cursor.getDouble(i)));  /////i = 0;rs里面的是从1开始
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
                    json.put("info","查询错误：" + e.getMessage());
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return isTrue;
    }
    public static boolean execUpdateSql(@NonNull final String table,final ContentValues values,final String whereClause,final String[] whereArgs,StringBuilder err){
        //执行updata语句    sql要执行的数据库修改语句 如果出错error包含错误信息
        try {
            synchronized (SQLiteHelper.class){
                mDb.update(table,values,whereClause,whereArgs);
            }
        }catch (SQLiteException e){
            e.printStackTrace();
            err.append(e.getMessage());
            return false;
        }
        return true;
    }
    public static boolean execInsertSql(@NonNull final String table,final String nullColumnHack,final ContentValues values,StringBuilder err){
        //执行updata语句    sql要执行的数据库修改语句 如果出错error包含错误信息
        boolean code = true;
        try {
            synchronized (SQLiteHelper.class){
                mDb.insert(table,nullColumnHack,values);
            }
        }catch (SQLiteException e){
            code = false;
            err.append(e.getMessage());
        }
        return code;
    }
    public static boolean execBatchUpdateSql(@NonNull final List<String> list,StringBuilder err){
        //执行updata语句    sql要执行的数据库修改语句 如果出错error包含错误信息
        boolean code = true;
        try {
            synchronized (SQLiteHelper.class){
                try {
                    mDb.beginTransaction();
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
            err.append(e.getMessage());
        }
        return code;
    }

    public static boolean execSQLByBatchFromJson(@NonNull JSONObject json, List<String> tables, List<List<String>> table_cols, StringBuilder err, int type ){//type 0 insert 1 replace
        boolean code = true;
        StringBuilder stringBuilderHead = new StringBuilder();
        StringBuilder stringBuilderfoot = new StringBuilder();
        JSONObject jsonObject;
        String table,key;
        SQLiteStatement statement = null;
        int columnN0 = 0;
        List<String> cls;
        JSONArray arrays;
        Iterator iterator;
        synchronized (SQLiteHelper.class){
            try {
                mDb.beginTransaction();
                for (int k = 0,size = tables.size();k < size;k++) {
                    table = tables.get(k);
                    arrays = json.getJSONArray(table);

                    if (stringBuilderHead.length() != 0)
                        stringBuilderHead.delete(0, stringBuilderHead.length());
                    if (stringBuilderfoot.length() != 0)
                        stringBuilderfoot.delete(0, stringBuilderfoot.length());

                    if (type == 1)
                        stringBuilderHead.append("REPLACE INTO ");
                    else
                        stringBuilderHead.append("INSERT INTO ");

                    stringBuilderHead.append(table);
                    stringBuilderHead.append(" (");
                    stringBuilderfoot.append("VALUES (");

                    if (table_cols != null){
                        cls = table_cols.get(k);
                        for (String cl : cls) {
                            stringBuilderHead.append(cl);
                            stringBuilderHead.append(",");

                            stringBuilderfoot.append("?");
                            stringBuilderfoot.append(",");
                        }
                    }else{
                        jsonObject = arrays.optJSONObject(0);
                        iterator = jsonObject.keys();
                        while(iterator.hasNext()){
                            key = (String) iterator.next();
                            stringBuilderHead.append(key);
                            stringBuilderHead.append(",");

                            stringBuilderfoot.append("?");
                            stringBuilderfoot.append(",");
                        }
                    }

                    stringBuilderHead.replace(stringBuilderHead.length() - 1, stringBuilderHead.length(), ")");
                    stringBuilderfoot.replace(stringBuilderfoot.length() - 1, stringBuilderfoot.length(), ")");
                    stringBuilderHead.append(stringBuilderfoot);


                    statement = mDb.compileStatement(stringBuilderHead.toString());
                    for (int i = 0, len = arrays.length(); i < len; i++) {
                        columnN0 = 0;
                        jsonObject = arrays.getJSONObject(i);
                        if (table_cols != null){
                            cls = table_cols.get(k);
                            for (String cl : cls) {
                                if ("".equals(jsonObject.optString(cl))) {
                                    statement.bindNull(++columnN0);
                                } else
                                    statement.bindString(++columnN0, jsonObject.optString(cl));
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
                }
                mDb.setTransactionSuccessful();
            } catch (SQLException | JSONException e) {
                code = false;
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
        }

         return code;
    }

    private static List<Map<String,Object>> rs2List(Cursor cursor) {

        List<Map<String,Object>> list = new ArrayList<>();
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
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
        JSONArray array = new JSONArray();
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
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
                            jsonObj.put(colNames.get(i) ,cursor.getFloat(i));  /////i = 0;rs里面的是从1开始
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
                    array.put(jsonObj);
                }
            }while(cursor.moveToNext());
        }else {
            do {
                jsonObj = new JSONObject();
                for (int i = 0; i < columnCount; i++) {
                    if (coltypes.get(i) == FIELD_TYPE_FLOAT) {
                        jsonObj.put(colNames.get(i),BigDecimal.valueOf(cursor.getDouble(i)));  /////i = 0;rs里面的是从1开始
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
                array.put(jsonObj);
            } while (cursor.moveToNext());
        }
        return array;
    }
    private static JSONArray rs2Values(Cursor cursor, Integer minRow, Integer maxRow, boolean row) throws JSONException {
        // json数组
        JSONArray array = new JSONArray();
        ArrayList<String> colNames=new ArrayList<String>();
        ArrayList<Integer> coltypes=new ArrayList<Integer>();
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
                    for (int i = 0; i < columnCount; i++) {
                        if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                            array.put(cursor.getFloat(i));
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            array.put(cursor.getInt(i));
                        }else {
                            if(cursor.getString(i)==null){
                                array.put( "");
                            }else{
                                array.put(cursor.getString(i).trim());
                            }
                        }
                    }
                }
            }while(cursor.moveToNext());
        }else {
            do {
                for (int i = 0; i < columnCount; i++) {
                    if(coltypes.get(i) == FIELD_TYPE_FLOAT){
                        array.put(cursor.getFloat(i));
                    }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                        array.put(cursor.getInt(i));
                    }else {
                        if(cursor.getString(i)==null){
                            array.put( "");
                        }else{
                            array.put(cursor.getString(i).trim());
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
                            values.put(colNames.get(i) ,cursor.getFloat(i));  /////i = 0;rs里面的是从1开始
                        }else if(coltypes.get(i) == FIELD_TYPE_INTEGER){
                            values.put(colNames.get(i) , cursor.getInt(i));
                        }else {
                            if(cursor.getString(i) == null){
                                values.put(colNames.get(i) , "");
                            }else{
                                values.put(colNames.get(i) , cursor.getString(i).trim());
                            }
                        }
                    }
                    array.put(values);
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
                array.put(values);
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
                "    category_id INTEGER UNIQUE\n" +
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
                "    pos_code        CHAR,\n" +
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
                ");",goods_group_view_sql = "CREATE VIEW IF NOT EXISTS vi_goods_group_info AS\n" +//组合商品视图
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
                "           c.status = 1;\n";

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
        list.add(goods_group_view_sql);
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
