package com.wyc.cloudapp.print;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.wyc.cloudapp.logger.Logger;

public final class PrintUtilsToBitbmp {
    private final static int WIDTH = 240;//8的整数倍
    private final static int HEIGHT = 240;//24的整数倍
    private final static int PER_POINT = 24;
    /*************************************************************************
     * 360*360的图片，8个字节（8个像素点）是一个二进制，将二进制转化为十进制数值
     * y轴：24个像素点为一组，即360就是15组（0-14）
     * x轴：360个像素点（0-359）
     * 里面的每一组（24*360），每8个像素点为一个二进制，（每组有3个，3*8=24）
     **************************************************************************/
    /**
     * 把一张Bitmap图片转化为打印机可以打印的bit(将图片压缩为360*360)
     * 效率很高（相对于下面）
     * @param bit
     * @return
     */
    public static byte[] draw2PxPoint(Bitmap bit) {
        Bitmap newBit = compressPic(bit);
        int w = newBit.getWidth();
        int h = newBit.getHeight();
        Logger.d("old_w:%d,old_height:%d,new_w:%d,new_h:%d",bit.getWidth(),bit.getHeight(),w,h);
        byte[] data = new byte[w * h + h / PER_POINT*6 + 8 ];//图片大小 + 指令字节 + 留空字节
        int k = 0;
        byte n2 = WIDTH / 256,n1 = (byte)(w - 256*n2);
        for (int j = 0; j < h / PER_POINT; j++) {
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
            data[k++] = n1;
            data[k++] = n2;
            for (int i = 0; i < w; i++) {
                for (int m = 0; m < 3; m++) {
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Binaryzation(i, j * 24 + m * 8 + n,newBit);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
            data[k++] = 10;
        }
        //重置打印机
        data[k++] = 0x1b;
        data[k] = 0x40;
        return data;
    }

    /**
     * 把一张Bitmap图片转化为打印机可以打印的bit
     * @param bit
     * @return
     */
    public static byte[] pic2PxPoint(Bitmap bit){
        Bitmap newBit = compressBitmap(bit);
        int w = newBit.getWidth();
        int h = newBit.getHeight();
        byte[] data = new byte[w * h + h / PER_POINT*6 + 8 ];//图片大小 + 指令字节 + 留空字节
        int k = 0;
        byte n2 = WIDTH / 256,n1 = (byte)(w - 256*n2);
        for (int i = 0; i < h / PER_POINT; i++) {
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
            data[k++] = n1;
            data[k++] = n2;
            for (int x = 0; x < w; x++) {
                for (int m = 0; m < 3; m++) {
                    byte[]  by = new byte[8];
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Binaryzation(x, i * 24 + m * 8 +7-n, newBit);//(7-n)从高位先取保证changePointPx1函数能正确处理
                        by[n] = b;
                    }
                    data[k++] = (byte) changePointPx1(by);
                }
            }
            data[k++] = 10;
        }
        //重置打印机
        data[k++] = 0x1b;
        data[k] = 0x40;

        return data;
    }

    /**
     * 图片二值化，黑色是1，白色是0
     * @param x  横坐标
     * @param y     纵坐标
     * @param bit 位图
     * @return
     */
    public static byte px2Binaryzation(int x, int y, Bitmap bit) {
        //最高一个字节为alpha;
        byte b;
        int pixel = bit.getPixel(x, y);
        int red = (pixel & 0x00ff0000) >> 16; // 取高两位
        int green = (pixel & 0x0000ff00) >> 8; // 取中两位
        int blue = pixel & 0x000000ff; // 取低两位
        int gray = RGB2Gray(red, green, blue);
        if ( gray < 128 ){
            b = 1;
        } else {
            b = 0;
        }
        return b;
    }

    /**
     * 图片灰度的转化
     * @param r
     * @param g
     * @param b
     * @return
     */
    private static int RGB2Gray(int r, int g, int b){
        return  (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式;
    }

    /**
     * 对图片进行压缩（去除透明度）,宽度对齐8，高度对齐24
     * @param bitmapOrg
     */
    public static Bitmap compressPic(Bitmap bitmapOrg) {
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        Bitmap targetBmp = Bitmap.createBitmap(alignToN(WIDTH,8), alignToN(HEIGHT,PER_POINT), Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmapOrg, new Rect(0, 0, width, height), new Rect(0, 0,alignToN(WIDTH,8), alignToN(HEIGHT,PER_POINT)), null);
        return targetBmp;
    }


    /**
     * 对图片进行压缩(不去除透明度),宽度对齐8，高度对齐24
     * @param bitmapOrg
     */
    public static Bitmap compressBitmap(Bitmap bitmapOrg) {
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        // 计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) WIDTH / width);
        float scaleHeight = ((float) HEIGHT / height);
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        // 创建新的图片
        return Bitmap.createBitmap(bitmapOrg, 0, 0, width,height, matrix, true);
    }

    /**
     * 将[1,0,0,1,0,0,0,1]这样的二进制转为化十进制的数值（效率更高）
     * @param arry
     * @return
     */
    public static int changePointPx1(byte[] arry){
        int v = 0;
        for (int j = 0; j <arry.length; j++) {
            if( arry[j] == 1) {
                v = v | 1 << j;
            }
        }
        return v;
    }

    /**
     * 将[1,0,0,1,0,0,0,1]这样的二进制转为化十进制的数值
     * @param arry
     * @return
     */
    public byte changePointPx(byte[] arry){
        byte v = 0;
        for (int i = 0; i < 8; i++) {
            v += v + arry[i];
        }
        return v;
    }

    //在bitmap画错误标志<红圈白×>
    public static Bitmap drawErrorSignToBitmap(final Bitmap in,int w,int h){
        Bitmap bitmap = in.copy(Bitmap.Config.ARGB_8888, true);
        int width = bitmap.getWidth(),height = bitmap.getHeight();
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        RectF rectF = new RectF(width- w,height - h,width - 2,height - 2);
        canvas.drawArc(rectF, 0, 360, false, paint);
        paint.setColor(Color.WHITE);
        canvas.drawLine(rectF.left+2,rectF.top+2,rectF.right-2,rectF.bottom-2,paint);
        canvas.drawLine(rectF.right-2,rectF.top+2, rectF.left+2,rectF.bottom-2,paint);

        return bitmap;
    }

    //加权法灰度化图片
    public static Bitmap bitmapGrayScale(Bitmap in){
        int width = in.getWidth();
        int height = in.getHeight();
        int[] pixels = new int[width*height],o_pixels = new int[width*height] ;
        in.getPixels(o_pixels,0,width,0,0,width,height);
        for (int i = 0;i < height;i ++){
            for (int j = 0;j < width;j++){
                int color = o_pixels[i*width + j];
                final int a = (color >> 24) & 0xff;
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                int  gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);//加权法灰度化;
                pixels[i*width + j] = getPixel(a,gray,gray,gray);
            }
        }
        return  Bitmap.createBitmap(pixels,width,height,Bitmap.Config.ARGB_8888);
    }
    //从指定ARGB返回像素值
    private static int getPixel(int a,int r,int g,int b){
        int newPixel = 0;
        newPixel |= (a & 0xff);
        newPixel = newPixel << 8 | r & 0xff ;
        newPixel = newPixel << 8 | g & 0xff ;
        newPixel = newPixel << 8 | b & 0xff ;
        return newPixel;
    }
    //把指定数对齐到目标数
    private static int alignToN(int num,int N){//N对齐target
        return num % N != 0  ? num + (N - num % N) : num;
    }
}