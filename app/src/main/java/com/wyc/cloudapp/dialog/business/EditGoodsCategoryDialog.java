package com.wyc.cloudapp.dialog.business;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.business.MobileEditGoodsCategoryActivity;
import com.wyc.cloudapp.adapter.bean.TreeListItem;
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
    private TreeListItem mSuperCategory;
    private TextView mCategoryCodeTv;
    private EditText mCategoryNameEt;
    public EditGoodsCategoryDialog(@NonNull MobileEditGoodsCategoryActivity context, boolean m) {
        super(context, context.getString(m ? R.string.modify_category_sz :R.string.new_category_sz),m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected int getLayout() {
        return R.layout.add_goods_category_dialog;
    }

    @Override
    protected void sure() {
        final String category_code = mCategoryCodeTv.getText().toString(),category_name = mCategoryNameEt.getText().toString();
        if (!Utils.isNotEmpty(category_code)){
            final String code_hint = mContext.getString(R.string.category_code_sz);
            MyDialog.ToastMessage(mCategoryCodeTv,mContext.getString(R.string.not_empty_hint_sz,code_hint.substring(0,code_hint.length() - 1)),mContext,getWindow());
            return;
        }
        if (!Utils.isNotEmpty(category_name)){
            final String name_hint = mContext.getString(R.string.category_name_colon_sz);
            MyDialog.ToastMessage(mCategoryNameEt,mContext.getString(R.string.not_empty_hint_sz,name_hint.substring(0,name_hint.length() - 1)),mContext,getWindow());
            return;
        }

        final JSONObject param_obj = new JSONObject();
        param_obj.put("appid",mContext.getAppId());
        param_obj.put("category_code",category_code);
        param_obj.put("name",category_name);

        showProgress();
        CustomApplication.execute(()->{
            final String param_sz = HttpRequest.generate_request_parm(param_obj,mContext.getAppSecret());
            JSONObject ret_obj = HttpUtils.sendPost(mContext.getUrl() + "/api/goods_set/category_set",param_sz,true);
            if (HttpUtils.checkRequestSuccess(ret_obj)){
                try {
                    ret_obj = JSONObject.parseObject(ret_obj.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(ret_obj)){
                        if (!modify){
                            ((MobileEditGoodsCategoryActivity)mContext).addCategory(ret_obj.getString("category_id"),category_code,category_name,mSuperCategory.getLevel() + 1);
                        }
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

    private void initView(){
        mCategoryNameEt = findViewById(R.id.category_name_tv);
        mCategoryNameEt.requestFocus();

        final TextView super_category_tv = findViewById(R.id.super_category_tv),category_code_tv = findViewById(R.id.category_code_tv);
        if (mSuperCategory != null){
            final String name = mSuperCategory.getItem_name(),code = mSuperCategory.getCode();
            super_category_tv.setText(String.format(Locale.CHINA,"%s[%s]",name,code));
            if (modify){//修改模式下mSuperCategory就是要修改的内容
                category_code_tv.setText(code);
                mCategoryNameEt.setText(name);
            }else {
                category_code_tv.setText(generateCategoryCode());
            }
        }
        mCategoryCodeTv = category_code_tv;
    }
    private String generateCategoryCode(){
        final String super_code = mSuperCategory.getCode(),
                sql = "SELECT category_code FROM shop_category where category_code like '"+ super_code +"%' and parent_id="+ mSuperCategory.getItem_id() +" order by category_code desc limit 1";
        final StringBuilder err = new StringBuilder();
        final String _code = SQLiteHelper.getString(sql,err);
        Logger.d("_code:%s,sql:%s",_code,sql);
        if (null != _code){
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
            MyDialog.ToastMessage(err.toString(),mContext,getWindow());
        }
        return "";
    }

    public void setSuperCategory(TreeListItem category) {
        this.mSuperCategory = category;
    }
}
