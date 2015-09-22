/**
* ClassName PLDAConfig.java
*
* Version information: 1.00
*
* Data: 2012-2-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;

/**
 * @author Shawn
 *
 */
public class PLDAConfig extends AbstractModelTrainerConfig{
	
	public static final String VISUALIZATION_TYPE ="com.alpine.datamining.api.impl.visual.PLDATrainerVisualizationType";
	public  static final String  ConstDictionarySchema="dictionarySchema";
//	public  static final String  ConstContentSchema="contentSchema";
	public  static final String  ConstDictionaryTable="dictionaryTable";
//	public  static final String  ConstContentTable="contentTable";
	public  static final String  ConstDicIndexColumn="dicIndexColumn";
	public  static final String  ConstDicContentColumn="dicContentColumn";
	public  static final String  ConstContentDocIndexColumn="contentDocIndexColumn";
	public  static final String  ConstContentWordColumn="contentWordColumn";
	public  static final String  ConstPLDAModelOutputTable="PLDAModelOutputTable";
  	public  static final String  ConstPLDAModelOutputSchema="PLDAModelOutputSchema";
  	public  static final String  ConstPLDADropIfExist="PLDADropIfExist";
	public  static final String  ConstAlpha="Alpha";
	public  static final String  ConstBeta="Beta";
	public  static final String  ConstTopicNumber="topicNumber";
	public  static final String  ConstTopicOutTable="topicOutTable";
	public  static final String  ConstIterationNumber="iterationNumber";
	public  static final String  ConstTopicOutSchema="topicOutSchema";
	public  static final String  ConstTopicDropIfExist="topicDropIfExist";
	public  static final String  ConstDocTopicOutTable="docTopicOutTable";
	public  static final String  ConstDocTopicOutSchema="docTopicOutSchema";
	public  static final String  ConstDocTopicDropIfExist="docTopicDropIfExist";
	public static final String ConstPLDAModelOutputTableStorageParams = "PLDAModelOutputTableStorageParameters";
	public static final String ConstTopicOutTableStorageParams = "topicOutTableStorageParameters";
	public static final String ConstDocTopicOutTableStorageParams = "docTopicOutTableStorageParameters";

	private static final ArrayList<String> parameterNames = new ArrayList<String>();
	private String dictionarySchema;
	private String dictionaryTable;
//	private String contentSchema;
//	private String contentTable;
  	private String dicIndexColumn;
  	private String topicNumber;
  	private String topicOutTable;
  	private String topicOutSchema;
  	private String topicDropIfExist;
  	private String iterationNumber;
  	
  	private String docTopicOutTable;
  	private String docTopicOutSchema;
  	private String docTopicDropIfExist;
  	
  	
	
	private String dicContentColumn;
  	private String contentDocIndexColumn;
  	private String contentWordColumn;
  	private String PLDAModelOutputTable;
  	private String PLDAModelOutputSchema;
  	private String PLDADropIfExist;
  	private String Alpha;
  	private String Beta;
  	private AnalysisStorageParameterModel PLDAModelOutputTableStorageParameters;
  	private AnalysisStorageParameterModel topicOutTableStorageParameters;
  	private AnalysisStorageParameterModel docTopicOutTableStorageParameters;

	static{
		parameterNames.add(ConstDictionarySchema);
//		parameterNames.add(ConstContentSchema);
		parameterNames.add(ConstDictionaryTable);
//		parameterNames.add(ConstContentTable);
		parameterNames.add(ConstDicIndexColumn);
		parameterNames.add(ConstDicContentColumn);
		parameterNames.add(ConstTopicNumber);
		parameterNames.add(ConstTopicOutTable);
		parameterNames.add(ConstIterationNumber);
		parameterNames.add(ConstTopicOutSchema);
		parameterNames.add(ConstContentDocIndexColumn);
		parameterNames.add(ConstContentWordColumn);
		parameterNames.add(ConstPLDAModelOutputTable);
		parameterNames.add(ConstPLDAModelOutputSchema);
		parameterNames.add(ConstPLDADropIfExist);
		parameterNames.add(ConstAlpha);
		parameterNames.add(ConstBeta);
		parameterNames.add(ConstDocTopicOutTable);
		parameterNames.add(ConstDocTopicOutSchema);
		parameterNames.add(ConstDocTopicDropIfExist);
		parameterNames.add(ConstPLDAModelOutputTableStorageParams);
		parameterNames.add(ConstTopicOutTableStorageParams);
		parameterNames.add(ConstDocTopicOutTableStorageParams);
	}
  	
  	
  	public PLDAConfig(){
  		super();
		setParameterNames(parameterNames);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
  	}
	
  	public String getDocTopicOutTable() {
		return docTopicOutTable;
	}

	public void setDocTopicOutTable(String docTopicOutTable) {
		this.docTopicOutTable = docTopicOutTable;
	}

	public String getDocTopicOutSchema() {
		return docTopicOutSchema;
	}

