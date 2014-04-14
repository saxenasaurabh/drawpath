package com.srbs.drawpath;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * Recommends the next point depending on the
 * previous points and current point.
 * @author saurabh
 *
 */
class Recommender {
	// If the recommended point is farther than this distance from
	// the recommended point it is ignored and the current point is
	// returned.
	double minDistFromCurrentPoint;
	
	private MainActivity activity;
	
	Recommender(MainActivity activity) {
		this.activity = activity;
		minDistFromCurrentPoint = activity.getResources().getInteger(R.integer.recommendation_max_dist);
	}
	Point getRecommendedPoint(List<Point> points, Point currentPoint) {
		assert(currentPoint != null);
		if (points == null || points.isEmpty()) {
			return currentPoint;
		}
		List<Point> recommendations = new ArrayList<Point>();
		
		// Add points on the same horizontal or vertical line.
		{
			Point lastPoint = points.get(points.size() - 1);
			Point pointX = new Point(lastPoint.x, currentPoint.y);
			Point pointY = new Point(currentPoint.x, lastPoint.y);
			recommendations.add(pointX);
			recommendations.add(pointY);
		}
		
		double minDist = Double.MAX_VALUE;
		Point recommendedPoint = currentPoint;
		for (Point recommendation : recommendations) {
			double dist = recommendation.distance(currentPoint);
			if (dist < minDist) {
				minDist = dist;
				recommendedPoint = recommendation;
			}
		}
		if (recommendedPoint.distance(currentPoint) > minDistFromCurrentPoint) {
			return currentPoint;
		}
		return recommendedPoint;
	}
}
