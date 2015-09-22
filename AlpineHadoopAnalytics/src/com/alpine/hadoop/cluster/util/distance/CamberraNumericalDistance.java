/**
 * ClassName CamberraDistance.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-12
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.cluster.util.distance;

import org.apache.hadoop.io.DoubleWritable;

/**
 * @author Jeff Dong
 *
 */
public class CamberraNumericalDistance implements Distance {

	@Override
	public <S> double compute(S[] first, S[] second) {
        if (first.length != second.length) {
            throw new RuntimeException("Input Vectors must be the same length!");
        }

        double sum = 0.0;

        for(int i=0;i<first.length;i++){
        	double data = ((DoubleWritable)first[i]).get();
        	double centroid = ((DoubleWritable)second[i]).get();
        	sum=sum+(Math.abs(data-centroid)/Math.abs(data+centroid));
        }
        return sum;
	}

}
