
package com.alpine.hadoop.cluster.util.distance;

import org.apache.hadoop.io.DoubleWritable;


public class JaccardNumericalSimilarityDistance implements Distance {

	@Override
	public <S> double compute(S[] first, S[] second) {
        if (first.length != second.length) {
            throw new RuntimeException("Input Vectors must be the same length!");
        }

        double sum = 0.0;
        double sum1 = 0.0;

        for(int i=0;i<first.length;i++){
        	double data = ((DoubleWritable)first[i]).get();
        	double centroid = ((DoubleWritable)second[i]).get();
        	sum=sum+data*centroid;
        	sum1=sum1+data+centroid;
        }
        sum=(-1.0)*sum/(sum1-sum);
        return sum;
	}

}
