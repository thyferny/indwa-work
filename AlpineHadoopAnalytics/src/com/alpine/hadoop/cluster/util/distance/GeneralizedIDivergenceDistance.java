
package com.alpine.hadoop.cluster.util.distance;

import org.apache.hadoop.io.DoubleWritable;


public class GeneralizedIDivergenceDistance implements Distance {

	@Override
	public <S> double compute(S[] first, S[] second) {
		//fisrt is data,second is sample centroid
        if (first.length != second.length) {
            throw new RuntimeException("Input Vectors must be the same length!");
        }

        double sum = 0.0;
        double sumLn = 0.0;
        double sumMinus = 0.0;

        for(int i=0;i<first.length;i++){
        	double data = ((DoubleWritable)first[i]).get();
        	double centroid = ((DoubleWritable)second[i]).get();
        	sumLn=sumLn+(centroid*Math.log(centroid/data));
        	sumMinus=sumMinus+(centroid-data);
        }
        sum=sumLn-sumMinus;
        return sum;
	}

}
