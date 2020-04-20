package com.wyc.cloudapp.dialog.barcodeScales;

import org.json.JSONObject;

public interface IBarCodeScale {
    String getPort();
    boolean down(JSONObject scales_info);
    boolean parse();
}
