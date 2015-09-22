/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterLift.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.operator.evaluator.DoubleListData;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualLine;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelLine;
import com.alpine.util.VisualUtils;
 
public class VisualAdapterLift  extends AbstractEvaluatorAdapter  implements OutPutVisualAdapter {
	private static final int LIFT_DEFAULT_WIDTH = 430; 
	private static final int LIFT_DEFAULT_HEIGHT = 444;
	private static java.text.DecimalFormat   df   =new   java.text.DecimalFormat( "0.000");
	
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterLift();
 @Override
 public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut, Locale locale) {
	 
	
	 
	 
		List<String> nameList = new ArrayList<String>();
		List<List<double[]>> xyCoordinateSet1List = new ArrayList<List<double[]>>();

		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			Object obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof List){
				Iterator liftlist = ((List) obj).iterator();
				while(liftlist.hasNext()){
					Object oo = liftlist.next();
					nameList.add(((DoubleListData)oo).getSourceName());
					List<double[]> xyCoordinateSet1 =  ((DoubleListData)oo).getDoubleList();
					xyCoordinateSet1List.add(xyCoordinateSet1);
				}
			}
		}
		List<VisualLine> lines =new ArrayList<VisualLine> ();
		
		for(int j=0;j<nameList.size();j++){
			System.out.println("==============" +nameList.get(j) +	"==============================");
			VisualLine line = new VisualLine(nameList.get(j)+" "+VisualNLS.getMessage(VisualNLS.LIFT_CURVE,locale));
			for (int i=0; i<xyCoordinateSet1List.get(j).size(); i++) {
				double x=xyCoordinateSet1List.get(j).get(i)[0];
				double y=xyCoordinateSet1List.get(j).get(i)[1];
				String xValue = df.format(x);
				String yValue = df.format(y);
				if(VisualUtils.containPoint(line,xValue,yValue)==false){
				
					 
				line.addVisualPoint(new VisualPoint(xValue , yValue)	);
				}
			 
			}
			List<VisualPoint> points = line.getPoints();
			Comparator<  VisualPoint> c = new Comparator<  VisualPoint> (){

				@Override
				public int compare(VisualPoint o1, VisualPoint o2) {
					if(o1!=null&&o2!=null){
						return o1.getX().compareTo(o2.getX());						
					}else{
						return 0;
					}

				}
				
			}; 
			Collections.sort(points, c) ;
			line.setPoints(points) ;
			//series1.setColor("red");
			lines.add(line);
			
		} 
		 
		VisualLine series2 = new VisualLine(VisualNLS.getMessage(VisualNLS.RANDOM,locale));
		//fixed point 
		series2.addVisualPoint(new VisualPoint("0", "1"));
		series2.addVisualPoint(new VisualPoint("1","1" ));
	 
		series2.setColor("green" );
		lines.add(series2);
	 	 
		VisualizationModelLine lineModel= new VisualizationModelLine(analyzerOutPut.getAnalyticNode().getName(),  lines );
		lineModel.setxAxisTitle(VisualNLS.getMessage(VisualNLS.PERCENTAGE_UPPER,locale));
		lineModel.setyAxisTitle("LIFT");
		lineModel.setWidth(LIFT_DEFAULT_WIDTH);
		lineModel.setHeight(LIFT_DEFAULT_HEIGHT) ;
 
 
 
		lineModel.setxMajorTickStep(X_Major_TickStep);
		lineModel.setxMinorTickStep(X_Minor_TickStep);
		
		VisualUtils.setAxisMaxAndMin(lineModel, true, true,df) ;
		VisualUtils.autoGenerateAxisLabel(lineModel, false, true,df) ;
		
		return lineModel;
	} 
 	

 
}