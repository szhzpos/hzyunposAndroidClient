package com.wyc.cloudapp.mobileFragemt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.TimeCardOrderDetailActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.adapter.MobileRetailOrderAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wyc.cloudapp.utils.FormatDateTimeUtils.setEndTime;
import static com.wyc.cloudapp.utils.FormatDateTimeUtils.setStartTime;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: TimeCardSaleQueryFrament
 * @Description: 次卡销售查询
 * @Author: wyc
 * @CreateDate: 2021-07-12 14:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-12 14:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TimeCardSaleQueryFragment extends AbstractTimeCardQueryFragment {
    @Override
    protected void viewCreated() {
        super.viewCreated();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void handleMsg(Integer status){
        if (isVisible() && null != mAdapter){
            ((OrderAdapter)mAdapter).updateSelectItem(status);
        }
    }

    @Override
    protected OrderAdapter getAdapter() {
        return new OrderAdapter(mContext);
    }

    @Override
    protected void query(long start, long end){
        ((OrderAdapter)mAdapter).setData(generateQueryCondition(start,end));
    }

    private String generateQueryCondition(long start,long end) {
        final String content = mSearchContent.getText().toString();
        final StringBuilder where_sql = new StringBuilder("select * from timeCardSaleOrder where online_order_no is not null and time between "+ start +" and "+ end );
        if (!content.isEmpty()){
            if (Utils.getViewTagValue(mCondition,1) == 1){
                where_sql.append(" and order_no like '%").append(content).append("'");
            }else {
                where_sql.append(" and vip_card_no ='").append(content).append("'");
            }
        }
        where_sql.append(" order by time desc");
        return where_sql.toString();
    }

    @Override
    public String getTitle(){
        return CustomApplication.self().getString(R.string.once_card_sale_sz) + CustomApplication.self().getString(R.string.query_sz);
    }

    static class OrderAdapter extends AbstractDataAdapterForList<TimeCardSaleOrder,OrderAdapter.MyViewHolder> implements View.OnClickListener {
        private final Context mContext;
        private int mSelectIndex = -1;

        public OrderAdapter(Context context){
            mContext = context;
        }
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id._order_detail){
                Object o = v.getTag();
                if (o instanceof TimeCardSaleOrder){
                    mSelectIndex = mData.indexOf(o);
                    TimeCardOrderDetailActivity.start(mContext, (TimeCardSaleOrder)o);
                }

            }
        }

        private void updateSelectItem(int status){
            Logger.d("mSelectIndex:%d,status:%d",mSelectIndex,status);
            TimeCardSaleOrder order = getItem(mSelectIndex);
            if (order != null){
                order.setStatus(status);
                notifyItemChanged(mSelectIndex);
            }
        }

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
            @BindView(R.id.order_code)
            TextView order_code;
            @BindView(R.id._order_status)
            TextView _order_status;
            @BindView(R.id._order_time)
            TextView _order_time;
            @BindView(R.id._order_cas_name)
            TextView _order_cas_name;
            @BindView(R.id._order_amt)
            TextView _order_amt;
            @BindView(R.id._vip_label)
            TextView _vip_label;
            @BindView(R.id._order_detail)
            TextView _order_detail;

            public MyViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = View.inflate(parent.getContext(), R.layout.time_card_sale_query_adapter, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final TimeCardSaleOrder order = getItem(position);
            if (null != order){
                holder.order_code.setText(order.getOnline_order_no());
                holder._order_status.setText(order.getStatusName());
                holder._order_time.setText(order.getFormatTime());
                holder._order_cas_name.setText(order.getCashierName());
                holder._order_amt.setText(String.format(Locale.CHINA,"%.2f",order.getAmt()));
                holder._vip_label.setText(String.format(Locale.CHINA,"会员：%s(%s)",order.getVip_name(),order.getVip_mobile()));

                holder._order_detail.setTag(order);
                if (!holder._order_detail.hasOnClickListeners()){
                    holder._order_detail.setOnClickListener(this);
                }
            }
        }

        private void setData(String query){
            Logger.d("sql:%s",query);
            try {
                setDataForList(TimeCardSaleOrder.getOrderByCondition(query));
            }catch (SQLiteException e){
                e.printStackTrace();
                MyDialog.toastMessage(e.getMessage());
            }
        }

    }
}
