/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterSVM.java
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

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.svm.SVMModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterSVM extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {


	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterSVM();
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale) 	throws RuntimeException {

		List<VisualizationModel> models= new ArrayList<VisualizationModel>();
		
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			EngineModel emodel = (EngineModel)((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
			if(emodel!=null&&(emodel.getModel() instanceof SVMModel)) {
			
				SVMModel model = (SVMModel)emodel.getModel();
			 
				//please be careful ,each subout of composite 's node is null
		 
		 
				String text=getVTextTable(model,locale); 
				VisualizationModel visualModel= new VisualizationModelText(VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale),text);
	 			
				models.add(visualModel) ;
		 
				   DataTable dataTable =getDataTable(model);
				visualModel= new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale),dataTable);
	 			
				models.add(visualModel) ;
				
		 
			}
		 
		}
	 	VisualizationModelComposite  visualModel= new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName()
				,models);
	 
		return visualModel;
	}

	private DataTable getDataTable(SVMModel model) {
		
		
		DataTable table= new DataTable();
		Double[] weights=model.getWeights();
		Double[] individuals=model.getIndividuals();
		

		DataSet newDataSet=model.getNewDataSet();
		Columns atts = newDataSet.getColumns();
		int colSize=atts.size()+1;
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo> ();
		columns.add(new TableColumnMetaInfo(WEIGHTS, "")) ;
		Iterator<Column> iter=atts.iterator();
		int count=1;
		while(iter.hasNext()){
			Column att=iter.next();
			columns.add(new TableColumnMetaInfo(att.getName(), "")) ;
	 
		}
		
		table.setColumns(columns);
		List<DataRow> rows = new ArrayList<DataRow> ();
		count=0;
		for(int i=0;i<weights.length;i++){
			String[] line=new String[colSize];
			line[0]=AlpineMath.powExpression(weights[i]);
			for(int j=0;j<atts.size();j++){
				line[j+1]=AlpineMath.powExpression(individuals[count++]);
			}
			DataRow row = new DataRow();
			row.setData(line) ;
			rows.add(row);
		}
	
		table.setRows(rows ) ;
		return table;
	}

	private String getVTextTable(SVMModel model,Locale locale) {
		int indDim=model.getIndDim();
		int nsvs=model.getNsvs();
		StringBuffer result = new StringBuffer();  
		
    	result.append(Tools.getLineSeparator() + VisualNLS.getMessage(VisualNLS.SVM_IND_DIM,locale)+":" + String.valueOf(indDim) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + VisualNLS.getMessage(VisualNLS.SVM_NSVS,locale)+":" + String.valueOf(nsvs) + Tools.getLineSeparator());
		return result.toString();
	}
	 
	 
}
