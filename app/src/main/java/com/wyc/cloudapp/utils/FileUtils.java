package com.wyc.cloudapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.wyc.cloudapp.BuildConfig;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.utils
 * @ClassName: FileUtils
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021/5/19 16:11
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/19 16:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FileUtils {
    public static void zipFile(File dbFile, OutputStream outputStream) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(outputStream)) {
            zip(dbFile.listFiles(),"",out);
        }
    }
    private static void zip(File[] files, String baseFolder, ZipOutputStream zos)throws IOException {
        if (files != null){
            ZipEntry entry = null;
            int count = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    zip(file.listFiles(), file.getName() + File.separator, zos);
                    continue;
                }
                entry = new ZipEntry(baseFolder + file.getName());

                zos.putNextEntry(entry);

                try(FileInputStream fis = new FileInputStream(file);) {
                    final byte[] buffer = new byte[1024];
                    while ((count = fis.read(buffer, 0, buffer.length)) != -1)
                        zos.write(buffer, 0, count);
                }
            }
        }
    }
    public static String getMIMEType(String fName)
    {
        String type="*/*";
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
        /* 获取文件的后缀名 */
        String end=fName.substring(dotIndex).toLowerCase();
        if("".equals(end))return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (String[] strings : MIME_MapTable) {
            if (end.equals(strings[0])){
                type = strings[1];
                break;
            }
        }
        return type;
    }

    private static final String[][] MIME_MapTable={
            //{后缀名，    MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",      "image/bmp"},
            {".c",        "text/plain"},
            {".class",    "application/octet-stream"},
            {".conf",    "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",    "application/x-gtar"},
            {".gz",        "application/x-gzip"},
            {".h",        "text/plain"},
            {".htm",    "text/html"},
            {".html",    "text/html"},
            {".jar",    "application/java-archive"},
            {".java",    "text/plain"},
            {".jpeg",    "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js",        "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",    "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",    "video/mp4"},
            {".mpga",    "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".prop",    "text/plain"},
            {".rar",    "application/x-rar-compressed"},
            {".rc",        "text/plain"},
            {".rmvb",    "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh",        "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            //{".xml",    "text/xml"},
            {".xml",    "text/plain"},
            {".z",        "application/x-compress"},
            {".zip",    "application/zip"},
            {"",        "*/*"}
    };

    private static ContentValues getImageContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Images.ImageColumns.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/*");
        localContentValues.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, paramLong);
        localContentValues.put(MediaStore.Images.ImageColumns.DATE_ADDED, paramLong);
        localContentValues.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        localContentValues.put(MediaStore.Images.ImageColumns.DATE_TAKEN,paramLong);
        localContentValues.put(MediaStore.Images.ImageColumns.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Images.ImageColumns.SIZE, paramFile.length());
        return localContentValues;
    }
    public static Uri getImgUri(Context context, File file) {
        ContentResolver localContentResolver = context.getContentResolver();
        ContentValues localContentValues = getImageContentValues(file, System.currentTimeMillis());
        return localContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
    }
    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    public static Uri createCropImageFile() {
        String imageFileName = "clip_wyc_." +  Bitmap.CompressFormat.JPEG.toString();
        String storageDir = CustomApplication.getGoodsImgSavePath();
        return Uri.parse("file://" + File.separator +storageDir + imageFileName);
    }
    public static Uri createCaptureImageFile() {
        String imageFileName = "capture_wyc_." +  Bitmap.CompressFormat.JPEG.toString();
        String storageDir = CustomApplication.getGoodsImgSavePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return FileProvider.getUriForFile(CustomApplication.self(), BuildConfig.APPLICATION_ID + ".FileProvider", new File(storageDir + imageFileName));
        }
        return Uri.parse("file://" + File.separator +storageDir + imageFileName);
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI,
                new String[]{MediaStore.Images.ImageColumns.DATA},null, null, null);
        if (cursor == null)
            result = contentURI.getPath();
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }

}
