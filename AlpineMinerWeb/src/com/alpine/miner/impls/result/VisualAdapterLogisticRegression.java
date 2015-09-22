/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterLogisticRegression.java
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
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.db.trainer.LogisticRegressionTrainerGeneral;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.regressions.LoRModelIfc;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;

import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.controller.ModelController;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener;
import com.alpine.miner.impls.web.resource.WebRunAnalyticProcessListener;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.util.AlpineUtil;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterLogisticRegression extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
  

	public static final VisualAdapterLogisticRegression INSTANCE = new VisualAdapterLogisticRegression();
    public static final int FOR_GROUP_BY_TEXT_TABLE_TYPE = 199;
    //user <group,model>
    public static Map<String,Map<String,VisualizationModelDataTable>> LogisticRegressionGroupModelMap = new HashMap<String, Map<String,VisualizationModelDataTable>>();

	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut , Locale locale)
			throws RuntimeException {

        //((AnalyzerOutPutTrainModel) analyzerOutPut).getDataAnalyzer();


		EngineModel model = null;
		if (analyzerOutPut instanceof AnalyzerOutPutTrainModel) {
			model = ((AnalyzerOutPutTrainModel) analyzerOutPut).getEngineModel();
		}
		if (model == null) {
            return null;
        }



        List<VisualizationModel> models = new ArrayList<VisualizationModel>();

        if(model.getModel() instanceof LogisticRegressionGroupModel){
            LogisticRegressionGroupModel groupModel = (LogisticRegressionGroupModel) model.getModel();
            Map<String,LogisticRegressionModelDB> groupModelMap = groupModel.getModelList();

            if(null!=groupModelMap){
                String currentUser = (String) AlpineUtil.VALUE_PASSER.get();
                LogisticRegressionGroupModelMap.put(currentUser,null);
                Set<Map.Entry<String,LogisticRegressionModelDB>> gropModelEntry = groupModelMap.entrySet();
                if(null!=gropModelEntry){
                    Iterator<Map.Entry<String,LogisticRegressionModelDB>> itor = gropModelEntry.iterator();
                    List<LogisticRegressionModelDB> logisticRegressionModelDBList = new ArrayList<LogisticRegressionModelDB>();
                    List<String> keys = new ArrayList<String>();

                    while (itor.hasNext()){
                        Map.Entry<String,LogisticRegressionModelDB> entry = itor.next();
                        LogisticRegressionModelDB lrmb = entry.getValue();
                        //String key = entry.getKey();
                        logisticRegressionModelDBList.add(lrmb);
                        keys.add(entry.getKey());
                        //buildGroupModules(models,lrmb,locale);
                    }
                    DataTable dataTable4Text = getVtext4Table(logisticRegressionModelDBList,keys,locale) ;
                    VisualizationModelDataTable tableModel = new VisualizationModelDataTable(
                            VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale), dataTable4Text);

                    tableModel.setVisualizationType(VisualAdapterLogisticRegression.FOR_GROUP_BY_TEXT_TABLE_TYPE);
                    models.add(tableModel);

                    if(logisticRegressionModelDBList.size()>0){
                        Map<String, VisualizationModelDataTable> map = new HashMap<String, VisualizationModelDataTable>();
                        for(int i=0;i<logisticRegressionModelDBList.size();i++){
                            LogisticRegressionModelDB lrmb =logisticRegressionModelDBList.get(i);

                            DataTable dataTable = getDataTable(lrmb);
                            VisualizationModelDataTable tableMD = new VisualizationModelDataTable(
                                    VisualNLS.getMessage(VisualNLS.DATA_GROUP,locale)+":'"+keys.get(i)+"'", dataTable);
                            map.put(keys.get(i),tableMD);
                           // buildGroupModules(models,currentUser,lrmb,keys.get(i),locale);
                        }
                        LogisticRegressionGroupModelMap.put(currentUser,map);
                    }
                }
            }
        }else{
            //no group by

                String message = "";
                message = toVText((LoRModelIfc) model.getModel(),locale);
                VisualizationModelText textModel = new VisualizationModelText(
                        VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale),
                        message);// message);
                models.add(textModel);

                DataTable dataTable = getDataTable((LoRModelIfc) model.getModel());
                VisualizationModelDataTable tableModel = new VisualizationModelDataTable(
                        VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale), dataTable);
                models.add(tableModel);

        }


        String name = analyzerOutPut.getAnalyticNode().getName();

		VisualizationModelComposite visualModel = new VisualizationModelComposite(name, models);

		return visualModel;

	}

    private void buildGroupModules(List<VisualizationModel> models,String currentUser,LogisticRegressionModelDB model,String key, Locale locale){
        //String message = "";
       // message = toVText(model,  locale);
        //VisualizationModelText textModel = new VisualizationModelText(
        //        VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale),
        //        message);// message);
       // models.add(textModel);

        DataTable dataTable = getDataTable(model);
        VisualizationModelDataTable tableModel = new VisualizationModelDataTable(
                VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale)+"['"+key+"']", dataTable);
        //models.add(tableModel);

    }

 
	private String[] getBias(LoRModelIfc model){
		double[] beta = model.getBeta();
		double[] standardError = model.getStandardError();
		double[] zValue = model.getzValue();
		double[] pValue = model.getpValue();
		double[] waldStatistic = model.getWaldStatistic();
		String[] bias = new String[7];

			bias[0] = BIAS_OFFSET;
			bias[1] = AlpineMath.powExpression(beta[beta.length - 1]);
			bias[2] = "";	
			bias[3] = AlpineMath.powExpression(standardError[standardError.length - 1]);
			bias[4] = AlpineMath.powExpression(zValue[standardError.length - 1]);
			bias[5] = AlpineMath.powExpression(pValue[standardError.length - 1]);
			bias[6] = AlpineMath.powExpression(waldStatistic[waldStatistic.length - 1]);
		
			return bias;

	}
	

	private DataTable getDataTable(LoRModelIfc model) {
		DataTable te = new DataTable();
		String bias[] = getBias(model);
		TextTable table = getVTextTable( model);
		List<DataRow> rows = new ArrayList<DataRow>();

		if(bias != null){
			DataRow row = new DataRow();
			row.setData(bias);
			rows.add(row);
			 
		}
		
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				String[] columnTypes = new String[]{DBUtil.TYPE_CATE,
						DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER};
				setColumns(te, table.getLines().get(i),columnTypes);
			 	
				 
			}else{
				String attribute = table.getLines().get(i)[0];
				table.getLines().get(i)[0] = BETA +
						"("+attribute+")";
				DataRow row = new DataRow();
				row.setData((table.getLines().get(i)));
				rows.add(row);
			}
		} 
		te.setRows(rows);
		return te;
	}


	public static  TextTable getVTextTable(LoRModelIfc model) {
		TextTable table= new TextTable();
		double[] beta = model.getBeta();
		String[]attributeNames=model.getColumnNames();
		double[] standardError = model.getStandardError();
		double[] zValue = model.getzValue();
		double[] pValue = model.getpValue();
		double[] waldStatistic = model.getWaldStatistic();
		
		table.addLine(new String[]{ATTRIBUTE,BETA,ODDS_RATIO,SE,Z_VALUE,P_VALUE,WALD});
    	
		for (int j = 0; j < beta.length - 1; j++) {
			table.addLine(new String[]{attributeNames[j],AlpineMath.powExpression(beta[j]),
					AlpineMath.powExpression(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(Math.exp(beta[j])))),
					AlpineMath.powExpression(standardError[j]),AlpineMath.powExpression(zValue[j]),
					AlpineMath.powExpression(pValue[j]),AlpineMath.powExpression(waldStatistic[j])});
				
		}
		return table;
	}

	/**
	 * @param model
	 * @return
	 */
	private String toVText(LoRModelIfc model,Locale locale) {
		StringBuffer result = getVtextText(model,  locale);
 
		return result.toString();
		 
	}

	/**
	 * @param model
	 * @return
	 */
	public static StringBuffer getVtextText(LoRModelIfc model,Locale locale) {
		StringBuffer result = new StringBuffer();
		if (model.isImprovementStop()==false)
    	{
    		result.append(VisualNLS.getMessage(VisualNLS.ALGORITHM_DID_NOT_CONVERGE,locale)).append(Tools.getLineSeparators(2));
    	}

    	result.append(VisualNLS.getMessage(VisualNLS.ITERATION,locale)+": "
    				+model.getIteration()+Tools.getLineSeparator());
    	   	
    	result.append(Tools.getLineSeparator() + DEVIANCE +
    			": " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getModelDeviance()) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + NULL_DEVIANCE +
    			": " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getNullDeviance()) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + CHI_SQUARE +
    			": " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getChiSquare()) + Tools.getLineSeparator());

    	result.append(Tools.getLineSeparator() + Fraction_of_Variance_Explained +
    			": " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getChiSquare()/model.getNullDeviance()) + Tools.getLineSeparator());

