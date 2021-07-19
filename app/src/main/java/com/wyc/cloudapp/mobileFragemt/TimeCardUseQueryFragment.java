package com.wyc.cloudapp.mobileFragemt;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.VipTimeCardUseOrder;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ArrayCallback;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: TimeCardUseQueryFragment
 * @Description: 次卡使用查询
 * @Author: wyc
 * @CreateDate: 2021-07-12 14:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-12 14:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TimeCardUseQueryFragment extends AbstractTimeCardQueryFragment {

    @Override
    protected AbstractDataAdapterForList<?,? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
        return new UseOrderAdapter();
    }

    @Override
    protected void query(long s, long e) {
        final JSONObject param = new JSONObject();

        param.put("appid",mContext.getAppId());
        final String content = mSearchContent.getText().toString();
        if (!content.isEmpty()){
            if (Utils.getViewTagValue(mCondition,1) == 1){
                param.put("order_code",content);
            }else {
                param.put("member_card",content);
            }
        }
        param.put("start_time",s);
        param.put("end_time",e);

        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(mContext,getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(mContext.getUrl() + "/api/once_cards/uses", HttpRequest.generate_request_parm(param,mContext.getAppSecret())).
                enqueue(new ArrayCallback<VipTimeCardUseOrder>(VipTimeCardUseOrder.class) {
                    @Override
                    protected void onError(String msg) {
                        progressDialog.dismiss();
                        MyDialog.toastMessage(msg);
                    }

                    @Override
                    protected void onSuccessForResult(List<VipTimeCardUseOrder> d, String hint) {
                        Logger.d(d);
                        ((UseOrderAdapter)mAdapter).setDataForList(d);
                        progressDialog.dismiss();
                    }
                });
    }


    @Override
    public String getTitle(){
        return CustomApplication.self().getString(R.string.once_card_use) + CustomApplication.self().getString(R.string.query_sz);
    }

    private static class UseOrderAdapter extends AbstractDataAdapterForList<VipTimeCardUseOrder,UseOrderAdapter.MyViewHolder>{

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{

            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        }
    }
}
