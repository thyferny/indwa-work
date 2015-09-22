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
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
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
public class PLDAContentColumnParameterHelper extends SingleColumnNameParamterHelper {
	
	 

	public PLDAContentColumnParameterHelper(){
		
	}

	// plda doc index -> int,cate,time
	// contentcolumn (pg gp oralce) -> int ,else int
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType) throws Exception {
		List<String> avaliableValues = new ArrayList<String>();
		Operator operator = parameter.getOperator();
		String paramName = parameter.getName();

		String schema = null;
		String table = null;
	 
		List<String[]> columns = null;
		String dataSystem =null;
		List<OperatorInputTableInfo> parantDBTables = operator
				.getParentDBTableSet();
		if (parantDBTables != null &&parantDBTables.size()>0) {
			OperatorInputTableInfo tableInfo = parantDBTables.get(0);
			schema = tableInfo.getSchema();
			table = tableInfo.getTable();
			columns =tableInfo.getFieldColumns() ;
			dataSystem =tableInfo.getSystem(); 

		}

		if (columns==null||StringUtil.isEmpty(table) || StringUtil.isEmpty(schema)	 ) {
			return avaliableValues;
		}
	 	 

		// avaliableValues= getColumnNames(tableColumns,
		// OperatorParameter.Column_Type_ALL );

		for (String[] column : columns) {
			String datatype = column[1];

			if (OperatorParameter.NAME_contentDocIndexColumn.equals(paramName)
					&&(isTypeOK(OperatorParameter.Column_Type_Int, datatype, dataSystem)
							||isTypeOK(OperatorParameter.Column_Type_Category, datatype, dataSystem)
							||isTypeOK(OperatorParameter.Column_Type_DateAndTime, datatype, dataSystem)
							)) {
				if(false==isTypeOK(OperatorParameter.Column_Type_AllArray, datatype,dataSystem)){
					avaliableValues.add(column[0]);
				}
			} else if (OperatorParameter.NAME_contentWordColumn.equals(paramName)){
				 
						if(dataSystem.equals(DataSourceInfoGreenplum.dBType)
						||dataSystem.equals(DataSourceInfoPostgres.dBType)
						||dataSystem.equals(DataSourceInfoOracle.dBType)) {
							if(isTypeOK(OperatorParameter.Column_Type_AllArray, datatype,dataSystem)){
								avaliableValues.add(column[0]);
							}
					}else{
						if(isTypeOK(OperatorParameter.Column_Type_Int, datatype,dataSystem)){
							avaliableValues.add(column[0]);
						}
					}
				
				}
			 
		}
		return avaliableValues;
	}
 
}
