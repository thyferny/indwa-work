package com.alpine.hadoop.variableselection;

import com.alpine.hadoop.VariableSelectionKeySet;
import com.alpine.hadoop.util.HadoopInteractionItem;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;


public class R2Mapper extends Mapper<LongWritable, Text, Text, DoubleArrayWritable> {


    int dependentColumId = -1;
    List<Integer> ids;

    double[] betas;
    double[] alphas;

    List<HadoopInteractionItem> interactionItems;
    double yavg = 0;

    MapReduceHelper utility;
    Text outputKey=new Text();
    DoubleArrayWritable outputValue=new DoubleArrayWritable();

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        utility.initInvolvedColumnIds(ids, interactionItems, dependentColumId);
        List<String[]> columnValuesList = utility.getCleanData(value, false);
        if (columnValuesList != null) {
            for (String[] columnValues : columnValuesList) {

                if (columnValues != null) {

                    int newColumnSize = ids.size()+1;


                    double[] usingColumn = new double[ids.size()];
                    for (int ks = 0; ks < ids.size(); ks++) {
                        usingColumn[ks] = Double.valueOf(columnValues[ids
                                .get(ks)]);
                    }

                    double y = Double.valueOf(columnValues[dependentColumId]);

                    DoubleWritable[] mapped = new DoubleWritable[(2 * ids.size())];

                    int index = 0;
                    for (int i = 0; i < newColumnSize - 1; i++) {
                        double alpha = alphas[i];
                        double beta = betas[i];
                        double indValue = usingColumn[i];


                        double ssErr = (y - alpha - (beta * indValue))*(y - alpha - (beta * indValue)) ;
                        double ssTot =  (y - yavg) * (y - yavg);

                        mapped[index] = new DoubleWritable(ssErr);
                        mapped[index+1] = new DoubleWritable(ssTot);
                        index+=2;


                    }
                    outputValue.set(mapped);
                    outputKey.set(String.valueOf(newColumnSize));

                    context.write(outputKey, outputValue);

                }
            }
        }

    }

    public void setup(Context context) {
        utility = new MapReduceHelper(context.getConfiguration(),
                context.getTaskAttemptID());
        interactionItems = utility
                .getInteractionItems(VariableSelectionKeySet.interactionItems);
        ids = utility.getColumnIds(VariableSelectionKeySet.columns);

        dependentColumId = utility.getDependentId(VariableSelectionKeySet.dependent);
        String[] betaArray = utility.getConfigArray(VariableSelectionKeySet.beta);
        String[] alphaArray = utility.getConfigArray(VariableSelectionKeySet.alpha);

        betas = new double[betaArray.length];
        alphas =  new double[betaArray.length];
        for (int i = 0; i < betaArray.length; i++) {
            betas[i] = Double.valueOf(betaArray[i]);
            alphas[i] = Double.valueOf(alphaArray[i]);
        }
        yavg = utility.getConfigDouble(VariableSelectionKeySet.dependent_avg);
    }
}