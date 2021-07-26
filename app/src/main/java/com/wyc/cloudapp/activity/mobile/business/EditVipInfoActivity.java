package com.wyc.cloudapp.activity.mobile.business;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.bean.VipGrade;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ArrayCallback;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class EditVipInfoActivity extends AbstractEditArchiveActivity {
    private List<VipGrade> vipGradeList;
    private String mBirthdayType;
    private TextView mVipBirthday;

    @BindView(R.id.p_num_et)
    EditText p_num_et;

    @BindView(R.id.card_et)
    EditText card_et;

    @BindView(R.id.name_et)
    EditText name_et;

    @BindView(R.id.grade_tv)
    TextView grade_tv;

    @BindView(R.id.sale_man_tv)
    TextView sale_man_tv;

    @BindView(R.id.referrer_et)
    EditText referrer_et;

    @BindView(R.id.remark_mt)
    EditText remark_mt;

    @BindView(R.id.male_rb)
    RadioButton male_rb;

    @BindView(R.id.female_rb)
    RadioButton female_rb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getString(R.string.add_vip_sz));

        initGrade();
        initSaleMan();
        initVipBirthdayEt();
        initBirthdayType();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        queryVipLevel();
    }
    private void queryVipLevel(){
        final JSONObject param = new JSONObject();
        param.put("appid",getAppId());
        HttpUtils.sendAsyncPost(getUrl() + InterfaceURL.VIP_GRADE,HttpRequest.generate_request_parm(param,getAppSecret()))
                .enqueue(new ArrayCallback<VipGrade>(VipGrade.class,true) {
                    @Override
                    protected void onError(String msg) {
                        MyDialog.toastMessage(msg);
                    }

                    @Override
                    protected void onSuccessForResult(@Nullable List<VipGrade> d, String hint) {
                        if (d != null){
                            VipGrade vipGrade = d.get(0);
                            grade_tv.setText(vipGrade.getGrade_name());
                            grade_tv.setTag(vipGrade.getGrade_id());
                            vipGradeList = d;
                        }
                    }
                });
    }
    private void initGrade(){
        grade_tv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
            final TreeListDialogForObj treeListDialog = new TreeListDialogForObj(this,"等级");
            treeListDialog.setData(convertGrade(),null,true);
            if (treeListDialog.exec() == 1){
                final TreeListItem object = treeListDialog.getSingleContent();
                grade_tv.setText(object.getItem_name());
                grade_tv.setTag(object.getItem_id());
            }
        }));
    }
    private List<TreeListItem> convertGrade(){
        List<TreeListItem> data = new ArrayList<>();
        if (null != vipGradeList){
            for (VipGrade vipGrade : vipGradeList){
                final TreeListItem item = new TreeListItem();
                item.setItem_id(vipGrade.getGrade_id());
                item.setCode(vipGrade.getGrade_id());
                item.setItem_name(vipGrade.getGrade_name());
                data.add(item);
            }
        }
        return data;
    }

    private void initSaleMan(){
        sale_man_tv.setOnClickListener(v -> {
            final JSONObject object = AbstractVipChargeDialog.showSaleInfo(EditVipInfoActivity.this);
            sale_man_tv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
            sale_man_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
        });
    }

    private void initVipBirthdayEt(){
        final TextView et = mVipBirthday = findViewById(R.id.n_vip_birthday);
        et.setOnFocusChangeListener((view, b) -> {
            Utils.hideKeyBoard((EditText) view);if (b)view.callOnClick();});
        et.setOnClickListener(view->{
            Calendar c = Calendar.getInstance();
            // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
            new DatePickerDialog(view.getContext(),
                    // 绑定监听器
                    (view1, year, monthOfYear, dayOfMonth) -> et.setText(String.format(Locale.CHINA,"%d-%02d-%02d", view1.getYear(), view1.getMonth() + 1, view1.getDayOfMonth()))
                    // 设置初始日期
                    , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                    .get(Calendar.DAY_OF_MONTH)).show();});
    }
    private void initBirthdayType(){
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.drop_down_style);

        arrayAdapter.add("农历");
        arrayAdapter.add("新历");

        Spinner b_type = findViewById(R.id.birthday_type);
        b_type.setAdapter(arrayAdapter);
        b_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    mBirthdayType = "2";
                }else {
                    mBirthdayType = "1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        b_type.setSelection(1);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_vip_info;
    }

    @Override
    protected void sure() {
        add(false);
    }

    @Override
    protected void saveAndAdd() {
        add(true);
    }
    
    private void add(boolean reset){
        final JSONObject param = getParameter();
        if (!param.isEmpty()){
            param.put("appid",getAppId());
            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_save_data_sz));
            HttpUtils.sendAsyncPost(getUrl() + "/api/member/mk",HttpRequest.generate_request_parm(param,getAppSecret())).enqueue(new ObjectCallback<String>(String.class,true) {
                @Override
                protected void onError(String msg) {
                    progressDialog.dismiss();
                    MyDialog.toastMessage(msg);
                }
                @Override
                protected void onSuccessForResult(String d,final String hint) {
                    progressDialog.dismiss();
                    MyDialog.toastMessage(hint);
                    if (reset){
                        reset();
                    }else finish();
                }
            });
        }
    }

    private JSONObject getParameter(){
        final String p_num = p_num_et.getText().toString(),card_no = card_et.getText().toString(),name = name_et.getText().toString(),birthday  = mVipBirthday.getText().toString();
        final JSONObject param = new JSONObject();
        if (!Utils.isNotEmpty(p_num)){
            p_num_et.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.vip_ph_num_not_colon_sz)), getWindow());
            return param;
        }
        if (!Utils.isNotEmpty(card_no)){
            card_et.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.card_code_sz)), getWindow());
            return param;
        }
        if (!Utils.isNotEmpty(name)){
            name_et.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.vip_name_sz)), getWindow());
            return param;
        }
        if (!Utils.isNotEmpty(birthday)){
            mVipBirthday.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.input_birthday_hint_sz)), getWindow());
            return param;
        }

        param.put("mobile",p_num);
        param.put("grade_id",grade_tv.getTag());
        param.put("name",name);
        param.put("sex",male_rb.isChecked() ? "男" : female_rb.isChecked() ? "女" : "-");
        param.put("card_code",card_no);
        param.put("birthday",mVipBirthday.getText().toString());
        param.put("birthday_type",mBirthdayType);
        param.put("re_mobile",referrer_et.getText().toString());
        param.put("sc_id",sale_man_tv.getTag());
        param.put("stores_id",getStoreId());
        param.put("origin",1);

        return param;
    }

    private void reset(){
        p_num_et.setText(R.string.space_sz);
        card_et.setText(R.string.space_sz);
        name_et.setText(R.string.space_sz);
        mVipBirthday.setText(R.string.space_sz);
        sale_man_tv.setText(R.string.space_sz);
        referrer_et.setText(R.string.space_sz);
        referrer_et.setText(R.string.space_sz);
        remark_mt.setText(R.string.space_sz);
    }

    public static void start(MobileVipManageActivity context){
        final Intent intent = new Intent();
        intent.setClass(context,EditVipInfoActivity.class);
        context.startActivity(intent);
    }
}