package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class HangBillDialog extends Dialog {
    private MainActivity mContext;
    private EditText mS_content;
    private SimpleCursorAdapter mHbCursorAdapter,mHbDetailCursorAdapter;
    private ListView mHangBillList,mHangBillDetails;
    private View mCurrentSelectedRow;
    public HangBillDialog(@NonNull MainActivity context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.hangbill_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //搜索内容text
        initSearch();
        //初始化表格
        initHangBillList();
        initHangBillDetail();


        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v->HangBillDialog.this.dismiss());
        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.8 * point.y); // 宽度
                dialogWindow.setAttributes(lp);
            }
        }

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            tmp_v.setOnClickListener(mKeyboardListener);
        }

    }

    @Override
    public void dismiss(){
        super.dismiss();
        if (mHbCursorAdapter != null){
            mHbCursorAdapter.changeCursor(null);
        }
        if (mHbDetailCursorAdapter != null){
            mHbDetailCursorAdapter.changeCursor(null);
        }
    }

    private void initSearch(){
        mS_content = findViewById(R.id.s_content);
        mS_content.setOnFocusChangeListener((v,b)-> Utils.hideKeyBoard((EditText) v));
        mS_content.postDelayed(()-> mS_content.requestFocus(),100);
        mS_content.setSelectAllOnFocus(true);
        mS_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loadHangBill(s.toString());
            }
        });
    }

    private void initHangBillList(){
        mHangBillList = findViewById(R.id.hangbill_list);
        mHangBillList.addHeaderView(LayoutInflater.from(mContext).inflate(R.layout.hangbill_header_layout,null));
        mHbCursorAdapter = new SimpleCursorAdapter(mContext,R.layout.hangbill_content_layout,null,new String[]{"_id","hang_id","h_amt","h_cas_name","oper_date"},new int[]{R.id._id,R.id.hang_id,R.id.h_amt,R.id.h_cas_name,R.id.h_time},1);
        mHbCursorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mCurrentSelectedRow != null){
                    setViewBackgroundColor(mCurrentSelectedRow,false);;
                    mCurrentSelectedRow = null;
                }
            }
        });
        mHbCursorAdapter.setViewBinder((view, cursor, columnIndex) -> {
            //setViewBackgroundColor(view,false);
            int col_type = cursor.getType(columnIndex);
            if (col_type == Cursor.FIELD_TYPE_FLOAT || col_type == Cursor.FIELD_TYPE_INTEGER){
                if (view instanceof TextView){
                    ((TextView) view).setText(String.format(Locale.CHINA,"%.2f",cursor.getDouble(columnIndex)));
                }
                return true;
            }
            return false;
        });
        mHangBillList.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0)return;
            TextView hang_id_v = view.findViewById(R.id.hang_id);
            if (null != hang_id_v){
                if (mCurrentSelectedRow != null){
                    setViewBackgroundColor(mCurrentSelectedRow,false);;
                  }
                mCurrentSelectedRow = view;
                setViewBackgroundColor(mCurrentSelectedRow,true);
                loadHangBillDetail(hang_id_v.getText().toString());
            }
        });
        mHangBillList.setAdapter(mHbCursorAdapter);
        loadHangBill(null);
    }

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null){
            int white = mContext.getColor(R.color.white);
            if (s){
                view.setBackgroundColor(mContext.getColor(R.color.listSelected));
                if (view instanceof LinearLayout){
                    LinearLayout linearLayout = (LinearLayout)view;
                    int count = linearLayout.getChildCount();
                    View ch;
                    for (int i = 0;i < count;i++){
                        ch = linearLayout.getChildAt(i);
                        if (ch instanceof TextView){
                            ((TextView) ch).setTextColor(white);
                        }
                    }
                }
            }else{
                view.setBackgroundColor(white);
                if (view instanceof LinearLayout){
                    LinearLayout linearLayout = (LinearLayout)view;
                    int count = linearLayout.getChildCount();
                    View ch;
                    for (int i = 0;i < count;i++){
                        ch = linearLayout.getChildAt(i);
                        if (ch instanceof TextView){
                            ((TextView) ch).setTextColor(mContext.getColor(R.color.text_color));
                        }
                    }
                }
            }
        }
    }

    private void initHangBillDetail(){
        mHangBillDetails = findViewById(R.id.hangbill_details_list);
        mHangBillDetails.addHeaderView(LayoutInflater.from(mContext).inflate(R.layout.hangbill_detail_header_layout,null));
        mHbDetailCursorAdapter = new SimpleCursorAdapter(mContext,R.layout.hangbill_detail_content_layout,null,new String[]{"_id","barcode","goods_title","xnum","sale_price","discount","sale_amt"},
                new int[]{R.id._id,R.id.barcode,R.id.goods_title,R.id.xnum,R.id.h_sale_price,R.id.h_discount,R.id.h_sale_amt},1);
        mHbDetailCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                String col_name = cursor.getColumnName(columnIndex);
                if ("sale_price".equals(col_name) || "sale_amt".equals(col_name)){
                    if (view instanceof TextView){
                        ((TextView) view).setText(String.format(Locale.CHINA,"%.2f",cursor.getDouble(columnIndex)));
                    }
                    return true;
                }
                return false;
            }
        });
        mHangBillDetails.setAdapter(mHbDetailCursorAdapter);
    }

    private void loadHangBill(final String hang_id){
        if (mHbCursorAdapter != null){
            String sql = "";
            if (hang_id == null){
                sql = "SELECT _id,hang_id,amt h_amt,cas_name h_cas_name,oper_date FROM hangbill";
            }else
                sql = "SELECT _id,hang_id,amt h_amt,cas_name h_cas_name,oper_date FROM hangbill where hang_id like '" + hang_id +"%'";

            try {
                Cursor cursor = SQLiteHelper.getCursor(sql,null);
                cursor.moveToFirst();
                mHbCursorAdapter.changeCursor(cursor);
                if (cursor.getCount() != 0){
                    mHbCursorAdapter.bindView(LayoutInflater.from(mContext).inflate(R.layout.hangbill_content_layout,null),mContext,cursor);
                    mHbCursorAdapter.notifyDataSetChanged();
                }
            }catch (SQLiteException e){
                e.printStackTrace();
                mContext.runOnUiThread(()->MyDialog.ToastMessage(e.getMessage(),mContext,getWindow()));
            }
        }
    }

    private void loadHangBillDetail(final String hang_id){
        if (mHbDetailCursorAdapter != null){
            try {
                Cursor cursor = SQLiteHelper.getCursor("SELECT _id,barcode,goods_title,xnum,sale_price,discount,sale_amt FROM hangbill_detail where hang_id = " + hang_id,null);
                cursor.moveToFirst();
                mHbDetailCursorAdapter.changeCursor(cursor);
                if (cursor.getCount() != 0){
                    mHbDetailCursorAdapter.bindView(LayoutInflater.from(mContext).inflate(R.layout.hangbill_detail_content_layout,null),mContext,cursor);
                    mHbDetailCursorAdapter.notifyDataSetChanged();
                }
            }catch (SQLiteException e){
                e.printStackTrace();
                mContext.runOnUiThread(()->MyDialog.ToastMessage(e.getMessage(),mContext,getWindow()));
            }
        }
    }
    private View.OnClickListener mKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int v_id = view.getId();
            EditText et_view = (EditText) getCurrentFocus();
            if (et_view  != null){
                Editable editable = et_view.getText();
                if (v_id == R.id._ok){

                }else if (v_id == R.id._back){
                    if (editable.length() != 0)
                        editable.delete(editable.length() - 1,editable.length());
                }else{
                    if (et_view.getSelectionStart() != et_view.getSelectionEnd()){
                        editable.replace(0,editable.length(),((Button)view).getText());
                        et_view.setSelection(editable.length());
                    }else
                        editable.append(((Button)view).getText());
                }
            }
        }
    };

    public boolean save(JSONArray array){
        boolean code = true;
        if (array != null){
            int hang_id = 0;
            double amt = 0.0;
            JSONObject data,tmp_obj;
            JSONArray orders = new JSONArray(),details = new JSONArray();

            try {
                final String stores_id = mContext.getStoreInfo().getString("stores_id"),
                        cas_id = mContext.getCashierInfo().getString("cas_id"),
                        cas_name = mContext.getCashierInfo().getString("cas_name");

                hang_id = getHangCounts();

                if (hang_id > 0){
                    for (int i = 0,length = array.length();i < length; i++){
                        tmp_obj = new JSONObject();
                        data = array.getJSONObject(i);
                        amt += data.getDouble("sale_amt");

                        tmp_obj.put("hang_id",hang_id);
                        tmp_obj.put("row_id",i + 1);
                        tmp_obj.put("stores_id",stores_id);
                        tmp_obj.put("barcode_id",data.getInt("barcode_id"));
                        tmp_obj.put("barcode",data.getString("barcode"));
                        tmp_obj.put("goods_title",data.getString("goods_title"));
                        tmp_obj.put("old_price",data.getDouble("old_price"));
                        tmp_obj.put("sale_price",data.getDouble("price"));
                        tmp_obj.put("xnum",data.getDouble("xnum"));
                        tmp_obj.put("unit_name",data.getString("unit_name"));
                        tmp_obj.put("sale_amt",data.getDouble("sale_amt"));
                        tmp_obj.put("vip_no",data.optString("card_code"));
                        tmp_obj.put("discount",data.getDouble("discount") * 100);
                        tmp_obj.put("discount_amt",data.getDouble("discount_amt"));
                        tmp_obj.put("sale_man",data.optString("sale_man"));
                        tmp_obj.put("cas_id",cas_id);

                        details.put(tmp_obj);
                    }
                    data = new JSONObject();
                    data.put("hang_id",hang_id);
                    data.put("amt",amt);
                    data.put("cas_id",cas_id);
                    data.put("cas_name",cas_name);
                    orders.put(data);

                    data = new JSONObject();
                    data.put("hangbill",orders);
                    data.put("hangbill_detail",details);

                    final List<String> tables = Arrays.asList("hangbill","hangbill_detail");
                    final StringBuilder err = new StringBuilder();
                    if (SQLiteHelper.execSQLByBatchFromJson(data,tables,null,err,0)){
                        mContext.runOnUiThread(()->MyDialog.ToastMessage("挂单成功！",mContext,mContext.getWindow()));
                    }else{
                        code = false;
                        mContext.runOnUiThread(()->MyDialog.ToastMessage("保存挂单信息错误：" + err,mContext,mContext.getWindow()));
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
                code = false;
                mContext.runOnUiThread(()->MyDialog.ToastMessage(e.getMessage(),mContext,null));
            }
        }
        return code;
    }

    public int getHangCounts(){
        JSONObject data = new JSONObject();
        if (!SQLiteHelper.execSql(data,"select count(hang_id) + 1 hang_id from hangbill")){
            mContext.runOnUiThread(()->MyDialog.ToastMessage("查询挂单号错误：" + data.optString("info"),mContext,mContext.getWindow()));
            return 0;
        }
        return data.optInt("hang_id");
    }
}
