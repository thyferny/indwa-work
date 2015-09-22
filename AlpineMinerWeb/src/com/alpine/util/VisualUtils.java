/**   
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 * ClassName AlpineUtil.java
 *   
 * Author   john zhao   
 *
 * Version  Ver 3.0
 *   
 * Date     2011-7-19    
 * 

 */

package com.alpine.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.result.OutPutVisualAdapter;
import com.alpine.miner.impls.result.OutPutVisualAdapterFactory;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.output.visual.BarchartSeries;
import com.alpine.miner.workflow.output.visual.VisualLine;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelBarChart;
import com.alpine.miner.workflow.output.visual.VisualizationModelChart;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelLine;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

public class VisualUtils {
	public static final int LABEL_NUMBER = 10;
	public static final String N_A = "N/A";
   
	public static String refineTableName(String dbSchemaName, String dbTableName,
			String user,String isGeneratedTable) {
		String schemaStr= dbSchemaName+".";
		//avoid the duplicated schema in the table name...
		if(dbTableName.startsWith(schemaStr)){
			dbTableName=dbTableName.substring(schemaStr.length(),dbTableName.length()) ;
		}
		//add table  prefix
		if(isGeneratedTable.equalsIgnoreCase("true")
				&&ProfileReader.getInstance(false).getParameter(ProfileUtility.UI_ADD_PREFIX).equalsIgnoreCase("true")){ 
			dbTableName=StringHandler.addPrefix(dbTableName, user) ;
		}
		return dbTableName;
	}

    public static String refineFileName(String filePath,String userName,boolean isHDFileOperator){
        // add file prefix
        if(ProfileReader.getInstance(false).getParameter(ProfileUtility.UI_ADD_PREFIX).equalsIgnoreCase("true") && isHDFileOperator==false){
            String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            String purePath = filePath.substring(0,filePath.lastIndexOf("/")+1);
            fileName = StringHandler.addPrefix(fileName, userName) ;
            filePath = purePath+fileName;
        }
        return filePath;
    }
	
	
	public static VisualizationModelLayered generateLayeredTableModel( String[] columnHeads,
			  List<String[]> rows, int nameColumnIndex,Locale locale, String[] columnTypes,
			  String labelMessage,String comboMessage) {
			
			
			List<String> keys = getColumnValue(rows,nameColumnIndex);
			HashMap<String, VisualizationModel> modelMap =createTableModelMap(keys,rows,columnHeads,  nameColumnIndex, columnTypes);
			VisualizationModelLayered visualizationModel= new VisualizationModelLayered(
					labelMessage,comboMessage,keys, modelMap);
	 	
			return visualizationModel; 	 
		}
	
