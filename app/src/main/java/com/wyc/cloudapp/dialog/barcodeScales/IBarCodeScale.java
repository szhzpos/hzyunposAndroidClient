package com.wyc.cloudapp.dialog.barcodeScales;

import com.alibaba.fastjson.JSONObject;

public interface IBarCodeScale {
    String getPort();
    boolean down(final JSONObject scales_info);
    void setUpdateStatus(UpdateStatusCallback o);
    boolean parse();

    interface UpdateStatusCallback{
        void updata(final String s);
    }
}
