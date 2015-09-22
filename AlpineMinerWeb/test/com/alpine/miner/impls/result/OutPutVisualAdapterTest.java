/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * OutPutVisualAdapterTest
 * 
 * @author john_zhao
 * 
 * Version 3.0
 * 
 * Date Jul 3, 2011
 */

package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.AfterClass;
import org.junit.Test;

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.impl.AnalyticNodeImpl;
import com.alpine.datamining.api.impl.algoconf.BarChartAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.table.TableAnalysisAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.flow.AbstractFlowTest;
import com.alpine.miner.utils.JSONUtility;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelBarChart;
import com.alpine.utility.db.TableColumnMetaInfo;

public class OutPutVisualAdapterTest extends  AbstractFlowTest{
 	
 
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Here is an example of how to read a flow file
	 * and publish it to the server.
	 * @throws Exception 
	 */
	@Test
	public void testDataGrid() throws Exception {
		DataAnalyzer analyzer = createSampleEvaluatorAnalyzer();
		
		AnalyzerOutPutTableObject sampleOutPut= new AnalyzerOutPutTableObject();
		sampleOutPut.setDataAnalyzer(analyzer);
		AnalyticNode analyticNode =new AnalyticNodeImpl();
 
		sampleOutPut.setAnalyticNode( analyticNode  );
		DataTable dataTable = new DataTable();
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		TableColumnMetaInfo e = new TableColumnMetaInfo("col1",""); 
		columns.add(e);
		 e = new TableColumnMetaInfo("col2",""); ; 
		columns.add(e);
		dataTable.setColumns(columns);
		List<DataRow> rows =new ArrayList<DataRow>();
		DataRow row = new DataRow();
		row.setData(new String[]{"101","102"});
		rows.add(row );
		row = new DataRow();
		row.setData(new String[]{"201","202"});
		rows.add(row);
		dataTable.setRows(rows);
		dataTable.setTableName("sampleTable") ;
		
		sampleOutPut.setDataTable(dataTable );
		VisualizationModel visualModel;
		try{
			visualModel = VisualAdapterTableData.INSTANCE.toVisualModel(sampleOutPut,Locale.getDefault());
		}catch(Exception e1){
			return;
		}
		  JSONObject json = JSONUtility.toJSONObject(visualModel,Locale.getDefault());
		  
		//{"id":"analyticNodeName","title":"analyticNodeName","visualType":"dataTable","visualDataName":"sampleTable","visualData":...
		Assert.assertNotNull(json);
		
 
		Assert.assertEquals(VisualizationModel.TYPE_DATATABLE,json.get(JSONUtility.OUT_KEY_VISUAL_TYPE));
	 
	
		JSONObject visualData=json.getJSONObject(JSONUtility.OUT_KEY_VISUAL_DATA) ;
		//{"columns":["col1","col2"],"items":[{"col1":"101","col2":"102"},{"col1":"201","col2":"202"}]}
		Assert.assertNotNull(visualData);
		
		JSONArray cols=visualData.getJSONArray(JSONUtility.OUT_KEY_COLUMNS);
		Assert.assertEquals("col1",cols.get(0));
		
		JSONArray items=		visualData.getJSONArray(JSONUtility.OUT_KEY_ITEMS);
		JSONObject item0=items.getJSONObject(0) ;
		Assert.assertEquals("101",item0.get("col1"));
		
	}
	
	/**
	 * Here is an example of how to read a flow file
	 * and publish it to the server.
	 * @throws Exception 
	 * @throws RuntimeException 
	 */
	@Test
	public void testBarChart() throws RuntimeException, Exception {
		
		 
		
		DataAnalyzer analyzer = createSampleBarchartAnalyzer();
		
		AnalyzerOutPutTableObject sampleOutPut= new AnalyzerOutPutTableObject();
		sampleOutPut.setDataAnalyzer(analyzer);
		
		AnalyticNode analyticNode =new AnalyticNodeImpl();
		analyticNode.setName("analyticNodeName") ;
		sampleOutPut.setAnalyticNode( analyticNode  );
		DataTable dataTable = new DataTable();
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		TableColumnMetaInfo e = new TableColumnMetaInfo("sales",""); 
		columns.add(e);
		 e = new TableColumnMetaInfo("region",""); ; 
		columns.add(e);
		 e = new TableColumnMetaInfo("quarter",""); ; 
			columns.add(e);
		dataTable.setColumns(columns);
		List<DataRow> rows =new ArrayList<DataRow>();
		addRow(rows,new String[]{"11","north","1"});
		addRow(rows,new String[]{"12","north","2"});
		addRow(rows,new String[]{"13","north","3"});
		addRow(rows,new String[]{"14","north","4"});

		addRow(rows,new String[]{"21","south","1"});
		addRow(rows,new String[]{"21","south","2"});
		addRow(rows,new String[]{"32","south","3"});
		addRow(rows,new String[]{"24","south","4"});

		
		addRow(rows,new String[]{"31","east","1"});
		addRow(rows,new String[]{"32","east","2"});
		addRow(rows,new String[]{"33","east","3"});
		addRow(rows,new String[]{"34","east","4"});

		
		addRow(rows,new String[]{"41","west","1"});
		addRow(rows,new String[]{"42","west","2"});
		addRow(rows,new String[]{"43","west","3"});
		addRow(rows,new String[]{"44","west","4"});

		
		dataTable.setRows(rows);
		dataTable.setTableName("sampleTable") ;
		
		sampleOutPut.setDataTable(dataTable );
	 
		  VisualizationModelBarChart vModel = (VisualizationModelBarChart) VisualAdapterBarChart.INSTANCE.toVisualModel(sampleOutPut,Locale.getDefault());
		
		  //{"id":"analyticNodeName","title":"analyticNodeName","visualType":"dataTable","visualDataName":"sampleTable","visualData":...
		Assert.assertNotNull(vModel);
		
		Assert.assertEquals("analyticNodeName",vModel.getTitle());
		Assert.assertEquals(VisualizationModel.TYPE_BAR_CHART,vModel.getVisualizationType());
	 		
		Assert.assertEquals("quarter",vModel.getxAxisTitle());
		Assert.assertEquals("region",vModel.getSeriesName());
		Assert.assertEquals("sales",vModel.getyAxisTitle());
  
		
	}

	private DataAnalyzer createSampleEvaluatorAnalyzer() {
		EvaluatorConfig config =new EvaluatorConfig();
	
	 	

		DataAnalyzer analyzer= new TableAnalysisAnalyzer();
		AnalyticSource source = new DataBaseAnalyticSource();
		source.setAnalyticConfiguration(config);
		analyzer.setAnalyticSource(source );
		return analyzer;
	}
	
	private DataAnalyzer createSampleBarchartAnalyzer() {
		BarChartAnalysisConfig config =new BarChartAnalysisConfig();
		config.setCategoryType("quarter");
		config.setScopeDomain("region");
		config.setValueDomain("sales");

		DataAnalyzer analyzer= new TableAnalysisAnalyzer();
		AnalyticSource source = new DataBaseAnalyticSource();
		source.setAnalyticConfiguration(config);
		analyzer.setAnalyticSource(source );
		return analyzer;
	}


	private void addRow(List<DataRow> rows, String[] data) {
		DataRow row = new DataRow();
		row.setData(data);
		rows.add(row );
	}
 
}