	public static VisualizationModelLayered generateLayeredTableModel( String[] columnHeads,
		  List<String[]> rows, int nameColumnIndex,Locale locale, String[] columnTypes) {
		
		return generateLayeredTableModel(columnHeads,rows,nameColumnIndex,locale,columnTypes,
				VisualNLS.getMessage(VisualNLS.SUMMARY,locale),
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale));	 
	}
	

	public static VisualizationModel createTableModel(String colName,
			List<String[]> rows, String[] columnHeads,int nameColumnIndex, String[] columnType) {
		DataTable tables= new DataTable();
		List<TableColumnMetaInfo> tableColumns=new ArrayList<TableColumnMetaInfo> (); 
		for (int i = 0; i < columnHeads.length; i++) {
			tableColumns.add(new TableColumnMetaInfo(columnHeads[i],columnType[i]));
		}
		tables.setColumns(tableColumns) ;
		List<DataRow> tableRows =new ArrayList<DataRow> ();
		for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
			String[] row = (String[]) iterator.next();
			String tempColName= row[   nameColumnIndex];
			if(tempColName!=null&&(tempColName.equals(colName)
					//||tempColName.replace(" ", "").equals(colName)
					)){
				DataRow dataRow = new DataRow();
				dataRow.setData(row);
				tableRows.add(dataRow);
			}
			
		}
		tables.setRows(tableRows ) ;
		VisualizationModelDataTable dataTable = new VisualizationModelDataTable(colName,tables); 
		return dataTable;
	 
	}
	
	public static HashMap<String, VisualizationModel> createTableModelMap(
			List<String> keys, List<String[]> rows, String[] columnHeads,int nameColumnIndex, String[] columnTypes) {
		HashMap<String, VisualizationModel> modelMap= new HashMap<String, VisualizationModel> ();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String colName = (String) iterator.next(); 
			VisualizationModel model= createTableModel(colName,rows,columnHeads,  nameColumnIndex,columnTypes); 
			modelMap.put(colName, model); 
			
		}
		return modelMap;
	}
	
	public static List<String> getColumnValue(List<String[]> rows, int index) {
		List<String> results= new ArrayList<String>();
		for (Iterator<String[]> iterator = rows.iterator(); iterator.hasNext();) {
			String[] strings = (String[]) iterator.next();
		//avoid blank for the web error (html or dojo id can not contains blank)
			String colName=strings[index];//.replace(" ", "");
			if(results.contains(colName)==false){
				results.add(colName);
			}
		}
		return results;
	}
	

	public static  VisualizationModel generateValueCountBarChars(Map<String,String> precisionMap,String[] columnHeads,
			List<String[]> rows,int columnIndexColName, int columnIndexX, int columnIndexY,Locale locale) {
		
		List<String> keys = VisualUtils.getColumnValue(rows,columnIndexColName);
		HashMap<String, VisualizationModel> modelMap =createBarChartModelMap4Precision(precisionMap,columnHeads,keys,rows ,  columnIndexColName,   columnIndexX,   columnIndexY , locale);
		VisualizationModelLayered visualizationModel= new VisualizationModelLayered(
				VisualNLS.getMessage(VisualNLS.COUNT_SHAPE_ANALYSIS,locale),
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale) ,	 keys, modelMap);
 	
		return visualizationModel;
  
	}

	//bins label
	public static  VisualizationModel generateValueCountBarChars4Histogram(String[] columnHeads,
			List<String[]> rows,int columnIndexColName, int columnIndexX, int columnIndexY, 
			int columnIndexStart ,int columnIndexEnd,int columnIndexBin,Boolean isHadoop,Locale locale) {
		

		
		List<String> keys = VisualUtils.getColumnValue(rows,columnIndexColName);
		HashMap<String, VisualizationModel> modelMap =createBarChartModelMap(columnHeads,keys,rows ,  columnIndexColName,   columnIndexX,   columnIndexY,locale);
		//create bins label...
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String colName = (String) iterator.next(); 
			VisualizationModelBarChart model = (VisualizationModelBarChart)modelMap.get(colName) ;
		 
			boolean needRotation = false;
			List<String[]> xLabels = model.getxLabels();
			for (Iterator it = xLabels.iterator(); it.hasNext();) {
				String[] xLabel = ((String[])it.next());
				String bin = xLabel[0];
                if(isHadoop == true){
                   // bin = String.valueOf(Integer.parseInt(xLabel[0])-1);
                    bin = xLabel[1];
                }
				xLabel[1]= getXLabel(colName,bin,rows,columnIndexColName,columnIndexBin,columnIndexStart,columnIndexEnd);
				if(null!=xLabel[1] && xLabel[1].length()>OutPutVisualAdapter.MAX_LENGTH_ROTATION_DEFAULT){
					needRotation= true;
				}
			}
			if(needRotation==true){
				model.setxLableRotation(-30);
			} 
	 	
		}
		
		
		VisualizationModelLayered visualizationModel= new VisualizationModelLayered(
				VisualNLS.getMessage(VisualNLS.COUNT_SHAPE_ANALYSIS,locale) ,
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale) ,	 keys, modelMap);
 	
		return visualizationModel;
  
	}
	
	private static String getXLabel(String colName, String bin,
			List<String[]> rows,int columnIndexColName,int columnIndexBin,int columnIndexStart,int columnIndexEnd) {
		for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
			String[] row = (String[]) iterator.next(); 
			if(row[columnIndexColName].
					//replace(" ", "").
					equals(colName)
					&&row[columnIndexBin].equals(bin)){
				return row[columnIndexStart]+" - "+row[columnIndexEnd];
			}
		 		
		}
		return null;
	}


	public static  VisualizationModel generateValueCountLineChars(String title,String[] columnHeads,
			List<String[]> rows,int columnIndexColName, int columnIndexX, int columnIndexY,Locale locale) {
		
		List<String> keys = VisualUtils.getColumnValue(rows,columnIndexColName);
		HashMap<String, VisualizationModel> modelMap =createLineChartModelMap(columnHeads,keys,rows ,  columnIndexColName,   columnIndexX,   columnIndexY);
		VisualizationModelLayered visualizationModel= new VisualizationModelLayered(title,
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale) ,	 keys, modelMap);
 	
		return visualizationModel;
  
	}

	public static  HashMap<String, VisualizationModel> createBarChartModelMap(
			String[] columnHeads, List<String> keys, List<String[]> rows, int columnIndexColName, int columnIndexX, int columnIndexY, Locale locale ) {
		 
 	 	
		HashMap<String, VisualizationModel> modelMap= new HashMap<String, VisualizationModel> ();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String colName = (String) iterator.next(); 
			
			VisualizationModelBarChart model= createBarChartModel(colName,rows,columnIndexColName,   columnIndexX,  columnIndexY , locale);
			model.setxAxisTitle(columnHeads[columnIndexX]) ;
			model.setyAxisTitle(columnHeads[columnIndexY]);
			modelMap.put(colName, model); 
			
		}
		return modelMap;
	}
	public static  HashMap<String, VisualizationModel> createBarChartModelMap4Precision(Map<String,String> precisionMap,
			String[] columnHeads, List<String> keys, List<String[]> rows, int columnIndexColName, int columnIndexX, int columnIndexY, Locale locale ) {
		
		
		HashMap<String, VisualizationModel> modelMap= new HashMap<String, VisualizationModel> ();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String colName = (String) iterator.next(); 
			String forPrecisionTip = "";
			if(null!=precisionMap){
				String prcesionNumStr = precisionMap.get(colName);
				if(null!=prcesionNumStr && "".equals(prcesionNumStr)==false){
					try {
						float n = Float.valueOf(prcesionNumStr);
						if(n>1){
							forPrecisionTip=" ("+prcesionNumStr+")";
						}
					} catch (NumberFormatException e) {
					}
				}
			}
			VisualizationModelBarChart model= createBarChartModel(colName,rows,columnIndexColName,   columnIndexX,  columnIndexY , locale);
			model.setxAxisTitle(columnHeads[columnIndexX]+forPrecisionTip) ;
			model.setyAxisTitle(columnHeads[columnIndexY]);
			modelMap.put(colName, model); 
			
		}
		return modelMap;
	}
	
	public static  HashMap<String, VisualizationModel> createLineChartModelMap(
			String[] columnHeads, List<String> keys, List<String[]> rows, int columnIndexColName, int columnIndexX, int columnIndexY ) {
		HashMap<String, VisualizationModel> modelMap= new HashMap<String, VisualizationModel> ();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String colName = (String) iterator.next(); 
			VisualizationModelLine model= createLineChartModel(colName,rows,columnIndexColName,   columnIndexX,  columnIndexY);
			model.setxAxisTitle(columnHeads[columnIndexX]) ;
			model.setyAxisTitle(columnHeads[columnIndexY]);
			//for hist and freq, the  verticle minor grid is not nessary 
			model.setvGrid(false) ;
			model.sethGrid(false) ;
			modelMap.put(colName, model); 
			
		}
		return modelMap;
	}
	//this is only for frequency and histogram
	public static  VisualizationModelBarChart createBarChartModel(String colName,
			List<String[]> rows, int columnIndexColName, int columnIndexX,int columnIndexY, Locale locale ) {
		List<BarchartSeries> series= new ArrayList<BarchartSeries> (); 
		
		List<String[]> xLabels = new ArrayList<String[]>();
		  List<Float> yValues=new ArrayList<Float>();
         int i=0;
         
		for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
			String[] row = (String[]) iterator.next();
			String tempColName= row[columnIndexColName];//COLUMN_INDEX_NAME
			if(tempColName!=null&&(tempColName.equals(colName)
					//||tempColName.replace(" ", "").equals(colName)
					)){
				yValues.add(parseValue(row[columnIndexY]));//COLUMN_INDEX_COUNT
				xLabels.add( new String[]{String.valueOf(i+1),row[columnIndexX]}); //COLUMN_INDEX_VALUE
				i++;
			}
			
		}
		
		
 		int maxCount = OutPutVisualAdapterFactory.getInstance().getMaxChartElements();
 		int total=yValues.size();
 		List<String> errorMessages = new ArrayList<String>(); 
 		if(total>maxCount){
 			errorMessages.add(VisualNLS.getMessage(VisualNLS.BARS_EXCEED_LIMIT, locale, maxCount)); 
 			List<String[]> newXlabels = new ArrayList<String[]>();
 			  List<Float> newYValues=new ArrayList<Float>();
 			int count=0;
 			int index=1;
			for(String[] dr:xLabels ){
				if((count%(total/maxCount))<1){
					dr[0]=String.valueOf(index);
					newXlabels.add(dr);
					
					index++;
				}
				count++;
			}		
			for(Float dr:yValues ){
				if((count%(total/maxCount))<1){
					newYValues.add(dr);
				}
				count++;
			}
			
			yValues=newYValues;
			
			xLabels= newXlabels ;
 		}
 		 
		
		
	  
		float[] yValuesArray =new float[yValues.size()];
		
		int arraySize =yValuesArray.length;
