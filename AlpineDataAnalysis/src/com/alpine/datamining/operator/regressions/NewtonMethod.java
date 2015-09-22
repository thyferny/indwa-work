/**
 * ClassName NewtonMethod.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.regressions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import com.alpine.datamining.MinerInit;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.Mapping;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.utility.ColumnTypeInteractionTransformer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
/**
 * This algorithm to determine a logistic regression model.
 * @author Eason Yu,Jeff Dong
 */
public abstract class NewtonMethod {
    private static final Logger logger = Logger.getLogger(NewtonMethod.class);

    protected int  maxGenerations;
	protected int fetchSize = 50000;
	protected Column label;
	protected DataSet dataSet;
	protected DataSet oldDataSet;
	protected String tableName = null;
	protected Statement st = null;
	protected DatabaseConnection databaseConnection = null;
	protected ResultSet rs = null;
	protected String goodValue = null;
    protected boolean addIntercept = true;
    protected boolean useCFunction = false;
    protected boolean SEUseNewBeta = true;
    protected double[] oldBeta = null;
    protected double[] oldOldBeta = null;
    private double[] currentBeta = null;
    private double[] bestBeta = null;
    protected int bestIterate = 0;
    private double bestFitness =0;
	protected double oldFitness = 0;
	protected double oldOldFitness = 0;
    protected double currentFitness = 0;
    protected double epsilon;
    protected double diff = Double.POSITIVE_INFINITY;
    //flag indicate whether sql return value is Nan
    protected boolean returnNan = false;

	protected boolean matrixInverseException = false;
	ArrayList<Double> derivative = new ArrayList<Double>();

	protected long iterationCycle = 0;

	protected IMultiDBUtility multiDBUtility = null;
	
	protected double deviance;
	protected double nullDeviance;
	protected double chiSquare;
    
    public NewtonMethod() {
        if (AlpineDataAnalysisConfig.SE_USE_NEW_BETA.equalsIgnoreCase("true"))
        {
        	SEUseNewBeta = true;
        }
        else
        {
        	SEUseNewBeta = false;
        }
    }