//    	Fraction of Variance Explained =chi-square/null deviance 
    	
		return result;
 
	}

    private DataTable getVtext4Table(List<LogisticRegressionModelDB> modelDBList,List<String> keys,Locale locale){
        DataTable table= new DataTable();
        List<TableColumnMetaInfo> columnMetaInfoList = new ArrayList<TableColumnMetaInfo>();
        columnMetaInfoList.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.Logistic_Regression_Group_By_Value,locale),"string"));
        columnMetaInfoList.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.ITERATION,locale),DBUtil.TYPE_NUMBER));
        columnMetaInfoList.add(new TableColumnMetaInfo(DEVIANCE,DBUtil.TYPE_NUMBER));
        columnMetaInfoList.add(new TableColumnMetaInfo(NULL_DEVIANCE,DBUtil.TYPE_NUMBER));
        columnMetaInfoList.add(new TableColumnMetaInfo(CHI_SQUARE,DBUtil.TYPE_NUMBER));
        columnMetaInfoList.add(new TableColumnMetaInfo(Fraction_of_Variance_Explained,DBUtil.TYPE_NUMBER));
        columnMetaInfoList.add(new TableColumnMetaInfo(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_IS_CONVERGE, locale),"string"));
        table.setColumns(columnMetaInfoList);
        List<DataRow> dataRows = new ArrayList<DataRow>();
        if(null!=modelDBList && modelDBList.size()>0){
          Iterator<LogisticRegressionModelDB> modelDBIterator = modelDBList.iterator();
            int i=0;
            while (modelDBIterator.hasNext()){
                LogisticRegressionModelDB modelDB= modelDBIterator.next();
                DataRow row = new DataRow(new String[]{
                        keys.get(i),
                        String.valueOf(modelDB.getIteration()),
                        com.alpine.utility.tools.AlpineMath.doubleExpression(modelDB.getModelDeviance()),
                        com.alpine.utility.tools.AlpineMath.doubleExpression(modelDB.getNullDeviance()),
                        com.alpine.utility.tools.AlpineMath.doubleExpression(modelDB.getChiSquare()),
                        com.alpine.utility.tools.AlpineMath.doubleExpression(modelDB.getChiSquare()/modelDB.getNullDeviance()),
                        modelDB.isImprovementStop()==true?"true":"false"
                });
                i=i+1;
               dataRows.add(row);
            }
        }
        table.setRows(dataRows);
        return  table;
    }

 
}
