package com.wyc.cloudapp.dialog.pay;

import com.alibaba.fastjson.JSONObject;

interface IPay {
    void setPayAmt(double amt);
    JSONObject getContent();
}
