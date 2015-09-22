/**
 * ClassName ColumnNamesParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;

/**
 * parent could be table, it is simple it also can be sampling or aggregate or
 * sth could add columns...
 * 
 * @author zhaoyong
 * 
 */
public class PLDADictColumnParameterHelper extends
		SingleColumnNameParamterHelper {
	private String columnType = null;

	public PLDADictColumnParameterHelper() {

	}

	public PLDADictColumnParameterHelper(String columnType) {
		this.columnType = columnType;
	}

	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType) throws Exception {
		List<String> avaliableValues = new ArrayList<String>();
		Operator operator = parameter.getOperator();
		String table = (String) operator.getOperatorParameter(
				OperatorParameter.NAME_dictionaryTable).getValue();
		String schema = (String) operator.getOperatorParameter(
				OperatorParameter.NAME_dictionarySchema).getValue();
		VariableModel variableModel = operator.getWorkflow()
				.getParentVariableModel();
		schema = VariableModelUtility.getReplaceValue(variableModel, schema);
		table = VariableModelUtility.getReplaceValue(variableModel, table);
		if (StringUtil.isEmpty(table) || StringUtil.isEmpty(schema)) {
			return avaliableValues;
		}
		String connName = OperatorUtility.getDBConnectionName(operator
				.getOperModel());
		if (StringUtil.isEmpty(connName)) {
			return avaliableValues;
		}
		DbConnectionInfo connInfo = DBResourceManagerFactory.INSTANCE
				.getManager().getDBConnection(userName, connName,
						ResourceType.Personal);

		String dataSystem = connInfo.getConnection().getDbType();
		List<TableColumnMetaInfo> tableColumns = DBMetadataManger.INSTANCE
				.getTableColumnInfoList(connInfo.getConnection(), schema, table);
		if (parameter.getName().equals(OperatorParameter.NAME_dicContentColumn)) {
			if (dataSystem.equals(DataSourceInfoGreenplum.dBType)
					|| dataSystem.equals(DataSourceInfoPostgres.dBType)
					|| dataSystem.equals(DataSourceInfoOracle.dBType)) {
				avaliableValues = getColumnNames(tableColumns,
						OperatorParameter.Column_Type_AllArray, connInfo
								.getConnection().getDbType());
			} else {
				avaliableValues = getColumnNames(tableColumns,
						OperatorParameter.Column_Type_ALL, connInfo
								.getConnection().getDbType());
			}

		} else {
			if (!StringUtil.isEmpty(columnType)) {
				avaliableValues = getColumnNames(tableColumns, columnType,
						connInfo.getConnection().getDbType());
			} else {
				avaliableValues = getColumnNames(tableColumns,
						OperatorParameter.Column_Type_ALL, connInfo
								.getConnection().getDbType());
			}
		}
		return avaliableValues;
	}

	private List<String> getColumnNames(List<TableColumnMetaInfo> tableColumns,
			String columnType, String dataSystem) {
		List<String> result = new ArrayList<String>();
		for (TableColumnMetaInfo column : tableColumns) {
			if (StringUtil.isEmpty(columnType)
					|| columnType.equals(OperatorParameter.Column_Type_ALL)
					|| isTypeOK(columnType, column.getColumnsType(), dataSystem))
				result.add(column.getColumnName());
		}

		return result;
	}

}
