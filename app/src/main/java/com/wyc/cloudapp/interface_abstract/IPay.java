package com.wyc.cloudapp.interface_abstract;

import org.json.JSONObject;

 interface IPay {
    void setPayAmt(double amt);
    JSONObject getContent();
}
