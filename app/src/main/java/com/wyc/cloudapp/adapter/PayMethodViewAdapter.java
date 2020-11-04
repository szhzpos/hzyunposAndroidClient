package com.wyc.cloudapp.adapter;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;

public class PayMethodViewAdapter extends RecyclerView.Adapter<PayMethodViewAdapter.MyViewHolder> {
    public static final String CASH_METHOD_ID = "1";//现金支付方式id
    private final MainActivity mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    private View mCurrentItemView,mDefaultPayMethodView;
    private final int mWidth;
    public PayMethodViewAdapter(MainActivity context,int width){
        this.mContext = context;
        mWidth = width;
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView pay_method_id,pay_method_name,pay_amt_tv;
        private final View mCurrentLayoutItemView;//当前布局的item
        MyViewHolder(View itemView) {
            super(itemView);
            pay_method_id = itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
            pay_amt_tv = itemView.findViewById(R.id._amt_tv);

            mCurrentLayoutItemView = itemView;

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.pay_method_content_layout, null);
        itemView.setLayoutParams( new RecyclerView.LayoutParams(mWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject pay_method_info = mDatas.getJSONObject(i);
            String szImage,pay_method_id;
            Drawable drawable = null;
            if (pay_method_info != null){
                szImage = (String) pay_method_info.remove("pay_img");
                if (!"".equals(szImage) && szImage != null){
                    szImage = szImage.substring(szImage.lastIndexOf("/") + 1);
                    drawable = Drawable.createFromPath(LoginActivity.IMG_PATH + szImage);
                }else{
                    drawable = mContext.getDrawable(R.drawable.default_pay);
                }
                if (drawable != null){
                    drawable.setBounds(0, 0, 32,32);
                    myViewHolder.pay_method_name.setCompoundDrawables(null,drawable,null,null);
                }
                pay_method_id = pay_method_info.getString("pay_method_id");
                myViewHolder.pay_method_id.setText(pay_method_id);
                myViewHolder.pay_method_name.setText(pay_method_info.getString("name"));

                if (myViewHolder.pay_amt_tv.getText().length() != 0)
                    myViewHolder.pay_amt_tv.setVisibility(View.VISIBLE);
                else
                    myViewHolder.pay_amt_tv.setVisibility(View.GONE);

                if(PayMethodViewAdapter.CASH_METHOD_ID.equals(pay_method_id)){//默认现金
                    showDefaultPayMethod(myViewHolder.mCurrentLayoutItemView);
                }

                if (mOnItemClickListener != null){
                    myViewHolder.mCurrentLayoutItemView.setOnClickListener(view -> {
                        View sign;
                        if (null != mCurrentItemView){
                            if (mCurrentItemView != view){
                                sign = view.findViewById(R.id.sel_sign);
                                if (sign != null)sign.setVisibility(View.VISIBLE);

                                sign = mCurrentItemView.findViewById(R.id.sel_sign);
                                if (sign != null)sign.setVisibility(View.GONE);

                                mCurrentItemView = view;
                            }
                        }else{
                            sign = view.findViewById(R.id.sel_sign);
                            if (sign != null)sign.setVisibility(View.VISIBLE);
                            mCurrentItemView = view;
                        }
                        mOnItemClickListener.onClick(view,i);
                    });
                }

                myViewHolder.mCurrentLayoutItemView.setOnHoverListener(hoverListener);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow (@NonNull MyViewHolder holder){

    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    private final View.OnHoverListener hoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            final TextView view = v.findViewById(R.id.pay_method_name);
            if (view != null){
                if (MotionEvent.ACTION_HOVER_MOVE == event.getAction())
                    view.setTextColor(mContext.getColor(R.color.white));
                else
                    view.setTextColor(mContext.getColor(R.color.blue));
            }
            return false;
        }
    };

    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public JSONObject getItem(int i){
        return mDatas == null ? null : mDatas.getJSONObject(i);
    }

    public void setDatas(final String support_code){
        final StringBuilder err = new StringBuilder();
        if (mContext.isConnection())
            mDatas = SQLiteHelper.getListToJson("select *  from pay_method where status = '1' and support like '%" + support_code +"%' order by sort",err);
        else
            mDatas = SQLiteHelper.getListToJson("select *  from pay_method where is_check = 2 and status = '1' and support like '%" + support_code +"%' order by sort",err);

        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载支付方式错误：" + err,mContext,null);
        }
    }

    public void loadRefundPayMeothd(){
        final StringBuilder err = new StringBuilder();
        mDatas = SQLiteHelper.getListToJson("select *  from pay_method where status = '1' and is_check = 2 order by sort",err);
        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载支付方式错误：" + err,mContext,null);
        }
    }

    public JSONObject get_pay_method(final String pay_method_id){
        if (mDatas != null && pay_method_id != null){
            for (int i = 0,length = mDatas.size();i < length;i++){
                JSONObject jsonObject = mDatas.getJSONObject(i);
                if (pay_method_id.equals(jsonObject.getString("pay_method_id"))){
                    return jsonObject;
                }
            }
        }
        return null;
    }

    public void setCurrentPayMethod(){
        if (mCurrentItemView != null)mCurrentItemView.callOnClick();
    }

    public String getDefaultPayMethodId(){
        if (null != mDefaultPayMethodView){
           final TextView idTv = mDefaultPayMethodView.findViewById(R.id.pay_method_id);
           if (null != idTv){
               return idTv.getText().toString();
           }
        }
        return "";
    }

    public void showDefaultPayMethod(View view){
        View sign;
        if (view != null)mDefaultPayMethodView = view;
        if (mDefaultPayMethodView != null){
            if (null != mCurrentItemView){
                if (mCurrentItemView != mDefaultPayMethodView){
                    sign = mDefaultPayMethodView.findViewById(R.id.sel_sign);
                    if (sign != null)sign.setVisibility(View.VISIBLE);

                    sign = mCurrentItemView.findViewById(R.id.sel_sign);
                    if (sign != null)sign.setVisibility(View.GONE);
                }
            }else{
                sign = mDefaultPayMethodView.findViewById(R.id.sel_sign);
                if (sign != null)sign.setVisibility(View.VISIBLE);
            }
            mCurrentItemView = mDefaultPayMethodView;
        }
    }

    public void showCurrentPayMethodAmt(double amt){
        if (mCurrentItemView != null){
            final TextView tv = mCurrentItemView.findViewById(R.id._amt_tv);
            tv.setVisibility(View.VISIBLE);
            tv.setText(String.valueOf(amt));

            notifyDataSetChanged();
        }
    }

}
