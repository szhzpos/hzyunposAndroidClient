package com.wyc.cloudapp.dialog.barcodeScales;

import org.json.JSONObject;

import java.io.IOException;

public interface IBarCodeScale {
    String getPort();
    boolean down(JSONObject scales_info,StringBuilder err);
    boolean parse();
}