//		if (arraySize>MAX_BARS){
//			arraySize=MAX_BARS;
//			xLabels= xLabels.subList(0, MAX_BARS);
//			
//		}
		for (int j = 0; j < arraySize ; j++) {
			yValuesArray[j]=yValues.get(j).floatValue();
		}
		BarchartSeries serie = new BarchartSeries(colName, yValuesArray ); 
		series.add(serie ) ;
		VisualizationModelBarChart  barchart= new VisualizationModelBarChart(colName, colName, series) ;
		
		barchart.setxLabels(xLabels);
        if(hasLongLabelNames(xLabels)==true){
            barchart.setxLableRotation(-20);
        }


		int width = VisualUtils.caculateBarchartWidth(xLabels.size(), xLabels.size());
		barchart.setWidth(width);
		barchart.setHeight(550);
		if(errorMessages.size()>0){
			barchart.setErrorMessage(errorMessages);
		}
		
		return barchart;
	}

    private static boolean hasLongLabelNames(List<String[]> xLabels) {
        if(null!=xLabels && xLabels.size()>0){
            for(int i=0;i<xLabels.size();i++){
               String[] labels =  xLabels.get(i);
                if(null!=labels && labels.length>0){
                    for(String label : labels){
                        if(null!=label && label.length()>5){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static int caculateBarchartWidth(int barNumbers, int groups) {
		 //30 is bar gap
		return 140+groups*40+ barNumbers*25;
	}


	public static  VisualizationModelLine createLineChartModel(String colName,
			List<String[]> rows, int columnIndexColName, int columnIndexX,int columnIndexY ) {
	 	
		List<String[]> xLabels = new ArrayList<String[]>();
         int i=0;
 		VisualLine line = new VisualLine(colName); 
 		line.setColor("red") ;
		for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
			String[] row = (String[]) iterator.next();
			String tempColName= row[columnIndexColName];//COLUMN_INDEX_NAME
			if(tempColName!=null&&(tempColName.equals(colName)
					//||tempColName.replace(" ", "").equals(colName)
					)){
				xLabels.add( new String[]{String.valueOf(i+1),row[columnIndexX]}); //COLUMN_INDEX_VALUE
				line.addVisualPoint(new VisualPoint(row[columnIndexX],String.valueOf(parseValue( row[columnIndexY])))) ;
				i++;
			}
			
		}
//	  
//		float[] yValuesArray =new float[yValues.size()];
//		for (int j = 0; j < yValuesArray.length; j++) {
//			yValuesArray[j]=yValues.get(j).floatValue();
//		}
 
		
		List<VisualLine> lines = new ArrayList<VisualLine>();

		lines.add(line);
		VisualizationModelLine   lineChart= new VisualizationModelLine(colName, lines)	;
		lineChart.setxLabels(xLabels );
		int width = xLabels.size()*45+120 ;
		if(width<420){
			width = 420;
		}
		lineChart.setWidth(420);
		lineChart.setHeight(420);
		lineChart.setMarkers(true) ;
		return lineChart;
	}

  

	private static  float parseValue(String string) {
		if(string.equals(N_A)){
			string="0";
		}
		if(string.endsWith("%")) {
			string=string.substring(0,string.length()-1) ;
			return Float.parseFloat(string);
		}
		return Float.parseFloat(string);
	}

	 public static  List<TableColumnMetaInfo> buildColumns(ResultSetMetaData rsmd) throws SQLException {
			List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo> ();
		
			
			int count = rsmd.getColumnCount();
			for(int i=0;i<count;i++){
				columns.add(new TableColumnMetaInfo( rsmd.getColumnName(i+1), rsmd.getColumnTypeName(i+1))) ; 
			}
			return columns;
		}
		
//		public static void setXMaxAndMin(VisualizationModelLine lineModel) {
//			String minX=null;
//			String maxX=null;
//			List<VisualLine> lines = lineModel.getLines();
//			if(lines!=null&&lines.size()>0){
//				
//				
//				for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
//					VisualLine visualLine = (VisualLine) iterator.next();
//					if(visualLine==null||visualLine.getPoints()==null||visualLine.getPoints().get(0)==null){
//						continue;
//					}
//					String lineMinX = visualLine.getPoints().get(0).getX() ;
//					String lineMaxX = visualLine.getPoints().get(visualLine.getPoints().size()-1).getX() ;
//					if(minX==null||Float.parseFloat(lineMinX)<Float.parseFloat(minX)){
//						minX=lineMinX;
//					}
//					if(maxX==null||Float.parseFloat(lineMaxX)>Float.parseFloat(maxX)){
//						maxX=lineMaxX;
//					}
//				}
//				if(maxX!=null){
//					lineModel.setMaxX(maxX) ;
//				}
//				if(minX!=null){
//					lineModel.setMinX(minX);
//				}
//				
//			}
//		}	

		public static void   autoGenerateAxisLabel(VisualizationModelChart lineModel,boolean x,boolean y, DecimalFormat df) {
		
			int step =LABEL_NUMBER;
		 
		    List<String[]> xLabels= new ArrayList<String[]>();
		    List<String[]> yLabels= new ArrayList<String[]>();
		    float minX= 0f,minY = 0f;;
			float maxX=	0f,maxY=0f;
			float deltaX =0f , deltaY =0f;
			long n = 1l;
			long m =1l;
			if(x){
				  minX=	Float.valueOf(lineModel.getMinX());
				  maxX=	Float.valueOf(lineModel.getMaxX());
				  deltaX =( maxX-minX)/step;
				  n=AlpineMath.adjustUnits(minX, maxX);
		    		 
			}
			if(y){
				  minY=	Float.valueOf(lineModel.getMinY());
				  maxY=	Float.valueOf(lineModel.getMaxY());
				  deltaY =( maxY-minY)/step;
				  m = AlpineMath.adjustUnits(minY, maxY);
			}
			
			for (int i = 0; i < step; i++) {
				if(x){
					float xValue = minX+deltaX*i; 
					String xLabel =String.valueOf(xValue/n);
					//String xLabel =VisualUtils.getScientificNumber(xValue/n);
					if(df!=null){
						xLabel=df.format(xValue);
					}
					
					if(xLabel.length()> OutPutVisualAdapter.MAX_LENGTH_ROTATION_DEFAULT){
						lineModel.setxLableRotation(OutPutVisualAdapter.DEFAULT_XLABEL_ROTATION) ;
					}
					
					xLabels.add(new String[]{xLabel,xLabel });
					 
					
				}
				if(y){
					float yValue = minY+deltaY*i;
					String yLabel =String.valueOf(yValue/m);
					//String yLabel =VisualUtils.getScientificNumber(yValue/m);
					if(df!=null){
						yLabel=df.format(yValue);
					}
					if(yLabel.length()> OutPutVisualAdapter.MAX_LENGTH_ROTATION_DEFAULT){
						lineModel.setyLableRotation(OutPutVisualAdapter.DEFAULT_XLABEL_ROTATION) ;
					}
				
					yLabels.add(new String[]{yLabel,yLabel });
				}
			}
			if(x){
				lineModel.setxLabels(xLabels);
			}
			if(y){
				lineModel.setyLabels(yLabels);
			}
			
			
		}

		public static List<String[]> autoGenerateXlabelForOne(VisualizationModelLine lineModel) {
			int step =LABEL_NUMBER;
			 List<String[]> labels= new ArrayList<String[]>();
		 	for (int i = 0; i < step; i++) {
				labels.add(new String[]{"0."+String.valueOf(i),"0."+String.valueOf(i)});
			}
			return labels;
		}
		
		
		public static void setAxisMaxAndMin(VisualizationModelLine lineModel,boolean x,boolean y, DecimalFormat df) {
			String minX=null;
			String maxX=null;
			String minY=null;
			String maxY=null;
			
			List<VisualLine> lines = lineModel.getLines();
			if(lines!=null&&lines.size()>0){
				
				
				for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
					VisualLine visualLine = (VisualLine) iterator.next();
					if(visualLine==null||visualLine.getPoints()==null||visualLine.getPoints().get(0)==null){
						continue;
					}
					if(x){
						String lineMinX = visualLine.getPoints().get(0).getX() ;
						String lineMaxX = visualLine.getPoints().get(visualLine.getPoints().size()-1).getX() ;
						if(minX==null||Float.parseFloat(lineMinX)<Float.parseFloat(minX)){
							minX=lineMinX;
						}
						if(maxX==null||Float.parseFloat(lineMaxX)>Float.parseFloat(maxX)){
							maxX=lineMaxX;
						}
					}
					 
					if(y){
						List<VisualPoint> points = visualLine.getPoints();
						String[] minmaxY=  getMinMaxY(points);
						String lineMinY = minmaxY[0];//visualLine.getPoints().get(0).getY() ;
						String lineMaxY =  minmaxY[1];//visualLine.getPoints().get(visualLine.getPoints().size()-1).getY() ;
						
						if(minY==null||Float.parseFloat(lineMinY)<Float.parseFloat(minY)){
							minY=lineMinY;
						}
						if(maxY==null||Float.parseFloat(lineMaxY)>Float.parseFloat(maxY)){
							maxY=lineMaxY;
						}
					}
				}
				if(x){
					if(maxX!=null){
						if(df!=null){
							maxX= df.format(Float.parseFloat(maxX)) ;
						}
						lineModel.setMaxX(maxX) ;
					}
					if(minX!=null){
						if(df!=null){
							minX= df.format(Float.parseFloat(minX)) ;
						}
						lineModel.setMinX(minX);
					}
				}
				if(y){
					if(maxY!=null){
						if(df!=null){
							maxY= df.format(Float.parseFloat(maxY)) ;
						}
						lineModel.setMaxY(maxY) ;
					}
					if(minX!=null){
						if(df!=null){
							minY= df.format(Float.parseFloat(minY)) ;
						}
						lineModel.setMinY(minY);
					}
				}
			}
		} 


		public  static String[] getMinMaxY(List<VisualPoint> points) {
			String[] result = new String[2];
			float minY =0;
			float maxY =0;
			 
			for (Iterator iterator = points.iterator(); iterator.hasNext();) {
				VisualPoint point = (VisualPoint)iterator.next();
				float y = Float.parseFloat(point.getY());
				if(minY>y){
					minY= y;
				}
				if(maxY<y){
					maxY= y;
				}
			}
			result[0] = String.valueOf(minY);
			result[1] =String.valueOf(maxY);;
			return result;
		}


		public static void generateAxisTickStep(
				VisualizationModelScatter scatterModel, boolean x, boolean y) {
			if(x){
				if(scatterModel.getxLabels().size()<LABEL_NUMBER){
					
				}else{
					String xMin = scatterModel.getMinX();
					String xMax = scatterModel.getMaxX();
					double delta = Double.parseDouble(xMax) -Double.parseDouble(xMin) ;
					String xMajorTickStep = String.valueOf(delta/LABEL_NUMBER);
					scatterModel.setxMajorTickStep(xMajorTickStep);
					String xMinorTickStep= String.valueOf(delta/(LABEL_NUMBER*5)) ;
					scatterModel.setxMinorTickStep(xMinorTickStep);
				}
				
				
			}
			if(y){
				String yMin = scatterModel.getMinY();
				String yMax = scatterModel.getMaxY();
				double delta = Double.parseDouble(yMax) -Double.parseDouble(yMin) ;
				String yMajorTickStep = String.valueOf(delta/LABEL_NUMBER);
				scatterModel.setyMajorTickStep(yMajorTickStep);
				String yMinorTickStep= String.valueOf(delta/(LABEL_NUMBER*5)) ;
				scatterModel.setyMinorTickStep(yMinorTickStep);
			}
 
		}	
		
	 	public static boolean containPoint(VisualLine line, String x, String y) {
	 		if(line!=null){
	 			List<VisualPoint> points = line.getPoints();
	 			if(points!=null){
	 				for(int i = 0;i< points.size();i++){
	 					VisualPoint point = points.get(i); 
	 					if(x.equals(point.getX())&&y.equals(point.getY())){
	 						return true;
	 					}
	 				}
	 			}
	 		}
	 
	 		return false;
	 	}
	 	
		public static String getScientificNumber(float number){
			int fractionDigits = 4; 
			try {
				    fractionDigits = Integer.parseInt(ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_ALG,PreferenceInfo.KEY_DECIMAL_PRECISION));
				} catch (NumberFormatException e1) {
					fractionDigits = 4;
				} catch (Exception e1) {
					fractionDigits = 4;
				}
			MathContext mc = new MathContext(1, RoundingMode.HALF_UP);
			
			BigDecimal bdc = new BigDecimal(number,mc);
	       
			return String.valueOf(bdc);
			
		}

}

