package com.wyc.cloudapp.dialog;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.os.Bundle;

import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class HangBillDialog extends AbstractDialogBaseOnMainActivityImp {
    private SimpleCursorAdapter mHbCursorAdapter,mHbDetailCursorAdapter;
    private View mVipInfoView;
    private String mCurrentHangId;
    private OnGetBillListener mGetListener;

    public HangBillDialog(@NonNull MainActivity context) {
        super(context,context.getString(R.string.hangbill_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化表格
        initHangBillDetail();
        initHangBillList();

        //初始化按钮事件
        initDelHangBill();
        findViewById(R.id.to_checkout).setOnClickListener(v -> {
            to_checkout();
        });

        //初始化窗口尺寸
        initWindowSize();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.hangbill_dialog_layout;
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

    private void initDelHangBill(){
        final Button del_hang_b = findViewById(R.id.del_hang_b);
        if (null != del_hang_b)
            del_hang_b.setOnClickListener(v -> {
                if (null != mCurrentHangId && !"".equals(mCurrentHangId)) {
                    if (1 == MyDialog.showMessageToModalDialog(mContext,"是否删除挂单?")){
                        if (verifyDeleteBillPermissions()){
                            final StringBuilder err = new StringBuilder();
                            if (!deleteBill(mCurrentHangId,err)){
                                MyDialog.ToastMessage("删除挂单信息错误：" + err, mContext, getWindow());
                            }
                        }
                    }
                } else {
                    MyDialog.ToastMessage("请选择需要删除的记录！", mContext, getWindow());
                }
            });
    }
    private void initWindowSize(){
        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.9 * point.y);
                dialogWindow.setAttributes(lp);
            }
        }
    }

    private void initHangBillList(){
        //表中区
        mHbCursorAdapter = new SimpleCursorAdapter(mContext,R.layout.hangbill_content_layout,null,new String[]{"_id","hang_id","h_amt","oper_date"},new int[]{R.id.row_id,R.id.hang_id,R.id.h_amt,R.id.h_time},1);
        mHbCursorAdapter.setViewBinder(((view, cursor, columnIndex) -> {
            if (view.getId() == R.id.hang_id ){
                TextView hang_id_v = (TextView)view;
                View view_tmp = (View)hang_id_v.getParent();

                if (mCurrentHangId == null){
                    mCurrentHangId = hang_id_v.getText().toString();
                    setViewBackgroundColor(view_tmp,true);
                }else{
                    if (mCurrentHangId.equals(cursor.getString(columnIndex))){
                        setViewBackgroundColor(view_tmp,true);
                    }else{
                        setViewBackgroundColor(view_tmp,false);
                    }
                }
                return false;
            }
            if ("h_amt".equals(cursor.getColumnName(columnIndex))){
                if (view instanceof TextView){
                    ((TextView) view).setText(String.format(Locale.CHINA,"%.2f",cursor.getDouble(columnIndex)));
                }
                return true;
            }
            return false;
        }));

        final ListView hang_bill_list = findViewById(R.id.hangbill_list);
        if (null != hang_bill_list){
            hang_bill_list.setOnItemClickListener((parent, view, position, id) -> {
                if (view == null)return;
                TextView hang_id_v = view.findViewById(R.id.hang_id);
                if (hang_id_v != null){
                    mCurrentHangId = hang_id_v.getText().toString();
                    showVipInfo();
                    loadHangBillDetail(mCurrentHangId);
                    mHbCursorAdapter.notifyDataSetChanged();
                }
            });
            hang_bill_list.setAdapter(mHbCursorAdapter);
            loadHangBill(null);
            if (mHbCursorAdapter.getCount() != 0){
                hang_bill_list.performItemClick(hang_bill_list.getAdapter().getView(0, null, null), 0, hang_bill_list.getItemIdAtPosition(0));
            }
        }
    }

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null){
            int color;
            if (s){
                color = mContext.getColor(R.color.listSelected);
            }else{
                color = mContext.getColor(R.color.white);
            }
            view.setBackgroundColor(color);
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

    private void initHangBillDetail(){
        //表中区
        final ListView mHangBillDetails = findViewById(R.id.hangbill_details_list);
        mHbDetailCursorAdapter = new SimpleCursorAdapter(mContext,R.layout.hangbill_detail_content_layout,null,new String[]{"_id","barcode","goods_title","xnum","sale_price","discount","sale_amt"},
                new int[]{R.id.row_id,R.id.barcode,R.id.goods_title,R.id.xnum,R.id.h_sale_price,R.id.h_discount,R.id.h_sale_amt},1);
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
        if (null != mHangBillDetails)
            mHangBillDetails.setAdapter(mHbDetailCursorAdapter);
    }

    private void loadHangBill(final String hang_id){
        if (mHbCursorAdapter != null){
            String sql = "SELECT _id,hang_id,amt h_amt,oper_date FROM hangbill";
            if (hang_id != null){
                sql = sql + " where cas_id = "+ mContext.getCashierInfo().getIntValue("cas_id") +" and hang_id like '" + hang_id +"%'";
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
                Cursor cursor = SQLiteHelper.getCursor("SELECT _id,barcode,goods_title,xnum,sale_price,discount,sale_amt,barcode_id FROM hangbill_detail where cas_id = " + mContext.getCashierInfo().getIntValue("cas_id") +" and hang_id = " + hang_id,null);
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
            mCurrentHangId = null;
            loadHangBill(null);
            mHbDetailCursorAdapter.changeCursor(null);
            if (mHbCursorAdapter.isEmpty())dismiss();
        }
        return code;
    }

    private boolean verifyDeleteBillPermissions(){
        return mContext.verifyPermissions("3",null);
    }

    private void showVipInfo(){
        if (mCurrentHangId != null){
            JSONObject object = new JSONObject();
            if (SQLiteHelper.execSql(object,"SELECT ifnull(vip_name,'') vip_name,ifnull(vip_mobile,'') vip_mobile FROM hangbill where hang_id = " + mCurrentHangId)){
                String sz_name = object.getString("vip_name"),sz_mobile = object.getString("vip_mobile");
                if (null == mVipInfoView){
                    mVipInfoView = findViewById(R.id.vip_info_linearLayout);
                }
                if (!"".equals(sz_mobile) && !"".equals(sz_name)){
                    if (mVipInfoView.getVisibility() == View.GONE){
                        mVipInfoView.setVisibility(View.VISIBLE);
                    }
                    TextView t_name = mVipInfoView.findViewById(R.id.vip_name),t_mobile = mVipInfoView.findViewById(R.id.vip_phone_num);
                    if (null != t_name && null != t_mobile){
                        t_name.setText(sz_name);
                        t_mobile.setText(sz_mobile);
                    }
                }else{
                    if (mVipInfoView.getVisibility() == View.VISIBLE){
                        mVipInfoView.setVisibility(View.GONE);
                    }
                }
            }else{
                MyDialog.ToastMessage("显示会员信息错误：" + object.getString("info"),mContext,getWindow());
            }
        }
    }

    private void to_checkout(){
        if (mGetListener != null && mHbDetailCursorAdapter != null){
            if (mCurrentHangId != null){
                final StringBuilder err = new StringBuilder();
                final JSONArray barcode_ids = SQLiteHelper.getListToJson("SELECT barcode_id,only_coding,gp_id,"+ GoodsInfoViewAdapter.W_G_MARK +",sale_price price,xnum,sale_amt FROM hangbill_detail where hang_id = " + mCurrentHangId, err);
                if (null != barcode_ids) {
                    JSONObject object = new JSONObject();
                    if (SQLiteHelper.execSql(object,"SELECT ifnull(card_code,'') card_code FROM hangbill where hang_id = " + mCurrentHangId)){
                        String card_code = object.getString("card_code");
                        if (!"".equals(card_code)) {
                            final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
                            progressDialog.setCancel(false).setMessage("正在查询会员信息...").show();
                            CustomApplication.execute(() -> {
                                try {
                                    final JSONArray vips = VipInfoDialog.serchVip(card_code);
                                    mContext.runOnUiThread(() -> {
                                        if (deleteBill(mCurrentHangId, err)) {
                                            mGetListener.onGet(barcode_ids, vips.getJSONObject(0));
                                        } else {
                                            MyDialog.ToastMessage("删除挂单信息错误：" + err, mContext, null);
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    mContext.runOnUiThread(() -> MyDialog.ToastMessage(e.getMessage(), mContext, null));
                                }
                                progressDialog.dismiss();
                            });
                        } else {
                            if (deleteBill(mCurrentHangId, err)) {
                                mGetListener.onGet(barcode_ids, null);
                            } else {
                                MyDialog.ToastMessage("删除挂单信息错误：" + err, mContext, getWindow());
                            }
                        }
                    }else{
                        MyDialog.ToastMessage("查询会员信息错误：" + object.getString("info"),mContext,getWindow());
                    }
                } else {
                    MyDialog.ToastMessage("查询挂单明细错误：" + err, mContext, getWindow());
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

                hang_id = getHangId();

                if (hang_id > 0){
                    for (int i = 0,length = array.size();i < length; i++){
                        tmp_obj = new JSONObject();
                        data = array.getJSONObject(i);
                        amt += data.getDouble("sale_amt");

                        tmp_obj.put("hang_id",hang_id);
                        tmp_obj.put("row_id",i + 1);
                        tmp_obj.put("stores_id",stores_id);
                        tmp_obj.put("gp_id",data.getIntValue("gp_id"));
                        tmp_obj.put("barcode_id",data.getIntValue("barcode_id"));
                        tmp_obj.put("barcode",data.getString("barcode"));
                        tmp_obj.put("only_coding",data.getString("only_coding"));
                        tmp_obj.put(GoodsInfoViewAdapter.W_G_MARK,data.getString(GoodsInfoViewAdapter.W_G_MARK));
                        tmp_obj.put("goods_title",data.getString("goods_title"));
                        tmp_obj.put("original_price",data.getDouble("original_price"));
                        tmp_obj.put("sale_price",data.getDouble("price"));
                        tmp_obj.put("xnum",data.getDouble("xnum"));
                        tmp_obj.put("unit_name",data.getString("unit_name"));
                        tmp_obj.put("sale_amt",data.getDouble("sale_amt"));
                        tmp_obj.put("discount",data.getDouble("discount") * 100);
                        tmp_obj.put("discount_amt",data.getDouble("discount_amt"));
                        tmp_obj.put("sale_man",data.getString("sale_man"));
                        tmp_obj.put("cas_id",cas_id);

                        details.add(tmp_obj);
                    }
                    data = new JSONObject();
                    data.put("stores_id",stores_id);
                    data.put("hang_id",hang_id);
                    data.put("amt",amt);
                    data.put("cas_id",cas_id);
                    data.put("cas_name",cas_name);
                    if (null != vip){
                        data.put("card_code",vip.getString("card_code"));
                        data.put("vip_name",vip.getString("name"));
                        data.put("vip_mobile",vip.getString("mobile"));
                    }
                    orders.add(data);

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

    private int getHangId(){
        final JSONObject data = new JSONObject();
        if (!SQLiteHelper.execSql(data,"select ifnull(max(hang_id),0) + 1 hang_id from hangbill where cas_id = " + mContext.getCashierInfo().getIntValue("cas_id"))){
            mContext.runOnUiThread(()->MyDialog.ToastMessage("查询挂单号错误：" + data.getString("info"),mContext,mContext.getWindow()));
            return 0;
        }
        return data.getIntValue("hang_id");
    }

    public static int getHangCounts(MainActivity context){
        final JSONObject data = new JSONObject();
        if (!SQLiteHelper.execSql(data,"select count(1) hang_counts from hangbill where cas_id = " + context.getCashierInfo().getIntValue("cas_id"))){
            context.runOnUiThread(()->MyDialog.ToastMessage("查询挂单数错误：" + data.getString("info"),context,context.getWindow()));
            return 0;
        }
        return data.getIntValue("hang_counts");
    }

    public void setGetBillDetailListener(OnGetBillListener listener){
        mGetListener = listener;
    }

    public interface OnGetBillListener{
        void onGet(final JSONArray array,final JSONObject vip);
    }
}
