package org.apache.pig.builtin;



class QuantileEstimator extends QuantileEstimatorBase
{
	private static QuantileEstimator instance;

	public int getNumberOfQuantiles()
	{
		return 5;
	}

	public static QuantileEstimator getInstance()
	{
		if (instance == null)
		{
			instance = new QuantileEstimator(5);

		}
		return instance;
	}

	private QuantileEstimator(int numQuantiles)
	{
		super(numQuantiles);
	}
}
