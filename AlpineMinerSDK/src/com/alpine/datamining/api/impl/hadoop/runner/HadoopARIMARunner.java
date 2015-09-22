package com.alpine.datamining.api.impl.hadoop.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopTimeSeriesConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.ARIMAHadoopModel;
import com.alpine.datamining.api.impl.hadoop.models.SingleARIMAHadoopModel;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.TimeSeriesKeySet;
import com.alpine.hadoop.timeseries.GroupingComparator;
import com.alpine.hadoop.timeseries.KeyComparator;
import com.alpine.hadoop.timeseries.LongSort;
import com.alpine.hadoop.timeseries.TimeSeriesMapper;
import com.alpine.hadoop.timeseries.TimeSeriesReducer;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
/**
 * @author Shawn,Peter
 * 
 * 
 */
public class HadoopARIMARunner extends AbstractHadoopRunner{

	private static Logger itsLogger = Logger.getLogger(HadoopARIMARunner.class);
	HadoopHDFSFileManager hdfsManager = HadoopHDFSFileManager.INSTANCE;
	Configuration timeSeriesJobConf = new Configuration();
	HadoopTimeSeriesConfig config = null;
	private String modelFilePath=null;
	public HadoopARIMARunner(AnalyticContext analyticContext,String operatorName){
		super(analyticContext,operatorName);
	}
	private HashMap<String,ArrayList<String>> modelResult= new HashMap<String,ArrayList<String>>();
	@Override
	public int run(String[] args) throws Exception {
		Job sortJob = createJob(HadoopConstants.JOB_NAME.TimeSeries_Sort, timeSeriesJobConf, 
				TimeSeriesMapper.class, TimeSeriesReducer.class, Text.class, Text.class, inputFileFullName, modelFilePath);
		sortJob.setSortComparatorClass(KeyComparator.class);
		sortJob.setGroupingComparatorClass(GroupingComparator.class);
		sortJob.setMapOutputKeyClass(LongSort.class);
		sortJob.setMapOutputValueClass(Text.class);
		sortJob.setInputFormatClass(TextInputFormat.class);
		sortJob.setOutputFormatClass(TextOutputFormat.class);
		super.setInputFormatClass(sortJob) ;
		runMapReduceJob(sortJob,true);
		badCounter=sortJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
		return 0;
	}

	@Override
	public Object runAlgorithm(AnalyticSource source) throws Exception {
		init((HadoopAnalyticSource) source);
		modelFilePath=tmpPath + "timeseries" + System.currentTimeMillis();
		initConf((HadoopAnalyticSource) source);
		String[] args=null;
		try {
			ToolRunner.run(this, args);
			String uri = modelFilePath + reducedFile;
			List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(uri,
					hadoopConnection, 0);
			for (String line : lineList) {
				String key=line.split("\t")[0];
				String value=line.split("\t")[1];
				if(modelResult.get(key)==null){
					ArrayList<String> modelResolv=new ArrayList<String>();
					modelResolv.add(value);
					modelResult.put(key, modelResolv);
				}
				else{
					modelResult.get(key).add(value);
				}
			}

			ARIMAHadoopModel resultModel=new ARIMAHadoopModel();
			for(String groupkey:modelResult.keySet()){
				ArrayList<String> modelResolv=modelResult.get(groupkey);
				resultModel.getModels().add(modelGenerator(modelResolv,groupkey));
				resultModel.setIdColumnName(config.getIdColumn());
				resultModel.setGroupColumnName(config.getGroupColumn());
				resultModel.setValueColumnName(config.getValueColumn());
			}
			return resultModel;
		} catch (Exception e) {
			throw e;
		}
		finally{
			deleteTemp();
		}
	}
	
