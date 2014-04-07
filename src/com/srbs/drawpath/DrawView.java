package com.srbs.drawpath;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener {
	static final long MAX_ALLOWED_DIST = 10;
	static final long TIMER_WAIT_MILLISECONDS = 200;
	
	Timer timer;
	List<Point> points = new ArrayList<Point>();
    boolean addingFirstPoint = true;
    Point currentPoint;
    Paint paint = new Paint();
    
    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        paint.setColor(Color.BLACK);
        setHapticFeedbackEnabled(true);
    }

    void resetTimer() {
    	if (timer != null) {
        	timer.cancel();
    	}
    	timer = new Timer();
    	timer.schedule(new TimerTask() {
    		public void run() {
    			if (currentPoint != null) {
    				points.add(currentPoint);
        			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        			Log.i("Adding point", "Adding point");
    			}
    		}
    	}, TIMER_WAIT_MILLISECONDS);
    }
    
    float[] getPointsArray() {
    	if (points.isEmpty()) {
    		return new float[0];
    	}
    	int size = 0;
    	if (currentPoint == null) {
    		size = points.size()*4;
    	} else {
    		size = (points.size() + 1)*4;
    	}
    	float[] arr = new float[size];
    	int index = 0;
    	for(; index < points.size() - 1; index++) {
    		arr[index*4 + 0] = points.get(index).x;
    		arr[index*4 + 1] = points.get(index).y;
    		arr[index*4 + 2] = points.get(index + 1).x;
    		arr[index*4 + 3] = points.get(index + 1).y;
    	}
    	if (currentPoint != null) {
	    	arr[index*4 + 0] = points.get(index).x;
			arr[index*4 + 1] = points.get(index).y;
			arr[index*4 + 2] = currentPoint.x;
			arr[index*4 + 3] = currentPoint.y;
    	}
    	return arr;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	Log.i("Drawing", "Drawing");
    	canvas.drawLines(getPointsArray(), paint);
    }
    
    public boolean onTouch(View view, MotionEvent event) {
    	int action = event.getActionMasked();
    	Point point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        
    	if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
    		currentPoint = null;
    		timer.cancel();
    		Log.i("Action Up", "Action Up");
    	} else {
    		if (addingFirstPoint) {
            	points.add(point);
            	addingFirstPoint = false;
            	resetTimer();
            } else {
            	if (action == MotionEvent.ACTION_DOWN || 
    	        	currentPoint.distance(point) > MAX_ALLOWED_DIST) {
    	        	resetTimer();
    	        }
        	}
    		currentPoint = point;
    	}
    	invalidate();
        return true;
    }
}

class Point {
    float x, y;
    double distance(Point target) {
    	double x2 = (target.x-x)*(target.x-x);
    	double y2 = (target.y-y)*(target.y-y);
    	return Math.sqrt(x2 + y2);
    }
}