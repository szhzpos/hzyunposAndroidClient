package com.wyc.cloudapp.mobileFragemt;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.QueryCondition;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import butterknife.BindView;

import static com.wyc.cloudapp.utils.FormatDateTimeUtils.setEndTime;
import static com.wyc.cloudapp.utils.FormatDateTimeUtils.setStartTime;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: AbstractTimeCardQueryFragment
 * @Description: 次卡查询父类
 * @Author: wyc
 * @CreateDate: 2021-07-19 16:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-19 16:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractTimeCardQueryFragment extends AbstractMobileFragment{
    protected AbstractDataAdapterForList<?,? extends AbstractDataAdapter.SuperViewHolder> mAdapter;
    private EditText mSearchContent;

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

    @CallSuper
    @Override
    protected void viewCreated() {
        initTimeTv();
        initSearchContent();
        initSwitchCondition();
        initOrderList();

        initTab();
    }

    protected abstract AbstractDataAdapterForList<?,? extends AbstractDataAdapter.SuperViewHolder> getAdapter();
    protected abstract void query(@NonNull QueryCondition condition);

    private void initOrderList(){
        final RecyclerView _order_list = findViewById(R.id._order_list);
        if (null != _order_list){
            mAdapter = getAdapter();
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
                if (i == 0)
                    object.put(TreeListBaseAdapter.COL_ID,QueryCondition.ORDER);
                else
                    object.put(TreeListBaseAdapter.COL_ID,QueryCondition.VIP_CODE);

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

        query(new QueryCondition(start / 1000,end / 1000,Utils.getViewTagValue(mCondition,QueryCondition.ORDER),mSearchContent.getText().toString()));
    }
}
