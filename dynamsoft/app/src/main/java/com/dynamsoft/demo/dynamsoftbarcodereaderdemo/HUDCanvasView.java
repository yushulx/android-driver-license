package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.dynamsoft.barcode.TextResult;

import java.util.ArrayList;

public class HUDCanvasView extends View {
    int paddingLeft;
    int paddingTop;
    int paddingRight;
    int paddingBottom;
    private ArrayList<Point[]> resultPoint = null;
    private ArrayList<Point[]> localPoint = null;
    private TextResult[] results;
    private Path path = new Path();
    private Path path2 = new Path();
    private Paint paint;
    private Paint paint2;
    private int degree;
    private float previewScale;
    private int srcWidth, srcHeight;
    private FrameUtil frameUtil;

    public HUDCanvasView(Context context) {
        super(context);
    }

    public HUDCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(9f);
        paint.setAntiAlias(true);

        paint2 = new Paint();
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(9f);
        paint2.setAntiAlias(true);
        paint2.setColor(getResources().getColor(R.color.viewfinder_laser));

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
        frameUtil = new FrameUtil();
        //Log.d("hud", "padding info : " + paddingLeft + " * " + paddingTop);
    }

    public HUDCanvasView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (resultPoint != null && resultPoint.size() > 0) {
            for (int i = 0; i < resultPoint.size(); i++) {
                int confidence = results[i].results[0].confidence;
                if (confidence < 40) {
                    paint.setColor(getResources().getColor(R.color.aboutBad));
                } else if (confidence < 70) {
                    paint.setColor(getResources().getColor(R.color.aboutNormal));
                } else {
                    paint.setColor(getResources().getColor(R.color.aboutOK));
                }
                path.reset();
                path.moveTo(resultPoint.get(i)[0].x + paddingLeft, resultPoint.get(i)[0].y + paddingTop);
                path.lineTo(resultPoint.get(i)[1].x + paddingLeft, resultPoint.get(i)[1].y + paddingTop);
                path.lineTo(resultPoint.get(i)[2].x + paddingLeft, resultPoint.get(i)[2].y + paddingTop);
                path.lineTo(resultPoint.get(i)[3].x + paddingLeft, resultPoint.get(i)[3].y + paddingTop);
                path.close();
                canvas.drawPath(path, paint);
            }
        }
        if (localPoint != null && localPoint.size() > 0) {
            for (int i = 0; i < localPoint.size(); i++) {
                path2.reset();
                path2.moveTo(localPoint.get(i)[0].x + paddingLeft, localPoint.get(i)[0].y + paddingTop);
                path2.lineTo(localPoint.get(i)[1].x + paddingLeft, localPoint.get(i)[1].y + paddingTop);
                path2.lineTo(localPoint.get(i)[2].x + paddingLeft, localPoint.get(i)[2].y + paddingTop);
                path2.lineTo(localPoint.get(i)[3].x + paddingLeft, localPoint.get(i)[3].y + paddingTop);
                path2.close();
                canvas.drawPath(path2, paint2);
            }
        }
    }

    public void setBoundaryPoints(ArrayList<Point[]> resultPoint, ArrayList<Point[]> localPoint, TextResult[] results) {
        this.results = results;
        this.resultPoint = resultPoint;
        this.localPoint = localPoint;
    }

    public void setBoundaryColor(String color) {
        paint.setColor(Color.parseColor(color));
    }

    public void setBoundaryThickness(int thickness) {
        paint.setStrokeWidth(thickness);
    }

    public void clear() {
        resultPoint = null;
        localPoint = null;
        invalidate();
    }

    public void setCanvasDegree(int degree) {
        this.degree = degree;
    }
}
