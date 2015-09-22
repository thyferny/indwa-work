package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.table.ScatterMatrixColumnPairs;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatterMatrix;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.dataexplorer.DataExplorerManagerImpl;
import com.alpine.miner.workflow.output.visual.MaxMinAxisValue;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualPointGroup;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;
import com.alpine.miner.workflow.output.visual.VisualizationModelTableGrouped;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.util.VisualUtils;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterScarrtMatrix extends AbstractOutPutVisualAdapter {

	private static final VisualAdapterScarrtMatrix INSTANCE = new VisualAdapterScarrtMatrix();
	private static final int VISUAL_TYPE_SCATT_PREVIEW = 26; 
	private static final int TYPE_SCATTER_MATRIX =28;
	
	public static VisualAdapterScarrtMatrix getInstance(){
		return INSTANCE;
	}
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,
			Locale locale) throws Exception {

		AnalyzerOutPutScatterMatrix scatMatrixOutPut = (AnalyzerOutPutScatterMatrix) analyzerOutPut;
		Map<ScatterMatrixColumnPairs, DataTable> dataTableMap = scatMatrixOutPut
				.getDataTableMap();
		List<Double> corrList = scatMatrixOutPut.getCorrList();
		String[] columnNames = scatMatrixOutPut.getColumnNames();

		List<List<VisualizationModel>> models = new ArrayList<List<VisualizationModel>>();
		for (int i = 0; i < columnNames.length; i++) {
			List<VisualizationModel> modelList = new ArrayList<VisualizationModel>();
			for (int j = 0; j < columnNames.length; j++) {

				VisualizationModel model = generateModelForTableCell(	dataTableMap, corrList, columnNames, i, j);
				modelList.add(model);
			}

			models.add(modelList);
		}String name= "";
		//from popup menu is null
		if(scatMatrixOutPut.getAnalyticNode()!=null){
			name=scatMatrixOutPut.getAnalyticNode().getName();
		}
		VisualizationModel visualModel = new VisualizationModelTableGrouped(name , null, models);
		visualModel.setVisualizationType(TYPE_SCATTER_MATRIX);
		return visualModel;
	}

	private VisualizationModel generateModelForTableCell(
			Map<ScatterMatrixColumnPairs, DataTable> dataTableMap,
			List<Double> corrList, String[] columnNames, int i,	int j) {
		VisualizationModel model = null;
		if (i == j) {
			// System.out.print("\t Name:"+columnNames[i]);
			model = new VisualizationModelText(null, columnNames[i]);
		}
		if (j > i) {
			// columnNames.length-i+1
			// System.out.print("\t numbers:"+corrList.remove(0));
			model = new VisualizationModelText(null, corrList.remove(0)
					.toString());
		}
		if (j < i) {
			// System.out.print("\t chart:");
			ScatterMatrixColumnPairs scattermatrixPairs = new ScatterMatrixColumnPairs(columnNames[j], columnNames[i]);
			DataTable dataTable = dataTableMap.get(scattermatrixPairs);
//			DataTable dataTable = getDataTable(columnNames[j], columnNames[i],
//					dataTableMap);
			List<DataRow> dataRows = dataTable.getRows();
			String[] precisionTitle = new String[]{"",""};
			getRowData4Precision(dataRows,precisionTitle);
			if (null != dataRows) {
				model = new VisualizationModelScatter();
				// scatterModel.setWidth(900);
				// scatterModel.setHeight(450);
				VisualizationModelScatter scatterModel = (VisualizationModelScatter) model;
				
			 	scatterModel.setVisualizationType(VISUAL_TYPE_SCATT_PREVIEW);
				scatterModel.sethGrid(true);
				scatterModel.setvGrid(true);
				
				scatterModel.setxAxisTitle(columnNames[j] + precisionTitle[0]);
				scatterModel.setyAxisTitle(columnNames[i] + precisionTitle[1]);
				List<VisualPointGroup> pointGroups = new ArrayList<VisualPointGroup>();
				VisualPointGroup points = new VisualPointGroup();
				MaxMinAxisValue maxMin =new MaxMinAxisValue(); 
				double[][] data = new double[2][dataRows.size()];
				for (int len = 0; len < dataRows.size(); len++) {
					DataRow dr = dataRows.get(len);
					double f1 = Float.valueOf(dr.getData(0));
					double f2 = Float.valueOf(dr.getData(1));
					String x = String.valueOf(f1);
					String y = String.valueOf(f2);
					points.addVisualPoint(new VisualPoint(x, y));
					maxMin.compareXY(f1, f2);
				}

				pointGroups.add(points);
				scatterModel.setPointGroups(pointGroups);
				DataExplorerManagerImpl.drawScatterLine(scatterModel, maxMin, points) ;
			 
			}
		}

		return model;
	}
    //Scrap
	private DataTable getDataTable(String x,String y,Map<ScatterMatrixColumnPairs,DataTable> dataTableMap){
		DataTable  dataTable = null;
		
//		System.out.println("new :"+columnNames[j]+"\t"+columnNames[i]);
		Set<Entry<ScatterMatrixColumnPairs,DataTable>> dataTableMapEntrySet =  dataTableMap.entrySet();
		if(dataTableMapEntrySet!=null){
			Iterator<Entry<ScatterMatrixColumnPairs,DataTable>> itor =dataTableMapEntrySet.iterator();
			while(itor.hasNext()){
				Entry<ScatterMatrixColumnPairs,DataTable> tempEntry = itor.next();
				ScatterMatrixColumnPairs tempColumnPars = tempEntry.getKey();
				if(tempColumnPars.getColumnX().equalsIgnoreCase(x) && tempColumnPars.getColumnY().equalsIgnoreCase(y)){
					//System.out.print(""+tempColumnPars.getColumnX()+"\t");
					//System.out.println(tempColumnPars.getColumnY());
					dataTable = tempEntry.getValue();
				}
			}
		}
		
		return dataTable;
		
	}
	
	private void getRowData4Precision(List<DataRow> dataRows,String[] precisionTitle){
		//0-> x 1-> y
		 if(null!=dataRows){
		    	DataRow tmpDataRow = dataRows.get(0);
	    		String maxX = tmpDataRow.getData(0);
	    		String minX = tmpDataRow.getData(0);
	    		String maxY = tmpDataRow.getData(1);
	    		String minY = tmpDataRow.getData(1);
	    		boolean xconvert = true;
	    		boolean yconvert = true;
	    		try{
	    			Float.valueOf(tmpDataRow.getData(0));
	    		}catch(NumberFormatException e){
	    			xconvert = false;
	    		}
	    		try{
	    			Float.valueOf(tmpDataRow.getData(1));
	    		}catch(NumberFormatException e){
	    			yconvert = false;
	    		}
		    	for (int i = 1; i < dataRows.size(); i++) {
		    		DataRow data = dataRows.get(i);
		    		if(xconvert == true && Float.valueOf(data.getData(0))>Float.valueOf(maxX)){
		    			maxX = data.getData(0);
		    		}
		    		if(xconvert == true && Float.valueOf(data.getData(0))<Float.valueOf(minX)){
		    			minX = data.getData(0);
		    		}
		    		if(yconvert == true && Float.valueOf(data.getData(1))>Float.valueOf(maxY)){
		    			maxY = data.getData(1);
		    		}
		    		if(yconvert == true && Float.valueOf(data.getData(1))<Float.valueOf(minY)){
		    			minY = data.getData(1);
		    		}
				}
		    	long n = 1l;
		    	if(xconvert==true){
		    		n = AlpineMath.adjustUnits(Double.valueOf(minX), Double.valueOf(maxX));
		    		
		    	}
		        
		    	long m = 1l;
		    	if(yconvert==true){
		    		m=AlpineMath.adjustUnits(Double.valueOf(minY), Double.valueOf(maxY));
		    	}
		    	if(n!=1){
		    		precisionTitle[0] = " ("+VisualUtils.getScientificNumber(n)+")";
		    	}
		    	if(m!=1){
		    		precisionTitle[1] = " ("+VisualUtils.getScientificNumber(m)+")";
		    	}
		    	               
		       	for (int j = 0; j < dataRows.size(); j++) {
		    		DataRow data = dataRows.get(j);
		    		String x = data.getData(0);
		    		String y = data.getData(1);
		    		if(xconvert==true){
		    			x = String.valueOf(Float.valueOf(data.getData(0))/n);
		    		}
		    		if(yconvert==true){
		    			y = String.valueOf(Float.valueOf(data.getData(1))/m);
		    		}
		    		data.setData(new String[]{x,y});
				}
		    }
		
	}
	
}


