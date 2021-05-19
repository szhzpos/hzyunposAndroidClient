package com.wyc.cloudapp.dialog.business;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.bean.VipGrade;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.business
 * @ClassName: EditVipCategoryDialog
 * @Description: 编辑会员类别对话框
 * @Author: wyc
 * @CreateDate: 2021/5/19 17:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/19 17:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class EditVipCategoryDialog extends AbstractEditArchiveDialog {
    private EditText mVipCategory,mVipDiscount,mVipIntegralRatio,mVipLowestAmt;
    private VipGrade mCurObj;
    public EditVipCategoryDialog(@NonNull MainActivity context, boolean m) {
        super(context, context.getString(m ? R.string.modify_vip_category_sz :R.string.new_vip_category_sz),m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public void show() {
        super.show();
        if (mCurObj != null){
            mVipCategory.setTag(mCurObj.getGrade_id());
            mVipCategory.setText(mCurObj.getGrade_name());
            mVipDiscount.setText(String.format(Locale.CHINA,"%.2f",mCurObj.getDiscount()));
            mVipIntegralRatio.setText(String.format(Locale.CHINA,"%.2f",mCurObj.getPoints_multiple()));
            mVipLowestAmt.setText(String.format(Locale.CHINA,"%.2f",mCurObj.getMin_recharge_money()));
        }
    }

    @Override
    protected double getWidthRatio(){
        return 0.98;
    }

    public void setCurObj(VipGrade obj) {
        this.mCurObj = obj;
    }

    private void initView(){
        mVipCategory = findViewById(R.id.vip_category_tv);
        mVipDiscount = findViewById(R.id.vip_discount_tv);
        mVipIntegralRatio = findViewById(R.id.vip_integral_ratio_tv);
        mVipLowestAmt = findViewById(R.id.vip_lowest_amt_tv);
    }



    @Override
    protected int getLayout() {
        return R.layout.edit_vip_category_dialog;
    }

    @Override
    protected void sure() {
        final String category_name = mVipCategory.getText().toString(),discount = mVipDiscount.getText().toString(),ratio = mVipIntegralRatio.getText().toString();
        if (!Utils.isNotEmpty(category_name)){
            final String code_hint = mContext.getString(R.string.vip_category_name_sz);
            MyDialog.ToastMessage(mVipCategory,mContext.getString(R.string.not_empty_hint_sz,code_hint.substring(0,code_hint.length() - 1)),mContext,getWindow());
            return;
        }
        if (!Utils.isNotEmpty(discount)){
            final String name_hint = mContext.getString(R.string.vip_discount_colon_sz);
            MyDialog.ToastMessage(mVipDiscount,mContext.getString(R.string.not_empty_hint_sz,name_hint.substring(0,name_hint.length() - 1)),mContext,getWindow());
            return;
        }
        if (!Utils.isNotEmpty(ratio)){
            final String name_hint = mContext.getString(R.string.vip_integral_ratio_sz);
            MyDialog.ToastMessage(mVipIntegralRatio,mContext.getString(R.string.not_empty_hint_sz,name_hint.substring(0,name_hint.length() - 1)),mContext,getWindow());
            return;
        }

        final JSONObject param_obj = new JSONObject();
        param_obj.put("appid",mContext.getAppId());
        param_obj.put("grade_name",category_name);
        param_obj.put("grade_id",Utils.getViewTagValue(mVipCategory,""));
        param_obj.put("discount",discount);
        param_obj.put("points_multiple",ratio);

        showProgress();
        CustomApplication.execute(()->{
            final String param_sz = HttpRequest.generate_request_parm(param_obj,mContext.getAppSecret());
            JSONObject ret_obj = HttpUtils.sendPost(mContext.getUrl() + "/api/goods_set/category_set",param_sz,true);
            if (HttpUtils.checkRequestSuccess(ret_obj)){
                try {
                    ret_obj = JSONObject.parseObject(ret_obj.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(ret_obj)){
                        Logger.d_json(ret_obj);
                        dismiss();
                    }else throw new JSONException(ret_obj.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
            dismissProgress();
        });
    }

    public static void start(MainActivity context, VipGrade grade,boolean modify){
        final EditVipCategoryDialog dialog = new EditVipCategoryDialog(context,modify);
        dialog.setCurObj(grade);
        dialog.show();
    }
}
