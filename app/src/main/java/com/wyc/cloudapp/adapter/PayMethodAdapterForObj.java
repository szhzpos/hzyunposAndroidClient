package com.wyc.cloudapp.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.data.room.entity.PayMethod;
import com.wyc.cloudapp.utils.Utils;

import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: PayMethodAdapterForObj
 * @Description: 支付方式适配器，使用bean对象
 * @Author: wyc
 * @CreateDate: 2021-07-12 10:39
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-12 10:39
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class PayMethodAdapterForObj extends AbstractDataAdapterForList<PayMethod,PayMethodAdapterForObj.MyViewHolder> {
    private static final int DEFAULT_PAY_METHOD_ID = PayMethod.CASH_METHOD_ID;//默认支付方式ID为现金
    private final MainActivity mContext;
    private final PayDetailViewAdapter mPayDetailViewAdapter;
    private OnItemClickListener mOnItemClickListener;
    private PayMethod mDefaultPayMethod,mCurrentPayMethod;
    public PayMethodAdapterForObj(final MainActivity context,final PayDetailViewAdapter payDetailViewAdapter){
        mContext = context;
        mPayDetailViewAdapter = payDetailViewAdapter;
    }

    static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
        private final TextView pay_method_id,pay_method_name,pay_amt_tv;
        public MyViewHolder(View itemView) {
            super(itemView);
            pay_method_id = findViewById(R.id.pay_method_id);
            pay_method_name =  findViewById(R.id.pay_method_name);
            pay_amt_tv = findViewById(R.id._amt_tv);
        }
    }

    public interface OnItemClickListener{
        void onClick(@NonNull final PayMethod object);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.pay_method_content_layout, null);
        itemView.setLayoutParams( new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) mContext.getResources().getDimension(R.dimen.pay_method_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
        final PayMethod payMethod = getItem(position);
        if (null != payMethod){
            Drawable drawable;
            String szImage = payMethod.getPay_img();
            if (Utils.isNotEmpty(szImage)){
                szImage = szImage.substring(szImage.lastIndexOf("/") + 1);
                drawable = Drawable.createFromPath(CustomApplication.getGoodsImgSavePath() + szImage);
            }else{
                drawable = mContext.getDrawable(R.drawable.default_pay);
            }
            if (drawable != null){
                drawable.setBounds(0, 0, 32,32);
                myViewHolder.pay_method_name.setCompoundDrawables(null,drawable,null,null);
            }

            final String pay_method_id = String.valueOf(payMethod.getPay_method_id());
            myViewHolder.pay_method_id.setText(pay_method_id);
            myViewHolder.pay_method_name.setText(payMethod.getName());

            if (mPayDetailViewAdapter != null){
                final double pay_amt = Utils.getNotKeyAsNumberDefault(mPayDetailViewAdapter.findPayDetailById(pay_method_id),"pamt",0.0);
                if (Utils.equalDouble(pay_amt,0.0))
                    myViewHolder.pay_amt_tv.setVisibility(View.GONE);
                else {
                    myViewHolder.pay_amt_tv.setText(String.format(Locale.CHINA,"%.2f",pay_amt));
                    myViewHolder.pay_amt_tv.setVisibility(View.VISIBLE);
                }
            }

            if (payMethod.isCur()){
                Utils.disableView(myViewHolder.itemView,300);
                setSelectedSignVisibility(myViewHolder.itemView,View.VISIBLE);
            }else {
                setSelectedSignVisibility(myViewHolder.itemView,View.GONE);
            }

            myViewHolder.itemView.setTag(payMethod);
            if (mOnItemClickListener != null){
                myViewHolder.itemView.setOnClickListener(mClickListener);
            }
        }
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final Object object = v.getTag();
            if (object instanceof PayMethod){
                PayMethod payMethod = (PayMethod) object;

                notifyDataSetChanged();
                if (!object.equals(mCurrentPayMethod)){
                    if (null != mCurrentPayMethod)
                        mCurrentPayMethod.setCur(false);

                    mCurrentPayMethod = payMethod;
                }
                if (!object.equals(mDefaultPayMethod)){
                    if (null != mDefaultPayMethod)
                        mDefaultPayMethod.setCur(false);
                }
                payMethod.setCur(true);
                if (mOnItemClickListener != null)mOnItemClickListener.onClick(payMethod);
            }
        }
    };

    public void setData(final String support_code){
        final List<PayMethod> payMethods = AppDatabase.getInstance().PayMethodDao().getPayMethodBySupport(support_code);
        if (!mContext.isConnection()){
            for (int i = payMethods.size() - 1;i >=0 ;i --){
                final PayMethod payMethod = payMethods.get(i);
                if (payMethod.getIs_check() != 2){
                    payMethods.remove(i);
                }
            }
        }
        setDataForList(payMethods);
        setDefaultPayMethod();
        this.notifyDataSetChanged();
    }
    public void showDefaultPayMethod(){
        if (null != mDefaultPayMethod){
            mDefaultPayMethod.setCur(true);
            if (mDefaultPayMethod != mCurrentPayMethod){
                mCurrentPayMethod.setCur(false);
            }
            notifyDataSetChanged();
        }
    }
    private void setDefaultPayMethod(){
        mDefaultPayMethod = findPayMethodById(DEFAULT_PAY_METHOD_ID);
        if (null != mDefaultPayMethod)mDefaultPayMethod.setCur(true);
    }

    public int getDefaultPayMethodId(){
        return DEFAULT_PAY_METHOD_ID;
    }

    public int findPayMethodIndexById(int pay_method_id){
        final List<PayMethod> array = mData;
        if (!array.isEmpty()){
            for (int i = 0,length = array.size();i < length;i++){
                final PayMethod payMethod = array.get(i);
                if (pay_method_id == payMethod.getPay_method_id()){
                    return i;
                }
            }
        }
        return -1;
    }

    public PayMethod findPayMethodById(int pay_method_id){
        final List<PayMethod> payMethods = mData;
        if (!payMethods.isEmpty()){
            for (PayMethod payMethod : payMethods){
                if (pay_method_id == payMethod.getPay_method_id()){
                    return payMethod;
                }
            }
        }
        return null;
    }

    private void setSelectedSignVisibility(final View view,int v){
        final View sign = view.findViewById(R.id.sel_sign);
        if (sign != null && sign.getVisibility() != v){
            sign.setVisibility(v);
        }
    }
}
