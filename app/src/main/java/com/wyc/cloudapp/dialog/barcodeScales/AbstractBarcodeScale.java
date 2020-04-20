package com.wyc.cloudapp.dialog.barcodeScales;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AbstractBarcodeScale implements IBarCodeScale {
    @Override
    public String getPort() {
        return null;
    }

    @Override
    public boolean down(JSONObject scales_info) {
        return false;
    }

    @Override
    public boolean parse() {
        return false;
    }

    static boolean scaleDownLoad(JSONObject scales_info){
        boolean code = true;

        return code;
    }

    static JSONObject getDHManufacturer() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray products = new JSONArray();

        object.put("name","大华系列");

        products.put(getScalseProduct("DH","大华TM-15A"));

        object.put("products",products);

        return object;
    }

    private static JSONObject getScalseProduct(final String s_id,final String s_type) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("s_id",s_id);
        object.put("s_type",s_type);
        return object;
    }
}
