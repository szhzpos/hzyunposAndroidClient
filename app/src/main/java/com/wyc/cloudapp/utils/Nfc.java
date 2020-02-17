package com.wyc.cloudapp.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Handler;
import android.widget.Toast;
import java.io.IOException;
import java.util.Properties;
import com.wyc.cloudapp.logger.Logger;
public class Nfc {
    private boolean isEnbleNfc = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private Context mActivity;
    private String passwrod;
    public Nfc(Context activity){
        mActivity = activity;
        Properties properties = Utils.loadProperties(activity);
        int bReadCard = Integer.valueOf(properties.getProperty("bReadCard","0"));
        if(bReadCard == 1){
            mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);
            if (mNfcAdapter == null) {
                Toast.makeText(mActivity, "NFC is not available", Toast.LENGTH_LONG).show();
            }else{
                if (!mNfcAdapter.isEnabled()){
                    Toast.makeText(mActivity, "请打开NFC功能！", Toast.LENGTH_LONG).show();
                }else{
                    passwrod = properties.getProperty("read_card_password","").trim();
                    isEnbleNfc = true;
                    mPendingIntent = PendingIntent.getActivity(mActivity, 0, new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                }
            }
        }
    }

    public void enbleNfc(){
        if (isEnbleNfc){
            IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter filter3 = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            IntentFilter[] mIntentFilter = new IntentFilter[]{filter,filter3,filter2};
            String[][] techListsArray = new String[][] { new String[] { MifareClassic.class.getName() } };
            mNfcAdapter.enableForegroundDispatch((Activity) mActivity, mPendingIntent, mIntentFilter, techListsArray);
        }
    }

    public void dianbleNfc(){
        if (isEnbleNfc)
            mNfcAdapter.disableForegroundDispatch((Activity) mActivity);
    }

    public void praseIntent(Intent intent, Handler myhandler) {
        String nfcAction = intent.getAction(); // 解析该Intent的Action
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(nfcAction)) {
            Logger.d("h_bl:" + "ACTION_TECH_DISCOVERED");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); // 获取Tag标签，既可以处理相关信息
            for (String tech : tag.getTechList()) {
                Logger.d("h_bl:"+ "tech=" + tech);
            }
            MifareClassic isNfcA = MifareClassic.get(tag);
            if (isNfcA != null){
                try {
                    isNfcA.connect(); // 连接
                    if (isNfcA.isConnected()) {
                        Logger.d("h_bl:" + "isNfcA.isConnected"); // 判断是否连接上
                    }
                    if(passwrod.length() == 6){
                        if(isNfcA.authenticateSectorWithKeyA(1,passwrod.getBytes())){
                            byte[] record = isNfcA.readBlock(5);
                            String card_id = new String(record);
                            Logger.d("扇区：%d 块:：%d strRecord：%s",1,5,card_id);
                            myhandler.obtainMessage(77,card_id.trim()).sendToTarget();
                        }else{
                            myhandler.obtainMessage(66,"读卡密码错误！").sendToTarget();
                        }
                    }else{
                        myhandler.obtainMessage(66,"读卡密码长度必须为6位！").sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    myhandler.obtainMessage(66,e.getLocalizedMessage()).sendToTarget();
                } finally {
                    try {
                        isNfcA.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                myhandler.obtainMessage(66,"不支持类型卡：" + nfcAction).sendToTarget();
            }
        }
    }
}