    public Model learn(DataSet dataSet,
    		LogisticRegressionParameter para
) throws OperatorException {
		String goodValue = para.getGoodValue();
		boolean  addIntercept = para.isAddInercept();
		double  epsilon = para.getEpsilon();
		int  maxGenerations = para.getMaxGenerations();

		this.multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		this.maxGenerations = maxGenerations;
		ColumnTypeInteractionTransformer transformer=new ColumnTypeInteractionTransformer();
		ArrayList<String> columnNamesList = new ArrayList<String>();
		if (para.getColumnNames() != null && !StringUtil.isEmpty(para.getColumnNames().trim())){
			String[] columnNamesArray=para.getColumnNames().split(",");
			for(String s:columnNamesArray)
			{
				columnNamesList.add(s);
			}
		}
		transformer.setColumnNames(columnNamesList);
		transformer.setAnalysisInterActionModel(para.getAnalysisInterAtionModel());
        DataSet newDataSet=transformer.TransformCategoryToNumeric_new(dataSet,null);
		this.dataSet = newDataSet;
		this.oldDataSet=dataSet;
		this.label = newDataSet.getColumns().getLabel();
		if (label.getMapping().size() != 2) {
            logger.error(
                    AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.LR_DEPENDENT_2_VALUE, AlpineThreadLocal.getLocale()));
			throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.LR_DEPENDENT_2_VALUE, AlpineThreadLocal.getLocale()));
		}
		this.tableName = ((DBTable) newDataSet.getDBTable())
				.getTableName();

		this.databaseConnection = ((DBTable) newDataSet
				.getDBTable()).getDatabaseConnection();
		try {
			this.st = databaseConnection.createStatement(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}
    	this.goodValue = goodValue;
    	this.addIntercept = addIntercept;
    	this.epsilon = epsilon;
    	this.useCFunction = MinerInit.isUseCFunction();
    	
		int columnCount = this.dataSet.getColumns().size();
		if (addIntercept)
		{
			currentBeta = new double[columnCount+1];
			oldBeta = new double[columnCount+1];
			oldOldBeta = new double[columnCount+1];
			bestBeta = new double[columnCount+1];
		}
		else
		{
			currentBeta = new double[columnCount];
			oldBeta = new double[columnCount];
			oldOldBeta = new double[columnCount];
			bestBeta = new double[columnCount];
		}   	
        try {
			LogisticRegressionModelDB model = train();
			if(!newDataSet.equals(dataSet))
			{
				model.setAllTransformMap_valueKey(transformer.getAllTransformMap_valueKey());
			}
			getPerformance();
			model.setModelDeviance(deviance);
			model.setNullDeviance(nullDeviance);
			model.setChiSquare(chiSquare);
			model.setIteration(iterationCycle);
			model.setInteractionColumnExpMap(transformer.getInteractionColumnExpMap());
			model.setInteractionColumnColumnMap(transformer.getInteractionColumnColumnMap());
			
			if(transformer.isTransform())
			{
				dropTable();
			}
			st.close();
			return model;
		} catch (Throwable e) {
			e.printStackTrace();
            logger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
    }

	protected void dropTable() throws OperatorException {
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		try {
            logger.debug("NewtonMethod.dropTable():sql="
                    + truncate.toString());
			st.execute(truncate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
            logger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
            logger.debug("NewtonMethod.dropTable():sql="
                    + dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
            logger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
    private LogisticRegressionModelDB train() throws OperatorException
    {

    	int reRunConfig = Integer.parseInt(AlpineDataAnalysisConfig.LR_RERUN);
    	int reRun = reRunConfig;
    	int i = 0;
    	boolean first = true;
    	for( i = 0; i < (maxGenerations) && diff > epsilon && !matrixInverseException && !returnNan; i++)
    	{
    		iterate(first);
    		if(first || currentFitness >= bestFitness){
    			bestIterate = i;
    			bestFitness = currentFitness;
    			for(int index = 0; index < bestBeta.length; index++){
    				bestBeta[index] = currentBeta[index];
    			}
    			reRun = reRunConfig;
    		}else{
    			if(reRun > 0){
    				i--;
    				reRun--;
    				currentFitness = oldFitness;
    				oldFitness = oldOldFitness;
    				for(int j = 0; j < oldBeta.length; j++){
    					currentBeta[j] = oldBeta[j];
    					oldBeta[j] = oldOldBeta[j];
    				}
    			}else{
    				reRun = reRunConfig;
    			}
    		}
            logger.debug("iteration:" + i + "reRun:" + reRun + " diff:" + diff + "oldfitness:" + oldFitness + "fitness:" + currentFitness + "matrixInverseException:" + matrixInverseException + " returnNan:" + returnNan + " lastfitness is best:" + (currentFitness == bestFitness) + " best iterate" + bestIterate);
        	if(first)
    		{
    			first = false;
    		}
    	}
    	iterationCycle = i;
    	boolean improvementStop = false;
    	if (diff > epsilon)
    	{
    		improvementStop = false;
    	}
    	else
    	{
    		improvementStop = true;
    	}
    	double[] variance = getVariance();

    	LogisticRegressionModelDB model = null;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			model = new LogisticRegressionModelDBOracle(this.dataSet,this.oldDataSet,getBestBeta(), variance,  addIntercept, goodValue);
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			model = new LogisticRegressionModelDB2(this.dataSet,this.oldDataSet,getBestBeta(), variance,  addIntercept, goodValue);
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			model = new LogisticRegressionModelNetezza(this.dataSet,this.oldDataSet,getBestBeta(), variance,  addIntercept, goodValue);
		}else{
			model = new LogisticRegressionModelDB(this.dataSet,this.oldDataSet,getBestBeta(), variance,  addIntercept, goodValue);
		}
    	model.setImprovementStop(improvementStop);
    	return model;
    }

	abstract protected double[] getVariance() throws OperatorException ;

	protected void iterate(boolean first) throws OperatorException {
    	double[] beta = getCurrentBeta();
    	StringBuffer columnArray = getColumnNamesArray();
    	StringBuffer where = getWhere();
    	double fitness = 0;
    	Matrix hessian = new Matrix(beta.length, beta.length);

    	//Generate parameters for SQL.
        String weightString = getWeightString();
        StringBuffer betaArray = getBetaArray(beta);
        StringBuffer sql = null;
        sql = getSqlFunction(first);
        sql.append(betaArray).append(",").append(columnArray).append(",");
        addIntercept(sql);
        sql.append(",").append(weightString).append(",").append(getLabelValue());
        if (first)
        {
        	sql.append(",").append("0");
        }
        sql.append("))");
        sql.append(" from ").append(tableName).append(where);

		try {
            logger.debug("NewtonMethod.iterate():sql=" + sql);
			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				Double[] b = getHeDev();
				if (returnNan)
				{
					return;
				}
				int index = 0;
				for (int x = 0; x < beta.length; x++)
				{
					for (int y = x; y < beta.length; y++)
					{
						double h = b[index];
				    	hessian.set(x, y, h);
				    	hessian.set(y, x, h);
				    	index++;
					}
				}
				derivative.clear();
				for(int i = 0; i < beta.length; i++)
				{
					derivative.add(b[index]);
					index++;
				}
				fitness = b[index];
			}
			rs.close();
		} catch (Throwable e) {
			
			e.printStackTrace();
            logger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
    	Matrix varianceCovarianceMatrix = null;
    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    	} catch (Exception e) {
    		matrixInverseException = true;
    		return;
    	}
    	
    	diff = 0;
    	double[] delta = new double[beta.length];
		for (int i = 0; i < beta.length; i++)
		{
			oldOldBeta[i] = oldBeta[i];
			oldBeta[i] = beta[i];
			delta[i] = 0;
			for(int j = 0; j < beta.length; j++)
			{
				delta[i] += varianceCovarianceMatrix.get(i, j) * derivative.get(j);
			}
			if (first)
			{
				beta[i] = -delta[i];
			}
			else
			{
				beta[i] = beta[i] - delta[i];
			}
		}
		oldOldFitness = oldFitness;
		oldFitness = fitness;
		currentFitness = getFitness(beta);
		diff = Math.abs(2*currentFitness - 2*oldFitness)/(0.1 + Math.abs(2*currentFitness));
    }

	abstract protected Double[] getHeDev() throws SQLException ;

	abstract protected void addIntercept(StringBuffer sql);

	abstract protected StringBuffer getSqlFunction(boolean first) ;

	protected double[] getVariance(int betaLength, Matrix hessian) {
		double[] variance = new double[betaLength];
    	Matrix varianceCovarianceMatrix = null;
    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    	} catch (Exception e) {
    		e.printStackTrace();
    		for (int j = 0; j < betaLength; j++) {
    			variance[j] = Double.NaN;
    		}
    		return variance;
    	}
    	for (int j = 0; j < betaLength; j++) {
   			variance[j] = Math.abs(varianceCovarianceMatrix.get(j, j));
    	}
    	return variance;
	}

    public void getPerformance() throws OperatorException {
    	double logLikelihood = 0;
    	logLikelihood = getBestFitness();
    	double restrictedLogLikelihood = 0.0d;

    	double weightSum = 0.0d;
    	double positiveSum = 0.0d;

    	StringBuilder sqlSum =new StringBuilder("select sum(");
    	
        String labelValue = getLabelValue();
        String weightString = getWeightString();

        sqlSum.append(weightString).append("*(").append(labelValue).append(")), ");
        sqlSum.append("sum(").append(weightString).append(")");
        sqlSum.append(" from ").append(tableName).append(getWhere());
        
		try {
            logger.debug("NewtonMethod.getPerformance():sql=" + sqlSum);
			rs = st.executeQuery(sqlSum.toString());
			
			while (rs.next()) {
				positiveSum = rs.getDouble(1);
				weightSum = rs.getDouble(2);
			}
			rs.close();
	
		} catch (SQLException e) {
			e.printStackTrace();
            logger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
    	double pi0 = positiveSum / weightSum;
    	restrictedLogLikelihood = weightSum * (pi0 * Math.log(pi0) + (1 - pi0) * Math.log(1 - pi0));
    	deviance = -2 * logLikelihood;
    	nullDeviance = -2*restrictedLogLikelihood;
    	chiSquare = nullDeviance - deviance;
    }
    protected String getLabelValue()
    {
        Mapping mapping = label.getMapping();
        String labelName = StringHandler.doubleQ(label.getName());
        StringBuilder sb= new StringBuilder("(case ");
        for (int i = 0; i < mapping.size(); i++)
        {
        	String value = mapping.mapIndex(i);
        	int valueNumber = 0;
        	if (value.equals(goodValue))
        	{
        		valueNumber = 1;
        	}
        	value=StringHandler.escQ(value);
        	sb.append("when ").append(labelName).append("='").append(value).append("' then ").append(valueNumber).append(" ");
        }
        sb.append(" else 0 end)");
        return sb.toString();
    }

    protected String getWeightString()
    {
    	StringBuilder sb=new StringBuilder("");
       	sb.append("1.0");
        return sb.toString();
    }
	public double[] getCurrentBeta() {
		return currentBeta;
	}
    public double[] getBestBeta() {
		return bestBeta;
	}

	public double getBestFitness() {
		return bestFitness;
	}
	public double[] getStatsBeta()
	{
    	double[] beta = null;
    	if (SEUseNewBeta)
    	{
    		beta = getBestBeta();
    	}
    	else
    	{
    		beta = oldBeta;
    	}
    	return beta;
	}
	public double getCurrentFitness() throws OperatorException {
		return currentFitness;
	}

	abstract protected double getFitness(double [] beta) throws OperatorException ;

	protected StringBuffer getWhere() {
		StringBuffer where = new StringBuffer(" where ");
		Iterator<Column> ii = dataSet.getColumns().iterator();
		boolean first = true;
		while (ii.hasNext()) {
			if (first)
			{
				first = false;
			}
			else
			{
				where.append(" and ");
			}
			where.append(StringHandler.doubleQ(ii.next().getName())).append(" is not null ");
		}
		where.append(" and ").append(StringHandler.doubleQ(label.getName())).append(" is not null ");
		return where;
	}
	/*GenerateSql for fitness */
	protected StringBuilder GenerateFitnessSql(double [] beta) {
		int columnCount = dataSet.getColumns().size();
		String[] columnName = new String[columnCount];

		Iterator<Column> ii = dataSet.getColumns().iterator();
		int j = 0;
		while (ii.hasNext()) {
			columnName[j] = ii.next().getName();
			j++;
		}
		String weightString = getWeightString();
		String labelname = StringHandler.doubleQ(label.getName());

		StringBuilder exp;
		exp = new StringBuilder("1.0/(1.0+exp(-(");
		boolean first = true;
		for (int i = 0; i < columnCount; i++) {
			if (first)
			{
				first = false;
			}
			else
			{
				exp.append("+");
			}
			exp.append(StringHandler.doubleQ(columnName[i])).append("*");
        	if (Double.isNaN(beta[i]))
        	{
        		exp.append("0.0");
        	}
        	else
        	{
        		exp.append(beta[i]);
        	}
		}
		if (addIntercept)
		{
			exp.append("+");
        	if (Double.isNaN(beta[beta.length - 1]))
        	{
        		exp.append("0.0");
        	}
        	else
        	{
        		exp.append(beta[beta.length - 1]);
        	}
		}
		exp.append(")))");

		StringBuilder sql = new StringBuilder("select ");
		StringBuilder sum = new StringBuilder("");
		sum.append("sum(").append(weightString).append("*(case when ").append(labelname).append("='").append(StringHandler.escQ(goodValue));// negativeValue);
		sum.append("' then ln(").append(exp).append(") else ln(1.0-").append(
				exp).append(") end))").append(" sum ");
		sql.append(sum).append(" from ").append(tableName);
		return sql;
	}
	
	protected StringBuilder generateFunctionFitnessSql(double [] beta) {
		StringBuffer columnNamesArray = getColumnNamesArray();
		StringBuffer betaArray = getBetaArray(beta);

		StringBuilder sql;
		sql = new StringBuilder("select ");
		StringBuilder sum = new StringBuilder("");
		
		StringBuffer addInterceptString = new StringBuffer();
        addIntercept(addInterceptString);

		sum.append("sum(alpine_miner_lr_ca_fitness(")
				.append(betaArray).append(",")
				.append(columnNamesArray).append(",")
				.append(addInterceptString).append(",")
				.append(getWeightString()).append(",")
				.append(getLabelValue())
				.append("))").append(" sum");

		sql.append(sum).append(" from ").append(tableName).append(getWhere());
		return sql;
	}
	protected StringBuffer getBetaArray(double[] beta)
	{
        StringBuffer betaArray = new StringBuffer(multiDBUtility.floatArrayHead());
        boolean first = true;
        for (int i = 0; i < beta.length; i++)
        {
        	if (first)
        	{
        		first = false;
        	}
        	else
        	{
        		betaArray.append(",");
        	}
        	if (Double.isNaN(beta[i]))
        	{
        		betaArray.append("0.0");
        	}
        	else
        	{
        		betaArray.append(beta[i]);
        	}
        }
        betaArray.append(multiDBUtility.floatArrayTail());
        return betaArray;
	}
	protected StringBuffer getColumnNamesArray() {
		StringBuffer columnNamesArray = new StringBuffer(multiDBUtility.floatArrayHead());
		Iterator<Column> ii = dataSet.getColumns().iterator();
		int i = 0;
		boolean first = true;
		while (ii.hasNext()) {
			if (first){
				first = false;
			}else{
				columnNamesArray.append(",");
			}
			columnNamesArray.append(StringHandler.doubleQ(ii.next().getName()));
			i++;
		}
		columnNamesArray.append(multiDBUtility.floatArrayTail());
		return columnNamesArray;
	}
}
