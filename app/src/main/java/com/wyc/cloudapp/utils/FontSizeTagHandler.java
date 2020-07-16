package com.wyc.cloudapp.utils;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import com.wyc.cloudapp.logger.Logger;
import org.xml.sax.XMLReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;

public class FontSizeTagHandler implements Html.TagHandler {
    private Context mContext;
    private Stack<Integer> startIndex;
    private Stack<String> propertyValue;

    public FontSizeTagHandler(final Context context){
        mContext = context;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (opening) {
            handlerStartTAG(tag, output, xmlReader);
        } else {
            handlerEndTAG(tag, output);
        }
    }

    private void handlerStartTAG(String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("size")) {
            handlerStartSIZE(output, xmlReader);
        }
    }

    private void handlerEndTAG(String tag, Editable output) {
         if (tag.equalsIgnoreCase("size")) {
            handlerEndSIZE(output);
        }
    }

    private void handlerStartSIZE(Editable output, XMLReader xmlReader) {
        if (startIndex == null) {
            startIndex = new Stack<>();
        }
        startIndex.push(output.length());

        if (propertyValue == null) {
            propertyValue = new Stack<>();
        }

        propertyValue.push(getProperty(xmlReader, "value"));
    }

    private void handlerEndSIZE(Editable output) {
        if (!isEmpty(propertyValue)) {
            try {
                int value = Integer.parseInt(propertyValue.pop());
                output.setSpan(new AbsoluteSizeSpan(Utils.sp2px(mContext,value)), startIndex.pop(), output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private String getProperty(XMLReader xmlReader, String property) {
        try {
            final Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            final Object element = elementField.get(xmlReader);
            if (element == null)return null;
            final Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            final Object atts = attsField.get(element);
            if (atts == null)return null;
            final Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            final String[] data = (String[]) dataField.get(atts);
            final Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (int) lengthField.get(atts);

            if (data != null){
                Logger.d("data:%s",Arrays.toString(data));
                for (int i = 0; i < len; i++) {
                    if (property.equals(data[i * 5 + 1])) {
                        return data[i * 5 + 4];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}