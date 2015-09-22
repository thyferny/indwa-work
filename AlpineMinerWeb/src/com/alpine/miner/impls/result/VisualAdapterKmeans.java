/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterKmeans.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.operator.kmeans.ClusterModel;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.view.ui.dataset.ClusterAllEntity;
import com.alpine.miner.view.ui.dataset.ClusterScatterEntity;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualPointGroup;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelClusterProfile;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelPieChart;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.db.DataTypeConverter;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterKmeans extends DBUpdateOutPutVisualAdapter implements
		OutPutVisualAdapter {

    private static Logger itsLogger = Logger.getLogger(VisualAdapterKmeans.class);
    public static final VisualAdapterKmeans INSTANCE = new VisualAdapterKmeans();
	//max 200 , dedfault 30 ... 

	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		// toVText((LinearRegressionModelDB)model.getModel())

		Object obj = null;
		ClusterModel clusterModel = null;
		if (analyzerOutPut instanceof AnalyzerOutPutObject) {
			obj = ((AnalyzerOutPutObject) analyzerOutPut).getOutPutObject();
			if (obj instanceof ClusterModel) {
				clusterModel = (ClusterModel) obj;
			}
		}

		String name = analyzerOutPut.getAnalyticNode().getName();
		String message = getMessage(clusterModel,  locale);
		VisualizationModelText textModel = new VisualizationModelText(VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale),
				message);// message);

		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
	
		models.add(textModel);
		
		models.add(createProfileChart(clusterModel,  locale));
	
		models.add(createSummaryTable(analyzerOutPut, clusterModel,  locale));
		models.add(createCenterPointTable(clusterModel,  locale));
		VisualizationModel warningModel=createWarningText(clusterModel,  locale);
		
		if(warningModel!=null){
			models.add(warningModel);
		}
		
		try {
			VisualizationModel model = createScatterPoint(analyzerOutPut,clusterModel,  locale);
			//this is special --don't create report because too many pictures MINERWEB-448 
			
			if(model!=null){
				model.setNeedGenerateReport(false);
				models.add(model);
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e) ;
			
		}

		VisualizationModelComposite visualModel = new VisualizationModelComposite(
				name, models);

		return visualModel;

	}

	//profile is a layered composite model, 4 pie chart share the same legend...(use xlabels...)
	private VisualizationModel createProfileChart(ClusterModel clusterModel,Locale locale) {
	
		List<String[]> list = clusterModel.getClustersArrays();
		//nothing to do
		if(list == null || list.size()==0){
			return null;
		}

		List<List<VisualizationModel>> models = new ArrayList<List<VisualizationModel>>();
		for (String[] temp : list) {
			List<VisualizationModel> modelList = new ArrayList<VisualizationModel>();
			for (int i = 0; i < temp.length; i++) {
				String strArray=temp[i];
				VisualizationModel model = null;
				model = createProfileModel(i, strArray);
				modelList.add(model);
			}
			models.add(modelList);
		}

		List<String> tableHeader = createProfileTableHeader(list,  locale);
		VisualizationModelClusterProfile tableGroupModel =new VisualizationModelClusterProfile(
				VisualNLS.getMessage(VisualNLS.CLUSTER_PROFILES,locale), tableHeader, models)	;	
		return tableGroupModel;
	}

	//this is the vmodel for each cell in the profile table
	private VisualizationModel createProfileModel(int index, String strArray) { 
		VisualizationModel model;
		if (index == 0) {
			// attribute name, the first column
			model = new VisualizationModelText("", strArray);
		} else if (index == 1) {
			if(strArray.contains("},{")){//handle array type
				String[] labels = strArray.split("\\},\\{");
				for(int i =0;i<labels.length;i++){
					if(i==0){
						labels[i]=labels[i]+"}";
					}else if(i==labels.length-1){
						labels[i]="{"+labels[i];
					}else{
						labels[i]="{"+labels[i]+"}";
					}
				}
				model = new VisualizationModelPieChart("",
						Arrays.asList(labels), null);
			}else{
				String[] labels = strArray.split(",");
				model = new VisualizationModelPieChart("",
						Arrays.asList(labels), null);
			}	
		} else {
			String numberStr = strArray.split(";")[1];
			String[] numbers = numberStr.split(",");
			// the client will draw a new chart from the barchart model
			model = new VisualizationModelPieChart("", null,
					Arrays.asList(numbers));
		}
		return model;
	}



	public  Map<String, HashMap<String, VisualizationModel>>  generateOutPutMap(
			AnalyticOutPut analyzerOutPut, Locale locale) throws Exception {

	 	Object obj = null;
		ClusterModel clusterModel = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String tableName = "";
		Connection conn = null;
		if (analyzerOutPut instanceof AnalyzerOutPutObject) {
			obj = ((AnalyzerOutPutObject) analyzerOutPut).getOutPutObject();
			if (obj instanceof ClusterModel) {
				clusterModel = (ClusterModel) obj;
				conn = ((DBTable) clusterModel.getDataSet().getDBTable())
						.getDatabaseConnection().getConnection();
				tableName = clusterModel.getResultTableName();
			}
		}
		DataSourceType stype = DataSourceType.getDataSourceType(analyzerOutPut
				.getAnalyticNode().getSource().getDataSourceType());

		if (clusterModel == null)
			return null;
		String[] defaultKey = new String[2];
		ArrayList<ArrayList<String>> list = clusterModel.getCenterPoint();
		if (list != null && list.size() > 0) {
			ClusterAllEntity allEntity = new ClusterAllEntity();
			if (list.get(0).size() < 3)
				return null;
			
			Map<String, HashMap<String, VisualizationModel>> eachClusterModel = new LinkedHashMap<String, HashMap<String, VisualizationModel>>();
			
			for (int i = 0; i < list.size(); i++) {
				if (i > 0) {// i=0 title name list
					ClusterScatterEntity entity = new ClusterScatterEntity();
					for (int j = 0; j < list.get(i).size(); j++) {
						if (j > 0) {// j=0 cluster number list
							entity.addCenterPoint(list.get(0).get(j),
									Double.valueOf(list.get(i).get(j)));
						}
					}
					try {
						String clusterColumnName = clusterModel
								.getClusterColumn();
						String countSql = "select count(*) from " + tableName
								+ " where " + clusterColumnName + "="
								+ list.get(i).get(0);
						itsLogger.debug(
								"KMeansClusterAllVisualizationType.generateOutPut():countSql="
										+ countSql);
						ps = conn.prepareStatement(countSql,
								ResultSet.TYPE_FORWARD_ONLY,
								ResultSet.CONCUR_READ_ONLY);
						rs = ps.executeQuery();
						rs.next();
						String count = rs.getString(1);
						rs.close();
						ps.close();

						String sql = ""; 
						String maxRows = ResourceManager.getInstance().getPreferenceProp(
					 			PreferenceInfo.GROUP_UI, PreferenceInfo.MAX_CLUSTER_POINTS) ; 
						if(analyzerOutPut.getAnalyticNode().getSource().getDataSourceType().equals(DataSourceInfoDB2.dBType)){
							sql = "select * from (select "
									+ tableName
									+ ".*,ROWNUMBER() over() as myrow_number from "
									+ tableName + " where " + clusterColumnName
									+ "=" + list.get(i).get(0)
									+ ") foo where mod(( myrow_number-1)*"
									+ maxRows + "," + count + ")<" 	+ maxRows;

						}else if(analyzerOutPut.getAnalyticNode().getSource().getDataSourceType().equals(DataSourceInfoNZ.dBType)){
							sql = "select * from (select *,row_number() over(order by 1) as myrow_number from "
								+tableName+" where "+clusterColumnName+"="+list.get(i).get(0)
								+") foo where mod(( myrow_number-1)*1.0,"+count+"*1.0/"
								+maxRows+")<1";
						}
						else{	
							
							sql =
								"select * from (select "
								+ tableName
								+ ".*,row_number() over(order by 1) as myrow_number from "
								+ tableName + " where " + clusterColumnName
								+ "=" + list.get(i).get(0)
								+ ") foo where mod(( myrow_number-1)*1.0,"
								+ count + "*1.0/"
								+ maxRows + ")<1";
						}
						
						
						itsLogger.debug(
								"KMeansClusterAllVisualizationType.generateOutPut():sql="
										+ sql);
						ps = conn.prepareStatement(sql,
								ResultSet.TYPE_FORWARD_ONLY,
								ResultSet.CONCUR_READ_ONLY);
						rs = ps.executeQuery();
						ResultSetMetaData rsmd = rs.getMetaData();
						int columneCount = rsmd.getColumnCount();
						List<String> doubleColumnList = new ArrayList<String>();
						for (int n = 0; n < columneCount; n++) {
							if (stype.isNumberColumnType(rsmd
									.getColumnTypeName(n + 1).toUpperCase())) {
								String columnName = rsmd.getColumnName(n + 1);
								doubleColumnList.add(columnName);
							}
						}

						filterDoubleColumn(doubleColumnList, entity);
						if (entity.getCenterHt().size() < 2)
							return null;
						setDefautlQueryKey(defaultKey, entity);
						setQueryColumn(allEntity, entity);
						while (rs.next()) {
							Set<String> set = entity.getCenterHt()
									.keySet();
							Iterator<String> iter = set.iterator();
							while (iter.hasNext()) {
								String key = iter.next();
								if (entity.getDataHt().get(key) == null) {
									List<Double> integerList = new ArrayList<Double>();
									entity.getDataHt().put(key, integerList);
								}
								entity.getDataHt().get(key)
										.add(rs.getDouble(key));
							}
						}
						Map<String, List<Double>> eachColumnArray = entity
								.getDataHt();
						Set<Entry<String, List<Double>>> entrySet = eachColumnArray
								.entrySet();
						Iterator<Entry<String, List<Double>>> iter = entrySet
								.iterator();
						while (iter.hasNext()) {
							Entry<String, List<Double>> entry = iter.next();
							String columnName = entry.getKey();
							HashMap<String, VisualizationModel> eachPoint = 	eachClusterModel.get(columnName);
							if(eachPoint==null){
								eachPoint=new LinkedHashMap<String, VisualizationModel>();
							}
						
							List<Double> columnValues = entry.getValue();
							Iterator<Entry<String, List<Double>>> newIter = entrySet
									.iterator();
							while (newIter.hasNext()) {
								Entry<String, List<Double>> newEntry = newIter
										.next();
								String newColumnName = newEntry.getKey();
								if (columnName.equals(newColumnName)) {
									eachPoint.put(newColumnName, null);
								} else {
									List<Double> newColumnValues = newEntry
											.getValue();
									VisualPointGroup pointList = new VisualPointGroup(
											VisualNLS.getMessage(VisualNLS.Cluster_Point,locale) +" " +(i-1));
									for (int j = 0; j < columnValues.size(); j++) {
										VisualPoint point=new VisualPoint(
												String.valueOf(newColumnValues.get(j)), //x
												String.valueOf(columnValues.get(j))); //y
									 
										pointList.addVisualPoint(point);
									}
									
									
									VisualizationModel vModel =eachPoint.get(newColumnName);
									if(vModel==null){
										
										vModel = createNewScatterModel(	columnName, newColumnName);
									}
									//....pointList
									 
									((VisualizationModelScatter)vModel).addVisualPointGroup(pointList);
									
									eachPoint.put(newColumnName, vModel);
								}
								
								// here need add center point...
								
								VisualizationModelScatter vModel =(VisualizationModelScatter)eachPoint.get(newColumnName);

								//for the center point ...
					 
									//the first line is the table column...
									ArrayList<String> colNames = list.get(0);
									if (i> 0&&vModel!=null) {
										VisualPointGroup group = new VisualPointGroup(
												VisualNLS.getMessage(VisualNLS.Cetner_Point,locale) 
												+" " +(i-1));
										group.setColor("red");
										ArrayList<String> row = list.get(i);
										String x=null;
										String y=null;
										for (int k=0;k<row.size();k++) {
											String value = (String) row.get(k);
											if(newColumnName.equals(colNames.get(k))){
												x=value;
											}else if(columnName.equals(colNames.get(k))){
												y=value;
											}
											
										}
										 if(x!=null&&y!=null){
									 		
											 group.addVisualPoint(new VisualPoint(x, y) ) ;
										 }
											vModel.addVisualPointGroup(group );
									}
								 

							}
							eachClusterModel.put(columnName, eachPoint);
						}
					} catch (SQLException e) {
						itsLogger.error(e.getMessage(),e);
						throw new RuntimeException(e);
					} finally {
						try {
							rs.close();
							ps.close();
						} catch (SQLException e) {
							itsLogger.error(e.getMessage(),e);
						}
					}
 
				}
			}
			//For precision
			buildeachClusterModel4Precision(eachClusterModel);
			//
			return eachClusterModel;
		}
		return null;
	}
    // for precision
	private void buildeachClusterModel4Precision(
			Map<String, HashMap<String, VisualizationModel>> eachClusterModel) {
		   if(null!=eachClusterModel){
			  Set<Entry<String, HashMap<String, VisualizationModel>>> set4ClusterModelSet = eachClusterModel.entrySet();
			  if(null!=set4ClusterModelSet && set4ClusterModelSet.size()>0){
				  for (Iterator iterator = set4ClusterModelSet.iterator(); iterator
						.hasNext();) {
					Entry<String, HashMap<String, VisualizationModel>> entry = (Entry<String, HashMap<String, VisualizationModel>>) iterator
							.next();
					buildVisualizationModel(entry.getValue());
					
				}
				  
			  }
		   }
		
	}
    //For precision
	private void buildVisualizationModel(
			HashMap<String, VisualizationModel> visualModelMap) {
		if(null!=visualModelMap){
			Set<Entry<String,VisualizationModel>> set4VisualModelSet = visualModelMap.entrySet();
			if(null!=set4VisualModelSet){
				for (Iterator iterator = set4VisualModelSet.iterator(); iterator
						.hasNext();) {
					Entry<String, VisualizationModel> entry = (Entry<String, VisualizationModel>) iterator
							.next();
					if(null!=entry){
						buildPrecisionVisualizationModelScatter((VisualizationModelScatter)entry.getValue());
					}
				}
			}
		}
	}
	
	

	private void buildPrecisionVisualizationModelScatter(
			VisualizationModelScatter scatterVisualModel) {
		// TODO Auto-generated method stub
		if(null!=scatterVisualModel){
			List<VisualPointGroup> pointGroups = scatterVisualModel.getPointGroups();
			float maxX= 0.0f;
			float minX = 0.0f;
			float maxY = 0.0f;
			float minY = 0.0f;
			try {
				maxX = Float.valueOf(pointGroups.get(0).getPoints().get(0).getX());
				minX = Float.valueOf(pointGroups.get(0).getPoints().get(0).getX());
				maxY = Float.valueOf(pointGroups.get(0).getPoints().get(0).getY());
				minY = Float.valueOf(pointGroups.get(0).getPoints().get(0).getY());				
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
			
		    for (Iterator iterator = pointGroups.iterator(); iterator.hasNext();) {
				VisualPointGroup visualPointGroup = (VisualPointGroup) iterator
						.next();
				
				List<VisualPoint> point = visualPointGroup.getPoints();
				
				for (Iterator iterator2 = point.iterator(); iterator2.hasNext();) {
					VisualPoint visualPoint = (VisualPoint) iterator2.next();
					float tempFloatX = Float.valueOf(visualPoint.getX());
					float tempFloatY = Float.valueOf(visualPoint.getY());
					
					if(maxX<tempFloatX){
						maxX = tempFloatX;
					}
					if(minX>tempFloatX){
						minX = tempFloatX;
					}
					if(maxY<tempFloatY){
						maxY = tempFloatY;
					}
					if(minY>tempFloatY){
						minY = tempFloatY;
					}
				}
			}
		    
	    	long n = 1l;
	    		n = AlpineMath.adjustUnits(Double.valueOf(minX), Double.valueOf(maxX));
	        
	    	long m = 1l;
	    		m=AlpineMath.adjustUnits(Double.valueOf(minY), Double.valueOf(maxY));
	    	String xTitle = "";
	    	String yTitle = "";
	    	if(n!=1){
	    		xTitle = " ("+VisualUtils.getScientificNumber(n)+")";
	    	}
	    	if(m!=1){
	    		yTitle = " ("+VisualUtils.getScientificNumber(m)+")";
	    	}
	    	
	    	if("".equals(xTitle)==false){
	    		scatterVisualModel.setxAxisTitle(scatterVisualModel.getxAxisTitle()+ xTitle);
	    		
	    	}
	    	if("".equals(yTitle)==false){
	    		scatterVisualModel.setyAxisTitle(scatterVisualModel.getyAxisTitle()+ yTitle);	    		
	    	}
	    	
	    	for (Iterator iterator = pointGroups.iterator(); iterator.hasNext();) {
				VisualPointGroup visualPointGroup = (VisualPointGroup) iterator
						.next();
				List<VisualPoint> point = visualPointGroup.getPoints();
				
				for (Iterator iterator2 = point.iterator(); iterator2.hasNext();) {
					VisualPoint visualPoint = (VisualPoint) iterator2.next();
					
					float tempFloatX = Float.valueOf(visualPoint.getX());
					float tempFloatY = Float.valueOf(visualPoint.getY());
					
					visualPoint.setX(String.valueOf(tempFloatX/n));
					visualPoint.setY(String.valueOf(tempFloatY/m));
				}
				
			}
		    
		}
	}

	private VisualizationModel createNewScatterModel(String columnName,
			String newColumnName) {
 
		VisualizationModelScatter	vModel=new VisualizationModelScatter( ) ;
		vModel.setSourceOperatorClass(VisualizationModelScatter.Source_Operator_KMeans);
		
		vModel.setWidth(900) ;
		vModel.setHeight(450) ;
		vModel.sethGrid(true);
		vModel.setvGrid(true);
		vModel.setxAxisTitle(newColumnName);
		vModel.setyAxisTitle(columnName);
		vModel.setTitle("X = \""+newColumnName +"\", Y = \"" +columnName+"\"") ;
		return vModel;
	}

	private void setDefautlQueryKey(String[] defaultKey,
			ClusterScatterEntity entity) {
		Set<String> set = entity.getCenterHt().keySet();
		List<String> keyList = new ArrayList<String>();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			keyList.add(iter.next());
		}
		String[] keys = new String[keyList.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = keyList.get(i);
		}
		Arrays.sort(keys);
		defaultKey[0] = keys[0];
		defaultKey[1] = keys[1];
	}

	private void filterDoubleColumn(List<String> doubleColumnList,
			ClusterScatterEntity entity) {
		Set<String> set = entity.getCenterHt().keySet();
		Set<String> needRemovedKeySet=new HashSet<String>();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			boolean isDouble = false;
			for (String doubleColumn : doubleColumnList) {
				if (key.equalsIgnoreCase(doubleColumn)) {
					isDouble = true;
				}
			}
			if (!isDouble) {
				needRemovedKeySet.add(key);
			}
		}
		if(needRemovedKeySet.size()>0){
			set.removeAll(needRemovedKeySet);
		}
	}

	private void setQueryColumn(ClusterAllEntity allEntity,
			ClusterScatterEntity entity) {
		if (allEntity.getQueryColumn() == null
				|| allEntity.getQueryColumn().length == 0) {
			String[] columns = new String[entity.getCenterHt().size()];
			Set<String> set = entity.getCenterHt().keySet();
			int i = 0;
			Iterator<String> iter = set.iterator();
			while (iter.hasNext()) {
				columns[i] = iter.next();
				i++;
			}
			Arrays.sort(columns);
			allEntity.setQueryColumn(columns);
		}
	}

	private VisualizationModel createScatterPoint(AnalyticOutPut outPut, ClusterModel clusterModel,Locale locale) throws Exception {

		 Map<String, HashMap<String, VisualizationModel> > outputModelMap= generateOutPutMap(outPut,locale);
		  
		 if(outputModelMap==null){
			 return null;
		 }
		// we use 2 layerd ..x, and y ...
		List<String> yKeys = getKeysList(outputModelMap.keySet(),null);

		HashMap<String, VisualizationModel> yModelMap = getYModelMaps(yKeys,outputModelMap,locale);

		VisualizationModelLayered layeredModel = new VisualizationModelLayered(
				VisualNLS.getMessage(VisualNLS.CLUSTER,locale), 
			 
				"Y "+VisualNLS.getMessage( VisualNLS.Axis,locale), yKeys, yModelMap);
		layeredModel.setVisualizationType(VisualizationModel.TYPE_CLUSRTER_CHART) ;
		
		return layeredModel;
	}

	private HashMap<String, VisualizationModel> getYModelMaps(
			List<String> yKeys,
			Map<String, HashMap<String, VisualizationModel>> outputModelMap, Locale locale) {
		HashMap<String, VisualizationModel>  maps= new HashMap<String, VisualizationModel> ();
		 
		//for test use
		
	 	for (Iterator iterator = yKeys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			HashMap<String, VisualizationModel> yMaps = outputModelMap.get(key); 
			VisualizationModelLayered layeredModel= new VisualizationModelLayered("",
					"X "+VisualNLS.getMessage( VisualNLS.Axis,locale), getKeysList(yMaps.keySet(),key), yMaps) ;
		
			layeredModel.setVisualizationType(VisualizationModel.TYPE_CLUSRTER_CHART) ;
			//this is test use to make the json size small
			maps.put(key, layeredModel);
		} 
	
		return maps;
	}

	private List<String> getKeysList(Set<String> keys,String notContain) {
		 List<String> result= new ArrayList<String>(); 
		 
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if(notContain==null||key.equals(notContain)==false){
				result.add(key) ;
			}
		}
		return result;
	}

	private VisualizationModel createCenterPointTable(ClusterModel clusterModel,Locale locale) {
		if (clusterModel == null)
			return null;
		DataTable dataTable = new DataTable();
		ArrayList<ArrayList<String>> list = clusterModel.getCenterPoint();
		if (list != null && list.size() > 0) {
			List<DataRow> rows = new ArrayList<DataRow>();

			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					List<TableColumnMetaInfo> columnNames = new ArrayList<TableColumnMetaInfo>();
					for (int j = 0; j < list.get(i).size(); j++) {
						columnNames.add(new TableColumnMetaInfo(list.get(i)
								.get(j), ""));
					}

					dataTable.setColumns(columnNames);

				} else {
					String[] item = new String[list.get(i).size()];
					for (int j = 0; j < list.get(i).size(); j++) {
						item[j] = AlpineMath.doubleExpression(Double
								.parseDouble(list.get(i).get(j)));
					}
					DataRow row = new DataRow();
					row.setData(item);
					rows.add(row);

				}
			}

			dataTable.setRows(rows);
		}

		VisualizationModel model = new VisualizationModelDataTable(
				VisualNLS.getMessage(VisualNLS.CENTER_POINT,locale), dataTable);
		return model;

	}

	private VisualizationModel createWarningText(ClusterModel clusterModel,Locale locale) {
		if (!clusterModel.getIsStable()) {

			VisualizationModel model = new VisualizationModelText(
					VisualNLS.getMessage(VisualNLS.WARING_MESSAGE_TITLE,locale),
					clusterModel.getStableInformation());
			return model;
		}
		return null;
	}

	private VisualizationModel createSummaryTable(
			AnalyticOutPut analyzerOutPut, ClusterModel clusterModel,Locale locale) {
		DataTable dataTable = new DataTable();

		Connection conn = ((DBTable) clusterModel.getDataSet().getDBTable())
				.getDatabaseConnection().getConnection();
		// schema already in ...
		String tableName = clusterModel.getResultTableName();

		fillDataTable(dataTable, analyzerOutPut, tableName, conn);

		VisualizationModelDataTable visualModel = new VisualizationModelDataTable(
                VisualNLS.getMessage(VisualNLS.KMeans,locale), dataTable);

		return visualModel;
	}

	private String getMessage(ClusterModel clusterModel,Locale locale) {

		StringBuffer sb = new StringBuffer();
		if (clusterModel != null) {
			sb.append(VisualNLS.getMessage(VisualNLS.CLUSTER_COLUMN_NAME,locale) + " : "
					+ clusterModel.getClusterColumn());
			sb.append("\n");
			sb.append(VisualNLS.getMessage(VisualNLS.CLUSTER_COUNT,locale) + " : "
					+ clusterModel.getNumberOfClusters());
			sb.append("\n");
			sb.append(VisualNLS.getMessage(VisualNLS.AVG_MEASUREMENT,locale) + " : "
					+ clusterModel.getMeasureAvg());
		}
		return sb.toString();
	}

	@Override
	protected void fillDataTables(DataTable dataTable, ResultSet rs,
			int fetchSize, ResultSetMetaData rsmd, AnalyticOutPut outPut)
			throws SQLException {
		int count = rsmd.getColumnCount();
		List<DataRow> rows = new ArrayList<DataRow>();
		while (rs.next() && fetchSize > 0) {
			fetchSize--;
			String[] items = new String[count];
			for (int i = 0; i < count; i++) {
				if (DataTypeConverter.isDoubleType(rsmd.getColumnType(i + 1))) {
					items[i] = AlpineUtil.dealNullValue(rs, i + 1);
				} else {
					items[i] = rs.getString(i + 1);
				}
			}

			DataRow row = new DataRow();
			row.setData(items);
			rows.add(row);

		}
		 
		dataTable.setRows(rows);
	}

	
	private List<String> createProfileTableHeader(List<String[]> list,Locale locale) {
		List<String> tableHeader = new ArrayList<String>();
		tableHeader.add( VisualNLS.getMessage(VisualNLS.VARIABLES,locale));
		tableHeader.add( VisualNLS.getMessage(VisualNLS.STATES,locale));
		for(int i=0;i<list.get(0).length;i++){
				if(i==2){
					tableHeader.add( VisualNLS.getMessage(VisualNLS.POPULATION,locale)+": "+list.get(0)[i].split(";")[0]);
				}
				if(i>2){
					tableHeader.add(VisualNLS.getMessage(VisualNLS.CLUSTER,locale)+list.get(0)[i].split(";")[0]);
				}
			}
		return tableHeader;
	}
}
