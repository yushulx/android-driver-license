package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.text.TextPaint;

import com.dynamsoft.barcode.LocalizationResult;
import com.dynamsoft.barcode.TextResult;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import io.fotoapparat.parameter.Resolution;

public class FrameUtil {

    private int viewWidth;
    private int viewHeight;
    public boolean dependOnWid;
    public float calculatePreviewScale(Resolution size, int viewWidth, int viewHeight) {
        if (size == null) {
            return 0;
        }
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        float previewScale;
        if (size.height > size.width) {
            if (((float) viewWidth / (float) size.width) > ((float) viewHeight / (float) size.height)) {
                previewScale = (float) viewWidth / (float) size.width;
                dependOnWid = true;
            } else {
                previewScale = (float) (viewHeight) / (float) size.height;
                dependOnWid = false;

            }
        } else {
            if (((float) viewWidth / (float) size.height) > ((float) viewHeight / (float) size.width)) {
                previewScale = (float) viewWidth / (float) size.height;
                dependOnWid = true;

            } else {
                previewScale = (float) (viewHeight) / (float) size.width;
                dependOnWid = false;

            }
        }
        return previewScale;
    }
    public ArrayList<Point[]> handlePoints(LocalizationResult[] localizationResults, float previewScale, int srcBitmapHeight, int srcBitmapWidth) {
        if (localizationResults == null) {
            return null;
        }
        ArrayList<Point[]> rectCoord = new ArrayList<>();
        Point point0;
        Point point1;
        Point point2;
        Point point3;
        Point[] points;
        for (int i = 0; i < localizationResults.length; i++) {
            points = new Point[4];
            point0 = new Point();
            point1 = new Point();
            point2 = new Point();
            point3 = new Point();
            if(dependOnWid){
                point0.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[0].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point0.y = (int)(localizationResults[i].resultPoints[0].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                point1.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[1].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point1.y = (int)(localizationResults[i].resultPoints[1].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                point2.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[2].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point2.y = (int)(localizationResults[i].resultPoints[2].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                point3.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[3].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point3.y = (int)(localizationResults[i].resultPoints[3].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                points[0] = point0;
                points[1] = point1;
                points[2] = point2;
                points[3] = point3;
                rectCoord.add(points);
            }
            else {
                point0.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[0].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point0.y = (int)(localizationResults[i].resultPoints[0].x * previewScale);
                point1.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[1].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point1.y = (int)(localizationResults[i].resultPoints[1].x * previewScale);
                point2.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[2].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point2.y = (int)(localizationResults[i].resultPoints[2].x * previewScale);
                point3.x = (int)((srcBitmapHeight - localizationResults[i].resultPoints[3].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point3.y = (int)(localizationResults[i].resultPoints[3].x * previewScale);
                points[0] = point0;
                points[1] = point1;
                points[2] = point2;
                points[3] = point3;
                rectCoord.add(points);
            }
        }
        return rectCoord;
    }
    public ArrayList<Point[]> handlePoints(TextResult[] textResults, float previewScale, int srcBitmapHeight, int srcBitmapWidth) {
        if (textResults == null) {
            return null;
        }
        ArrayList<Point[]> rectCoord = new ArrayList<>();
        Point point0;
        Point point1;
        Point point2;
        Point point3;
        Point[] points;
        for (int i = 0; i < textResults.length; i++) {
            points = new Point[4];
            point0 = new Point();
            point1 = new Point();
            point2 = new Point();
            point3 = new Point();
            if(dependOnWid){
                point0.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[0].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point0.y = (int)(textResults[i].localizationResult.resultPoints[0].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                point1.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[1].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point1.y = (int)(textResults[i].localizationResult.resultPoints[1].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                point2.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[2].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point2.y = (int)(textResults[i].localizationResult.resultPoints[2].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                point3.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[3].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point3.y = (int)(textResults[i].localizationResult.resultPoints[3].x * previewScale - (srcBitmapWidth * previewScale - viewHeight) / 2);
                points[0] = point0;
                points[1] = point1;
                points[2] = point2;
                points[3] = point3;
                rectCoord.add(points);
            }
            else {
                point0.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[0].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point0.y = (int)(textResults[i].localizationResult.resultPoints[0].x * previewScale);
                point1.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[1].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point1.y = (int)(textResults[i].localizationResult.resultPoints[1].x * previewScale);
                point2.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[2].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point2.y = (int)(textResults[i].localizationResult.resultPoints[2].x * previewScale);
                point3.x = (int)((srcBitmapHeight - textResults[i].localizationResult.resultPoints[3].y) * previewScale - (srcBitmapHeight * previewScale - viewWidth) / 2);
                point3.y = (int)(textResults[i].localizationResult.resultPoints[3].x * previewScale);
                points[0] = point0;
                points[1] = point1;
                points[2] = point2;
                points[3] = point3;
                rectCoord.add(points);
            }
        }
        return rectCoord;
    }
    public static Bitmap rotateBitmap(Bitmap origin) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        origin.recycle();
        return newBM;
    }
    public static ArrayList<Point[]> rotatePoints(TextResult[] textResults, int srcBitmapHeight, int srcBitmapWidth) {
        if (textResults != null && textResults.length > 0) {
            ArrayList<Point[]> rectCoord = new ArrayList<>();
            Point point0;
            Point point1;
            Point point2;
            Point point3;
            Point[] points;
            for (int i = 0; i < textResults.length; i++) {
                points = new Point[4];
                point0 = new Point();
                point1 = new Point();
                point2 = new Point();
                point3 = new Point();
                point0.x = (srcBitmapHeight - textResults[i].localizationResult.resultPoints[0].y);
                point0.y = textResults[i].localizationResult.resultPoints[0].x;
                point1.x = (srcBitmapHeight - textResults[i].localizationResult.resultPoints[1].y);
                point1.y = textResults[i].localizationResult.resultPoints[1].x;
                point2.x = (srcBitmapHeight - textResults[i].localizationResult.resultPoints[2].y);
                point2.y = textResults[i].localizationResult.resultPoints[2].x;
                point3.x = (srcBitmapHeight - textResults[i].localizationResult.resultPoints[3].y);
                point3.y = textResults[i].localizationResult.resultPoints[3].x;
                points[0] = point0;
                points[1] = point1;
                points[2] = point2;
                points[3] = point3;
                rectCoord.add(points);
            }
            return rectCoord;
        } else {
            return null;
        }
    }
    public static Bitmap drawRectOnBitmap(Bitmap bitmap, ArrayList<Point[]> Points, TextResult[] results,float strokeWidth, Context context) {
        if (Points == null) {
            return bitmap;
        }
        Bitmap rectBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(rectBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(context.getResources().getColor(R.color.aboutOK));
        paint.setAntiAlias(true);
        Path path = new Path();
        int width = (rectBitmap.getWidth() <= rectBitmap.getHeight()) ? rectBitmap.getWidth() : rectBitmap.getHeight();
        float r = width / 70;
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(2.5f * r);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setColor(context.getResources().getColor(R.color.white));
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setAntiAlias(true);
        Paint roundPaint = new Paint();
        roundPaint.setStyle(Paint.Style.STROKE);
        roundPaint.setStrokeWidth(2 * r);
        roundPaint.setColor(context.getResources().getColor(R.color.aboutOK));
        roundPaint.setAntiAlias(true);
        for (int i = 0; i < Points.size(); i++) {
            int confidence = results[i].results[0].confidence;
            if (confidence < 40) {
                paint.setColor(context.getResources().getColor(R.color.aboutBad));
                roundPaint.setColor(context.getResources().getColor(R.color.aboutBad));
            } else if (confidence < 70) {
                paint.setColor(context.getResources().getColor(R.color.aboutNormal));
                roundPaint.setColor(context.getResources().getColor(R.color.aboutNormal));
            } else {
                paint.setColor(context.getResources().getColor(R.color.aboutOK));
                roundPaint.setColor(context.getResources().getColor(R.color.aboutOK));
            }
            path.reset();
            path.moveTo(Points.get(i)[0].x, Points.get(i)[0].y);
            path.lineTo(Points.get(i)[1].x, Points.get(i)[1].y);
            path.lineTo(Points.get(i)[2].x, Points.get(i)[2].y);
            path.lineTo(Points.get(i)[3].x, Points.get(i)[3].y);
            path.close();
            canvas.drawPath(path, paint);
            float x = (Points.get(i)[0].x + Points.get(i)[1].x + Points.get(i)[2].x + Points.get(i)[3].x) / 4;
            float y = (Points.get(i)[0].y + Points.get(i)[1].y + Points.get(i)[2].y + Points.get(i)[3].y) / 4;
            canvas.drawCircle(x, y, r, roundPaint);
            if ((i + 1) < 10) {
                canvas.drawText(String.valueOf(i + 1), x - 0.63f * r, y + 0.92f * r, textPaint);
            } else {
                canvas.drawText(String.valueOf(i + 1), x - 1.33f * r, y + 0.92f * r, textPaint);
            }
        }

        return rectBitmap;
    }

    public static Bitmap convertYUVtoRGB(byte[] yuvData, int width, int height, Context context) {
        RenderScript rs;
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
        Type.Builder yuvType, rgbaType;
        Allocation in, out;
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuvData.length);
        in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        in.copyFrom(yuvData);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }
    public static byte[] convertImage(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buf);
        byte[] b =buf.array();
        for (int i = 0; i < b.length / 4; i++) {
            byte temp = b[i * 4];
            b[i * 4] = b[i * 4 + 2];
            b[i * 4 + 2] = temp;
        }
        return b;
    }
}
