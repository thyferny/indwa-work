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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceItem;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.association.AssociationOperator;
import com.alpine.miner.workflow.operator.customize.CustomizedOperator;
import com.alpine.miner.workflow.operator.datasource.TableJoinOperator;
import com.alpine.miner.workflow.operator.field.AggregateOperator;
import com.alpine.miner.workflow.operator.field.HistogramOperator;
import com.alpine.miner.workflow.operator.field.ReplaceNullOperator;
import com.alpine.miner.workflow.operator.field.VariableOperator;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator;
import com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.OperatorParameterImpl;
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
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinCondition;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldItem;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.miner.workflow.operator.sampling.AbstractSamplingOperator;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class FlowMigrator23To3 extends AbstractFlowMigrator {

	public static final FlowMigrator INSTANCE = new FlowMigrator23To3();

	@Override
	public List<OperatorParameter> doReadOperatorMigrator(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		if (operator instanceof AggregateOperator) {
			return setAggregateParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof TableJoinOperator) {
			return setTableJoinParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof VariableOperator) {
			return setVariableParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof LinearRegressionOperator) {
			return setLinearRegressionParameters(operator, opTypeXmlManager,
					element);
		} else if (operator instanceof LogisticRegressionOperator) {
			return setLogisticRegressionParameters(operator, opTypeXmlManager,
					element);
		} else if (operator instanceof NeuralNetworkOperator) {
			return setNNParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof HistogramOperator) {
			return setHistogramParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof AdaboostOperator) {
			return setAdaboostParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof ReplaceNullOperator) {
			return setReplaceNullParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof AssociationOperator) {
			return setAssociationParameters(operator, opTypeXmlManager, element);
		} else if (operator instanceof AbstractSamplingOperator) {
			return setSamplingParameters(operator, opTypeXmlManager, element);
		}else {
			return setSimpleParameters(operator, opTypeXmlManager, element);
		}
	}

	private List<OperatorParameter> setSamplingParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		String sampleSize = null;
		String sampleCount = null;

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals(OperatorParameter.NAME_sampleSize)) {
				sampleSize = paraValue;
			} else if(paraName.equals(OperatorParameter.NAME_sampleCount)){
				sampleCount = paraValue;
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}
		}
		OperatorParameter operatorParameter = new OperatorParameterImpl(
				operator, OperatorParameter.NAME_sampleSize);

		List<String> sampleIdList = new ArrayList<String>();
		List<String> sampleSizeList = new ArrayList<String>();
		
		if(StringUtil.isEmpty(sampleCount)==false&&
				AlpineUtil.isInteger(sampleCount)
				&&StringUtil.isEmpty(sampleSize)==false
				&&AlpineUtil.isNumber(sampleSize)){
			int sampleCountInt = Integer.parseInt(sampleCount);
			for(int i=0;i<sampleCountInt;i++){
				sampleIdList.add(String.valueOf(i+1));
				sampleSizeList.add(String.valueOf(Double.parseDouble(sampleSize)/sampleCountInt));
			}
		}
		SampleSizeModel model=new SampleSizeModel(sampleIdList, sampleSizeList);
		
		operatorParameter.setValue(model);
		

		operatorParameter.setOperator(operator);
		paraMap.put(OperatorParameter.NAME_sampleSize, operatorParameter);

		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setAssociationParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		String expression = null;
		String positiveValue = null;

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals("expression")) {
				expression = paraValue;
			} else if (paraName.equals("positiveValue")) {
				positiveValue = paraValue;
			} else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}
		}
		OperatorParameter operatorParameter = new OperatorParameterImpl(
				operator, OperatorParameter.NAME_expression);

		if (!StringUtil.isEmpty(expression)
				&& !StringUtil.isEmpty(positiveValue)) {
			ExpressionModel expModel = new ExpressionModel(positiveValue,
					expression);
			operatorParameter.setValue(expModel);
		} else {
			operatorParameter.setValue(null);
		}

		operatorParameter.setOperator(operator);
		paraMap.put(OperatorParameter.NAME_expression, operatorParameter);

		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setReplaceNullParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");

		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		String columnNames = null;
		String replacement = null;

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals("columnNames")) {
				columnNames = paraValue;
			} else if (paraName.equals("replacement")) {
				replacement = paraValue;
			} else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}
		}
		OperatorParameter operatorParameter = new OperatorParameterImpl(
				operator, OperatorParameter.NAME_replacement_config);

		if (!StringUtil.isEmpty(columnNames)
				&& !StringUtil.isEmpty(replacement)) {
			String[] tempColumnNames = columnNames.split(",");
			String[] tempReplacement = replacement.split(",");
			NullReplacementModel replacmentsModel = new NullReplacementModel();
			for (int i = 0; i < tempColumnNames.length; i++) {
				NullReplacementItem replacementItem = new NullReplacementItem(
						tempColumnNames[i], tempReplacement[i],"");
				replacmentsModel.addNullReplacement(replacementItem);
			}
			operatorParameter.setValue(replacmentsModel);
		} else {
			operatorParameter.setValue(null);
		}

		operatorParameter.setOperator(operator);
		paraMap.put(OperatorParameter.NAME_replacement_config,
				operatorParameter);

		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setAdaboostParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			setSimpleParametersValue(operator, operatorParameters, paraName,
					paraValue, paraMap);
		}
		ArrayList<Node> adaboostNodeList = opTypeXmlManager.getNodeList(
				(Node) element, AdaboostPersistenceModel.TAG_NAME);
		if (adaboostNodeList != null && adaboostNodeList.size() > 0) {
			Element adaboostElement = (Element) adaboostNodeList.get(0);
			AdaboostPersistenceModel adaboostModel = AdaboostPersistenceModel
					.fromXMLElement(adaboostElement);
			List<AdaboostPersistenceItem> adaBoostModels = adaboostModel
					.getAdaboostUIItems();
			for (AdaboostPersistenceItem item : adaBoostModels) {
				String adaType = item.getAdaType();
				if (adaType.indexOf(".gef.runoperator.") > 0) {
					adaType = adaType.replace(".gef.runoperator.",
							".workflow.operator.");
				}
				item.setAdaType(adaType);
			}
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_adaboostUIModel);
			operatorParameter.setValue(adaboostModel);
			operatorParameter.setOperator(operator);
			paraMap.put(OperatorParameter.NAME_adaboostUIModel,
					operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setHistogramParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");

		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		String columnNames = null;
		String bins = null;

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals("columnNames")) {
				columnNames = paraValue;
			} else if (paraName.equals("bin")) {
				bins = paraValue;
			}
		}
		OperatorParameter operatorParameter = new OperatorParameterImpl(
				operator, OperatorParameter.NAME_Columns_Bins);

		if (!StringUtil.isEmpty(columnNames) && !StringUtil.isEmpty(bins)) {
			String[] tempColumnNames = columnNames.split(",");
			String[] tempBins = bins.split(",");
			ColumnBinsModel columnBinsModel = new ColumnBinsModel();
			for (int i = 0; i < tempColumnNames.length; i++) {
				ColumnBin columnBin = new ColumnBin(tempColumnNames[i], tempBins[i]);
				columnBinsModel.addColumnBin(columnBin);
			}
			operatorParameter.setValue(columnBinsModel);
		} else {
			operatorParameter.setValue(null);
		}

		operatorParameter.setOperator(operator);
		paraMap.put(OperatorParameter.NAME_Columns_Bins, operatorParameter);

		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setSimpleParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");

			if (operator instanceof CustomizedOperator
					&& ((CustomizedOperator) operator).getCoModel()
							.hasParamPosition(paraName) == true) {
				paraName = ((CustomizedOperator) operator).getCoModel()
						.getParamNameByPosition(paraName);

				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);

			} else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}
		}
		setParameters(operator, operatorParameters, paraMap);
		return operatorParameters;
	}

	private List<OperatorParameter> setNNParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");

		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		String hidden_layers = null;
		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals("hidden_layers")) {
				hidden_layers = paraValue;
			} else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}
		}
		if (!StringUtil.isEmpty(hidden_layers)) {
			List<HiddenLayer> hiddenLayers = new ArrayList<HiddenLayer>();
			String[] hiddenLayersArray = hidden_layers.split(";");
			for (String s : hiddenLayersArray) {
				String[] temp = s.split(",");
				HiddenLayer hiddenLayer = new HiddenLayer(temp[0], temp[1]);
				hiddenLayers.add(hiddenLayer);
			}
			HiddenLayersModel hiddenLayersModel = new HiddenLayersModel();
			hiddenLayersModel.setHiddenLayers(hiddenLayers);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_hidden_layers);
			operatorParameter.setValue(hiddenLayersModel);
			operatorParameter.setOperator(operator);
			paraMap
					.put(OperatorParameter.NAME_hidden_layers,
							operatorParameter);
			// operatorParameters.add(operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setLogisticRegressionParameters(
			Operator operator, XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");

		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			setSimpleParametersValue(operator, operatorParameters, paraName,
					paraValue, paraMap);
		}
		ArrayList<Node> interActionNodeList = opTypeXmlManager.getNodeList(
				(Node) element, InterActionColumnsModel.TAG_NAME);
		if (interActionNodeList != null && interActionNodeList.size() > 0) {
			Element interActionElement = (Element) interActionNodeList.get(0);
			InterActionColumnsModel interActionModel = InterActionColumnsModel
					.fromXMLElement(interActionElement);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_Interaction_Columns);
			operatorParameter.setValue(interActionModel);
			operatorParameter.setOperator(operator);
			paraMap.put(OperatorParameter.NAME_Interaction_Columns,
					operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setLinearRegressionParameters(
			Operator operator, XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");

		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			setSimpleParametersValue(operator, operatorParameters, paraName,
					paraValue, paraMap);
		}
		ArrayList<Node> interActionNodeList = opTypeXmlManager.getNodeList(
				(Node) element, InterActionColumnsModel.TAG_NAME);
		if (interActionNodeList != null && interActionNodeList.size() > 0) {
			Element interActionElement = (Element) interActionNodeList.get(0);
			InterActionColumnsModel interActionModel = InterActionColumnsModel
					.fromXMLElement(interActionElement);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_Interaction_Columns);
			operatorParameter.setValue(interActionModel);
			operatorParameter.setOperator(operator);
			paraMap.put(OperatorParameter.NAME_Interaction_Columns,
					operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setVariableParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		HashMap<String, String> deriveFieldMap = new HashMap<String, String>();
		String selectedField = null;
		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals("fieldList")
					|| paraName.equals("expressionList")
					|| paraName.equals("dataTypeList")) {
				deriveFieldMap.put(paraName, paraValue);
			} else if (paraName.equals("selectedFieldList")) {
				selectedField = paraValue;
			} else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}

		}

		if (!StringUtil.isEmpty(deriveFieldMap.get("fieldList"))
				&& !StringUtil.isEmpty(deriveFieldMap.get("expressionList"))
				&& !StringUtil.isEmpty(deriveFieldMap.get("dataTypeList"))) {
			List<DerivedFieldItem> derivedFieldsList = new ArrayList<DerivedFieldItem>();

			String deriveField = deriveFieldMap.get("fieldList");
			String expressionField = deriveFieldMap.get("expressionList");
			String dataTypeField = deriveFieldMap.get("dataTypeList");

			String[] deriveFieldArray = deriveField
					.split(com.alpine.utility.db.Resources.FieldSeparator);
			String[] expressionFieldArray = expressionField
					.split(com.alpine.utility.db.Resources.FieldSeparator);
			String[] dataTypeFieldArray = dataTypeField
					.split(com.alpine.utility.db.Resources.FieldSeparator);
			for (int i = 0; i < deriveFieldArray.length; i++) {
				DerivedFieldItem field = new DerivedFieldItem(
						deriveFieldArray[i], dataTypeFieldArray[i],
						expressionFieldArray[i]);
				derivedFieldsList.add(field);
			}
			DerivedFieldsModel dfModel = new DerivedFieldsModel();
			dfModel.setDerivedFieldsList(derivedFieldsList);
			String[] temp = selectedField.split(",");
			List<String> selectedFieldList = new ArrayList<String>();
			for (String s : temp) {
				selectedFieldList.add(s);
			}
			dfModel.setSelectedFieldList(selectedFieldList);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_fieldList);
			operatorParameter.setValue(dfModel);
			paraMap.put(OperatorParameter.NAME_fieldList, operatorParameter);
		} else if (!StringUtil.isEmpty(selectedField)) {
			String[] temp = selectedField.split(",");
			List<String> selectedFieldList = new ArrayList<String>();
			for (String s : temp) {
				selectedFieldList.add(s);
			}
			DerivedFieldsModel dfModel = new DerivedFieldsModel();
			dfModel.setSelectedFieldList(selectedFieldList);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_fieldList);
			operatorParameter.setValue(dfModel);
			paraMap.put(OperatorParameter.NAME_fieldList, operatorParameter);
		}

		ArrayList<Node> quantileNodeList = opTypeXmlManager.getNodeList(
				(Node) element, QuantileFieldsModel.TAG_NAME);
		if (quantileNodeList != null && quantileNodeList.size() > 0) {
			Element quantileElement = (Element) quantileNodeList.get(0);
			QuantileFieldsModel quantileModel = QuantileFieldsModel
					.fromXMLElement(quantileElement);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_quantileFieldList);
			operatorParameter.setValue(quantileModel);
			operatorParameter.setOperator(operator);
			paraMap.put(OperatorParameter.NAME_quantileFieldList,
					operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);
		return operatorParameters;
	}

	private List<OperatorParameter> setTableJoinParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();

		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			setSimpleParametersValue(operator, operatorParameters, paraName,
					paraValue, paraMap);
		}
		ArrayList<Node> tableJoinNodeList = opTypeXmlManager.getNodeList(
				(Node) element, TableJoinModel.TAG_NAME);
		;
		if (tableJoinNodeList != null && tableJoinNodeList.size() > 0) {
			Element tableJoinElement = (Element) tableJoinNodeList.get(0);
			TableJoinModel tableJoinModel = TableJoinModel
					.fromXMLElement(tableJoinElement);

			List<JoinCondition> conditions = tableJoinModel.getJoinConditions();
			for (JoinCondition conn : conditions) {
				String leftTable = conn.getTableAlias1();
				String rightTable = conn.getTableAlias2();
				String leftColumn = conn.getColumn1();
				String rightColumn = conn.getColumn2();
				String leftCombin = StringHandler.combinTableName(leftTable,
						leftColumn);
				String rightCombin = StringHandler.combinTableName(rightTable,
						rightColumn);
				String condition = conn.getCondition();
				if (condition.startsWith(leftCombin)) {
					condition = condition.substring(leftCombin.length(),
							condition.lastIndexOf(rightCombin));
				}
				conn.setCondition(condition);
				conn.setColumn1(leftCombin);
				conn.setColumn2(rightCombin);
			}

			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_Set_Table_Join_Parameters);
			operatorParameter.setValue(tableJoinModel);
			operatorParameter.setOperator(operator);
			paraMap.put(OperatorParameter.NAME_Set_Table_Join_Parameters,
					operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);

		return operatorParameters;
	}

	private List<OperatorParameter> setAggregateParameters(Operator operator,
			XmlDocManager opTypeXmlManager, Element element) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				(Node) element, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();
		HashMap<String, String> aggFieldMap = new HashMap<String, String>();
		HashMap<String, String> winFieldMap = new HashMap<String, String>();
		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = ((Element) parameterNode).getAttribute("value");
			if (paraName.equals("aggregateFieldList")
					|| paraName.equals("groupByFieldList")
					|| paraName.equals("aggregateExpressionList")
					|| paraName.equals("parentFieldList")
					|| paraName.equals("aggregateDataTypeList")) {
				aggFieldMap.put(paraName, paraValue);
			} else if (paraName.equals("windowSpecList")
					|| paraName.equals("windowFunctionList")
					|| paraName.equals("windowFieldList")
					|| paraName.equals("windowDataTypeList")) {
				winFieldMap.put(paraName, paraValue);
			} else {
				setSimpleParametersValue(operator, operatorParameters,
						paraName, paraValue, paraMap);
			}

		}
			String aggregateFieldList = aggFieldMap.get("aggregateFieldList");
			String groupByFieldList = aggFieldMap.get("groupByFieldList");
			String parentFieldList = aggFieldMap.get("parentFieldList");
			String aggregateDataTypeList = aggFieldMap.get("aggregateDataTypeList");
			String aggregateExpressionList = aggFieldMap
					.get("aggregateExpressionList");
			List<AggregateField> aggFieldsList = new ArrayList<AggregateField>();
			if(!StringUtil.isEmpty(aggFieldMap.get("aggregateFieldList"))&& 
					!StringUtil.isEmpty(aggFieldMap
					.get("aggregateExpressionList"))
					&&!StringUtil.isEmpty(aggFieldMap
							.get("aggregateDataTypeList"))){
				String[] aggFieldArray = aggregateFieldList
				.split(com.alpine.utility.db.Resources.FieldSeparator);
				String[] aggExpArray = aggregateExpressionList
				.split(com.alpine.utility.db.Resources.FieldSeparator);
				String[] dataTypeArray = aggregateDataTypeList.split(com.alpine.utility.db.Resources.FieldSeparator);		
				for (int j = 0; j < aggFieldArray.length; j++) {
					AggregateField field = new AggregateField(aggFieldArray[j],
							aggExpArray[j],dataTypeArray[j]);
					aggFieldsList.add(field);
				}
			}

			List<String> groupByList = new ArrayList<String>();
			if (!StringUtil.isEmpty(groupByFieldList)){
				String[] groupByArray = groupByFieldList
				.split(com.alpine.utility.db.Resources.FieldSeparator);
				for (String s : groupByArray) {
					groupByList.add(s);
				}
			}
			
			List<String> parentList = new ArrayList<String>();
			if (!StringUtil.isEmpty(parentFieldList)){
				String[] parentArray = parentFieldList
				.split(com.alpine.utility.db.Resources.FieldSeparator);
				for(String s: parentArray){
					parentList.add(s);
				}
			}

			AggregateFieldsModel aggModel = new AggregateFieldsModel(
					aggFieldsList, groupByList,parentList);
			OperatorParameter aggOperatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_aggregateFieldList);
			aggOperatorParameter.setValue(aggModel);
			paraMap.put(OperatorParameter.NAME_aggregateFieldList,
					aggOperatorParameter);
			
			
		if (!StringUtil.isEmpty(winFieldMap.get("windowSpecList"))
				&& !StringUtil.isEmpty(winFieldMap.get("windowFunctionList"))
				&& !StringUtil.isEmpty(winFieldMap.get("windowFieldList"))
				&& !StringUtil.isEmpty(winFieldMap.get("windowDataTypeList"))) {
			String windowSpecList = winFieldMap.get("windowSpecList");
			String windowFunctionList = winFieldMap.get("windowFunctionList");
			String windowFieldList = winFieldMap.get("windowFieldList");
			String windowDataTypeList = winFieldMap.get("windowDataTypeList");
			String[] windowSpecArray = windowSpecList
					.split(com.alpine.utility.db.Resources.FieldSeparator);
			String[] windowFunctionArray = windowFunctionList
					.split(com.alpine.utility.db.Resources.FieldSeparator);
			String[] windowFieldArray = windowFieldList
					.split(com.alpine.utility.db.Resources.FieldSeparator);
			String[] windowDataTypeArray = windowDataTypeList
					.split(com.alpine.utility.db.Resources.FieldSeparator);

			List<WindowField> winFieldsList = new ArrayList<WindowField>();
			for (int j = 0; j < windowSpecArray.length; j++) {
				WindowField field = new WindowField(windowFieldArray[j],
						windowFunctionArray[j], windowSpecArray[j],
						windowDataTypeArray[j]);
				winFieldsList.add(field);
			}
			WindowFieldsModel winModel = new WindowFieldsModel();
			winModel.setWindowFieldList(winFieldsList);
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					operator, OperatorParameter.NAME_windowFieldList);
			operatorParameter.setValue(winModel);
			operatorParameter.setOperator(operator);
			paraMap.put(OperatorParameter.NAME_windowFieldList,
					operatorParameter);
		}
		setParameters(operator, operatorParameters, paraMap);
		return operatorParameters;
	}

	@Override
	public void doSaveOperatorMigrator(Operator operator, Document xmlDoc,
			Element element, String username, boolean addSuffixToOutput) {
		// TODO Auto-generated method stub
	}

}
