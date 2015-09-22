/**
 * ClassName VisualAdapterEMClustering.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-19
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.operator.EMCluster.EMClusterNode;
import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.TableColumnMetaInfo;

/**
 * @author Jeff Dong
 *
 */
public class VisualAdapterEMClustering extends AbstractOutPutVisualAdapter
		implements OutPutVisualAdapter {

	public static final VisualAdapterEMClustering INSTANCE = new VisualAdapterEMClustering();
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,
			Locale locale) throws Exception {
		AnalyzerOutPutTrainModel modelOutput=(AnalyzerOutPutTrainModel)analyzerOutPut;
		EMModel trainModel = (EMModel)modelOutput.getEngineModel().getModel();
		List<VisualizationModel> models= new ArrayList<VisualizationModel>();
		
		VisualizationModelDataTable vModel = generateSummaryInfoVModel(locale,
				trainModel);
		models.add(vModel);
		
		VisualizationModel clusterInfoVModel=generateClusterInfoVModel(locale, trainModel);
		models.add(clusterInfoVModel);
		
		VisualizationModelComposite  visualModel= new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName()
						,models);
		return visualModel;
	}

	protected VisualizationModelDataTable generateSummaryInfoVModel(
			Locale locale, EMModel trainModel) {
		DataTable table=new DataTable();
		List<TableColumnMetaInfo> columns=new ArrayList<TableColumnMetaInfo>();
		table.setColumns(columns);
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.CLUSTER,locale),DBUtil.TYPE_NUMBER));
		columns.add(new TableColumnMetaInfo("Alpha",DBUtil.TYPE_NUMBER));
		List<DataRow> tableRows =new ArrayList<DataRow>();
		for(int i:trainModel.getClusteInfo().keySet()){
			EMClusterNode singleCluster = trainModel.getClusteSingleValue(i);
			DataRow dataRow = new DataRow();
			String[] row=new String[2];
			row[0]=String.valueOf(i);
			row[1]=String.valueOf(singleCluster.getAlpha());
			dataRow.setData(row);
			tableRows.add(dataRow);
		}
		table.setRows(tableRows);
		VisualizationModelDataTable vModel=new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.SUMMARY,locale),table);
		return vModel;
	}

	protected VisualizationModel generateClusterInfoVModel(Locale locale, EMModel trainModel) {
		List<String[]> rows=new ArrayList<String[]>();
		for(int i:trainModel.getClusteInfo().keySet()){
			EMClusterNode singleCluster = trainModel.getClusteSingleValue(i);
			Map<String, Double> muValue = singleCluster.getMuValue();
			Map<String, Double> sigmaValue = singleCluster.getSigmaValue();
			Set<String> keySet = muValue.keySet();
			Iterator<String> iter = keySet.iterator();
			while(iter.hasNext()){
				String column=iter.next();
				String[] row=new String[4];
				row[0]=String.valueOf(i);
				row[1]=column;
				row[2]=String.valueOf(muValue.get(column));
				row[3]=String.valueOf(sigmaValue.get(column));
				rows.add(row);
			}
		}
		
		String[] columnHeads=new String[4];
		columnHeads[0]=VisualNLS.getMessage(VisualNLS.CLUSTER,locale);
		columnHeads[1]=VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale);
		columnHeads[2]="Mu";
		columnHeads[3]="Sigma";
		String[] columnTypes =new String[] {
				DBUtil.TYPE_CATE,
				DBUtil.TYPE_CATE,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER};
		
		VisualizationModel vModel = VisualUtils.generateLayeredTableModel(columnHeads,rows,0,locale,columnTypes,
				VisualNLS.getMessage(VisualNLS.EM_CLUSTER_INFO,locale),
				VisualNLS.getMessage(VisualNLS.EM_CLUSTER_VALUE,locale));
		return vModel;
	}

}
