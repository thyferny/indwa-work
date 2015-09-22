package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;


public class PLDAPredictConfig extends PredictorConfig{
	
	public static final String VISUALIZATION_TYPE ="com.alpine.datamining.api.impl.visual.PLDAPredictorVisualizationType";

	public  static final String  ConstPLDADocTopicOutputTable="PLDADocTopicOutputTable";
  	public  static final String  ConstPLDADocTopicOutputSchema="PLDADocTopicOutputSchema";
	public  static final String  ConstPLDADocTopicDropIfExist="PLDADocTopicDropIfExist";
	public  static final String  ConstIterationNumber="iterationNumber";
	public static final String ConstPLDADocTopicOutputTableStorageParams = "PLDADocTopicOutputTableStorageParameters";

	private static final ArrayList<String> parameterNames = new ArrayList<String>();
  	private String PLDADocTopicOutputTable;
  	private String PLDADocTopicOutputSchema;
  	private String PLDADocTopicDropIfExist;
  	private String iterationNumber;
  	private AnalysisStorageParameterModel PLDADocTopicOutputTableStorageParameters;

  	
	static{
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstOutputTableStorageParameters);
		parameterNames.add(ConstPLDADocTopicOutputSchema);
		parameterNames.add(ConstPLDADocTopicDropIfExist);
		parameterNames.add(ConstIterationNumber);
		parameterNames.add(ConstPLDADocTopicOutputTableStorageParams);
		
	}

	public PLDAPredictConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}


	public String getPLDADocTopicOutputTable() {
		return PLDADocTopicOutputTable;
	}


	public void setPLDADocTopicOutputTable(String pLDADocTopicOutputTable) {
		PLDADocTopicOutputTable = pLDADocTopicOutputTable;
	}


	public String getPLDADocTopicOutputSchema() {
		return PLDADocTopicOutputSchema;
	}


	public void setPLDADocTopicOutputSchema(String pLDADocTopicOutputSchema) {
		PLDADocTopicOutputSchema = pLDADocTopicOutputSchema;
	}


	public String getPLDADocTopicDropIfExist() {
		return PLDADocTopicDropIfExist;
	}


	public void setPLDADocTopicDropIfExist(String pLDADocTopicDropIfExist) {
		PLDADocTopicDropIfExist = pLDADocTopicDropIfExist;
	}


	public String getIterationNumber() {
		return iterationNumber;
	}


	public void setIterationNumber(String iterationNumber) {
		this.iterationNumber = iterationNumber;
	}


	public AnalysisStorageParameterModel getPLDADocTopicOutputTableStorageParameters() {
		return PLDADocTopicOutputTableStorageParameters;
	}


	public void setPLDADocTopicOutputTableStorageParameters(
			AnalysisStorageParameterModel pLDADocTopicOutputTableStorageParameters) {
		PLDADocTopicOutputTableStorageParameters = pLDADocTopicOutputTableStorageParameters;
	}


	
	
}
