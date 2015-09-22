/**
* ClassName PLDAImpl.java
*
* Version information: 1.00
*
* Data: 2012-2-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.trainer.PLDA;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import org.apache.log4j.Logger;
/**
 * @author Shawn
 *
 */
public abstract class PLDAImpl {
    private AnalysisStorageParameterModel PLDAModelOutputTableStorageParameters;
	private AnalysisStorageParameterModel topicOutTableStorageParameters;
  	private AnalysisStorageParameterModel docTopicOutTableStorageParameters;


	String PLDAPreString="PLDA";
	String PLDANoArrayPreString="PL";
	public abstract void pldaTrain(Connection conncetion, Statement st,
			String dicSchema, String dicTable, String dicIndexColumn,
			String dicContentColumn, String contentSchema, String contentTable,
			String contentColumn, String contentIDColumn, long timeStamp, String dropIfExist, 
			double alpha, double beta, String modelOutSchema, String modelOutTable,long topicnumber
			,long iterationNumber,String topicOutSchema,String topicOutTable, String topicTableDropIfExists
			,String docTopicTableDropIfExists,String docTopicOutSchema,String docTopicOutTable) throws SQLException ;
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