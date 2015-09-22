package com.alpine.datamining.utility;

public class NormDistributionUtility {

	public static double normDistributionQuantile(double p,double sigma) {
		if (p == 0.5) {
			return 0;
		}
		double[] b = { 0.1570796288E1, 0.3706987906E-1, -0.8364353589E-3,
				-0.2250947176E-3, 0.6841218299E-5, 0.5824238515E-5,
				-0.1045274970E-5, 0.8360937017E-7, -0.3231081277E-8,
				0.3657763036E-10, 0.6936233982E-12 };

		double alpha = 0;
		if ((0 < p) && (p < 0.5)) {
			alpha = p;
		} else if ((0.5 < p) && (p < 1)) {
			alpha = 1 - p;
		}

		double y = -Math.log(4 * alpha * (1 - alpha));
		double u = 0;

		for (int i = 0; i < b.length; i++) {
			u = u + b[i] * Math.pow(y, i);
		}
		u = Math.sqrt(y * u);

		double up = 0;
		if ((0 < p) && (p < 0.5)) {
			up = -u;
		} else if ((0.5 < p) && (p < 1)) {
			up = u;
		}
		if(sigma != Double.NaN){
			return up*sigma;
		}else{
			return up;
		}
	}
}
