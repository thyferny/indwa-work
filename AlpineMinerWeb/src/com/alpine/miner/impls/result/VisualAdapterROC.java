/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterROC.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.operator.evaluator.DoubleListAndDoubleData;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualLine;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelLine;
import com.alpine.utility.tools.AlpineMath;

/**
 * roc will draw a line from 0,0 to 1,1 
 * so it may contains 100 points for the cliet draw API like dojo, jFreechart
 * this axis value is from 0 to 1 ,but we get the units =100
 * so for the client ,it is easy to draw a line from 0,0 to 100,100
 * even you only have 2 points, the client will compute the others...
 * */

public class VisualAdapterROC  extends AbstractEvaluatorAdapter  implements OutPutVisualAdapter {
	
	private static final String AUC = "AUC";
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterROC();
	private static final int ROC_DEFAULT_WIDTH = 430; 
	private static final int ROC_DEFAULT_HEIGHT = 430;
 @Override
 public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale) {
	 
	 java.text.DecimalFormat   df   =new   java.text.DecimalFormat( "0.000");
	 
	 
		 
		List<String> nameList = new ArrayList<String>();
		List<List<double[]>> xyCoordinateSet1List = new ArrayList<List<double[]>>();
		List<Double> valueList = new ArrayList<Double>();
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			Object obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof List){
				Iterator roclist = ((List) obj).iterator();
				while(roclist.hasNext()){
					Object oo = roclist.next();
					nameList.add(((DoubleListAndDoubleData)oo).getSourceName());
					List<double[]> xyCoordinateSet1 =  ((DoubleListAndDoubleData)oo).getDoubleList();
					double value = ((DoubleListAndDoubleData)oo).getDouble();
					valueList.add(Double.parseDouble(AlpineMath.doubleExpression(value)));
					xyCoordinateSet1List.add(xyCoordinateSet1);
				}
				
			}
		}
	 
		List<VisualLine> lines =new ArrayList<VisualLine> ();
		for(int j=0;j<nameList.size();j++){
				VisualLine series1 = new VisualLine(nameList.get(j)+" "+VisualNLS.getMessage(VisualNLS.ROC_CURVE,locale));
			for (int i=0; i<xyCoordinateSet1List.get(j).size(); i++) {
				double x=xyCoordinateSet1List.get(j).get(i)[0];
				double y=xyCoordinateSet1List.get(j).get(i)[1];
				
				series1.addVisualPoint(new VisualPoint( df.format(x),
						df.format(y))	);
			 
			}
			//series1.setColor("red");
			lines.add(series1);
			
		}
		//value must be there
		VisualLine series2 = new VisualLine(VisualNLS.getMessage(VisualNLS.RANDOM,locale));
		//even 2 point the client may compute 100 poits for the dojo charting
//		for (int i = 0; i < 200; i++) {
//	 
//			String x= df.format(((float)(i))/200); 
//			String y= x;
//			series2.addVisualPoint(new VisualPoint(x,y));
//		}
		series2.addVisualPoint(new VisualPoint("0", "0"));
		series2.addVisualPoint(new VisualPoint("1","1" ));
	 
		series2.setColor("green" );
		lines.add(series2);
		
	 
		 
		VisualizationModelLine lineModel= new VisualizationModelLine(analyzerOutPut.getAnalyticNode().getName(),  lines );
		lineModel.setxAxisTitle(VisualNLS.getMessage(VisualNLS.FALSE_POSITIVE_RATE,locale));
		lineModel.setyAxisTitle(VisualNLS.getMessage(VisualNLS.SENSITIVITY,locale));
		//no need, the charting will do it automaticlly
		//lineModel.setxLabels(autoGenerateXlabelForOne(lineModel));
		
		lineModel.setWidth(ROC_DEFAULT_WIDTH);
		lineModel.setHeight(ROC_DEFAULT_HEIGHT) ;
		
		
		
		super.setAxisTicks(lineModel);
 
		//0.01,0.02.....
		/*List<String[]> xLabels=new ArrayList<String[]>();
		for (int i = 0; i < 10; i++) {
			float x= ((float)(i*20))/200;
			String value=df.format(x);
			xLabels.add(new String[]{value,value}); 
		}
		lineModel.setxLabels(xLabels);
		*/
		StringBuffer  lable=new StringBuffer();
		for(int i=0;i<valueList.size();i++){
		
			String value=Double.toString(valueList.get(i));
			if(value.indexOf(".")>0&&value.substring(value.indexOf("."),value.length()).length()>6){
				value=value.substring(0,value.indexOf(".")+6);
			}
			lable.append("  "+nameList.get(i)+" "+AUC + " = "+value);
			if(i%2==1){
				lable.append("\n");
			}
		
		}
		
		lineModel.setDescription(lable.toString())  ;
		return lineModel;
	}
 
}