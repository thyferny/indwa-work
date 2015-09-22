/**
 * ClassName FlowMigrator1To3.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.db.attribute.model.customized.COUtility;
import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;
import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceItem;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.association.AssociationOperator;
import com.alpine.miner.workflow.operator.customize.CustomizedOperator;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.datasource.TableJoinOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.field.AggregateOperator;
import com.alpine.miner.workflow.operator.field.HistogramOperator;
import com.alpine.miner.workflow.operator.field.ReplaceNullOperator;
import com.alpine.miner.workflow.operator.field.VariableOperator;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator;
import com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateField;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowField;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBin;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayer;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayersModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementItem;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinTable;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldItem;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class FlowMigrator3To23 extends AbstractFlowMigrator {
    private static final Logger itsLogger=Logger.getLogger(FlowMigrator3To23.class);

    public static final FlowMigrator INSTANCE = new FlowMigrator3To23();

	@Override
	public List<OperatorParameter> doReadOperatorMigrator(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		return null;
	}

	@Override
	public void doSaveOperatorMigrator(Operator operator, Document xmlDoc,
			Element element, String username, boolean addSuffixToOutput) {
		saveSimpleParameters(operator, xmlDoc, element, username);

		if (operator instanceof DbTableOperator) {
			Map<String, String> paraMap = ((DbTableOperator) operator)
					.refreshTableInfo();
			if (paraMap == null)
				return;
			createSimpleElements(xmlDoc, element, paraMap.get("userName"),
					"userName");
			createSimpleElements(xmlDoc, element, paraMap.get("url"), "url");
			createSimpleElements(xmlDoc, element, paraMap.get("password"),
					"password");
			createSimpleElements(xmlDoc, element, paraMap.get("system"),
					"system");
		} else if (operator instanceof AggregateOperator) {
			OperatorParameter fieldsModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_aggregateFieldList);
			setFieldModel(xmlDoc, element, addSuffixToOutput,
					fieldsModelParameter);

			OperatorParameter windowModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_windowFieldList);
			setWindowModel(xmlDoc, element, addSuffixToOutput,
					windowModelParameter);
				
		} else if (operator instanceof TableJoinOperator) {
			OperatorParameter tableJoinParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_Set_Table_Join_Parameters);

			setJoinModel(operator, xmlDoc, element, tableJoinParameter,
					username, addSuffixToOutput);
		} else if (operator instanceof VariableOperator) {
			OperatorParameter quantileModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_quantileFieldList);

			if (quantileModelParameter != null
					&& quantileModelParameter.getValue() != null) {
				element
						.appendChild(((QuantileFieldsModel) quantileModelParameter
								.getValue()).toXMLElement(xmlDoc));
			}
			OperatorParameter derivedFieldsModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_fieldList);

			setDeriveFieldsModel(xmlDoc, element, derivedFieldsModelParameter);
		} else if (operator instanceof LinearRegressionOperator) {
			OperatorParameter interModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_Interaction_Columns);

			Object value = interModelParameter.getValue();
			if (!(value instanceof InterActionColumnsModel)) {
				return;
			}
			InterActionColumnsModel interActionModel = (InterActionColumnsModel) value;
			element.appendChild(interActionModel.toXMLElement(xmlDoc));
		} else if (operator instanceof LogisticRegressionOperator) {
			OperatorParameter interModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_Interaction_Columns);

			Object value = interModelParameter.getValue();
			if (!(value instanceof InterActionColumnsModel)) {
				return;
			}
			InterActionColumnsModel interActionModel = (InterActionColumnsModel) value;
			element.appendChild(interActionModel.toXMLElement(xmlDoc));
		} else if (operator instanceof NeuralNetworkOperator) {
			OperatorParameter nnModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_hidden_layers);

			setNNModel(xmlDoc, element, nnModelParameter);
		} else if (operator instanceof SQLExecuteOperator) {
			setSQLExecuteModel(operator, xmlDoc, element);
		} else if (operator instanceof CustomizedOperator) {
			String operatorName = ((CustomizedOperator) operator)
					.getOperatorName();
			CustomizedOperatorModel model = ((CustomizedOperator) operator)
					.getCoModel();

			String udfschema = model.getUdfSchema();
			String udfName = model.getUdfName();

			element.setAttribute(COUtility.OPERATOR_NAME, operatorName);
			element.setAttribute("udfschema", udfschema);
			element.setAttribute("udfName", udfName);
			
			HashMap<String, String> outputMap=model.getOutputColumnMap();
			Iterator<Map.Entry<String,String>> iter=outputMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, String>  entry=iter.next();
				Element parameter = xmlDoc.createElement("outputColumns");
				parameter.setAttribute("column", entry.getKey());
				parameter.setAttribute("type", entry.getValue());
				createParameterElement(xmlDoc, element, "column", entry.getKey());
				createParameterElement(xmlDoc, element, "type", entry.getValue());
			}
			
		} else if (operator instanceof HistogramOperator) {
			OperatorParameter hisModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_Columns_Bins);

			setHisModel(xmlDoc, element, hisModelParameter);
		} else if (operator instanceof AdaboostOperator) {
			OperatorParameter adaboostParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_adaboostUIModel);

			setAdaBoostModel(operator, xmlDoc, element, adaboostParameter,
					username, addSuffixToOutput);
		} else if (operator instanceof ReplaceNullOperator) {
			OperatorParameter repModelParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_replacement_config);

			setRepModel(xmlDoc, element, repModelParameter);
		} else if (operator instanceof AssociationOperator) {
			OperatorParameter expParameter = ParameterUtility
					.getParameterByName(operator,
							OperatorParameter.NAME_expression);
			setExpModel(xmlDoc, element, expParameter);
		}
	}

	private void setExpModel(Document xmlDoc, Element element,
			OperatorParameter expParameter) {
		Object value = expParameter.getValue();
		if (!(value instanceof ExpressionModel)) {
			return;
		}
		ExpressionModel expressionModel = (ExpressionModel) value;

		createParameterElement(xmlDoc, element, "expression", expressionModel
				.getPositiveValue());
		createParameterElement(xmlDoc, element, "positiveValue",
				expressionModel.getExpression());
	}

	private void setRepModel(Document xmlDoc, Element element,
			OperatorParameter repModelParameter) {
		Object value = repModelParameter.getValue();
		if (!(value instanceof NullReplacementModel)) {
			return;
		}
		NullReplacementModel replacementModel = (NullReplacementModel) value;

		List<NullReplacementItem> replacementItems = replacementModel
				.getNullReplacements();

		StringBuilder sbColumn = new StringBuilder();
		StringBuilder sbRep = new StringBuilder();
		for (NullReplacementItem replacementItem : replacementItems) {
			sbColumn.append(replacementItem.getColumnName()).append(",");
			sbRep.append(replacementItem.getValue()).append(",");
		}
		if (sbColumn.length() > 0) {
			sbColumn = sbColumn.deleteCharAt(sbColumn.length() - 1);
		}
		if (sbRep.length() > 0) {
			sbRep = sbRep.deleteCharAt(sbRep.length() - 1);
		}

		createParameterElement(xmlDoc, element, "columnNames", sbColumn
				.toString());
		createParameterElement(xmlDoc, element, "replacement", sbRep.toString());
	}

	private void setAdaBoostModel(Operator operator, Document xmlDoc,
			Element element, OperatorParameter adaboostParameter,
			String username, boolean addSuffixToOutput) {
		if (adaboostParameter != null && adaboostParameter.getValue() != null) {
			AdaboostPersistenceModel adaboostModel=(AdaboostPersistenceModel)adaboostParameter.getValue();
			List<AdaboostPersistenceItem>  adaBoostModels=adaboostModel.getAdaboostUIItems();
			for(AdaboostPersistenceItem item:adaBoostModels){
				String adaType=item.getAdaType();
				if(adaType.indexOf(".workflow.operator.")>0){
					adaType=adaType.replace(".workflow.operator.", ".gef.runoperator.");
				}
				item.setAdaType(adaType);
			}
			
			element.appendChild(((AdaboostPersistenceModel) adaboostParameter
					.getValue()).toXMLElement(xmlDoc));
		}
	}

	private void setHisModel(Document xmlDoc, Element element,
			OperatorParameter hisModelParameter) {
		Object value = hisModelParameter.getValue();
		if (!(value instanceof ColumnBinsModel)) {
			return;
		}
		ColumnBinsModel columnBinsModel = (ColumnBinsModel) value;

		List<ColumnBin> columnBins = columnBinsModel.getColumnBins();

		StringBuilder sbColumn = new StringBuilder();
		StringBuilder sbBin = new StringBuilder();
		for (ColumnBin columnBin : columnBins) {
			sbColumn.append(columnBin.getColumnName()).append(",");
			sbBin.append(columnBin.getBin()).append(",");
		}
		if (sbColumn.length() > 0) {
			sbColumn = sbColumn.deleteCharAt(sbColumn.length() - 1);
		}
		if (sbBin.length() > 0) {
			sbBin = sbBin.deleteCharAt(sbBin.length() - 1);
		}

		createParameterElement(xmlDoc, element, "columnNames", sbColumn
				.toString());
		createParameterElement(xmlDoc, element, "bin", sbBin.toString());
	}

	private void setSQLExecuteModel(Operator operator, Document xmlDoc,
			Element element) {
		String system = null;
		String url = null;
		String connUserName = null;
		String password = null;

		DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE
				.getManager();
		List<DbConnectionInfo> dbInfos = dbManager.getDBConnectionList(System
				.getProperty("user.name"));

		for (DbConnectionInfo dbInfo : dbInfos) {
			DbConnection conn = dbInfo.getConnection();
			String connName = (String) operator.getOperatorParameter(
					OperatorParameter.NAME_dBConnectionName).getValue();
			if(StringUtil.isEmpty(connName)){
				return;
			}
			if (!conn.getConnName().equals(connName)) {
				continue;
			}
			system = conn.getDbType();
			url = conn.getUrl();
			connUserName = conn.getDbuser();
			password = conn.getPassword();
			break;
		}

		Element parameter1 = xmlDoc.createElement("Parameter");
		parameter1.setAttribute("key", OperatorParameter.NAME_URL);
		parameter1.setAttribute("value", url);
		element.appendChild(parameter1);
		Element parameter2 = xmlDoc.createElement("Parameter");
		parameter2.setAttribute("key", OperatorParameter.NAME_System);
		parameter2.setAttribute("value", system);
		element.appendChild(parameter2);
		Element parameter3 = xmlDoc.createElement("Parameter");
		parameter3.setAttribute("key", OperatorParameter.NAME_UserName);
		parameter3.setAttribute("value", connUserName);
		element.appendChild(parameter3);
		Element parameter4 = xmlDoc.createElement("Parameter");
		parameter4.setAttribute("key", OperatorParameter.NAME_Password);
		parameter4.setAttribute("value", XmlDocManager
				.encryptedPassword(password));
		element.appendChild(parameter4);
	}

	private void setWindowModel(Document xmlDoc, Element element,
			boolean addSuffixToOutput, OperatorParameter windowModelParameter) {
		Object value = windowModelParameter.getValue();
		if (!(value instanceof WindowFieldsModel)) {
			return;
		}
		WindowFieldsModel windowModel = (WindowFieldsModel) value;
		List<WindowField> windowFieldsList = windowModel.getWindowFieldList();
		List<String> dataTypeList = new ArrayList<String>();
		List<String> fieldNameList = new ArrayList<String>();
		List<String> funList = new ArrayList<String>();
		List<String> specList = new ArrayList<String>();
		for (WindowField winField : windowFieldsList) {
			dataTypeList.add(winField.getDataType());
			fieldNameList.add(winField.getResultColumn());
			funList.add(winField.getWindowFunction());
			specList.add(winField.getWindowSpecification());
		}
		String windowDataTypeList = connectString(dataTypeList, connString);
		String windowFieldList = connectString(fieldNameList, connString);
		String windowFunctionList = connectString(funList, connString);
		String windowSpecList = connectString(specList, connString);
		createParameterElement(xmlDoc, element, "windowDataTypeList",
				windowDataTypeList);
		createParameterElement(xmlDoc, element, "windowFieldList",
				windowFieldList);
		createParameterElement(xmlDoc, element, "windowFunctionList",
				windowFunctionList);
		createParameterElement(xmlDoc, element, "windowSpecList",
				windowSpecList);
	}

	private void setFieldModel(Document xmlDoc, Element element,
			boolean addSuffixToOutput, OperatorParameter fieldsModelParameter) {
		Object value = fieldsModelParameter.getValue();
		if (!(value instanceof AggregateFieldsModel)) {
			return;
		}
		AggregateFieldsModel aggModel = (AggregateFieldsModel) value;
		List<AggregateField> aggFieldsList = aggModel.getAggregateFieldList();
		List<String> aggFieldNameList = new ArrayList<String>();
		List<String> aggExpList = new ArrayList<String>();
		List<String> addDataTypeList=new ArrayList<String>();
		for (AggregateField aggField : aggFieldsList) {
			aggFieldNameList.add(aggField.getAlias());
			aggExpList.add(aggField.getAggregateExpression());
			addDataTypeList.add(aggField.getDataType());
		}
		List<String> groupByList = aggModel.getGroupByFieldList();
		List<String> parentList = aggModel.getParentFieldList();
		String groupByFieldList = connectString(groupByList, connString);
		String parentFieldList = connectString(parentList, connString);
		String aggregateFieldList = connectString(aggFieldNameList, connString);
		String aggregateExpressionList = connectString(aggExpList, connString);
		String aggDataTypeList = connectString(addDataTypeList, connString);
		createParameterElement(xmlDoc, element, "aggregateFieldList",
				aggregateFieldList);
		createParameterElement(xmlDoc, element, "aggregateExpressionList",
				aggregateExpressionList);
		createParameterElement(xmlDoc, element, "groupByFieldList",
				groupByFieldList);
		createParameterElement(xmlDoc, element, "parentFieldList",
				parentFieldList);
		createParameterElement(xmlDoc, element, "aggregateDataTypeList",
				aggDataTypeList);
	}

	private void setJoinModel(Operator operator, Document xmlDoc,
			Element element, OperatorParameter tableJoinParameter,
			String username, boolean addSuffixToOutput) {
		if (tableJoinParameter != null && tableJoinParameter.getValue() != null) {
			if (addSuffixToOutput) {
				TableJoinModel tjd = (TableJoinModel) tableJoinParameter
						.getValue();
				TableJoinModel tjdNew = null;
				try {
					tjdNew = (TableJoinModel) tjd.clone();
				} catch (CloneNotSupportedException e) {
					itsLogger.error(e.getMessage(),e);
				}
				if (tjd != null && tjdNew != null) {
					HashMap<String, Boolean> isParentDbTableMap = new HashMap<String, Boolean>();
					List<UIOperatorModel> opParentsModelList = new ArrayList<UIOperatorModel>();
					List<UIConnectionModel> connList = operator.getOperModel()
							.getSourceConnection();
					for (UIConnectionModel connModel : connList) {
						opParentsModelList.add(connModel.getSource());
					}
					for (UIOperatorModel opModel : opParentsModelList) {
						List<OperatorParameter> operatorParameterList = opModel
								.getOperator().getOperatorParameterList();
						for (OperatorParameter operatorParameter : operatorParameterList) {
							String key = operatorParameter.getName();
							Object obj = operatorParameter.getValue();
							if (!(obj instanceof String))
								continue;
							String value = (String) operatorParameter
									.getValue();
							if (key.equals(XmlDocManager.TABLE_NAME)) {
								isParentDbTableMap.put(value, true);
								break;
							}
							if (key.equals(XmlDocManager.SELECTED_OUTPUT_TABLE)) {
								String[] temp = value.split("\\.");
								value = temp[1];
							}
							isParentDbTableMap.put(value, false);
						}
					}

					List<JoinTable> jtModelList = tjdNew.getJoinTables();
					Iterator<JoinTable> iter = jtModelList.iterator();
					while (iter.hasNext()) {
						JoinTable jtModel = iter.next();
						if (isParentDbTableMap.get(jtModel.getTable()) != null
								&& !isParentDbTableMap.get(jtModel.getTable())) {
							jtModel.setTable(StringHandler.addPrefix(jtModel
									.getTable(), username));
						}
					}
				}
				element.appendChild(((TableJoinModel) tableJoinParameter
						.getValue()).toXMLElement(xmlDoc));
			} else {
				element.appendChild(((TableJoinModel) tableJoinParameter
						.getValue()).toXMLElement(xmlDoc));
			}
		}
	}

	private void setDeriveFieldsModel(Document xmlDoc, Element element,
			OperatorParameter derivedFieldsModelParameter) {
		Object value = derivedFieldsModelParameter.getValue();
		if (!(value instanceof DerivedFieldsModel)) {
			return;
		}
		DerivedFieldsModel dfModel = (DerivedFieldsModel) value;
		List<DerivedFieldItem> derivedFieldList = dfModel
				.getDerivedFieldsList();
		List<String> varDataTypeList = new ArrayList<String>();
		List<String> fieldNameList = new ArrayList<String>();
		List<String> expList = new ArrayList<String>();
		List<String> selectedFieldList = dfModel.getSelectedFieldList();
		for (DerivedFieldItem derivedField : derivedFieldList) {
			varDataTypeList.add(derivedField.getDataType());
			fieldNameList.add(derivedField.getResultColumnName());
			expList.add(derivedField.getSqlExpression());
		}
		String fieldList = connectString(fieldNameList, connString);
		String dataTypeList = connectString(varDataTypeList, connString);
		String expressionList = connectString(expList, connString);
		String selectedlist = connectString(selectedFieldList, ",");
		createParameterElement(xmlDoc, element, "fieldList", fieldList);
		createParameterElement(xmlDoc, element, "dataTypeList", dataTypeList);
		createParameterElement(xmlDoc, element, "expressionList",
				expressionList);
		createParameterElement(xmlDoc, element, "selectedFieldList",
				selectedlist);
	}

	private void setNNModel(Document xmlDoc, Element element,
			OperatorParameter nnModelParameter) {
		Object value = nnModelParameter.getValue();
		if (!(value instanceof HiddenLayersModel)) {
			return;
		}
		HiddenLayersModel hiddenLayersModel = (HiddenLayersModel) value;
		List<HiddenLayer> hiddenLayerList = hiddenLayersModel.getHiddenLayers();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hiddenLayerList.size(); i++) {
			String layerName = hiddenLayerList.get(i).getLayerName();
			String layerSize = String.valueOf(hiddenLayerList.get(i)
					.getLayerSize());
			sb.append(layerName).append(",").append(layerSize);
			if (i != hiddenLayerList.size() - 1) {
				sb.append(";");
			}
		}
		createParameterElement(xmlDoc, element, "hidden_layers", sb.toString());
	}
}
