
package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class LinearRegressionModelDB extends Prediction {
    private static final Logger logger = Logger.getLogger(LinearRegressionModelDB.class);

    
	private static final long serialVersionUID = -9077215958415330973L;

	private String[] columnNames;
	
	private String specifyColumn;

	private String errorString;
	
	private List<double[]> residuals;
		
	private HashMap<String,HashMap<String,String>> allTransformMap_valueKey=new HashMap<String,HashMap<String,String>>(); 
	protected HashMap<String, String> interactionColumnExpMap = new HashMap<String, String>();
	private HashMap<String, String[]> interactionColumnColumnMap = new HashMap<String, String[]>();


	public HashMap<String, String[]> getInteractionColumnColumnMap() {
		return interactionColumnColumnMap;
	}

	public void setInteractionColumnColumnMap(
			HashMap<String, String[]> interactionColumnColumnMap) {
		this.interactionColumnColumnMap = interactionColumnColumnMap;
	}

	public HashMap<String, String> getInteractionColumnExpMap() {
		return interactionColumnExpMap;
	}

	public void setInteractionColumnExpMap(
			HashMap<String, String> interactionColumnExpMap) {
		this.interactionColumnExpMap = interactionColumnExpMap;
	}

	public HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}

	public void setAllTransformMap_valueKey(
			HashMap<String, HashMap<String, String>> allTransformMapValueKey) {
		allTransformMap_valueKey = allTransformMapValueKey;
	}


	protected HashMap<String,Double> coefficientsMap;
	
	protected Double[] coefficients;
	
	private double[] se;

	private double[] t;

	private double[] p;
	
	private double r2;
	
	private double s;
	protected String predictedLabelName;

	public LinearRegressionModelDB(DataSet dataSet, String [] columnNames,String specifyColumn,Double[] coefficients, HashMap<String,Double> coefficientmap) {
		super(dataSet);
		this.columnNames = columnNames;
		this.specifyColumn=specifyColumn;
		this.coefficients=coefficients;
		this.coefficientsMap=coefficientmap;
		if (coefficients != null)
		{
			se = new double[coefficients.length];
			t = new double[coefficients.length];
			p = new double[coefficients.length];
			for (int i = 0 ; i < coefficients.length; i++)
			{
				se[i] = Double.NaN;
				t[i] = Double.NaN;
				p[i] = Double.NaN;
			}
		}
	}
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException
	{
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
			logger.debug("LinearRegressionModelDB.performPrediction():sql=" + sb_update);
			st.execute(sb_update.toString());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
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
		predictedString.append(coefficients[coefficients.length - 1]);
		Columns atts=getTrainingHeader().getColumns();
		Iterator<Column> atts_i=atts.iterator();
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{			
				if(coefficientsMap.get(att.getName())==null)continue;
				double coefficient=coefficientsMap.get(att.getName());
				predictedString.append("+").append(coefficient).append("*").append(columnName);
			}else
			{
				
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(coefficientsMap.get(columnname)==null)continue;
 	 					double coefficient=coefficientsMap.get(columnname);

 	 					predictedString.append("+(").append(coefficient).append(")*").append("(case ");
 	 					predictedString.append(" when ").append(columnName).append("=");
 	 					value=StringHandler.escQ(value);
						value = CommonUtility.quoteValue(
								dbType, att, value);
 	 					predictedString.append(value).append(" then 1  else 0 end)");

 	 				}
			}
		}
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(coefficientsMap.get(key)!= null){
				predictedString.append("+"+value +"*(" + coefficientsMap.get(key)+")");
			}
		} 
		return predictedString;
	}

	public HashMap<String, Double> getCoefficientsMap() {
		return coefficientsMap;
	}

	public void setCoefficientsMap(HashMap<String, Double> coefficientsMap) {
		this.coefficientsMap = coefficientsMap;
	}

	public String getSpecifyColumn() {
		return specifyColumn;
	}

	public void setSpecifyColumn(String specifyColumn) {
		this.specifyColumn = specifyColumn;
	}
	
	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		if(this.errorString==null)
		{
			this.errorString = errorString;
		}else
		{
			this.errorString += errorString;
		}
		
	}
	
	
	public Double[] getCoefficients() {
		return coefficients;
	}

	
	public void setCoefficients(Double[] coefficients) {
		this.coefficients = coefficients;
	}

	
	public double[] getSe() {
		return se;
	}

	
	public void setSe(double[] se) {
		this.se = se;
	}

	
	public double[] getT() {
		return t;
	}

	
	public void setT(double[] t) {
		this.t = t;
	}

	
	public double[] getP() {
		return p;
	}

	
	public void setP(double[] p) {
		this.p = p;
	}
	public double getR2() {
		return r2;
	}
	
	public void setR2(double r2) {
		this.r2 = r2;
	}

	
	public double getS() {
		return s;
	}

	
	public void setS(double s) {
		this.s = s;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		if(getErrorString()!=null)
		{
			result.append(getErrorString());
		}
		result.append(getLabel().getName()+ " = ");
		for (int i = 0; i < columnNames.length; i++) {
				result.append(getCoefficientString(coefficients[index], first) + " * " + columnNames[i]);
				index++;
				first = false;
		}
		result.append(getCoefficientString(coefficients[coefficients.length - 1], first)+Tools.getLineSeparator());
		result.append(Tools.getLineSeparator());
		result.append("R2: "+getR2());
		result.append(Tools.getLineSeparator());

		if (Double.isNaN(getS()))
		{
			result.append(Tools.getLineSeparator());
			result.append("data size too small!");
			result.append(Tools.getLineSeparator());
			return result.toString();
		}
		result.append("Standard Error: "+getS());
		result.append(Tools.getLineSeparator());
    	result.append("Coefficients:" + Tools.getLineSeparator());
		result.append("Intercept: \t"+coefficients[columnNames.length]+"\tSE: "+se[columnNames.length] +"\tT-statistics: "+ t[columnNames.length] +"\tP-value: "+ p[columnNames.length]+Tools.getLineSeparator());
		for (int i = 0; i < columnNames.length; i++) {
			result.append("coefficient("+columnNames[i]+"): "+coefficients[i]+"\tSE: "+se[i] +"\tT-statistics: "+ t[i] +"\tP-value: "+ p[i]+Tools.getLineSeparator());
		}
		return result.toString();
	}
	public String getPredictedLabelName() {
		return predictedLabelName;
	}

	public void setPredictedLabelName(String predictedLabelName) {
		this.predictedLabelName = predictedLabelName;
	}

	public String getCoefficientString(double coefficient, boolean first) {
		if (!first) {
			if (coefficient >= 0)
				return " + " + Math.abs(coefficient);
			else
				return " - " + Math.abs(coefficient);
		} else {
			if (coefficient >= 0)
				return Double.toString(Math.abs(coefficient));
			else
				return " - " + Math.abs(coefficient);
		}
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	
	public void addResidual(double[] data){
		if(residuals==null){
			residuals=new ArrayList<double[]>();
		}
		residuals.add(data);
	}
	
	public List<double[]> getResiduals(){
		return residuals;
	}
}
