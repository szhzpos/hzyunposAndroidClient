package com.wyc.cloudapp.activity.mobile.cashierDesk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.AbstractSelectAdapter;
import com.wyc.cloudapp.decoration.LinearItemDecoration;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.activity.mobile
 * @ClassName: AbstractSelectActivity
 * @Description: 选择项目activity
 * @Author: wyc
 * @CreateDate: 2021-07-21 16:12
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-21 16:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractSelectActivity<E extends Parcelable,T extends AbstractSelectAdapter<E,?>> extends AbstractDefinedTitleActivity {
    public static final int SELECT_ITEM = 0x000000cc;
    public static final String ITEM_KEY = "I";
    private T mAdapter;
    private EditText mSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);

        initTimeCardInfo();

        initSearchContent();

        showActivity();
    }

    protected abstract T getAdapter();
    protected abstract void loadData(final String c);

    protected final void setData(List<E> data){
        if (null != mAdapter)mAdapter.setDataForList(data);
    }

    private void initTimeCardInfo(){
        final RecyclerView once_card_list = findViewById(R.id.once_card_list);
        mAdapter = getAdapter();
        once_card_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        once_card_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),3));
        mAdapter.setSelectListener(this::setResult);
        once_card_list.setAdapter(mAdapter);
    }
    private void setResult(E cardInfo){
        final Intent intent = new Intent();
        intent.putExtra(ITEM_KEY, cardInfo);
        setResult(RESULT_OK,intent);
        finish();
    }
    public static <E extends Parcelable> E getItem(@NonNull Intent intent){
        return intent.getParcelableExtra(ITEM_KEY);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.search_once_card);
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    loadData(search.getText().toString());
                }
            }
            return false;
        });
        mSearch = search;
    }

    @Override
    public boolean hookEnterKey() {
        if (mSearch != null)loadData(mSearch.getText().toString());
        return true;
    }

    @Override
    protected final int getContentLayoutId() {
        return R.layout.activity_select_once_card;
    }

    private void showActivity(){
        final Intent intent = getIntent();
        if (null != intent){
            List<E> data = intent.getParcelableArrayListExtra("result");
            if (null !=data){
                mAdapter.setDataForList(data);
            }else loadData(mSearch.getText().toString());
        }else loadData(mSearch.getText().toString());
    }
}
