package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;

import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.R;

public class CustomePopupWindow extends PopupWindow {
    private static final String SEPARATE = "\t";
    private ListView mListView;
    private ArrayAdapter mArrayAdapter, mTmpAdapter;
    private boolean isSelect = false,isAutoSetContent;
    private View mView, mFoucs_view;
    private int mShowContentType;
    private Context mContext;
    private JSONArray mShowContents;
    private OngetSelectContent mGetSelectContent;

    public CustomePopupWindow(Context context){
        super(context);
        mContext = context;
        setOutsideTouchable(true);
        this.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        this.setBackgroundDrawable(mContext.getDrawable(R.drawable.round_border_sub_gray));
        mListView = new ListView(context);
        mListView.setPadding(2,2,2,2);
        mArrayAdapter = new ArrayAdapter(context,R.layout.drop_down_style);//由于要重复显示 在构造中直接新建对象，并且在取消显示的时候只是清空内容没有清除对象，这样不会导致系统重复分配释放内存
        mTmpAdapter = new ArrayAdapter(context,R.layout.drop_down_style);

        this.setContentView(mListView);

        mListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            String szTmp,szView = ((TextView)view1).getText().toString();
            String[] sz_arr;
            if (szView.contains(SEPARATE)){
                sz_arr = szView.split(SEPARATE);
                switch (mShowContentType){
                    case 1:
                        szTmp =sz_arr[0];
                        break;
                    case 2:
                        szTmp = szView;
                        break;
                    case 3:
                        mView.setTag(szView.split(SEPARATE)[0]);
                        szTmp = szView.split(SEPARATE)[1];
                        break;
                    default:
                        szTmp =sz_arr[1];
                        break;
                }

            }else{
                szTmp = szView;
            }
            ((TextView) mView).setText(szTmp);
            isSelect = true;
            new Handler().postDelayed(() -> {
                if (mFoucs_view != null && mFoucs_view instanceof  EditText){
                    if (mContext instanceof  Activity)
                        Utils.setFocus((Activity) mContext,(EditText) mFoucs_view);
                }
            },300);

            if (mGetSelectContent != null){
                mGetSelectContent.getContent(mShowContents.getJSONObject(i));
            }

            dismiss();
        }
        );
    }

    public void initContent(@NonNull View v, @NonNull  String sql, @NonNull String[] sz, int show,boolean setContent,OngetSelectContent ongetSelectContent){
        //mShowContentType 1 有编码显示编码 2 两个都显示 默认显示名称 setContent设置在没选择内容的情况下是否自动选择内容
        StringBuilder err = new StringBuilder();
        mShowContents = SQLiteHelper.getListToJson(sql, 0, 0, false,err);
        StringBuilder stringBuilder = new StringBuilder();
        mView = v;
        mShowContentType = show ;
        isAutoSetContent = setContent;
        mGetSelectContent = ongetSelectContent;

        if (isSelect)isSelect = false;
        if (null != mShowContents) {
            if (!mArrayAdapter.isEmpty()) mArrayAdapter.clear();
            if (!isAutoSetContent) mArrayAdapter.add("");
            for (int i = 0, len = mShowContents.size(); i < len; i++) {
                for (String tmp : sz){
                    if (stringBuilder.length() > 0)stringBuilder.append(SEPARATE);
                    if ("NULL".equals(mShowContents.getJSONObject(i).getString(tmp)))
                        stringBuilder.append(" ");
                    else
                        stringBuilder.append(mShowContents.getJSONObject(i).getString(tmp));
                }
                mArrayAdapter.add(stringBuilder.toString());
                stringBuilder.delete(0,stringBuilder.length());
            }
            mListView.setAdapter(mArrayAdapter);
            this.setWidth(mView.getWidth());
        }else{
           MyDialog.displayErrorMessage(null,"查询内容出错： " + err,v.getContext());
        }
    }

    public void initContent(View foucs,@NonNull View v, @NonNull  String sql, @NonNull String[] sz, int show,boolean setContent,OngetSelectContent ongetSelectContent ){
        //mShowContentType 1 有编码显示编码 2 两个都显示 默认显示名称 setContent设置在没选择内容的情况下是否自动选择内容
        StringBuilder err = new StringBuilder();
        mShowContents = SQLiteHelper.getListToJson(sql, 0, 0, false,err);
        StringBuilder stringBuilder = new StringBuilder();
        mView = v;
        mFoucs_view = foucs;
        mShowContentType = show ;
        isAutoSetContent = setContent;
        mGetSelectContent = ongetSelectContent;

        if (isSelect)isSelect = false;
        if (null != mShowContents) {
            if (!mArrayAdapter.isEmpty()) mArrayAdapter.clear();
            if (!isAutoSetContent) mArrayAdapter.add("");
            for (int i = 0, len = mShowContents.size(); i < len; i++) {
                for (String tmp : sz){
                    if (stringBuilder.length() > 0)stringBuilder.append(SEPARATE);
                    if ("NULL".equals(mShowContents.getJSONObject(i).getString(tmp)))
                        stringBuilder.append(" ");
                    else
                        stringBuilder.append(mShowContents.getJSONObject(i).getString(tmp));
                }
                mArrayAdapter.add(stringBuilder.toString());
                stringBuilder.delete(0,stringBuilder.length());
            }
            mListView.setAdapter(mArrayAdapter);
            this.setWidth(mView.getWidth());
        }else{
            MyDialog.displayErrorMessage(null,"查询内容出错： " + err,v.getContext());
        }
    }

    public void initContent(View foucs,@NonNull View v, @NonNull  JSONArray array,String[] sz, int show,boolean setContent,OngetSelectContent ongetSelectContent ){
        //show 1 有编码显示编码 2 两个都显示 默认显示名称 setContent设置在没选择内容的情况下是否自动选择内容

        StringBuilder stringBuilder = new StringBuilder();
        mView = v;
        mFoucs_view = foucs;
        mShowContentType = show ;
        isAutoSetContent = setContent;
        mGetSelectContent = ongetSelectContent;
        mShowContents = array;

        if (isSelect)isSelect = false;
        if (!mArrayAdapter.isEmpty()) mArrayAdapter.clear();
        if (!isAutoSetContent) mArrayAdapter.add("");
        for (int i = 0, len = mShowContents.size(); i < len; i++) {
            if (sz != null){
                for (String tmp : sz){
                    if (stringBuilder.length() > 0)stringBuilder.append(SEPARATE);
                    if ("NULL".equals(mShowContents.getJSONObject(i).getString(tmp)))
                        stringBuilder.append(" ");
                    else
                        stringBuilder.append(mShowContents.getJSONObject(i).getString(tmp));
                }
                mArrayAdapter.add(stringBuilder.toString());
                stringBuilder.delete(0,stringBuilder.length());
            }else {
                mArrayAdapter.add(mShowContents.get(i));
            }
        }
        mListView.setAdapter(mArrayAdapter);
        this.setWidth(mView.getWidth());
    }

    public void updateContent(CharSequence charSequence){
        Object objTmp;
        if (this.isShowing()) {
            if (!mTmpAdapter.isEmpty()) mTmpAdapter.clear();
            mTmpAdapter.add("");
            for (int i = 0; i < mArrayAdapter.getCount(); i++) {
                objTmp = mArrayAdapter.getItem(i);
                if (objTmp != null) {
                    if (objTmp.toString().contains(charSequence)) {
                        mTmpAdapter.add(objTmp);
                    }
                }

            }
            if (!mTmpAdapter.isEmpty() && mTmpAdapter.getCount() != 1) {
                mListView.setAdapter(null);
                mListView.setAdapter(mTmpAdapter);
             } else {
               this.dismiss();
            }
        }
    }

    @Override
    public void dismiss(){
        super.dismiss();
        if (isAutoSetContent) {
            if (!isSelect && mListView.getAdapter() != null) {
                if (!mListView.getAdapter().isEmpty()) {
                    String szView = mListView.getAdapter().getItem(0).toString(), szTmp;
                    if (szView.contains(SEPARATE)) {
                        switch (mShowContentType) {
                            case 1:
                                szTmp = szView.split(SEPARATE)[0];
                                break;
                            case 2:
                                szTmp = szView;
                                break;
                            case 3:
                                this.mView.setTag(szView.split(SEPARATE)[0]);
                                szTmp = szView.split(SEPARATE)[1];
                                break;
                            default:
                                szTmp = szView.split(SEPARATE)[1];
                                break;
                        }
                    } else {
                        szTmp = szView;
                    }
                    ((EditText) this.mView).setText(szTmp);
                } else {
                    if (this.mView != null) {
                        ((EditText) this.mView).setText("");
                    }
                }
            }
        }
        if (!mArrayAdapter.isEmpty()) mArrayAdapter.clear();
        if (!mTmpAdapter.isEmpty()) mTmpAdapter.clear();
        setBackgroundAlpha(1.0f);
    }
    public void show(View view){
        if (!this.isShowing() && !mArrayAdapter.isEmpty()){
            this.setHeight((int)mContext.getResources().getDimension(R.dimen.drop_down_height) * 2);
            setBackgroundAlpha(0.5f);
            this.showAsDropDown(view,0,0);
        }
    }

    public void show(View view,int size){
        if (!this.isShowing() && !mArrayAdapter.isEmpty()){
            this.setHeight((int)mContext.getResources().getDimension(R.dimen.drop_down_height) * size);
            setBackgroundAlpha(0.5f);
            showAtLocation(view,Gravity.CENTER,0,0);
        }
    }

    private void setBackgroundAlpha(float bgAlpha) {
        if (mContext instanceof Activity) {
            Activity a = (Activity)mContext;
            WindowManager.LayoutParams lp = a.getWindow()
                    .getAttributes();
            lp.alpha = bgAlpha;
            a.getWindow().setAttributes(lp);
        }
    }

    public interface OngetSelectContent{
        void getContent(JSONObject json);
    }
}
