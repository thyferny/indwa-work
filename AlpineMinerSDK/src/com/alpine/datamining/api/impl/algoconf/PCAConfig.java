/**
 * ClassName PCAConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-20
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;
/**
 * @author Shawn
 *
 */
import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;



public class PCAConfig extends AbstractAnalyticConfig{
	
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.PCAResultTableVisualizationType"+","+
	"com.alpine.datamining.api.impl.visual.PCATableVisualizationType";
		
	public static final String ConstAnalysisType = "analysisType";
	public static final String ConstPercent = "percent";
	
    public static final String ConstPCAQoutputTable = "PCAQoutputTable";
	public static final String ConstPCAQoutputSchema = "PCAQoutputSchema";
	public static final String ConstPCAQDropIfExist = "PCAQDropIfExist";
	public static final String ConstPCAQTableStorageParams = "PCAQoutputTableStorageParameters";

    public static final String ConstPCAQvalueOutputTable = "PCAQvalueOutputTable";
	public static final String ConstPCAQvalueOutputSchema = "PCAQvalueOutputSchema";
	public static final String ConstPCAQvalueDropIfExist = "PCAQvalueDropIfExist";
	public static final String ConstPCAQvalueTableStorageParams = "PCAQvalueOutputTableStorageParameters";

	public static final String ConstRemainColumns="remainColumns";
	
	  	private String analysisType=null;
		private static final ArrayList<String> parameterNames = new ArrayList<String>();
	  	private String percent;
	  	private String PCAQoutputTable;
	  	private String PCAQoutputSchema;
	  	private String PCAQDropIfExist;
	  	private String PCAQvalueOutputTable;
	  	private String PCAQvalueOutputSchema;
	  	private String PCAQvalueDropIfExist;
	  	private String remainColumns;
	  	private AnalysisStorageParameterModel PCAQoutputTableStorageParameters;
	  	private AnalysisStorageParameterModel PCAQvalueOutputTableStorageParameters;
		static{
			parameterNames.add(ConstAnalysisType);
			parameterNames.add(ConstPercent);
			parameterNames.add(ConstPCAQoutputTable);
			parameterNames.add(ConstPCAQoutputSchema);
			parameterNames.add(ConstPCAQDropIfExist);
			parameterNames.add(ConstPCAQvalueOutputTable);
			parameterNames.add(ConstPCAQvalueOutputSchema);
			parameterNames.add(ConstPCAQvalueDropIfExist);
			parameterNames.add(PARAMETER_COLUMN_NAMES);
			parameterNames.add(ConstRemainColumns);
			parameterNames.add(ConstPCAQTableStorageParams);
			parameterNames.add(ConstPCAQvalueTableStorageParams);
		}

	  	public PCAConfig(){
	  		setParameterNames(parameterNames);
	  		setVisualizationTypeClass(VISUALIZATION_TYPE);
	  	}

		public String getAnalysisType() {
			return analysisType;
		}


		public void setAnalysisType(String analysisType) {
			this.analysisType = analysisType;
		}

		public String getPercent() {
			return percent;
		}

		public void setPercent(String percent) {
			this.percent = percent;
		}

		public String getPCAQoutputTable() {
			return PCAQoutputTable;
		}


		public void setPCAQoutputTable(String pCAQoutputTable) {
			PCAQoutputTable = pCAQoutputTable;
		}


		public String getPCAQoutputSchema() {
			return PCAQoutputSchema;
		}


		public void setPCAQoutputSchema(String pCAQoutputSchema) {
			PCAQoutputSchema = pCAQoutputSchema;
		}


		public String getPCAQDropIfExist() {
			return PCAQDropIfExist;
		}


		public void setPCAQDropIfExist(String pCAQDropIfExist) {
			PCAQDropIfExist = pCAQDropIfExist;
		}


		public String getPCAQvalueOutputTable() {
			return PCAQvalueOutputTable;
		}


		public void setPCAQvalueOutputTable(String pCAQvalueOutputTable) {
			PCAQvalueOutputTable = pCAQvalueOutputTable;
		}


		public String getPCAQvalueOutputSchema() {
			return PCAQvalueOutputSchema;
		}


		public void setPCAQvalueOutputSchema(String pCAQvalueOutputSchema) {
			PCAQvalueOutputSchema = pCAQvalueOutputSchema;
		}


		public String getPCAQvalueDropIfExist() {
			return PCAQvalueDropIfExist;
		}


		public void setPCAQvalueDropIfExist(String pCAQvalueDropIfExist) {
			PCAQvalueDropIfExist = pCAQvalueDropIfExist;
		}
		
		
		public String getRemainColumns() {
			return remainColumns;
		}


		public void setRemainColumns(String remainColumns) {
			this.remainColumns = remainColumns;
		}

		public AnalysisStorageParameterModel getPCAQoutputTableStorageParameters() {
			return PCAQoutputTableStorageParameters;
		}

		public void setPCAQoutputTableStorageParameters(
				AnalysisStorageParameterModel pCAQoutputTableStorageParameters) {
			PCAQoutputTableStorageParameters = pCAQoutputTableStorageParameters;
		}

		public AnalysisStorageParameterModel getPCAQvalueOutputTableStorageParameters() {
			return PCAQvalueOutputTableStorageParameters;
		}

		public void setPCAQvalueOutputTableStorageParameters(
				AnalysisStorageParameterModel pCAQvalueOutputTableStorageParameters) {
			PCAQvalueOutputTableStorageParameters = pCAQvalueOutputTableStorageParameters;
		}
}
