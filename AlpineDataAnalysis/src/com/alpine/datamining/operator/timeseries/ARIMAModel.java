/**
 * ClassName ARIMA Model
 *
 * Version information: 1.00
 *
 * Data: 2010-4-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.timeseries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.utility.DataType;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * The model of the improved neural net.
 * 
 */
public class ARIMAModel extends Prediction {
    private static final Logger itsLogger = Logger.getLogger(ARIMAModel.class);
    private static final long serialVersionUID = -3496752146166330659L;
	private List<SingleARIMAModel> models = new ArrayList<SingleARIMAModel>();
	private String dbType;
	private Column idColumn ;
	private Column groupColumn;
	private String idColumnName;
	private String valueColumnName;
	private String groupColumnName = null;
	private String idTypeName;
	private String groupTypeName;
	private int idSqlType;
	private int groupSqlType;

	public List<SingleARIMAModel> getModels() {
		return models;
	}

	public void setModels(List<SingleARIMAModel> models) {
		this.models = models;
	}	

	public Column getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(Column idColumn) {
		this.idColumn = idColumn;
	}

	public Column getGroupColumn() {
		return groupColumn;
	}

	public void setGroupColumn(Column groupColumn) {
		this.groupColumn = groupColumn;
	}

	public String getIdColumnName() {
		return idColumnName;
	}

	public void setIdColumnName(String idColumnName) {
		this.idColumnName = idColumnName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public String getGroupColumnName() {
		return groupColumnName;
	}

	public void setGroupColumnName(String groupColumnName) {
		this.groupColumnName = groupColumnName;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getIdTypeName() {
		return idTypeName;
	}

	public void setIdTypeName(String idTypeName) {
		this.idTypeName = idTypeName;
	}

	public String getGroupTypeName() {
		return groupTypeName;
	}

	public void setGroupTypeName(String groupTypeName) {
		this.groupTypeName = groupTypeName;
	}

	public int getIdSqlType() {
		return idSqlType;
	}

	public void setIdSqlType(int idSqlType) {
		this.idSqlType = idSqlType;
	}

	public int getGroupSqlType() {
		return groupSqlType;
	}

	public void setGroupSqlType(int groupSqlType) {
		this.groupSqlType = groupSqlType;
	}

	public ARIMAModel(DataSet dataSet) {
		super(dataSet);
	}
	public ARIMAModel(DataSet dataSet ,String idColumn, String valueColumn, int p, int d, int q, double[]phi, double[]theta, double intercept,double[] se, double[] data,double[] resid, double mu, int[]arma,MakeARIMARet model, double sigma2, int ncxreg, double likelihood) {
		super(dataSet);
	}
	public String toString(){
		String str = "";
		for(int i = 0; i < models.size(); i++){
			str += models.get(i).toString();
		}
		return str;
	}
	private void createTable(Connection connection, String tableName, AnalysisStorageParameterModel analysisStorageParameterModel) throws OperatorException {
		String idType = null;
		
		if(idColumn.getValueType()==DataType.DATE){
			idType = "date";
		}else if(idColumn.getValueType()==DataType.TIME){
			if(!dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
				idType = "time";
			}else{
				idType = "date";
			}
		}else if(idColumn.getValueType()==DataType.DATE_TIME){
			if(!dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
				idType = "timestamp";
			}else{
				if(idTypeName.equalsIgnoreCase("DATE")){
					idType = "date";
				}else{
					idType = "timestamp";
				}
			}
		}else if(idColumn.isNumerical()){
			if(idColumn.getValueType() == DataType.INTEGER){
				idType = "integer";
			}else{
				idType = "float";
			}
		}else{
			idType = "integer";
		}

		String create = null;
	
		
		if(groupColumn == null){
			create = "create table "+tableName+" ("+StringHandler.doubleQ(idColumnName)+" "+idType+","+StringHandler.doubleQ(valueColumnName)+" float, "+StringHandler.doubleQ("alpine_se")+" float) ";
		}else{
			String groupType = null;
			if(groupColumn.getValueType()==DataType.DATE){
				groupType = "date";
			}else if(groupColumn.getValueType()==DataType.TIME){
				if(!dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
					groupType = "time";
				}else{
					groupType = "date";
				}
			}else if(groupColumn.getValueType()==DataType.DATE_TIME){
				if(!dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
					groupType = "timestamp";
				}else{
					if(groupTypeName.equalsIgnoreCase("DATE")){
						groupType = "date";
					}else{
						groupType = "timestamp";
					}
				}
			}else if(groupColumn.isNumerical()){
				if(groupColumn.getValueType() == DataType.INTEGER){
					groupType = "integer";
				}else{
					groupType = "float";
				}
			}else{
				if(dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
					groupType = "varchar2(2000)";
				}else if (dbType.equalsIgnoreCase(DataSourceInfoDB2.dBType) || dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
					groupType = "varchar(2000)";
				}else{
					groupType = "text";
				}
			}
			create = "create table "+tableName+" ("+StringHandler.doubleQ(idColumnName)+" "+idType+","+StringHandler.doubleQ(groupColumnName)+" "+groupType+","+StringHandler.doubleQ(valueColumnName)+" float, "+StringHandler.doubleQ("alpine_se")+" float) ";
		}
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
		if(analysisStorageParameterModel != null){
			create += sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel());
		}
		if(!dbType.equals(DataSourceInfoDB2.dBType)){
			create += sqlGenerator.setCreateTableEndingSql((analysisStorageParameterModel == null )? null : analysisStorageParameterModel.getSqlDistributeString());
		}

		try {
			Statement st = connection.createStatement();
			itsLogger.debug("ARIMAModel.createTable():sql="+create);
			st.execute(create);
			st.close();
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	public ARIMARPredictResult prediction(int nAhead, Connection connection, String tableName,AnalysisStorageParameterModel analysisStorageParameterModel) throws OperatorException{
		createTable(connection, tableName, analysisStorageParameterModel);
		ARIMARPredictResult result = new ARIMARPredictResult();
		result.setGroupColumnName(groupColumnName);
		SingleARIMARPredictResult singleResult = new SingleARIMARPredictResult();
		for(int i = 0; i < models.size(); i++){
			singleResult = models.get(i).prediction(nAhead, connection, tableName);
			result.getResults().add(singleResult);
		}
		return result;
	}
	@Override
	public DataSet performPrediction(DataSet dataSet,
			Column predictedLabel) throws OperatorException {
		return null;
	}
}
