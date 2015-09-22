package org.apache.pig.builtin;


public class QuantileEstimatorForSummaryStatistics extends QuantileEstimatorBase {

	public QuantileEstimatorForSummaryStatistics() {
		this(5);
	}

	private QuantileEstimatorForSummaryStatistics(int numQuantiles) {
		super(5);
	}
}
