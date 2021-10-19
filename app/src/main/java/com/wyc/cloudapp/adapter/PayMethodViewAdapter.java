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
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;
/*
* 20210712 sign deprecated
* */
@Deprecated
public class PayMethodViewAdapter extends RecyclerView.Adapter<PayMethodViewAdapter.MyViewHolder> {
    private static final String CASH_METHOD_ID = "1";//现金支付方式id
    private static final String VIP_METHOD_ID = "5";//会员支付方式id
    private static final String DEFAULT_PAY_METHOD_ID = CASH_METHOD_ID;//默认支付方式ID为现金
    private static final String CUR_PAY_METHOD_LABEL = "isCur";
    private final MainActivity mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    private final PayDetailViewAdapter mPayDetailViewAdapter;
    private JSONObject mDefaultPayMethod,mCurrentPayMethod;
    public PayMethodViewAdapter(MainActivity context,final PayDetailViewAdapter payDetailViewAdapter){
        mContext = context;
        mPayDetailViewAdapter = payDetailViewAdapter;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView pay_method_id,pay_method_name,pay_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            pay_method_id = itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
            pay_amt_tv = itemView.findViewById(R.id._amt_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.pay_method_content_layout, null);
        itemView.setLayoutParams( new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) mContext.getResources().getDimension(R.dimen.pay_method_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
    }

    @Override
    public void onViewAttachedToWindow (@NonNull MyViewHolder holder){
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            final JSONObject pay_method_info = mDatas.getJSONObject(i);
            if (pay_method_info != null){
                Drawable drawable = null;
                String szImage = pay_method_info.getString("pay_img");
                if (!"".equals(szImage) && szImage != null){
                    szImage = szImage.substring(szImage.lastIndexOf("/") + 1);
                    drawable = Drawable.createFromPath(CustomApplication.getGoodsImgSavePath() + szImage);
                }else{
                    drawable = mContext.getDrawable(R.drawable.default_pay);
                }
                if (drawable != null){
                    drawable.setBounds(0, 0, 32,32);
                    myViewHolder.pay_method_name.setCompoundDrawables(null,drawable,null,null);
                }
                final String pay_method_id = pay_method_info.getString("pay_method_id");
                myViewHolder.pay_method_id.setText(pay_method_id);
                myViewHolder.pay_method_name.setText(pay_method_info.getString("name"));

                if (mPayDetailViewAdapter != null){
                    final double pay_amt = Utils.getNotKeyAsNumberDefault(mPayDetailViewAdapter.findPayDetailById(pay_method_id),"pamt",0.0);
                    if (Utils.equalDouble(pay_amt,0.0))
                        myViewHolder.pay_amt_tv.setVisibility(View.GONE);
                    else {
                        myViewHolder.pay_amt_tv.setText(String.format(Locale.CHINA,"%.2f",pay_amt));
                        myViewHolder.pay_amt_tv.setVisibility(View.VISIBLE);
                    }
                }

                if (pay_method_info.getBooleanValue(CUR_PAY_METHOD_LABEL)){
                    Utils.disableView(myViewHolder.itemView,300);
                    setSelectedSignVisibility(myViewHolder.itemView,View.VISIBLE);
                }else {
                    setSelectedSignVisibility(myViewHolder.itemView,View.GONE);
                }

                myViewHolder.itemView.setTag(pay_method_id);
                if (mOnItemClickListener != null){
                    myViewHolder.itemView.setOnClickListener(mClickListener);
                }
                myViewHolder.itemView.setOnHoverListener(hoverListener);
            }
        }
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final JSONObject object = findPayMethodById(Utils.getViewTagValue(v,""));
            if (object != null && mOnItemClickListener != null){
                notifyDataSetChanged();

                if (object != mCurrentPayMethod){
                    if (null != mCurrentPayMethod)
                        mCurrentPayMethod.put(CUR_PAY_METHOD_LABEL,false);

                    mCurrentPayMethod = object;
                }

                if (object != mDefaultPayMethod){
                    if (null != mDefaultPayMethod)
                        mDefaultPayMethod.put(CUR_PAY_METHOD_LABEL,false);
                }

                object.put(CUR_PAY_METHOD_LABEL,true);
                mOnItemClickListener.onClick(object);
            }
        }
    };

    private void setSelectedSignVisibility(final View view,int v){
        final View sign = view.findViewById(R.id.sel_sign);
        if (sign != null && sign.getVisibility() != v){
            sign.setVisibility(v);
        }
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
        void onClick(@NonNull final JSONObject object);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setDatas(final String support_code){
        final StringBuilder err = new StringBuilder();
        if (mContext.isConnection())
            mDatas = SQLiteHelper.getListToJson("select *  from pay_method where status = '1' and support like '%" + support_code +"%' order by sort",err);
        else
            mDatas = SQLiteHelper.getListToJson("select *  from pay_method where is_check = 2 and status = '1' and support like '%" + support_code +"%' order by sort",err);

        if (mDatas != null){
            setDefaultPayMethod();
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载支付方式错误：" + err, null);
        }
    }

    public void loadRefundPayMethod(){
        final StringBuilder err = new StringBuilder();
        mDatas = SQLiteHelper.getListToJson("select *  from pay_method where status = '1' and is_check = 2 order by sort",err);
        if (mDatas != null){
            setDefaultPayMethod();
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载支付方式错误：" + err, null);
        }
    }

    private void setDefaultPayMethod(){
        mDefaultPayMethod = findPayMethodById(DEFAULT_PAY_METHOD_ID);
        if (null != mDefaultPayMethod){
            mDefaultPayMethod.put(CUR_PAY_METHOD_LABEL,true);
        }
    }

    public JSONObject findPayMethodById(final String pay_method_id){
        final JSONArray array = mDatas;
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

    public int findPayMethodIndexById(final String pay_method_id){
        final JSONArray array = mDatas;
        if (array != null && pay_method_id != null){
            for (int i = 0,length = array.size();i < length;i++){
                final JSONObject jsonObject = array.getJSONObject(i);
                if (pay_method_id.equals(jsonObject.getString("pay_method_id"))){
                    return i;
                }
            }
        }
        return -1;
    }

    public static JSONObject getPayMethod(final String pay_method_id){
        final JSONObject object = new JSONObject();
        if (SQLiteHelper.execSql(object,"select *  from pay_method where status = '1' and pay_method_id = '" + pay_method_id + "'")){
            if (object.isEmpty())return null;
            return object;
        }
        return null;
    }

    public void showDefaultPayMethod(){
        if (null != mDefaultPayMethod){
            mDefaultPayMethod.put(CUR_PAY_METHOD_LABEL,true);
            if (mDefaultPayMethod != mCurrentPayMethod){
                mCurrentPayMethod.put(CUR_PAY_METHOD_LABEL,false);
            }
            notifyDataSetChanged();
        }
    }

    public JSONObject getDefaultPayMethod(){
        return mDefaultPayMethod;
    }
    public static String getDefaultPayMethodId(){
        return DEFAULT_PAY_METHOD_ID;
    }
    public static String getCashMethodId(){
        return CASH_METHOD_ID;
    }
    public static boolean isApiCheck(int check){
        return check == 1;
    }

    public static boolean isMolForPayMethod(final JSONObject object){
        return object != null && 2 == object.getIntValue("is_moling");
    }

    public static boolean isVipPay(final JSONObject object){
        return Utils.getNullStringAsEmpty(object,"pay_method_id").equals(VIP_METHOD_ID);
    }

    public boolean isMolWithPayed(){//查询已支付记录里面是否存在参与抹零的支付方式
        final JSONArray array = mPayDetailViewAdapter == null ? null :mPayDetailViewAdapter.getDatas();
        if (array != null){
            for (int i = 0,length = array.size();i < length;i ++){
                final JSONObject jsonObject = array.getJSONObject(i);
                if (PayMethodViewAdapter.isMolForPayMethod(findPayMethodById(jsonObject.getString("pay_method_id"))))return true;
            }
        }
        return false;
    }
}
