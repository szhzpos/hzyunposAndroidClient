package com.wyc.cloudapp.data.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.GiftCardInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.callback.ArrayResult;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.viewModel
 * @ClassName: GiftCardInfoModel
 * @Description: 查询购物卡信息
 * @Author: wyc
 * @CreateDate: 2021-11-02 17:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-11-02 17:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class GiftCardInfoModel extends ViewModelBase {
    private MutableLiveData<List<GiftCardInfo>> currentModel ;
    public void refresh(final MainActivity context, final String code) {
        if (null == currentModel)return ;
        if (!Utils.isNotEmpty(code)){
            MyDialog.toastMessage(context.getString(R.string.not_empty_hint_sz, context.getString(R.string.input_gift_card_hints)));
            return;
        }
        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(context, context.getString(R.string.hints_query_data_sz));
        launchWithHandler((coroutineScope, continuation) -> {
            final JSONObject object = new JSONObject();
            object.put("appid", context.getAppId());
            object.put("pos_num",context.getPosNum());
            object.put("stores_id",context.getStoreId());
            object.put("card_no",code);

            String err = "";
            try(Response response = netRequest(CustomApplication.self().getUrl() + InterfaceURL.GIFT_CARD_INFO, HttpRequest.generate_request_parma(object,CustomApplication.self().getAppSecret())).execute()) {
                final ResponseBody body = response.body();
                if (body != null){
                    final ArrayResult<GiftCardInfo> data =  parseArray(GiftCardInfo.class,body.string());
                    if (data.isSuccess()){
                        final List<GiftCardInfo> giftCardInfos = data.getData();
                        if (giftCardInfos == null || giftCardInfos.isEmpty()){
                            err = CustomApplication.self().getString(R.string.not_exist_hint_sz, "卡号：" + code);
                        }else
                            currentModel.postValue(giftCardInfos);
                    }else {
                        err = data.getInfo();
                    }
                }
            }catch (IOException | JSONException e){
                e.printStackTrace();
                err = e.getMessage();
            }
            progressDialog.dismiss();
            if (Utils.isNotEmpty(err))throw new IllegalArgumentException(err);
            return null;
        });
    }
    public GiftCardInfoModel AddObserver(@NonNull LifecycleOwner owner,Observer<? super List<GiftCardInfo>> observer){
        if (currentModel == null){
            currentModel = new MutableLiveData<>();
            currentModel.observe(owner,observer);
        }
        return this;
    }
}
