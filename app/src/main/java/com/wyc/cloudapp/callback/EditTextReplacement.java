package com.wyc.cloudapp.callback;

import android.text.method.ReplacementTransformationMethod;

public class EditTextReplacement extends ReplacementTransformationMethod {
    @Override
    protected char[] getOriginal() {
        StringBuilder strWord = new StringBuilder();
        for (char i = 0; i < 256; i++) {
            strWord.append(i);
        }
        return strWord.toString().toCharArray();
    }

    @Override
    protected char[] getReplacement() {
        char[] charReplacement = new char[255];
        for (int i = 0; i < 255; i++) {
            charReplacement[i] = '*';
        }
        return charReplacement;
    }
}
