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
public class TimeCardSaleQueryFragment extends AbstractMobileFragment {
    private EditText mSearchContent;
    private OrderAdapter mAdapter;

    @BindView(R.id._tab_layout)
    TabLayout _tab_layout;
    @BindView(R.id.m_start_date)
    TextView mStartDate;
    @BindView(R.id.m_end_date)
    TextView mEndDate;
    @BindView(R.id.switch_condition)
    TextView mCondition;

    @Override
    protected int getRootLayout() {
        return R.layout.time_card_sale_query_fragment;
    }

    @Override
    protected void viewCreated() {
        EventBus.getDefault().register(this);

        initTimeTv();
        initSearchContent();
        initSwitchCondition();
        initOrderList();

        initTab();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void handleMsg(Integer status){
        if (null != mAdapter){
            mAdapter.updateSelectItem(status);
        }
    }

    private void initOrderList(){
        final RecyclerView _order_list = findViewById(R.id._order_list);
        if (null != _order_list){
            mAdapter = new OrderAdapter(getContext());
            _order_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            _order_list.setAdapter(mAdapter);
            _order_list.addItemDecoration(new LinearItemDecoration(mContext.getColor(R.color.white)));
        }
    }
    private void initSwitchCondition(){
        final JSONArray array = createSwitchConditionContentAndSetDefaultValue(mCondition);
        mCondition.setOnClickListener(v -> {
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,mContext.getString(R.string.query_way_sz));
            treeListDialog.setData(array,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    mCondition.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    mCondition.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                }
            });
        });
    }
    private JSONArray createSwitchConditionContentAndSetDefaultValue(@NonNull final TextView view){
        final JSONArray array = getConditionSwitchContent();
        if (!array.isEmpty()){
            final JSONObject object = array.getJSONObject(0);
            view.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
            view.setText(object.getString(TreeListBaseAdapter.COL_NAME));
        }
        return array;
    }

    private JSONArray getConditionSwitchContent() {
        final JSONArray array = new JSONArray();
        final String search_hint = mContext.getString(R.string.m_search_hint);
        if (search_hint != null){
            final String[] sz = search_hint.split("/");
            for (int i = 0,length = sz.length;i < length;i ++){
                final JSONObject object = new JSONObject();
                object.put(TreeListBaseAdapter.COL_ID,i + 1);
                object.put(TreeListBaseAdapter.COL_NAME,sz[i]);
                array.add(object);
            }
        }
        return array;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText order_vip_search = findViewById(R.id.order_vip_search);
        order_vip_search.setTransformationMethod(new ReplacementTransformationMethod() {
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
        order_vip_search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                _tab_layout.selectTab(_tab_layout.getTabAt(_tab_layout.getSelectedTabPosition()));
                return true;
            }
            return false;
        });
        order_vip_search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = order_vip_search.getWidth();
                if (dx > (w - order_vip_search.getCompoundPaddingRight())) {
                    _tab_layout.selectTab(_tab_layout.getTabAt(_tab_layout.getSelectedTabPosition()));
                }
            }
            return false;
        });
        mSearchContent = order_vip_search;
    }

    private void initTimeTv(){
        mStartDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
        mStartDate.setOnClickListener(v -> Utils.showDatePickerDialog(mContext,mStartDate, Calendar.getInstance()));
        mEndDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
        mEndDate.setOnClickListener(v -> Utils.showDatePickerDialog(mContext,mEndDate, Calendar.getInstance()));

        mStartDate.addTextChangedListener(textWatcher);
        mEndDate.addTextChangedListener(textWatcher);
    }
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            _tab_layout.selectTab(_tab_layout.getTabAt(_tab_layout.getSelectedTabPosition()));
        }
    };

    private void initTab(){
        _tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                selectTab(tab);
            }
        });

        _tab_layout.addTab(_tab_layout.newTab().setText(R.string.today_sz));
        _tab_layout.addTab(_tab_layout.newTab().setText(R.string.yesterday_sz));
        _tab_layout.addTab(_tab_layout.newTab().setText(R.string.other_sz));
    }
    private void selectTab(TabLayout.Tab tab){
        int index = tab.getPosition();
        final Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeZone(TimeZone.getDefault());

        long start = 0,end = 0;

        LinearLayout _query_time_layout = findViewById(R.id._query_time_layout);
        switch (index){
            case 1://today
                _query_time_layout.setVisibility(View.GONE);
                rightNow.setTime(new Date());

                rightNow.add(Calendar.DAY_OF_YEAR,-1);

                setStartTime(rightNow);
                start = rightNow.getTime().getTime();

                setEndTime(rightNow);
                end = rightNow.getTime().getTime();
                break;
            case 2://other
                _query_time_layout.setVisibility(View.VISIBLE);
                final SimpleDateFormat sdf = new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA);
                try {
                    rightNow.setTime(Objects.requireNonNull(sdf.parse(mStartDate.getText() + " 00:00:00")));
                    start = rightNow.getTime().getTime();
                    rightNow.setTime(Objects.requireNonNull(sdf.parse(mEndDate.getText() + " 23:59:59")));
                    end = rightNow.getTime().getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            default:
                _query_time_layout.setVisibility(View.GONE);
                rightNow.setTime(new Date());

                setStartTime(rightNow);
                start = rightNow.getTime().getTime();

                setEndTime(rightNow);
                end = rightNow.getTime().getTime();
        }
        query(start / 1000,end / 1000);
    }
    private void query(long start,long end){
        mAdapter.setData(generateQueryCondition(start,end));
    }

    @Override
    public String getTitle(){
        return CustomApplication.self().getString(R.string.once_card_sale_sz) + CustomApplication.self().getString(R.string.query_sz);
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
