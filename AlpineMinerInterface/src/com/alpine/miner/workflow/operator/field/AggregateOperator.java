/**
] * ClassName AggregateOperator.java
 *
 * Version information:1.00
 *
 * Date:Jun 4, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateField;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowField;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 * 
 */
public class AggregateOperator extends DataOperationOperator {
	public static final List<String> parameterNames = Arrays
			.asList(new String[] { OperatorParameter.NAME_aggregateFieldList,
					OperatorParameter.NAME_windowFieldList,
					OperatorParameter.NAME_outputType,
					OperatorParameter.NAME_outputSchema,
					OperatorParameter.NAME_outputTable,
					OperatorParameter.NAME_outputTable_StorageParams,
					OperatorParameter.NAME_dropIfExist, });

	public AggregateOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.AGGREGATE_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		AggregateFieldsModel fieldModel = null;
		WindowFieldsModel windModel = null;
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		for (OperatorParameter para : paraList) {
			if (para.getValue() instanceof String)
				continue;
			Object paraObj = para.getValue();
			if (paraObj instanceof AggregateFieldsModel) {
				fieldModel = (AggregateFieldsModel) paraObj;
			} else if (paraObj instanceof WindowFieldsModel) {
				windModel = (WindowFieldsModel) paraObj;
			}
		}
		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if (paraName.equals(OperatorParameter.NAME_aggregateFieldList)) {
				if (fieldModel == null && windModel == null) {
					invalidParameterList.add(paraName);
                    invalidParameterList.add(OperatorParameter.NAME_groupByColumn);// group by must will be invalid if fieldModel is null.
					continue;
				}
				List<AggregateField> aggFieldList = null;
				List<String> groupByList = null;
				if (fieldModel != null) {
					groupByList = fieldModel.getGroupByFieldList();
					aggFieldList = fieldModel.getAggregateFieldList();
				}

                List<WindowField> winFieldList = null;
                if (windModel != null) {
                    winFieldList = windModel.getWindowFieldList();
                }

				if (groupByList == null || groupByList.size() == 0) {
					invalidParameterList.add(OperatorParameter.NAME_groupByColumn);
                    if ((aggFieldList == null || aggFieldList.size() == 0)
                            && (winFieldList == null || winFieldList.size() == 0)) {
                        invalidParameterList.add(paraName);
                    }
					continue;
				}

				if ((aggFieldList == null || aggFieldList.size() == 0)
						&& (winFieldList == null || winFieldList.size() == 0)) {
					invalidParameterList.add(paraName);
					continue;
				}
				validateGroupByField(fieldList,invalidParameterList,OperatorParameter.NAME_groupByColumn,fieldModel); //pivotal 36368403
			} else if (paraName.equals(OperatorParameter.NAME_windowFieldList)) {
				if (fieldModel == null) {
					invalidParameterList.add(paraName);
					continue;
				}
				List<AggregateField> aggFieldList = null;
				List<String> groupByList = null;
				if (fieldModel != null) {
					aggFieldList = fieldModel.getAggregateFieldList();
					groupByList = fieldModel.getGroupByFieldList();
				}

                List<WindowField> winFieldList = null;
                if (windModel != null) {
                    winFieldList = windModel.getWindowFieldList();
                }

				if (groupByList == null || groupByList.size() == 0) {
					invalidParameterList.add(OperatorParameter.NAME_groupByColumn);
                    if ((aggFieldList == null || aggFieldList.size() == 0)
                            && (winFieldList == null || winFieldList.size() == 0)) {
                        invalidParameterList.add(paraName);
                    }
					continue;
				}

				if ((aggFieldList == null || aggFieldList.size() == 0)
						&& (winFieldList == null || winFieldList.size() == 0)) {
					invalidParameterList.add(paraName);
					continue;
				}
			} else if (paraName.equals(OperatorParameter.NAME_outputSchema)) {
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			} else if (paraName.equals(OperatorParameter.NAME_outputTable)) {
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			} else if (paraName.equals(OperatorParameter.NAME_dropIfExist)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_outputType)) {
				validateNull(invalidParameterList, paraName, paraValue);
			}
		}
		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}



	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				operatorInputTableInfo.setSchema((String) getOperatorParameter(
						OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String) getOperatorParameter(
						OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo
						.setTableType((String) getOperatorParameter(
								OperatorParameter.NAME_outputType).getValue());

				List<String[]> oldFieldColumns = operatorInputTableInfo
						.getFieldColumns();
				List<String[]> newFieldColumns = new ArrayList<String[]>();

				AggregateFieldsModel aggModel = (AggregateFieldsModel) getOperatorParameter(
						OperatorParameter.NAME_aggregateFieldList).getValue();
				if (aggModel != null) {
					List<String> groupByFieldList = aggModel
							.getGroupByFieldList();
					for (String s : groupByFieldList) {
						for (String[] fieldColumn : oldFieldColumns) {
							if (fieldColumn[0].equals(s)) {
								String[] newFieldColumn = new String[] { s,
										fieldColumn[1] };
								newFieldColumns.add(newFieldColumn);
								break;
							}
						}
					}
					List<AggregateField> aggField = aggModel
							.getAggregateFieldList();
					for (AggregateField field : aggField) {
						String[] fieldColumn = new String[] { field.getAlias(),
								field.getDataType() };
						newFieldColumns.add(fieldColumn);
					}
				}

				WindowFieldsModel winModel = (WindowFieldsModel) getOperatorParameter(
						OperatorParameter.NAME_windowFieldList).getValue();
				if (winModel != null) {
					List<WindowField> winField = winModel.getWindowFieldList();
					for (WindowField field : winField) {
						String[] fieldColumn = new String[] {
								field.getResultColumn(), field.getDataType() };
						newFieldColumns.add(fieldColumn);
					}
				}
				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(operatorInputTableInfo);
				break;
			}
		}
		return operatorInputList;
	}

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(
				opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);

		readAggregateModel(opTypeXmlManager, opNode);

		ArrayList<Node> winFieldNodeList = opTypeXmlManager.getNodeList(opNode,
				WindowFieldsModel.TAG_NAME);
		if (winFieldNodeList != null && winFieldNodeList.size() > 0) {
			WindowFieldsModel winModel = WindowFieldsModel
					.fromXMLElement((Element) winFieldNodeList.get(0));
			getOperatorParameter(OperatorParameter.NAME_windowFieldList)
					.setValue(winModel);
		}

		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);

 
		storeAggregateFielsdModel(xmlDoc, element);

		OperatorParameter windowModelParameter = ParameterUtility
				.getParameterByName(this,
						OperatorParameter.NAME_windowFieldList);
		setWindowModel(xmlDoc, element, windowModelParameter);

	}

	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}


	private void validateGroupByField(List<String> fieldList, List<String> invalidParameterList, String paraName, AggregateFieldsModel aggModel) {
		if (invalidParameterList.contains(paraName)||aggModel == null)
			return;
		List<String> groupByList = aggModel.getGroupByFieldList();

		List<String> newGroupByList = new ArrayList<String>();
		for (String s : groupByList) {
			if (fieldList.contains(s)) {
				newGroupByList.add(s);
			}
		}
		
		if(groupByList.size()!=newGroupByList.size()){
			invalidParameterList.add(paraName);
		}
	}
	private void setWindowModel(Document xmlDoc, Element element,
			OperatorParameter windowModelParameter) {
		Object value = windowModelParameter.getValue();
		if (!(value instanceof WindowFieldsModel)) {
			return;
		}
		WindowFieldsModel windowModel = (WindowFieldsModel) value;
		element.appendChild(windowModel.toXMLElement(xmlDoc));
	}

 

}
