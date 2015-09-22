/**
 * ClassName WOEAutoGroup.java
 *
 * Version information: 1.00
 *
 * Data: 2 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.attribute.woe;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.WeightOfEvidenceAutoGroupAnalyzer;

/**
 * @author Shawn
 * 
 */
public class WOEAutoGroup {

	public static AnalyticOutPut autoGroup(AnalyticSource analyticSource)
			throws AnalysisException {
		WeightOfEvidenceAutoGroupAnalyzer WOEAutoGroup=new WeightOfEvidenceAutoGroupAnalyzer();
			
		return WOEAutoGroup.doAnalysis(analyticSource);
	}
	
	public static AnalyticOutPut computeWOE(AnalyticSource analyticSource)
	throws AnalysisException {
		WeightOfEvidenceAutoGroupAnalyzer WOEAutoGroup=new WeightOfEvidenceAutoGroupAnalyzer();
		return WOEAutoGroup.computeWOEStatic(analyticSource);
}
	

}
