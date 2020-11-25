package com.wyc.cloudapp.dialog.vip;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.MobileCashierActivity;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MobileVipChargeDialog extends AbstractDialogMainActivity implements MobileCashierActivity.ScanCallback {
    private View mRoot;
    private JSONObject mVip,mPayMethodSelected;
    private EditText mSearchContent,mChargeAmtEt,mRemarkEt,mChargePlanEt,mPresentAmtEt,mPayMethodEt;
    private TextView mVip_name,mVip_sex,mVip_p_num,mVip_card_id,mVip_balance,mVip_integral,mVipGrade,mVipDiscount;
    private CustomProgressDialog mProgressDialog;
    private JSONArray mChargePlans,mPayMethods;
    private String mPayCode = "";
    private Button mChargeBtn;
    public MobileVipChargeDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.vip_charge_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(mContext);

        initVipInfoFields();
        initSearchContent();
        initChargeAmt();
        initRemark();
        initChargePlan();
        initPayMethod();
        initPrintSwitch();
        initChargeBtn();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_vip_charge_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        final Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRoot = window.getDecorView();
        //mRoot.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }


    @Override
    public void callback(String code) {
        mPayCode = code;
        mChargeBtn.callOnClick();
    }

    private void initVipInfoFields(){
        mVip_name = findViewById(R.id.vip_name);
        mVip_sex = findViewById(R.id.vip_sex);
        mVip_p_num = findViewById(R.id.vip_p_num);
        mVip_card_id = findViewById(R.id.vip_card_id);
        mVip_balance = findViewById(R.id.vip_balance);
        mVip_integral = findViewById(R.id.vip_integral);
        mVipGrade = findViewById(R.id.vip_grade_tv);
        mVipDiscount = findViewById(R.id.vip_discount);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText _search_content = findViewById(R.id.m_search_content);
        _search_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        _search_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                restInfo();
            }
        });
        _search_content.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getX() > (_search_content.getWidth() - _search_content.getCompoundPaddingRight())){
                        searchVip(_search_content.getText().toString());
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        });
        mSearchContent = _search_content;
    }

    private void searchVip(final String mobile){
        if(mobile != null && mobile.length() != 0){
            mProgressDialog.setMessage("正在查询会员...").show();
            CustomApplication.execute(()->{
                try {
                    final JSONArray array = VipInfoDialog.searchVip(mobile);
                    final JSONObject vip = array.getJSONObject(0);
                    loadChargePlan(Utils.getNullStringAsEmpty(vip,"openid"));
                    mRoot.post(()-> showVipInfo(vip));
                } catch (JSONException e) {
                    e.printStackTrace();
                    mRoot.post(()->{
                        MyDialog.ToastMessage(e.getMessage(),mContext,getWindow());
                    });
                }
                mRoot.post(()->mProgressDialog.dismiss());
            });
        }else{
            MyDialog.ToastMessage(mSearchContent,mSearchContent.getHint().toString(),mContext,getWindow());
        }
    }

    private void loadChargePlan(final String openid){
        JSONObject object = new JSONObject(),ret_json;
        final HttpRequest httpRequest = new HttpRequest();
        if (SQLiteHelper.getLocalParameter("connParam",object)){
            object.put("appid",object.getString("appId"));
            object.put("openid",openid);
            object.put("origin",1);
            ret_json = httpRequest.sendPost(object.getString("server_url") + "/api/member_recharge/member_recharge", HttpRequest.generate_request_parm(object,object.getString("appSecret")),true);
            switch (ret_json.getIntValue("flag")){
                case 0:
                    throw new JSONException(ret_json.getString("info"));
                case 1:
                    ret_json = JSON.parseObject(ret_json.getString("info"));
                    switch (ret_json.getString("status")){
                        case "n":
                            throw new JSONException(ret_json.getString("info"));
                        case "y":
                            final JSONObject data = JSON.parseObject(ret_json.getString("data"));
                            Logger.d_json(data.toString());
                            mChargePlans = parse_charge_plan_and_set_default(data.getJSONArray("money_list"));
                            break;
                    }
                    break;
            }
        }else
            throw new JSONException(object.getString("info"));
    }

    private void showVipInfo(JSONObject object){
        Logger.d(object);
        if (null != object){
            mVip = object;

            final String grade_name = object.getString("grade_name");
            if (grade_name != null)mVipGrade.setText(grade_name);

            mVip_name.setText(object.getString("name"));
            mVip_sex.setText(object.getString("sex"));
            mVip_p_num.setText(object.getString("mobile"));

            mVipDiscount.setText(object.getString("discount"));
            mVip_card_id.setText(object.getString("card_code"));
            mVip_balance.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("money_sum")));
            mVip_integral.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("points_sum")));
        }
    }

    private void restInfo(){
        if (mVip != null){
            final CharSequence space = mContext.getText(R.string.space_sz);
            mVip_name.setText(space);
            mVip_sex.setText(space);
            mVip_p_num.setText(space);
            mVipGrade.setText(space);
            mVipDiscount.setText(space);
            mVip_card_id.setText(space);
            mVip_balance.setText(space);
            mVip_integral.setText(space);

            clearChargePlan();

        }
    }

    private void clearChargePlan(){
        final CharSequence space = mContext.getText(R.string.space_sz);
        mChargePlanEt.setText(space);
        mChargePlanEt.setTag(null);
        mChargeAmtEt.setText(R.string.zero_p_z_sz);
        mPresentAmtEt.setText(R.string.zero_p_z_sz);

        mPayCode = space.toString();
    }

    private void initChargeAmt(){
        mChargeAmtEt = findViewById(R.id.mobile_charge_amt);
    }

    private void initRemark(){
        mRemarkEt = findViewById(R.id.mobile_charge_remark);
    }

    private void initChargePlan(){
        final EditText mobile_charge_plan = findViewById(R.id.mobile_charge_plan);
        mobile_charge_plan.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                v.callOnClick();
            }
        });
        mobile_charge_plan.setOnClickListener(v -> {
            final String charge_plan_sz = mContext.getString(R.string.charge_plan_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(mContext,charge_plan_sz.substring(0,charge_plan_sz.length() - 1));
            treeListDialog.setDatas(mChargePlans,null,true);
            mobile_charge_plan.post(()->{
                if (treeListDialog.exec() == 1){
                    showChargePlan(treeListDialog.getSingleContent(),mobile_charge_plan);
                }
            });
        });
        mChargePlanEt = mobile_charge_plan;
    }

    private void showChargePlan(@NonNull final JSONObject object,final EditText mobile_charge_plan){
        int item_id = object.getIntValue("item_id");
        final String item_name = object.getString("item_name");

        mobile_charge_plan.setText(item_name);
        mobile_charge_plan.setTag(item_id);

        mPresentAmtEt = findViewById(R.id.mobile_present_amt);
        if (item_id != -1){
            mChargeAmtEt.setText(object.getString("money"));
            mChargeAmtEt.setEnabled(false);
            mPresentAmtEt.setText(object.getString("give_money"));
        }else {
            mobile_charge_plan.setText(item_name);

            final String zero_p_z_sz = mContext.getString(R.string.zero_p_z_sz);
            mPresentAmtEt.setText(zero_p_z_sz);
            mChargeAmtEt.setEnabled(true);
            mChargeAmtEt.setText(zero_p_z_sz);
        }
    }

    private JSONArray parse_charge_plan_and_set_default(final JSONArray plans){
        final JSONArray array  = new JSONArray();
        final String default_item_name = "自由充值";
        JSONObject object = new JSONObject();
        object.put("level",0);
        object.put("unfold",false);
        object.put("isSel",false);
        object.put("item_id",-1);
        object.put("item_name",default_item_name);
        array.add(object);

        mChargePlanEt.post(()->{
            mChargePlanEt.setText(default_item_name);
            mChargeAmtEt.setTag(-1);;
        });

        if (plans != null){
            for (int i = 0,size = plans.size();i < size;i++){
                final JSONObject tmp = plans.getJSONObject(i);
                final int id = Utils.getNotKeyAsNumberDefault(tmp,"_id",-1);
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("item_name",tmp.getString("advstr"));
                object.put("money",tmp.getString("money"));
                object.put("give_money",tmp.getString("give_money"));

                array.add(object);
            }
        }
        return array;
    }


    private void initPayMethod(){
        final EditText mobile_pay_method = findViewById(R.id.mobile_pay_method);
        final StringBuilder err = new StringBuilder();
        if (mContext.isConnection())
            mPayMethods = SQLiteHelper.getListToJson("select *  from pay_method where status = '1' and support like '%3%' order by sort",err);
        else
            mPayMethods = SQLiteHelper.getListToJson("select *  from pay_method where is_check = 2 and status = '1' and support like '%3%' order by sort",err);

        final JSONArray array = mPayMethods;
        Logger.d(array);
        if (array != null){
            mobile_pay_method.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)v.callOnClick();
            });
            mobile_pay_method.setOnClickListener(v -> {
                final String pay_method_name_colon_sz = mContext.getString(R.string.pay_method_name_colon_sz);
                final TreeListDialog treeListDialog = new TreeListDialog(mContext,pay_method_name_colon_sz.substring(0,pay_method_name_colon_sz.length() - 1));
                treeListDialog.setDatas(parse_pay_method_and_set_default(mPayMethods),null,true);
                mobile_pay_method.post(()->{
                    if (treeListDialog.exec() == 1){
                        final JSONObject object = treeListDialog.getSingleContent();
                        final String _id = Utils.getNullStringAsEmpty(object,"item_id");
                        mobile_pay_method.setTag(_id);
                        mobile_pay_method.setText(Utils.getNullStringAsEmpty(object,"item_name"));

                        mPayMethodSelected = get_pay_method(_id);

                        launchScan();
                    }
                });
            });

            //默认支付方式-现金
            final JSONObject default_cash = get_pay_method(PayMethodViewAdapter.CASH_METHOD_ID);
            mobile_pay_method.setTag(Utils.getNullStringAsEmpty(default_cash,"pay_method_id"));
            mobile_pay_method.setText(Utils.getNullStringAsEmpty(default_cash,"name"));
            mPayMethodSelected = default_cash;
        }else{
            MyDialog.ToastMessage(err.toString(),mContext,getWindow());
        }

        mPayMethodEt = mobile_pay_method;
    }

    private void launchScan(){
        int is_check = mPayMethodSelected.getIntValue("is_check");
        if (is_check != 2){
            final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            mContext.startActivityForResult(intent, MessageID.PAY_REQUEST_CODE);
            mContext.setScanCallback(this);
        }
    }

    private JSONArray parse_pay_method_and_set_default(final JSONArray methods){
        final JSONArray array  = new JSONArray();
        JSONObject object,method;
        if (methods != null){
            for (int i = 0,size = methods.size();i < size;i++){
                method = methods.getJSONObject(i);
                final int id = Utils.getNotKeyAsNumberDefault(method,"pay_method_id",-1);
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("item_name",method.getString("name"));

                array.add(object);
            }
        }
        return array;
    }

    private JSONObject get_pay_method(final String pay_method_id){
        final JSONArray array = mPayMethods;
        if (array != null && pay_method_id != null){
            for (int i = 0,length = array.size();i < length;i++){
                final JSONObject jsonObject = array.getJSONObject(i);
                if (pay_method_id.equals(jsonObject.getString("pay_method_id"))){
                    return jsonObject;
                }
            }
        }
        return null;
    }

    private void initPrintSwitch(){
        final Switch mobile_print_switch = findViewById(R.id.mobile_print_switch);
        mobile_print_switch.setChecked(mContext.getPrintStatus());
        mobile_print_switch.setOnClickListener(v -> mContext.switchPrintStatus());
    }


    private void initChargeBtn(){
        final Button mobile_charge_btn = findViewById(R.id.mobile_charge_btn);
        mobile_charge_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int is_check = mPayMethodSelected.getIntValue("is_check");
                if ("".equals(mPayCode) && is_check != 2){
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    mContext.startActivityForResult(intent, MessageID.PAY_REQUEST_CODE);
                    mContext.setScanCallback(MobileVipChargeDialog.this);
                }else
                    vip_charge();
            }
        });

        mChargeBtn = mobile_charge_btn;
    }

    private String generate_pay_son_order_id(){
        return "MPAY" + new SimpleDateFormat("yyyyMMdd",Locale.CHINA).format(new Date()) + Utils.getNonce_str(8);
    }

    private boolean verify(){
        return MyDialog.ToastMessage(null,"会员信息不能为空！",mContext,getWindow(),mVip != null);
    }
    private void showPayError(final String message){
        mChargeAmtEt.post(()-> {
            mProgressDialog.dismiss();
            MyDialog.displayErrorMessage(null,message,mContext);
        });
    }

    private void chargeSuccess(final JSONObject member){
        mChargeAmtEt.post(()->{
            mProgressDialog.dismiss();
            MyDialog.ToastMessage("充值成功！",mContext,getWindow());
            clearChargePlan();
            showVipInfo(member);
        });
    }

    private void vip_charge(){
        if (verify()){
            mProgressDialog.setCancel(false).setMessage("正在生成充值订单...").show();
            CustomApplication.execute(()->{
                final HttpRequest httpRequest = new HttpRequest();
                final JSONObject member_order_info = new JSONObject();
                final StringBuilder err = new StringBuilder();

                JSONObject cashier_info = mContext.getCashierInfo(),store_info = mContext.getStoreInfo(),data_ = new JSONObject(),retJson,info_json;
                final String url = mContext.getUrl(),appId = mContext.getAppId(),appSecret = mContext.getAppSecret(),stores_id = store_info.getString("stores_id"),sz_moeny =  mChargeAmtEt.getText().toString(),
                        member_id = mVip.getString("member_id"),third_order_id = generate_pay_son_order_id();

                member_order_info.put("stores_id",stores_id);
                member_order_info.put("member_id",member_id);
                member_order_info.put("status",1);
                member_order_info.put("addtime",System.currentTimeMillis() / 1000);

                member_order_info.put("card_code",mVip.getString("card_code"));
                member_order_info.put("mobile",mVip.getString("mobile"));
                member_order_info.put("name",mVip.getString("name"));

                member_order_info.put("third_order_id",third_order_id);
                member_order_info.put("cashier_id",cashier_info.getString("cas_id"));
                member_order_info.put("order_money",sz_moeny);

                //保存单据
                if (!SQLiteHelper.saveFormJson(member_order_info,"member_order_info",null,0,err)){
                    mChargeAmtEt.post(()->{
                        MyDialog.displayErrorMessage(null,err.toString(),mContext);
                    });
                    return;
                }

                try {

                    data_.put("appid",appId);
                    data_.put("stores_id",stores_id);
                    data_.put("member_id",member_id);
                    data_.put("cashier_id",cashier_info.getString("cas_id"));
                    data_.put("order_money",sz_moeny);

                    String sz_param = HttpRequest.generate_request_parm(data_,appSecret);

                    Logger.i("生成充值订单参数:url:%s%s,param:%s",url ,"/api/member/mk_money_order" ,sz_param);
                    retJson = httpRequest.sendPost(url + "/api/member/mk_money_order",sz_param,true);
                    Logger.i("生成充值订单返回:%s",retJson.toString());

                    switch (retJson.getIntValue("flag")) {
                        case 0:
                            showPayError(retJson.getString("info"));
                            break;
                        case 1:
                            info_json = JSON.parseObject(retJson.getString("info"));
                            switch (info_json.getString("status")){
                                case "n":
                                    showPayError(info_json.getString("info"));
                                    break;
                                case "y":
                                    Logger.d_json(info_json.toString());

                                    int is_check = mPayMethodSelected.getIntValue("is_check");

                                    final String order_code = info_json.getString("order_code"),pay_method_id = mPayMethodSelected.getString("pay_method_id"),
                                            whereClause = "member_id = ? and third_order_id = ?";

                                    final String[] whereArgs = new String[]{member_id,third_order_id};
                                    final ContentValues values = new ContentValues();

                                    //保存支付单号
                                    values.put("order_code",order_code);
                                    if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                        showPayError(err.toString());
                                        return;
                                    }

                                    //发起支付请求
                                    if (is_check != 2){
                                        String unified_pay_order = mPayMethodSelected.getString("unified_pay_order"),
                                                unified_pay_query = mPayMethodSelected.getString("unified_pay_query");

                                        if ("null".equals(unified_pay_order) || "".equals(unified_pay_order)){
                                            unified_pay_order = "/api/pay2/index";
                                        }
                                        if ("null".equals(unified_pay_query) || "".equals(unified_pay_query)){
                                            unified_pay_query = "/api/pay2_query/query";
                                        }

                                        mProgressDialog.setMessage("正在发起支付请求...").refreshMessage();
                                        data_ = new JSONObject();
                                        data_.put("appid",appId);
                                        data_.put("stores_id",stores_id);
                                        data_.put("order_code",order_code);
                                        data_.put("pos_num",cashier_info.getString("pos_num"));
                                        data_.put("is_wuren",2);
                                        data_.put("order_code_son",third_order_id);
                                        data_.put("pay_money",sz_moeny);
                                        data_.put("pay_method",pay_method_id);
                                        data_.put("pay_code_str",mPayCode);

                                        sz_param = HttpRequest.generate_request_parm(data_,appSecret);

                                        Logger.i("会员充值请求支付参数:url:%s%s,param:%s",url ,unified_pay_order,sz_param);
                                        retJson = httpRequest.sendPost(url + unified_pay_order,sz_param,true);
                                        Logger.i("会员充值支付请求返回:%s",retJson.toString());

                                        switch (retJson.getIntValue("flag")){
                                            case 0:
                                                showPayError(retJson.getString("info"));
                                                return;
                                            case 1:
                                                info_json = JSON.parseObject(retJson.getString("info"));
                                                switch (info_json.getString("status")){
                                                    case "n":
                                                        showPayError(info_json.getString("info"));
                                                        return;
                                                    case "y":
                                                        int res_code = info_json.getIntValue("res_code");
                                                        switch (res_code){
                                                            case 1://支付成功
                                                                break;
                                                            case 2:
                                                                showPayError(info_json.getString("info"));
                                                                return;
                                                            case 3:
                                                            case 4:
                                                                while (res_code == 3 ||  res_code == 4){
                                                                    mProgressDialog.setMessage("正在查询支付结果...").refreshMessage();
                                                                    data_ = new JSONObject();

                                                                    data_.put("appid",appId);
                                                                    data_.put("pay_code",info_json.getString("pay_code"));
                                                                    data_.put("order_code_son",info_json.getString("order_code_son"));

                                                                    if (res_code == 4){
                                                                        data_.put("pay_password","");
                                                                    }
                                                                    sz_param = HttpRequest.generate_request_parm(data_,appSecret);

                                                                    Logger.i("会员充值支付查询参数:url:%s%s,param:%s",url,unified_pay_order,sz_param);
                                                                    retJson = httpRequest.sendPost(url + unified_pay_query,sz_param,true);
                                                                    Logger.i("会员充值支付查询返回:%s",retJson.toString());

                                                                    switch (retJson.getIntValue("flag")){
                                                                        case 0:
                                                                            showPayError(retJson.getString("info"));
                                                                            return;
                                                                        case 1:
                                                                            info_json = JSON.parseObject(retJson.getString("info"));
                                                                            Logger.json(info_json.toString());
                                                                            switch (info_json.getString("status")){
                                                                                case "n":
                                                                                    showPayError(info_json.getString("info"));
                                                                                    return;
                                                                                case "y":
                                                                                    res_code = info_json.getIntValue("res_code");
                                                                                    if (res_code == 2){//支付失败
                                                                                        showPayError(info_json.getString("info"));
                                                                                        return;
                                                                                    }
                                                                                    break;
                                                                            }
                                                                            break;
                                                                    }
                                                                }
                                                                break;
                                                        }
                                                        break;
                                                }
                                                break;
                                        }

                                    }

                                    //处理充值订单
                                    mProgressDialog.setMessage("正在处理充值订单...").refreshMessage();

                                    //保存支付方式,更新状态为已支付完成
                                    values.clear();
                                    values.put("pay_method_id",pay_method_id);
                                    values.put("status",2);
                                    if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                        showPayError(err.toString());
                                        return;
                                    }

                                    data_ = new JSONObject();
                                    data_.put("appid",appId);
                                    data_.put("order_code",order_code);
                                    if (is_check == 2)
                                        data_.put("case_pay_money",sz_moeny);
                                    data_.put("pay_method",pay_method_id);

                                    Logger.d_json(data_.toJSONString());

                                    sz_param = HttpRequest.generate_request_parm(data_,appSecret);
                                    retJson = httpRequest.sendPost(url + "/api/member/cl_money_order",sz_param,true);

                                    switch (retJson.getIntValue("flag")) {
                                        case 0:
                                            showPayError(retJson.getString("info"));
                                            break;
                                        case 1:
                                            info_json = JSON.parseObject(retJson.getString("info"));
                                            switch (info_json.getString("status")){
                                                case "n":
                                                    showPayError(info_json.getString("info"));
                                                    break;
                                                case "y":
                                                    Logger.d_json(info_json.toJSONString());
                                                    final JSONArray members = JSON.parseArray(info_json.getString("member")),money_orders = JSON.parseArray(info_json.getString("money_order"));
                                                    final JSONObject member = members.getJSONObject(0),pay_info = money_orders.getJSONObject(0);

                                                    if (pay_info != null && member != null){

                                                        values.clear();
                                                        values.put("status",3);//已完成
                                                        values.put("xnote",info_json.toJSONString());
                                                        values.put("give_money",pay_info.getDoubleValue("give_money"));
                                                        if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                                            showPayError(err.toString());
                                                        }else {
                                                            chargeSuccess(member);
                                                            if (mContext.getPrintStatus()){
                                                                Printer.print(mContext,VipChargeDialogImp.get_print_content(mContext,order_code));
                                                            }
                                                        }
                                                    }else {
                                                        Logger.e("服务器返回member：%s,money_order：%s",members,money_orders);
                                                        showPayError("服务器返回信息为空！");
                                                    }
                                                    break;
                                            }
                                            break;
                                    }
                                    break;
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage("参数解析错误：" + e.getMessage(),mContext,getWindow());
                }
            });
        }
    }

    private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        private int rootViewVisibleHeight;
        private boolean isShow = true;
        @Override
        public void onGlobalLayout() {
            //获取当前根视图在屏幕上显示的大小
            final Rect r = new Rect();
            //获取rootView在窗体的可视区域
            mRoot.getWindowVisibleDisplayFrame(r);
            int visibleHeight = r.height();
            if (rootViewVisibleHeight == 0) {
                rootViewVisibleHeight = visibleHeight;
                return;
            }

            //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
            if (rootViewVisibleHeight == visibleHeight) {
                return;
            }

            //根视图显示高度变小超过200，可以看作软键盘显示了
            if (rootViewVisibleHeight - visibleHeight > 200) {
                if (mSearchContent != getCurrentFocus()){
                    mRoot.scrollBy(0,rootViewVisibleHeight - visibleHeight);
                    isShow = true;
                }
                rootViewVisibleHeight = visibleHeight;
                return;
            }

            //根视图显示高度变大超过200，可以看作软键盘隐藏了
            if (visibleHeight - rootViewVisibleHeight > 200) {
                if (isShow){
                    mRoot.scrollBy(0,-(visibleHeight - rootViewVisibleHeight));
                    isShow = false;
                }
                rootViewVisibleHeight = visibleHeight;
            }
        }
    };

}
