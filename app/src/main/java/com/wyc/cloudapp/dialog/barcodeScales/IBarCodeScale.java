package com.wyc.cloudapp.dialog.barcodeScales;

import com.alibaba.fastjson.JSONObject;

public interface IBarCodeScale {
    String getPort();
    boolean down(final JSONObject scales_info);
    void setShowStatus(OnShowStatusCallback o);
    interface OnShowStatusCallback {
        void OnShow(final String s);
    }
}
