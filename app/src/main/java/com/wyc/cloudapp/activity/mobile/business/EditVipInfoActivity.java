package com.wyc.cloudapp.activity.mobile.business;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.bean.TreeListItem;
import com.wyc.cloudapp.adapter.bean.VipGrade;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditVipInfoActivity extends AbstractEditArchiveActivity {
    private List<VipGrade> vipGradeList;
    private String mBirthdayType;
    private TextView mVipBirthday;

    @BindView(R.id.grade_tv)
    TextView grade_tv;
    @BindView(R.id.sale_man_tv)
    TextView sale_man_tv;

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
        Observable.create((ObservableOnSubscribe<List<VipGrade>>) emitter -> {
            final JSONObject param = new JSONObject();
            param.put("appid",getAppId());
            JSONObject ret_obj = HttpUtils.sendPost(getUrl() + "/api/member/get_member_grade", HttpRequest.generate_request_parm(param,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(ret_obj)){
                try {
                    ret_obj = JSONObject.parseObject(ret_obj.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(ret_obj)){
                        final JSONArray data = ret_obj.getJSONArray("data");
                        emitter.onNext(data.toJavaList(VipGrade.class));
                    }else throw new JSONException(ret_obj.getString("info"));
                }catch (JSONException e){
                    emitter.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(vipGrades -> {
            if (null != vipGrades && !vipGrades.isEmpty()){
                VipGrade vipGrade = vipGrades.get(0);
                grade_tv.setText(vipGrade.getGrade_name());
                grade_tv.setTag(vipGrade.getGrade_id());
                vipGradeList = vipGrades;
            }
        }, throwable -> {
            throwable.printStackTrace();
            MyDialog.ToastMessage(throwable.getMessage(),this,null);
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

    }

    @Override
    protected void saveAndAdd() {

    }

    public static void start(MobileVipManageActivity context){
        final Intent intent = new Intent();
        intent.setClass(context,EditVipInfoActivity.class);
        context.startActivity(intent);
    }
}