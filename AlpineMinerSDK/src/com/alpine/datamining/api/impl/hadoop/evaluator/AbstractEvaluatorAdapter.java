package com.alpine.datamining.api.impl.hadoop.evaluator;

import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;


public abstract class AbstractEvaluatorAdapter implements EvaluatorAdapter {

	private boolean localMode = false;

	@Override
	public boolean isLocalMode() {
		return localMode;
	}

	@Override
	public void setLocalMode(boolean localMode) {
		this.localMode = localMode;

	}
	
	public HadoopAnalyticSource lightlySourceCloneWithNoConfig(AnalyticSource source) throws CloneNotSupportedException {
		HadoopAnalyticSource hdSource=null;
		if(source instanceof HadoopAnalyticSource){
			hdSource=(HadoopAnalyticSource) source;
			HadoopAnalyticSource clone=new HadoopAnalyticSource();
			
			AnalysisFileStructureModel fsModel = hdSource.getHadoopFileStructureModel().clone();
			if(hdSource.getFileFormat()!=null){
				clone.setFileFormat(new String(hdSource.getFileFormat()));
			}
			if(hdSource.getFileName()!=null){
				clone.setFileName(new String(hdSource.getFileName()));
			}
			clone.setHadoopFileStructureModel(fsModel);
			clone.setHadoopInfo(hdSource.getHadoopInfo());
			if(hdSource.getInputTempName()!=null){
				clone.setInputTempName(new String(hdSource.getInputTempName()));
			}
			if(hdSource.getNameAlias()!=null){
				clone.setNameAlias(new String(hdSource.getNameAlias()));
			}
			return clone;
		}
		else{
			return null;
		}
	}

}
