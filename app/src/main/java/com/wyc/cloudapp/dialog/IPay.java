package com.wyc.cloudapp.dialog;

import org.json.JSONObject;

public interface IPay {
    void setPayAmt(double amt);
    JSONObject getPayContent();
}
