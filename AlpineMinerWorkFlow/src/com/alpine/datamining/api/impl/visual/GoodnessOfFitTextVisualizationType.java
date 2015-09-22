/**
 * ClassName GoodnessOfFitTextVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

/**
 * yiling
 */
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitOutPut;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.datamining.operator.evaluator.ValueGoodnessOfFit;

public class GoodnessOfFitTextVisualizationType extends TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		StringBuffer message = new StringBuffer();
		if (analyzerOutPut instanceof GoodnessOfFitOutPut) {
			obj = ((GoodnessOfFitOutPut)analyzerOutPut).getResultList();
			if (obj instanceof List ){
				message.append(toTextOutPut((List<GoodnessOfFit>)obj));
			}
		}
		
		TextVisualizationOutPut output = new TextVisualizationOutPut(message.toString());
		output.setVisualizationType(this);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale));
		return output;
	}
	
	public String toTextOutPut(List<GoodnessOfFit> list){
	
 		StringBuffer sb=new StringBuffer();
 		sb.append("\n");
		for(int i =0;i<list.size();i++){
			GoodnessOfFit gft=list.get(i);
			TextTable textTable=new TextTable();
			
			String name=gft.getSourceName();
			sb.append(name);
			 double accu = gft.getAccuracy();
			 sb.append("  Accuracy:"+accu);
			double error = gft.getError();
			sb.append("  Error:"+error+"\n");
  		
			ArrayList<ValueGoodnessOfFit> gds = gft. getGoodness();
			textTable.addLine(new String[]{"Stats","Recall","Precision","F1","Specificity","Sensitivity"});
		 
			for(int j=0;j<gds.size();j++){
				ValueGoodnessOfFit vof=gds.get(j);
				String value=vof.getValue();
				String recall=String.valueOf(vof.getRecall());
				String precision=String.valueOf( vof.getPrecision());
				String f1=String.valueOf( vof.getF1());
				String specificity=String.valueOf( vof.getSpecificity());		
				String sensitivity=String.valueOf( vof.getSensitivity());
				textTable.addLine(new String[]{value,recall,precision,f1,specificity,sensitivity});
				
			}
			sb.append(textTable.toTableString()); 
			sb.append("\n"); 
		}
		return sb.toString();
	}
	
}
