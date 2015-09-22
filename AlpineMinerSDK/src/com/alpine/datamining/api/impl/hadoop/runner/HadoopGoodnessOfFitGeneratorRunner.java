package com.alpine.datamining.api.impl.hadoop.runner;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.datamining.operator.evaluator.ValueGoodnessOfFit;
import com.alpine.hadoop.GoodnessOfFitKeySet;
import com.alpine.hadoop.goodnessoffit.GoodnessOfFitCombiner;
import com.alpine.hadoop.goodnessoffit.GoodnessOfFitMapper;
import com.alpine.hadoop.goodnessoffit.GoodnessOfFitReducer;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.tools.StringHandler;

public class HadoopGoodnessOfFitGeneratorRunner extends AbstractHadoopRunner {

	protected long timestamp = System.currentTimeMillis();
	protected String gofDataFile = null;
	private static Logger itsLogger = Logger
			.getLogger(HadoopGoodnessOfFitGeneratorRunner.class);
	HadoopHDFSFileManager hdfsManager = HadoopHDFSFileManager.INSTANCE;
	private Configuration gofConf = new Configuration();

	public ArrayList<String> dependValues = new ArrayList<String>();
	private String cIndexes;

	public String getcIndexes() {
		return cIndexes;
	}

	public void setcIndexes(String cIndexes) {
		this.cIndexes = cIndexes;
	}

	public HadoopGoodnessOfFitGeneratorRunner(AnalyticContext context,String operatorName) {
		super(context,operatorName);
	}

	public void createGoodnessOfFit(AnalyticSource source,
			GoodnessOfFit goodnessOfFit,
			AnalysisFileStructureModel analysisFileStructureModel)
			throws  Exception {
		init((HadoopAnalyticSource) source);
		this.fileFormatHelper = createFileFormatHelper(analysisFileStructureModel);
		setRGofJobValue();
		String[] args = null;
		try {
			ToolRunner.run(this, args);

			String[] result = null;
			long dataSize = 0;

			String fileString = null;

			List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(gofDataFile + reducedFile, hadoopConnection, 1);
			if(lineList.size()>0){
				fileString=lineList.get(0);
			}
			dataSize = Long.parseLong(fileString.split("\t")[1]);
			String resultString = fileString.split("\t")[0];
			resultString = resultString.substring(1, resultString.length() - 1);
			result = resultString.split(",");
			for (int i = 0; i < result.length; i++) {
				result[i] = result[i].trim();
			}

			int allTrue = 0;
			for (int i = 0; i < dependValues.size(); i++) {
				String targetClass = dependValues.get(i);
				targetClass = StringHandler.escQ(targetClass);
				long actual = 0;
				long predicted = 0;
				long predictedTrue = 0;
				actual = Long.parseLong(result[i * 3]);
				predicted = Long.parseLong(result[i * 3 + 1]);
				predictedTrue = Long.parseLong(result[i * 3 + 2]);
				allTrue += predictedTrue;
				double recall = Double.NaN;
				if (actual != 0) {
					recall = predictedTrue * 1.0 / actual;
				}
				// Precision(Positive Predicted Value,PV+)=true positive/ total
				// predicted positive=d/b+d
				double precision = Double.NaN;
				if (predicted != 0) {
					precision = predictedTrue * 1.0 / predicted;
				}
				double f1 = Double.NaN;
				if (!Double.isNaN(recall) && !Double.isNaN(precision)
						&& recall + precision != 0) {
					f1 = 2 * recall * precision / (recall + precision);
				}
				double specificity = Double.NaN;
				double sensitivity = Double.NaN;
				if (dependValues.size() == 2) {
					if (dataSize - actual != 0) {
						specificity = (dataSize - actual - predicted + predictedTrue)
								* 1.0 / (dataSize - actual);
					}
					if (actual != 0) {
						sensitivity = predictedTrue * 1.0 / actual;
					}
				}
				ValueGoodnessOfFit data = new ValueGoodnessOfFit(targetClass,
						recall, precision, f1, specificity, sensitivity);
				// Specificity (TNR) = (TN) / actually are negative (TN+FP).
				// Sensitivity (TPR) = (TP) / actually are positive (TP+FN).
				goodnessOfFit.getGoodness().add(data);
			}

			double accuracy = Double.NaN;
			if (dataSize != 0) {
				accuracy = allTrue * 1.0 / dataSize;
			}
			goodnessOfFit.setAccuracy(accuracy);
			goodnessOfFit.setError(1 - accuracy);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		} finally {
			deleteTemp();
		}
	}

	@Override
	public int run(String[] arg0) throws Exception {
		dropIfExists(gofDataFile);
		Job gofJob = createJob(HadoopConstants.JOB_NAME.Goodness_Of_Fit,
				gofConf, GoodnessOfFitMapper.class, GoodnessOfFitReducer.class,
				Text.class, Text.class, inputFileFullName, gofDataFile);
		gofJob.setCombinerClass(GoodnessOfFitCombiner.class);
		gofJob.setMapOutputKeyClass(LongWritable.class);
		gofJob.setMapOutputValueClass(DoubleArrayWritable.class);
		super.setInputFormatClass(gofJob);
		runMapReduceJob(gofJob, true);
		return 0;
	}
	
	private void setRGofJobValue()
			throws AnalysisException {
		super.initHadoopConfig(gofConf,HadoopConstants.JOB_NAME.Goodness_Of_Fit);
		String depend = ((EvaluatorConfig) hadoopSource.getAnalyticConfig())
				.getDependentColumn();
		int dependIndex = fileStructureModel.getColumnNameList()
				.indexOf(depend);
		gofConf.set(GoodnessOfFitKeySet.dependent, dependIndex + "");
		gofConf.set(GoodnessOfFitKeySet.cIndex, cIndexes);
		StringBuffer dependString = new StringBuffer();
		for (int i = 0; i < dependValues.size(); i++) {
			if (i == 0) {
				dependString.append(dependValues.get(i).replace(",", "_"));
			} else {
				dependString.append(",").append(
						dependValues.get(i).replace(",", "_"));
			}

		}
		gofConf.set(GoodnessOfFitKeySet.dependValues, dependString.toString());
	}

	protected void init(HadoopAnalyticSource hadoopSource) throws Exception {
		super.init(hadoopSource);
		gofDataFile = tmpPath + "gofDataFile" + timestamp;
	}

	@Override
	public Object runAlgorithm(AnalyticSource source) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
