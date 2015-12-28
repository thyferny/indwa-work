
package com.alpine.hadoop.cluster.util.distance;

import org.apache.hadoop.io.DoubleWritable;


public class InnerProductSimilarityDistance implements Distance {

	@Override
	public <S> double compute(S[] first, S[] second) {
        if (first.length != second.length) {
            throw new RuntimeException("Input Vectors must be the same length!");
        }

        double sum = 0.0;

        for(int i=0;i<first.length;i++){
        	double data = ((DoubleWritable)first[i]).get();
        	double centroid = ((DoubleWritable)second[i]).get();
        	sum=sum+data*centroid;
        }
        return sum*(-1.0);
	}

}
