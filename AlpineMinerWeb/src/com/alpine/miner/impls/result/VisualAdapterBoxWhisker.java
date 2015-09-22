/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterBoxWhisker.java
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.algoconf.TableBoxAndWhiskerConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutBoxWhisker;
import com.alpine.datamining.api.impl.output.BoxAndWhiskerItem;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualBoxWhisker;
import com.alpine.miner.workflow.output.visual.VisualBoxWhiskerGroup;
import com.alpine.miner.workflow.output.visual.VisualLabel;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelBoxWhisker;
import com.alpine.util.VisualUtils;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterBoxWhisker extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	private static final String CATEGORY = "category";
	private static final String NUMBER = "number";
	public static final VisualAdapterBoxWhisker INSTANCE = new VisualAdapterBoxWhisker();

	   
	/**
	 * This json is for the client js dojo grid use
	 * 
	 * "dataTable":
	 * {
	{"columns":["a1","a2","a3","a4","id"],
	"items":[
	{a1:xxx,a2:yyy,a3:vvv,a4:zzz,id:000},
	{a1:xxx,a2:yyy,a3:vvv,a4:zzz,id:000},
	{a1:xxx,a2:yyy,a3:vvv,a4:zzz,id:000}
	]
	}
	The json utility class will transfer the model to json
	 * */
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut, Locale locale)
			throws RuntimeException {
		AnalyzerOutPutBoxWhisker outputTable = (AnalyzerOutPutBoxWhisker)outPut;
		 
		List<BoxAndWhiskerItem> items = outputTable.getItemList(); 
		
		//
		String yLabel = "";
		if(null!=items && items.size()>0){
			float min= 0.0f,max=0.0f;
			
			for (int i = 0; i < items.size(); i++) {
			   if(min> items.get(i).getMin().floatValue()){
				   min = items.get(i).getMin().floatValue();
			   }
			   if(max> items.get(i).getMax().floatValue()){
				   max = items.get(i).getMax().floatValue();
			   }
			}
			float n = AlpineMath.adjustUnits(min, max);
			if(n!=1.0f){
				yLabel = "  ("+VisualUtils.getScientificNumber(n)+")";
			}
			for (int j = 0; j < items.size(); j++) {
				items.get(j).setMax(items.get(j).getMax().floatValue()/n);
				items.get(j).setMean(items.get(j).getMean().floatValue()/n);
				items.get(j).setMedian(items.get(j).getMedian().floatValue()/n);
				items.get(j).setMin(items.get(j).getMin().floatValue()/n);
				items.get(j).setQ1(items.get(j).getQ1().floatValue()/n);
				items.get(j).setQ3(items.get(j).getQ3().floatValue()/n);
				
			}
		}
		
		
		TableBoxAndWhiskerConfig config =(TableBoxAndWhiskerConfig) outputTable.getDataAnalyzer().getAnalyticSource().getAnalyticConfig();
		
 		List<String> errorMessages = new ArrayList<String>(); 
 		int maxCount = OutPutVisualAdapterFactory.getInstance().getMaxChartElements();
 		int total=items.size();
 		if(total>maxCount){
 			errorMessages.add(VisualNLS.getMessage(VisualNLS.BARS_EXCEED_LIMIT, locale, maxCount));
 		 
 			int count=0;
 			List<BoxAndWhiskerItem> newRows = new ArrayList<BoxAndWhiskerItem>(); 
 			
			for(BoxAndWhiskerItem item:items){ 
				if((count%(total/maxCount))<1){
					newRows.add(item);
				}
				count++;
			}
			
			items = newRows ;
 		}

        if (((AnalyzerOutPutBoxWhisker) outPut).isApprox())
        {
            errorMessages.add(VisualNLS.getMessage(VisualNLS.APPROXIMATE_VALUES, locale, maxCount));
        }
		
 		List<VisualBoxWhiskerGroup> boxWhiskers = generateVisualBoxWhiskerGroups(items); 
 		
 		Collections.sort(boxWhiskers,new VisualBoxWhiskerGroupComparator());
	
		VisualizationModelBoxWhisker visualModel
			= new VisualizationModelBoxWhisker(outPut.getDataAnalyzer().getName(),
				boxWhiskers, config.getSeriesDomain(), config.getTypeDomain(),config.getAnalysisValueDomain()+yLabel);
	 
		/**[{value:0,text:""}, {value:1,text:"11/3"}, {value:2,text:"11/4"}, 
	                                               {value:3,text:"11/5"}, {value:4,text:"11/6"}, {value:5,text:"11/7"}, 
	                                               {value:6,text:"11/8"} ,{value:7,text:"11/9"} ,{value:8,text:""}] 
		 * */
		
		String seriesType=NUMBER;
		if(items.size()>0){
			String seriesValue = items.get(0).getType();
			try{
				Double.parseDouble(seriesValue);
			}catch (Exception e) {
				 
				seriesType=CATEGORY ;
			} 
 		}
		visualModel.setxDataType(seriesType);
		setAxisValues(items, visualModel,seriesType);
		countXOffset(visualModel,seriesType);	
		
		
		if(errorMessages.size()>0){
			visualModel.setErrorMessage(errorMessages);
		}
		
		return visualModel;
	}

	private void countXOffset(VisualizationModelBoxWhisker visualModel,
			String seriesType) {
 		
		//each group is a series
		List<VisualBoxWhiskerGroup> grups = visualModel.getBoxWhiskers();
		List<VisualLabel> xLabels = visualModel.getxLabels();
        List<String> xValues = visualModel.getxValues();

		HashMap xMap= new HashMap(); 
	    for(Iterator<VisualLabel> iterator = xLabels.iterator(); iterator.hasNext();){
	    	VisualLabel label = iterator.next();
	    	xMap.put(label.getText(), label.getValue()) ;
	    }
		int seriesNumber = grups.size();
	 
		double widthScope =0.8;
		
		double offsetStep = widthScope/ seriesNumber;
		int series=0;
	    for(Iterator<VisualBoxWhiskerGroup> iterator = grups.iterator(); iterator.hasNext();){
	    	double start= offsetStep*0.5+(1 - (widthScope/2)) + series*offsetStep; //start from 0.6
	    	VisualBoxWhiskerGroup group = iterator.next();
	    	List<VisualBoxWhisker> items = group.getBoxWhiskers();
	     
	    	for(Iterator<VisualBoxWhisker> it = items.iterator(); it.hasNext();){
	    		 VisualBoxWhisker item = it.next();
	    		int realIndex=Integer.valueOf(xMap.get(item.getType()).toString());
	    		 
	    		
	    		 //this is the value could be duplicated, need count the offset...
	    		item.setxValue(realIndex-1+start);

                item.setxIndex( xValues.indexOf(item.getType()));
                item.setSeriesIndex(series);

            }
	    	series=series+1;
	    }
	 	
	}


	private void setAxisValues(List<BoxAndWhiskerItem> items,
			VisualizationModelBoxWhisker visualModel ,String xdataType) {

		double maxX=0;
		double maxY=0;
		double minX=0;
		double minY=0;

        double numX = 0;
		List<String > typeValueList = new ArrayList<String > (); 
		for (Iterator iterator = items.iterator(); iterator.hasNext();) {
			BoxAndWhiskerItem boxAndWhiskerItem = (BoxAndWhiskerItem) iterator
					.next();
//			if(xdataType.equals("number")) {
//				double typeValue=Double.parseDouble(boxAndWhiskerItem.getType());  
//				if(minX>typeValue){
//					minX=typeValue;
//				}
//				else if(maxX<typeValue){ 
//					maxX=typeValue;
//				}
//
//			}
			double boxMax = Double.parseDouble(boxAndWhiskerItem.getMax().toString());
			if(boxMax>maxY){
				maxY=boxMax;
			}
			double boxMin = Double.parseDouble(boxAndWhiskerItem.getMin().toString());
			if(boxMin<minY){
				minY=boxMin; 
				
			}
			if(false==typeValueList.contains(boxAndWhiskerItem.getType())){
				typeValueList.add(boxAndWhiskerItem.getType());
			}
			
		}
		 List<VisualLabel> xLabels= new ArrayList<VisualLabel>();
        List<String> xValues= new ArrayList<String>();

//		 if(xdataType.equals("number")){
//			 //number, show 10 axis
//			 double delta=maxX-minX;
//			 int step=(int)delta/10;
//			 int x=(int)minX-step;
//			 for (int i=0;i<=10;i++) {
//			 
//					xLabels.add(new VisualLabel(   x,  Integer.toString(x))) ;		
//					x=x+step;
//				}
//			 
//		 }else{//categroy, show each cate
			 xLabels.add(new VisualLabel(0, "")) ;		
			 for (int i=1;i<=typeValueList.size();i++) {
                 xValues.add(typeValueList.get(i -1));
				String visualLabel = typeValueList.get(i -1) ;
				xLabels.add(new VisualLabel(i, visualLabel)) ;		
				
			}
			xLabels.add(new VisualLabel(xLabels.size(), " ")) ;	
			minX=0;
			maxX=xLabels.size()-1;
            numX = xValues.size();
//		 }
	
		 
		 
	 //if series is category, set the x labels
	//all this will be used in the js file 	
	 visualModel.setxLabels(xLabels);
     visualModel.setxValues(xValues);
	 visualModel.setMaxX(maxX);
	 visualModel.setMinX(minX);
	 visualModel.setMaxY(maxY);
	 visualModel.setMinY(minY);
     visualModel.setNumX(numX);
	}

	//MINERWEB-902
	private class VisualBoxWhiskerGroupComparator implements Comparator {

		@Override
		public int compare(Object arg0, Object arg1) {
            //time | number | String
			VisualBoxWhiskerGroup parm1 = (VisualBoxWhiskerGroup)arg0;
			VisualBoxWhiskerGroup parm2 = (VisualBoxWhiskerGroup)arg1;
			try {
				//time
				long date1 = Date.parse(parm1.getSeries());
				long date2 = Date.parse(parm2.getSeries());
				if(date1>date2){
					return 1;
				}else if(date1==date2){
					return 0;
				}else{
					return -1;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			try {
				Double num1 = Double.valueOf(parm1.getSeries());
				Double num2 = Double.valueOf(parm2.getSeries());
				if(num1>num2){
					return 1;
				}else if(num1==num2){
					return 0;
				}else{
					return -1;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
             //string
             return (parm1.getSeries().compareTo(parm2.getSeries()));
		}
		
	}

	private List<VisualBoxWhiskerGroup> generateVisualBoxWhiskerGroups(
			List<BoxAndWhiskerItem> items) {
		//type, List<BoxAndWhiskerItem>
		HashMap<String, List<BoxAndWhiskerItem>> seriesMap= new HashMap <String, List<BoxAndWhiskerItem>> ();
		for (Iterator iterator = items.iterator(); iterator.hasNext();) {
			BoxAndWhiskerItem boxAndWhiskerItem = (BoxAndWhiskerItem) iterator
					.next();
			if(false==seriesMap.keySet().contains(boxAndWhiskerItem.getSeries())){
				seriesMap.put(boxAndWhiskerItem.getSeries(), new ArrayList<BoxAndWhiskerItem>()) ; 
			} 
			seriesMap.get(boxAndWhiskerItem.getSeries()).add(boxAndWhiskerItem) ;
	 		 
 		}

		//series....
			
		List<VisualBoxWhiskerGroup> boxWhiskers = new ArrayList<VisualBoxWhiskerGroup>();
		Set<String> types = seriesMap.keySet();
		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
			String seriesValue = (String) iterator.next(); 
			List<BoxAndWhiskerItem> typeBoxs = seriesMap.get(seriesValue);
			List<VisualBoxWhisker> boxs=generateVisualBoxList(typeBoxs); 
			VisualBoxWhiskerGroup group = new VisualBoxWhiskerGroup(seriesValue,boxs, true);
			boxWhiskers.add(group) ;
		}
	 
		return boxWhiskers;
	}


	private List<VisualBoxWhisker> generateVisualBoxList(
			List<BoxAndWhiskerItem> typeBoxs) {
		List<VisualBoxWhisker> visualBoxList= new ArrayList<VisualBoxWhisker>();

	for (Iterator iterator = typeBoxs.iterator(); iterator.hasNext();) {
		BoxAndWhiskerItem boxItem = (BoxAndWhiskerItem) iterator.next(); 
		VisualBoxWhisker visualBox= new VisualBoxWhisker(boxItem.getType(), 
				boxItem.getMax(),boxItem.getMin(),boxItem.getQ1() ,boxItem.getQ3(),boxItem.getMean(),boxItem.getMedian());
		
		visualBoxList.add(visualBox) ;
	}
		return visualBoxList;
	}
 
 	 
}
