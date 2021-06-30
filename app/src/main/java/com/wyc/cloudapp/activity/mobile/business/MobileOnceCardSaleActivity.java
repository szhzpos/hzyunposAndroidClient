package com.wyc.cloudapp.activity.mobile.business;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.BasketView;
import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.MobileOnceCardSaleAdapter;
import com.wyc.cloudapp.bean.OnceCardInfo;
import com.wyc.cloudapp.bean.OnceCardSaleInfo;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.decoration.SaleGoodsItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

import butterknife.BindView;

import static com.wyc.cloudapp.constants.ScanCallbackCode.CODE_REQUEST_CODE;

public class MobileOnceCardSaleActivity extends AbstractMobileActivity implements View.OnClickListener {
    private Button mCurrentBtn;
    private EditText mSearchContent;
    private VipInfo mVip;
    private JSONObject mSaleManInfo;
    private MobileOnceCardSaleAdapter mSaleAdapter;

    @BindView(R.id.basketView)
    BasketView mBasketView;
    @BindView(R.id.sale_amt_tv)
    TextView sale_amt_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getIntent().getStringExtra("title"));

        initFunctionBtn();
        initSearchContent();
        initVipBtn();
        initSaleBtn();
        initSaleOnceCardAdapter();
    }

    private void initSaleOnceCardAdapter(){
        final RecyclerView sale_once_card_list = findViewById(R.id.sale_once_card_list);
        mSaleAdapter = new MobileOnceCardSaleAdapter(this);
        sale_once_card_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        sale_once_card_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),3));
        sale_once_card_list.setAdapter(mSaleAdapter);
    }

    private void initSaleBtn(){
        final Button btn = findViewById(R.id.mobile_sale_man_btn);
        if (btn != null)
            btn.setOnClickListener(v -> {
                final TextView sale_man_name = findViewById(R.id.sale_man_name);
                final JSONObject object = AbstractVipChargeDialog.showSaleInfo(this);

                final String name = Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_NAME);

                mSaleManInfo = new JSONObject();
                mSaleManInfo.put("id",Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID));
                mSaleManInfo.put("name",name);
                sale_man_name.setText(name);
            });
    }

    private void initVipBtn(){
        final Button btn = findViewById(R.id.mobile_vip_btn);
        if (btn != null)
            btn.setOnClickListener(v -> {
                final VipInfoDialog vipInfoDialog = new VipInfoDialog(this);
                if (mVip != null){
                    if (1 == MyDialog.showMessageToModalDialog(this,"已存在会员信息,是否清除？")){
                        clearVipInfo();
                    }
                }else
                    vipInfoDialog.setYesOnclickListener(dialog -> {showVipInfo(dialog.getVipBean());dialog.dismiss(); }).show();
            });
    }

    public void clearVipInfo(){
        final ConstraintLayout mobile_bottom_btn_layout = findViewById(R.id.mobile_bottom_btn_layout);
        final TextView vip_name_tv = mobile_bottom_btn_layout.findViewById(R.id.vip_name);
        if (null != vip_name_tv){
            vip_name_tv.setText(getText(R.string.space_sz));
        }
        mVip = null;
    }

    public void showVipInfo(final VipInfo vip){
        final ConstraintLayout mobile_bottom_btn_layout = findViewById(R.id.mobile_bottom_btn_layout);
        if ( vip != null && mobile_bottom_btn_layout != null){
            final TextView vip_name_tv = mobile_bottom_btn_layout.findViewById(R.id.vip_name);
            if (null != vip_name_tv){
                vip_name_tv.setText(vip.getName());
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.mobile_search_content);
        search.setTransformationMethod(new ReplacementTransformationMethod() {
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
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                queryOnceCardById(search.getText().toString());
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    search.requestFocus();
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    startActivityForResult(intent, CODE_REQUEST_CODE);
                }else if(dx < search.getCompoundPaddingLeft()){
                    SelectOnceCardActivity.start(this);
                }
            }
            return false;
        });
        mSearchContent = search;
    }

    private void queryOnceCardById(String id) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SelectOnceCardActivity.SELECT_ONCE_CARD){
                OnceCardInfo info = SelectOnceCardActivity.getOnceCardInfo(data);
                Logger.d(info);
                add(info);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void add(OnceCardInfo info){
        final OnceCardSaleInfo saleInfo = new OnceCardSaleInfo.Builder()
                .onceCardId(info.getOnce_card_id())
                .name(info.getTitle())
                .price(info.getPrice())
                .num(1).build();
        mSaleAdapter.addOnceCard(saleInfo);
    }

    private void initFunctionBtn(){
        final InterceptLinearLayout func_btn_layout = findViewById(R.id.func_btn_layout);
        final Button today = func_btn_layout.findViewById(R.id.once_card_sale_btn);
        func_btn_layout.post(()->{
            float corner_size = func_btn_layout.getHeight() / 2.0f;
            func_btn_layout.setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,getColor(R.color.transparent), Utils.dpToPx(this,1),getColor(R.color.blue)));
        });
        func_btn_layout.setClickListener(this);
        func_btn_layout.post(today::callOnClick);
    }
    @Override
    public void onClick(View v) {
        final Button btn = (Button) v;

        int white = getColor(R.color.white),text_color = getColor(R.color.text_color),blue = getColor(R.color.blue);
        final int id = btn.getId();

        float corner_size = (float) (btn.getHeight() / 2.0);
        float[] corners = new float[8];

        if (id == R.id.once_card_sale_btn){
            corners[0] = corners[1] =  corners[6] = corners[7] = corner_size;
        }else {
            corners[2] = corners[3] =  corners[4] = corners[5] = corner_size;
        }

        if (btn != mCurrentBtn){
            btn.setTextColor(white);
            btn.setBackground(DrawableUtil.createDrawable(corners,blue,0,blue));
            if (null != mCurrentBtn){
                mCurrentBtn.setTextColor(text_color);
                if (mCurrentBtn.getId() == R.id.m_yesterday_btn){
                    mCurrentBtn.setBackground(getDrawable(R.drawable.left_right_separator));
                }else
                    mCurrentBtn.setBackgroundColor(white);
            }
            mCurrentBtn = btn;
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_once_card;
    }

}