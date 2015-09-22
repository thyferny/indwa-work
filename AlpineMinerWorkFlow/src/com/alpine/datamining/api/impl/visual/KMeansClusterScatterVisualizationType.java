/**
 * ClassName KMeansTableVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import java.awt.Color;
import java.awt.Shape;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.operator.kmeans.ClusterModel;
import com.alpine.miner.view.ui.dataset.ClusterScatterEntity;
import com.alpine.utility.db.DataSourceType;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class KMeansClusterScatterVisualizationType extends TableVisualizationType {
    private static final Logger itsLogger =Logger.getLogger(KMeansClusterScatterVisualizationType.class);

    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		ClusterModel clusterModel= null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String tableName ="";
		Connection conn=null;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof ClusterModel){
				clusterModel = (ClusterModel)obj;
				conn =((DBTable) clusterModel.getDataSet().getDBTable()).getDatabaseConnection().getConnection();
				tableName = clusterModel.getResultTableName();
			}
		}
		
		DataSourceType stype = DataSourceType.getDataSourceType(
				analyzerOutPut.getAnalyticNode().getSource().getDataSourceType());
		
		if(clusterModel == null)return null;
		String[] defaultKey = new String[2];
		ArrayList<ArrayList<String>> list = clusterModel.getCenterPoint();
		if(list != null && list.size()>0){
			CompositeVisualizationOutPut output = new CompositeVisualizationOutPut();
			Hashtable<String, ClusterScatterEntity> clusterHt = new Hashtable<String, ClusterScatterEntity>();
			if(list.get(0).size()<3)return null;
			for(int i=0;i<list.size();i++){
				if(i>0){
					ClusterScatterEntity entity = new ClusterScatterEntity();
					for(int j=0;j<list.get(i).size();j++){
						if(j>0){
							entity.addCenterPoint(list.get(0).get(j),Double.valueOf(list.get(i).get(j)));
						}
					}
					try {
						String countSql = "select count(*) from "+tableName+" where cluster="+list.get(i).get(0);
						ps = conn.prepareStatement(countSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
						rs = ps.executeQuery();
						rs.next();
						String count = rs.getString(1);
						rs.close();
						ps.close();
						String maxRows= ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT);
						
						String sql = "select * from (select *,row_number() over() as myrow_number from "+tableName+" where cluster="+list.get(i).get(0)+") as foo where (( myrow_number-1)::numeric%("+count+"::numeric/"+maxRows+"))<1";	
						ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
						rs = ps.executeQuery();
						ResultSetMetaData rsmd = rs.getMetaData();
						int columneCount = rsmd.getColumnCount();
						List<String> doubleColumnList = new ArrayList<String>();
						for(int n=0;n<columneCount;n++){
							if(stype.isNumberColumnType(rsmd.getColumnTypeName(n+1).toUpperCase())){
								String columnName = rsmd.getColumnName(n+1); 
								doubleColumnList.add(columnName);
							}
						}
						
						filterDoubleColumn(doubleColumnList,entity);
						setDefautlQueryKey(defaultKey,entity);
						
						
						while(rs.next()){
							Set<String> set = entity.getCenterHt().keySet();
							Iterator<String> iter = set.iterator();
							while(iter.hasNext()){
								String key = iter.next();
								if(entity.getDataHt().get(key)==null){
									List<Double> integerList = new ArrayList<Double>();
									entity.getDataHt().put(key, integerList);
								}
								entity.getDataHt().get(key).add(rs.getDouble(key));
							}
						}
					} catch (SQLException e) {
						itsLogger.error(e.getMessage(),e);
						throw new RuntimeException(e);
					}finally{
						try {
							rs.close();
							ps.close();
						} catch (SQLException e) {
							itsLogger.error(e.getMessage(),e);
						}
					}
					clusterHt.put(list.get(i).get(0), entity);
				}
			}
			Enumeration<String> keyEnum = clusterHt.keys();
			List<String> keyList = new ArrayList<String>();
			while(keyEnum.hasMoreElements()){
				keyList.add(keyEnum.nextElement());
			}
			String[] keys = new String[keyList.size()];
			for(int num=0;num<keys.length;num++){
				keys[num]=keyList.get(num);
			}
			Arrays.sort(keys);
			Color[] colors = VisualUtility.getRandomColor(keys.length);
			for(int i=0;i<keys.length;i++){
				clusterHt.get(keys[i]).setDefaultColumn(defaultKey);
				clusterHt.get(keys[i]).setColor(colors[i]);
				JFreeChart chart = CreateChart(defaultKey, clusterHt.get(keys[i]));
				clusterHt.get(keys[i]).setJfreechart(chart);
				ClusterScatterVisualizationOutPut chartOutput = new ClusterScatterVisualizationOutPut(clusterHt.get(keys[i]));
				chartOutput.setName(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER,locale)+" "+keys[i]);
			    output.addChildOutPut(chartOutput);
			}
			
			return output;
		}
		return null;
	}

	private void setDefautlQueryKey(String[] defaultKey,
			ClusterScatterEntity entity) {
		Set<String> set = entity.getCenterHt().keySet();
		List<String> keyList = new ArrayList<String>();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()){
			keyList.add(iter.next());
		}
		String[] keys = new String[keyList.size()];
		for(int i=0;i<keys.length;i++){
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
		while(iter.hasNext()){
			String key = iter.next();
			boolean isDouble = false;
			for(String doubleColumn:doubleColumnList){
				if(key.equalsIgnoreCase(doubleColumn)){
					isDouble = true;
				}
			}
			if(!isDouble){
				needRemovedKeySet.add(key);
			}
		}
		if(needRemovedKeySet.size()>0){
			set.removeAll(needRemovedKeySet);
		}
	}

	public   JFreeChart CreateChart(String[] defaultKey,
			ClusterScatterEntity entity) {
		JFreeChart chart = null;
		double[][] data = new double[2][entity.getDataHt().get(defaultKey[0]).size()];
		for(int n=0;n<data[0].length;n++){
			data[0][n] = entity.getDataHt().get(defaultKey[0]).get(n);
			data[1][n] = entity.getDataHt().get(defaultKey[1]).get(n);
		}
		DefaultXYDataset xydataset = new DefaultXYDataset();
		xydataset.addSeries(VisualLanguagePack.getMessage(VisualLanguagePack.SCATTER_POINT,locale), data);
		double[][] centerPoint = new double[2][1];
		centerPoint[0][0] = entity.getCenterHt().get(defaultKey[0]); 
		centerPoint[1][0] = entity.getCenterHt().get(defaultKey[1]);
		xydataset.addSeries(VisualLanguagePack.getMessage(VisualLanguagePack.CENTER_POINT,locale), centerPoint);
		chart = ChartFactory.createScatterPlot("", defaultKey[0],defaultKey[1],  xydataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot  plot   =   chart.getXYPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		Shape shapeCenter = VisualUtility.getCenterPoint();
		Shape shapeScatter = VisualUtility.getScatterPoint();
		renderer.setSeriesShape(0,shapeScatter);
		renderer.setSeriesShape(1,shapeCenter);
		renderer.setSeriesPaint(0,entity.getColor());
		renderer.setSeriesPaint(1,entity.getColor());
		plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
		plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		chart.getLegend().setItemFont(VisualResource.getChartFont());
		return chart;
	}
}
