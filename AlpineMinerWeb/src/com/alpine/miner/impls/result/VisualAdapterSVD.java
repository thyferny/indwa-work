/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterSVD.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
@Deprecated
//seems no use any more...
public class VisualAdapterSVD extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterSVD INSTANCE = new VisualAdapterSVD();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
	 	return null;
	}
	 
 	 
}
