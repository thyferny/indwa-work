/**
 * 

 * ClassName HadoopARIMATrainer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-11-5
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */

package com.alpine.datamining.api.impl.hadoop.predictor;

import java.io.OutputStream;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.TimeSeriesHadoopPredictorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelPredictor;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.ARIMAHadoopModel;
import com.alpine.datamining.api.impl.hadoop.models.SingleARIMAHadoopModel;
import com.alpine.datamining.api.impl.output.hadoop.HadoopAnalyzerOutPutARIMARPredict;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.fs.HDFSFileCompressHelper;
/**
 * @author Peter
 * 
 * 
 */
public class HadoopTimeSeriesPredictor extends AbstractHadoopModelPredictor{

	private static Logger itsLogger = Logger
			.getLogger(HadoopTimeSeriesPredictor.class);
	@Override
	protected HadoopAnalyzerOutPutARIMARPredict doPredict(HadoopPredictorConfig config)
			throws AnalysisException, Exception {
		HadoopAnalyzerOutPutARIMARPredict outPut;
		HadoopARIMARPredictResult ret=new HadoopARIMARPredictResult();
		EngineModel eModel= config.getTrainedModel();
		ARIMAHadoopModel arimaModel=(ARIMAHadoopModel) eModel.getModel();
		TimeSeriesHadoopPredictorConfig tsConf=(TimeSeriesHadoopPredictorConfig)config;
		
		
		String resultsName = config.getResultsName();
		String resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		String fullPathFileName=resultLocaltion+resultsName;
		ret.setGroupColumnName(arimaModel.getGroupColumnName());
		if(HadoopHDFSFileManager.INSTANCE.exists(fullPathFileName, tsConf.getHadoopInfo())){//true===override??
			if(Resources.YesOpt.equals(config.getOverride())){
				boolean success = HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(fullPathFileName, tsConf.getHadoopInfo());
				if(success==false){
					throw new Exception("Can not delete out put directory "+fullPathFileName);
				}
			}
			else{
				AnalysisException e = new AnalysisException("file already exist");
				itsLogger.error(e);
				throw new AnalysisException(e);
			}
		}
		String line="";
		
		
		OutputStream out =null;
		try {
			FileSystem fs = HadoopHDFSFileManager.INSTANCE.getHadoopFileSystem(tsConf.getHadoopInfo());
//			
			out = HDFSFileCompressHelper.INSTANCE.generateOutputStream(fs, HadoopHDFSFileManager.INSTANCE.getHadoopPath(tsConf.getHadoopInfo(),fullPathFileName));
			writePridictResult(ret, arimaModel, tsConf, fullPathFileName,
					line,out);
	 
		} catch (Throwable e) {
			itsLogger.error("Were unable to copy over the file into Hadoop", e);
			throw new Exception(e);
		}
		finally{
			if(out!=null){
				out.flush();
				out.close();
			}
		}
		outPut=new HadoopAnalyzerOutPutARIMARPredict(ret);
		return outPut;
	}

	private boolean writePridictResult(HadoopARIMARPredictResult ret,
			ARIMAHadoopModel arimaModel,
			TimeSeriesHadoopPredictorConfig tsConf, String fullPathFileName,
			String line,OutputStream output) throws Exception {
		for(SingleARIMAHadoopModel singleModel:arimaModel.getModels()){
			HadoopSingleARIMARPredictResult singleResult=singleModel.prediction(Integer.parseInt(tsConf.getAheadNumber()));
			ret.getResults().add(singleResult);
			double[] value=singleResult.getPredict();
			double[] se=singleResult.getSe();
			Object[] id=singleResult.getIDData();
			int idType=singleResult.getType();
			String formatType=singleModel.getFormatType();
			for(int i=0;i<Integer.parseInt(tsConf.getAheadNumber());i++){
				if(idType==Types.DATE){
					SimpleDateFormat dateFormator=new SimpleDateFormat(formatType);
					line=line+dateFormator.format(id[i])+","+singleModel.getGroupColumnValue()+","+value[i]+","+se[i]+"\n";
				}
				else{
					line=line+id[i]+","+singleModel.getGroupColumnValue()+","+value[i]+","+se[i]+"\n";
				}
			}
			
			if(line.length()>10000){
				output.write(line.getBytes("UTF-8")) ;
			}
		}
		if(line.length()>0){
			output.write(line.getBytes("UTF-8")) ;
		}
		return true;
	}

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_PREDICT_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

	@Override
	protected HadoopMultiAnalyticFileOutPut doPredict(
			HadoopAnalyticSource source, HadoopPredictorConfig config)
			throws AnalysisException, Exception {
		// nothing different structure
		return null;
	}

}
