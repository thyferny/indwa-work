package com.alpine.datamining.api.impl.hadoop.runner;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.NaiveBayesHadoopModel;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.evaluator.ConfusionMatrix;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.naivebayes.NaiveBayesConfusionMapReduce;
import com.alpine.hadoop.naivebayes.NaiveBayesPredictorMapper;
import com.alpine.hadoop.utily.conversion.Stringifier;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class HadoopNaiveBayesConfusionRunner extends AbstractHadoopRunner {
    Configuration predictConf = new Configuration();

    protected String resultLocaltion;

    protected EvaluatorConfig config;
    protected String outputFileFullName;
    private String confusionFileName;
    private String inputFileFullName;

    private ConfusionMatrix resultMatrix;

    private static Logger itsLogger = Logger.getLogger(HadoopNaiveBayesPredictRunner.class);
    public HadoopNaiveBayesConfusionRunner(AnalyticContext context,String operatorName) {
        super(context,operatorName);
    }


    public void createConfusionMatrix(AnalyticSource source,EngineModel eModel) throws Exception {

        init((HadoopAnalyticSource)source);
        setPredictConfValues(eModel.getModel());

        try {
            ToolRunner.run(this, null);
        } catch (Exception e) {
            itsLogger.error(e);
            throw new AnalysisException(e);
        }
        finally{
            deleteTemp();
        }

    }

    @Override
    public Object runAlgorithm(AnalyticSource source) throws Exception {
        return null;
    }


    protected void init(HadoopAnalyticSource hadoopSource) throws Exception{
        super.init(hadoopSource) ;
        config = (EvaluatorConfig) hadoopSource.getAnalyticConfig();

        confusionFileName  = tmpPath + "PredictFile";
        if (!StringUtil.isEmpty(resultLocaltion)
                && resultLocaltion.endsWith(HadoopFile.SEPARATOR) == false) {
            resultLocaltion = resultLocaltion + HadoopFile.SEPARATOR;
        }
        outputFileFullName = resultLocaltion + confusionFileName + System.currentTimeMillis();
        inputFileFullName = hadoopSource.getFileName();

//        setMapReduceCompress(outputFileFullName, predictConf);
    }

    private void setPredictConfValues(Model model) throws AnalysisException, IOException {
        super.initHadoopConfig(predictConf,HadoopConstants.JOB_NAME.NaiveBayesPredictor);
        NaiveBayesHadoopModel nModel = (NaiveBayesHadoopModel) model;

        Stringifier<String,Long > stringifier = new Stringifier<String, Long>();

        /*putting the model object on the conf to be loaded in the map-red process later*/
        predictConf.setLong(NaiveBayesKeySet.numSamples_, nModel.getNumSamples_());
        predictConf.set(NaiveBayesKeySet.isRealValue_,Arrays.toString(nModel.getRealValue_().toArray()));
        predictConf.set(NaiveBayesKeySet.featureName_,Arrays.toString(nModel.getFeatureName_().toArray()));
        predictConf.set(NaiveBayesKeySet.featureValue_, Arrays.toString(nModel.getFeatureValue_().toArray()));
        predictConf.set(NaiveBayesKeySet.className_,  Arrays.toString(nModel.getClassName_().toArray()));
        predictConf.set(NaiveBayesKeySet.classValue_, Arrays.toString(nModel.getClassValue_().toArray()));
        predictConf.set(NaiveBayesKeySet.featureValueCount_, Arrays.toString(nModel.getFeatureValueCount_().toArray()));
        predictConf.set(NaiveBayesKeySet.classValueCount_,Arrays.toString(nModel.getClassValueCount_().toArray()));
        predictConf.set(NaiveBayesKeySet.mean_, Arrays.toString(nModel.getMean_().toArray()));
        predictConf.set(NaiveBayesKeySet.variance_, Arrays.toString(nModel.getVariance_().toArray()));
        predictConf.set(NaiveBayesKeySet.dependent, nModel.getDependentcolumnName_());

        predictConf.set(NaiveBayesKeySet.distinctClassValues_, stringifier.mapToString(nModel.getDistinctClassValues_()));

        if(nModel.getColumnNames()!=null){
            predictConf.set(NaiveBayesKeySet.columns, nModel.getColumnNames());
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        Job predictJob=new Job(predictConf);
        FileInputFormat.addInputPath(predictJob, new Path(inputFileFullName));
        FileOutputFormat.setOutputPath(predictJob, new Path(outputFileFullName));
        predictJob.setJobName(HadoopConstants.JOB_NAME.NaiveBayesConfusion);
        getContext().registerMapReduceJob(predictJob) ;
        predictJob.setMapperClass(NaiveBayesConfusionMapReduce.NaiveBayesConfusionMapper.class);
        predictJob.setReducerClass(NaiveBayesConfusionMapReduce.NaiveBayesTrainingReducer.class);
        predictJob.setCombinerClass(NaiveBayesConfusionMapReduce.NaiveBayesTrainingReducer.class);
        predictJob.setInputFormatClass(TextInputFormat.class);
        predictJob.setOutputFormatClass(TextOutputFormat.class);
        predictJob.setOutputKeyClass(Text.class);
        predictJob.setOutputValueClass(Text.class);
        super.setInputFormatClass(predictJob) ;
        runMapReduceJob(predictJob,true);
        badCounter=predictJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
        resultMatrix = GenerateConfusionMXStructure(predictJob);
      //  List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(rocDataFile
      //          + reducedFile, hadoopConnection, 1);
        return 0;



//        FileInputFormat.addInputPath(job, new Path(input_));
//        FileOutputFormat.setOutputPath(job, new Path(output));
//        FileSystem.get(conf).delete(new Path(output));


    }
    public ConfusionMatrix getResults()
    {
        return resultMatrix;
    }


    /**
     * this is a helper method that creates a confusion matrix from the output
     * of the map-reduce job
     * @param job
     * @throws IOException
     */
    public ConfusionMatrix GenerateConfusionMXStructure(Job job) throws IOException {

        FileSystem Fs = FileSystem.get(job.getConfiguration());
        String fileName = "Conf_MX" + System.nanoTime() + ".txt";
        FileUtil.copyMerge(Fs, new Path(outputFileFullName), Fs, new Path(fileName), true, job.getConfiguration(), "");
        BufferedReader bfr = new BufferedReader(new InputStreamReader(Fs.open(new Path(fileName))));

        HashMap<String,HashMap<String,Long>> confMX_ = new HashMap<String,HashMap<String,Long>>();
        ArrayList<String> class_label_indx = new ArrayList<String>();
        int count = 0;
        for (String line = bfr.readLine(); line != null; line = bfr.readLine()) {
            String[] fields =line.split("\t"); // this is not a safe code! (AD)
            String class_label = fields[0];
            fields = fields[1].split(NaiveBayesConfusionMapReduce.seprator_);
            HashMap<String,Long> mx_row = new HashMap<String, Long>();
            for (int i = 0; i < fields.length; i++){
                mx_row.put(fields[i].split(",")[0], new Long(fields[i].split(",")[1]));
            }
            confMX_.put(class_label,mx_row);
            class_label_indx.add(class_label);

        }
        bfr.close();
        Fs.delete(new Path(outputFileFullName), true);
        Fs.delete(new Path(fileName),true);

        long [][] Matrix =  new long[class_label_indx.size()][class_label_indx.size()];
        for (int i=0; i< class_label_indx.size(); i++)
            for (int j=0; j< class_label_indx.size(); j++){
                Matrix[i][j] = (long)0;
            }


        for (int ind_itr = 0; ind_itr < class_label_indx.size(); ind_itr++){
            HashMap<String, Long> mx_row = confMX_.get(class_label_indx.get(ind_itr));
            for (Map.Entry<String, Long> entry2 : mx_row.entrySet()){


                Matrix[ind_itr][class_label_indx.indexOf(entry2.getKey())] = entry2.getValue();
            }
        }

        HashMap<String,Float> classes_precision = new HashMap<String, Float>();
        HashMap<String,Float> classes_recall = new HashMap<String, Float>();
        /*Computing the precision and recall*/
        long total_TP = 0; // used for computing the general accuracy
        long num_samples = 0;
        for (int ind_itr = 0; ind_itr < class_label_indx.size(); ind_itr++){
            String class_label = class_label_indx.get(ind_itr);
            long TP = Matrix[ind_itr][ind_itr];
            total_TP += TP;
            long FP = (long)0;
            long FN = (long)0;
            for (int i = 0; i < class_label_indx.size(); i++){
                num_samples += Matrix[ind_itr][i]; // sum of the elements in the matrix represents the num samples
                if ( i != ind_itr ){
                    FP += Matrix[i][ind_itr];
                    FN += Matrix[ind_itr][i];
                }
            }
            float precision = (TP +FP ==0)?0:(float)TP / (TP + FP);
            float recall = (TP +FN ==0)?0:(float)TP /(TP + FN);
            classes_precision.put(class_label,precision);
            classes_recall.put(class_label,recall);
        }

        ConfusionMatrix conf_mx = new ConfusionMatrix(Matrix,class_label_indx,classes_precision,classes_recall,(((float)total_TP)/num_samples));
        return conf_mx;
    }


}
