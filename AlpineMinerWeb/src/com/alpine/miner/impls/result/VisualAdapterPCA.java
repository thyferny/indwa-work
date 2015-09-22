/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterAssociation.java
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

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPCA;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterPCA extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterPCA INSTANCE = new VisualAdapterPCA();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut, Locale locale)
			throws  Exception {
	   if(analyzerOutPut instanceof AnalyzerOutPutPCA ){
		 
			 AnalyzerOutPutPCA outPut = (AnalyzerOutPutPCA)analyzerOutPut;
			List<VisualizationModel> models = new ArrayList<VisualizationModel>(); 
			models.add(createResultTableModel(outPut, locale)) ;
			models.add(createPCATableModel(outPut, locale)) ;
			VisualizationModelComposite visualModel= new VisualizationModelComposite(analyzerOutPut.getDataAnalyzer().getName(),
					models);
			return visualModel;
	   }
	   else{
		   return null;
	   }
	}
	private VisualizationModel createPCATableModel(AnalyzerOutPutPCA outPut, Locale locale) throws AnalysisException {
		AnalyzerOutPutTableObject result  =outPut.getPCAResultTables();
	 
		if(result == null){
			return null;
		}
		
		
		DataTable dataTable =tableVType.getResultTableSampleRow(outPut, result.getSchemaName(),result.getTableName()) ;
		result.setColumns(dataTable.getColumns()) ;

		
		String dbType = outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
		DBUtil.reSetColumnType(dbType, dataTable) ;
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.PCA_OUTPUTTABLE,locale),
	
				dataTable);
		return visualModel;
	}
	private VisualizationModel createResultTableModel(AnalyzerOutPutPCA outPut, Locale locale) throws AnalysisException {

		AnalyzerOutPutTableObject result=outPut.getPCAQvalueTables();
		if(result == null){
			return null;
		}
 	
		DataTable dataTable =tableVType.getResultTableSampleRow(outPut, result.getSchemaName(),result.getTableName()) ;
		result.setColumns(dataTable.getColumns()) ;
		

		String dbType = outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
		DBUtil.reSetColumnType(dbType, dataTable) ;
	
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.PCA_VALUEOUTPUTTABLE,locale),
				dataTable);
		return visualModel;
		
	 

	}
 
 
}
