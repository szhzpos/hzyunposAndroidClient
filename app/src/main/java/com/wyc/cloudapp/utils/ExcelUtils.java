package com.wyc.cloudapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {

    private static WritableCellFormat arial14format = null;
    private static WritableCellFormat arial10format = null;
    private static WritableCellFormat arial12format = null;
    private static WritableCellFormat arial12format_head = null;

    private final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    public static void format() {
        try {
            WritableFont arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            WritableFont arial10font  = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10font.setColour(Colour.WHITE);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);


            arial12format = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 12));
            arial12format.setAlignment(jxl.format.Alignment.CENTRE);
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);


            arial12format_head = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 12));
            arial12format_head.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial12format_head.setAlignment(jxl.format.Alignment.CENTRE);
            arial12format_head.setBackground(Colour.GRAY_25);
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(Context mContext,List<T> objList,int resource,String[] colName,int[] ids ,String fileName) throws Exception{
        if (objList != null && objList.size() > 0) {
            WritableWorkbook workbook = null;
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzposExcel/" + fileName ;
            format();
            try {
                File file  = new File(filePath);
                if (!file.getParentFile().exists()) {
                    if (file.getParentFile().mkdir()){
                        file.createNewFile();
                    }
                }else {
                    file.createNewFile();
                }
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                workbook = Workbook.createWorkbook(file,setEncode);
                WritableSheet sheet = workbook.createSheet("Sheet1", 0);
                sheet.addCell((WritableCell) new Label(0, 0, fileName.substring(0,fileName.indexOf('.')), arial14format));
                sheet.mergeCells(0,0,colName.length - 1,0);
                View view = View.inflate(mContext,resource,null);
                for (int col = 0; col < ids.length; col++) {
                    TextView textView = view.findViewById(ids[col]);
                    if (textView != null)
                        sheet.addCell(new Label(col, 1, textView.getText().toString(), arial10format));
                }
                for (int j = 0; j < objList.size(); j++) {
                    Map<String,Object> map = (Map<String,Object>) objList.get(j);
                    for (int i = 0,len = colName.length;i < len ;i++){
                        if (map.get(colName[i]) != null)
                            sheet.addCell(new Label(i, j + 2, map.get(colName[i]).toString(), arial12format));
                    }
                }
                workbook.write();
            }  finally {
                if (workbook != null) {
                    workbook.close();
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(Context mContext, @NonNull ContentValues values, List<T> objList, int resource, String[] colName, int[] ids , String fileName) throws Exception{
        if (objList != null && objList.size() > 0) {
            WritableWorkbook workbook = null;
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzposExcel/" + fileName ;
            format();
            try {
                File file  = new File(filePath);
                if (!file.getParentFile().exists()) {
                    if (file.getParentFile().mkdir()){
                        file.createNewFile();
                    }
                }else {
                    file.createNewFile();
                }
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                workbook = Workbook.createWorkbook(file,setEncode);
                WritableSheet sheet = workbook.createSheet("Sheet1", 0);
                sheet.addCell((WritableCell) new Label(0, 0, fileName.substring(0,fileName.indexOf('.')), arial14format));
                sheet.mergeCells(0,0,colName.length - 1,0);
                View view = View.inflate(mContext,resource,null);

                int headCol = 1,iLine_left = (colName.length - 1) / 2 ,headCol_tmp = 0;
                String szKey ,szValues;

                for (int col = 0; col < ids.length; col++) {
                    TextView textView = view.findViewById(ids[col]);
                    if (textView != null)
                        sheet.addCell(new Label(col, headCol, textView.getText().toString(), arial10format));
                }
                for (int j = 0; j < objList.size(); j++) {
                    Map<String,Object> map = (Map<String,Object>) objList.get(j);
                    for (int i = 0,len = colName.length;i < len ;i++){
                        if (map.get(colName[i]) != null)
                            sheet.addCell(new Label(i, j + headCol + 1, map.get(colName[i]).toString(), arial12format));
                    }
                }
                workbook.write();
            }  finally {
                if (workbook != null) {
                    workbook.close();
                }
            }
        }
    }

}
