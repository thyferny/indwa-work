package com.alpine.datamining.api.impl.hadoop.runner;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.NaiveBayesConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.NaiveBayesHadoopModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.VariableSelectionKeySet;
import com.alpine.hadoop.naivebayes.NaiveBayesTrainer;
import com.alpine.hadoop.util.KnuthMorrisPratt;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import com.alpine.hadoop.variableselection.AlphaBetaCombiner;
import com.alpine.hadoop.variableselection.AlphaBetaMapper;
import com.alpine.hadoop.variableselection.AlphaBetaReducer;
import com.alpine.utility.hadoop.HadoopConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HadoopNaiveBayesTrainRunner  extends AbstractHadoopRunner {

    public static final String seprator_ = "___"; // choice of a safe seprator; hopefully no feature value contains this!
    private static final String classCounterPrefix_ = "Class:";
    private static final String intermedOutputPath_ = "NaiveBayes";
    private static final String finaloutputPath_ = "NaiveBayes.model";
    //private static final int dependentColumId = 2;

    private NaiveBayesHadoopModel resultModel;
    private static Logger itsLogger = Logger.getLogger(HadoopNaiveBayesTrainRunner.class);
    protected NaiveBayesConfig config;
    Configuration baseConf = new Configuration();
    private long objectTimeStamp=System.currentTimeMillis();
    private String nbTemp=null;

    public HadoopNaiveBayesTrainRunner(AnalyticContext context,String operatorName) {
        super(context,operatorName);

    }

    @Override
    public int run(String[] strings) throws Exception {
        //config is where all my parameters are stored.  they should be already loaded.
        callNBJob();
        return 0;
    }


    private void callNBJob() throws Exception {
        initConf();
        Configuration betaConf=new Configuration(baseConf);
        super.initHadoopConfig(betaConf, HadoopConstants.JOB_NAME.NaiveBayes);
        nbTemp=tmpPath+"NaiveBayesTemp"+objectTimeStamp;
        Job NBJob = createJob(HadoopConstants.JOB_NAME.NaiveBayes, betaConf,
                NaiveBayesTrainer.NaiveBayesTrainingMapper.class, NaiveBayesTrainer.NaiveBayesTrainingReducer.class, Text.class,Text.class, inputFileFullName, nbTemp);

        NBJob.setCombinerClass(NaiveBayesTrainer.NaiveBayesTrainingCombiner.class);
        super.setInputFormatClass(NBJob) ;
        runMapReduceJob(NBJob,true);
        badCounter=NBJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
        SaveModel(NBJob);
    }

    /**
     * Merges and copies data from hdfs to local file system, overwrites
     * <tt>output</tt>.
     */
    public void SaveModel(Job job) throws IOException {



        FileSystem Fs = FileSystem.get(job.getConfiguration());
        String fileName = "NaiveBayes" + System.nanoTime() + ".model";
        FileUtil.copyMerge(Fs, new Path(nbTemp), Fs, new Path(fileName), true, job.getConfiguration(), "");
        /*now rewritting the file with class and sample stats*/
        if (new File(finaloutputPath_).exists()) {
            new File(finaloutputPath_).delete();
        }
        BufferedReader bfr = new BufferedReader(new InputStreamReader(Fs.open(new Path(fileName))));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(finaloutputPath_));
        Long num_samples = job.getCounters().findCounter("Job_Stats", "Num_Samples").getValue();

        ArrayList<String> isRealValue = new ArrayList<String>();
        ArrayList<String> featureName = new ArrayList<String>();
        ArrayList<String> featureValue = new ArrayList<String>();
        ArrayList<String> className = new ArrayList<String>();
        ArrayList<String> classValue = new ArrayList<String>();
        ArrayList<String> featureValueCount= new ArrayList<String>();
        ArrayList<String> classValueCount= new ArrayList<String>();
        ArrayList<String> mean = new ArrayList<String>();
        ArrayList<String> variance = new ArrayList<String>();

        HashMap<String,Long> distinctClassValuesCount = new HashMap<String,Long>();
        String classNameString = null;
        for (String line = bfr.readLine(); line != null; line = bfr.readLine()) {
            String class_label = getClassVal(line);
            Long label_count = job.getCounters().findCounter("Job_Stats", new StringBuilder(classCounterPrefix_).append(class_label).
                    toString()).getValue();

            ArrayList<Integer> positions = KnuthMorrisPratt.kmp(line, NaiveBayesTrainer.seprator_, 5);

            boolean isReal = NaiveBayesTrainer.isrealValued(line);

            String classValueForRow =  NaiveBayesTrainer.getClassValue(line, positions);
            classNameString = NaiveBayesTrainer.getClassName(line, positions);
            isRealValue.add((isReal?"1":"0"));
            featureName.add(NaiveBayesTrainer.getFeatureName(line, positions));
            featureValue.add(NaiveBayesTrainer.getFeatureValue(line, positions));
            className.add(classNameString);
            classValue.add(classValueForRow);

            if (isReal)
            {
                featureValueCount.add("");
                mean.add(NaiveBayesTrainer.getMean(line, positions));
                variance.add(NaiveBayesTrainer.getVariance(line, positions));
            } else
            {
                featureValueCount.add(NaiveBayesTrainer.getFeatureValCount(line, positions));
                mean.add("");
                variance.add("");
            }
            distinctClassValuesCount.put(classValueForRow,label_count);
            classValueCount.add(label_count.toString());
            bfw.append(line).append(seprator_).append(label_count.toString()).append(seprator_).append(num_samples.toString()).append("\n");
        }
        bfr.close();
        bfw.close();
        Fs.delete(new Path(fileName), true);


        resultModel = new NaiveBayesHadoopModel(num_samples,isRealValue,featureName,featureValue,className,classValue,featureValueCount,classValueCount,mean,variance,distinctClassValuesCount,classNameString);
        resultModel.setColumnNames(config.getColumnNames());
    }


    /*class value is located between the 4th and 5th seprators*/
    public static String getClassVal(String row) {
        ArrayList<Integer> pos = KnuthMorrisPratt.kmp(row, seprator_, 5);
        return row.substring(pos.get(3) + seprator_.length(), pos.get(4));
    }

    @Override
    public Object runAlgorithm(AnalyticSource source) throws  Exception {
        init((HadoopAnalyticSource) source);
        try {
            ToolRunner.run(this, null);
        } catch (Exception e) {
            itsLogger.error(e);
            throw new AnalysisException(e);
        }
        finally{
            deleteTemp();
        }
        return resultModel;
    }


    private void initConf() throws AnalysisException {
            baseConf.set(NaiveBayesKeySet.dependent, config.getDependentColumn());
            if(config.getColumnNames()!=null){
                baseConf.set(NaiveBayesKeySet.columns, config.getColumnNames());
            }

    }

    protected void init(HadoopAnalyticSource hadoopSource) throws Exception {
        super.init(hadoopSource) ;
        config = (NaiveBayesConfig) hadoopSource.getAnalyticConfig();
    }



}
