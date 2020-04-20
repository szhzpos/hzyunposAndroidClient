package com.wyc.cloudapp.dialog.pay;

import org.json.JSONObject;

 interface IPay {
    void setPayAmt(double amt);
    JSONObject getContent();
}
