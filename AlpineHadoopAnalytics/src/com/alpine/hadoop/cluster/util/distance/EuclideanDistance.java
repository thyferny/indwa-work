

package com.alpine.hadoop.cluster.util.distance;

import org.apache.hadoop.io.DoubleWritable;



public class EuclideanDistance implements Distance {
    public <S> double compute(S[] first, S[] second) {
        // TODO: Error checking on inputs. Overflow checking and precision
        if (first.length != second.length) {
        	System.out.println(first + "	" + second);
            throw new RuntimeException("Input Vectors must be the same length!");
        }

        double sum = 0.0;
        int i = 0;
        for(S point : first) {
            sum += Math.pow((((DoubleWritable) point).get() - ((DoubleWritable) second[i]).get()), 2);
            i++;
        }

        return Math.sqrt(sum);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}

