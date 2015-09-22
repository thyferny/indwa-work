/**
 * ClassName HadoopFowFilterAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.util.Locale;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopRowFilterConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.resources.SDKLanguagePack;

/**
 * @author Jeff Dong
 *
 */
public class HadoopRowFilterAnalyzer extends AbstractHadoopAttributeAnalyzer {
	
	@Override
	public String generateScript(HadoopDataOperationConfig config,String pureFileName) {
		String script = getOutputTempName() + " = FILTER " + pureFileName +" by " + ((HadoopRowFilterConfig)config).getFilterCondition()+" ; ";
		return script;
	}

	@Override
	public AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

}
