/**
 * ClassName DbTableAnalysisImageVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.miner.view.ui.dataset.DropDownListEntity;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.StringHandler;
/**
 * jimmy
 */
public class DbTableAnalysisImageVisualizationType extends
		ImageVisualizationType {
    private static final Logger itsLogger =Logger.getLogger(DbTableAnalysisImageVisualizationType.class);

    private static final double MAX_SHOW = 100.0;
	
	private static final Font font=new Font("Arial",Font.BOLD,12);

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		
		DataTable dataTable = new DataTable();
		if(analyzerOutPut instanceof AnalyzerOutPutTableObject){
			dataTable = ((AnalyzerOutPutTableObject)analyzerOutPut).getDataTable();
		}
		DataTable dt = null;
		try {
			dt = getResultTableSampleRow(analyzerOutPut,dataTable.getSchemaName(),dataTable.getTableName());
		} catch (AnalysisException e) {
			itsLogger.error(e.getMessage(),e);
			return null;
		}
		
		DefaultCategoryDataset dataset =new DefaultCategoryDataset();
		int count=0;
		int total=dataTable.getRows().size();
		
		int columnSize = dataTable.getColumns().size();

		boolean containCategoryColumn = true;
		
		if(columnSize==2){
			containCategoryColumn = false;
		}
		
		String xLabel = null;
		String yLabel = null;
		if(containCategoryColumn==true){
			String categoryColumnType=getCategoryDataType(dt.getColumns(), dataTable.getColumns().get(2).getColumnName());
			
			String dbType = analyzerOutPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
			
			DataSourceType stype = DataSourceType.getDataSourceType(dbType);
			
			boolean categoryColumnTypeIsNum=stype.isNumberColumnType(categoryColumnType);
					
			xLabel=dataTable.getColumns().get(2).getColumnName();
			yLabel=dataTable.getColumns().get(0).getColumnName();
			
			List<DataRow> dataRows=new ArrayList<DataRow>();
			
				//find min and max
				List<Double> xList=new ArrayList<Double>();
				List<Double> yList=new ArrayList<Double>();
				for(DataRow dr:dataTable.getRows()){
					if((total<=10||(count%(total/MAX_SHOW))<1)&&dr.getData(0)!=null){
						yList.add(Double.parseDouble(dr.getData(0)));
						if(categoryColumnTypeIsNum){
							xList.add(Double.parseDouble(dr.getData(2)));
						}
						dataRows.add(dr);
					}
					count++;
				}
			
				DataRow[] dataRowArray=dataRows.toArray(new DataRow[dataRows.size()]);
							
				Double[] yArray = yList.toArray(new Double[yList.size()]);
				Arrays.sort(yArray);
				double minY = yArray[0].doubleValue();
				double maxY = yArray[yArray.length-1].doubleValue();
				long m = AlpineMath.adjustUnits(minY, maxY);
				
				if(categoryColumnTypeIsNum){
					Double[] xArray = xList.toArray(new Double[xList.size()]);
					Arrays.sort(xArray);
					double minX = xArray[0].doubleValue();
					double maxX = xArray[xArray.length-1].doubleValue();
					long n = AlpineMath.adjustUnits(minX, maxX);
					
					for(DataRow dr:dataRowArray){
						if(dr.getData(0)!=null){
							dataset.addValue(Double.parseDouble(dr.getData(0))/m,dr.getData(1),String.valueOf(Double.parseDouble(dr.getData(2))/n));
						}
					}
					 xLabel=n==1?xLabel:xLabel
								+" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
								" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
				}else{			
					for(DataRow dr:dataTable.getRows()){
						if(dr.getData(0)!=null){
							dataset.addValue(Double.parseDouble(dr.getData(0))/m,dr.getData(1),dr.getData(2));
						}
					}
				}
				yLabel=m==1?yLabel:yLabel
						 +" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
							" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(m)+")";
		}else{
			xLabel=dataTable.getColumns().get(1).getColumnName();
			yLabel=dataTable.getColumns().get(0).getColumnName();
			List<DataRow> dataRows=new ArrayList<DataRow>();
			
			//find min and max
			List<Double> yList=new ArrayList<Double>();
			for(DataRow dr:dataTable.getRows()){
				if((total<=10||(count%(total/MAX_SHOW))<1)&&dr.getData(0)!=null){
					yList.add(Double.parseDouble(dr.getData(0)));
					dataRows.add(dr);
				}
				count++;
			}
			
			Double[] yArray = yList.toArray(new Double[yList.size()]);
			Arrays.sort(yArray);
			double minY = yArray[0].doubleValue();
			double maxY = yArray[yArray.length-1].doubleValue();
			long m = AlpineMath.adjustUnits(minY, maxY);
			
			for(DataRow dr:dataTable.getRows()){
				if(dr.getData(0)!=null){
					dataset.addValue(Double.parseDouble(dr.getData(0))/m,dr.getData(1),"");
				}
			}
			
			yLabel=m==1?yLabel:yLabel
					 +" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(m)+")";
		}
		
		JFreeChart chart=ChartFactory.createBarChart3D("",xLabel,yLabel, dataset, PlotOrientation.VERTICAL,true,true,true);
		chart.getLegend().setItemFont(VisualResource.getChartFont());
		LegendTitle legend = chart.getLegend();
		legend.setVisible(false);

		CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
		
		LegendTitle legendtitle = new LegendTitle(categoryplot);
		
		BlockContainer blockcontainer = new BlockContainer(
			    new BorderArrangement());
			  blockcontainer.setBorder(1.0D, 1.0D, 1.0D, 1.0D);

			  LabelBlock labelblock = new LabelBlock(dataTable.getColumns().get(1).getColumnName()+":",font);
			  labelblock.setPadding(5D, 5D, 5D, 15D);

			  blockcontainer.add(labelblock, RectangleEdge.LEFT);
			  
			  legendtitle.setItemFont(font);
			  BlockContainer blockcontainer1 = legendtitle.getItemContainer();

			  blockcontainer1.setPadding(2D, 10D, 5D, 2D);

			  blockcontainer.add(blockcontainer1);
			  legendtitle.setWrapper(blockcontainer);

			  legendtitle.setPosition(RectangleEdge.BOTTOM);

			  legendtitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
			  chart.addSubtitle(legendtitle);
		
		categoryplot.getRenderer().getLegendItems();
		
		CategoryAxis dox  = categoryplot.getDomainAxis();
		dox.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		
		categoryplot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
		categoryplot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		categoryplot.getDomainAxis().setTickLabelFont(VisualResource.getChartFont());
		DropDownListEntity entity=new DropDownListEntity();
		entity.setJfreechart(chart);
		entity.setObj(DbTableAnalysisImageVisualizationType.class.getCanonicalName());
		DropDownListVisualizationOutPut output = new DropDownListVisualizationOutPut(entity);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
	
	private String getCategoryDataType(List<TableColumnMetaInfo> columns,
			String categoryColumn) {
		String categoryColumnType=null;
		Iterator<TableColumnMetaInfo>  iter=columns.iterator();
		while(iter.hasNext()){
			TableColumnMetaInfo meta=iter.next();
			if(StringHandler.doubleQ(meta.getColumnName()).equals(categoryColumn)){
				categoryColumnType=meta.getColumnsType();
				break;
			}
		}
		return categoryColumnType;
	}

}
