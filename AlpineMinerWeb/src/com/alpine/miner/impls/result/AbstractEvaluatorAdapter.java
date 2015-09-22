/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AbstractEvaluatorAdapter.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import com.alpine.miner.workflow.output.visual.VisualizationModelLine;

/**
 * Now the evaluate chart contains the LIFT and ROC ,they are both from (0,0) to (1,1)
 * So the axis tick step is fixed as 0.1 and 0.05
 * */

public abstract class AbstractEvaluatorAdapter extends AbstractOutPutVisualAdapter{
	//this is only for lifr and roc ...  x axis
	
	protected static final String X_Major_TickStep= "0.1"; 
	protected static final String X_Minor_TickStep= "0.05"; 
	
	protected static final String Y_Major_TickStep= "0.1"; 
	protected static final String Y_Minor_TickStep= "0.05"; 
	
 
	
	
	protected void setAxisTicks(VisualizationModelLine lineModel) {
		lineModel.setxMajorTickStep(X_Major_TickStep);
		lineModel.setxMinorTickStep(X_Minor_TickStep);
		lineModel.setyMajorTickStep(Y_Major_TickStep) ;
		lineModel.setyMinorTickStep(Y_Minor_TickStep) ;
	}
	 
}
