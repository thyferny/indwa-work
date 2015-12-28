
package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.resources.AlpineThreadLocal;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class LinearRegressionGroupGPModel extends LinearRegressionModelDB{

	
    private static final Logger itsLogger = Logger.getLogger(LinearRegressionGroupGPModel.class);
    private static final long serialVersionUID = 1188841435428609671L;

	private String groupByColumn;
	
	
	private Map<String,LinearRegressionModelDB> modelList=new LinkedHashMap<String,LinearRegressionModelDB>();
	
	public LinearRegressionGroupGPModel(DataSet dataSet, String[] columnNames,
			String specifyColumn, Double[] coefficients,
			HashMap<String, Double> coefficientmap) {
		super(dataSet, columnNames, specifyColumn, coefficients, coefficientmap);
		// TODO Auto-generated constructor stub
	}

	public String getGroupByColumn() {
		return groupByColumn;
	}

	public void setGroupByColumn(String groupByColumn) {
		this.groupByColumn = groupByColumn;
	}

	public Map<String, LinearRegressionModelDB> getModelList() {
		return modelList;
	}

	public void setModelList(Map<String, LinearRegressionModelDB> modelList) {
		this.modelList = modelList;
	}
	
	public  LinearRegressionModelDB getOneModel(String groupString) {
		return modelList.get(groupString);
	}
	public void addOneModel( LinearRegressionModelDB model,String groupString) {
		modelList.put(groupString, model);
	}
	
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
	throws OperatorException {			
		this.predictedLabelName = predictedLabel.getName();
	
	dataSet.getColumns().setLabel(getLabel());


	String tableName=((DBTable) dataSet.getDBTable())
	.getTableName();
	String predictedLabelName = StringHandler.doubleQ(predictedLabel.getName());
	StringBuilder sb_update=new StringBuilder("update ");
	sb_update.append(tableName).append(" set (").append(predictedLabelName).append(")=(");
	StringBuffer predictedString = generatePredictedString(dataSet);
	sb_update.append(predictedString).append(")");
	DatabaseConnection databaseConnection = ((DBTable) dataSet
			.getDBTable()).getDatabaseConnection();

	Statement st = null;
	try {
		st = databaseConnection.createStatement(false);
		itsLogger.debug("LinearRegressionModelDB.performPrediction():sql="+sb_update);
		st.execute(sb_update.toString());
	} catch (SQLException e) {
		itsLogger.error(e.getMessage(),e);
		if(e.getErrorCode()==-206){//DB2
			throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.DB2_PREDICT_WRONG_DEPENDENTCOLUMN,
					AlpineThreadLocal.getLocale()));
		}else{
			throw new OperatorException(e.getLocalizedMessage());
		}	
	}
	return dataSet;
	}
	
	public StringBuffer generatePredictedString(DataSet dataSet) {
		String dbType = ((DBTable) dataSet.getDBTable())
		.getDatabaseConnection().getProperties().getName();
		StringBuffer predictedString = new StringBuffer();
		predictedString.append(" case ");
		
		for (String groupValue:this.modelList.keySet())
		{
			predictedString.append(" when ").append(StringHandler.doubleQ(groupByColumn)).append("=")
			.append(StringHandler.singleQ(groupValue)).append(" then ");
		
			predictedString.append(modelList.get(groupValue).coefficients[coefficients.length - 1]);
			Columns atts=getTrainingHeader().getColumns();
			Iterator<Column> atts_i=atts.iterator();
			while(atts_i.hasNext())
			{
				Column att=atts_i.next();
				String columnName=StringHandler.doubleQ(att.getName());
				if(att.isNumerical())
				{			
					if(modelList.get(groupValue).coefficientsMap.get(att.getName())==null)continue;
					double coefficient=modelList.get(groupValue).coefficientsMap.get(att.getName());
					predictedString.append("+").append(coefficient).append("*").append(columnName);
				}else
				{
				
					List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=modelList.get(groupValue).getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(modelList.get(groupValue).coefficientsMap.get(columnname)==null)continue;
 	 					double coefficient=modelList.get(groupValue).coefficientsMap.get(columnname);

 	 					predictedString.append("+(").append(coefficient).append(")*").append("(case ");
 	 					predictedString.append(" when ").append(columnName).append("=");
 	 					value=StringHandler.escQ(value);
						value = CommonUtility.quoteValue(
								dbType, att, value);
 	 					predictedString.append(value).append(" then 1  else 0 end)");

 	 				}
				}
			}
			Iterator<Entry<String, String>>  iter =modelList.get(groupValue).interactionColumnExpMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				String key = entry.getKey();
				String value = entry.getValue();
				if(coefficientsMap.get(key)!= null){
					predictedString.append("+"+value +"*(" + coefficientsMap.get(key)+")");
				}
			} 
		}
		predictedString.append(" end ");
		return predictedString;
	}

}
