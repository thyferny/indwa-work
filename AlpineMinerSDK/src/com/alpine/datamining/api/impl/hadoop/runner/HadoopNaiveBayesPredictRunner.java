package com.alpine.datamining.api.impl.hadoop.runner;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.NaiveBayesHadoopModel;
import com.alpine.datamining.operator.Model;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.logistic.LogisticPredictorMapper;
import com.alpine.hadoop.naivebayes.NaiveBayesPredictorMapper;
import com.alpine.hadoop.utily.conversion.Stringifier;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public class HadoopNaiveBayesPredictRunner extends AbstractHadoopRunner {
    Configuration predictConf = new Configuration();

    protected String resultLocaltion;

    protected HadoopPredictorConfig config;
    protected String outputFileFullName;

    private static Logger itsLogger = Logger.getLogger(HadoopNaiveBayesPredictRunner.class);
    public HadoopNaiveBayesPredictRunner(AnalyticContext context,String operatorName) {
        super(context,operatorName);
    }

    @Override
    public Object runAlgorithm(AnalyticSource source) throws Exception {
        init((HadoopAnalyticSource)source);
        HadoopPredictorConfig config=(HadoopPredictorConfig)source.getAnalyticConfig();
        Model model = config.getTrainedModel().getModel();  //this is the model data.

        setPredictConfValues(model);

       String[] args =new String[1];

        args[0]=hadoopSource.getFileName();
        HadoopHDFSFileManager hdfsManager=HadoopHDFSFileManager.INSTANCE;
        if(hdfsManager.exists(outputFileFullName, hadoopConnection)){//true===override??
            if(Resources.YesOpt.equals(config.getOverride())){
                hdfsManager.deleteHadoopFile(outputFileFullName, hadoopConnection);
            }
            else{
                AnalysisException e = new AnalysisException("file already exist");
                itsLogger.error(e);
                throw new AnalysisException(e);
            }
        }
        try {
            ToolRunner.run(this, args);
        } catch (Exception e) {
            itsLogger.error(e);
            throw new AnalysisException(e);
        }
        finally{
            deleteTemp();
        }
        return null;
    }


    protected void init(HadoopAnalyticSource hadoopSource) throws Exception{
        super.init(hadoopSource) ;
        config = (HadoopPredictorConfig) hadoopSource.getAnalyticConfig();

        String resultsLocation = ((HadoopPredictorConfig)hadoopSource.getAnalyticConfig()).getResultsLocation();
        String resultsName = ((HadoopPredictorConfig)hadoopSource.getAnalyticConfig()).getResultsName();
        if(!StringUtil.isEmpty(resultsLocation)&&resultsLocation.endsWith(HadoopFile.SEPARATOR)==false){
            resultsLocation=resultsLocation+ HadoopFile.SEPARATOR;
        }
        outputFileFullName = resultsLocation+resultsName;

        setMapReduceCompress(outputFileFullName, predictConf);
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
        FileInputFormat.addInputPath(predictJob, new Path(strings[0]));
        FileOutputFormat.setOutputPath(predictJob, new Path(outputFileFullName));
        predictJob.setJobName(HadoopConstants.JOB_NAME.NaiveBayesPredictor);
        getContext().registerMapReduceJob(predictJob) ;
        predictJob.setJarByClass(NaiveBayesPredictorMapper.class);
        predictJob.setMapperClass(NaiveBayesPredictorMapper.class);

        predictJob.setInputFormatClass(TextInputFormat.class);
        predictJob.setOutputFormatClass(TextOutputFormat.class);
        predictJob.setNumReduceTasks(0);
        predictJob.setOutputKeyClass(Text.class);
        predictJob.setOutputValueClass(Text.class);
        super.setInputFormatClass(predictJob) ;
        runMapReduceJob(predictJob,true);
        badCounter=predictJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
        return 0;



//        FileInputFormat.addInputPath(job, new Path(input_));
//        FileOutputFormat.setOutputPath(job, new Path(output));
//        FileSystem.get(conf).delete(new Path(output));


    }
}
