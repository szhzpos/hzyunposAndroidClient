package com.wyc.cloudapp.dialog.business;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.MobileEditGoodsCategoryActivity;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.business
 * @ClassName: AddGoodsCategoryDialog
 * @Description: 新增商品分类
 * @Author: wyc
 * @CreateDate: 2021/5/11 16:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/11 16:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class EditGoodsCategoryDialog extends AbstractEditArchiveDialog {
    private TreeListItem mCategory;
    private TextView mCategoryCodeTv;
    private EditText mCategoryNameEt;
    public EditGoodsCategoryDialog(@NonNull MobileEditGoodsCategoryActivity context, boolean m) {
        super(context, context.getString(m ? R.string.modify_category_sz :R.string.new_category_sz),m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDefaultSuperCategory();

        initView();
    }

    @Override
    protected int getLayout() {
        return R.layout.edit_goods_category_dialog;
    }

    @Override
    protected void sure() {
        final String category_code = mCategoryCodeTv.getText().toString(),category_name = mCategoryNameEt.getText().toString();
        if (!Utils.isNotEmpty(category_code)){
            final String code_hint = mContext.getString(R.string.category_code_sz);
            MyDialog.ToastMessage(mCategoryCodeTv,mContext.getString(R.string.not_empty_hint_sz,code_hint.substring(0,code_hint.length() - 1)), getWindow());
            return;
        }
        if (!Utils.isNotEmpty(category_name)){
            final String name_hint = mContext.getString(R.string.category_name_colon_sz);
            MyDialog.ToastMessage(mCategoryNameEt,mContext.getString(R.string.not_empty_hint_sz,name_hint.substring(0,name_hint.length() - 1)), getWindow());
            return;
        }

        final JSONObject param_obj = new JSONObject();
        param_obj.put("appid",mContext.getAppId());
        param_obj.put("category_code",category_code);
        param_obj.put("name",category_name);

        showProgress();
        CustomApplication.execute(()->{
            final String param_sz = HttpRequest.generate_request_parma(param_obj,mContext.getAppSecret());
            JSONObject ret_obj = HttpUtils.sendPost(mContext.getUrl() + "/api/goods_set/category_set",param_sz,true);
            if (HttpUtils.checkRequestSuccess(ret_obj)){
                try {
                    ret_obj = JSONObject.parseObject(ret_obj.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(ret_obj)){
                        final MobileEditGoodsCategoryActivity activity = ((MobileEditGoodsCategoryActivity)mContext);
                        if (modify){
                            activity.updateCategory(category_name);
                        }else
                            activity.addCategory(ret_obj.getString("category_id"),category_code,category_name, mCategory.getLevel() + 1);

                        dismiss();
                    }else throw new JSONException(ret_obj.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.toastMessage(e.getMessage());
                }
            }
            dismissProgress();
        });
    }

    private void initDefaultSuperCategory(){
        TreeListItem parent = new TreeListItem();
        parent.setItem_id("0");
        parent.setCode("");
        parent.setLevel(-1);
        parent.setItem_name(mContext.getString(R.string.all_category_sz));
        if (mCategory == null){
            mCategory = new TreeListItem();
            mCategory.setLevel(-1);
            mCategory.setP_ref(parent);
        }else {
            if (null == mCategory.getP_ref()){
                mCategory.setP_ref(parent);
            }
        }
    }

    private void initView(){
        mCategoryNameEt = findViewById(R.id.category_name_tv);
        mCategoryNameEt.requestFocus();

        final TextView super_category_tv = findViewById(R.id.super_category_tv),category_code_tv = findViewById(R.id.category_code_tv);
        String name = mCategory.getItem_name(),code = mCategory.getCode();

        if (modify){
            TreeListItem parent = mCategory.getP_ref();
            if (null != parent){
                final String p_name = parent.getItem_name(),p_code = parent.getCode();
                super_category_tv.setText(Utils.isNotEmpty(p_code) ? String.format(Locale.CHINA,"%s[%s]",p_name,p_code) : p_name);
            }
            category_code_tv.setText(code);
            mCategoryNameEt.setText(name);
        }else {
            if (Utils.isNotEmpty(code))
                super_category_tv.setText(String.format(Locale.CHINA,"%s[%s]",name,code));
            else {
                super_category_tv.setText(mContext.getString(R.string.all_category_sz));
            }
            category_code_tv.setText(generateCategoryCode());
        }
        mCategoryCodeTv = category_code_tv;
    }
    private String generateCategoryCode(){
        String super_code = mCategory.getCode(),id = mCategory.getItem_id(),
                sql = "SELECT category_code FROM shop_category where category_code like '"+ (Utils.isNotEmpty(super_code) ? super_code : "%") +"%' and parent_id="+ (Utils.isNotEmpty(id) ? id : 0) +" order by category_code desc limit 1";
        final StringBuilder err = new StringBuilder();
        final String _code = SQLiteHelper.getString(sql,err);
        Logger.d("_code:%s,sql:%s",_code,sql);
        if (null != _code){
            if (null == super_code)super_code = "";

            int index = -1;
            if (!"".equals(_code)){
                final String IDX = _code.substring(super_code.length());
                try {
                    index = Integer.parseInt(IDX);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }else {
                index = 0;
            }
            if (index != -1){
                return String.format(Locale.CHINA,"%s%02d",super_code,index + 1);
            }
        }else {
            MyDialog.ToastMessage(err.toString(), getWindow());
        }
        return "";
    }

    public void setCategory(TreeListItem category) {
        this.mCategory = category;
    }
}
