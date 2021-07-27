package com.wyc.cloudapp.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.application
 * @ClassName: CrashHandler
 * @Description: 线程未处理异常自定义处理器
 * @Author: wyc
 * @CreateDate: 2021/3/4 11:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/4 11:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static String TAG = "AppCrash";
    @SuppressLint("StaticFieldLeak")
    private static final CrashHandler instance = new CrashHandler();
    // 用来存储设备信息和异常信息
    private final Map<String, String> infos = new HashMap<>();
    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return instance;
    }

    public static void setTag(String tag) {
        TAG = tag;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(@NonNull Thread thread,@NonNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            SystemClock.sleep(3000);
            CustomApplication.self().exit();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 需要处理的异常
     * @return true:如果处理了该异常信息; 否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        try {
            // 使用Toast来显示异常信息
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    MyDialog.toastMessage(CustomApplication.self().getString(R.string.tip_crash));
                    Looper.loop();
                }
            }.start();
            // 收集设备参数信息
            collectDeviceInfo(mContext);
            // 保存日志文件
            saveCrashInfoFile(ex);
            SystemClock.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx "
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName + "";
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e( "%s an error occured when collect package info。e:%s",TAG,e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Logger.e( "%s an error occured when collect crash info。e:%s",TAG,e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex 异常
     * @return 返回文件名称, 便于将文件传送到服务器
     * @throws Exception “
     */
    private String saveCrashInfoFile(Throwable ex) throws Exception {
        final StringBuilder sb = new StringBuilder();
        try {
            final String date = FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1);
            sb.append("\r\n").append(date).append("\n");
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append("=").append(value).append("\n");
            }

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);

            Logger.d("%s-%s--uncaughtException:%s",TAG,"Crash!",result);

            return writeFile(sb.toString());
        } catch (Exception e) {
            Logger.e( "%s an error occured while writing file... e:%s",TAG,e);
            sb.append("an error occured while writing file...\r\n");
            writeFile(sb.toString());
        }
        return null;
    }

    private String writeFile(String sb) throws Exception {
        final String fileName = "crash-" + new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(new Date()) + ".log";
        final String path = CustomApplication.getCrashSavePath();
        final FileOutputStream fos = new FileOutputStream(path + fileName, true);
        fos.write(sb.getBytes());
        fos.flush();
        fos.close();
        return fileName;
    }
}