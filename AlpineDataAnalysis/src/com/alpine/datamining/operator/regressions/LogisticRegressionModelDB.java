
package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.datamining.MinerInit;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.BinaryPredictionModel;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class LogisticRegressionModelDB extends BinaryPredictionModel implements LoRModelIfc{
    private static final Logger logger = Logger.getLogger(LogisticRegressionModelDB.class);

    
	private static final long serialVersionUID = -4961451839928536806L;

	public static final String interceptString = "intercept:lr:alpine";
	protected double[] beta = null;
    
    private double[] standardError = null;
    

	private double[] waldStatistic = null;
    
    private double[] zValue = null;
    
    private double[] pValue = null;

    private double modelDeviance = Double.NaN;
    
    private double nullDeviance = Double.NaN;
    
    private double chiSquare = Double.NaN;
    
    private String[] columnNames;
    
    protected DataSet oldDataSet;
	private ArrayList<String> columnNamesArray;
    
    protected boolean interceptAdded;

	protected boolean useCFunction = false;
    
    private boolean improvementStop;
    private long iteration = 20;
	protected IDataSourceInfo dataSourceInfo = null;
	protected IMultiDBUtility multiDBUtility = null;
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
	public long getIteration() {
		return iteration;
	}
	public void setIteration(long iteration) {
		this.iteration = iteration;
	}
	protected String good = null;
	private HashMap<String,HashMap<String,String>> allTransformMap_valueKey=new HashMap<String,HashMap<String,String>>();    
    
    public HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}
	public void setAllTransformMap_valueKey(
			HashMap<String, HashMap<String, String>> allTransformMapValueKey) {
		allTransformMap_valueKey = allTransformMapValueKey;
	}
    
    public boolean isInterceptAdded() {
		return interceptAdded;
	}
	public LogisticRegressionModelDB(DataSet dataSet,DataSet oldDataSet, double[] beta, double[] variance,boolean interceptAdded, String goodValue) {
        super(dataSet, 0.5d);
        this.dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
        this.multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
        this.good = goodValue;
        this.columnNames = com.alpine.datamining.db.CommonUtility.getRegularColumnNames(dataSet);
        this.columnNamesArray = new ArrayList<String>();
        for (int i = 0; i < columnNames.length; i++)
        {
        	columnNamesArray.add(columnNames[i]);
        }
        this.beta = beta;
        this.interceptAdded = interceptAdded;
        this.oldDataSet=oldDataSet;
        
        standardError = new double[variance.length];
        waldStatistic = new double[variance.length];
        zValue = new double[variance.length];
        pValue = new double[variance.length];
        for (int j = 0; j < beta.length; j++) {
        	standardError[j] = Math.sqrt(variance[j]);
        	waldStatistic[j] = beta[j] * beta[j] / variance[j];
        	zValue[j] = beta[j]/standardError[j];
        	pValue[j] = norm(zValue[j]);
        }

        this.useCFunction = MinerInit.isUseCFunction();
    }
	double norm(double z) {
		z=Math.abs(z);
		double p=1 + z*(0.04986735+ z*(0.02114101+ z*(0.00327763+ z*(0.0000380036+ z*(0.0000488906+ z*0.000005383)))));
		p=p*p; p=p*p; p=p*p;
		return 1/(p*p);
	}

	public String getGood()
    {
    	return good;
    }
    public void setGood(String good)
    {
    	this.good = good;
    }
	
	public double getModelDeviance() {
		return modelDeviance;
	}
	
	public void setModelDeviance(double modelDeviance) {
		this.modelDeviance = modelDeviance;
	}
	
	public double getNullDeviance() {
		return nullDeviance;
	}
	
	public void setNullDeviance(double nullDeviance) {
		this.nullDeviance = nullDeviance;
	}
	
	public double getChiSquare() {
		return chiSquare;
	}
	
	public void setChiSquare(double chiSquare) {
		this.chiSquare = chiSquare;
	}
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
	throws OperatorException {	

		dataSet.getColumns().setLabel(getLabel());
		
    	Statement st = null;
    	DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}
			
		StringBuilder probability = getProbability();

		StringBuilder sql =new StringBuilder("");
		sql.setLength(0);
		StringBuilder functionValuesb=new StringBuilder("(");
		functionValuesb.append(probability).append(")");
		
		String goodColumn = good;
		String badColumn;
		if (getLabel().getMapping().mapIndex(0).equals(good))
		{
			badColumn = getLabel().getMapping().mapIndex(1);
		}
		else if(getLabel().getMapping().mapIndex(1).equals(good))
		{
			badColumn = getLabel().getMapping().mapIndex(0);
		}
		else
		{
			logger.error(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.GOOD_VALUE_NOT_EXIST, AlpineThreadLocal.getLocale()));
			throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.GOOD_VALUE_NOT_EXIST, AlpineThreadLocal.getLocale()));
		}
		goodColumn=StringHandler.escQ(goodColumn);
		badColumn=StringHandler.escQ(badColumn);
		StringBuilder predictionStringsb = new StringBuilder("(case when ");
		predictionStringsb.append(functionValuesb).append(" > 0.5 then ");
		appendValue(goodColumn, predictionStringsb);
		predictionStringsb.append(" else ");
		appendValue(badColumn, predictionStringsb);
		predictionStringsb.append(" end)");
		String predictedLabelName=StringHandler.doubleQ(predictedLabel.getName());
		sql.append("update ").append(tableName);
		appendUpdateSet(dataSet, sql, functionValuesb, goodColumn, badColumn,
				predictionStringsb, predictedLabelName);
		sql.append(getWhere(oldDataSet));
		try {
			logger.debug("LogisticRegressionModelDB.performPrediction():sql=" + sql);
			st.executeUpdate(sql.toString());		
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Sql Error!");
			throw new OperatorException(e.getLocalizedMessage());
		}

		return dataSet;
	}
	protected void appendUpdateSet(DataSet dataSet, StringBuilder sql,
			StringBuilder functionValuesb, String goodColumn, String badColumn,
			StringBuilder predictionStringsb, String predictedLabelName) {
		sql.append(" set (").append(predictedLabelName).append(",")
		.append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + goodColumn).getName()))
		.append(",").append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + badColumn).getName()));
		sql.append(") =( ").append(predictionStringsb).append(" ,").append(functionValuesb).append(" ,1.0 - ").append(functionValuesb).append(")");
	}
	protected StringBuilder getProbability() {
		StringBuilder probability = null;
		if (useCFunction)
		{
			probability = getProbabilityFunction(oldDataSet);
		}
		else
		{
			probability = getProbabilitySql(oldDataSet);
		}
		return probability;
	}
	protected void appendValue(String value, StringBuilder sql) {
		if (getLabel().isNominal())
		{
			sql.append("'");
		}
		sql.append(value);
		if (getLabel().isNominal())
		{
			sql.append("'");
		}
	}

	protected StringBuilder getProbabilityFunction(DataSet dataSet) {
		StringBuffer columnNamesArray = new StringBuffer(multiDBUtility.floatArrayHead());
		StringBuffer betaArray = new StringBuffer(multiDBUtility.floatArrayHead());
		HashMap<String, Double> betaMap = getBetaMap();
		boolean first = true;
		for (Column column : dataSet.getColumns()) {
			String ColumnName = StringHandler.doubleQ(column.getName());
			if (column.isNumerical()) {
				if(betaMap.get(column.getName())==null)continue;
				double beta = betaMap.get(column.getName());
				if (!first) {
					columnNamesArray.append(",");
					betaArray.append(",");
				}
				else
				{
					first = false;
				}
				columnNamesArray.append(ColumnName);
				betaArray.append(beta);
			} else {
				HashMap<String, String> TransformMap_valueKey = new HashMap<String, String>();
				TransformMap_valueKey = getAllTransformMap_valueKey().get(
						column.getName());
				if(TransformMap_valueKey==null)continue;
				Iterator<String> valueIterator = TransformMap_valueKey.keySet().iterator();
				String value = null;
				while (valueIterator.hasNext())
				{
					value = valueIterator.next();
					String columnname = TransformMap_valueKey.get(value);
					if (betaMap.get(columnname) == null)
						continue;
					double beta = betaMap.get(columnname);
					value=StringHandler.escQ(value);
					if(!first)
					{
						columnNamesArray.append(",");
						betaArray.append(",");
					}
					else
					{
						first = false;
					}
					columnNamesArray.append("(case").append(
					" when ").append(ColumnName)
					.append("=")
					.append(CommonUtility.quoteValue(dataSourceInfo.getDBType(),column, value))
					.append(" then 1 else 0 end)");
					betaArray.append(beta);
				}
			}
		}

		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(betaMap.get(key)!= null){
				if(!first)
				{
					columnNamesArray.append(",");
					betaArray.append(",");
				}
				else
				{
					first = false;
				}
				betaArray.append(betaMap.get(key));
				columnNamesArray.append(value);
			}
		}

		if (interceptAdded) {
			if(first)
			{
				betaArray.append(betaMap.get(interceptString));
			}else
			{
				first = false;
				betaArray.append(",").append(betaMap.get(interceptString));
			}
			
		}

		columnNamesArray.append(multiDBUtility.floatArrayTail());
		betaArray.append(multiDBUtility.floatArrayTail());
		StringBuilder probability = new StringBuilder("alpine_miner_lr_ca_pi(");
		probability.append(betaArray).append(",").append(columnNamesArray).append(",");
		addIntercept(probability);
		probability.append(")");
		return probability;
	}
	protected void addIntercept(StringBuilder probability) {
		if (interceptAdded)
		{
			probability.append("true");
		}
		else
		{
			probability.append("false");
		}
	}
	
	protected StringBuffer getWhere(DataSet dataSet) {
		StringBuffer where = new StringBuffer(" where ");
		boolean first = true;
		for (Column column : dataSet.getColumns()) {
			if (column.isNominal())
			{
				continue;
			}
			if (!first)
			{
				where.append(" and ");
			}
			else
			{
				first = false;
			}
			String columnName = StringHandler.doubleQ(column.getName());
			where.append(columnName).append(" is not null ");
		}
		if (first)
		{
			return new StringBuffer("");
		}
		else
		{
			return where;
		}
	}
	protected StringBuilder getProbabilitySql(DataSet dataSet) {
		ISqlGeneratorMultiDB sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(dataSourceInfo.getDBType());

		StringBuilder gx = new StringBuilder("(");	
		
		HashMap<String, Double> betaMap = getBetaMap();
		int i = 0; 
		for(Column column : dataSet.getColumns())
		{
			 String columnName = StringHandler.doubleQ(column.getName());
			 if(column.isNumerical())
			 {
			 		if(betaMap.get(column.getName())==null)continue;
					double beta=betaMap.get(column.getName());
					if (i == 0)
					{
						gx.append("(").append(sqlGeneratorMultiDB.castToDouble(String.valueOf(beta))).append(")*").append(sqlGeneratorMultiDB.castToDouble(columnName));
					}else
					{
						gx.append("+(").append(sqlGeneratorMultiDB.castToDouble(String.valueOf(beta))).append(")*").append(sqlGeneratorMultiDB.castToDouble(columnName));
					}
					i++;
			 }else
			 {
				 List<String> mapList=column.getMapping().getValues();
	    		HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    		TransformMap_valueKey= getAllTransformMap_valueKey().get(column.getName());
	    		if(TransformMap_valueKey==null)continue;
	    		Iterator<String> mapList_i=mapList.iterator();
	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(betaMap.get(columnname)==null)continue;
 	 					double beta=betaMap.get(columnname);
 	 					value=StringHandler.escQ(value);
 	 					if(i == 0)
 	 					{
 	 						gx.append("(").append(sqlGeneratorMultiDB.castToDouble(String.valueOf(beta))).append(")*").append(sqlGeneratorMultiDB.castToDouble(" (case"+" when "+columnName+"="+"'"+value+"' then 1.0 else 0.0 end)"));
 	 					}else
 	 					{
 	 						gx.append("+(").append(sqlGeneratorMultiDB.castToDouble(String.valueOf(beta))).append(")*").append(sqlGeneratorMultiDB.castToDouble(" (case"+" when "+columnName+"="+"'"+value+"' then 1.0 else 0.0 end)"));
 	 					}
 	 					i++;
 	 				}
			 }
		}
		
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(betaMap.get(key)!= null){
				gx.append("+").append(sqlGeneratorMultiDB.castToDouble(String.valueOf(betaMap.get(key)))).append("*(").append(sqlGeneratorMultiDB.castToDouble(value)).append(")");
			}
		}
        if (interceptAdded) {
        	gx.append("+(").append(sqlGeneratorMultiDB.castToDouble(String.valueOf(betaMap.get(interceptString)))).append(")");
        }
		gx.append(")");
		StringBuffer gxSim = new StringBuffer();
		gxSim.append("(case when ").append(gx).append(" > 30 then 30 when  ").append(gx).append(" < -30 then -30 else ").append(gx).append(" end)");
		StringBuilder probability=new StringBuilder("");

		probability.append("( ").append(sqlGeneratorMultiDB.castToDouble("1.0")).append("  /(  ").append(sqlGeneratorMultiDB.castToDouble(" 1.0 ")).append(" +exp(-(").append(gxSim).append("))))");
		return probability;
	}
	protected HashMap<String, Double> getBetaMap() {
		HashMap<String,Double> betaMap=new HashMap<String,Double>();
		Iterator<String> columnNamesArray_ii=columnNamesArray.iterator();
		int count=0;
		while(columnNamesArray_ii.hasNext())
			{
				String columnName=columnNamesArray_ii.next();
				if (Double.isNaN(beta[count]))
				{
					betaMap.put(columnName, 0.0);
				}
				else
				{
					betaMap.put(columnName, beta[count]);
				}
				count++;
			}
		if (interceptAdded)
		{
			if (Double.isNaN(beta[beta.length - 1]))
			{
				betaMap.put(interceptString, 0.0);
			}
			else
			{
				betaMap.put(interceptString, beta[beta.length - 1]);
			}
		}
		return betaMap;
	}

    public String toString() {
    	StringBuffer result = new StringBuffer();
    	if (!improvementStop)
    	{
    		result.append(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.ALGORITHM_DID_NOT_CONVERGE, AlpineThreadLocal.getLocale())).append(Tools.getLineSeparators(2));
    	}

    	result.append(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.ITERATION, AlpineThreadLocal.getLocale())+": "+ getIteration()+Tools.getLineSeparators(2));
    	
    	if (interceptAdded) {
    		result.append("Bias (offset): " + beta[beta.length - 1]);
    		result.append("  \t(SE: " + standardError[standardError.length - 1]);
    		result.append(", z-value: " + zValue[standardError.length - 1]);
    		result.append(", p-value: " + pValue[standardError.length - 1]);
    		result.append(", Wald: " + waldStatistic[waldStatistic.length - 1]+ ")" + Tools.getLineSeparators(2));
    	}
    	result.append("Coefficients:" + Tools.getLineSeparator());
		for (int j = 0; j < beta.length - 1; j++) {
			result.append("beta(" + columnNames[j] + ") = " + beta[j]);
			result.append(" \t\t(SE: " + standardError[j]);
			result.append(", z-value: " + zValue[j]);
			result.append(", p-value: " + pValue[j]);
			result.append(", Wald: " + waldStatistic[j] + ")" + Tools.getLineSeparator());
		}
    	result.append(Tools.getLineSeparator() + "Odds Ratios:" + Tools.getLineSeparator());
		for (int j = 0; j < beta.length - 1; j++) {
			result.append("odds_ratio(" + columnNames[j] + ") = " + Math.exp(beta[j]) + Tools.getLineSeparator());
		}
    	result.append(Tools.getLineSeparator() + "Deviance: " + modelDeviance + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + "nullDeviance: " + nullDeviance + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + "chiSquare: " + chiSquare + Tools.getLineSeparator());

    	return result.toString();
    }
 
	public double[] getBeta()
	{
		return beta;
	}


	public double[] getOddsArrays()
	{
		double[] dou=new double[beta.length-1];
		for(int i=0;i<beta.length - 1;i++)
		{
			dou[i] = Math.exp(beta[i]);
		}
		return dou;
	}

	public boolean isImprovementStop() {
		return improvementStop;
	}
	public void setImprovementStop(boolean improvementStop) {
		this.improvementStop = improvementStop;
	}
    public String[] getColumnNames() {
		return columnNames;
	}
    
    public double[] getStandardError() {
		return standardError;
	}
	public double[] getWaldStatistic() {
		return waldStatistic;
	}
	public double[] getzValue() {
		return zValue;
	}
	public double[] getpValue() {
		return pValue;
	}

	@Override
	public void setBeta(double[] beta) {
		this.beta=beta;
		
	}

	@Override
	public void setStandardError(double[] standardError) {
		this.standardError=standardError;
		
	}

	@Override
	public void setWaldStatistic(double[] waldStatistic) {
		this.waldStatistic=waldStatistic;
		
	}

	@Override
	public void setzValue(double[] zValue) {
		this.zValue=zValue;
		
	}

	@Override
	public void setpValue(double[] pValue) {
		this.pValue=pValue;
		
	}

	@Override
	public void setColumnNames(String[] columnNames) {
		this.columnNames=columnNames;
		
	}
}
	
