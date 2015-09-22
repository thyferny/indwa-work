/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterARIMARTrainer.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.timeseries.ARIMAModel;
import com.alpine.datamining.operator.timeseries.SingleARIMAModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.file.StringUtil;

public class VisualAdapterARIMARTrainer extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 

	public static final VisualAdapterARIMARTrainer INSTANCE = new VisualAdapterARIMARTrainer();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		
		EngineModel model  = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			model = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		 
		String message = "";
		if(model != null){
			message = generateTextOutput(((ARIMAModel)model.getModel()).getModels());
		}else{
			return null;
		}
		//toVText((LinearRegressionModelDB)model.getModel())
		String name=analyzerOutPut.getAnalyticNode().getName();
		List<VisualizationModel> models= new ArrayList<VisualizationModel>(); 
		
		VisualizationModelText textModel = new  VisualizationModelText(name, 	message);//message);
		models.add(textModel);
		
		List<SingleARIMAModel> amodels = ((ARIMAModel)model.getModel()).getModels();
		if(amodels!=null&&amodels.size()>0){
			VisualizationModel  vmodel =null;
			if(StringUtil.isEmpty(amodels.get(0).getGroupColumnName())){
				DataTable dataTable = generateSingleModelTable(amodels.get(0) ) ;
				vmodel=new VisualizationModelDataTable( 			VisualNLS.getMessage(VisualNLS.MODEL,locale), dataTable);
			}
			else{
				List<String> keys = new ArrayList<String>() ;
				HashMap<String, VisualizationModel> modelMap = new HashMap<String, VisualizationModel>(); 
				
				for (Iterator iterator = amodels.iterator(); iterator.hasNext();) {
					SingleARIMAModel singleARIMAModel = (SingleARIMAModel) iterator
							.next();
					DataTable dataTable = generateSingleModelTable(singleARIMAModel) ;
					String key = singleARIMAModel.getGroupColumnValue();
					VisualizationModelDataTable  tableModel=new VisualizationModelDataTable( 		key, dataTable);
					keys.add(key);
					modelMap.put(key, tableModel) ;
					
				}
			
			   vmodel = new VisualizationModelLayered(
					VisualNLS.getMessage(VisualNLS.MODEL,locale), amodels.get(0).getGroupColumnName(), keys, modelMap) ;
			}
			models.add( vmodel);
		}
		VisualizationModelComposite  visualModel= new VisualizationModelComposite(name	,models);
		
		return visualModel;
 
	}
 

	private DataTable generateSingleModelTable(SingleARIMAModel model) {
		TextTable table= new TextTable();

		
		double[] phi = model.getPhi();
		double[] standardError = model.getSe();
		double[] theta = model.getTheta();
		double intercept = model.getIntercept();
		int  ncxreg = model.getNcxreg();
		
		table.addLine(new String[]{ATTRIBUTE,COEFFICIENT,SE});
		String[] columnTypes = new String[]{DBUtil.TYPE_CATE,
				DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER};
		
    	if(ncxreg > 0){
    		table.addLine(new String[]{
					INTERCEPT,AlpineMath.powExpression(intercept),AlpineMath.powExpression(standardError[standardError.length - 1])
			});
    	}
		for(int i=0;i<phi.length;i++){
			table.addLine(new String[]{
					AR+i,AlpineMath.powExpression(phi[i]),AlpineMath.powExpression(standardError[i])
			});
		}
    	for(int i=0;i<theta.length;i++){
    		table.addLine(new String[]{
					MA+i,AlpineMath.powExpression(theta[i]),AlpineMath.powExpression(standardError[i+phi.length])
			});
    	}
		 
    	return  getDataTable4TimeSeries(table,columnTypes);
	}
 
	private String generateTextOutput(List<SingleARIMAModel> models) {
		StringBuffer result = new StringBuffer();  
		if(models!=null&&models.size()>0){
			double p=models.get(0).getP();
			double q=models.get(0).getQ();
			double d=models.get(0).getD();
	
			result.append(Tools.getLineSeparator() + P2 + String.valueOf(p) + Tools.getLineSeparator());
			result.append(Tools.getLineSeparator() + Q2 + String.valueOf(q) + Tools.getLineSeparator());
			result.append(Tools.getLineSeparator() + D2 + String.valueOf(d) + Tools.getLineSeparator());
			
			for (Iterator iterator = models.iterator(); iterator.hasNext();) {
				SingleARIMAModel model = (SingleARIMAModel) iterator.next();
				
			
				double sigma=model.getSigma2();
				double likelihood=model.getLikelihood();
				String str = "arima("+p+","+d+","+q+"):sigma^2 estimated as "+com.alpine.utility.tools.AlpineMath.doubleExpression(sigma)+
				":  part log likelihood = "+com.alpine.utility.tools.AlpineMath.doubleExpression(likelihood);
	
				String groupColumn=model.getGroupColumnName();
				if(StringUtil.isEmpty(groupColumn)){
					result.append(Tools.getLineSeparator() + SUMMARY + str + Tools.getLineSeparator());
				}else{
					result.append(Tools.getLineSeparator() +groupColumn+" = "+model.getGroupColumnValue()+" "
							+ SUMMARY + str + Tools.getLineSeparator());
				}
			}
    	}
		return result.toString();
	}
 
}
