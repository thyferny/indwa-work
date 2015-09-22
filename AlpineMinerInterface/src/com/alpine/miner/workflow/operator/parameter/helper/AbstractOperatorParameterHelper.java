/**
 * ClassName AbstractOperatorParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterDataType;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author zhaoyong
 * 
 */

public abstract class AbstractOperatorParameterHelper implements
		OperatorParameterHelper {
	// defalt value
	private String dataType = ParameterDataType.UNKNOWN;

	//
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType) throws Exception {
		// TODO :other subclass need implement this if needed
		return null;
	}
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType,Locale locale) throws Exception {
		// TODO :other subclass need implement this if needed
		return null;
	}

	@Override
	public boolean doValidate(OperatorParameter parameter) {
		// TODO :other subclass need validate the parameter can override this
		// method
		return true;
	}


	@Override
	public String getParameterLabel(String parameterName) {		
		return getParameterLabel(parameterName,Locale.getDefault());
	}
	
	@Override
	public String getParameterLabel(String parameterName,Locale locale) {		
		String label = LanguagePack.getMessage(parameterName,locale);
		if (label==null) {
			label = parameterName;
		}
		return label;
	}
	

	@Override
	public String getParameterDataType(String parameterName) {
		return dataType;
	}

	protected void setParameterDataType(String dataType) {
		this.dataType = dataType;
	}

	@Override
	public Element toXMLElement(OperatorParameter parameter) {
		return null;
	}

	@Override
	public OperatorParameter fromXMLElement(Element element) {
		return null;
	}

	protected String getParameterValueRecrusively(Operator operator,
			String paramName) {
		return getParameterValueRecrusivelyByPossibaleName(operator, Arrays
				.asList(new String[] { paramName }));
	}

	// from paren and parent's parent
	protected String getParameterValueRecrusivelyByPossibaleName(
			Operator operator, List<String> possibaleParamNames) {
		String foundParamNames = getFirstContainedParamName(operator,
				possibaleParamNames);
		if (foundParamNames != null) {
			return (String) operator.getOperatorParameter(foundParamNames)
					.getValue();
		} else {
			List<Operator> parents = operator.getParentOperators();
			for (Iterator<Operator> iterator = parents.iterator(); iterator
					.hasNext();) {
				Operator parentOperator = iterator.next();
				String parentValue = getParameterValueRecrusivelyByPossibaleName(
						parentOperator, possibaleParamNames);
				if (parentValue != null) {
					return parentValue;
				}
			}
			return null;
		}
	}

	private String getFirstContainedParamName(Operator operator,
			List<String> paramNames) {
		if (paramNames != null) {
			for (Iterator<String> iterator = paramNames.iterator(); iterator
					.hasNext();) {
				String paramName = iterator.next();

				if (operator.getParameterNames().contains(paramName)) {
					return paramName;
				}

			}
		}

		return null;
	}

	protected String getSchemaNameInOperator(Operator operator) {
		return getParameterValueRecrusivelyByPossibaleName(operator, Arrays
				.asList(new String[] { OperatorParameter.NAME_schemaName,
						OperatorParameter.NAME_outputSchema }));
	}

	//
	protected String getTableNameInOperator(Operator operator) {

		return getParameterValueRecrusivelyByPossibaleName(operator, Arrays
				.asList(new String[] { OperatorParameter.NAME_tableName,
						OperatorParameter.NAME_selectedTable,
						OperatorParameter.NAME_outputTable }));

	}

	protected String getDBConnNameInOperator(Operator operator) {
		return getParameterValueRecrusively(operator,
				OperatorParameter.NAME_dBConnectionName);
	}

	/**
	 * public static final String Column_Type_Numeric="Numeric"; public static
	 * final String Column_Type_Category="String"; public static final String
	 * Column_Type_Int="String"; public static final String
	 * Column_Type_CategoryAndInt="StringInt";
	 * 
	 * @param dbtype
	 * @throws Exception
	 * */

	protected List<String> getColumnNames(Operator operator, String columnType,
			String userName, ResourceType dbtype) throws Exception {

		List<String> result = new ArrayList<String>();

		List<String[]> list = OperatorUtility
				.getAvailableFieldColumnsList(operator);

		for (String[] name : list) {
			if (isTypeOK(operator, columnType, name[1], userName, dbtype) == true&&result.contains(name[0])==false) { 
				result.add(name[0]);
			}
		}

		return result;
	}

	/**
	 * @param operator
	 * @param columnType
	 * @param userName
	 * @param dbType
	 * @return
	 * @throws Exception
	 */
	protected boolean isTypeOK(Operator operator, String targetType,
			String thisType, String userName, ResourceType dbType)
			throws Exception {
		if (targetType == null || targetType.trim().length() == 0
				|| targetType.equals(OperatorParameter.Column_Type_ALL)) {
			return true;
		}
	
//		String dbConnName = getDBConnNameInOperator(operator);
		String dataSystem=null;
		if(operator instanceof HadoopOperator){
			dataSystem = HadoopDataType.HADOOP;
		}else{
			List<Object> operatorInputList = operator.getOperatorInputList();
			for(Object obj:operatorInputList){
				if(obj instanceof OperatorInputTableInfo){
					dataSystem=((OperatorInputTableInfo)obj).getSystem();
					break;
				}
			}
		}
		if(StringUtil.isEmpty(dataSystem)){
			return false;
		}
//		DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE
//				.getManager();
//		DbConnectionInfo dbInfo = dbManager.getDBConnection(userName,
//				dbConnName, dbType);
//
//		String dataSystem = dbInfo.getConnection().getDbType();

		return isTypeOK(targetType, thisType, dataSystem);

	}
	protected boolean isTypeOK(String targetType, String thisType,
			String dataSystem) {
		if (targetType.equals(OperatorParameter.Column_Type_Numeric)) {

			return ParameterUtility.isNumberColumnType(thisType, dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_Category)) {

			return ParameterUtility.isCategoryColumnType(thisType, dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_Int)) {

			return ParameterUtility.isIntegerColumnType(thisType, dataSystem);
		} else if (targetType
				.equals(OperatorParameter.Column_Type_CategoryAndInt)) {

			return ParameterUtility.isIntegerColumnType(thisType, dataSystem)
					|| ParameterUtility.isCategoryColumnType(thisType,
							dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_NoFloat)) {
			return ParameterUtility.isIntegerColumnType(thisType, dataSystem)
					|| !ParameterUtility.isNumberColumnType(thisType,
							dataSystem);
		} else if (targetType
				.equals(OperatorParameter.Column_Type_NoDateAndTime)) {
			return !ParameterUtility.isDateColumnType(thisType, dataSystem)
					&& !ParameterUtility.isDateTimeColumnType(thisType,
							dataSystem)
					&& !ParameterUtility.isPureDateColumnType(thisType,
							dataSystem)
					&& !ParameterUtility.isTimeColumnType(thisType, dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_DateAndTime)) {
			return ParameterUtility.isDateColumnType(thisType, dataSystem)
					|| ParameterUtility.isDateTimeColumnType(thisType,
							dataSystem)
					|| ParameterUtility.isPureDateColumnType(thisType,
							dataSystem)
					|| ParameterUtility.isTimeColumnType(thisType, dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_NoNumeric)) {
			return !ParameterUtility.isNumberColumnType(thisType, dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_NoAllArray)) {
			return !ParameterUtility.isArrayType(thisType, dataSystem)
					&& !ParameterUtility.isArrayArrayType(thisType, dataSystem);
		} else if (targetType.equals(OperatorParameter.Column_Type_AllArray)) {
			return ParameterUtility.isArrayType(thisType, dataSystem)
					|| ParameterUtility.isArrayArrayType(thisType, dataSystem);
		} else {
			return false;
		}

	}

}
