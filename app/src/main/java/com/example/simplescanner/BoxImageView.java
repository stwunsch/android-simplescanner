package com.example.simplescanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.List;

public class BoxImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Paint paint;
    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;
    private List<Point> points;
    private int canvasWidth;
    private int canvasHeight;
    boolean boxIsInitialized;

    private static final double DISTANCE_TO_MOVE_EDGE = 100;

    public void resetBox() {
        boxIsInitialized = false;
    }

    public List<Point> getPoints() {
        return points;
    }

    private void initMembers() {
        paint = new Paint();
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);

        topLeft = new Point();
        topRight = new Point();
        bottomLeft = new Point();
        bottomRight = new Point();

        points = new LinkedList<Point>();
        points.add(topLeft);
        points.add(topRight);
        points.add(bottomRight);
        points.add(bottomLeft);

        boxIsInitialized = false;
    }

    public BoxImageView(Context context) {
        super(context);
        initMembers();
    }

    public BoxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMembers();
    }

    public BoxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMembers();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("BoxImageView", "Draw canvas");
        if (!boxIsInitialized) {
            canvasWidth = getWidth();
            canvasHeight = getHeight();
            int drawOffset = (int) (canvasWidth * 0.02);
            topLeft.set(drawOffset, drawOffset);
            topRight.set(canvasWidth - drawOffset, drawOffset);
            bottomLeft.set(drawOffset, canvasHeight - drawOffset);
            bottomRight.set(canvasWidth - drawOffset, canvasHeight - drawOffset);
            boxIsInitialized = true;
        }
        canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, paint);
        canvas.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y, paint);
        canvas.drawLine(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y, paint);
        canvas.drawLine(bottomLeft.x, bottomLeft.y, topLeft.x, topLeft.y, paint);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (!boxIsInitialized) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            Log.d("BoxImageView", "Touch event at " + x + ", " + y);
            for (int j = 0; j < 4; j++) {
                Point p = points.get(j);
                double dx = x - p.x;
                double dy = y - p.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < DISTANCE_TO_MOVE_EDGE) {
                    if (x < 0) x = 0;
                    else if (x > canvasWidth) x = canvasWidth;
                    if (y < 0) y = 0;
                    else if (y > canvasHeight) y = canvasHeight;
                    points.get(j).set((int) x, (int) y);
                    invalidate();
                    return true;
                }
            }
        }
        return true;
    }
}