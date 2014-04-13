// TODO(saurabh):
// 1. Show circles at point of touch.
// 2. Show guide lines
// 3. Show recommended points and snap to those points.
// 4. Ability to draw multiple paths.

package com.srbs.drawpath;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener,
		PathModelListener {
	PathModel pathModel;
    Paint paint = new Paint();
    MainActivity currentActivity;
    
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        
        setBackgroundColor(Color.WHITE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setHapticFeedbackEnabled(true);
        setLongClickable(true);
        setOnTouchListener(this);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	initModelIfNeeded();
		canvas.drawLines(pathModel.getPathArray(), paint);
    }
    
    public boolean onTouch(View view, MotionEvent event) {
    	initModelIfNeeded();
    	int action = event.getActionMasked();
    	Point point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        
    	if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
    		pathModel.addPoint(null);
    		Log.i("Action Up", "Action Up");
    	} else {
        	pathModel.addPoint(point);
    	}
    	invalidate();
        return true;
    }
    
    public void onPointAdded() {
    	performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }
    
    /**
     * TODO(saurabh):
     * This method is a hack because the activity is not available
     * in the constructor. Find a way to detect when activity is
     * ready. 
     */
    private void initModelIfNeeded() {
    	if (pathModel != null) {
    		return;
    	}
    	currentActivity = (MainActivity) getContext();
        pathModel = new PathModel(currentActivity);
        pathModel.onPointAdded(this);
    }
}


interface PathModelListener {
	void onPointAdded();
}

/**
 * 
 * @author saurabh
 *
 * This model listens for new points of touch and
 * adds them to the path when the point of touch
 * does not vary for too long.
 */
class PathModel {
	private long MAX_ALLOWED_DIST = 10;
	private long TIMER_WAIT_MILLISECONDS = 300;
	
	private final String timerWaitPrefKey;
	private final String distThresholdPrefKey;
	private final int timerWaitDefault;
	private final int distThresholdDefault;
	
	private MainActivity currentActivity;
	private Timer timer;
    private Point currentPoint;
	private ArrayList<PathModelListener> listeners =
			new ArrayList<PathModelListener> ();
    
	PathModel(MainActivity activity) {
		currentActivity = activity;
		timerWaitPrefKey = currentActivity.getResources().getString(R.string.timer_wait_preference_key);
		distThresholdPrefKey = currentActivity.getResources().getString(R.string.dist_threshold_preference_key);
		timerWaitDefault = currentActivity.getResources().getInteger(R.integer.timer_wait_default);
		distThresholdDefault = currentActivity.getResources().getInteger(R.integer.dist_threshold_default);
		
		currentActivity.prefs.registerOnSharedPreferenceChangeListener(
				new SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
							String key) {
				    	TIMER_WAIT_MILLISECONDS = sharedPreferences.getLong(timerWaitPrefKey,
				    			timerWaitDefault);
				    	MAX_ALLOWED_DIST = sharedPreferences.getLong(distThresholdPrefKey,
				    			distThresholdDefault);
					}
				});
	}
	
    public List<Point> points = new ArrayList<Point>();
    
    public void onPointAdded(PathModelListener listener) {
    	listeners.add(listener);
    }
    
	public void addPoint(Point p) {
		if (p == null) {
			// Touch event cancelled or user lifted finger from screen
			currentPoint = null;
			timer.cancel();
		} else {
			if (currentPoint == null || currentPoint.distance(p) > MAX_ALLOWED_DIST) {
	        	resetTimer();
	        }
    		currentPoint = p;
		}
	}
	
	/**
	 * Returns flattened list of path points which can
	 * be directly rendered on the canvas.
	 */
	public float[] getPathArray() {
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
	
	private void resetTimer() {
    	if (timer != null) {
        	timer.cancel();
    	}
    	timer = new Timer();
    	timer.schedule(new TimerTask() {
    		public void run() {
    			if (currentPoint != null) {
    				points.add(currentPoint);
    				for(PathModelListener listener: listeners) {
    					listener.onPointAdded();
    				}
        			Log.i("Adding point", "Adding point");
    			}
    		}
    	}, TIMER_WAIT_MILLISECONDS);
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