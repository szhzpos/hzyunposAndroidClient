package com.wyc.cloudapp.dialog.serialScales;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mt.retail.weighapi.IMtWeighView;
import com.mt.retail.weighapi.MtWeighApi;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customizationView.WeightInfoView;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.serialScales
 * @ClassName: ToledoScale
 * @Description: 托利多 Plus U2
 * @Author: wyc
 * @CreateDate: 2021-12-22 17:08
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-22 17:08
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ToledoScale extends AbstractWeightedScaleImp implements IMtWeighView{
    private static final int RETURN_CODE_WEIGHT_UNSTABLE       = -1000; // 重量未稳定
    private static final int RETURN_CODE_WEIGHT_UNDERLOAD      = -1001; // 重量欠载
    private static final int RETURN_CODE_WEIGHT_OVERLOAD       = -1002; // 重量超载
    private static final int RETURN_CODE_WEIGHT_INVALID        = -1003; // 重量无效或秤台上电未能清零

    // 称重命令返回结果中的JSON字符串的key，详细说明请参考开发文档中对应称重命令的【参数说明】
    public static final String RET_JSON_VALUE_STATUS_OK      = "0";
    public static final String RET_JSON_KEY_STATUS           = "result";
    public static final String RET_JSON_KEY_WEIGHT_NET       = "net";
    public static final String RET_JSON_KEY_ZERO             = "zero";
    public static final String RET_JSON_KEY_TARE_TYPE        = "tareType";
    public static final String RET_JSON_KEY_TARE             = "tare";
    public static final String RET_JSON_KEY_UNIT             = "unit";
    public static final String RET_JSON_KEY_TARE_UNIT        = "tareUnit";
    public static final String RET_JSON_KEY_MIN_PRESET_TARE  = "minPresetTare";
    public static final String RET_JSON_KEY_MAX_PRESET_TARE  = "maxPresetTare";
    public static final String RET_JSON_KEY_RANGE_NUMBER     = "rangeNumber";
    public static final String RET_JSON_KEY_MIN_RANGE        = "minRange";
    public static final String RET_JSON_KEY_MAX_RANGE        = "maxRange";
    public static final String RET_JSON_KEY_RESOLUTION       = "resolution";

    private MtWeighApi mMtWeightService;
    private boolean mZero = false;
    private boolean openCashBox = false;
    public ToledoScale(final String port){
        mPort = port;
        mMtWeightService = MtWeighApi.getInstance();
    }
    private ToledoScale(){
        openCashBox = true;
        mMtWeightService = MtWeighApi.getInstance();
        if (mMtWeightService.isNotConnectToService()){
            mMtWeightService.connectToService(CustomApplication.self(),this);
        }else {
            mMtWeightService.openCashDrawer();
        }
    }
    @Override
    public void startRead() {
        mMtWeightService.connectToService(CustomApplication.self(),this);
    }

    @Override
    public void stopRead() {
        clear();
        mOnReadStatus = null;
    }
    private void clear(){
        mMtWeightService.disconnectService(CustomApplication.self());
        try {
            final Field field = mMtWeightService.getClass().getDeclaredField("mIMtWeighView");
            field.setAccessible(true);
            field.set(mMtWeightService,null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        mMtWeightService = null;
    }

    @Override
    public void onMtWeighServiceConnected() {
        if (openCashBox){
            mMtWeightService.openCashDrawer();
            clear();
        }
    }

    @Override
    public void onMtWeighServiceDisconnected() {
    }

    @Override
    public void onWeightChanged(String msg) {
        if (mOnReadStatus != null){
            try {
                final JSONObject weInfo = JSONObject.parseObject(msg);
                int result = weInfo.getIntValue(RET_JSON_KEY_STATUS);
                double net = weInfo.getDoubleValue(RET_JSON_KEY_WEIGHT_NET);//净重
                mZero = weInfo.getIntValue(RET_JSON_KEY_ZERO) == 1;//毛重零位  1：在 0 位，0：不是 0 位； 毛重零时（卸掉所有载荷后的空秤台），才允许清除测量皮重。
                switch (result){
                    case 0:
                        mOnReadStatus.onFinish(AbstractWeightedScaleImp.OnReadStatus.STABLE,net);
                        break;
                    case RETURN_CODE_WEIGHT_UNSTABLE:
                        mOnReadStatus.onFinish(AbstractWeightedScaleImp.OnReadStatus.NO_STABLE,net);
                        break;
                    case RETURN_CODE_WEIGHT_INVALID:
                    case RETURN_CODE_WEIGHT_OVERLOAD:
                    case RETURN_CODE_WEIGHT_UNDERLOAD:
                        mOnReadStatus.onFinish(AbstractWeightedScaleImp.OnReadStatus.NO_STABLE, WeightInfoView.INVALID);
                        break;
                    default:
                        showMsg(result);

                }
            }catch (JSONException e){
                e.printStackTrace();
                MyDialog.toastMessage(e.getMessage());
            }
        }
    }

    @Override
    public void onBaseInfoChanged(String msg) {

    }

    @Override
    public void onSetTareFinished(String msg) {
        try {
            final JSONObject weInfo = JSONObject.parseObject(msg);
            int result = weInfo.getIntValue(RET_JSON_KEY_STATUS);
            showMsg(result);
        }catch (JSONException e){
            e.printStackTrace();
            MyDialog.toastMessage(e.getMessage());
        }
    }
    private void showMsg(int result){
        switch (result){
            case 0:
                MyDialog.toastMessage(R.string.success);
                break;
            case RETURN_CODE_WEIGHT_UNSTABLE:
                MyDialog.toastMessage("重量未稳定");
                break;
            case RETURN_CODE_WEIGHT_UNDERLOAD:
                MyDialog.toastMessage("重量欠载");
                break;
            case RETURN_CODE_WEIGHT_OVERLOAD:
                MyDialog.toastMessage("重量超载");
                break;
            case RETURN_CODE_WEIGHT_INVALID:
                MyDialog.toastMessage("重量无效或秤台上电未能清零");
                break;
            default:
                MyDialog.toastMessage(String.format(Locale.CHINA,"其他错误。错误码：%d",result));
                break;
        }
    }

    @Override
    public void onZeroFinished(int result) {
        showMsg(result);
    }

    @Override
    public void rZero() {
        if (mMtWeightService != null)mMtWeightService.setZeroAsync();
    }

    @Override
    public void tare() {
        if (mMtWeightService != null){
            if (mZero){
                showMsg(mMtWeightService.clearTare());
            }else
                mMtWeightService.setTareAsync();
        }
    }
    public static void openCashBox(){
        new ToledoScale();
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.d("%s has finished",this);
    }
}
