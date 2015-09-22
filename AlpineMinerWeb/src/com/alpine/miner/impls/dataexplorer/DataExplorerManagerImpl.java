/**
 * ClassName :DataExplorerManagerImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-25
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.dataexplorer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.algoconf.BarChartAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.CorrelationAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.FrequencyAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.TableBoxAndWhiskerConfig;
import com.alpine.datamining.api.impl.algoconf.ValueAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.CorrelationAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.FrequencyAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.HistogramAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.ValueAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.db.table.TableAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableBoxAndWhiskerAnalyzer;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.result.OutPutVisualAdapter;
import com.alpine.miner.impls.result.OutPutVisualAdapterFactory;
import com.alpine.miner.impls.result.VisualAdapterARIMARPredictor;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.output.visual.AbstractVisualizationModelChart;
import com.alpine.miner.workflow.output.visual.MaxMinAxisValue;
import com.alpine.miner.workflow.output.visual.VisualLine;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualPointGroup;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLine;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DBMetaDataUtil;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

/**
 * @author zhaoyong
 *
 */
public class DataExplorerManagerImpl implements DataExplorerManager {
    private static Logger itsLogger = Logger.getLogger(DataExplorerManagerImpl.class);


    ResourceType getResourceType(String type){
		if(type==null){
			return ResourceType.Personal;
		}
		if(type.equals(ResourceType.Public.toString())){
			return ResourceType.Public; 
		}else if(type.equals(ResourceType.Group.toString())){
			return ResourceType.Group;
		}else{
			return ResourceType.Personal;
		}
	}
	
	
 

	//this is important
	@Override
	public VisualizationModel runAnalyzerForVisual(String user,
			String dbConnName, String dbSchemaName, String dbTableName,
			AnalyticConfiguration config,AbstractDBAnalyzer analyzer,
			String resourceType,String isGeneratedTable, Locale locale) throws  Exception {
	 
		DbConnectionInfo connInfo = ResourceManager.getInstance()
				.getDBConnection(user, dbConnName,getResourceType(resourceType));
		DbConnection conn = connInfo.getConnection();
		dbTableName = VisualUtils.refineTableName(dbSchemaName, dbTableName, user,  isGeneratedTable );
		DataBaseAnalyticSource source = new DataBaseAnalyticSource(
				conn.getDbType(), conn.getUrl(), conn.getDbuser(),
				conn.getPassword(), dbSchemaName, dbTableName, conn.getUseSSL());
		source.setAnalyticConfiguration(config);
		VisualizationModel visualModel =null;
		final Connection connection = analyzer.createConnection(source);
		try{
			
		
		source.setConenction(connection);
		analyzer.setAnalyticSource(source);
	  
		AnalyticOutPut outPut = analyzer.doAnalysis(source);
 
		outPut.setDataAnalyzer(analyzer);
		visualModel = OutPutVisualAdapterFactory
				.getInstance().getAdapter(outPut).toVisualModel(outPut,  locale);
		visualModel.setTitle(dbTableName);
	}catch (Exception e){
		throw e;
	}finally{
		if(connection!=null){
			connection.close();
		}
	}
		return visualModel;
	}
	

	
	//=======================================================================
	
