
package com.alpine.hadoop.variableselection;


import com.alpine.hadoop.VariableSelectionKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Arrays;


public class AlphaBetaReducer extends
		Reducer<Text, DoubleArrayWritable, Text, Text> {

	public void reduce(Text key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		
		int columnSize = Integer.valueOf(key.toString());
        boolean firstTime = true;
		double[] resultSum = null;
	 
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			if (firstTime) {
				resultSum = new double[in.length];
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = ((DoubleWritable) in[i]).get();
				}
			} else {
				for (int i = 0; i < in.length; i++) {
					resultSum[i] += ((DoubleWritable) in[i]).get();
				}
			}
            firstTime=false;
		}

        //everything has now been reduced to one array of doubles - now time to do the manipulation

        double finalCount =  resultSum[resultSum.length-1];
        double sum_dep = resultSum[0];
        double yavg = sum_dep/finalCount;
        double[] alphas = new double[columnSize - 1];
        double[] betas = new double[columnSize - 1];

        for (int i =0;i < columnSize - 1;i++)
        {
            double  sum_ind_dep =  resultSum[3*i + 1];
            double  sum_ind =   resultSum[3*i + 2];
            double  sum_ind_sq =  resultSum[3*i + 3];

            double beta_top = sum_ind_dep - (sum_ind)*(sum_dep)/finalCount;
            double beta_bottom = sum_ind_sq - (sum_ind)*(sum_ind)/finalCount;
            double beta = beta_top / beta_bottom;
            double alpha = (sum_dep  - beta * sum_ind)/finalCount;

            alphas[i] = alpha;
            betas[i] = beta;
        }


		context.write(new Text(VariableSelectionKeySet.beta), new Text(Arrays.toString(betas)));
        context.write(new Text(VariableSelectionKeySet.alpha), new Text(Arrays.toString(alphas)));
		context.write(new Text(VariableSelectionKeySet.dependent_avg), new Text(yavg+""));
	}



}
