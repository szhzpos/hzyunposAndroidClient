package com.wyc.cloudapp.dialog.orderDialog;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MobileQueryRetailOrderDialog extends AbstractDialogMainActivity {
    protected TextView mStartDateTv,mEndDateTv;
    public MobileQueryRetailOrderDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.m_sale_query_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initQueryTimeBtn();
        initEndDateAndTime();
        initStartDateAndTime();
        initSearchContent();
        initSwitchCondition();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_retail_order_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void initEndDateAndTime(){
        final TextView end_date = mEndDateTv = findViewById(R.id.m_end_date);
        if (null != end_date){
            end_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            end_date.setOnClickListener(v -> Utils.showDatePickerDialog(mContext,(TextView) v, Calendar.getInstance()));
        }
    }
    private void initStartDateAndTime(){
        final TextView start_date = mStartDateTv = findViewById(R.id.m_start_date);
        if (null != start_date) {
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            start_date.setOnClickListener(v -> Utils.showDatePickerDialog(mContext, (TextView) v, Calendar.getInstance()));
        }
    }

    private void initQueryTimeBtn(){

        final LinearLayout m_query_time_tv_layout = findViewById(R.id.m_query_time_tv_layout);

        final Button today = findViewById(R.id.m_today_btn),yesterday = findViewById(R.id.m_yesterday_btn),other = findViewById(R.id.m_other_btn);
        int white = mContext.getColor(R.color.white),text_color = mContext.getColor(R.color.text_color),blue = mContext.getColor(R.color.blue),borderWidth = Utils.dpToPx(mContext,1);
        final ColorStateList colorStateList = DrawableUtil.createColorStateList(text_color,white,text_color,text_color);

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        final Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeZone(TimeZone.getDefault());

        today.setTextColor(colorStateList);
        today.post(()->{
            float corner_size = (float) (today.getHeight() / 2.0);
            float[] corners = new float[]{corner_size,corner_size,0,0,0,0,corner_size,corner_size};
            final Drawable press = DrawableUtil.createDrawable(corners,blue,borderWidth,blue);
            today.setBackground(DrawableUtil.createStateDrawable(press,DrawableUtil.createDrawable(corners,white,borderWidth,blue)));
        });
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_query_time_tv_layout.setVisibility(View.GONE);

                rightNow.setTime(new Date());

                long start_time = -1,end_time = -1;
                setStartTime(rightNow);
                start_time = rightNow.getTime().getTime();

                setEndTime(rightNow);
                end_time = rightNow.getTime().getTime();


                Logger.d("start:%s,end:%s",sdf.format(new Date(start_time)),sdf.format(new Date(end_time)));

            }
        });


        yesterday.setTextColor(colorStateList);
        yesterday.post(()->{
            float[] corners = new float[]{0,0,0,0,0,0,0,0};
            final Drawable press = DrawableUtil.createDrawable(corners,blue,borderWidth,blue);
            yesterday.setBackground(DrawableUtil.createStateDrawable(press,DrawableUtil.createDrawable(corners,white,borderWidth,blue)));
        });
        yesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_query_time_tv_layout.setVisibility(View.GONE);

                long start_time = -1,end_time = -1;

                rightNow.setTime(new Date());

                rightNow.add(Calendar.DAY_OF_YEAR,-1);

                setStartTime(rightNow);
                start_time = rightNow.getTime().getTime();

                setEndTime(rightNow);
                end_time = rightNow.getTime().getTime();

                Logger.d("start:%s,end:%s",sdf.format(new Date(start_time)),sdf.format(new Date(end_time)));
            }
        });

        other.setTextColor(colorStateList);
        other.post(()->{
            float corner_size = (float) (today.getHeight() / 2.0);
            float[] corners = new float[]{0,0,corner_size,corner_size,corner_size,corner_size,0,0};
            final Drawable press = DrawableUtil.createDrawable(corners,blue,borderWidth,blue);
            other.setBackground(DrawableUtil.createStateDrawable(press,DrawableUtil.createDrawable(corners,white,borderWidth,blue)));
        });
        other.setOnClickListener(v -> {
            m_query_time_tv_layout.setVisibility(View.VISIBLE);
        });
    }

    private void setStartTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
    }
    private void setEndTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText order_vip__search = findViewById(R.id.order_vip__search);
        order_vip__search.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                return new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            }

            @Override
            protected char[] getReplacement() {
                return new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            }
        });
        order_vip__search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                query();
                return true;
            }
            return false;
        });
        order_vip__search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = order_vip__search.getWidth();
                if (dx > (w - order_vip__search.getCompoundPaddingRight())) {
                    query();
                }
            }
            return false;
        });
    }

    private void initSwitchCondition(){
        final TextView switch_condition = findViewById(R.id.switch_condition);
        final JSONArray array = createSwitchConditionContentAndSetDefaultValue(switch_condition);
        switch_condition.setOnClickListener(v -> {
            final String pay_method_name_colon_sz = mContext.getString(R.string.pay_method_name_colon_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(mContext,pay_method_name_colon_sz.substring(0,pay_method_name_colon_sz.length() - 1));
            treeListDialog.setDatas(array,null,true);
            switch_condition.post(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    switch_condition.setTag(object.getIntValue("item_id"));
                    switch_condition.setText(object.getString("item_name"));
                }
            });
        });
    }
    private JSONArray createSwitchConditionContentAndSetDefaultValue(@NonNull final TextView view){
        final JSONArray array = new JSONArray();
        final String search_hint = mContext.getString(R.string.m_search_hint);
        if (search_hint != null){
            final String[] sz = search_hint.split("/");
            if (sz != null && sz.length > 1){
                JSONObject object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",1);
                object.put("item_name",sz[0]);

                array.add(object);

                view.setTag(1);
                view.setText(sz[0]);

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",2);
                object.put("item_name",sz[1]);

                array.add(object);
            }
        }
        return array;
    }

    private void query(){
        final String content = "",where_sql;
        if (!content.isEmpty()){
            if (Utils.getViewTagValue(findViewById(R.id.switch_condition),1) == 1){
                where_sql = " order_code ='"+ content +"'";
            }else {
                where_sql = " card_code ='"+ content +"'";
            }
        }else
            where_sql = "";

        final String sql = "SELECT \n" +
                "       a.remark," +
                "       a.card_code," +
                "       a.name vip_name," +
                "       a.mobile," +
                "       a.transfer_status s_e_status,\n" +
                "       case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "       a.upload_status,\n" +
                "       case a.upload_status when 1 then '未上传' when 2 then '已上传' else '其他' end upload_status_name,\n" +
                "       a.pay_status,\n" +
                "       case a.pay_status when 1 then '未支付' when 2 then '已支付' when 3 then '支付中' else '其他' end pay_status_name,\n" +
                "       a.order_status,\n" +
                "       case a.order_status when 1 then '未付款' when 2 then '已付款' when 3 then '已取消' when 4 then '已退货' else '其他'  end order_status_name,\n" +
                "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                "       a.remark,\n" +
                "       a.cashier_id,\n" +
                "       b.cas_name,\n" +
                "       a.discount_price reality_amt,\n" +
                "       a.total order_amt,\n" +
                "       a.order_code,\n" +
                "       c.sc_name\n" +
                "  FROM retail_order a left join cashier_info b on a.cashier_id = b.cas_id left join sales_info c on a.sc_ids = c.sc_id " + where_sql;

        final StringBuilder err = new StringBuilder();
    }
}
