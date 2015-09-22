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

import java.util.*;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.models.ARIMAHadoopModel;
import com.alpine.datamining.api.impl.hadoop.models.SingleARIMAHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.file.StringUtil;

public class VisualAdapterHadoopARIMARTrainer extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 

	public static final VisualAdapterHadoopARIMARTrainer INSTANCE = new VisualAdapterHadoopARIMARTrainer();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		
		EngineModel model  = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			model = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		 
		String message = "";
		if(model != null){
			message = generateTextOutput(((ARIMAHadoopModel)model.getModel()).getModels());
		}else{
			return null;
		}
		//toVText((LinearRegressionModelDB)model.getModel())
		String name=analyzerOutPut.getAnalyticNode().getName();
		List<VisualizationModel> models= new ArrayList<VisualizationModel>(); 
		
		VisualizationModelText textModel = new  VisualizationModelText(name, 	message);//message);
		models.add(textModel);
		
		List<SingleARIMAHadoopModel> amodels = ((ARIMAHadoopModel)model.getModel()).getModels();
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
					SingleARIMAHadoopModel singleARIMAModel = (SingleARIMAHadoopModel) iterator
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
 

	private DataTable generateSingleModelTable(SingleARIMAHadoopModel model) {
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
 
	private String generateTextOutput(List<SingleARIMAHadoopModel> models) {
        Collections.sort(models,new Comparator<SingleARIMAHadoopModel>() {
            @Override
            public int compare(SingleARIMAHadoopModel o1, SingleARIMAHadoopModel o2) {
                String groupV1 = o1.getGroupColumnValue();
                String groupV2 = o2.getGroupColumnValue();
                if(null==groupV1 || groupV2==null){
                    return 0;
                }
                try{
                  int v1 =Integer.parseInt(groupV1);
                  int v2 =Integer.parseInt(groupV2);
                  if(v1>v2){
                      return 1;
                  }else if(v1==v2){
                      return 0;
                  }else{
                      return -1;
                  }
                }catch (NumberFormatException e){
                    return groupV1.compareTo(groupV2);
                }
            }
        });
		StringBuffer result = new StringBuffer();  
		if(models!=null&&models.size()>0){
			double p=models.get(0).getP();
			double q=models.get(0).getQ();
			double d=models.get(0).getD();
	
			result.append(Tools.getLineSeparator() + P2 + String.valueOf(p) + Tools.getLineSeparator());
			result.append(Tools.getLineSeparator() + Q2 + String.valueOf(q) + Tools.getLineSeparator());
			result.append(Tools.getLineSeparator() + D2 + String.valueOf(d) + Tools.getLineSeparator());
			
			for (Iterator iterator = models.iterator(); iterator.hasNext();) {
				SingleARIMAHadoopModel model = (SingleARIMAHadoopModel) iterator.next();
				
			
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
