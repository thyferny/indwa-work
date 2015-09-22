/**
 * ClassName ROCDataGeneratorGeneral.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-17
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.hadoop.runner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.operator.evaluator.DoubleListData;
import com.alpine.hadoop.RocKeySet;
import com.alpine.hadoop.roc.FindMinMaxCombiner;
import com.alpine.hadoop.roc.FindMinMaxMapper;
import com.alpine.hadoop.roc.FindMinMaxReducer;
import com.alpine.hadoop.roc.RocCombiner;
import com.alpine.hadoop.roc.RocMapper;
import com.alpine.hadoop.roc.RocReducer;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

/**
 * Helper class containing some methods for ROC plots, threshold finding and
 * area under curve calculation.
 * 
 */
public class HadoopLiftDataGeneratorRunner extends AbstractHadoopRunner {
	/** Defines the maximum amount of points which is plotted in the ROC curve. */
	public static final int MAX_LIFT_POINTS = 200;

	Configuration maxMinConf = new Configuration();
	Configuration rocConf = new Configuration();

	protected String resultsName;
	HadoopHDFSFileManager hdfsManager = HadoopHDFSFileManager.INSTANCE;
	String maxPi;
	String minPi;

	protected String resultLocaltion;

	protected HadoopPredictorConfig config;
	protected String outputTempName;
	protected String outputFileFullName;

	protected long timestamp=System.currentTimeMillis();
	protected String rocPredictFileName = null;
	protected String rocTempFileName = null;
	protected String rocDataFile = null;

	private static Logger itsLogger = Logger
			.getLogger(HadoopLiftDataGeneratorRunner.class);

	/**
	 * @param description
	 */
	public HadoopLiftDataGeneratorRunner(AnalyticContext context,String operatorName) {
		super(context,operatorName);
	}