	public void setDocTopicOutSchema(String docTopicOutSchema) {
		this.docTopicOutSchema = docTopicOutSchema;
	}

	public String getDocTopicDropIfExist() {
		return docTopicDropIfExist;
	}

	public void setDocTopicDropIfExist(String docTopicDropIfExist) {
		this.docTopicDropIfExist = docTopicDropIfExist;
	}

	public String getTopicNumber() {
		return topicNumber;
	}

	public void setTopicNumber(String topicNumber) {
		this.topicNumber = topicNumber;
	}

	public String getTopicOutSchema() {
		return topicOutSchema;
	}

	public void setTopicOutSchema(String topicOutSchema) {
		this.topicOutSchema = topicOutSchema;
	}

	public String getDictionarySchema() {
		return dictionarySchema;
	}

	public void setDictionarySchema(String dictionarySchema) {
		this.dictionarySchema = dictionarySchema;
	}

	public String getTopicDropIfExist() {
		return topicDropIfExist;
	}

	public void setTopicDropIfExist(String topicDropIfExist) {
		this.topicDropIfExist = topicDropIfExist;
	}

//	public String getContentSchema() {
//		return contentSchema;
//	}
//
//	public void setContentSchema(String contentSchema) {
//		this.contentSchema = contentSchema;
//	}

	public String getDictionaryTable() {
		return dictionaryTable;
	}

	public void setDictionaryTable(String dictionaryTable) {
		this.dictionaryTable = dictionaryTable;
	}

//	public String getContentTable() {
//		return contentTable;
//	}
//
//	public void setContentTable(String contentTable) {
//		this.contentTable = contentTable;
//	}

	public String getAlpha() {
		return Alpha;
	}

	public void setAlpha(String pLDAAlpha) {
		Alpha = pLDAAlpha;
	}

	public String getBeta() {
		return Beta;
	}

	public void setBeta(String pLDABeta) {
		Beta = pLDABeta;
	}

	public String getDicIndexColumn() {
		return dicIndexColumn;
	}

	public void setDicIndexColumn(String dicIndexColumn) {
		this.dicIndexColumn = dicIndexColumn;
	}

	public String getDicContentColumn() {
		return dicContentColumn;
	}

	public void setDicContentColumn(String dicContentColumn) {
		this.dicContentColumn = dicContentColumn;
	}

	public String getContentDocIndexColumn() {
		return contentDocIndexColumn;
	}

	public void setContentDocIndexColumn(String contentDocIndexColumn) {
		this.contentDocIndexColumn = contentDocIndexColumn;
	}

	public String getContentWordColumn() {
		return contentWordColumn;
	}

	public void setContentWordColumn(String contentWordColumn) {
		this.contentWordColumn = contentWordColumn;
	}



	public String getPLDAModelOutputTable() {
		return PLDAModelOutputTable;
	}

	public void setPLDAModelOutputTable(String pLDAModelOutputTable) {
		PLDAModelOutputTable = pLDAModelOutputTable;
	}

	public String getPLDAModelOutputSchema() {
		return PLDAModelOutputSchema;
	}

	public void setPLDAModelOutputSchema(String pLDAModelOutputSchema) {
		PLDAModelOutputSchema = pLDAModelOutputSchema;
	}

	public String getPLDADropIfExist() {
		return PLDADropIfExist;
	}

	public void setPLDADropIfExist(String pLDADropIfExist) {
		PLDADropIfExist = pLDADropIfExist;
	}

	public static ArrayList<String> getParameternames() {
		return parameterNames;
	}
	


	public String getTopicOutTable() {
		return topicOutTable;
	}

	public void setTopicOutTable(String topicOutTable) {
		this.topicOutTable = topicOutTable;
	}

	public String getIterationNumber() {
		return iterationNumber;
	}

	public void setIterationNumber(String iterationNumber) {
		this.iterationNumber = iterationNumber;
	}

	public AnalysisStorageParameterModel getPLDAModelOutputTableStorageParameters() {
		return PLDAModelOutputTableStorageParameters;
	}

	public void setPLDAModelOutputTableStorageParameters(
			AnalysisStorageParameterModel pLDAModelOutputTableStorageParameters) {
		PLDAModelOutputTableStorageParameters = pLDAModelOutputTableStorageParameters;
	}

	public AnalysisStorageParameterModel getTopicOutTableStorageParameters() {
		return topicOutTableStorageParameters;
	}

	public void setTopicOutTableStorageParameters(
			AnalysisStorageParameterModel topicOutTableStorageParameters) {
		this.topicOutTableStorageParameters = topicOutTableStorageParameters;
	}

	public AnalysisStorageParameterModel getDocTopicOutTableStorageParameters() {
		return docTopicOutTableStorageParameters;
	}

	public void setDocTopicOutTableStorageParameters(
			AnalysisStorageParameterModel docTopicOutTableStorageParameters) {
		this.docTopicOutTableStorageParameters = docTopicOutTableStorageParameters;
	}

}
