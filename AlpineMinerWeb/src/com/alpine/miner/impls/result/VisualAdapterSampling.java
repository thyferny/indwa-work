/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterSampling.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutSampling;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterSampling extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {

	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterSampling();
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale) 	throws RuntimeException, AnalysisException {

		List<VisualizationModel> models= new ArrayList<VisualizationModel>();
		
		if(outPut instanceof AnalyzerOutPutSampling && ((AnalyzerOutPutSampling) outPut).isHadoopSampling() == true){
            AnalyzerOutPutSampling outputTableSampling = (AnalyzerOutPutSampling)outPut;

            List<AnalyzerOutPutTableObject> outputTables = outputTableSampling.getSampleTables();

            for (Iterator<AnalyzerOutPutTableObject> iterator = outputTables.iterator(); iterator.hasNext();) {

                AnalyzerOutPutTableObject tableObject =  iterator .next();


               DataTable dataTable = tableObject.getDataTable();

                VisualizationModelDataTable visualModel= new VisualizationModelDataTable(tableObject.getTableName(),dataTable);

                models.add(visualModel) ;

            }
		 
		}else if(outPut instanceof AnalyzerOutPutSampling && ((AnalyzerOutPutSampling) outPut).isHadoopSampling() == false){
            AnalyzerOutPutSampling outputTableSampling = (AnalyzerOutPutSampling)outPut;

            List<AnalyzerOutPutTableObject> outputTables = outputTableSampling.getSampleTables();

            for (Iterator<AnalyzerOutPutTableObject> iterator = outputTables.iterator(); iterator.hasNext();) {

                AnalyzerOutPutTableObject tableObject =  iterator .next();


                DataTable dataTable =tableVType.getResultTableSampleRow(outPut, tableObject.getSchemaName(),tableObject.getTableName()) ;
                tableObject.setColumns(dataTable.getColumns()) ;



                String dbType = outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
                DBUtil.reSetColumnType(dbType, dataTable) ;

                VisualizationModelDataTable visualModel= new VisualizationModelDataTable(tableObject.getTableName(),dataTable);

                models.add(visualModel) ;

            }
        }
		
		
		VisualizationModel  visualModel= new VisualizationModelComposite(outPut.getDataAnalyzer().getName()
				,models);
		 
		return visualModel;
	}


}