	/**
	 * Creates a list of ROC data points from the given hadoo file. The data set
	 * must have a label column and confidence values for both values, i.e. a
	 * model must have been applied on the data.
	 * @param analysisFileStructureModel 
	 * 
	 * @throws Exception
	 */
	public DoubleListData createLiftData(AnalyticSource source,
			EngineModel eModel, String piIndex,String goodValue, AnalysisFileStructureModel analysisFileStructureModel) throws Exception {

		init((HadoopAnalyticSource) source);
		this.fileFormatHelper=createFileFormatHelper(analysisFileStructureModel);

		List<double[]> tableData = new LinkedList<double[]>();
		
		double sum = 0;
		double tp = 0;
		setMaxMinJobValue(piIndex);
		setRocJobValue(goodValue, piIndex);
		String[] args = null;

		try {
			ToolRunner.run(this, args);

		
			List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(rocDataFile
					+ reducedFile, hadoopConnection, 1);
			String yAndPi = lineList.get(0);
			yAndPi = yAndPi.split("\t")[0];
			yAndPi = yAndPi.substring(1, yAndPi.length() - 1);
			String[] yAndPiArray = yAndPi.split(",");
			double sumTotal=Double.parseDouble(yAndPiArray[MAX_LIFT_POINTS-1]);
			double pTotal = Double.parseDouble(yAndPiArray[2*MAX_LIFT_POINTS-1]);

			if (sumTotal == 0)
			{
				sumTotal += 0.001;
			}
			double rateTotal = pTotal / (sumTotal);
			if (rateTotal == 0)
			{
				rateTotal += 0.00001;
			}
			
			for (int i = 0; i < MAX_LIFT_POINTS; i++) {
				sum = Double.parseDouble(yAndPiArray[i]);
				tp = Double.parseDouble(yAndPiArray[MAX_LIFT_POINTS + i]);
				if (sum == 0)
				{
					sum += 0.001;
				}
				double ratio = sum/sumTotal;
				double lift = tp / (sum) / (rateTotal);
				if ( i == 1)
				{
					if (ratio > 1.0/MAX_LIFT_POINTS)
					{
						tableData.add(new double[] {1.0/MAX_LIFT_POINTS , lift });
					}
				}
				tableData.add(new double[] {ratio , lift });
			}
			tableData.add(new double[] {1.0 , 1.0 });
			DoubleListData listData = new DoubleListData(tableData);
			listData.setSourceName(eModel.getName());
			return listData;
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
		finally{
			deleteTemp();
		}
	}

	public int run(String[] args) throws Exception {

		Job maxMinJob = createJob(HadoopConstants.JOB_NAME.Max_Min_Job, maxMinConf, 
				FindMinMaxMapper.class, FindMinMaxReducer.class, Text.class, Text.class, inputFileFullName, rocTempFileName);
		maxMinJob.setCombinerClass(FindMinMaxCombiner.class);
		maxMinJob.setMapOutputKeyClass(LongWritable.class);
		maxMinJob.setMapOutputValueClass(DoubleWritable.class);
		super.setInputFormatClass(maxMinJob) ;
		runMapReduceJob(maxMinJob,true);

		List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList4All(rocTempFileName
				+ reducedFile, hadoopConnection);
		
		HashMap<String, String> iterationMap = new HashMap<String, String>();
		for (String line : lineList) {
			iterationMap.put(line.split("\t")[0], line.split("\t")[1]);
		}
		maxPi = iterationMap.get(RocKeySet.max_roc_probability);
		minPi = iterationMap.get(RocKeySet.min_roc_probability);
		rocConf.set(RocKeySet.max_roc_probability, maxPi);
		rocConf.set(RocKeySet.min_roc_probability, minPi);

		Job rocJob = createJob(HadoopConstants.JOB_NAME.Lift_DataGenerator, rocConf, 
				RocMapper.class, RocReducer.class, Text.class, Text.class, inputFileFullName, rocDataFile);
		
		rocJob.setCombinerClass(RocCombiner.class);
		rocJob.setMapOutputKeyClass(LongWritable.class);
		rocJob.setMapOutputValueClass(DoubleArrayWritable.class);
		super.setInputFormatClass(rocJob) ;
		runMapReduceJob(rocJob,true);

		return 0;
	}

	private void setMaxMinJobValue(String piIndex)
			throws AnalysisException {
		super.initHadoopConfig(maxMinConf,HadoopConstants.JOB_NAME.Max_Min_Job);
		maxMinConf.set(RocKeySet.piIndex, piIndex);
	}

	private void setRocJobValue(String goodValue, String piIndex)
			throws AnalysisException {
		super.initHadoopConfig(rocConf,HadoopConstants.JOB_NAME.ROC_DataGenerator);
		rocConf.set(RocKeySet.piIndex, piIndex);
		rocConf.set(RocKeySet.good,goodValue);
		String depend = ((EvaluatorConfig) hadoopSource.getAnalyticConfig())
				.getDependentColumn();
		int dependIndex = fileStructureModel.getColumnNameList()
				.indexOf(depend);
		rocConf.set(RocKeySet.dependent, dependIndex + "");
		rocConf.set(RocKeySet.max_roc_points, MAX_LIFT_POINTS + "");
	}

	protected void init(HadoopAnalyticSource hadoopSource) throws Exception {
		super.init(hadoopSource) ;
		
		rocPredictFileName = tmpPath + "PredictFile" + timestamp;
		rocTempFileName = tmpPath + "rocMaxMinFile" + timestamp;
		rocDataFile = tmpPath + "rocDataFile" + timestamp;
		
		if (!StringUtil.isEmpty(resultLocaltion)
				&& resultLocaltion.endsWith(HadoopFile.SEPARATOR) == false) {
			resultLocaltion = resultLocaltion + HadoopFile.SEPARATOR;
		}
		outputFileFullName = resultLocaltion + resultsName;

		dropIfExists(rocPredictFileName);
		dropIfExists(rocTempFileName);
		dropIfExists(rocDataFile);
	}

	@Override
	public Object runAlgorithm(AnalyticSource source) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
