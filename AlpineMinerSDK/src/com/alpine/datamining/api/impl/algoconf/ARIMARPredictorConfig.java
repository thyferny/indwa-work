/**
 * ClassName LogisticRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;

/**
 * @author John Zhao
 * 
 */
public class ARIMARPredictorConfig extends PredictorConfig {

	private static final List<String> parameterNames = new ArrayList<String>();
	
	public static final String ConstAheadNumber = "aheadNumber";
	public static final String ConstDbConnectionName = "dbConnectionName";
	public static final String   Const_URL="url";
	public static final String  Const_PASSWORD="password";
	public static final String  Const_USERNAME="userName";
	public static final String  Const_SYSTEM="system";
 	public static final String  ConstTableType="tableType";

	static{
		parameterNames.add(ConstAheadNumber);
		parameterNames.add(ConstDbConnectionName);
		parameterNames.add(Const_URL);
		parameterNames.add(Const_PASSWORD);
		parameterNames.add(Const_USERNAME);
		parameterNames.add(Const_SYSTEM);
		parameterNames.add(ConstTableType);
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstOutputTableStorageParameters);
	}
	public ARIMARPredictorConfig(EngineModel trainedModel) {
		super(trainedModel);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.TimeSeriesPredictShapeVisualizationType"
				+","+"com.alpine.datamining.api.impl.visual.TimeSeriesPredictTableVisualizationType");
		setParameterNames(parameterNames);

	}
	public ARIMARPredictorConfig() {
		super();
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.TimeSeriesPredictShapeVisualizationType"
				+","+"com.alpine.datamining.api.impl.visual.TimeSeriesPredictTableVisualizationType");
		setParameterNames(parameterNames);
	}
	private String aheadNumber;
	private String dbConnectionName = null;
	private String url;
	private String password;
	private String userName;
	private String system;
	private String tableType;

	public String getAheadNumber() {
		return aheadNumber;
	}

	public void setAheadNumber(String aheadNumber) {
		this.aheadNumber = aheadNumber;
	}
	
	public String getDbConnectionName() {
		return dbConnectionName;
	}
	public void setDbConnectionName(String dbConnectionName) {
		this.dbConnectionName = dbConnectionName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
}
