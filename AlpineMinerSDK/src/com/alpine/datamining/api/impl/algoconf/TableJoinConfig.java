/**
 * ClassName  TableJoinConfig
 *
 * Version information: 1.00
 *
 * Data: 2010-6-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisTableJoinModel;

/**
 * @author John Zhao
 * 
 */
public class TableJoinConfig extends DataOperationConfig {

	// private static final String Alpine_ID =TableJoinAnalyzer.Alpine_ID;

	private String createSequenceID;

	AnalysisTableJoinModel tableJoinDef;

	private static final List<String> parameterNames = new ArrayList<String>();

	private static final String Const_CreateSequenceID = "createSequenceID";

	static {

		parameterNames.add(ConstOutputType);
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstOutputTableStorageParameters);
		parameterNames.add(Const_CreateSequenceID);
	}

	public TableJoinConfig() {
		super();
		setParameterNames(parameterNames);
	}

	public AnalysisTableJoinModel getTableJoinDef() {
		return tableJoinDef;
	}

	public void setTableJoinDef(AnalysisTableJoinModel tableJoinDef) {
		this.tableJoinDef = tableJoinDef;
	}

	public String getCreateSequenceID() {
		return createSequenceID;
	}

	public void setCreateSequenceID(String createSequenceID) {
		this.createSequenceID = createSequenceID;
	}

}
