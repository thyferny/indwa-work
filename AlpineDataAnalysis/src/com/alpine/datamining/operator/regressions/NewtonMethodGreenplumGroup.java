package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class NewtonMethodGreenplumGroup extends NewtonMethodGreenplum {
    private static final Logger itsLogger = Logger.getLogger(NewtonMethodGreenplumGroup.class);
	protected Map<String, double[]> oldBeta = new HashMap<String, double[]>();
	protected Map<String, double[]> oldOldBeta = new HashMap<String, double[]>();
	private Map<String, double[]> currentBeta = new HashMap<String, double[]>();
	private Map<String, double[]> bestBeta = new HashMap<String, double[]>();
	private Map<String, Double> bestFitness = new HashMap<String, Double>();
	protected Map<String, Double> oldFitness = new HashMap<String, Double>();
	protected Map<String, Double> oldOldFitness = new HashMap<String, Double>();
	protected Map<String, Double> currentFitness = new HashMap<String, Double>();
	protected ArrayList<String> columnNamesList = new ArrayList<String>();
	protected ArrayList<String> groupByList = new ArrayList<String>();
	protected ArrayList<String> tempGroupByList = new ArrayList<String>();
	protected Map<String,Double> allModelDiff = new HashMap<String,Double>();
	protected Map<String,ArrayList<Double>> derivatives = new HashMap<String,ArrayList<Double>>();
	protected Map<String,Integer> endIter =new HashMap<String,Integer>();
	protected ArrayList<String> removeList=new ArrayList<String>();
	protected String classLogInfo=NewtonMethodGreenplumGroup.class.getCanonicalName();
	
	private String groupByColumn;

	public NewtonMethodGreenplumGroup() {
		if (AlpineDataAnalysisConfig.SE_USE_NEW_BETA.equalsIgnoreCase("true")) {
			SEUseNewBeta = true;
		} else {
			SEUseNewBeta = false;
		}
	}

	public Model learn(DataSet dataSet, LogisticRegressionParameter para)
			throws OperatorException {
		String goodValue = para.getGoodValue();
		boolean addIntercept = para.isAddInercept();
		double epsilon = para.getEpsilon();
		int maxGenerations = para.getMaxGenerations();

		this.multiDBUtility = MultiDBUtilityFactory
				.createConnectionInfo(((DBTable) dataSet.getDBTable())
						.getDatabaseConnection().getProperties().getName());
		this.maxGenerations = maxGenerations;
		ColumnTypeInteractionTransformer transformer = new ColumnTypeInteractionTransformer();

		if (para.getColumnNames() != null
				&& !StringUtil.isEmpty(para.getColumnNames().trim())) {
			String[] columnNamesArray = para.getColumnNames().split(",");
			for (String s : columnNamesArray) {
				columnNamesList.add(s);
			}
		}
//		if(!columnNamesList.contains(para.getGroupByColumn()))
//		{
//			columnNamesList.add(para.getGroupByColumn());
//		}
		
		transformer.setColumnNames(columnNamesList);
		transformer.setAnalysisInterActionModel(para
				.getAnalysisInterAtionModel());
		DataSet newDataSet = transformer
				.TransformCategoryToNumeric_new(dataSet, para.getGroupByColumn());
		this.dataSet = newDataSet;
		this.oldDataSet = dataSet;
		this.label = newDataSet.getColumns().getLabel();
		if (label.getMapping().size() != 2) {
            itsLogger.error(
							AlpineDataAnalysisLanguagePack
									.getMessage(
											AlpineDataAnalysisLanguagePack.LR_DEPENDENT_2_VALUE,
											AlpineThreadLocal.getLocale()));
			throw new OperatorException(
					AlpineDataAnalysisLanguagePack
							.getMessage(
									AlpineDataAnalysisLanguagePack.LR_DEPENDENT_2_VALUE,
									AlpineThreadLocal.getLocale()));
		}
		this.tableName = ((DBTable) newDataSet.getDBTable()).getTableName();

		this.databaseConnection = ((DBTable) newDataSet.getDBTable())
				.getDatabaseConnection();
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
		this.groupByColumn = para.getGroupByColumn();
		try {
			getGroupByList();
		} catch (SQLException e2) {
			throw new OperatorException(e2.toString());
		}
		if (addIntercept) {
			for (String s : tempGroupByList) {
				currentBeta.put(s, new double[columnCount + 1]);
				oldBeta.put(s, new double[columnCount + 1]);
				oldOldBeta.put(s, new double[columnCount + 1]);
				bestBeta.put(s, new double[columnCount + 1]);
			}
		} else {
			for (String s : tempGroupByList) {
				currentBeta.put(s, new double[columnCount]);
				oldBeta.put(s, new double[columnCount]);
				oldOldBeta.put(s, new double[columnCount]);
				bestBeta.put(s, new double[columnCount]);
			}
		}
		try {
			LogisticRegressionModelDB model = train();
			if (!newDataSet.equals(dataSet)) {
				model.setAllTransformMap_valueKey(transformer
						.getAllTransformMap_valueKey());
			}
			updatePerformance((LogisticRegressionGroupModel)model);
//			model.setModelDeviance(deviance);
//			model.setNullDeviance(nullDeviance);
//			model.setChiSquare(chiSquare);
			model.setIteration(iterationCycle);
			model.setInteractionColumnExpMap(transformer
					.getInteractionColumnExpMap());
			model.setInteractionColumnColumnMap(transformer
					.getInteractionColumnColumnMap());

			if (transformer.isTransform()) {
				dropTable();
			}
			st.close();
			return model;
		} catch (Throwable e) {
			e.printStackTrace();
            itsLogger.error(classLogInfo+e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private void getGroupByList() throws SQLException {
		StringBuffer getGroupByList = new StringBuffer();
		getGroupByList.append(" select distinct(").append(StringHandler.doubleQ(this.groupByColumn))
				.append(") from ").append(tableName).append(this.getWhere());
		rs = st.executeQuery(getGroupByList.toString());
		
		while (rs.next()) {
//			if(rs.getString(1)==null)
//			{
//				continue;
//			}
//			else{
			groupByList.add(rs.getString(1));
			tempGroupByList.add(rs.getString(1));
//			}
		}
	}

	protected void dropTable() throws OperatorException {
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		try {
			itsLogger.debug(
					"NewtonMethod.dropTable():sql=" + truncate.toString());
			st.execute(truncate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(classLogInfo+e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
			itsLogger.debug(
					classLogInfo+"dropTable():sql=" + dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private LogisticRegressionModelDB train() throws OperatorException {

		int reRunConfig = Integer.parseInt(AlpineDataAnalysisConfig.LR_RERUN);
		int reRun = reRunConfig;
		int i = 0;
		boolean first = true;
		for (i = 0; i < (maxGenerations) && diff > epsilon
				&& !matrixInverseException && !returnNan&&tempGroupByList.size()>0; i++) {
			tempGroupByList.removeAll(removeList);
			if(tempGroupByList.isEmpty())
			{
				break;
			}
			iterate(first,i);
			for (String s : tempGroupByList) {
				if (first || currentFitness.get(s) >= bestFitness.get(s)) {
					bestIterate = i;
					bestFitness = currentFitness;
					for (int index = 0; index < bestBeta.get(s).length; index++) {
						bestBeta.get(s)[index] = currentBeta.get(s)[index];
					}
					reRun = reRunConfig;
				} else {
					if (reRun > 0) {
						i--;
						reRun--;
						currentFitness = oldFitness;
						oldFitness = oldOldFitness;
						for (int j = 0; j < oldBeta.get(s).length; j++) {
							currentBeta.get(s)[j] = oldBeta.get(s)[j];
							oldBeta.get(s)[j] = oldOldBeta.get(s)[j];
						}
					} else {
						reRun = reRunConfig;
					}
				}
			}
			itsLogger.debug(classLogInfo+
					"iteration:" + i + "reRun:" + reRun + " diff:" + diff
							+ "oldfitness:" + oldFitness + "fitness:"
							+ currentFitness + "matrixInverseException:"
							+ matrixInverseException + " returnNan:"
							+ returnNan + " lastfitness is best:"
							+ (currentFitness == bestFitness) + " best iterate"
							+ bestIterate);
			if (first) {
				first = false;
			}
			
		}
		iterationCycle = i;
		boolean improvementStop = false;
		if (diff > epsilon) {
			improvementStop = false;
		} else {
			improvementStop = true;
		}
		Map<String, double[]> variance = getVarianceGroup();
 
		LogisticRegressionGroupModel resultModel=new LogisticRegressionGroupModel(dataSet, oldDataSet, getBestBeta(groupByList.get(0)), getBestBeta(groupByList.get(0)), addIntercept, goodValue);;
		
		resultModel.setGroupByColumn(StringHandler.doubleQ(groupByColumn));
		for (String s : groupByList) {
			LogisticRegressionModelDB model = null;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection()
				.getProperties().getName().equalsIgnoreCase(
						DataSourceInfoOracle.dBType)) {
			model = new LogisticRegressionModelDBOracle(this.dataSet,
					this.oldDataSet, getBestBeta(s), variance.get(s), addIntercept,
					goodValue);
		} else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection()
				.getProperties().getName().equalsIgnoreCase(
						DataSourceInfoDB2.dBType)) {
			model = new LogisticRegressionModelDB2(this.dataSet,
					this.oldDataSet, getBestBeta(s), variance.get(s), addIntercept,
					goodValue);
		} else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection()
				.getProperties().getName().equalsIgnoreCase(
						DataSourceInfoNZ.dBType)) {
			model = new LogisticRegressionModelNetezza(this.dataSet,
					this.oldDataSet, getBestBeta(s), variance.get(s), addIntercept,
					goodValue);
		} else {
			model = new LogisticRegressionModelDB(this.dataSet,
					this.oldDataSet, getBestBeta(s), variance.get(s), addIntercept,
					goodValue);
		}
		if(endIter.get(s)!=null)
		{
			model.setIteration(endIter.get(s));
		}else
		{
			model.setIteration(maxGenerations);
		}
		if(allModelDiff.get(s)!=null&&allModelDiff.get(s)<= epsilon)
		{
			model.setImprovementStop(true); 
		} else {
			model.setImprovementStop(false);  
		}
		
		
		resultModel.addOneModel(model, s);
		}
		resultModel.setImprovementStop(improvementStop);
		return resultModel;
	}

	protected void iterate(boolean first,int currentIter) throws OperatorException {
		Map<String, double[]> beta = getGroupCurrentBeta();
		StringBuffer columnArray = getColumnNamesArray();
		StringBuffer where = getWhere();
		Map<String,Double> fitness = new HashMap<String,Double>();
		Map<String, Matrix> hessianGroup = new HashMap<String, Matrix>();

		// Generate parameters for SQL.
		String weightString = getWeightString();
		StringBuffer betaArray = getBetaArray(beta);
		StringBuffer sql = null;
		sql = getSqlFunction(first);
		sql.append(betaArray).append(",").append(columnArray).append(",");
		addIntercept(sql);
		sql.append(",").append(weightString).append(",")
				.append(getLabelValue());
		if (first) {
			sql.append(",").append("0");
		}
		sql.append("))");
		sql.append(" , ").append(StringHandler.doubleQ(groupByColumn)).append(" from ").append(
				tableName).append(where).append(" group by ").append(
						StringHandler.doubleQ(groupByColumn));

		try {
			itsLogger.debug(
					classLogInfo+"iterate():sql=" + sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {

				Double[] b = getHeDev();
				String s = rs.getString(2);
				Matrix hessian = new Matrix(beta.get(s).length,
						beta.get(s).length);
				if (returnNan) {
					return;
				}
				int index = 0;
				for (int x = 0; x < beta.get(s).length; x++) {
					for (int y = x; y < beta.get(s).length; y++) {
						double h = b[index];
						hessian.set(x, y, h);
						hessian.set(y, x, h);
						index++;
					}
				}
				
			 
				ArrayList<Double> tempderivative = new ArrayList<Double>();
				for (int i = 0; i < beta.get(s).length; i++) {
					tempderivative.add(b[index]);
					index++;
				}
				derivatives.put(s, tempderivative);
				fitness.put(s,b[index]);
				hessianGroup.put(s, hessian);
			}
			rs.close();
		} catch (Throwable e) {

			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

		for (String s : tempGroupByList) {

			Matrix varianceCovarianceMatrix = null;
			try {
				varianceCovarianceMatrix = hessianGroup.get(s).SVDInverse();
			} catch (Exception e) {
				matrixInverseException = true;
				return;
			}

			
			double[] delta = new double[beta.get(s).length];
			for (int i = 0; i < beta.get(s).length; i++) {
				oldOldBeta.get(s)[i] = oldBeta.get(s)[i];
				oldBeta.get(s)[i] = beta.get(s)[i];
				delta[i] = 0;
				for (int j = 0; j < beta.get(s).length; j++) {
					delta[i] += varianceCovarianceMatrix.get(i, j)
							* derivatives.get(s).get(j);
				}
				if (first) {
					beta.get(s)[i] = -delta[i];
				} else {
					beta.get(s)[i] = beta.get(s)[i] - delta[i];
				}
			}
			if(oldFitness.get(s)!=null)
			{
				oldOldFitness.put(s, oldFitness.get(s));
			}else{
				oldOldFitness.put(s, 0.0);
			};
			if(fitness.get(s)!=null)
			{
				oldFitness.put(s, fitness.get(s));
			}else{
				oldFitness.put(s, 0.0);
			};
			
		}
		diff = 0;
		currentFitness=getGroupFitness(beta);
		double largestDiff=0;
		removeList.clear();
		for (String s : tempGroupByList) {
			
		double tempdiff = Math.abs(2 * currentFitness.get(s) - 2 * oldFitness.get(s))
				/ (0.1 + Math.abs(2 * currentFitness.get(s)));
		allModelDiff.put(s, tempdiff);
		
		if(tempdiff>largestDiff)
			{
				largestDiff=tempdiff;
			}
			if(tempdiff<epsilon )
			{
				removeList.add(s);
				if(endIter.get(s)==null)
				{
					endIter.put(s, currentIter+1);
				}
			}
		}
		
		
		diff=largestDiff;
	}

	private Map<String, Double> getGroupFitness(Map<String, double[]> beta) throws OperatorException {
		StringBuilder sql = null;
		if (useCFunction)
		{
			sql = generateFunctionFitnessGroupSql(beta);
		}
		else
		{
			sql = GenerateFitnessSqlGroup(beta);
		}
		double fitness = 0;
		Map<String, Double>  fitnessResult=new HashMap<String,Double>();
		String s=new String();
		try {
			itsLogger.debug(classLogInfo+"getFitness():sql="+sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				fitness = rs.getDouble(1);
				s=rs.getString(2);
				fitnessResult.put(s, fitness);
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getSQLState().equals("2201E")||e.getSQLState().equals("22003")) {
				fitness = Double.NEGATIVE_INFINITY;
				return fitnessResult;
			}
			throw new OperatorException(e.getLocalizedMessage());
		}
		return fitnessResult;
	}

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



	public void updatePerformance(LogisticRegressionGroupModel model) throws OperatorException {
		
		double restrictedLogLikelihood = 0.0d;

		double weightSum = 0.0d;
		double positiveSum = 0.0d;

		StringBuilder sqlSum = new StringBuilder("select sum(");

		String labelValue = getLabelValue();
		String weightString = getWeightString();

		sqlSum.append(weightString).append("*(").append(labelValue).append(
				")), ");
		sqlSum.append("sum(").append(weightString).append("),").append(StringHandler.doubleQ(groupByColumn));
		sqlSum.append(" from ").append(tableName).append(getWhere()).append(" group by ").append(StringHandler.doubleQ(groupByColumn));

		try {
			itsLogger.debug(
					classLogInfo+"getPerformance():sql=" + sqlSum);
			rs = st.executeQuery(sqlSum.toString());

			while (rs.next()) {
				positiveSum = rs.getDouble(1);
				weightSum = rs.getDouble(2);
				String s=rs.getString(3);
				double logLikelihood = 0;
				logLikelihood = getBestFitnessGroup(s);
				double pi0 = positiveSum / weightSum;
				restrictedLogLikelihood = weightSum
						* (pi0 * Math.log(pi0) + (1 - pi0) * Math.log(1 - pi0));
				deviance = -2 * logLikelihood;
				nullDeviance = -2 * restrictedLogLikelihood;
				chiSquare = nullDeviance - deviance;
				model.getOneModel(s).setModelDeviance(deviance);
				model.getOneModel(s).setNullDeviance(nullDeviance);
				model.getOneModel(s).setChiSquare(chiSquare);
				}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		double pi0 = positiveSum / weightSum;
		restrictedLogLikelihood = weightSum
				* (pi0 * Math.log(pi0) + (1 - pi0) * Math.log(1 - pi0));
	 
	}

	public double getBestFitnessGroup(String s) {
		return bestFitness.get(s);
	}
	
	
	protected String getLabelValue() {
		Mapping mapping = label.getMapping();
		String labelName = StringHandler.doubleQ(label.getName());
		StringBuilder sb = new StringBuilder("(case ");
		for (int i = 0; i < mapping.size(); i++) {
			String value = mapping.mapIndex(i);
			int valueNumber = 0;
			if (value.equals(goodValue)) {
				valueNumber = 1;
			}
			value = StringHandler.escQ(value);
			sb.append("when ").append(labelName).append("='").append(value)
					.append("' then ").append(valueNumber).append(" ");
		}
		sb.append(" else 0 end)");
		return sb.toString();
	}

	protected String getWeightString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("1.0");
		return sb.toString();
	}

	public Map<String, double[]> getGroupCurrentBeta() {
		return currentBeta;
	}

	public Map<String, double[]> getGroupBestBeta() {
		return bestBeta;
	}

	public Map<String, Double> getGroupBestFitness() {
		return bestFitness;
	}

	public Map<String, double[]> getGroupStatsBeta() {
		Map<String, double[]> beta = null;
		if (SEUseNewBeta) {
			beta = getGroupBestBeta();
		} else {
			beta = oldBeta;
		}
		return beta;
	}

	public Map<String, Double> getGroupCurrentFitness()
			throws OperatorException {
		return this.currentFitness;
	}

	protected StringBuffer getWhere() {
		StringBuffer where = new StringBuffer(" where ");
		Iterator<Column> ii = dataSet.getColumns().iterator();
		boolean first = true;
		while (ii.hasNext()) {
			if (first) {
				first = false;
			} else {
				where.append(" and ");
			}
			where.append(StringHandler.doubleQ(ii.next().getName())).append(
					" is not null ");
		}
		where.append(" and ").append(StringHandler.doubleQ(label.getName()))
				.append(" is not null ");
		where.append(" and ").append(StringHandler.doubleQ(this.groupByColumn))
		.append(" is not null ");
		return where;
	}

	
	protected StringBuilder GenerateFitnessSqlGroup(Map<String,double[]> beta) {
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
			if (first) {
				first = false;
			} else {
				exp.append("+");
			}

		}

		exp.append(")))");

		StringBuilder sql = new StringBuilder("select ");
		StringBuilder sum = new StringBuilder("");
		sum.append("sum(").append(weightString).append("*(case when ").append(
				labelname).append("='").append(StringHandler.escQ(goodValue));// negativeValue);
		sum.append("' then ln(").append(exp).append(") else ln(1.0-").append(
				exp).append(") end))").append(" sum ");
		sql.append(sum).append(" from ").append(tableName);
		return sql;
	}

	protected StringBuilder generateFunctionFitnessGroupSql(Map<String,double[]> beta) {
		StringBuffer columnNamesArray = getColumnNamesArray();
		StringBuffer betaArray = getBetaArray(beta);

		StringBuilder sql;
		sql = new StringBuilder("select ");
		StringBuilder sum = new StringBuilder("");

		StringBuffer addInterceptString = new StringBuffer();
		addIntercept(addInterceptString);

		sum.append("sum(alpine_miner_lr_ca_fitness(").append(betaArray).append(
				",").append(columnNamesArray).append(",").append(
				addInterceptString).append(",").append(getWeightString())
				.append(",").append(getLabelValue()).append("))")
				.append(" sum");

		sql.append(sum).append(" , ").append(StringHandler.doubleQ(groupByColumn)).append(" from ").append(tableName).append(getWhere())
		.append(" group by ").append(StringHandler.doubleQ(groupByColumn));
		return sql;
	}

	protected StringBuffer getBetaArray(Map<String, double[]> beta) {
		StringBuffer caseWhenSql = new StringBuffer();
		caseWhenSql.append(" case ");
		for (String s : groupByList) {
			caseWhenSql.append(" when ").append(StringHandler.doubleQ(groupByColumn)).append("='")
					.append(s).append("' then ");
			StringBuffer betaArray = new StringBuffer(multiDBUtility
					.floatArrayHead());
			boolean first = true;
			for (int i = 0; i < beta.get(s).length; i++) {
				if (first) {
					first = false;
				} else {
					betaArray.append(",");
				}
				if (Double.isNaN(beta.get(s)[i])) {
					betaArray.append("0.0");
				} else {
					betaArray.append(beta.get(s)[i]);
				}
			}
			betaArray.append(multiDBUtility.floatArrayTail());
			caseWhenSql.append(betaArray);
		}
		caseWhenSql.append(" end ");
		return caseWhenSql;
	}

	protected StringBuffer getColumnNamesArray() {
		StringBuffer columnNamesArray = new StringBuffer(multiDBUtility
				.floatArrayHead());
		Iterator<Column> ii = dataSet.getColumns().iterator();
		int i = 0;
		boolean first = true;
		while (ii.hasNext()) {
			if (first) {
				first = false;
			} else {
				columnNamesArray.append(",");
			}
			columnNamesArray.append(StringHandler.doubleQ(ii.next().getName()));
			i++;
		}
		columnNamesArray.append(multiDBUtility.floatArrayTail());
		return columnNamesArray;
	}

	protected Map<String, double[]> getVarianceGroup() throws OperatorException {
		Map<String, double[]> variance = null;

		variance = estimateVarianceGroupFunction();
		return variance;
	}

	protected Map<String, double[]> estimateVarianceGroupFunction()
			throws OperatorException {
		itsLogger.info(classLogInfo+"Enter estimateVarianceFunction");
		Map<String, double[]> varianceGroup=new HashMap<String, double[]>();
		 
			
			StringBuffer columnArray = getColumnNamesArray();
			StringBuffer where = getWhere();
			// Generate parameters for SQL.
			String weightString = getWeightString();
			StringBuffer betaArray = getBetaArray(bestBeta);
			StringBuffer sql = new StringBuffer(
					"select (alpine_miner_lr_ca_he(");
			sql.append(betaArray).append(",").append(columnArray).append(",");
			addIntercept(sql);
			sql.append(",").append(weightString).append("))");
			sql.append(" , ").append(StringHandler.doubleQ(groupByColumn)).append(" from ").append(tableName).append(where);
			sql.append(" group by ").append(StringHandler.doubleQ(groupByColumn));
			try {
				itsLogger.debug(
						classLogInfo+"estimateVarianceFunction():sql="
								+ sql);
				rs = st.executeQuery(sql.toString());
				while (rs.next()) {
					
					String s=rs.getString(2);
					double[] beta = getStatsBeta(s);
					Matrix hessian = new Matrix(beta.length, beta.length);
					Number[] b = (Number[]) rs.getArray(1).getArray();
					int index = 0;
					for (int x = 0; x < beta.length; x++) {
						for (int y = x; y < beta.length; y++) {
							double h = 0.0;
							if (!Double.isNaN(b[index].doubleValue())) {
								h = b[index].doubleValue();
							}
							hessian.set(x, y, h);
							hessian.set(y, x, h);
							index++;
						}
					}
					varianceGroup.put(s, getVariance(beta.length, hessian));
				}
				
				
				
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		 
		return varianceGroup;
	}

	
	public double[] getStatsBeta(String s)
	{
    	double[] beta = null;
    	if (SEUseNewBeta)
    	{
    		beta = getBestBeta(s);
    	}
    	else
    	{
    		beta = oldBeta.get(s);
    	}
    	return beta;
	}
	
	
	  public double[] getBestBeta(String s) {
			return bestBeta.get(s);
		}

}
