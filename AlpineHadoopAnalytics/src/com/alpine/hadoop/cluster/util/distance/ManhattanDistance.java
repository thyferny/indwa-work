
package com.alpine.hadoop.cluster.util.distance;

import org.apache.hadoop.io.DoubleWritable;


public class ManhattanDistance implements Distance {

    public <S> double compute(S[] first, S[] second) {
        if (first.length != second.length) {
            throw new RuntimeException("Input Vectors must be the same length!");
        }

        double sum = 0.0;

        for(int i=0;i<first.length;i++){
        	sum=sum+Math.abs(((DoubleWritable)first[i]).get()-((DoubleWritable)second[i]).get());
        }
        return sum;
    }

}
