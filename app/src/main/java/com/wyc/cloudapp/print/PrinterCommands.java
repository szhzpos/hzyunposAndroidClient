package com.wyc.cloudapp.print;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class PrinterCommands {
    public static final String  CHARACTER_SET = "GB2312";
    public static final byte[][] byteCommands = {
            { 0x1b, 0x4d, 0x00 },// 标准ASCII字体
            { 0x1b, 0x4d, 0x01 },// 压缩ASCII字体
            { 0x1b, 0x7b, 0x00 },// 取消倒置打印
            { 0x1b, 0x7b, 0x01 },// 选择倒置打印
            { 0x1d, 0x42, 0x00 },// 取消黑白反显
            { 0x1d, 0x42, 0x01 },// 选择黑白反显
            { 0x1b, 0x56, 0x00 },// 取消顺时针旋转90°
            { 0x1b, 0x56, 0x01 },// 选择顺时针旋转90°
    };


    /**
     * 复位打印机
     */
    public static final byte[] RESET = {0x1b, 0x40};

    /**
     * 左对齐
     */
    public static final byte[] ALIGN_LEFT = {0x1b, 0x61, 0x00};

    /**
     * 中间对齐
     */
    public static final byte[] ALIGN_CENTER = {0x1b, 0x61, 0x01};

    /**
     * 右对齐
     */
    public static final byte[] ALIGN_RIGHT = {0x1b, 0x61, 0x02};

    /**
     * 选择加粗模式
     */
    public static final byte[] BOLD = {0x1b, 0x45, 0x01};

    /**
     * 取消加粗模式
     */
    public static final byte[] BOLD_CANCEL = {0x1b, 0x45, 0x00};

    /**
     * 宽高加倍
     */
    public static final byte[] DOUBLE_HEIGHT_WIDTH = {0x1d, 0x21, 0x11};

    /**
     * 宽加倍
     */
    public static final byte[] DOUBLE_WIDTH = {0x1d, 0x21, 0x10};

    /**
     * 高加倍
     */
    public static final byte[] DOUBLE_HEIGHT = {0x1d, 0x21, 0x01};

    /**
     * 字体不放大
     */
    public static final byte[] NORMAL = {0x1d, 0x21, 0x00};

    /**
     * 设置行间距
     */
    public static final byte[] LINE_SPACING_DEFAULT = {0x1b, 0x32};
    public static final byte[] LINE_SPACING_8 = {0x1b,0x33};
    public static final byte[] LINE_SPACING_4 = {0x1b,0x33,0x04};
    public static final byte[] LINE_SPACING_2 = {0x1b,0x33,0x02};
    public static final byte[] LINE_SPACING_16 = {0x1b,0x33,0x59};
    /**
     * 换行
     */
    public static final byte[] NEW_LINE  = {0x0A, 0x0D};

    /**
     * 退纸
     */
    public static final byte[] BACK_STEP  = {0x1B, 0x6A};

    /**
     * 开钱箱
     */
    public static final byte[] OPEN_CASHBOX  = {0x1B, 0x70,0x0,0x3c,0x79};

    /**
     * 打印纸一行最大的字节
     */
    private static final int LINE_BYTE_SIZE = 32;

    /**
     * 打印三列时，中间一列的中心线距离打印纸左侧的距离
     */
    private static final int LEFT_LENGTH = 16;

    /**
     * 打印三列时，中间一列的中心线距离打印纸右侧的距离
     */
    private static final int RIGHT_LENGTH = 16;

    /**
     * 打印三列时，第一列汉字最多显示几个文字
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 8;

    public static void set_line_spacing(OutputStream writer, int n) throws IOException{
        writer.write(new byte[]{0x1B,0x33});
        writer.write(n);
        writer.flush();
    }
    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    public static String printTwoData(int align,String leftText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        // 计算两侧文字中间的空格
        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;
            if (align == 1)
                for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
                    sb.append(" ");
                }
                else
                sb.append(" ");

        sb.append(rightText);
        return sb.toString();
    }

    public static String printThreeData(int space,String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // 计算左侧文字和中间文字的空格长度
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - space / 2 - leftTextLength - middleTextLength / 2;
        if (marginBetweenLeftAndMiddle > 0) {
            for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
                sb.append(" ");
            }
        }else {
            sb.append(" ").append(" ");
        }
        sb.append(middleText);

        // 计算右侧文字和中间文字的空格长度
        int marginBetweenMiddleAndRight = RIGHT_LENGTH - space / 2 - middleTextLength / 2 - rightTextLength;
        if (marginBetweenMiddleAndRight > 0) {
            for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
                sb.append(" ");
            }
        }else {
            sb.append(" ").append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }

    public static String commandToStr(byte[] bytes){
        try {
            return new String(bytes,PrinterCommands.CHARACTER_SET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
