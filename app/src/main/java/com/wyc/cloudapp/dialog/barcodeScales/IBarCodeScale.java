package com.wyc.cloudapp.dialog.barcodeScales;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

public interface IBarCodeScale {
    String getPort();
    boolean down(@NonNull final JSONObject scale_info);
    void setShowStatus(OnShowStatusCallback o);
    interface OnShowStatusCallback {
        void OnShow(final String s);
    }
}
