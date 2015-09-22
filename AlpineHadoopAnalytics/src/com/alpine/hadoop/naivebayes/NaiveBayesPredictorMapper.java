package com.alpine.hadoop.naivebayes;

import java.util.*;
import java.io.IOException;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.conversion.Stringifier;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * User: adehghani
 */

public class NaiveBayesPredictorMapper extends Mapper<LongWritable, Text, Text, Text> {

    MapReduceHelper helper;
    Map<String,String> distinctClassValues;

    private static HashMap<String, NaiveBayesModelRow> naiveBayesModel_;
    private static long numSamples_;
    private static int depColId;

    private static HashSet<String> classLabels;
    /*variance is actually variance^2*/

    private static float evalGaussian(float mean, float variance, float test_sample) {
        return (float) (Math.pow(Math.E, -1 * Math.pow(test_sample - mean, 2) / (2 * variance))) * (float) (1 / (Math.sqrt(2 * Math.PI * variance)));
    }

    @Override
    protected void setup(Context context) throws IOException {
        helper = new MapReduceHelper(context.getConfiguration(),
                context.getTaskAttemptID());

        List<Integer> ids = helper.getColumnIds(NaiveBayesKeySet.columns);
        //ids is missing the dependent column id, but has everything else.

        ArrayList<Integer> involvedColumnIds = new ArrayList<Integer>();
        for (int id : ids) {
            involvedColumnIds.add(id);
        }
        helper.setInvolvedColumnIds(involvedColumnIds);

//        String[] columnStrings = helper.getConfigArray(NaiveBayesKeySet.columns);
        String[] isRealValue_ = helper.getConfigArray(NaiveBayesKeySet.isRealValue_);
        String[] featureName_ = helper.getConfigArray(NaiveBayesKeySet.featureName_);
        String[] featureValue_ = helper.getConfigArray(NaiveBayesKeySet.featureValue_);
        String[] className_ = helper.getConfigArray(NaiveBayesKeySet.className_);
        String[] classValue_ = helper.getConfigArray(NaiveBayesKeySet.classValue_);
        String[] featureValueCount_ = helper.getConfigArray(NaiveBayesKeySet.featureValueCount_);
        String[] classValueCount_ = helper.getConfigArray(NaiveBayesKeySet.classValueCount_);
        String[] mean_ = helper.getConfigArray(NaiveBayesKeySet.mean_);
        String[] variance_ = helper.getConfigArray(NaiveBayesKeySet.variance_);
        String numSamples = helper.getConfigString(NaiveBayesKeySet.numSamples_);
        Stringifier<String,String> stringifier= new Stringifier<String,String>();

        distinctClassValues = new HashMap<String,String>();

        stringifier.stringToMap(helper.getConfigString(NaiveBayesKeySet.distinctClassValues_), distinctClassValues);

        classLabels = new HashSet<String>();
        classLabels.addAll(Arrays.asList(classValue_));

        numSamples_ = new Long(numSamples).longValue();
        depColId = helper.getDependentId(NaiveBayesKeySet.dependent);
        /*creating a hashmap of key values*/
        naiveBayesModel_ = new HashMap<String, NaiveBayesModelRow>();
        for (int i =0; i<isRealValue_.length; i++ ){
            NaiveBayesModelRow value;
            String Key;
            String class_val = classValue_[i].trim();
            String feature_name = featureName_[i].trim();
            String feature_val = featureValue_[i].trim();

            if ("1".equals(isRealValue_[i].trim())) {
                Key = feature_name.concat(NaiveBayesTrainer.seprator_).concat(class_val);
                value = new NaiveBayesModelRow(true, new Long(classValueCount_[i].trim()).longValue(), new Long(numSamples_),
                        new Float(mean_[i]), new Float(variance_[i]));
            } else {
                Key = feature_val.concat(NaiveBayesTrainer.seprator_).concat(class_val);
                value = new NaiveBayesModelRow(false,new Long(featureValueCount_[i].trim()).longValue() ,new Long(classValueCount_[i].trim()).longValue()
                        ,new Long(numSamples_).longValue());
            }
            naiveBayesModel_.put(Key, value);
        }
    }

    @Override
    public void map(LongWritable key, Text sample, Context context) throws InterruptedException, IOException {

       Iterator<String> iterator = classLabels.iterator();
    float maxClassProp = -1;
        String predictedClassLabel = null;
        StringBuilder theProbs = new StringBuilder(",");
        while (iterator.hasNext())
        {
            String classLabel = iterator.next().trim();
            float prior =    (new Long(distinctClassValues.get(classLabel))/(float)numSamples_);
            float f = compClassprob(sample,classLabel)*prior;
            theProbs.append(f).append(",");
            if (f > maxClassProp)
            {
                maxClassProp = f;
                predictedClassLabel = classLabel;
            }
            if (f == -1) return;

        }
        StringBuilder blah = new StringBuilder(sample.toString()).append(",").append(predictedClassLabel).append(theProbs.toString());

        context.write(new Text(blah.toString()),null);

    }


    /**
     * Computes the probability of sample based on a class
     *
     * @param sample
     * @param class_val
     * @return
     */
    private float compClassprob(Text sample, String class_val) {
        List<String[]> columnValuesList = helper.getCleanData(sample, false);
        float prob = 1;
        if (columnValuesList == null) return -1;
            for (String[] columnValues : columnValuesList) {
                if (columnValues != null) {
                    Iterator<Integer> columnIterator = helper.getInvolvedColumnIds().iterator();
                    while(columnIterator.hasNext())
                    {
                        int i = columnIterator.next().intValue(); //this gets the indep column id
                        boolean isReal= ("1".equals(helper.getConfigArray(NaiveBayesKeySet.isRealValue_)[i].trim())?true:false);

                        String feature_val = columnValues[i].trim();
                        String feature_name = helper.getColumnNames()[i].trim();

                        if (!isReal) { // this is a categorical data
                            String key = new StringBuilder(feature_val.concat(NaiveBayesTrainer.seprator_).concat(class_val)).toString();
                            NaiveBayesModelRow row = naiveBayesModel_.get(key);
                            if (row != null) {

                                prob *= ((float)row.featureValCount_ + 1) / row.classValCount_;
                            } else { // applying the Laplace smoothing
                                prob *= 1 / numSamples_;
                            }
                        } else { // this is a real-valued feature
                            String key = new StringBuilder(feature_name).append(NaiveBayesTrainer.seprator_).append(class_val).toString();
                            NaiveBayesModelRow row = naiveBayesModel_.get(key);
                            if (row != null) {
                                float newprob = evalGaussian(row.mean_, row.variance_, new Float(columnValues[i]).floatValue());
                                prob *=  newprob;
                            } else { // this
                                throw new IllegalStateException(row.toString().concat(" NOT found in the Naive Bayes model which is weird!"));
                            }
                        }
                    }




    }




    }


        return prob;



}
}