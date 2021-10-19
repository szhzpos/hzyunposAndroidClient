package com.wyc.cloudapp.dialog.vip;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
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
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractVipChargeDialog extends AbstractDialogMainActivity {
    protected JSONObject mPayMethodSelected;
    protected static final String PAY_CODE_LABEL = "payCode";

    private VipInfo mVip;
    private EditText mSearchContent,mChargeAmtEt,mRemarkEt,mPresentAmtEt;
    private TextView mVip_name,mVip_sex,mVip_p_num,mVip_card_id,mVip_balance,mVip_integral,mVipGrade,mVipDiscount,mChargePlanTv,mSaleManTv;
    private CustomProgressDialog mProgressDialog;
    private JSONArray mChargePlans,mPayMethods;
    private Button mChargeBtn;
    public AbstractVipChargeDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.vip_charge_sz));
    }
    public AbstractVipChargeDialog(@NonNull MainActivity context, final VipInfo object){
        this(context);
        mVip = object;
        CustomApplication.execute(()-> loadChargePlan(object.getOpenid()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(mContext);

        initVipInfoFields();
        initSearchContent();
        initChargeAmt();
        initPresentAmt();
        initRemark();
        initChargePlan();
        initPayMethod();
        initPrintSwitch();
        initChargeBtn();
        initSaleMan();

        showVipInfo();
    }

    private void initSaleMan(){
        final TextView mobile_sale_man = findViewById(R.id.mobile_sale_man);
        mobile_sale_man.setOnClickListener(v -> {
            final JSONObject object = showSaleInfo(mContext);
            if (object.isEmpty()){
                mobile_sale_man.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                mobile_sale_man.setText(object.getString(TreeListBaseAdapter.COL_NAME));
            }
        });
        mSaleManTv = mobile_sale_man;
    }

    private static JSONArray parse_sale_man(final MainActivity activity){
        final StringBuilder err = new StringBuilder();
        final JSONArray array = new JSONArray(),
                sales = SQLiteHelper.getListToJson("SELECT sc_id,sc_name FROM sales_info where stores_id = '" + activity.getStoreId() +"' and sc_status = 1",err);
        JSONObject object = new JSONObject(),tmp;

        object.put("level",0);
        object.put("unfold",false);
        object.put("isSel",false);
        object.put(TreeListBaseAdapter.COL_ID,"-1");
        object.put(TreeListBaseAdapter.COL_NAME,"无营业员");

        array.add(object);
        if (sales != null){
            for (int i = 0,size = sales.size();i < size;i++){
                tmp = sales.getJSONObject(i);
                final String id = tmp.getString("sc_id");
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,tmp.getString("sc_name"));

                array.add(object);
            }
        }else {
            MyDialog.ToastMessage("查询营业员错误:" + err, null);
        }
        return array;
    }

    public static JSONObject showSaleInfo(final MainActivity activity){
        final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(activity,activity.getString(R.string.sale_man_sz));
        treeListDialog.setData(parse_sale_man(activity),null,true);
        if (treeListDialog.exec() == 1){
            return treeListDialog.getSingleContent();
        }

        return new JSONObject();
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

    public VipInfo getVip(){
        return mVip;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText _search_content = findViewById(R.id.m_search_content);
        _search_content.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus){
                final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.HIDE_NOT_ALWAYS);
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
            mProgressDialog.setMessage("正在查询会员...").refreshMessage().show();
            CustomApplication.execute(()->{
                try {
                    final JSONObject object = VipInfoDialog.searchVip(mobile).getJSONObject(0);
                    VipInfo vip = object == null ? null : object.toJavaObject(VipInfo.class);
                    if (vip != null){
                        loadChargePlan(vip.getOpenid());
                        CustomApplication.runInMainThread(()-> showVipInfo(vip));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    CustomApplication.runInMainThread(()-> {
                        restInfo();
                        MyDialog.ToastMessage(e.getMessage(), getWindow());
                    });
                }
                CustomApplication.runInMainThread(()->mProgressDialog.dismiss());
            });
        }else{
            MyDialog.ToastMessage(mSearchContent,mSearchContent.getHint().toString(), getWindow());
        }
    }

    private void loadChargePlan(final String openid){
        JSONObject object = new JSONObject(),ret_json;
        final HttpRequest httpRequest = new HttpRequest();
        object.put("appid",mContext.getAppId());
        object.put("openid",openid);
        object.put("origin",1);
        ret_json = httpRequest.sendPost(mContext.getUrl() + "/api/member_recharge/member_recharge", HttpRequest.generate_request_parm(object,mContext.getAppSecret()),true);
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
                        mChargePlans = parse_charge_plan_and_set_default(data.getJSONArray("money_list"));
                        break;
                }
                break;
        }
    }

    private void showVipInfo(VipInfo object){
        mVip = object;
        showVipInfo();
    }
    private void showVipInfo(){
        final VipInfo object = mVip;
        if (null != object){
            final String grade_name = object.getGradeName(),mobile = object.getMobile();
            if (grade_name != null)mVipGrade.setText(grade_name);

            mVip_name.setText(object.getName());
            mVip_sex.setText(object.getSex());

            mVip_p_num.setText(mobile);
            if (mSearchContent.getText().length() == 0){
                mSearchContent.setText(mobile);
            }

            mVipDiscount.setText(String.valueOf(object.getDiscount()));
            mVip_card_id.setText(object.getCard_code());
            mVip_balance.setText(String.format(Locale.CHINA,"%.2f",object.getMoney_sum()));
            mVip_integral.setText(String.format(Locale.CHINA,"%.2f",object.getPoints_sum()));
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

            restChargeInfo();

            mVip = null;
        }
    }

    protected void restChargeInfo(){
        final CharSequence space = mContext.getText(R.string.space_sz);

        mChargePlanTv.setText("自由充值");
        mChargePlanTv.setTag(-1);
        mChargeAmtEt.setText(R.string.zero_p_z_sz);
        mChargeAmtEt.setEnabled(true);

        mPresentAmtEt.setEnabled(true);
        mPresentAmtEt.setText(R.string.zero_p_z_sz);

        mSaleManTv.setText(space);
        mSaleManTv.setTag("-1");

        mRemarkEt.setText(space);

        clearPayCode(true);
    }

    private void initChargeAmt(){
        mChargeAmtEt = findViewById(R.id.mobile_charge_amt);
    }

    private void initRemark(){
        mRemarkEt = findViewById(R.id.mobile_charge_remark);
    }
    private void initPresentAmt(){
        mPresentAmtEt = findViewById(R.id.mobile_present_amt);
    }

    private void initChargePlan(){
        final TextView mobile_charge_plan = findViewById(R.id.mobile_charge_plan);
        mobile_charge_plan.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                v.callOnClick();
            }
        });
        mobile_charge_plan.setOnClickListener(v -> {
            final String charge_plan_sz = mContext.getString(R.string.charge_plan_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,charge_plan_sz.substring(0,charge_plan_sz.length() - 1));
            treeListDialog.setData(mChargePlans,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    showChargePlan(treeListDialog.getSingleContent(),mobile_charge_plan);
                }
            });
        });
        mChargePlanTv = mobile_charge_plan;
    }

    private void showChargePlan(@NonNull final JSONObject object,final TextView mobile_charge_plan){
        int item_id = object.getIntValue(TreeListBaseAdapter.COL_ID);
        final String item_name = object.getString(TreeListBaseAdapter.COL_NAME);

        mobile_charge_plan.setText(item_name);
        mobile_charge_plan.setTag(item_id);

        if (item_id != -1){
            mChargeAmtEt.setText(object.getString("money"));
            mChargeAmtEt.setEnabled(false);
            mPresentAmtEt.setEnabled(false);
            mPresentAmtEt.setText(object.getString("give_money"));
        }else {
            mobile_charge_plan.setText(item_name);

            final String zero_p_z_sz = mContext.getString(R.string.zero_p_z_sz);
            mPresentAmtEt.setEnabled(true);
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
        object.put(TreeListBaseAdapter.COL_ID,-1);
        object.put(TreeListBaseAdapter.COL_NAME,default_item_name);
        array.add(object);

        CustomApplication.runInMainThread(()->{
            mChargePlanTv.setText(default_item_name);
            mChargePlanTv.setTag(-1);;
        });

        if (plans != null){
            for (int i = 0,size = plans.size();i < size;i++){
                final JSONObject tmp = plans.getJSONObject(i);
                final int id = Utils.getNotKeyAsNumberDefault(tmp,"_id",-1);
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,tmp.getString("advstr"));
                object.put("money",tmp.getString("money"));
                object.put("give_money",tmp.getString("give_money"));

                array.add(object);
            }
        }
        return array;
    }


    private void initPayMethod(){
        final TextView mobile_pay_method = findViewById(R.id.mobile_pay_method);
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
                final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,pay_method_name_colon_sz.substring(0,pay_method_name_colon_sz.length() - 1));
                treeListDialog.setData(parse_pay_method(mPayMethods),null,true);
                mobile_pay_method.post(()->{
                    if (treeListDialog.exec() == 1){
                        set_pay_method_and_check_scan(treeListDialog.getSingleContent(),mobile_pay_method);
                    }
                });
            });

            //默认支付方式-现金
            final JSONObject default_cash = PayMethodViewAdapter.getPayMethod(PayMethodViewAdapter.getCashMethodId());
            mobile_pay_method.setTag(Utils.getNullStringAsEmpty(default_cash,"pay_method_id"));
            mobile_pay_method.setText(Utils.getNullStringAsEmpty(default_cash,"name"));
            mPayMethodSelected = default_cash;
        }else{
            MyDialog.ToastMessage(err.toString(), getWindow());
        }
    }

    public abstract boolean checkPayMethod();
    public abstract void clearPayCode(boolean clearView);

    private void set_pay_method_and_check_scan(@NonNull final JSONObject object,final TextView mobile_pay_method){
        final String _id = Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID);
        mPayMethodSelected = get_pay_method(_id);
        if (mPayMethodSelected != null){
            mobile_pay_method.setTag(_id);
            mobile_pay_method.setText(Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_NAME));

            checkPayMethod();
        }
    }

    private void initChargeBtn(){
        final Button mobile_charge_btn = findViewById(R.id.mobile_charge_btn);
        mobile_charge_btn.setOnClickListener(v -> {
            if (checkPayMethod()){
                vip_charge();
            }
        });

        mChargeBtn = mobile_charge_btn;
    }
    protected void triggerCharge(){
        if (mChargeBtn != null)mChargeBtn.callOnClick();
    }

    private JSONArray parse_pay_method(final JSONArray methods){
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
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,method.getString("name"));

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

    private static String generate_pay_son_order_id(){
        return "MPAY" + new SimpleDateFormat("yyyyMMdd",Locale.CHINA).format(new Date()) + Utils.getNonce_str(8);
    }

    private boolean verify(){
        if (mVip == null){
            MyDialog.ToastMessage(mVip_card_id,mContext.getString(R.string.not_empty_hint_sz,mContext.getString(R.string.vip_dialog_title_sz)), getWindow());
            return false;
        }
        if (checkChargeAmtEqualZero()){
            mChargeAmtEt.requestFocus();
            MyDialog.ToastMessage(mChargeAmtEt,mContext.getString(R.string.not_zero_hint_sz,mContext.getString(R.string.deposit_amt_sz)), getWindow());
            return false;
        }else if (checkMinChargeAmt()){
            mChargeAmtEt.requestFocus();
            MyDialog.ToastMessage(mChargeAmtEt,String.format(Locale.CHINA,"本会员级别最小充值金额:%.2f",mVip.getMin_recharge_money()), getWindow());
            return false;
        }

        if (mPayMethodSelected == null){
            MyDialog.ToastMessage(mContext.getString(R.string.pay_m_hint_sz), getWindow());
            return false;
        }

        return true;
    }

    private boolean checkChargeAmtEqualZero(){
        double amt = 0.0;
        if (mChargeAmtEt != null){
            try {
                amt =Double.parseDouble(mChargeAmtEt.getText().toString());
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return Utils.equalDouble(amt,0.0);
    }

    private boolean checkMinChargeAmt(){
        double amt = 0.0,min_recharge_money = 0.0;
        if (mChargeAmtEt != null){
            try {
                amt =Double.parseDouble(mChargeAmtEt.getText().toString());
                min_recharge_money = mVip.getMin_recharge_money();
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return amt < min_recharge_money;
    }


    private void showPayError(final String message){
        CustomApplication.runInMainThread(()-> {
            mProgressDialog.dismiss();
            clearPayCode(false);
            MyDialog.displayErrorMessage(mContext, message);
        });
    }

    private void chargeSuccess(final VipInfo member){
        CustomApplication.runInMainThread(()->{
            mProgressDialog.dismiss();
            MyDialog.ToastMessage("充值成功！", getWindow());
            restChargeInfo();
            showVipInfo(member);
        });
    }

    private double getPresentAmt(){
        double amt = 0.0;
        try {
            amt = Double.parseDouble(mPresentAmtEt.getText().toString());
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return amt;
    }

    private void vip_charge(){
        if (verify()){
            mProgressDialog.setCancel(false).setMessage("正在生成充值订单...").refreshMessage().show();

            final String sale_man_id = Utils.getViewTagValue(mSaleManTv,"-1"),charge_moeny =  mChargeAmtEt.getText().toString();
            final double present_amt = getPresentAmt();

            CustomApplication.execute(()->{
                final HttpRequest httpRequest = new HttpRequest();
                final JSONObject member_order_info = new JSONObject();
                final StringBuilder err = new StringBuilder();

                JSONObject data_ = new JSONObject(),retJson,info_json;
                final String url = mContext.getUrl(),appId = mContext.getAppId(),appSecret = mContext.getAppSecret(),stores_id = mContext.getStoreId(),
                        member_id = String.valueOf(mVip.getMember_id()),third_order_id = generate_pay_son_order_id(),pay_method_id = mPayMethodSelected.getString("pay_method_id");

                member_order_info.put("stores_id",stores_id);
                member_order_info.put("member_id",member_id);
                member_order_info.put("status",1);
                member_order_info.put("order_type",1);
                member_order_info.put("addtime",System.currentTimeMillis() / 1000);

                member_order_info.put("card_code",mVip.getCard_code());
                member_order_info.put("mobile",mVip.getMobile());
                member_order_info.put("name",mVip.getName());
                member_order_info.put("pay_method_id",pay_method_id);
                member_order_info.put("third_order_id",third_order_id);
                member_order_info.put("cashier_id",mContext.getCashierId());

                if (!"-1".equals(sale_man_id))
                    member_order_info.put("sc_id",sale_man_id);

                member_order_info.put("order_money",charge_moeny);

                //保存单据
                if (!SQLiteHelper.saveFormJson(member_order_info,"member_order_info",null,0,err)){
                    CustomApplication.runInMainThread(()-> MyDialog.displayErrorMessage(mContext, err.toString()));
                    return;
                }

                try {

                    data_.put("appid",appId);
                    data_.put("stores_id",stores_id);
                    data_.put("member_id",member_id);
                    data_.put("cashier_id",mContext.getCashierId());
                    data_.put("hand_give_money",present_amt);
                    data_.put("order_money",charge_moeny);

                    if (!"-1".equals(sale_man_id))
                            data_.put("sc_id",sale_man_id);

                    data_.put("xnote","手机充值;" + mRemarkEt.getText().toString());

                    String sz_param = HttpRequest.generate_request_parm(data_,appSecret);

                    final String api = "/api/member/mk_money_order";
                    Logger.i("生成充值订单参数:url:%s%s,param:%s",url , api,sz_param);
                    retJson = httpRequest.sendPost(url + api,sz_param,true);
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

                                    final String order_code = info_json.getString("order_code"),whereClause = "member_id = ? and third_order_id = ?";

                                    final String[] whereArgs = new String[]{member_id,third_order_id};
                                    final ContentValues values = new ContentValues();

                                    //保存支付单号
                                    values.put("order_code",order_code);
                                    if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                        showPayError(err.toString());
                                        return;
                                    }

                                    //发起支付请求
                                    if (PayMethodViewAdapter.isApiCheck(is_check)){
                                        String unified_pay_order = mPayMethodSelected.getString("unified_pay_order"),
                                                unified_pay_query = mPayMethodSelected.getString("unified_pay_query"),
                                                pay_code = mPayMethodSelected.getString(PAY_CODE_LABEL);

                                        if ("null".equals(unified_pay_order) || "".equals(unified_pay_order)){
                                            unified_pay_order = InterfaceURL.UNIFIED_PAY;
                                        }
                                        if ("null".equals(unified_pay_query) || "".equals(unified_pay_query)){
                                            unified_pay_query = InterfaceURL.UNIFIED_PAY_QUERY;
                                        }

                                        mProgressDialog.setMessage("正在发起支付请求...").refreshMessage();
                                        data_ = new JSONObject();
                                        data_.put("appid",appId);
                                        data_.put("stores_id",stores_id);
                                        data_.put("order_code",order_code);
                                        data_.put("pos_num",mContext.getPosNum());
                                        data_.put("is_wuren",2);
                                        data_.put("order_code_son",third_order_id);
                                        data_.put("pay_money",charge_moeny);
                                        data_.put("pay_method",pay_method_id);
                                        data_.put("pay_code_str",pay_code);

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

                                                                    Logger.i("会员充值支付查询参数:url:%s%s,param:%s",url,unified_pay_query,sz_param);
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
                                    values.put("status",2);
                                    if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                        showPayError(err.toString());
                                        return;
                                    }

                                    data_ = new JSONObject();
                                    data_.put("appid",appId);
                                    data_.put("order_code",order_code);
                                    if (is_check == 2)
                                        data_.put("case_pay_money",charge_moeny);
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
                                                            chargeSuccess(member.toJavaObject(VipInfo.class));
                                                            if (mContext.getPrintStatus()){
                                                                Printer.print(get_print_content(mContext,order_code));
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
                    MyDialog.ToastMessage("参数解析错误：" + e.getMessage(), getWindow());
                }
            });
        }
    }

    private static String c_format_58(final MainActivity context,final JSONObject format_info,final JSONObject order_info){
        final StringBuilder info = new StringBuilder(),out = new StringBuilder();

        final String store_name = Utils.getNullStringAsEmpty(format_info,"s_n");
        final String new_line =  "\n";
        final String footer_c = Utils.getNullStringAsEmpty(format_info,"f_c");

        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1);
        int footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);

        final JSONArray welfare = Utils.getNullObjectAsEmptyJsonArray(order_info,"welfare"),money_orders = Utils.getNullObjectAsEmptyJsonArray(order_info,"money_order"),
                members = Utils.getNullObjectAsEmptyJsonArray(order_info,"member");

        if (money_orders.isEmpty() || members.isEmpty())return "";//打印内容为空直接返回空

        final JSONObject stores_info = CustomApplication.self().getStoreInfo(),money_order = money_orders.getJSONObject(0),member = members.getJSONObject(0);

        while (print_count-- > 0) {//打印份数
            if (info.length() > 0){
                info.append(new_line).append(new_line);
                out.append(info);
                continue;
            }
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name.length() == 0 ? stores_info.getString("stores_name") : store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(context.getString(R.string.store_name_sz).concat(Utils.getNullStringAsEmpty(stores_info,"stores_name"))).append(new_line);
            info.append(context.getString(R.string.order_sz).concat(Utils.getNullStringAsEmpty(money_order,"order_code"))).append(new_line);

            final String origin_order_code = Utils.getNullStringAsEmpty(money_order,"source_order_code");
            if (!"".equals(origin_order_code))
                info.append(context.getString(R.string.origin_order_code_sz).concat("：").concat(origin_order_code)).append(new_line);

            if (!SQLiteHelper.getLocalParameter("MEMBER_PARAMETER",stores_info))Logger.d("查询会员参数错误:%s",stores_info.getString("info"));
            String vip_name = Utils.getNullStringAsEmpty(member,"name"),card_code = Utils.getNullStringAsEmpty(member,"card_code"),mobile = Utils.getNullStringAsEmpty(member,"mobile");
            if (Utils.getNotKeyAsNumberDefault(stores_info,"member_secret_protect",0) == 1){
                if (vip_name.length() > 2)
                    vip_name = vip_name.replace(vip_name.substring(1),Printer.REPLACEMENT);
                else {
                    vip_name = vip_name.concat(Printer.REPLACEMENT);
                }
                int len = card_code.length();
                if (len <= 3){
                    card_code = card_code.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    card_code = card_code.replace(card_code.substring(3,len - 1),Printer.REPLACEMENT);
                }else {
                    card_code = card_code.replace(card_code.substring(3,7),Printer.REPLACEMENT);
                }
                int mobile_len = mobile.length();
                if (mobile_len <= 3){
                    mobile = mobile.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    mobile = mobile.replace(mobile.substring(3,len - 1),Printer.REPLACEMENT);
                }else {
                    mobile = mobile.replace(mobile.substring(3,7),Printer.REPLACEMENT);
                }
            }

            info.append(context.getString(R.string.oper_sz).concat("：").concat(Utils.getNullStringAsEmpty(CustomApplication.self().getCashierInfo(),"cas_name"))).append(new_line);
            info.append(context.getString(R.string.vip_card_id_sz).concat(card_code)).append(new_line);
            info.append("会员姓名：".concat(vip_name)).append(new_line);
            info.append("支付方式：".concat(Utils.getNullStringAsEmpty(money_order,"pay_method_name"))).append(new_line);
            info.append(context.getString(R.string.charge_amt_colon_sz).concat(Utils.getNullStringAsEmpty(money_order,"order_money"))).append(new_line);
            info.append(context.getString(R.string.give_amt).concat("：").concat(Utils.getNullOrEmptyStringAsDefault(money_order,"give_money","0.00"))).append(new_line);
            info.append("会员余额：".concat(Utils.getNullStringAsEmpty(member,"money_sum"))).append(new_line);
            info.append("会员积分：".concat(Utils.getNullStringAsEmpty(member,"points_sum"))).append(new_line);
            info.append("会员电话：".concat(mobile)).append(new_line);

            info.append("时    间：".concat(new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA).format(money_order.getLongValue("addtime") * 1000))).append(new_line);
            if (welfare.size() != 0){
                for (int i = 0,size = welfare.size();i < size;i++){
                    if (i == 0)info.append("优惠信息").append(new_line);
                    info.append("  ").append(welfare.getString(i)).append(new_line);
                }
            }
            if (footer_c.isEmpty()){
                info.append(new_line).append(context.getString(R.string.hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(stores_info,"telphone","")).append(new_line);
                info.append(context.getString(R.string.stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(stores_info,"region","")).append(new_line);
            }else {
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c).append(Printer.commandToStr(Printer.ALIGN_LEFT));
            }

            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);
        }
        out.append(info);

        return out.toString();
    }

    static String get_print_content(final MainActivity context,final String order_code){
        final JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("v_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.vip_c_format){
                final JSONObject xnote = new JSONObject();
                if (SQLiteHelper.execSql(xnote,"SELECT xnote FROM member_order_info where order_code = '" + order_code + "'")){
                    try {
                        final JSONObject order_info = JSON.parseObject(xnote.getString("xnote"));
                        switch (print_format_info.getIntValue("f_z")){
                            case R.id.f_58:
                                content = c_format_58(context,print_format_info,order_info);
                                break;
                            case R.id.f_76:
                                break;
                            case R.id.f_80:
                                break;
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_c_err_hint_sz,e.getLocalizedMessage()), context.getWindow()));
                    }
                }else
                    context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_c_err_hint_sz,xnote.getString("info")), context.getWindow()));
            }else {
                context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.f_not_sz), context.getWindow()));
            }
        }else
            context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")), context.getWindow()));

        return content;
    }

    public static void vipRefundAmt(final MainActivity context,final String order_id){
        final StringBuilder order_code = new StringBuilder(order_id);
        final CustomProgressDialog dialog = new CustomProgressDialog(context);
        final JEventLoop loop = new JEventLoop();
        dialog.setMessage("正在退款...").show();
        CustomApplication.execute(()->{
            boolean code = false;
            try {
                code = vipAllRefund(context,order_code);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (code){
                loop.done(1);
            }else
                loop.done(0);
        });
        if (loop.exec() == 1){
            MyDialog.ToastMessage(order_code.toString(), null);
        }else {
            MyDialog.ToastMessage(order_code.toString(), null);
        }
        dialog.dismiss();
    }
    private static boolean vipAllRefund(final MainActivity context,final StringBuilder sb){
        final JSONObject object = new JSONObject();
        final String origin_order_code = sb.toString();
        if (SQLiteHelper.execSql(object,"select a.status,a.order_type,cashier_id,b.is_check,a.pay_method_id,a.stores_id, a.name, a.mobile, a.card_code, a.order_money, a.give_money,member_id, a.sc_id from " +
                "member_order_info a inner join pay_method b on a.pay_method_id = b.pay_method_id where order_code ='" + origin_order_code +"'")){

            if (!object.isEmpty()){
                int status = Utils.getNotKeyAsNumberDefault(object,"status",-1),order_type = Utils.getNotKeyAsNumberDefault(object,"order_type",-1);
                if ((order_type == 1 && status == 6) || (order_type == 2 && status == 3)){
                    sb.append("订单已处理!");
                }else {
                    double order_money = -object.getDoubleValue("order_money");
                    if (status == 3){
                        final JSONObject member_order_info = Utils.JsondeepCopy(object);

                        //删除不需要的内容
                        member_order_info.remove("is_check");

                        //更新内容
                        member_order_info.put("status",5);
                        member_order_info.put("order_type",2);
                        member_order_info.put("origin_order_code",origin_order_code);
                        member_order_info.put("addtime",System.currentTimeMillis() / 1000);
                        member_order_info.put("third_order_id",generate_pay_son_order_id());
                        member_order_info.put("order_money",order_money);

                        //保存单据
                        if (!SQLiteHelper.saveFormJson(member_order_info,"member_order_info",null,0,sb)){
                            return false;
                        }
                    }
                    object.put("origin_order_code",origin_order_code);
                    object.put("order_money",order_money);
                    if (createRefundOrder(context,object)){
                        int is_check = Utils.getNotKeyAsNumberDefault(object,"is_check",-1);
                        if (PayMethodViewAdapter.isApiCheck(is_check)){
                            if (!refundWithCheck(context,object)){
                                return false;
                            }
                        }
                        if (disposeRefundOrder(context,object)){
                            sb.append(object.getString("info"));
                            return true;
                        }else {
                            sb.append(object.getString("info"));
                        }
                    }else {
                        sb.append(object.getString("info"));
                    }
                }
            }else {
                sb.append("不存在订单：").append(origin_order_code);
            }
        }else {
            sb.append(object.getString("info"));
        }
        return false;
    }

    private static boolean createRefundOrder(final MainActivity context,@NonNull final JSONObject object){
        /*
        * object 必须包含stores_id 仓库编号 member_id会员id origin_order_code充值单号 order_money充值金额(负数),成功之后用过object的refund_order_code返回退款单号
        * */
        final HttpRequest httpRequest = new HttpRequest();
        JSONObject data_ = new JSONObject(),retJson,info_json;
        final String url = context.getUrl(),appId = context.getAppId(),appSecret = context.getAppSecret(),stores_id = object.getString("stores_id"),
                member_id = object.getString("member_id"),origin_order_code = object.getString("origin_order_code");

        data_.put("appid",appId);
        data_.put("stores_id",stores_id);
        data_.put("member_id",member_id);
        data_.put("order_code",origin_order_code);
        data_.put("order_money",Utils.getNotKeyAsNumberDefault(object,"order_money",0.0));

        data_.put("xnote","会员手机充值退款;");

        String sz_param = HttpRequest.generate_request_parm(data_,appSecret),api = "/api/member/mk_refund_money_order",prefix = "生成会员充值退款";

        Logger.i("%s订单参数:url:%s%s,param:%s",prefix,url ,api,sz_param);
        retJson = httpRequest.sendPost(url + api,sz_param,true);
        Logger.i("%s订单返回:%s",prefix,retJson.toString());


        Logger.d_json(retJson.toJSONString());

        switch (retJson.getIntValue("flag")) {
            case 0:
                object.put("info",retJson.getString("info"));
                break;
            case 1:
                info_json = JSON.parseObject(retJson.getString("info"));
                switch (info_json.getString("status")) {
                    case "n":
                        object.put("info",info_json.getString("info"));
                        break;
                    case "y":
                        Logger.d_json(info_json.toJSONString());

                        final String whereClause = "member_id = ? and origin_order_code = ? and order_type=2",refund_order_code = info_json.getString("order_code");

                        final String[] whereArgs = new String[]{member_id,origin_order_code};
                        final ContentValues values = new ContentValues();

                        //保存退款单号
                        final StringBuilder err = new StringBuilder();
                        values.put("order_code",refund_order_code);
                        if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) >= 0){
                            object.put("refund_order_code",refund_order_code);
                            object.put("origin_order_code",origin_order_code);
                            return true;
                        }
                        Logger.d(err);
                        break;
                }
        }
        return false;
    }

    private static boolean disposeRefundOrder(final MainActivity context,final JSONObject object){
        /*
        * object 必须包含member_id，refund_order_code,origin_order_code
        * */
        final String url = context.getUrl(),appId = context.getAppId(),appSecret = context.getAppSecret();
        final HttpRequest httpRequest = new HttpRequest();
        final JSONObject data_ = new JSONObject();


        final String api = "/api/member/cl_refund_money_order",prefix = "处理会员充值退款",member_id = object.getString("member_id"),
                refund_order_code = object.getString("refund_order_code"),origin_order_code = object.getString("origin_order_code");

        //处理退款订单
        data_.put("appid",appId);
        data_.put("order_code",refund_order_code);


        final String sz_param = HttpRequest.generate_request_parm(data_,appSecret);
        Logger.i("%s订单参数:url:%s%s,param:%s",prefix,url ,api,sz_param);
        final JSONObject retJson = httpRequest.sendPost(url + api,sz_param,true);
        Logger.i("%s订单返回:%s",prefix,retJson.toString());

        switch (retJson.getIntValue("flag")) {
            case 0:
                object.put("info",retJson.getString("info"));
                break;
            case 1:
                final JSONObject info_json = JSON.parseObject(retJson.getString("info"));
                Logger.d_json(info_json.toJSONString());

                if ("y".equals(info_json.getString("status"))){
                    //保存退款单号并更新订单状态
                    final StringBuilder err = new StringBuilder();
                    final List<ContentValues> valueList = new ArrayList<>();
                    final List<String> tables = new ArrayList<>(),whereClauseList = new ArrayList<>();
                    final List<String[]> whereArgsList = new ArrayList<>();

                    tables.add("member_order_info");
                    tables.add("member_order_info");


                    final ContentValues refund_values = new ContentValues(),charge_values = new ContentValues();
                    refund_values.put("order_code",refund_order_code);
                    refund_values.put("status",3);
                    refund_values.put("xnote",info_json.toString());

                    charge_values.put("status",6);

                    valueList.add(refund_values);
                    valueList.add(charge_values);

                    whereClauseList.add("member_id = ? and order_code = ? and order_type=2");
                    whereClauseList.add("member_id = ? and order_code = ? and order_type=1");

                    whereArgsList.add(new String[]{member_id,refund_order_code});
                    whereArgsList.add(new String[]{member_id,origin_order_code});

                    int[] rows = SQLiteHelper.execBatchUpdateSql(tables, valueList,whereClauseList,whereArgsList,err);
                    if (rows == null){
                        Logger.e("支付更新订单状态错误：%s",err);
                    }else{
                        int index = SQLiteHelper.verifyUpdateResult(rows);
                        if (index == -1){
                            if (context.getPrintStatus()){
                                Printer.print(get_print_content(context,origin_order_code));
                            }
                            object.put("info",info_json.getString("info"));
                            return true;
                        } else{
                            final String sz_err = String.format(Locale.CHINA,"数据表,%s未更新，value:%s,whereClause:%s,whereArgs:%s",tables.get(index),valueList.get(index),whereClauseList.get(index), Arrays.toString(whereArgsList.get(index)));
                            Logger.e(sz_err);
                            object.put("info",sz_err);
                        }
                    }
                }else {
                    object.put("info",info_json.getString("info"));
                }
                break;
        }
        return false;
    }

    private static boolean refundWithCheck(final MainActivity context,final JSONObject object){
        final JSONObject data = new JSONObject();
        data.put("appid",context.getAppId());
        data.put("order_code",object.getString("origin_order_code"));
        final String sz_param = HttpRequest.generate_request_parm(data,context.getAppSecret());
        HttpRequest httpRequest = new HttpRequest();
        JSONObject retJson = httpRequest.sendPost(context.getUrl() + "/api/pay2_refund/refund",sz_param,true);
        switch (retJson.getIntValue("flag")){
            case 0:
                object.put("info",retJson.getString("info"));
                break;
            case 1:
                JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                switch (info.getString("status")){
                    case "n":
                        object.put("info",info.getString("info"));
                        break;
                    case "y":
                        if (info.getIntValue("recode") == 1) {
                            final JSONArray refund_money_info = info.getJSONArray("refund_money_info");
                            return true;
                        }else {
                            object.put("info",info.getString("info"));
                        }
                        break;
                }
                break;
        }
        return false;
    }
}
