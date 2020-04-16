package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.os.Bundle;

import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static android.content.Context.WINDOW_SERVICE;

public class HangBillDialog extends Dialog {
    private MainActivity mContext;
    private SimpleCursorAdapter mHbCursorAdapter,mHbDetailCursorAdapter;
    private View mCurrentSelectedRow,mVipInfoView;
    private OnGetBillListener mGetListener;
    public HangBillDialog(@NonNull MainActivity context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.hangbill_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //初始化表格
        initHangBillList();
        initHangBillDetail();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v->HangBillDialog.this.dismiss());
        findViewById(R.id.del_hang_b).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mCurrentSelectedRow) {
                    TextView h_id_v = mCurrentSelectedRow.findViewById(R.id.hang_id);
                    if (null != h_id_v) {
                        StringBuilder err = new StringBuilder();
                        if (!deleteBill(h_id_v.getText().toString(),err)){
                            MyDialog.ToastMessage("删除挂单信息错误：" + err, mContext, getWindow());
                        }
                    }
                } else {
                    MyDialog.ToastMessage("请选择需要删除的记录！", mContext, getWindow());
                }
            }
        });
        findViewById(R.id.to_checkout).setOnClickListener(v -> {
            if (mGetListener != null && mHbDetailCursorAdapter != null){
                if (mCurrentSelectedRow != null){
                    TextView hang_id_v = mCurrentSelectedRow.findViewById(R.id.hang_id),card_code_v = mCurrentSelectedRow.findViewById(R.id.card_code);
                    if (hang_id_v != null){
                        final StringBuilder err = new StringBuilder();
                        final String hang_id = hang_id_v.getText().toString();
                        final JSONArray barcode_ids = SQLiteHelper.getListToJson("SELECT barcode_id FROM hangbill_detail where hang_id = " + hang_id,err);
                        if (null != barcode_ids){
                             if (null != card_code_v){
                                 final String  card_code = card_code_v.getText().toString();
                                 if (!"".equals(card_code)){
                                     final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
                                     progressDialog.setCancel(false).setMessage("正在查询会员信息...").show();
                                     CustomApplication.execute(()->{
                                         try {
                                             final JSONArray vips = VipInfoDialog.serchVip(card_code);
                                             mContext.runOnUiThread(()->{
                                                 if (deleteBill(hang_id,err)){
                                                     mContext.runOnUiThread(()->mGetListener.onGet(barcode_ids,vips.optJSONObject(0)));
                                                 }else{
                                                     mContext.runOnUiThread(()->MyDialog.ToastMessage("删除挂单信息错误：" + err,mContext,null));
                                                 }
                                             });
                                         } catch (JSONException e) {
                                             e.printStackTrace();
                                             mContext.runOnUiThread(()->MyDialog.ToastMessage(e.getMessage(),mContext,null));
                                         }
                                         progressDialog.dismiss();
                                     });
                                 }else{
                                     if (deleteBill(hang_id,err)){
                                         mGetListener.onGet(barcode_ids,null);
                                     }else{
                                         MyDialog.ToastMessage("删除挂单信息错误：" + err, mContext, getWindow());
                                     }
                                 }

                            }

                        }else{
                            MyDialog.ToastMessage("查询挂单明细错误：" + err,mContext,getWindow());
                        }
                    }
                }
            }
        });

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

    private void initHangBillList(){
        //表头
        ListView header = findViewById(R.id.header);
        header.addHeaderView(LayoutInflater.from(mContext).inflate(R.layout.hangbill_header_layout,null));
        header.setAdapter(new SimpleCursorAdapter(mContext,R.layout.hangbill_header_layout,null,null,null,1));
        //表中区
        ListView mHangBillList = findViewById(R.id.hangbill_list);
        mHbCursorAdapter = new SimpleCursorAdapter(mContext,R.layout.hangbill_content_layout,null,new String[]{"_id","hang_id","h_amt","h_cas_name","oper_date","card_code","vip_name","vip_mobile"},new int[]{R.id._id,R.id.hang_id,R.id.h_amt,R.id.h_cas_name,R.id.h_time,R.id.card_code,R.id.vip_name,R.id.vip_mobile},1);
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
            if ("h_amt".equals(cursor.getColumnName(columnIndex))){
                if (view instanceof TextView){
                    ((TextView) view).setText(String.format(Locale.CHINA,"%.2f",cursor.getDouble(columnIndex)));
                }
                return true;
            }
            return false;
        });
        mHangBillList.setOnItemClickListener((parent, view, position, id) -> {
            TextView hang_id_v = view.findViewById(R.id.hang_id);
            if (null != hang_id_v){
                if (mCurrentSelectedRow != null){
                    setViewBackgroundColor(mCurrentSelectedRow,false);;
                  }
                mCurrentSelectedRow = view;
                setViewBackgroundColor(mCurrentSelectedRow,true);
                showVipInfo();
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
        //表头
        ListView mDetailHeader = findViewById(R.id.h_detail_header);
        mDetailHeader.addHeaderView(LayoutInflater.from(mContext).inflate(R.layout.hangbill_detail_header_layout,null));
        mDetailHeader.setAdapter(new SimpleCursorAdapter(mContext,R.layout.hangbill_detail_header_layout,null,null,null,1));
        //表中区
        ListView mHangBillDetails = findViewById(R.id.hangbill_details_list);
        mHbDetailCursorAdapter = new SimpleCursorAdapter(mContext,R.layout.hangbill_detail_content_layout,null,new String[]{"_id","barcode","goods_title","xnum","sale_price","discount","sale_amt"},
                new int[]{R.id._id,R.id.barcode,R.id.goods_title,R.id.xnum,R.id.h_sale_price,R.id.h_discount,R.id.h_sale_amt},1);
        mHbDetailCursorAdapter.setViewBinder((view, cursor, columnIndex) -> {
            String col_name = cursor.getColumnName(columnIndex);
            if ("sale_price".equals(col_name) || "sale_amt".equals(col_name)){
                if (view instanceof TextView){
                    ((TextView) view).setText(String.format(Locale.CHINA,"%.2f",cursor.getDouble(columnIndex)));
                }
                return true;
            }
            return false;
        });
        mHangBillDetails.setAdapter(mHbDetailCursorAdapter);
    }

    private void loadHangBill(final String hang_id){
        if (mHbCursorAdapter != null){
            String sql = "SELECT _id,hang_id,amt h_amt,cas_name h_cas_name,oper_date,card_code,vip_name,vip_mobile FROM hangbill";
            if (hang_id != null){
                sql = sql + " where hang_id like '" + hang_id +"%'";
            }
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
                Cursor cursor = SQLiteHelper.getCursor("SELECT _id,barcode,goods_title,xnum,sale_price,discount,sale_amt,barcode_id FROM hangbill_detail where hang_id = " + hang_id,null);
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

    private boolean deleteBill(final String id,@NonNull final StringBuilder err){
        boolean code = true;
        String sql = "delete from hangbill where hang_id = " + id,sql_detail = "delete from hangbill_detail where hang_id = " + id;
        List<String> sqls = new ArrayList<>();
        sqls.add(sql);
        sqls.add(sql_detail);
        if ((code = SQLiteHelper.execBatchUpdateSql(sqls,err))){
            loadHangBill(null);
            mHbDetailCursorAdapter.changeCursor(null);
        }
        return code;
    }

    private void showVipInfo(){
        if (mCurrentSelectedRow != null){
            TextView name,mobile,t_name,t_mobile;
            name = mCurrentSelectedRow.findViewById(R.id.vip_name);
            mobile = mCurrentSelectedRow.findViewById(R.id.vip_mobile);
            if (null != name && null != mobile){
                CharSequence sz_name = name.getText(),sz_mobile = mobile.getText();
                if (null == mVipInfoView){
                    mVipInfoView = findViewById(R.id.vip_info_linearLayout);
                }
                if (sz_mobile.length() != 0 && sz_name.length() != 0){
                    if (mVipInfoView.getVisibility() == View.GONE){
                        mVipInfoView.setVisibility(View.VISIBLE);
                    }
                    t_name = mVipInfoView.findViewById(R.id.vip_name);
                    t_mobile = mVipInfoView.findViewById(R.id.vip_phone_num);
                    if (null != t_name && null != t_mobile){
                        t_name.setText(sz_name);
                        t_mobile.setText(sz_mobile);
                    }
                }else{
                    if (mVipInfoView.getVisibility() == View.VISIBLE){
                        mVipInfoView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public boolean save(JSONArray array,JSONObject vip,final StringBuilder err){
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
                    if (null != vip){
                        data.put("card_code",vip.getString("card_code"));
                        data.put("vip_name",vip.getString("name"));
                        data.put("vip_mobile",vip.getString("mobile"));
                    }
                    orders.put(data);

                    data = new JSONObject();
                    data.put("hangbill",orders);
                    data.put("hangbill_detail",details);

                    final List<String> tables = Arrays.asList("hangbill","hangbill_detail");
                    code = SQLiteHelper.execSQLByBatchFromJson(data,tables,null,err,0);
                }else{
                    code = false;
                    err.append("获取挂单号错误,单号：<").append(hang_id).append(">");
                }
            }catch (JSONException e){
                e.printStackTrace();
                code = false;
                err.append(e.getMessage());
            }
        }
        return code;
    }

    public int getHangCounts(){
        JSONObject data = new JSONObject();
        if (!SQLiteHelper.execSql(data,"select ifnull(max(hang_id),0) + 1 hang_id from hangbill")){
            mContext.runOnUiThread(()->MyDialog.ToastMessage("查询挂单号错误：" + data.optString("info"),mContext,mContext.getWindow()));
            return 0;
        }
        return data.optInt("hang_id");
    }

    public void setGetBillDetailListener(OnGetBillListener listener){
        mGetListener = listener;
    }

    public interface OnGetBillListener{
        void onGet(final JSONArray array,final JSONObject vip);
    }
}
