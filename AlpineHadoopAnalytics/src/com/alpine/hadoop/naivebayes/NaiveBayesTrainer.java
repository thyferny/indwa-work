package com.alpine.hadoop.naivebayes;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.util.DataPretreatUtility;
import com.alpine.hadoop.util.HadoopInteractionItem;
import com.alpine.hadoop.util.KnuthMorrisPratt;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: adehghani
 * Date: 1/18/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class NaiveBayesTrainer{

    /*behdad*/

    public static final String seprator_ = "___"; // choice of a safe seprator; hopefully no feature value contains this!
    private static final String classCounterPrefix_ = "Class:";
    private static final String intermedOutputPath_ = "NaiveBayes";
    private static final String finaloutputPath_ = "NaiveBayes.model";
    //private static final int dependentColumId = 2;






    public static class NaiveBayesTrainingMapper extends Mapper<LongWritable, Text, Text, Text> {

        MapReduceHelper helper;
        Text outputKey=new Text();
        DoubleArrayWritable outputValue = new DoubleArrayWritable();
        int dependentColumId = -1;
        List<Integer> ids;
        private static Logger itsLogger = Logger.getLogger(NaiveBayesTrainingMapper.class);


        @Override
        public void map(LongWritable key, Text value, Context context) throws InterruptedException,IOException {

            List<String[]> columnValuesList = helper.getCleanData(value, false);
            if (columnValuesList != null) {    //if null, not using that row, so ignore
                for (String[] columnValues : columnValuesList) {

                    if (columnValues != null) {
                      String yValue = columnValues[dependentColumId];
                      String class_name = helper.getColumnNames()[dependentColumId];

                        byte[] isRealValued = new byte[columnValues.length];
                        Arrays.fill(isRealValued, (byte) 0);
                        Iterator<Integer> columnIterator = helper.getInvolvedColumnIds().iterator();
                        while(columnIterator.hasNext())
                        {
                           int i = columnIterator.next().intValue(); // this is the independent column id
                            if (i != dependentColumId) {
                                String xValue = columnValues[i];
                                String feature_name = helper.getColumnNames()[i];

                                //figure out whether real or not.
                                String dataType = helper.getColumnTypes()[i];
                                boolean isReal = DataPretreatUtility.isNumberType(dataType);

                                Text new_key;
                                if (!isReal) { // this is a categorial data

                                    /*Key format: {0:not real-value or 1: real-valued}seprator{feature name}seprator{feature_value}_seprator{class_name}seprator{class_value}*/
                                    /*value: 1*/
                                    new_key = new Text(new StringBuilder().append("0").append(seprator_).append(feature_name).append(seprator_).append(columnValues[i])
                                            .append(seprator_).append(class_name).append(seprator_).append(columnValues[dependentColumId]).toString());
                                    context.write(new_key, new Text("1"));
                                } else {
                                    /*Key format: {0:not real-value or 1: real-valued}seprator{feature name}seprator{feature name}_seprator{class_name}seprator{class_value}*/
                                    /*value: feature value*/
                                    new_key = new Text(new StringBuilder().append("1").append(seprator_).append(feature_name).append(seprator_).append(feature_name)
                                            .append(seprator_).append(class_name).append(seprator_).append(columnValues[dependentColumId]).toString());
                                    context.write(new_key, new Text(columnValues[i]));
                                }
                            }
                        }
                        //context.write(new Text("Class:".concat(features[dependentColumId])), new LongWritable(1));
                        context.getCounter("Job_Stats", "Num_Samples").increment(1);
                        context.getCounter("Job_Stats", new StringBuilder(classCounterPrefix_).append(columnValues[dependentColumId]).toString()).increment(1);
                    }
                }

            }



         }


        public void setup(Context context) {
            helper = new MapReduceHelper(context.getConfiguration(),
                    context.getTaskAttemptID());
            ids = helper.getColumnIds(NaiveBayesKeySet.columns);
            //ids is missing the dependent column id, but has everything else.
            dependentColumId = helper.getDependentId(NaiveBayesKeySet.dependent);

            ArrayList<Integer> involvedColumnIds = new ArrayList<Integer>();
            for (int id : ids) {
                involvedColumnIds.add(id);
            }
            involvedColumnIds.add(dependentColumId);
            helper.setInvolvedColumnIds(involvedColumnIds);

        }

    }


    /*
     * The combiner class is essentially useful for non-real-valued features. Since real valued feature should get
     * accumulated into the reducer fro fitting a prior probablity function to them
     */
    public static class NaiveBayesTrainingCombiner extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context) throws IOException, InterruptedException {

            if (isrealValued(key.toString())) {// this is  a real-valued feature. So don't apply the combiner!
                Iterator<Text> it = values.iterator();
                while (it.hasNext()) {
                    context.write(key, it.next());
                }
            } else { // the feature is categorial (string or descrete number)
                Long sum = new Long(0);
                Iterator<Text> it = values.iterator();
                while (it.hasNext()) {
                    sum += new Integer(it.next().toString()).intValue();
                }
                context.write(key, new Text(sum.toString()));
            }

        }
    }





    public static class NaiveBayesTrainingReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context) throws IOException, InterruptedException {

            Iterator<Text> it = values.iterator();
            if (!isrealValued(key.toString())) {// this is for a categorial feature
                Long sum = new Long(0);
                while (it.hasNext()) {
                    sum += new Long(it.next().toString()).longValue();
                }
                context.write(new Text(new StringBuilder(key.toString()).append(seprator_).append(sum.toString()).toString()), null);
            } else { //this is for  areal-valued feature
                File dataFile = File.createTempFile("NaiveBayes", ".tmp_file");
                BufferedWriter bfw = new BufferedWriter(new FileWriter(dataFile), 128 * 1024);
                long num_recs = 0;
                float sum_recs = 0;
                while (it.hasNext()) {
                    String feature_val = it.next().toString();
                    try {
                        sum_recs += new Float(feature_val).floatValue();
                        num_recs++;
                        /*writing to a temporary file for next pass for computing the variance*/
                        bfw.write(feature_val);
                        bfw.write("\n");
                    } catch (NumberFormatException e) {
                        continue; // if for some reason the feature value is not a float value
                    }
                }
                bfw.close();
                /*second pass of going through the data is for finding the gaussian variance*/
                float mean = (num_recs == 0 ? 0 :  (sum_recs / num_recs));
                BufferedReader bfr = new BufferedReader(new FileReader(dataFile), 128 * 1024);
                num_recs = 0;
                sum_recs = 0;
                for (String feature_val = bfr.readLine(); feature_val != null; feature_val = bfr.readLine()) {
                    num_recs++;
                    float tmp = new Float(feature_val).floatValue() - mean;
                    sum_recs += tmp * tmp;
                }

                float variance = (num_recs <2 ? 0 :  (sum_recs / (num_recs-1)));

                    //TODO
                dataFile.delete(); // cleaning the temp data
                context.write(new Text(new StringBuilder(key.toString()).append(seprator_).append(mean + "," + variance).toString()), null);
            }
        }
    }

    public static boolean isrealValued(String row) {
        return ("1".equals(row.substring(0, 1)) ? true : false);
    }

    /**
     * Returns the feature name from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return: the feature name is located between first and second separator
     */
    public static String getFeatureName(String row, ArrayList<Integer> poses) {
        return (row.substring(poses.get(0) + seprator_.length(), poses.get(1)));
    }

    /**
     * Returns the feature value from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the feature value is located between the second and third
     * separator
     */
    public static String getFeatureValue(String row, ArrayList<Integer> poses) {
        return (row.substring(poses.get(1) + seprator_.length(), poses.get(2)));
    }

    /**
     * Returns the class name from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the class name is located between the third and fourth
     * separator
     */
    public static String getClassName(String row, ArrayList<Integer> poses) {
        return (row.substring(poses.get(2) + seprator_.length(), poses.get(3)));
    }

    /**
     * Returns the class value count from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the feature value is located between the sixth and seventh
     * separator
     */
    public static long getClassValCount(String row, ArrayList<Integer> poses) {
        return (new Long(row.substring(poses.get(5) + seprator_.length(), poses.get(6))));
    }


    /**
     * Returns the class name from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the class name is located between the third and fourth
     * separator
     */
    public static String getClassValue(String row, ArrayList<Integer> poses) {
        return (row.substring(poses.get(3) + seprator_.length(), poses.get(4)));
    }

    /**
     * Returns the feature value count from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the feature value count is located between the fifth and sixth
     * separator
     */
    public static String getFeatureValCount(String row, ArrayList<Integer> poses) {
        return (row.substring(poses.get(4) + seprator_.length()));
    }

    /**
     * Returns the computed Gaussian mean from a row of Naive Bayes model
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the mean is located between the fifth and sixth separator and
     * comma separated from variance
     */
    public static String getMean(String row, ArrayList<Integer> poses) {
        String str = row.substring(poses.get(4) + seprator_.length());
        return str.substring(0, KnuthMorrisPratt.kmp(str, ",", 1).get(0));
    }

    /**
     * Returns the computed Gaussian variance from a row of Naive Bayes model
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the variance is located between the fifth and sixth separator and
     * comma separated from mean
     */
    public static String getVariance(String row, ArrayList<Integer> poses) {
        String str = row.substring(poses.get(4) + seprator_.length());
        return str.substring(KnuthMorrisPratt.kmp(str, ",", 1).get(0)+1,str.length());
    }


    /**
     * Returns the number of samples from a row of Naive Bayes model
     *
     * @param poses: is the array of indexes of separators found by applying KMP
     * algorithm
     * @return the feature value is located between the fifth and sixth
     * separator
     */
    public static String getNumSamples(String row, ArrayList<Integer> poses) {
        return (row.substring(poses.get(6) + seprator_.length(), row.length()));
    }

    /*variance is actually variance^2*/
    private static float evalGaussian(float mean, float variance, float test_sample) {
        return (float) (Math.pow(Math.E, -1 * Math.pow(test_sample - mean, 2) / (2 * variance))) * (float) (1 / (Math.sqrt(2 * Math.PI * variance)));
    }




}
