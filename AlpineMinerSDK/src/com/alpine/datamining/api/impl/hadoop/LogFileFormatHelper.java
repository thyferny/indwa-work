package com.alpine.datamining.api.impl.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisLogFileStructureModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.ext.LogParserFactory;

public class LogFileFormatHelper implements FileFormatHelper {
	private AnalysisLogFileStructureModel fileStructureModel;
	private HadoopAnalyticSource hadoopSource;

	public LogFileFormatHelper(
			AnalysisLogFileStructureModel fileStructureModel,
			HadoopAnalyticSource hadoopSource) {
			this.fileStructureModel = fileStructureModel ;
			this.hadoopSource=hadoopSource;
		}

	@Override
	public void initHadoopConfig(Configuration hadoopConf)
			throws AnalysisException {
		hadoopConf.set(AlpineHadoopConfKeySet.INPUT_FORMAT_KEY,AlpineHadoopConfKeySet.INPUT_FORMAT_VALUE_LOG); 
		hadoopConf.set(LogParserFactory.LOG_TYPE, fileStructureModel.getLogType()) ;
		hadoopConf.set(LogParserFactory.LOG_FORMAT,fileStructureModel.getLogFormat());
		
	}

	@Override
	public void setInputFormatClass(Job job) {
	//nothing to do , will use default textinputformat
	}

}
