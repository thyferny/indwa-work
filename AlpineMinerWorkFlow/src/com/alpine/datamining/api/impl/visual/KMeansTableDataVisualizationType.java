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
import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.AbstractDBTableOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.operator.kmeans.ClusterModel;
import com.alpine.miner.view.ui.dataset.TableEntity;
import org.apache.log4j.Logger;

public class KMeansTableDataVisualizationType extends TableVisualizationType {
    private static final Logger itsLogger =Logger.getLogger(KMeansTableDataVisualizationType.class);

    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		ClusterModel clusterModel= null;
		
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof ClusterModel){
				clusterModel = (ClusterModel)obj;
				
				DataTable dt=null;
				
				try {
					dt = getResultTableSampleRow(analyzerOutPut,clusterModel.getSchemaName(),clusterModel.getTableName());
				} catch (AnalysisException e) {
					itsLogger.error(e.getMessage(),e);
					return null;
				}
				TableEntity tableEntity = new TableEntity();
				
				generateTableEntity(dt, tableEntity);
				
				DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(tableEntity);
				output.setName(analyzerOutPut.getAnalyticNode().getName());
			 
				output.fillDBTableInfo((AbstractDBTableOutPut)analyzerOutPut);
				
				return output;
			}
		}	 
		return null;
	}
}
