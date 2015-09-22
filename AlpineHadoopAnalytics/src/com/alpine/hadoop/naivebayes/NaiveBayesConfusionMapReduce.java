package com.alpine.hadoop.naivebayes;

import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.conversion.Stringifier;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class NaiveBayesConfusionMapReduce
{
    public static final String seprator_ = "___";


    public static class NaiveBayesConfusionMapper extends Mapper<LongWritable, Text, Text, Text> {


        MapReduceHelper helper;
        Map<String,String> distinctClassValues;

        ArrayList<String> columnsIdString = new ArrayList<String>();
        private static String dependentColumn_;

        private static HashMap<String, NaiveBayesModelRow> naiveBayesModel_;
        private static long numSamples_;

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
            //in this case, we need the dependent id as well.
            involvedColumnIds.add(helper.getDependentId(NaiveBayesKeySet.dependent));
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
            dependentColumn_ = helper.getConfigString(NaiveBayesKeySet.dependent);
            Stringifier<String,String> stringifier= new Stringifier<String,String>();

            distinctClassValues = new HashMap<String,String>();

            stringifier.stringToMap(helper.getConfigString(NaiveBayesKeySet.distinctClassValues_), distinctClassValues);

            classLabels = new HashSet<String>();
            classLabels.addAll(Arrays.asList(classValue_));

            numSamples_ = new Long(numSamples).longValue();
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
            Prediction f = null;
            while (iterator.hasNext())
            {
                String classLabel = iterator.next().trim();
                float prior = (new Long(distinctClassValues.get(classLabel))/(float)numSamples_);
                f = compClassprob(sample,classLabel);
                if (f== null) return;
                f.prob *= prior;
                if (f.prob > maxClassProp)
                {
                    maxClassProp = f.prob;
                    predictedClassLabel = classLabel;
                }
                if (f.prob == -1) return;
            }
            /*the key is going to the actual class*/
            /*value is going to actual_{0,1}_predicted{0,1}*/
            if (f.actual_.equals(predictedClassLabel)){
                context.write(new Text(f.actual_),new Text(new StringBuilder(f.actual_).append(",").append("1").toString()));
            } else {
                context.write(new Text(f.actual_),new Text(new StringBuilder(f.actual_).append(",").append("0").
                        append(seprator_).append(predictedClassLabel).append(",").append("1").toString()));
            }

        }


        /**
         * Computes the probability of sample based on a class
         *
         * @param sample
         * @param class_val
         * @return
         */
        private Prediction compClassprob(Text sample, String class_val) {
            List<String[]> columnValuesList = helper.getCleanData(sample, false);
            float prob = 1;
            if (columnValuesList == null) return null;

            String[] ColumnNames = helper.getColumnNames();
            int dependedCol_ind = helper.getDependentId(NaiveBayesKeySet.dependent);

            Prediction pred = new Prediction();

            for (String[] columnValues : columnValuesList) {
                if (columnValues != null) {
                    Iterator<Integer> columnIterator = helper.getInvolvedColumnIds().iterator();
                    while(columnIterator.hasNext())
                    {
                        int i = columnIterator.next().intValue(); //this gets the indep column id


                        if (i == dependedCol_ind){
                            pred.actual_ = columnValues[i];
                        }  else{
                            boolean isReal= ("1".equals(helper.getConfigArray(NaiveBayesKeySet.isRealValue_)[i].trim())?true:false);
                            String feature_val = columnValues[i].trim();
                            String feature_name = helper.getColumnNames()[i].trim();


                            if (!isReal) { // this is a categorical data
                                String key = new StringBuilder(feature_val.concat(NaiveBayesTrainer.seprator_).concat(class_val)).toString();
                                NaiveBayesModelRow row = naiveBayesModel_.get(key);
                                if (row != null) {
                                    prob *= ((float)row.featureValCount_ +1) / row.classValCount_;
                                } else { // applying the Laplace smoothing
                                    prob *= 1 / numSamples_;
                                }
                            } else { // this is a real-valued feature
                                String key = new StringBuilder(feature_name).append(NaiveBayesTrainer.seprator_).append(class_val).toString();
                                NaiveBayesModelRow row = naiveBayesModel_.get(key);
                                if (row != null) {
                                    prob *= evalGaussian(row.mean_, row.variance_, new Float(columnValues[i]).floatValue());
                                } else { // this
                                    throw new IllegalStateException(row.toString().concat(" NOT found in the Naive Bayes model which is weird!"));
                                }
                            }
                        }


                    }






                }

            }


            pred.predicted_ = class_val;
            pred.prob = prob;
            return pred;




        }

        private static class Prediction{
            String actual_;
            float prob;
            String predicted_;
        }

    }



    public static class NaiveBayesTrainingReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context) throws IOException, InterruptedException {

            HashMap<String,Long> conf_mx_row = new HashMap<String,Long>();

            while (values.iterator().hasNext()){
                String Line = values.iterator().next().toString();
                String[] lineSplit= Line.split(seprator_);  //will be array of "Label, value"
                for (int i =0; i < lineSplit.length; i++)
                {
                    String[] eachValue = lineSplit[i].split(",");
                    long previous= 0;
                    if (conf_mx_row.get(eachValue[0]) != null)
                    {
                        previous =  conf_mx_row.get(eachValue[0]);
                    }
                    conf_mx_row.put(eachValue[0], previous + new Long(eachValue[1]));
                }

            }
            StringBuilder builder = new StringBuilder() ;
            for (Map.Entry<String, Long> entry : conf_mx_row.entrySet()) {
                String newkey = entry.getKey();
                Long value = entry.getValue();
                builder.append(newkey).append(",").append(value).append(seprator_);

            }


            context.write(key,new Text(builder.toString()));

        }



    }

}