	@Override
	public  VisualizationModel getBarchartVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String valueDomain,
			String scopeDomain, String categoryType, String resourceType,
			String isGeneratedTable, String user, Locale locale) throws Exception {
		AnalyticConfiguration barChartAnalysisConfig =new BarChartAnalysisConfig(valueDomain,scopeDomain,categoryType);
		barChartAnalysisConfig.setLocale(locale);
		
		TableAnalysisAnalyzer barchartAnalyzer=new TableAnalysisAnalyzer();
		
		VisualizationModel visualModel = runAnalyzerForVisual(user, dbConnName, dbSchemaName,
				dbTableName, barChartAnalysisConfig, barchartAnalyzer, resourceType,  isGeneratedTable,locale);
		visualModel.setTitle(dbTableName);
		return visualModel;
	}
	
	@Override
	public VisualizationModelDataTable getTableDataVModel(String user,
			String dbConnName, String dbSchemaName, String dbTableName,
			String resourceType, String isGeneratedTable) throws  Exception {
		VisualizationModelDataTable tableModel = null;
	 
		
			DbConnectionInfo connInfo = ResourceManager.getInstance()
					.getDBConnection(user, dbConnName ,getResourceType(resourceType));
		 
			if (connInfo != null && connInfo.getConnection() != null) {
				String dbType = connInfo.getConnection().getDbType(); 
				DBDataUtil util = new DBDataUtil(connInfo.getConnection());
				dbTableName = VisualUtils.refineTableName(dbSchemaName, dbTableName, user, isGeneratedTable);
				DataTable datatable = util.getTableDataList(dbSchemaName,
						dbTableName, ProfileReader.getInstance(false).getProperties().getProperty("ui_para2"), connInfo.getConnection()
								.getDbType());
				DBUtil.reSetColumnType(dbType,datatable);
				  tableModel = new VisualizationModelDataTable(dbTableName, datatable);
			
			}
	
		return tableModel;
	}
	@Override
	public VisualizationModel getBoxWhiskerVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String analysisValue,
			String analysisSeries, String analysisType, String resourceType,
			String isGeneratedTable, String user, Locale locale) throws Exception {
		TableBoxAndWhiskerConfig config= new TableBoxAndWhiskerConfig(analysisValue, analysisSeries, analysisType);
		config.setLocale(locale);
 
		TableBoxAndWhiskerAnalyzer analyzer= new TableBoxAndWhiskerAnalyzer();
		
		
		VisualizationModel visualModel = runAnalyzerForVisual(user, dbConnName, dbSchemaName,
				dbTableName, config, analyzer,resourceType ,  isGeneratedTable,locale);
		return visualModel;
	}

	@Override
	public VisualizationModel getStatisticsVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String resourceType, String isGeneratedTable, String user,
			Locale locale) throws Exception{
		String columnNames= columnNameIndex ; 
		ValueAnalysisConfig config= new ValueAnalysisConfig( );
		config.setColumnNames(columnNames) ;
		config.setLocale(locale);
		ValueAnalysisAnalyzer analyzer= new ValueAnalysisAnalyzer();
		
		VisualizationModel visualModel = runAnalyzerForVisual(user, dbConnName, dbSchemaName,
				dbTableName, config, analyzer, resourceType,  isGeneratedTable ,locale);
		return visualModel;
	}
	

	@Override
	public VisualizationModel getFrequencyVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String resourceType, String isGeneratedTable, String user,
			Locale locale) throws Exception{
		String columnNames=  columnNameIndex; 
		FrequencyAnalysisConfig config= new FrequencyAnalysisConfig(columnNames);
		config.setLocale(locale);
		FrequencyAnalysisAnalyzer analyzer= new FrequencyAnalysisAnalyzer();
		
		 
		
		VisualizationModel visualModel = runAnalyzerForVisual(user, dbConnName, dbSchemaName,
				dbTableName, config, analyzer,  resourceType ,  isGeneratedTable ,locale);
		return visualModel;
	}
	
	@Override
	public VisualizationModel getCorrelationVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String resourceType, String isGeneratedTable, String user,
			Locale locale) throws Exception{
		String columnNames=columnNameIndex; 
		CorrelationAnalysisConfig config= new CorrelationAnalysisConfig( );
		config.setColumnNames(columnNames) ;
		config.setLocale(locale);
		CorrelationAnalysisAnalyzer analyzer= new CorrelationAnalysisAnalyzer();
		
		VisualizationModel visualModel = runAnalyzerForVisual(user, dbConnName, dbSchemaName,
				dbTableName, config, analyzer,  resourceType ,  isGeneratedTable,locale);
		
		visualModel.setTitle(dbTableName);
		return visualModel;
	}
	
	

	@Override
	public VisualizationModel getHistogramVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			AnalysisColumnBin[] columnBins, String resourceType, String isGeneratedTable,
			String user, Locale locale) throws Exception {
		String columnNames = columnNameIndex; 
 
		HistogramAnalysisConfig config= new HistogramAnalysisConfig( );
//		List<AnalysisColumnBin> columnBinList= new ArrayList<AnalysisColumnBin>(); 
//		if(columnBins!=null&&columnBins.trim().length()>0){
//			StringTokenizer stBin = new StringTokenizer(columnBins,",");
//			StringTokenizer stColumn = new StringTokenizer(columnNameIndex,",");
//			while(stBin.hasMoreTokens()){
//				String bin = stBin.nextToken();
//				String columnName = stColumn.nextToken(); 
//				AnalysisColumnBin colBin = new AnalysisColumnBin(columnName, Integer.valueOf(bin));  
//				columnBinList.add(colBin) ;
//			}
//			
//		}
		AnalysisColumnBinsModel columnBinModel = new AnalysisColumnBinsModel(Arrays.asList(columnBins));
		config.setColumnBinModel(columnBinModel); 
		config.setColumnNames(columnNames) ;
		config.setLocale(locale);
		HistogramAnalysisAnalyzer analyzer= new HistogramAnalysisAnalyzer();
		
		VisualizationModel visualModel = runAnalyzerForVisual(user, dbConnName, dbSchemaName,
				dbTableName, config, analyzer, resourceType,  isGeneratedTable,locale);
		return visualModel;
	}
	
	
	@Override
	public VisualizationModelScatter getScatterVModel( String dbConnName,
			String dbSchemaName, String dbTableName, String analysisColumn,
			String referenceColumn, String categoryColumn, String referenceColumnType, String resourceType,
			String isGeneratedTable, String user,Locale locale) throws Exception, IOException {
		dbTableName = VisualUtils.refineTableName(dbSchemaName, dbTableName, user,
				isGeneratedTable);
		VisualizationModelScatter scatterModel = new VisualizationModelScatter();
		scatterModel.setTitle(dbTableName) ;
		scatterModel.setWidth(900);
		scatterModel.setHeight(450);
		scatterModel.sethGrid(true);
		scatterModel.setvGrid(true);
		scatterModel.setxAxisTitle(referenceColumn);
		scatterModel.setyAxisTitle(analysisColumn);
        scatterModel.setVisualizationType(12);
		// key is the value of category column
		HashMap<String, VisualPointGroup> pointGroupMap = new HashMap<String, VisualPointGroup>();

		DbConnectionInfo connInfo = ResourceManager.getInstance()
				.getDBConnection(user, dbConnName,
						getResourceType(resourceType));
		if (connInfo == null || connInfo.getConnection() == null) {
 
			return null; 
		}
		MaxMinAxisValue maxMin = new MaxMinAxisValue();

		DbConnection dbConnection = connInfo.getConnection();

		DBDataUtil dbd = new DBDataUtil(dbConnection);

		DataTable dt = null;
	    Map<String,String> cateMap = null; 	
	    
		// only one, and no line?
		if (categoryColumn == null || categoryColumn.trim().equals("")) {
			dt = dbd.getSampleTableDataList(dbSchemaName, dbTableName,
					new String[] { analysisColumn, referenceColumn },
					ResourceManager.getInstance().getPreferenceProp( 
							PreferenceInfo.GROUP_UI, PreferenceInfo.KEY_MAX_SCATTER_POINTS)
					, referenceColumn);

			VisualPointGroup pointList = new VisualPointGroup();
			List<DataRow> dataRows = dt.getRows();
			
			//set data precision add by will 
			String[] precisionTitle = new String[]{"",""};
			getRowData4Precision(dataRows,precisionTitle);
			
			scatterModel.setxAxisTitle(referenceColumn + precisionTitle[0]);
			scatterModel.setyAxisTitle(analysisColumn + precisionTitle[1]);
			
			if(null!=referenceColumnType &&( "cate".equalsIgnoreCase(referenceColumnType)|| "date".equalsIgnoreCase(referenceColumnType))){
				cateMap = new HashMap<String,String>(); 
				fillCateMap(referenceColumnType, cateMap, dataRows); 
		    }
			
		    String x="",y="";
			for (Iterator iterator = dataRows.iterator(); iterator
					.hasNext();) {
				DataRow dataRow = (DataRow) iterator.next();
								
				if(null!=referenceColumnType &&( "cate".equalsIgnoreCase(referenceColumnType)|| "date".equalsIgnoreCase(referenceColumnType))){
					 x = cateMap.get(dataRow.getData(1));
					 y = dataRow.getData(0);
				} else{
					 x = dataRow.getData(1);
					 y = dataRow.getData(0);
				}
				maxMin.compareXY(x, y);
				pointList.addVisualPoint(new VisualPoint(x, y));
			}

			scatterModel.addVisualPointGroup(pointList);
			drawScatterLine(scatterModel, maxMin, pointList);

		} else {
			String maxRows=ProfileReader.getInstance(false).getProperties().getProperty(OutPutVisualAdapter.UI_PARA2);
			 
			dt = dbd.getSampleTableDataList(dbSchemaName, dbTableName,
					new String[] { analysisColumn, referenceColumn,
							categoryColumn },
					maxRows, referenceColumn); // referenceColumn
						 

			List<DataRow> dataRows = dt.getRows();	
			
			String[] precisionTitle = new String[]{"",""};
			getRowData4Precision(dataRows,precisionTitle);
			
			scatterModel.setxAxisTitle(referenceColumn + precisionTitle[0]);
			scatterModel.setyAxisTitle(analysisColumn + precisionTitle[1]);
			
			if(null!=referenceColumnType &&( "cate".equalsIgnoreCase(referenceColumnType)|| "date".equalsIgnoreCase(referenceColumnType))){
				cateMap = new HashMap<String,String>(); 
		        fillCateMap(referenceColumnType, cateMap, dataRows);
		    } 
			for (Iterator iterator = dataRows.iterator(); iterator
					.hasNext();) {
				DataRow dataRow = (DataRow) iterator.next();
				VisualPointGroup pointList = pointGroupMap.get(dataRow
						.getData()[SCATTER_CATEGORY_INDEX]);
				if (pointList == null) {
					pointList = new VisualPointGroup();
					pointList
							.setLabel(dataRow.getData()[SCATTER_CATEGORY_INDEX]);
					pointGroupMap.put(
							dataRow.getData()[SCATTER_CATEGORY_INDEX],
							pointList);
				}

				String xValue =	 dataRow.getData(1);
				if(null!=referenceColumnType &&( "cate".equalsIgnoreCase(referenceColumnType)|| "date".equalsIgnoreCase(referenceColumnType))){
					xValue = cateMap.get(xValue);
				}
				pointList.addVisualPoint(new VisualPoint(xValue , dataRow.getData(0)));
				maxMin.compareXY(xValue, dataRow.getData(0));

			}

			drawVisualLine4Scatter(scatterModel, pointGroupMap, maxMin);

		}
		// minx maxx and miny maxy has been updated

		scatterModel.generateMaxMinAxis(maxMin);
		
		
		if(cateMap==null){
			VisualUtils.autoGenerateAxisLabel(scatterModel, true, true,null);
			
		}else{
			VisualUtils.autoGenerateAxisLabel(scatterModel, false, true,null);
		    getXLable4Scatter(cateMap,scatterModel); 
			
		 
		}
		VisualUtils.generateAxisTickStep(scatterModel, true, true);
		return scatterModel;
	}
	
	private void getRowData4Precision(List<DataRow> dataRows,String[] precisionTitle){
		//1-> x 0-> y
		
		 if(null!=dataRows){
		    	DataRow tmpDataRow = dataRows.get(0);
	    		String maxX = tmpDataRow.getData(1);
	    		String minX = tmpDataRow.getData(1);
	    		String maxY = tmpDataRow.getData(0);
	    		String minY = tmpDataRow.getData(0);
	    		boolean xconvert = true;
	    		boolean yconvert = true;
	    		try{
	    			Float.valueOf(tmpDataRow.getData(1));
	    		}catch(NumberFormatException e){
	    			xconvert = false;
	    		}
	    		try{
	    			Float.valueOf(tmpDataRow.getData(0));
	    		}catch(NumberFormatException e){
	    			yconvert = false;
	    		}
	    	
		    	for (int i = 1; i < dataRows.size(); i++) {
		    		DataRow data = dataRows.get(i);
		    		if(xconvert == true && Float.valueOf(data.getData(1))>Float.valueOf(maxX)){
		    			maxX = data.getData(1);
		    		}
		    		if(xconvert == true && Float.valueOf(data.getData(1))<Float.valueOf(minX)){
		    			minX = data.getData(1);
		    		}
		    		if(yconvert == true && Float.valueOf(data.getData(0))>Float.valueOf(maxY)){
		    			maxY = data.getData(0);
		    		}
		    		if(yconvert == true && Float.valueOf(data.getData(0))<Float.valueOf(minY)){
		    			minY = data.getData(0);
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
		    		precisionTitle[0] = "  ("+VisualUtils.getScientificNumber(n)+")";
		    	}
		    	if(m!=1){
		    		precisionTitle[1] = "  ("+VisualUtils.getScientificNumber(m)+")";
		    	}
		    	
		       	for (int j = 0; j < dataRows.size(); j++) {
		    		DataRow data = dataRows.get(j);
		    		String x = data.getData(1);
		    		String y = data.getData(0);
		    		String cate = "";
		    		if(data.getData().length==3){
		    			cate = data.getData(2);
		    		}
		    		 
		    		if(xconvert==true){
		    			x = String.valueOf(Float.valueOf(data.getData(1))/n);
		    		}
		    		if(yconvert==true){
		    			y = String.valueOf(Float.valueOf(data.getData(0))/m);
		    		}
		    		if(data.getData().length==3){
		    			data.setData(new String[]{y,x,cate});
		    		}else{
		    			data.setData(new String[]{y,x});
		    		}
				}
		    }
		
	}




	private void getXLable4Scatter(Map<String, String> cateMap, AbstractVisualizationModelChart scatterModel) {
		int step= VisualUtils.LABEL_NUMBER;
		List<String[]> xLabels =new ArrayList<String[]> ();
		List<String[]> newXLabels  =new ArrayList<String[]> ();
		if(cateMap!=null){
			Set<String> keys = cateMap.keySet(); 
			if(keys!=null){
				 
				for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
				 
						xLabels.add(new String[]{cateMap.get(key),key} ) ;
						if(key.length()> OutPutVisualAdapter.MAX_LENGTH_ROTATION_DEFAULT){
							scatterModel.setxLableRotation(OutPutVisualAdapter.DEFAULT_XLABEL_ROTATION) ;
						}
					 
					 
				}
				Collections.sort(xLabels,new Comparator<String[]>(){

					@Override
					public int compare(String[] o1, String[] o2) {
						return (Integer.valueOf(o1[0]).compareTo(Integer.valueOf(o2[0]) )) ;
					}
					 
				});
				if(keys.size()>step){
					int index =1;
					for (Iterator iterator = xLabels.iterator(); iterator.hasNext();) {
						String[] labels = (String[]) iterator.next();
						if(index%step==0){
							newXLabels.add(labels ) ;
							if(labels[1].length()> OutPutVisualAdapter.MAX_LENGTH_ROTATION_DEFAULT){
								scatterModel.setxLableRotation(OutPutVisualAdapter.DEFAULT_XLABEL_ROTATION) ;
							}
						}
						index++;
					}
				}else{
					newXLabels=xLabels;
				}
		}
			
			
		}		
		scatterModel.setxLabels(newXLabels);
	}




	private void fillCateMap(String referenceColumnType,
			Map<String, String> cateMap, List<DataRow> dataRows) {
		if(null!=referenceColumnType && ("cate".equalsIgnoreCase(referenceColumnType)|| "date".equalsIgnoreCase(referenceColumnType))){
			if(null!=dataRows && dataRows.size()>0){
				//
				boolean isNaNX = false;
				boolean isNaNY = false;
				try {
					
					Float.valueOf(dataRows.get(0).getData(1));
				} catch (Exception e) {
					// x is not number scatter
					isNaNX = true;
				}
				try {
					
					Float.valueOf(dataRows.get(0).getData(0));
				} catch (Exception e) {
					// y is not number scatter
					isNaNY = true;
				}
				
				int x=1;
				int y =1;
				for(int i=0;i<dataRows.size();i++){
					if(isNaNX){
						//avoid the duplicated x ...
						if(cateMap.containsKey(dataRows.get(i).getData(1))==false){
							cateMap.put(dataRows.get(i).getData(1),String.valueOf(x));
							x++;
						}
					}
					if(isNaNY){
						if(cateMap.containsKey(dataRows.get(i).getData(0))==false){
							cateMap.put(dataRows.get(i).getData(0),String.valueOf(y));
							y++;
						}
					}
				}
			}
		}
	}
	

	public static boolean drawScatterLine(VisualizationModelScatter scatterModel,
			MaxMinAxisValue maxMin, VisualPointGroup visualPointGroup) {
		List<VisualPoint> points = visualPointGroup.getPoints();
		
		double[][] xyLinedataset = new double[points.size()][2];
		double minX=0;
		double maxX=0;
		for(int i=0;i<points.size();i++){
			double x= Double.parseDouble(points.get(i).getX()) ;
			xyLinedataset[i][0]=x;
			xyLinedataset[i][1]=Double.parseDouble(points.get(i).getY());
			if (x>maxX){
				maxX=x;
			}
			if (x<minX){
				minX=x;
			}
		}
		//add by Will MINERWEB-939
		try {
			double ab[] =getOLSRegression(xyLinedataset );
			double a =ab[0];
			double b =ab[1];
			double minY=a+b*minX;
			double maxY=a+b*maxX;	
			maxMin.compareXY(minX,minY);
			maxMin.compareXY(maxX,maxY);
			//draw a line ...
			if(String.valueOf(minX).equals("NaN")==false
					&&String.valueOf(minY).equals("NaN")==false
					&&String.valueOf(maxX).equals("NaN")==false
					&&String.valueOf(maxY).equals("NaN")==false){
				VisualLine visualLine = new VisualLine(visualPointGroup.getLabel()); 
				visualLine.addVisualPoint( new VisualPoint(String.valueOf(minX),String.valueOf(minY))) ;
				visualLine.addVisualPoint( new VisualPoint(String.valueOf(maxX),String.valueOf(maxY))) ;
				scatterModel.addVisualLine(visualLine);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	
	}
	public static boolean drawScatterLine4Regression(VisualizationModelScatter scatterModel,
			MaxMinAxisValue maxMin, VisualPointGroup visualPointGroup) {
		List<VisualPoint> points = visualPointGroup.getPoints();

		double[][] xyLinedataset = new double[points.size()][2];
		double minX=0;
		double maxX=0;
		for(int i=0;i<points.size();i++){
			double x= Double.parseDouble(points.get(i).getX()) ;
			xyLinedataset[i][0]=x;
			xyLinedataset[i][1]=Double.parseDouble(points.get(i).getY());
			if (x>maxX){
				maxX=x;
			}
			if (x<minX){
				minX=x;
			}
		}
		try {
			double ab[] =getOLSRegression(xyLinedataset );
			double a =ab[0];
			double b =ab[1];
			double minY=a+b*minX;
			double maxY=a+b*maxX;
//			maxMin.compareXY(minX,minY);
//			maxMin.compareXY(maxX,maxY);
			//draw a line ...
			if(String.valueOf(minX).equals("NaN")==false
					&&String.valueOf(minY).equals("NaN")==false
					&&String.valueOf(maxX).equals("NaN")==false
					&&String.valueOf(maxY).equals("NaN")==false){
				VisualLine visualLine = new VisualLine(visualPointGroup.getLabel());
				visualLine.addVisualPoint( new VisualPoint(String.valueOf(minX),String.valueOf(0))) ;
				visualLine.addVisualPoint( new VisualPoint(String.valueOf(maxX),String.valueOf(0))) ;
				scatterModel.addVisualLine(visualLine);
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	private void drawVisualLine4Scatter(VisualizationModelScatter scatterModel,
			HashMap<String, VisualPointGroup> pointGroupMap, MaxMinAxisValue maxMin) {
		Collection<VisualPointGroup> values = pointGroupMap.values();
		//MINERWEB-939 
		boolean isAllCanDrawLine = true;		
		Iterator<VisualPointGroup> vpg = values.iterator();
		double minXValue = 0D;
		if(vpg.hasNext()){
			minXValue =  Double.parseDouble(vpg.next().getPoints().get(0).getX());
		}
		
		for (Iterator iterator = values.iterator(); iterator
				.hasNext();) {
			VisualPointGroup visualPointGroup = (VisualPointGroup) iterator
					.next();
			boolean canDrawLine = drawScatterLine(scatterModel, maxMin, visualPointGroup);
			isAllCanDrawLine &= canDrawLine;
			if(!canDrawLine){
				double currentXValue = Double.parseDouble(visualPointGroup.getPoints().get(0).getX());
				minXValue = currentXValue < minXValue ? currentXValue : minXValue;
			}
		
			scatterModel.addVisualPointGroup(visualPointGroup);
		}
		if(!isAllCanDrawLine){
			maxMin.setMinX(minXValue);
			scatterModel.setMinX(Double.toString(minXValue));
		}
	}
	
	@Override
	public VisualizationModel getTimeSeriesVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String idColumn,
			String valueColumn, String groupByColumn, String resourceType,
			String isGeneratedTable, String user, Locale locale)
			throws Exception   {
		dbTableName = VisualUtils.refineTableName(dbSchemaName, dbTableName, user ,  isGeneratedTable);
		String wholeTableName;
		
		if(dbSchemaName==null||dbSchemaName.isEmpty()){
			wholeTableName=StringHandler.doubleQ(dbTableName);
		}else{
			wholeTableName=StringHandler.doubleQ(dbSchemaName)+"."+StringHandler.doubleQ(dbTableName);
		}
		DbConnectionInfo dbinfo = ResourceManager.getInstance().getDBConnection(user, dbConnName,getResourceType(resourceType));
		VisualizationModel tableModel =null;
		Connection connection= createDBConnection(dbinfo); 
		try{
			String idColumnDataType=
				getColumnType(user, dbConnName, dbSchemaName, dbTableName,idColumn, getResourceType(resourceType));
			
			tableModel = generateTimeSeriesModel( groupByColumn,  idColumn,
					  valueColumn,  connection,
					  wholeTableName,idColumnDataType,dbinfo.getConnection().getDbType(),locale);
		}catch (Exception e){
			throw e;
		}finally{
			if(connection!=null){
				connection.close();
			}
		}
		return tableModel;
	}
	

	/**
	 * @param idColumn
	 * @param valueColumn
	 * @param connection
	 * @param wholeTableName
	 * @param locale 
	 * @return
	 * @throws Exception 
	 */
	private VisualizationModel generateTimeSeriesModel(String groupByColumn,String idColumn,
			String valueColumn, Connection connection, String wholeTableName,
			String idColumnDataType,String dataSystem, Locale locale) throws Exception {
		VisualizationModel  model= null;
		if(StringUtil.isEmpty(groupByColumn)){
			
			model = generateModelWithoutGroupBy(
				idColumn, valueColumn, connection, wholeTableName,
				idColumnDataType, dataSystem,locale);
		}else{
			model = generateModelWithGroupBy(groupByColumn,
					idColumn, valueColumn, connection, wholeTableName,
					idColumnDataType, dataSystem,locale);
		}
		
		return model;
 
	}



	

	private VisualizationModelLine createMarkedLineModel(String xColumn,
			String yColumn, List<String[]> xLabels, VisualLine line,String title,int index, String[] precision)  {
		List<VisualLine> lines = new ArrayList<VisualLine>(); 
		lines.add(line);
		//line.setColor(OutPutVisualAdapter.CONST_Colors[index/OutPutVisualAdapterFactory.getInstance().getMaxChartElements()]);
		
		VisualizationModelLine  lineModel=new VisualizationModelLine(title, lines);
  	
		lineModel.setMarkers(true);
		lineModel.setxAxisTitle(xColumn + precision[0]);
		lineModel.setyAxisTitle(yColumn + precision[1]) ;
		lineModel.setWidth(900) ;
		lineModel.setHeight(600) ;
	
	 
		lineModel.setxLabels(xLabels) ;
	//	lineModel.setyLabels(yLabels) ;
		
		
		
		return lineModel;
	}

	/**
	 * @param dbinfo
	 * @return
	 * @throws AnalysisException 
	 */
	private Connection createDBConnection(	DbConnectionInfo dbinfo ) throws AnalysisException {
		Connection connection=null;
		try {
			connection = AlpineUtil.createConnection(dbinfo.getConnection());
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		return connection;
	}
	
	
	/**
	 * @param groupByColumn
	 * @param idColumn
	 * @param valueColumn
	 * @param connection
	 * @param wholeTableName
	 * @param idColumnDataType
	 * @param dataSystem
	 * @param locale 
	 * @return
	 * @throws Exception 
	 */
	private VisualizationModel generateModelWithGroupBy(String groupByColumn,
			String idColumn, String valueColumn, Connection connection,
			String wholeTableName, String idColumnDataType, String dataSystem, Locale locale) throws Exception {
  
		
		List<VisualLine> lines = new ArrayList<VisualLine>();
		List<String[]> xLabels = new ArrayList<String[]> ();
		List<String[]> yLabels = new ArrayList<String[]> ();
		
		ResultSet rs=null;
		Statement st=connection.createStatement();
		StringBuilder sb=new StringBuilder();
		sb.append("select ").append(StringHandler.doubleQ(groupByColumn)).append(",");
		sb.append("count(").append(StringHandler.doubleQ(idColumn)).append("),");
		sb.append("count(distinct ").append(StringHandler.doubleQ(idColumn)).append(")");
		sb.append(" from ").append(wholeTableName).append(" group by ").append(StringHandler.doubleQ(groupByColumn));

		rs=st.executeQuery(sb.toString());
		LinkedHashMap<String, Long> originalCountMap = new LinkedHashMap<String, Long>(); 
		
		LinkedHashMap<String, Long> countMap = new LinkedHashMap<String, Long>();
		while(rs.next()){
			String groupByColumnValue=null;
			long tableCount=0;
			long idDistinctCount=0;
			groupByColumnValue=rs.getString(1);
			tableCount=rs.getLong(2);
			idDistinctCount=rs.getLong(3);
			if(tableCount!=idDistinctCount){ 
				throw new IOException(ErrorNLS.getMessage(ErrorNLS.Timeseries_ID_Not_Dicsinct, locale)+idColumn);

			 
			}
			originalCountMap.put(groupByColumnValue, tableCount);
		}
		
		List<String> errorMessages = new ArrayList<String>();
		int groupNumbers =originalCountMap.keySet().size(); 
		int max_lines = OutPutVisualAdapterFactory.getInstance().getMaxChartElements(); 
		if (groupNumbers > max_lines){
			errorMessages.add(VisualNLS.getMessage(VisualNLS.BARS_EXCEED_LIMIT, locale, max_lines)); 
			Iterator<String> keys = originalCountMap.keySet().iterator();
			for (int i = 0; i < max_lines; i++) {
				String key = keys.next();
				countMap.put(key, originalCountMap.get(key)) ;
			}
			
			
		}else{
			countMap = originalCountMap;
		}
		int index=0;
		boolean isDateType= ParameterUtility.isDateColumnType(idColumnDataType, dataSystem) 
								||ParameterUtility.isDateTimeColumnType(idColumnDataType, dataSystem)
								||ParameterUtility.isPureDateColumnType(idColumnDataType, dataSystem)
								||ParameterUtility.isTimeColumnType(idColumnDataType, dataSystem);
		 
		HashMap<String, String> xLabelMap = new HashMap<String, String>();
		
		//For time series chart precsion add by Will
		String[] precision = new String[2];
		precision[0] = "";
		precision[1] = "";
		
		if(isDateType==true){ 
 		
			
			Iterator<Entry<String, Long>>  iter_count=countMap.entrySet().iterator();
			
			while(iter_count.hasNext()){
				Entry<String, Long> entry=iter_count.next();
				String groupByColumnValue=entry.getKey();
				long tableCount=entry.getValue();
				executeGroupBySql(idColumn, valueColumn, groupByColumn,
						wholeTableName, sb, groupByColumnValue, tableCount);
				
				rs=st.executeQuery(sb.toString());
				
				
				VisualLine trainedLine = createTimeSeriesLine(groupByColumnValue,
						idColumnDataType, dataSystem, rs, xLabels, yLabels, xLabelMap,isDateType,precision);
				lines.add(trainedLine) ;
				 
				index=index+1;
	 				
			}
	 			
			
		}else {//numeric type
 
			Iterator<Entry<String, Long>>  iter_count=countMap.entrySet().iterator();
			while(iter_count.hasNext()){
				Entry<String, Long> entry=iter_count.next();
				String groupByColumnValue=entry.getKey();
				long tableCount=entry.getValue();
				executeGroupBySql(idColumn, valueColumn, groupByColumn,
						wholeTableName, sb, groupByColumnValue, tableCount);
  									
				rs=st.executeQuery(sb.toString());
	 
				VisualLine trainedLine = createTimeSeriesLine(groupByColumnValue,
						idColumnDataType, dataSystem, rs, xLabels, yLabels, null,isDateType,precision); 
				lines.add(trainedLine) ;
//				trainedLine.setColor(OutPutVisualAdapter.CONST_Colors[index]) ;
				index=index+1;
			}	
				
		}		
 		
		  
		String title = VisualNLS.getMessage(VisualNLS.TIMESERIES_PREDICTION_SHARP,locale) ; 

		VisualizationModelLine  lineModel=new VisualizationModelLine(title, lines);
	  	
		lineModel.setMarkers(true);
		//For time series line Modify by Will
		lineModel.setxAxisTitle(idColumn + precision[0]);
		lineModel.setyAxisTitle(valueColumn + precision[1]) ;
		
		
//		lineModel.setWidth(900) ;
//		lineModel.setHeight(600) ;
	
		 if(isDateType == true){
			 
			VisualAdapterARIMARPredictor.handleDateLineModelLabel(lineModel,xLabelMap);			 	
			 }
		 else{
			 VisualAdapterARIMARPredictor.handleNumericModelLabel(lineModel,xLabels);
		 }
 
		
		if(errorMessages.size()>0){
			lineModel.setErrorMessage(errorMessages);
		}
	//	VisualUtils.setXMaxAndMin(lineModel);
		return lineModel;
	}

	 


	private VisualizationModelLine generateModelWithoutGroupBy(String idColumn,
			String valueColumn, Connection connection, String wholeTableName,
			String idColumnDataType, String dataSystem, Locale locale) throws Exception {
		Statement st=connection.createStatement();
		StringBuilder sb=new StringBuilder();
		sb.append("select count(*) ,count(distinct ").append(StringHandler.doubleQ(idColumn));
		sb.append(" )from ").append(wholeTableName); 
		
		
		ResultSet rs=st.executeQuery(sb.toString());
		long tableCount=0;
		long idDistinctCount=0;
		 
		while(rs.next() ){
			tableCount=rs.getLong(1);
			idDistinctCount=rs.getLong(2);
			 
		}
		if(tableCount!=idDistinctCount){ 
			throw new IOException(ErrorNLS.getMessage(ErrorNLS.Timeseries_ID_Not_Dicsinct, locale)+idColumn);
		}
		sb.setLength(0);
		String maxRows = ResourceManager.getInstance().getPreferenceProp(
	 			PreferenceInfo.GROUP_UI, PreferenceInfo.MAX_CLUSTER_POINTS) ; 

		sb.append("select ").append(StringHandler.doubleQ(valueColumn)).append(",");
		sb.append(StringHandler.doubleQ(idColumn)).append(" from (select ");
		sb.append(StringHandler.doubleQ(valueColumn)).append(",");
		sb.append(StringHandler.doubleQ(idColumn)).append(",row_number() over (order by ");
		sb.append(StringHandler.doubleQ(idColumn)).append(") as alpine_rownum from ");
		sb.append(wholeTableName).append(") foo where mod((alpine_rownum-1)*");
		//
		sb.append( maxRows ).append( ",") .append(tableCount).append( ")<").append( maxRows);
//		sb.append(tableCount).append("*1.0/").append( 
//				ResourceManager.getInstance().getPreferenceProp( 
//						PreferenceInfo.GROUP_UI, PreferenceInfo.KEY_MAX_TIMESERIES_POINTS)).append(")<1");
		itsLogger.debug("TimeSeriesActionrun.run():sql="+sb.toString());
		rs=st.executeQuery(sb.toString());
		
		
		List<String[]> xLabels = new ArrayList<String[]> ();
		List<String[]> yLabels = new ArrayList<String[]> ();
		HashMap<String, String> xLabelMap = new HashMap<String, String>  ();
		boolean isDateType= ParameterUtility.isDateColumnType(idColumnDataType, dataSystem) 
							||ParameterUtility.isDateTimeColumnType(idColumnDataType, dataSystem)
							||ParameterUtility.isPureDateColumnType(idColumnDataType, dataSystem)
							||ParameterUtility.isTimeColumnType(idColumnDataType, dataSystem);
		
		//For time series chart precsion add by Will
		String[] precision = new String[2];
		precision[0] = "";
		precision[1] = "";
		
		VisualLine trainedLine = createTimeSeriesLine(valueColumn,
				idColumnDataType, dataSystem, rs, xLabels, yLabels, xLabelMap,isDateType,precision);
		
		String title = VisualNLS.getMessage(VisualNLS.TIMESERIES_PREDICTION_SHARP, locale) ; 
		
		
		VisualizationModelLine lineModel = createMarkedLineModel(idColumn,
				valueColumn, xLabels, trainedLine,title,0,precision);
		
		 if(isDateType == true){
			 
				VisualAdapterARIMARPredictor.handleDateLineModelLabel(lineModel,xLabelMap);			 	
		 }
		 else{
			 VisualAdapterARIMARPredictor.handleNumericModelLabel(lineModel,xLabels);
		 }
		 
		 return lineModel;
	}

	private VisualLine createTimeSeriesLine(String title,
			String idColumnDataType, String dataSystem, ResultSet rs,
			List<String[]> xLabels, List<String[]> yLabels, HashMap<String, String> xLabelMap, boolean isDateType,String[] precision) throws SQLException { 
		//for number title begin
		if(null!=title){
			boolean canParseTitleToNumber = false;
			try {
				Float.parseFloat(title);
				canParseTitleToNumber = true;
			} catch (NumberFormatException e) {
				canParseTitleToNumber = false;
			}
			if(canParseTitleToNumber==true){
				NumberFormat nf = new DecimalFormat("0.00E0");
				title = nf.format(Float.parseFloat(title));
			}
		}
		// end
		VisualLine trainedLine = new VisualLine(title);
		if(isDateType==true){ 
			    List<List> dataList = new ArrayList<List>();
				while(rs.next()){
					List<Object> tempList = new ArrayList<Object>();
					String value=rs.getString(1);		
					java.util.Date d =  rs.getDate(2);
					tempList.add(value);
					tempList.add(d);
					dataList.add(tempList);
					  /*if(null!=d){
						  long timemills = d.getTime();
						  String xValue = String.valueOf(timemills);
						  VisualPoint point = new VisualPoint(xValue,value);
						  trainedLine.addVisualPoint(point);
						  xLabels.add(new String[]{xValue,d.toString()});
						  xLabelMap.put(xValue, d.toString()) ;
						  yLabels.add(new String[]{value,value});
					  }*/
				}
				if(dataList.size()>0){
					float valueMax = 0.0f;
					float valueMin = 0.0f;
					try {
						valueMax = Float.valueOf(dataList.get(0).get(0).toString());
						valueMin = Float.valueOf(dataList.get(0).get(0).toString());
					} catch (Exception e) {}
					
					for (int i = 0; i < dataList.size(); i++) {
						if(dataList.get(i).get(0)!=null){
							if(valueMax<Float.valueOf(dataList.get(i).get(0).toString())){
								try {
									valueMax = Float.valueOf(dataList.get(i).get(0).toString());
								} catch (Exception e) {
								}
							}
							if(valueMin>Float.valueOf(dataList.get(i).get(0).toString())){
								try {
									valueMin = Float.valueOf(dataList.get(i).get(0).toString());
								} catch (Exception e) {
								}
							}
						}
					}
					float n = AlpineMath.adjustUnits(valueMin, valueMax);
					if(n==0.0f){
						n = 1f;
					}
					if(n!=1.0f){
						precision[1] = " ("+VisualUtils.getScientificNumber(n)+")";
					}else{
						precision[1] = "";
					}
					for (int i = 0; i < dataList.size(); i++) {
						if(dataList.get(i).get(0)!=null && dataList.get(i).get(1)!=null){
							long timemills = 0l;
							String value = String.valueOf(Float.valueOf(dataList.get(i).get(0).toString())/n);
							java.util.Date d = (Date) dataList.get(i).get(1);
							timemills =d.getTime();
							String xValue = String.valueOf(timemills);
							VisualPoint point = new VisualPoint(xValue,value);
							trainedLine.addVisualPoint(point);
							xLabels.add(new String[]{xValue,d.toString()});
							xLabelMap.put(xValue, d.toString()) ;
							yLabels.add(new String[]{value,value});
						}
					}
					
					
				}
			
		}else { //numeric type
			try {
			   List<String[]> ptlist= new ArrayList<String[]>();
             
				while(rs.next()){
					String id=rs.getString(2);	
					String value=rs.getString(1);		
					ptlist.add(new String[]{id,value});
					//VisualPoint point = new VisualPoint(id,value);
					//trainedLine.addVisualPoint(point);
					//xLabels.add(new String[]{id,id});
					////yLabels.add(new String[]{value,value});
				}
				if(ptlist.size()>0){
					float maxId = 0.0f;
					float minId = 0.0f;
					float maxValue = 0.0f;
					float minValue = 0.0f;
					for (int i = 0; i < ptlist.size(); i++) {
						boolean canParseId = true;
						boolean canParseValue = true;
						String id = ptlist.get(i)[0];
						String value = ptlist.get(i)[1];
						float tmpId = 0.0f;
						float tmpValue = 0.0f;
						try {
							tmpId=Float.valueOf(id);
						} catch (Exception e) {
							// TODO: handle exception
						}
						try {
							tmpValue=Float.valueOf(value);
						} catch (Exception e) {
							// TODO: handle exception
						}
						if(maxId<tmpId){
							maxId= tmpId;
						}
						if(minId>tmpId){
							minId= tmpId;
						}
						if(maxValue<tmpValue){
							maxValue= tmpValue;
						}
						if(minValue>tmpValue){
							minValue= tmpValue;
						}
						
					}
					float n = AlpineMath.adjustUnits(minId, maxId);
					float m = AlpineMath.adjustUnits(minValue, maxValue);
					if(n==0.0f){
						n=1.0f;
					}
					if(m==0.0f){
						m=1.0f;
					}
					if(n!=1){
						precision[0] = " ("+VisualUtils.getScientificNumber(n)+")";
					}else{
						precision[0] = "";
					}
					if(m!=1){
						precision[1] = " ("+VisualUtils.getScientificNumber(m)+")";
					}else{
						precision[1] = "";
					}
					
					
					
					for (int i = 0; i < ptlist.size(); i++) {
						String id = ptlist.get(i)[0];
						String value = ptlist.get(i)[1];
						float tmpId = 0.0f;
						float tmpValue = 0.0f;
						try {
							tmpId=Float.valueOf(id);
						} catch (Exception e) {
							// TODO: handle exception
						}
						try {
							tmpValue=Float.valueOf(value);
						} catch (Exception e) {
							// TODO: handle exception
						}
						VisualPoint point = new VisualPoint(String.valueOf(tmpId/n),String.valueOf(tmpValue/m));
						trainedLine.addVisualPoint(point);
						xLabels.add(new String[]{point.getX(),point.getX()});
						
					}
					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return trainedLine;
	}
	
	private void executeGroupBySql(String idColumn, String valueColumn,
			String groupByColumn, String wholeTableName, StringBuilder sb,
			String groupByColumnValue, long tableCount) throws Exception {
		sb.setLength(0);
		String maxRows=ProfileReader.getInstance(false).getProperties().getProperty(OutPutVisualAdapter.UI_PARA2);

            sb.append("select ").append(StringHandler.doubleQ(valueColumn)).append(",");
            sb.append(StringHandler.doubleQ(idColumn)).append(" from (select ");
            sb.append(StringHandler.doubleQ(valueColumn)).append(",");
            sb.append(StringHandler.doubleQ(idColumn)).append(",row_number() over (order by ");
            sb.append(StringHandler.doubleQ(idColumn)).append(") as alpine_rownum from ");
            sb.append(wholeTableName).append(" where ").append(StringHandler.doubleQ(groupByColumn)).append("='").append(null==groupByColumnValue?0:groupByColumnValue).append("'");
            sb.append(") foo where mod((alpine_rownum-1)*");
            sb.append( maxRows ).append( ",") .append(tableCount).append( ")<").append( maxRows);

//		sb.append(tableCount).append("*1.0/").append(
//				ResourceManager.getInstance().getPreferenceProp( 
//						PreferenceInfo.GROUP_UI, PreferenceInfo.KEY_MAX_TIMESERIES_POINTS)).append(")<1");//AlpineMinerUIConfig.TIME_SERIES_CHART_MAX
		
		itsLogger.debug("DataExplorerController.generateChartWithGroupByColumn():sql="+sb.toString());
	}

	@Override
	public void clearIntermediaryFile(String userName, ClearFileInfo clearFileInfo)
			throws Exception {
		HadoopConnection hadoopConnetion = HadoopConnectionManagerFactory.INSTANCE.getManager().
		readHadoopConnection(clearFileInfo.getConnectionName(), userName);
		String needAddPrefix = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_DB,PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX);
		String filePath = clearFileInfo.getFilePath();

		filePath = Resources.TrueOpt.equals(needAddPrefix) ? VisualUtils.refineFileName(filePath, userName, false) : filePath; 
		
		HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(filePath, hadoopConnetion);
	}
	/**
	 * @param userName
	 * @param clearnInfo
	 * @throws Exception
	 */
	@Override
	public   void clearIntermediaryTable(String userName,ClearTableInfo clearnInfo) throws Exception{

		Connection connection = null;
		try {
			DbConnectionInfo dbinfo = ResourceManager.getInstance().getDBConnection(userName, clearnInfo.getConnectionName(),getResourceType(clearnInfo.getResourceType()));
			connection= createDBConnection(dbinfo); 
			String needAddPrefix = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_DB,PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX);
			String tableName ="true".equals(needAddPrefix) ? VisualUtils.refineTableName(clearnInfo.getSchemaName(), clearnInfo.getTableName(), userName, needAddPrefix) : clearnInfo.getTableName(); 
			AlpineUtil.dropTable(connection, 
					clearnInfo.getSchemaName(), 
									tableName, 
									clearnInfo.getOutputType(), 
									dbinfo.getConnection().getDbType());
			//also here need refresh the cache
			//MINERWEB-689 DB metadata cache for Illuminator like alpine miner eclipse
			 DBMetadataManger.INSTANCE.removeTableFromCache(dbinfo.getConnection(),clearnInfo.getSchemaName(),tableName);
			
		} catch (Exception e) {
			throw e;
		}finally{
			if(connection!=null){
//				connection.commit();
				connection.close();
			}
		}
	}
	
	@Override
	public  VisualizationModelComposite getUniverateVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String referenceColumn, String resourceType,
			String isGeneratedTable, String user ,Locale locale) throws Exception {
		VisualizationModelComposite visualModel=null;
		DbConnectionInfo connInfo = ResourceManager.getInstance()
				.getDBConnection(user, dbConnName,getResourceType(resourceType));
		
		dbTableName = VisualUtils.refineTableName(dbSchemaName, dbTableName, user,  isGeneratedTable);
		if (connInfo != null && connInfo.getConnection() != null) {

			DBDataUtil util = new DBDataUtil(connInfo.getConnection());
			String[] allSelectedColumns=createAllSelectedColumns(columnNameIndex,referenceColumn);
			DataTable datatable = util.getSampleTableDataList(dbSchemaName, dbTableName, 
					allSelectedColumns,"60",    referenceColumn);
			//0 is refrence column
			
	       List<DataRow> rows = datatable.getRows();
			
			//For precision add by Will
	        float[] precisionArray = new float[rows.get(0).getData().length];
		    if(null!=rows && rows.size()>0){
		    	float[] max = new float[rows.get(0).getData().length];
		    	float[] min = new float[rows.get(0).getData().length];
		        String [] initMaxMinValues = rows.get(0).getData();
		        if(null!=initMaxMinValues && initMaxMinValues.length>0){
		        	for (int l = 0; l < initMaxMinValues.length; l++) {
		        		try {
		        			max[l] = Float.valueOf(initMaxMinValues[l]);
			        		min[l] = Float.valueOf(initMaxMinValues[l]);
						} catch (NumberFormatException e) {
						}
		        		
					}
		        }
		        
		    	for (int i = 0; i < rows.size(); i++) {
		    		String[] valueItems = rows.get(i).getData();
		    		if(null!=valueItems && valueItems.length>0){
		    			for (int j = 0; j < valueItems.length; j++) {
							if(max[j]<Float.valueOf(valueItems[j])){
								max[j] = Float.valueOf(valueItems[j]);
							}
							if(min[j]>Float.valueOf(valueItems[j])){
								min[j] = Float.valueOf(valueItems[j]);
							}
						}
		    		}
				}
		    	
		    	 
		    	for (int i = 0; i < max.length; i++) {
		    		precisionArray[i] = AlpineMath.adjustUnits(min[i], max[i]);		 
				}

		    	for (int i = 0; i < rows.size(); i++) {
		    		String[] valueItems = rows.get(i).getData();
		    		if(null!=valueItems && valueItems.length>0){
		    			for (int j = 0; j < valueItems.length; j++) {
		    				valueItems[j] = String.valueOf(Float.valueOf(valueItems[j])/precisionArray[j]);
						}
		    			rows.get(i).setData(valueItems);
		    		}
				}
		    	

		    	
		    	
		    }
			
			//
		
			
			List<VisualizationModel> lineModels = new ArrayList<VisualizationModel>(); 
			
			for (int i = 1; i < allSelectedColumns.length; i++) {
				VisualLine line = new VisualLine(allSelectedColumns[i]);
				//no x lable,  the dojo will generate automatically
				String[] forPrecision = new String[]{"",""};
				if(precisionArray[0]!=1){
					forPrecision[0] = "   ("+VisualUtils.getScientificNumber(precisionArray[0])+")"; //x is first in array
				}
				if(precisionArray[i]!=1){
					forPrecision[1] = "   ("+VisualUtils.getScientificNumber(precisionArray[i])+")"; 
				}
				
				VisualizationModelLine linemodel = createMarkedLineModel(referenceColumn,allSelectedColumns[i],null,line,allSelectedColumns[i],i,forPrecision);
			
				lineModels.add(linemodel) ;
			}
			
		
			
//				PointUltra ultra = PointUltra.getInstance(allSelectedColumns);
			for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
				DataRow dataRow = (DataRow) iterator.next();
				 
				for (int i = 1; i < allSelectedColumns.length; i++) {
					VisualizationModelLine lineModel=(VisualizationModelLine)lineModels.get(i-1);
					
					String xValue = dataRow.getData(0) ;
					String	yValue=dataRow.getData(i) ;
//						VisualPoint point = ultra.loadPoint(allSelectedColumns[i - 1], new VisualPoint(xValue, yValue));
//						if(point == null){
//							continue;
//						}
					lineModel.getLines().get(0).addVisualPoint(new VisualPoint(xValue, yValue)) ;

					
				}
			}
			for (Iterator iterator = lineModels.iterator(); iterator.hasNext();) {
				VisualizationModelLine lineModel =(VisualizationModelLine) iterator.next(); 
				VisualUtils.setAxisMaxAndMin(lineModel,true,true,null);
				  VisualUtils.autoGenerateAxisLabel(lineModel,true,false,null);
			 
			}
			
		
		
			
			  visualModel = new VisualizationModelComposite("",lineModels);
			
			
			
		}
		return visualModel;
	}
	
	
	/**
	 * @param columnNameIndex
	 * @param referenceColumn
	 * @return
	 */
	private String[] createAllSelectedColumns(String columnNameIndex,
			String referenceColumn) {
		String[] names = columnNameIndex.split(",") ;
		List <String> nameList= new ArrayList <String>();
		nameList.add(referenceColumn) ;
		for (int i = 0; i < names.length; i++) {
			if(names[i]!=null&& names[i].trim().length()>0){
				nameList.add(names[i]) ;
			}
		}
		
		return ( String[] )nameList.toArray(new String[nameList.size()] );
	}

	
	private static String  getColumnType  (String user, String conn, String schema,
			String table,String column,ResourceType dbType) throws Exception {
		
		DbConnectionInfo info = WebDBResourceManager.getInstance().getDBConnection(user, conn,dbType);
		if (info == null) {
			return null;
		}
		DBMetaDataUtil util = new DBMetaDataUtil(info.getConnection());
		ArrayList<String[]> list  = null;
		try{
			util.setJudgeConnection(true);
			list = util.getColumnList(schema, table);
		}catch(Exception e){
			throw e;
		}finally{
			if(util!=null){
				util.disconnect();
			}	
		}
 
		if(list!=null){
			for (String[] name : list) {
				if(column.equals(name[0])){
					return name[1];
				}
			}
		}
		return null;
	}
	
	public static double[] getOLSRegression(double[][] data) {

		int n = data.length;
		if (n < 2) {
			throw new IllegalArgumentException("Not enough data.");
		}

		double sumX = 0;
		double sumY = 0;
		double sumXX = 0;
		double sumXY = 0;
		for (int i = 0; i < n; i++) {
			double x = data[i][0];
			double y = data[i][1];
			sumX += x;
			sumY += y;
			double xx = x * x;
			sumXX += xx;
			double xy = x * y;
			sumXY += xy;
		}
		double sxx = sumXX - (sumX * sumX) / n;
		double sxy = sumXY - (sumX * sumY) / n;
		double xbar = sumX / n;
		double ybar = sumY / n;

		double[] result = new double[2];
		result[1] = sxy / sxx;
		result[0] = ybar - result[1] * xbar;

		return result;

	}
}
