/**
 * ClassName HadoopKmeansClusterVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-13
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.awt.Shape;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.miner.view.ui.dataset.ClusterAllEntity;
import com.alpine.miner.view.ui.dataset.ClusterScatterEntity;

/**
 * @author Jeff Dong
 * 
 */
public class HadoopKmeansClusterVisualizationType extends
		TableVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {

		Object obj = null;
		ClusterOutputModel clusterModel = null;

		if (analyzerOutPut instanceof HadoopKmeansOutput) {
			obj = ((HadoopKmeansOutput) analyzerOutPut).getClusterModel();
			if (obj instanceof ClusterOutputModel) {
				clusterModel = (ClusterOutputModel) obj;
			}
		}

		ClusterAllEntity allEntity = new ClusterAllEntity();

		List<String> columnNames = clusterModel.getColumnNames();
		Map<String, Map<String, Double>> centroids = clusterModel
				.getCentroidsContents();
		Map<String, Map<String, List<Double>>> scatters = clusterModel
				.getOutputScatters();
		allEntity.setQueryColumn(columnNames.toArray(new String[columnNames.size()]));
		if (columnNames != null && columnNames.size() > 1 && centroids != null
				&& scatters != null) {
			String[] defaultKey = new String[2];
			defaultKey[0] = columnNames.get(0);
			defaultKey[1] = columnNames.get(1);
			Hashtable<String, ClusterScatterEntity> clusterHt = new Hashtable<String, ClusterScatterEntity>();
			Iterator<Entry<String, Map<String, Double>>> iter = centroids.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, Map<String, Double>> entry = iter.next();
				String clusterNum = entry.getKey();
				Map<String, Double> centroid = entry.getValue();
				ClusterScatterEntity entity = new ClusterScatterEntity();
				for (String columnName : columnNames) {
					entity.addCenterPoint(columnName, centroid.get(columnName));
				}
				clusterHt.put(String.valueOf(clusterNum), entity);
			}
			Iterator<Entry<String, Map<String, List<Double>>>> iterScatters = scatters.entrySet().iterator();
			while(iterScatters.hasNext()){
				Entry<String, Map<String, List<Double>>> entry = iterScatters.next();
				String cluster = entry.getKey();
				Map<String, List<Double>> scatter = entry.getValue();
				ClusterScatterEntity entity = clusterHt.get(cluster);
				Map<String, List<Double>> dataHt = entity.getDataHt();
				for (String columnName : columnNames) {
					dataHt.put(columnName, scatter.get(columnName));
				}
			}

			int keyIndex=0;
			String[] keys = new String[centroids.size()];
			Iterator<String> keyIter = centroids.keySet().iterator();
			while(keyIter.hasNext()){
				String key = keyIter.next();
				keys[keyIndex++]=key;
			}
			JFreeChart chart = createChart(defaultKey, clusterHt, keys, "ALL");
			allEntity.setClusters(keys);
			allEntity.setEntityHt(clusterHt);
			allEntity.setDefaultColumn(defaultKey);
			allEntity.setJfreechart(chart);
		}
		ClusterAllVisualizationOutPut chartOutput = new ClusterAllVisualizationOutPut(
				allEntity);
		chartOutput.setName(VisualLanguagePack.getMessage(
				VisualLanguagePack.CLUSTER, locale)
				+ " "+VisualLanguagePack.getMessage(
						VisualLanguagePack.SCATTER_POINT, locale));
		return chartOutput;
	}

	private JFreeChart createChart(String[] defaultKey,
			Hashtable<String, ClusterScatterEntity> entityHt, String[] keys,
			String cluster) {
		JFreeChart chart = null;
		DefaultXYDataset xydataset = new DefaultXYDataset();
		Color[] colors = VisualUtility.getRandomColor(keys.length);
		for (int i = 0; i < keys.length; i++) {
			if (cluster != null && !cluster.equals("")
					&& !cluster.equals("ALL")) {
				if (!cluster.equalsIgnoreCase(keys[i])) {
					continue;
				}
			}
			entityHt.get(keys[i]).setColor(colors[i]);
			ClusterScatterEntity entity = entityHt.get(keys[i]);
			double[][] data = new double[2][entity.getDataHt()
					.get(defaultKey[0]).size()];
			for (int n = 0; n < data[0].length; n++) {
				data[0][n] = entity.getDataHt().get(defaultKey[0]).get(n);
				data[1][n] = entity.getDataHt().get(defaultKey[1]).get(n);
			}
			xydataset
					.addSeries(
							VisualLanguagePack.getMessage(
									VisualLanguagePack.SCATTER_POINT, locale)
									+ keys[i], data);
			double[][] centerPoint = new double[2][1];
			centerPoint[0][0] = entity.getCenterHt().get(defaultKey[0]);
			centerPoint[1][0] = entity.getCenterHt().get(defaultKey[1]);
			xydataset.addSeries(
					VisualLanguagePack.getMessage(
							VisualLanguagePack.CENTER_POINT, locale) + keys[i],
					centerPoint);
		}
		chart = ChartFactory.createScatterPlot("", defaultKey[0],
				defaultKey[1], xydataset, PlotOrientation.VERTICAL, true, true,
				false);
		XYPlot plot = chart.getXYPlot();
		int seriesCount = plot.getSeriesCount();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot
				.getRenderer();
		Shape shapeCenter = VisualUtility.getCenterPoint();
		Shape shapeScatter = VisualUtility.getScatterPoint();
		if (cluster != null && !cluster.equals("") && !cluster.equals("ALL")) {
			for (int i = 0; i < seriesCount; i++) {
				if (i % 2 == 1) {
					renderer.setSeriesShape(i, shapeCenter);
					renderer.setSeriesPaint(i - 1, entityHt.get(cluster)
							.getColor());
					renderer.setSeriesPaint(i, entityHt.get(cluster).getColor());
				} else {
					renderer.setSeriesShape(i, shapeScatter);
				}
			}
		} else {
			for (int i = 0; i < seriesCount; i++) {
				if (i % 2 == 1) {
					renderer.setSeriesShape(i, shapeCenter);
					renderer.setSeriesPaint(i - 1, colors[i / 2]);
					renderer.setSeriesPaint(i, colors[i / 2]);
				} else {
					renderer.setSeriesShape(i, shapeScatter);
				}
			}
		}
		plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
		plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		chart.getLegend().setItemFont(VisualResource.getChartFont());
		return chart;
	}
}
