/**
 * ClassName AbstractSVM
 *
 * Version information: 1.00
 *
 * Data: Apr 18, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.utility.ColumnTypeTransformer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.tools.StringHandler;

public abstract class AbstractSVMLearner{
	private static Logger logger = Logger.getLogger(AbstractSVMLearner.class);
	protected SVMParameter para;
	private ColumnTypeTransformer transformer = new ColumnTypeTransformer();
	private IDataSourceInfo  dataSourceInfo;

	abstract public Model train(DataSet dataSet,SVMParameter parameter) throws OperatorException;

	public SVMParameter getPara() {
		return para;
	}
	public void setPara(SVMParameter para) {
		this.para = para;
	}
	public ColumnTypeTransformer getTransformer() {
		return transformer;
	}
	public void setTransformer(ColumnTypeTransformer transformer) {
		this.transformer = transformer;
	}
	public IDataSourceInfo getDataSourceInfo() {
		return dataSourceInfo;
	}
	public void setDataSourceInfo(IDataSourceInfo dataSourceInfo) {
		this.dataSourceInfo = dataSourceInfo;
	}
	public StringBuffer getColumnWhere(DataSet newDataSet){
		StringBuffer where = new StringBuffer();
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		boolean first = true;
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(!first){
				where.append(" and ");
			}else{
				first = false;
			}
			where.append(columnName).append(" is not null ");
		}
		return where;
	}

	public StringBuffer getColumnArray(DataSet newDataSet){
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		ArrayList<String> columnsArray = new ArrayList<String>();
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			columnsArray.add(columnName);
		}
		StringBuffer ind = new StringBuffer();
		if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			ind.append(CommonUtility.array2OracleArray(columnsArray, CommonUtility.OracleDataType.Float));
		}else{
			ind.append("array[");
			for(int i = 0; i < columnsArray.size(); i++){
				if(i != 0){
					ind.append(",");
				}
				ind.append(columnsArray.get(i));
			}
			ind.append("]");
		}
		return ind;
	}
	Array getColumnSqlArray(DataSet newDataSet) throws SQLException {
		String [] columns = new String[0];
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter = attsNew.iterator();
		ArrayList<String> columnsArray = new ArrayList<String>();
		while (attsIter.hasNext()) {
			Column att = attsIter.next();
			String columnName = StringHandler.doubleQ(att.getName());
			columnsArray.add(columnName);
		}
		columns = columnsArray.toArray(columns);
		DatabaseConnection databaseConnection = ((DBTable) newDataSet
					.getDBTable()).getDatabaseConnection();
		Array sqlArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		return sqlArray;
	}

	public void setModel(ResultSet rs, SVMModel model) throws SQLException{
		try {
			if(rs.next()){
				model.setInds(rs.getInt(1));
				model.setCumErr(rs.getDouble(2));
				model.setEpsilon(rs.getDouble(3));
				model.setRho(rs.getDouble(4));
				model.setB(rs.getDouble(5));
				model.setNsvs(rs.getInt(6));
				model.setIndDim(rs.getInt(7));
				Double [] weights = null;
				if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
					BigDecimal[] results = (BigDecimal[])rs.getArray(8).getArray();
					if (results != null){
						weights = new Double[model.getNsvs()];
						for(int i = 0; i < results.length && i < model.getNsvs(); i++){
							weights[i] = results[i].doubleValue();
						}
					}
				}else{
					weights = (Double[])rs.getArray(8).getArray();
				}
				Double[] weightsArray = null; 
				if (weights != null ){
					weightsArray = new Double[model.getNsvs()];
					for(int i = 0; i < weights.length && i < model.getNsvs(); i++){
						if (weights[i] != null && !Double.isNaN(weights[i])){
							weightsArray[i] = weights[i].doubleValue();
						}else{
							weightsArray[i] = 0.0;
						}
					}
				}
				model.setWeights(weightsArray);
				Double [] individuals = null;
				if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
					BigDecimal[] results = (BigDecimal[])rs.getArray(9).getArray();
					if (results != null){
						individuals = new Double[model.getNsvs() * model.getIndDim()];
						for(int i = 0; i < results.length && i < model.getNsvs() * model.getIndDim(); i++){
							if (results[i] != null){
								individuals[i] = results[i].doubleValue();
							}
						}
					}
				}else{
					individuals = (Double [])rs.getArray(9).getArray();
				}
				Double[] individualsArray = null;
				if(individuals != null){
					individualsArray = new Double[model.getNsvs() * model.getIndDim()];
					for(int i = 0; i < individuals.length && i < model.getNsvs() * model.getIndDim(); i++){
								 if (individuals[i] != null && !Double.isNaN(individuals[i])){
									 individualsArray[i] = individuals[i].doubleValue();
								 }else{
									 individualsArray[i] = 0.0;
								 }
					}
				}else{
					individualsArray = null;
				}
				model.setIndividuals(individualsArray);
			}
		} catch (SQLException e) {
			throw e;
		}
	}
	public void setModel(CallableStatement st, SVMModel model, int startIndex) throws SQLException{
		try {
				model.setInds(st.getInt(startIndex+9));
				model.setCumErr(st.getDouble(startIndex+10));
				model.setEpsilon(st.getDouble(startIndex+11));
				model.setRho(st.getDouble(startIndex+12));
				model.setB(st.getDouble(startIndex+13));
				model.setNsvs(st.getInt(startIndex+14));
				model.setIndDim(st.getInt(startIndex+15));
				Double [] weights = null;
				weights = (Double[])st.getArray(startIndex+16).getArray();
				Double[] weightsArray = null; 
				if (weights != null ){
					weightsArray = new Double[model.getNsvs()];
					for(int i = 0; i < weights.length && i < model.getNsvs(); i++){
						if (weights[i] != null && !Double.isNaN(weights[i])){
							weightsArray[i] = weights[i].doubleValue();
						}else{
							weightsArray[i] = 0.0;
						}
					}
				}
				model.setWeights(weightsArray);
				Double [] individuals = null;
				individuals = (Double [])st.getArray(startIndex+17).getArray();
				Double[] individualsArray = null;
				if(individuals != null){
					individualsArray = new Double[model.getNsvs() * model.getIndDim()];
					for(int i = 0; i < individuals.length && i < model.getNsvs() * model.getIndDim(); i++){
								 if (individuals[i] != null && !Double.isNaN(individuals[i])){
									 individualsArray[i] = individuals[i].doubleValue();
								 }else{
									 individualsArray[i] = 0.0;
								 }
					}
				}else{
					individualsArray = null;
				}
				model.setIndividuals(individualsArray);
		} catch (SQLException e) {
			throw e;
		}
	}

	protected void dropTable(Statement st,String tableName) throws OperatorException {
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		try {
			logger.debug("AbstractSVM.dropTable():sql="
							+ truncate.toString());
			st.execute(truncate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
			logger.debug("AbstractSVM.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

}
