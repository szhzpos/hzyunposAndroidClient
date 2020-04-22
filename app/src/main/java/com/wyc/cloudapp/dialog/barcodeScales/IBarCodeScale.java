package com.wyc.cloudapp.dialog.barcodeScales;

import org.json.JSONObject;
public interface IBarCodeScale {
    String getPort();
    boolean down(JSONObject scales_info);
    void setUpdateStatus(UpdateStatusCallback o);
    boolean parse();

    interface UpdateStatusCallback{
        void updata(final String s);
    }
}
