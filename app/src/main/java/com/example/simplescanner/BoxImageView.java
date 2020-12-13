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
    private Paint paintLine;
    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;
    private List<Point> points;
    private int canvasWidth;
    private int canvasHeight;
    private int closestPoint;
    boolean boxIsInitialized;

    private static final double DISTANCE_TO_MOVE_EDGE = 100;

    public void resetBox() {
        boxIsInitialized = false;
    }

    public List<Point> getPoints() {
        return points;
    }

    private void initMembers() {
        paintLine = new Paint();
        paintLine.setDither(true);
        paintLine.setColor(0xFFD50000);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeJoin(Paint.Join.ROUND);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        paintLine.setStrokeWidth(5);

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
        canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, paintLine);
        canvas.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y, paintLine);
        canvas.drawLine(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y, paintLine);
        canvas.drawLine(bottomLeft.x, bottomLeft.y, topLeft.x, topLeft.y, paintLine);
    }

    private void updateClosestPoint(double x, double y) {
        if (x < 0) x = 0;
        else if (x > canvasWidth) x = canvasWidth;
        if (y < 0) y = 0;
        else if (y > canvasHeight) y = canvasHeight;
        points.get(closestPoint).set((int) x, (int) y);
        invalidate();
    }

    private void findClosestPoint(double x, double y) {
        double minDistance = Double.MAX_VALUE;
        for (int j = 0; j < 4; j++) {
            Point p = points.get(j);
            double dx = x - p.x;
            double dy = y - p.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = j;
            }
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (!boxIsInitialized) {
            return true;
        }
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(getClass().getSimpleName(), "Touched down at " + x + ", " + y);
            findClosestPoint(x, y);
            updateClosestPoint(x, y);
        } else if (action == MotionEvent.ACTION_MOVE) {
            updateClosestPoint(x, y);
        } else if (action == MotionEvent.ACTION_UP) {
            Log.d(getClass().getSimpleName(), "Released touch at " + x + ", " + y);
            closestPoint = -1;
        }
        return true;
    }
}