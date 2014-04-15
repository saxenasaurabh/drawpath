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
	List<RecommendationRule> recommendationRules;
		
	Recommender(MainActivity activity) {
		minDistFromCurrentPoint = activity.getResources().getInteger(
				R.integer.recommendation_max_dist);
		initRecommendationRules();
	}
	
	private void initRecommendationRules() {
		recommendationRules = new ArrayList<RecommendationRule>();
		// Add new recommendation rules here
		recommendationRules.add(new XYRuleLastAdded());
		recommendationRules.add(new ExistingPointRule());
//		recommendationRules.add(new XYRuleAllButLast());
	}
	
	Point getRecommendedPoint(List<Point> points, Point currentPoint) {
		assert(currentPoint != null);
		if (points == null || points.isEmpty()) {
			return currentPoint;
		}
		List<Recommendation> recommendations = new ArrayList<Recommendation>();
		
		for (RecommendationRule rule: recommendationRules) {
			rule.addRecommedation(points, currentPoint, recommendations);
		}
		
		// TODO(saurabh):
		// Use more parameters for choosing the best point.
		// List no. of recommendations
		double minDist = Double.MAX_VALUE;
		Point recommendedPoint = currentPoint;
		for (Recommendation recommendation : recommendations) {
			double dist = recommendation.point.distance(currentPoint);
			if (dist < minDist) {
				minDist = dist;
				recommendedPoint = recommendation.point;
			}
		}
		if (recommendedPoint.distance(currentPoint) > minDistFromCurrentPoint) {
			return currentPoint;
		}
		return recommendedPoint;
	}
}

/**
 * The rule asserts that none of the inputs is null.
 * @author srbs
 *
 */
interface RecommendationRule {
	void addRecommedation(
			List<Point> points,
			Point currentPoint,
			List<Recommendation> recommendations);
}

/**
 * The recommended point and the recommendation reason.
 * @author saurabh
 *
 */
class Recommendation {
	Point point;
	RecommendationReason reason;
	public Recommendation(Point point, RecommendationReason reason) {
		this.point = point;
		this.reason = reason;
	}
}

/**
 * 
 * @author saurabh
 *
 */
class RecommendationReason {
	Point point;
	boolean showGuide;
	public RecommendationReason(Point point, boolean showGuide) {
		this.point = point;
		this.showGuide = showGuide;
	}
	public RecommendationReason(Point point) {
		this(point, false);
	}
}

/*******************************************************************************
 * RECOMMENDATION RULES
 ******************************************************************************/

/**
 * Recommends points that are on the same horizontal/vertical
 * line as the last added point.
 * @author srbs
 *
 */
class XYRuleLastAdded implements RecommendationRule {
	@Override
	public void addRecommedation(List<Point> points, Point currentPoint,
			List<Recommendation> recommendations) {
		assert(currentPoint != null && recommendations != null && currentPoint != null);
		if (points.isEmpty()) {
			return;
		}
		Point lastPoint = points.get(points.size() - 1);
		Point pointX = new Point(lastPoint.x, currentPoint.y);
		Point pointY = new Point(currentPoint.x, lastPoint.y);
		recommendations.add(new Recommendation(pointX,
				new RecommendationReason(lastPoint)));
		recommendations.add(new Recommendation(pointY,
				new RecommendationReason(lastPoint)));
	}
}

class XYRuleAllButLast implements RecommendationRule {
	@Override
	public void addRecommedation(List<Point> points, Point currentPoint,
			List<Recommendation> recommendations) {
		assert(currentPoint != null && recommendations != null && currentPoint != null);
		if (points.size() <= 1) {
			return;
		}
		for (int i = 0; i < points.size() - 1; i++) {
			Point p = points.get(i);
			Point pointX = new Point(p.x, currentPoint.y);
			Point pointY = new Point(currentPoint.x, p.y);
			recommendations.add(new Recommendation(pointX,
					new RecommendationReason(points.get(i), true)));
			recommendations.add(new Recommendation(pointY,
					new RecommendationReason(points.get(i), true)));
		}
	}
}

/**
 * Recommends existing points other than the previous one.
 * @author saurabh
 *
 */
class ExistingPointRule implements RecommendationRule {
	@Override
	public void addRecommedation(List<Point> points, Point currentPoint,
			List<Recommendation> recommendations) {
		assert(currentPoint != null && recommendations != null && currentPoint != null);
		if (points.size() <= 1) {
			return;
		}
		for (int i = 0; i < points.size() - 1; i++) {
			recommendations.add(new Recommendation(points.get(i),
					new RecommendationReason(points.get(i))));
		}
	}
}

/**
 * TODO
 * Add rec rule names. Will be nice to debug and enable disable rules.
 * If currPoint lies on a previously drawn line.
 * If currPoint lines up with a previously drawn point.
 * Stretch: Pause mode to choose between recommendations
 */


