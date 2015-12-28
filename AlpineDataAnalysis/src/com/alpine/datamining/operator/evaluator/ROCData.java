
package com.alpine.datamining.operator.evaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.utility.Tools;


public class ROCData implements Iterable<ROCPoint> {

	private List<ROCPoint> points = new ArrayList<ROCPoint>();
	private double sumPos;
	private double sumNeg;
	private double bestIsometricsTP;

	public void addPoint(ROCPoint point) {
		points.add(point);
	}

	public void remove(ROCPoint point) {
		points.remove(point);
	}

	public int getNumberOfPoints() {
		return points.size();
	}

	public ROCPoint getPoint(int index) {
		return points.get(index);
	}

	public double getInterpolatedTP(double d) {
		if (Tools.isZero(d))
			return 0.0d;

		if (Tools.isGreaterEqual(d, getTotalPositives()))
			return getTotalPositives();

		if (points.size() == 2) {
			if (Tools.isLess(d, 1.0d)) {
				return sumPos % 2 == 0 ? sumPos / 2 : sumPos / 2 + 1;
			} else {
				return sumPos;
			}
		}

		ROCPoint last = null;
		for (ROCPoint p : this) {
			double fpDivN = p.getFP() / getTotalNegatives();
			if (Tools.isGreater(fpDivN, d)) {
				if (last == null) {
					return 0;
				} else {
					return last.getTP();
				}
			}
			last = p;
		}
		return getTotalPositives();
	}

	public double getThreshold(double d) {
		if (Tools.isZero(d))
			return 1.0d;

		if (Tools.isGreaterEqual(d, getTotalPositives()))
			return 0.0d;

		if (points.size() == 2) {
			if (Tools.isLess(d, 1.0d)) {
				return points.get(1).getConfidence();
			} else {
				return 0.0d;
			}
		}

		ROCPoint last = null;
		for (ROCPoint p : this) {
			double fpDivN = p.getFP() / getTotalNegatives();
			if (Tools.isGreater(fpDivN, d)) {
				if (last == null) {
					return 1.0d;
				} else {
					return last.getConfidence();
				}
			}
			last = p;
		}
		return 0.0d;
	}

	public Iterator<ROCPoint> iterator() {
		return points.iterator();
	}

	public void setTotalPositives(double sumPos) {
		this.sumPos = sumPos;
	}

	public double getTotalPositives() {
		return this.sumPos;
	}

	public void setTotalNegatives(double sumNeg) {
		this.sumNeg = sumNeg;
	}

	public double getTotalNegatives() {
		return this.sumNeg;
	}

	public void setBestIsometricsTPValue(double value) {
		this.bestIsometricsTP = value;
	}

	public double getBestIsometricsTPValue() {
		return this.bestIsometricsTP;
	}

}
