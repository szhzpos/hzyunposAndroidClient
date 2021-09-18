package com.wyc.cloudapp.data.viewModel;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.BarcodeOnlyCodeInfo;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.callback.ObjectResult;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.viewModel
 * @ClassName: BarcodeOnlyCodeViewModel
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021-09-09 16:05
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-09-09 16:05
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BarcodeOnlyCodeViewModel extends ViewModelBase {
    private final MutableLiveData<BarcodeOnlyCodeInfo> currentModel = new MutableLiveData<>();

    public MutableLiveData<BarcodeOnlyCodeInfo> getCurrentModel(final String cate_id,final String spec_id) {
        launchWithHandler((coroutineScope, continuation) -> {
            final JSONObject object = new JSONObject();
            object.put("appid", CustomApplication.self().getAppId());
            object.put("category_id",cate_id);
            object.put("spec_id",spec_id);

            try(Response response = netRequest(CustomApplication.self().getUrl() + "/api/goods_set/get_onlycode_barcode",HttpRequest.generate_request_parm(object,CustomApplication.self().getAppSecret())).execute()) {
                ResponseBody body = response.body();
                if (body != null){
                    final ObjectResult<BarcodeOnlyCodeInfo> data =  parseObject(BarcodeOnlyCodeInfo.class,body.string());
                    if (data.isSuccess()){
                        currentModel.postValue(data.getData());
                    }else {
                        throw new IllegalArgumentException(data.getInfo());
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
            return null;
        });
        return currentModel;
    }
}