package com.alpine.hadoop.variableselection;

import com.alpine.hadoop.VariableSelectionKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Arrays;

public class R2Reducer extends
        Reducer<Text, DoubleArrayWritable, Text, Text> {

    MapReduceHelper utility;
    @Override
    public void reduce(Text key, Iterable<DoubleArrayWritable> values,Context context)
            throws IOException, InterruptedException {
        int columnSize = Integer.valueOf(key.toString());
        boolean firstTime = true;
        double[] resultSum = null;
        double[] r2s = new double[columnSize - 1];

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

        //everything has now been reduced to one array of doubles - finally we can figure out all the r2s.

        for (int i =0;i < columnSize - 1;i++)
        {
            double  ssErr =  resultSum[2*i];
            double  ssTotal =   resultSum[2*i + 1];
            double r2=1-(ssErr/ssTotal);

            r2s[i] = r2;

        }


        context.write(new Text(VariableSelectionKeySet.r2),new Text(Arrays.toString(r2s)));
    }


    public void setup(Context context) {
        utility = new MapReduceHelper(context.getConfiguration(),
                context.getTaskAttemptID());
    }
}