	private void initConf(HadoopAnalyticSource source) throws AnalysisException {
		config=(HadoopTimeSeriesConfig) source.getAnalyticConfig();
		String id_column = config.getIdColumn();
		String value_column = config.getValueColumn();
		String group_column = config.getGroupColumn();
		Long length_of_window=Long.parseLong(config.getLengthOfWindow())<AlpineDataAnalysisConfig.ARIMA_MAX_COUNT?Long.parseLong(config.getLengthOfWindow()):AlpineDataAnalysisConfig.ARIMA_MAX_COUNT;
		int id_column_index = fileStructureModel.getColumnNameList().indexOf(
				id_column);
		int value_column_index = fileStructureModel.getColumnNameList().indexOf(
				value_column); 
		int group_column_index =0;
		if(group_column!=null){
			group_column_index = fileStructureModel.getColumnNameList().indexOf(
				group_column);
		} 
		else{
			group_column_index=-1;
		}
		super.initHadoopConfig(timeSeriesJobConf,HadoopConstants.JOB_NAME.TimeSeries_Sort);
		timeSeriesJobConf.set(TimeSeriesKeySet.id, id_column_index+"");
		timeSeriesJobConf.set(TimeSeriesKeySet.value,value_column_index+"");
		timeSeriesJobConf.set(TimeSeriesKeySet.groupby,group_column_index+"");
		timeSeriesJobConf.set(TimeSeriesKeySet.autoregressive,config.getP());
		timeSeriesJobConf.set(TimeSeriesKeySet.movingaverage,config.getQ());
		timeSeriesJobConf.set(TimeSeriesKeySet.integrated,config.getD());
		timeSeriesJobConf.set(TimeSeriesKeySet.lengthOfWindow, length_of_window+"");
		timeSeriesJobConf.set(TimeSeriesKeySet.timeFormat, config.getTimeFormat());
		timeSeriesJobConf.set(TimeSeriesKeySet.lastData, AlpineDataAnalysisConfig.ARIMA_LAST_DATA_COUNT+"");
	}
	private SingleARIMAHadoopModel modelGenerator(ArrayList<String> modelResolv, String groupkey) throws Exception{
		double[] bestPhi = null;
		double[] bestTheta = null;
		double intercept = 0;
		double[] varCoef = null;
		double sigma2 = 0;
		int ncxreg = 0;
		double likelihood = 0;
		double[] data = null;
		double[] residuals = null;
		int[] arma = null;
		long interval = 0;
		double[] id=null;
		com.alpine.datamining.operator.timeseries.MakeARIMARet model=new com.alpine.datamining.operator.timeseries.MakeARIMARet();
		for(String result:modelResolv){
			String key=result.split(":")[0];
			String value=result.split(":")[1];
			
			if(key.equals("error"))
			{
				itsLogger.error("ID column Must be distinct");

				throw new Exception(WrongUsedException.getErrorMessage(  AlpineAnalysisErrorName.ID_NOT_DISTINCT,null));
			}
			else if(key.equals("small")){
				itsLogger.error("This group size is too small for arima");

				throw new Exception(WrongUsedException.getErrorMessage(  AlpineAnalysisErrorName.ARIMA_DATASET_TOO_SAMLL_GROUP,new Object[]{groupkey}));
			}
			
			if(key.equals("bestPhi")){
				bestPhi=stringToArray(value);
			}
			if(key.equals("bestTheta")){
				bestTheta=stringToArray(value);
			}
			if(key.equals("VarCoef")){
				varCoef=stringToArray(value);
			}
			if(key.equals("data")){
				data=stringToArray(value);
			}
			if(key.equals("residuals")){
				residuals=stringToArray(value);
			}
			if(key.equals("arma")){
				arma=stringToArrayI(value);
			}
			if(key.equals("intercept")){
				intercept=Double.parseDouble(value);
			}
			if(key.equals("sigma2")){
				sigma2=Double.parseDouble(value);
			}
			if(key.equals("ncxreg")){
				ncxreg=Integer.parseInt(value);
			}
			if(key.equals("likelihood")){
				likelihood=Double.parseDouble(value);
			}
			if(key.equals("MakeARIMARet.A")){
				model.setA(stringToArray(value));
			}
			if(key.equals("MakeARIMARet.Coefs")){
				model.setCoefs(stringToArray(value));
			}
			if(key.equals("MakeARIMARet.Delta")){
				model.setDelta(stringToArray(value));
			}
			if(key.equals("MakeARIMARet.Phi")){
				model.setPhi(stringToArray(value));
			}
			if(key.equals("MakeARIMARet.Z")){
				model.setZ(stringToArray(value));
			}
			if(key.equals("MakeARIMARet.P")){
				String[] ps=value.split(";");
				double[][] p=new double[ps.length][];
				for(int i=0;i<ps.length;i++){
					p[i]=stringToArray(ps[i]);
				}
				model.setP(p);
			}
			if(key.equals("MakeARIMARet.Pn")){
				String[] pns=value.split(";");
				double[][] pn=new double[pns.length][];
				for(int i=0;i<pns.length;i++){
					pn[i]=stringToArray(pns[i]);
				}
				model.setPn(pn);
			}
			if(key.equals("MakeARIMARet.T")){
				String[] ts=value.split(";");
				double[][] t=new double[ts.length][];
				for(int i=0;i<ts.length;i++){
					t[i]=stringToArray(ts[i]);
				}
				model.setT(t);
			}
			if(key.equals("MakeARIMARet.V")){
				String[] vs=value.split(";");
				double[][] v=new double[vs.length][];
				for(int i=0;i<vs.length;i++){
					v[i]=stringToArray(vs[i]);
				}
				model.setV(v);
			}
			if(key.equals("MakeARIMARet.H")){
				model.setH(Double.parseDouble(value));
			}
			if(key.equals("idData")){
				id=stringToArray(value);
			}
			if(key.equals("interval")){
				interval=Long.parseLong(value);
			}
		}
		SingleARIMAHadoopModel singleModel=new 	SingleARIMAHadoopModel(config.getIdColumn(),config.getValueColumn(),Integer.parseInt(config.getP()),
				Integer.parseInt(config.getD()),Integer.parseInt(config.getQ()),
				bestPhi, bestTheta, intercept,varCoef,data,residuals,0.0,arma,model,sigma2,ncxreg,likelihood);
		singleModel.setInterval(interval);
		singleModel.setTrainLastIDData(id);
		singleModel.setGroupColumnValue(groupkey);
		singleModel.setGroupColumnName(config.getGroupColumn());
		singleModel.setIdColumnName(config.getIdColumn());
		singleModel.setGroupColumnName(config.getGroupColumn());
		singleModel.setValueColumnName(config.getValueColumn());
		singleModel.setTrainLastData(data);
		singleModel.setFormatType(config.getTimeFormat());
		return singleModel;
	}
	private double[] stringToArray(String arrayString){
		if(arrayString.indexOf("]")==-1){
			arrayString=arrayString.substring(arrayString.indexOf("[")+1);
		}
		else arrayString=arrayString.substring(arrayString.indexOf("[")+1,arrayString.indexOf("]"));
		String[] array=arrayString.split(",");
		double[] result=new double[array.length];
		for(int i=0;i<array.length;i++){
			try{
				result[i]=Double.parseDouble(array[i].trim());
			}
			catch(NumberFormatException e){
				result[i]=Double.NaN;
			}
		}
		return result;
	}
	private int[] stringToArrayI(String arrayString){
		if(arrayString.indexOf("]")==-1){
			arrayString=arrayString.substring(arrayString.indexOf("[")+1);
		}
		else arrayString=arrayString.substring(arrayString.indexOf("[")+1,arrayString.indexOf("]"));
		String[] array=arrayString.split(",");
		int[] result=new int[array.length];
		for(int i=0;i<array.length;i++){
			try{
				result[i]=Integer.parseInt(array[i].trim());
			}
			catch(NumberFormatException e){
				result[i]=-1;
			}
		}
		return result;
	}
}
