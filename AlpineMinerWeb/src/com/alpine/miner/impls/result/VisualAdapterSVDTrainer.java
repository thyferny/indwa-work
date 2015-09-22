/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterSVDTrainer.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.output.SVDLanczosAnalyzerOutPutTrainModel;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterSVDTrainer extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterSVDTrainer INSTANCE = new VisualAdapterSVDTrainer();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws Exception {
		SVDLanczosAnalyzerOutPutTrainModel svdModel=(SVDLanczosAnalyzerOutPutTrainModel)analyzerOutPut;
 	 	 
		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
		
		
		
		String dbType = analyzerOutPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
		
		 AnalyzerOutPutTableObject dataTableObject = svdModel.getUmatrixTable(); 
		
		DataTable dataTable =tableVType.getResultTableSampleRow(analyzerOutPut, dataTableObject.getSchemaName(),dataTableObject.getTableName()) ;
		dataTableObject.setColumns(dataTable.getColumns()) ;		
		DBUtil.reSetColumnType(dbType, dataTable) ;	 
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(dataTableObject.getTableName(),dataTable);
		models.add(visualModel);

		dataTableObject = svdModel.getVmatrixTable(); 
		dataTable =tableVType.getResultTableSampleRow(analyzerOutPut, dataTableObject.getSchemaName(),dataTableObject.getTableName()) ;
		dataTableObject.setColumns(dataTable.getColumns()) ;	
		DBUtil.reSetColumnType(dbType, dataTable) ;	 
		visualModel= new VisualizationModelDataTable(dataTableObject.getTableName(),dataTable	);
		models.add(visualModel); 
		
		dataTableObject = svdModel.getSingularValueTable() ; 		
		dataTable =tableVType.getResultTableSampleRow(analyzerOutPut, dataTableObject.getSchemaName(),dataTableObject.getTableName()) ;
		dataTableObject.setColumns(dataTable.getColumns()) ;
		DBUtil.reSetColumnType(dbType, dataTable) ;
		
		visualModel= new VisualizationModelDataTable(dataTableObject.getTableName(),dataTable	);
		models.add(visualModel); 
  		
		//svd trainer allways get 2 table
		VisualizationModelComposite vModel= new VisualizationModelComposite(analyzerOutPut.getDataAnalyzer().getName(),
				models);
		
		return vModel;
	}
	
	

 	 
}
