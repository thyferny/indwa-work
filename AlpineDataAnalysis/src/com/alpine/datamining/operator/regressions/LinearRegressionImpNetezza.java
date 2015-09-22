/**
 * ClassName LinearRegressionImpNetezza.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;

import java.sql.ResultSet;
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
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 *  <p>This Netezza algorithm to calculate a linear regression model.</p>
 * @author Eason
 */
public class LinearRegressionImpNetezza extends LinearRegressionImp {
    private static final Logger itsLogger = Logger.getLogger(LinearRegressionImpNetezza.class);
    private int maxColumn = 1000;
	private Double[] betas;
	private String[] columns;
	private int aliasCount; 
	public LinearRegressionImpNetezza() {
		super();
		maxColumn = AlpineDataAnalysisConfig.NZ_MAX_COLUMN_COUNT;
	}
	public Model learn(DataSet dataSet, LinearRegressionParameter para, String columnNames) throws OperatorException {
		this.dataSet = dataSet;
		ArrayList<String> columnNamesList = new ArrayList<String>();
		int k = 0;
		StringBuilder orignotnull = new StringBuilder();
		orignotnull.append(" where ");
		if (columnNames != null && !StringUtil.isEmpty(columnNames.trim())){
			String[] columnNamesArray=columnNames.split(",");
			for(String s:columnNamesArray)
			{
				columnNamesList.add(s);
				if (k == 0) {
					orignotnull.append(StringHandler.doubleQ(s)).append(" is not null");
					k = 1;
				} else
					orignotnull.append(" and ").append(StringHandler.doubleQ(s)).append(" is not null ");
			}
		}
		if(k==0) orignotnull=new StringBuilder("");
		transformer.setColumnNames(columnNamesList);
		transformer.setAnalysisInterActionModel(para.getAnalysisInterActionModel());
		newDataSet=transformer.TransformCategoryToNumeric_new(dataSet,null);
		DatabaseConnection databaseConnection = ((DBTable) newDataSet
				.getDBTable()).getDatabaseConnection();
		
		Column label =newDataSet.getColumns().getLabel();
		String labelName =StringHandler.doubleQ(label.getName()) ;
		String tableName=((DBTable) dataSet.getDBTable())
		.getTableName();

		String newTableName=((DBTable) newDataSet.getDBTable())
		.getTableName();
		
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		try {
			newDataSet.computeAllColumnStatistics();
			Columns atts=newDataSet.getColumns();

			Iterator<Column> atts_i=atts.iterator();
		
			int count=0;
			String [] columnNamesArray = new String[atts.size()];
			while(atts_i.hasNext())
			{
				Column att=atts_i.next();
				columnNamesArray[count]=att.getName();
				count++;
			}
			null_list=calculateNull(newDataSet);
			StringBuilder sb_notNull = getWhere(atts);
			if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1 || (AlpineDataAnalysisConfig.NZ_PROCEDURE_SWITCH == 0 && newDataSet.getColumns().size() < AlpineDataAnalysisConfig.NZ_PROCEDURE_COLUMN_LIMIT)){
				getCoefficientAndR2Sql(columnNames, dataSet, labelName,tableName,
						newTableName, atts, columnNamesArray,orignotnull, sb_notNull);

			}else{
				getCoefficientAndR2(columnNames, dataSet, labelName,tableName,
					newTableName, atts, columnNamesArray, orignotnull,sb_notNull);
			}
			
			StringBuffer sSQL = new StringBuffer();
			
			sSQL.append("select count(*) from ").append(tableName).append(orignotnull.toString());
			int datasize=0;
			try {
				itsLogger.debug("LinearRegressionImpNetezza.learn():sql=" + sSQL);
				rs = st.executeQuery(sSQL.toString());
				while (rs.next()) {
					datasize = rs.getInt(1);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
			
			long dof = datasize - columnNamesArray.length - 1;
			
			if (dof <= 0)
			{
				model.setS(Double.NaN);
				return model;
			}

			double s = 0.0;
			if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1 || (AlpineDataAnalysisConfig.NZ_PROCEDURE_SWITCH == 0 && newDataSet.getColumns().size() < AlpineDataAnalysisConfig.NZ_PROCEDURE_COLUMN_LIMIT)){
				sSQL = null;
				if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1 ){
					sSQL = createSSQLLAlias(datasize, newTableName, label,
							columnNamesArray, coefficients);

				}else{
					sSQL = createSSQLL(datasize, newTableName, label,
						columnNamesArray, coefficients);
				}
				s = 0.0;
	
				try {
					itsLogger.debug("LinearRegressionImp.learn():sql="+sSQL);
					rs = st.executeQuery(sSQL.toString());			
					while (rs.next()) {
				    	s = rs.getDouble(1);
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}else{
				s = model.getS();
			}
			Matrix varianceCovarianceMatrix = getVarianceCovarianceMatrix(
					newTableName, columnNamesArray,  st);
			if(varianceCovarianceMatrix==null)
			{
				model.setErrorString(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.MATRIX_IS_SIGULAR, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
			}
			caculateStatistics( columnNamesArray, coefficients, model, s,
					varianceCovarianceMatrix, dof);
			
			if(null_list.size()!=0)
			{
				StringBuilder sb_null=new StringBuilder();
				for(int i=0;i<null_list.size();i++)
				{
					sb_null.append(StringHandler.doubleQ(null_list.get(i))).append(",");
				}
				sb_null=sb_null.deleteCharAt(sb_null.length()-1);
				String table_exist_null=AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.TABLE_EXIST_NULL, AlpineThreadLocal.getLocale());
				String[] temp=table_exist_null.split(";");
				model.setErrorString(temp[0]+sb_null.toString()+temp[1]+Tools.getLineSeparator());
			}
			if(transformer.isTransform())
			{
				dropTable(newTableName);
			}
			st.close();
            itsLogger.debug(LogUtils.exit("LinearRegressionImp", "learn", model.toString()));
			return model;
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	protected void getCoefficientAndR2(String columNames,
			DataSet dataSet, String labelName,String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder orignotnull,StringBuilder sbNotNull) throws OperatorException {
//		double r2 = 0;
		coefficients = getCoefficient(newTableName,
				columnNames, labelName, st, sbNotNull);
		getCoefficientMap(columnNames);
		model =  new LinearRegressionModelNetezza(dataSet,columnNames, columNames, coefficients,coefficientmap);
		if(!this.newDataSet.equals(this.dataSet))
		{
			model.setAllTransformMap_valueKey(transformer.getAllTransformMap_valueKey());
		}
		model.setInteractionColumnExpMap(transformer.getInteractionColumnExpMap());
		model.setInteractionColumnColumnMap(transformer.getInteractionColumnColumnMap());
		long dof = newDataSet.size() - columnNames.length - 1;

		double[] result = cacluateRSquareAndS(dataSet, tableName,
				labelName, model, dof);			
		model.setR2(result[0]);
		model.setS(result[1]);
	}

	protected Double[] getCoefficient(String tableName,
			String[] columnNames, String label, Statement st,
			StringBuilder sbNotNull)
			throws OperatorException {
		hessian = new Matrix(columnNames.length + 1, columnNames.length + 1);
		Matrix XY = new Matrix(columnNames.length + 1,1);
		ArrayList<String> sbAllArray = new ArrayList<String>();
		StringBuilder XYSql = new StringBuilder("select  ");//FloatArray( 
      	for (int x = 0; x < columnNames.length + 1; x++) {
			StringBuilder X=new StringBuilder();
			if ( x == 0)
			{
		        	X.append("1.0::double");
			}
			else
			{
				X.append(StringHandler.doubleQ(columnNames[x-1])).append("::double");
				XYSql.append(",");
			}
			XYSql.append("sum((").append(X).append(")*").append(label).append("::double )");
    		for (int y = x; y < columnNames.length + 1; y++) {
    			StringBuilder Y=new StringBuilder();

    			if (y == 0)
    			{
    		        	Y.append("1.0::double");
    			}
    			else
    			{
    				Y.append(StringHandler.doubleQ(columnNames[y-1])).append("::double");
    			}
    			sbAllArray.add("sum(("+X+")*"+Y+")");
    		}//end for(y)
    	}//end for(x)
      	XYSql.append(" from ").append(tableName).append(sbNotNull);
//      	itsLogger.info("LinearRegressionImpNetezza.getCoefficient():sql="+sbAll);
      	try {
          	itsLogger.info("LinearRegressionImpNetezza.getCoefficient():sql="+XYSql);
			ResultSet rs = st.executeQuery(XYSql.toString());
			while(rs.next())
			{
				for(int i = 0; i < columnNames.length + 1; i++){
					double doubleValue = rs.getDouble(i + 1);;
					XY.set(i,0,doubleValue);
				}
			}
			int times = sbAllArray.size() / maxColumn;
			double[] rsResult = new double[sbAllArray.size()];
			for(int i = 0; i < times; i++){
				StringBuffer sql = new StringBuffer(" select ");
				for(int j = 0 ; j < maxColumn; j++){
					if(j != 0){
						sql.append(",");
					}
					sql.append(sbAllArray.get(i * maxColumn + j));
				}
				sql.append(" from ").append(tableName).append(sbNotNull);
		      	itsLogger.info("LinearRegressionImpNetezza.getCoefficient():sql="+sql);
				rs = st.executeQuery(sql.toString());
				if (rs.next()){
					for(int j = 0; j < maxColumn; j++){
						rsResult[i * maxColumn + j] = rs.getDouble(j + 1);
					}
				}
			}
			if (sbAllArray.size() > times*maxColumn){
				StringBuffer sql = new StringBuffer(" select ");
				for(int j = 0 ; j < sbAllArray.size() - times*maxColumn; j++){
					if(j != 0){
						sql.append(",");
					}
					sql.append(sbAllArray.get(times * maxColumn + j));
				}
				sql.append(" from ").append(tableName).append(sbNotNull);
		      	itsLogger.info("LinearRegressionImpNetezza.getCoefficient():sql="+sql);
				rs = st.executeQuery(sql.toString());
				if (rs.next()){
					for(int j = 0; j < sbAllArray.size() - times * maxColumn; j++){
						rsResult[times * maxColumn + j] = rs.getDouble(j + 1);
					}
				}
			}
				int i=0;
				for(int x = 0 ; x < columnNames.length + 1; x++)
				{
					for (int y = x; y < columnNames.length + 1; y++) {
						{
							double h = 0.0;
							if(!Double.isNaN(rsResult[i])){
								h=rsResult[i];
							}
							hessian.set(x, y, h);
							if(x!=y)
							{
								hessian.set(y, x, h);
							}
							i++;
						}
					}
				}
//			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		Matrix beta = null;
    	Matrix varianceCovarianceMatrix = null;
		coefficients = new Double[columnNames.length + 1];
		for(int i = 0; i < coefficients.length; i++){
			coefficients[i] = 0.0;
		}
   	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    		beta = varianceCovarianceMatrix.times(XY);
    		for ( int i = 0; i < beta.getRowDimension(); i++)
    		{
    			if (i == 0)
    			{
    				coefficients[beta.getRowDimension() - 1] = beta.get(i, 0);
    			}
    			else
    			{
    				coefficients[i - 1] = beta.get(i, 0);
    			}
    		}

    	} catch (Exception e) {
    		itsLogger.warn(e.getLocalizedMessage());
   			return null;
    	}
		return coefficients;
	}

	private void generateBetaAndColumns() {
		betas = new Double[coefficients.length];
		columns = new String[coefficients.length - 1];
		betas[coefficients.length - 1] = coefficients[coefficients.length - 1];
		Columns atts=newDataSet.getColumns();
		Iterator<Column> attsIterator=atts.iterator();
		int i = 0;
		while(attsIterator.hasNext())
		{
			Column att=attsIterator.next();
			String columnName=StringHandler.doubleQ(att.getName());
			betas[i] = coefficients[i];
			columns[i] = columnName;
			i++;
		}
	}

	protected double[] cacluateRSquareAndS(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model, long dof) throws OperatorException {
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg((").append(labelName).append(")::double) from ").append(tableName);
		double avg = 0.0;
		try {
			itsLogger.debug("LinearRegressionImpNetezza.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				Double ret = rs.getDouble(1);
				if (ret != null)
				{
					avg = ret.doubleValue();
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			return new double[]{Double.NaN, Double.NaN};
		}

		String newTableName=((DBTable) newDataSet.getDBTable())
		.getTableName();
		long currentTime = System.currentTimeMillis();
		String resultTableName = "R" + currentTime;
		String betaTableName = "B"+currentTime;
		String columnTableName = "C"+currentTime;

		StringBuffer sql = new StringBuffer();
		double[] result = null;
		try {
			TableTransferParameter.createDoubleTable(betaTableName, st);
			TableTransferParameter.createDoubleTable(resultTableName, st);
			TableTransferParameter.createStringTable(columnTableName, st);
			generateBetaAndColumns();
			TableTransferParameter.insertTable(betaTableName, st, betas);
			TableTransferParameter.insertTable(columnTableName, st, columns);

			StringBuilder where = getWhere(newDataSet.getColumns());

		sql = new StringBuffer();

		sql.append("call  alpine_miner_lir_ca_r2s_proc('");
		
		sql.append(newTableName).append("','") 
		.append(where).append("','") 
		.append(labelName).append("',")
		.append(avg).append(",")
		.append(dof).append(",'")
		.append(betaTableName).append("','") 
		.append(columnTableName).append("','")
		.append(resultTableName).append("')");

			itsLogger.debug(
					"LinearRegressionImpNetezza.cacluateRSquare():sql=" + sql);
			st.execute(sql.toString());
			result = TableTransferParameter.getResult(resultTableName, st);
			TableTransferParameter.dropResultTable(betaTableName, st);
			TableTransferParameter.dropResultTable(columnTableName, st);
			TableTransferParameter.dropResultTable(resultTableName, st);
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		
		return result;
	}
	protected void getCoefficientAndR2Sql(String columNames,
			DataSet dataSet, String labelName,String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder orignotnull,StringBuilder sbNotNull) throws OperatorException {
		double r2 = 0;
		coefficients = getCoefficient(newTableName,
				columnNames, labelName, st, sbNotNull);
		getCoefficientMap(columnNames);
		model =  new LinearRegressionModelNetezza(dataSet,columnNames, columNames, coefficients,coefficientmap);
		if(!this.newDataSet.equals(this.dataSet))
		{
			model.setAllTransformMap_valueKey(transformer.getAllTransformMap_valueKey());
		}
		model.setInteractionColumnExpMap(transformer.getInteractionColumnExpMap());
		model.setInteractionColumnColumnMap(transformer.getInteractionColumnColumnMap());
		if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1){
			r2 = cacluateRSquareAlias(dataSet, tableName,
				labelName, model,orignotnull);
		}else{
			r2 = cacluateRSquare(dataSet, tableName,
					labelName, model,sbNotNull);
		}
		model.setR2(r2);
	}

	protected double cacluateRSquareAlias(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model, StringBuilder orignotnull) throws OperatorException {
		double RSquare = 0.0;
		StringBuffer RSquareSQL = new StringBuffer();
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg((").append(labelName).append(")::double) from ").append(tableName).append(" ").append(orignotnull);
		double avg = 0.0;
		try {
			itsLogger.debug("LinearRegressionImpNetezza.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				Double ret = rs.getDouble(1);
				if (ret != null)
				{
					avg = ret.doubleValue();
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			return Double.NaN;
		}

		StringBuffer predict = new StringBuffer(" select ");
		predict.append(generatePredictedStringForR(dataSet)).append(",").append(labelName);
		predict.append(" from ").append(tableName).append(" ").append(orignotnull);
		StringBuffer predictedY = new StringBuffer("(e0");
		for(int i = 0; i < aliasCount - 1; i++){
			predictedY.append("+e").append(i+1);
		}
		predictedY.append(")");
		RSquareSQL.append("select 1 - sum((").append(predictedY).append("-(").append(labelName).
		append(")::double)*(").append(predictedY).append("-(").append(labelName).
		append(")::double))*1.0/sum(((").append(labelName).append("::double)-").append(avg).append("::double)*((").append(labelName).append(")::double-").append(avg).append("::double)) from (").append(predict).append("  limit all ) foo");
		try {
			itsLogger.debug("LinearRegressionImpNetezza.cacluateRSquare():sql="+RSquareSQL);
			ResultSet rs = st.executeQuery(RSquareSQL.toString());
			if (rs.next())
			{
				Double ret = rs.getDouble(1);
				if (ret != null)
				{
					RSquare = ret.doubleValue();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			return Double.NaN;
		}
		return RSquare;
	}
	/**
	 * @param datasize
	 * @param tableName
	 * @param label
	 * @param columnNames
	 * @param coefficients
	 * @return
	 */
	protected StringBuffer createSSQLLAlias(int datasize, String tableName,
			Column label, String[] columnNames, Double[] coefficients) {
		int aliasCount = 0;
		StringBuffer subSql = new StringBuffer();
		subSql.append("select ").append(coefficients[coefficients.length - 1]);
		boolean first = false;
		for (int i = 0; i < columnNames.length; i++){
			if((i+1) % AlpineDataAnalysisConfig.NZ_ALIAS_NUM == 0 || i == columnNames.length - 1){
				if(first){
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients[i]+"::double*"+StringHandler.doubleQ(columnNames[i])).append(" e"+(aliasCount));
				aliasCount++;
				first = true;
			}else{
				if(first){
					first = false;
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients[i]+"::double*"+StringHandler.doubleQ(columnNames[i]));
			}
		}
		subSql.append(",").append(StringHandler.doubleQ(label.getName())).append(" from ").append(tableName);
		String labelName=StringHandler.doubleQ(label.getName())+"::double";
		StringBuffer predictedY = new StringBuffer("(e0");
		for(int i = 0; i < aliasCount - 1; i++){
			predictedY.append("+e").append(i+1);
		}
		predictedY.append(")");
		StringBuffer sSQL = new StringBuffer("select sqrt(");
			
		sSQL.append("sum(("+labelName+" - "+predictedY+")*("+labelName+" - "+predictedY+"))/"+(datasize-columnNames.length - 1));
		sSQL.append(") from (").append(subSql).append(" limit all ) foo ");
		return sSQL;
	}
	
	public StringBuffer generatePredictedStringForR(DataSet dataSet) {
		String dbType = ((DBTable) dataSet.getDBTable())
		.getDatabaseConnection().getProperties().getName();
		int count = 0;
		StringBuffer predictedString = new StringBuffer();
		predictedString.append("(");
		
		count++;
		predictedString.append(coefficients[coefficients.length - 1]).append("::double");
		Columns atts=model.getTrainingHeader().getColumns();
		Iterator<Column> attsIterator=atts.iterator();
		int i = 0;
		ArrayList<String> coefficients = new ArrayList<String>();
		ArrayList<String> columns = new ArrayList<String>();
		
		while(attsIterator.hasNext())
		{
			Column att=attsIterator.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{			
				if(model.getCoefficientsMap().get(att.getName())==null)continue;
				double coefficient=model.getCoefficientsMap().get(att.getName());
				predictedString.append("+");
				predictedString.append("(");
				count++;
				predictedString.append("((").append(coefficient).append(")::double*").append(columnName).append(")");
				coefficients.add("("+coefficient+")::double");
				columns.add(columnName);
				i++;
			}else
			{
				
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=model.getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(model.getCoefficientsMap().get(columnname)==null)continue;
 	 					double coefficient=model.getCoefficientsMap().get(columnname);
 	 					predictedString.append("+");
 	 					predictedString.append("(");
 	 					count++;
 	 					predictedString.append("((").append(coefficient).append(")::double*").append("(case ");
 	 					predictedString.append(" when ").append(columnName).append("=");
 	 					value=StringHandler.escQ(value);
						value = CommonUtility.quoteValue(
								dbType, att, value);
 	 					predictedString.append(value).append(" then 1  else 0 end))");
 	 					coefficients.add("("+coefficient+")::double");
 	 					columns.add("(case  when "+columnName+"="+value+" then 1 else 0 end)");
 	 					i++;

 	 				}
			}
		}
		Iterator<Entry<String, String>>  iter = model.getInteractionColumnExpMap().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(model.getCoefficientsMap().get(key)!= null){
				predictedString.append("+");
				predictedString.append("(");
				count++;
				predictedString.append("("+value +"*(" + model.getCoefficientsMap().get(key)+"))");
				coefficients.add("("+model.getCoefficientsMap().get(key)+")::double");
				columns.add(value);
				i++;
			}
		}
		int aliasCount = 0;
		StringBuffer subSql = new StringBuffer(String.valueOf(this.coefficients[this.coefficients.length - 1])+"::double");
		boolean first = false;
		for (i = 0; i < coefficients.size(); i++){
			if((i+1) % AlpineDataAnalysisConfig.NZ_ALIAS_NUM == 0 || i == coefficients.size() - 1){
				if(first){
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients.get(i)+"*"+columns.get(i)).append(" e"+(aliasCount));
				aliasCount++;
				first = true;
			}else{
				if(first){
					first = false;
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients.get(i)+"*"+columns.get(i));
			}
		}
		this.aliasCount = aliasCount;
		return subSql;
	}

	protected double cacluateRSquare(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model, StringBuilder orignotnull) throws OperatorException {
		double RSquare = 0.0;
		StringBuffer RSquareSQL = new StringBuffer();
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg((").append(labelName).append(")::double) from ").append(tableName).append(" ").append(orignotnull);
		double avg = 0.0;
		try {
			itsLogger.debug("LinearRegressionImpNetezza.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				Double ret = rs.getDouble(1);
				if (ret != null)
				{
					avg = ret.doubleValue();
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			return Double.NaN;
		}

		StringBuffer predictedValueSQL = new StringBuffer();
		predictedValueSQL.append(model.generatePredictedString(dataSet));
		RSquareSQL.append("select 1 - sum((").append(predictedValueSQL).append("-(").append(labelName).
		append(")::double)*(").append(predictedValueSQL).append("-(").append(labelName).
		append(")::double))*1.0/sum(((").append(labelName).append("::double)-").append(avg).append("::double)*((").append(labelName).append(")::double-").append(avg).append("::double)) from ").append(tableName).append(" ").append(orignotnull);
		try {
			itsLogger.debug("LinearRegressionImpNetezza.cacluateRSquare():sql="+RSquareSQL);
			ResultSet rs = st.executeQuery(RSquareSQL.toString());
			if (rs.next())
			{
				Double ret = rs.getDouble(1);
				if (ret != null)
				{
					RSquare = ret.doubleValue();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Double.NaN;
		}
		return RSquare;
	}
	/**
	 * @param datasize
	 * @param tableName
	 * @param label
	 * @param columnNames
	 * @param coefficients
	 * @return
	 */
	protected StringBuffer createSSQLL(int datasize, String tableName,
			Column label, String[] columnNames, Double[] coefficients) {
		StringBuffer predictedY = new StringBuffer("("+coefficients[coefficients.length - 1]);
		predictedY.append("::double");
		for (int i = 0; i < columnNames.length; i++){
			predictedY.append("+"+coefficients[i]+"::double*\""+columnNames[i]+"\"");
		}
		String labelName=StringHandler.doubleQ(label.getName())+"::double";
		predictedY.append(")");
		StringBuffer sSQL = new StringBuffer("select sqrt(");
			
		sSQL.append("sum(("+labelName+" - "+predictedY+")*1.0*("+labelName+" - "+predictedY+"))/"+(datasize-columnNames.length - 1));
		sSQL.append(") from ").append(tableName);
		return sSQL;
	}

}
