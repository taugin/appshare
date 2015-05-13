package com.cocos.appshare.util;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.cocos.appshare.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


public class QRCodeHelper {

    private Context mContext;
    public QRCodeHelper(Context context) {
        mContext = context;
    }

    public Bitmap createImage(String qrText, int qrWidth, int qrHeight) {
        Log.i(Log.TAG, "qrText ：" + qrText);
        if (TextUtils.isEmpty(qrText)) {
            return null;
        }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = writer.encode(qrText,
                    BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
            int[] pixels = new int[qrWidth * qrHeight];
            for (int y = 0; y < qrHeight; y++) {
                for (int x = 0; x < qrWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * qrWidth + x] = 0xff000000;
                    } else {
                        pixels[y * qrWidth + x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(qrWidth, qrHeight,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qrWidth, 0, 0, qrWidth, qrHeight);
            Bitmap dstBmp = Bitmap.createBitmap(qrWidth, qrHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(dstBmp);
            int res[] = bitMatrix.getEnclosingRectangle();
            Log.d(Log.TAG, "res[0] : " + res[0] + " , res[1] : " + res[1] + " , res[2] : " + res[2] + " , res[3] : " + res[3]);
            int whiteMargin = 5;
            Rect src = new Rect(res[0] - whiteMargin, res[1] - whiteMargin, res[0] + res[2] + whiteMargin, res[1] + res[3] + whiteMargin);
            Rect dst = new Rect(0, 0, qrWidth, qrHeight);
            canvas.drawBitmap(bitmap, src, dst, null);
            bitmap.recycle();
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.home_icon);
            Rect iconRect = new Rect(0, 0, dst.width() / 6, dst.height() / 6);
            float dx = (dst.width() - iconRect.width()) / 2;
            float dy = (dst.height() - iconRect.height()) / 2;
            drawable.setBounds(iconRect);
            canvas.save();
            canvas.translate(dx, dy);
            drawable.draw(canvas);
            canvas.restore();
            return dstBmp;

        } catch (WriterException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
