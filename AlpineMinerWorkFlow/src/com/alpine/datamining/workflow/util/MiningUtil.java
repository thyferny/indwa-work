/**
T * ClassName  MiningUtil.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import sun.misc.BASE64Decoder;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticFactory;
import com.alpine.datamining.api.AnalyticFlow;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.AnalyticNodeSubflow;
import com.alpine.datamining.api.impl.DBTableSelectorConfig;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.ModelWrapperConfig;
import com.alpine.datamining.api.impl.algoconf.AdaboostConfig;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.algoconf.DataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.DecisionTreeConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopKMeansConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPigExecuteConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NaiveBayesConfig;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.PCAConfig;
import com.alpine.datamining.api.impl.algoconf.PLDAConfig;
import com.alpine.datamining.api.impl.algoconf.PLDAPredictConfig;
import com.alpine.datamining.api.impl.algoconf.SVDLanczosConfig;
import com.alpine.datamining.api.impl.algoconf.SVMClassificationConfig;
import com.alpine.datamining.api.impl.algoconf.SampleSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.SubFlowConfig;
import com.alpine.datamining.api.impl.algoconf.TableJoinConfig;
import com.alpine.datamining.api.impl.algoconf.TableSetConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.db.DBTableSelector;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.TableSetAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModelItem;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionSourceColumn;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinTable;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisColumnMap;
import com.alpine.datamining.api.impl.db.execute.SQLAnalyzer;
import com.alpine.datamining.api.impl.db.tablejoin.TableJoinAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopJoinAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopUnionAnalyzer;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceItem;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceModel;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceItem;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.storageparam.StorageParameterModel;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingItem;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author John Zhao
 * 
 */
public class MiningUtil {
	private static final Logger itsLogger = Logger.getLogger(MiningUtil.class);
	private static final String OP_UUID_ATTR = "uuid";
	private static final String LINK_TARGET_ATTR = "target";
	private static final String LINK_SOURCE_ATTR = "source";
	private static final String LINK_TAG = "Link";
	private static final String SubFlowOperator_CLASS = "com.alpine.miner.gef.runoperator.structual.SubFlowOperator";
	private static final String OP_TYPE_ATTR = "type";
	private static final String OP_NAME_ATTR = "name";
	private static final String OPERATOR_TAG = "Operator";
	private static final String PARAMETER = "Parameter";
	private static final String VALUE = "value";
	private static final String KEY = "key";
	public static final HashMap<String, String> operatorAnalyzerMap = new HashMap<String, String>();
	public static final HashMap<String, String> analyzerVisualTypeMap = new HashMap<String, String>();
	public static final HashMap<String, String> analyzerConfigMap = new HashMap<String, String>();
	public static final HashMap<String, String> modelVisualTypeMap = new HashMap<String, String>();
	static {
		OperatorAnalyzerMap.initOperatorAnalyzerMap(operatorAnalyzerMap); 

		AnalyzerVisualTypeMap.initAnalyzerVisualTypeMap(analyzerVisualTypeMap);// Especial For Predict Operator

		AnalyzerConfigMap.initAnalyzerConfigMap(analyzerConfigMap);

		ModelVisualTypeMap.initModelVisualTypeMap(modelVisualTypeMap);// Especial For Model Operator
	}

	/**
	 *an analyticNode can also be model...
	 * 
	 * @param analyticNodeImpl
	 * @return
	 */

	public static AnalyticProcess parseXMLFile(String filePath, Locale locale,
			boolean withSubFlow) {

		XmlDocManager xmlDocManager = new XmlDocManager();
		try {
			xmlDocManager.parseXMLFile(filePath);
			VariableModel variableModel = readVariableModel(xmlDocManager);
			// subflownode name -> varaibleMode
			HashMap<String, VariableModel> subFlowVariableMap = readSubflowVariableModel(xmlDocManager);
			// child name->parent uuid
			HashMap<String, List<String>> childParentMap = readChildParentMap(xmlDocManager);
			// subflowName -> subflowUUID (for table)
			HashMap<String, String> subflowNameUUIDMap = getSubflowUUIDNameMap(xmlDocManager);

			// Map for each operator which has a subflow parent
			// and the parentLink Map...
			return parseXMLWithVariableModel(filePath, locale, xmlDocManager,
					variableModel, withSubFlow, subFlowVariableMap,
					childParentMap, subflowNameUUIDMap);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		}
		return null;
	}

	// make it reusable for subflow
	private static AnalyticProcess parseXMLWithVariableModel(String filePath,
			Locale locale, XmlDocManager xmlManager,
			VariableModel flowVariableModel, boolean withSubflow,
			HashMap<String, VariableModel> subFlowVariableMap,
			HashMap<String, List<String>> childParentMap,
			HashMap<String, String> subflowNameUUIDMap) throws Exception {
		
		
		itsLogger.debug(
				"parseXMLWithVariableModel [filePath= " + filePath
						+ ", flowVariableModel" + flowVariableModel);
		AnalyticProcess process = AnalyticFactory.instance
				.createAnalyticProcess(filePath);
		AnalyticFlow flow = AnalyticFactory.instance.createAnalyticFlow();

		String filePathPrefix = filePath.substring(0, filePath
				.lastIndexOf(File.separator) + 1);

		/**
		 * set parent Map
		 */

		Node processNode = xmlManager.getNodeListByTag("Process").get(0);
		NamedNodeMap attributes = processNode.getAttributes();
		flow.setFlowOwnerUser(attributes.getNamedItem("UserName")
				.getNodeValue());
		flow.setFlowDescription(attributes.getNamedItem("Description")
				.getNodeValue());
		flow.setVersion(attributes.getNamedItem("Version").getNodeValue());
		/**
		 * read operator model transfer into model
		 */
		ArrayList<Node> opNodes = xmlManager.getNodeListByTag(OPERATOR_TAG); // Get

		// nodemap is used to build the link relation ship
		HashMap<String, AnalyticNode> nodeMap = new HashMap<String, AnalyticNode>();

		List<AnalyticNode> child = new ArrayList<AnalyticNode>();
		HashMap<AnalyticNodeSubflow, List<AnalyticNode>> potentialFlowNodeMap = new HashMap<AnalyticNodeSubflow, List<AnalyticNode>>();
		HashMap<AnalyticNode, VariableModel> variableModelMap = new HashMap<AnalyticNode, VariableModel>();
		for (Node opNode : opNodes) {

			AnalyticNode analyticNode = createAnalyticNode(xmlManager, opNode,
					locale, flowVariableModel, subFlowVariableMap,
					childParentMap, subflowNameUUIDMap);
			if (analyticNode == null) {
				continue;
			}
			child.add(analyticNode);
			nodeMap.put(analyticNode.getName(), analyticNode);
			boolean isSubflowNode = isSubflowNode(opNode);
			// then handle the subflow now
			if (withSubflow == false || false == isSubflowNode) {
				continue;
			}

			AnalyticNodeSubflow subFlowNode = (AnalyticNodeSubflow) analyticNode;
			itsLogger.debug(
					"parseXMLWithVariableModel [subFlowNode found:= "
							+ subFlowNode);

			VariableModel subFlowVariableModel = getSubFlowVariableModel(
					(Element) opNode, xmlManager);

			variableModelMap.put(subFlowNode, subFlowVariableModel);
			// re
			AnalyticProcess subProcess = getSubFlowProcess(subFlowNode
					.getName(), (Element) opNode, xmlManager, filePathPrefix,
					locale, subFlowVariableModel);
			// subflow is empty
			if (subProcess == null || subProcess.getFlow() == null
					|| subProcess.getFlow().getAllNodes() == null) {
				continue;
			}
			List<AnalyticNode> subFlowChildNodes = subProcess.getFlow()
					.getAllNodes();
			subFlowNode.addSubFlowChildNodes(subFlowChildNodes);
			List<AnalyticNode> potentailNodesList = new ArrayList<AnalyticNode>();

			potentialFlowNodeMap.put(subFlowNode, potentailNodesList);

			List<AnalyticNode> subFlowInputTableNodes = getSubflowInputTableNodes(subFlowChildNodes);
			for (int i = 0; i < subFlowChildNodes.size(); i++) {
				AnalyticNode subflowAnalyticNode = subFlowChildNodes.get(i);
				String exitOperatorName = subFlowNode.getSubFlowConfig()
						.getExitOperator();
				if (subflowAnalyticNode.getName().equals(exitOperatorName)) {
					AnalyticNode exitNode = getRealExitNode(subProcess,
							subflowAnalyticNode);// exitnode could be a
					// subflow...
					subFlowNode.setExitNode(exitNode);
				}

				subflowAnalyticNode.setName(subFlowNode.getName() + "_"
						+ subflowAnalyticNode.getName());
				// if not a input table
				if (subFlowInputTableNodes.contains(subflowAnalyticNode) == false) {
					// not a input table set node, should be added to the parent
					// flow
					nodeMap.put(subflowAnalyticNode.getName(),
							subflowAnalyticNode);
					child.add(subflowAnalyticNode);
				} else {
					// add to a map to judge if added with table mapping
					// (refined use the variable model)

					if (potentailNodesList.contains(subflowAnalyticNode) == false) {
						potentailNodesList.add(subflowAnalyticNode);
					}

					// or is a inputtable but not in the table join
				}

			}

		}

		flow.setAllNodes(child);

		/**
		 * read link in diagram
		 */
		ArrayList<Node> linkNodes = xmlManager.getNodeListByTag(LINK_TAG); // Get

		for (Node linkNode : linkNodes) {
			setLinks(nodeMap, linkNode);
		}

		// refine the tableMappingwithVariable
		refineTableMappingModelWithVariable(child, variableModelMap,
				flowVariableModel);

		Iterator<AnalyticNodeSubflow> it = potentialFlowNodeMap.keySet()
				.iterator();
		AnalyticNode subflowAnalyticNode;
		while (it.hasNext()) {
			AnalyticNodeSubflow subFlowNode = it.next();
			List<AnalyticNode> pNodes = potentialFlowNodeMap.get(subFlowNode);
			if (pNodes != null) {
				Iterator<AnalyticNode> itrator = pNodes.iterator();
				while (itrator.hasNext()) {
					subflowAnalyticNode = itrator.next();
					if (false == isInTableMapping(subflowAnalyticNode,
							subFlowNode.getSubFlowConfig().getTableMapping())) {
						child.add(subflowAnalyticNode);
					}
				}
			}
		}

		// after build the correct link for the subflow operator,we then handle
		// the link of subflow's nodes
		handleAllSubFlowLinks(flow);

		process.setFlow(flow);
		File file = new File(filePath);
		process.setName(file.getName());

		return process;
	}
	
	/**
	 * @param opTypeXmlManager
	 * @param opNode
	 * @return
	 * @throws Exception 
	 */

	private static AnalyticNode createAnalyticNode(
			XmlDocManager opTypeXmlManager, Node opNode, Locale locale,
			VariableModel variableModel,
			HashMap<String, VariableModel> subFlowVariableMap,
			HashMap<String, List<String>> childParentMap,
			HashMap<String, String> subflowNameUUIDMap) throws Exception {

		String operClass = opNode.getAttributes().getNamedItem(OP_TYPE_ATTR)
				.getNodeValue();
		if(operClass.equals("com.alpine.miner.gef.runoperator.structual.NoteOperator")){
			return null;
		}

		AnalyticNode analyticNode = AnalyticFactory.instance
				.createAnalyticNode();
		if (operClass.equals(SubFlowOperator_CLASS)) {
			analyticNode = new AnalyticNodeSubflow();
		}
		String operName = opNode.getAttributes().getNamedItem(OP_NAME_ATTR)
				.getNodeValue();
		analyticNode.setName(operName);

		String analyzerClass = operatorAnalyzerMap.get(operClass);
		analyticNode.setAnalyzerClass(analyzerClass);

		/**
		 * read parameter into operatorParameter
		 */
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				opNode, PARAMETER);
		HashMap<String, String> parameters = new HashMap<String, String>();
		for (Node parameterNode : parameterNodeList) {

			String paramName = ((Element) parameterNode).getAttribute(KEY);
			String paramValue = ((Element) parameterNode).getAttribute(VALUE);

			paramValue = VariableModelUtility.getReplaceValue(variableModel,
					paramValue);

			parameters.put(paramName, paramValue);
		}

		AnalyticConfiguration config = createConfig(parameters, analyticNode
				.getAnalyzerClass(), opNode, opTypeXmlManager, locale,
				variableModel, subflowNameUUIDMap, subFlowVariableMap);

		if (operClass.equals(SubFlowOperator_CLASS)) {

			ArrayList<Node> tableMappingNodeList = opTypeXmlManager
					.getNodeList(opNode, TableMappingModel.TAG_NAME);
			TableMappingModel tableMpapping = null;
			if (tableMappingNodeList != null && tableMappingNodeList.size() > 0) {
				tableMpapping = TableMappingModel
						.fromXMLElement((Element) tableMappingNodeList.get(0));
			}
			ArrayList<Node> subflowVariableNodeList = opTypeXmlManager
					.getNodeList(opNode, VariableModel.MODEL_TAG_NAME);
			VariableModel subflowVariable = null;
			if (subflowVariableNodeList != null
					&& subflowVariableNodeList.size() > 0) {
				subflowVariable = VariableModel
						.fromXMLElement((Element) subflowVariableNodeList
								.get(0));
			}

			config = new SubFlowConfig(parameters
					.get(OperatorParameter.NAME_subflowPath),
					// need refine use the parent 's vmodel ...
					toHashMap(tableMpapping, null), parameters
							.get(OperatorParameter.NAME_exitOperator),
					toHashMap(subflowVariable));

		}

		VariableModel realVariableModel = variableModel;
		if (childParentMap != null && childParentMap.containsKey(operName)) {
			List<String> parents = childParentMap.get(operName);
			if (parents != null) {
				for (String parent : parents) {
					if (subFlowVariableMap.containsKey(parent)) {
						realVariableModel = subFlowVariableMap.get(parent);
						itsLogger.debug(
								"Found realVariableModel for :" + operName
										+ " : " + realVariableModel);
						break;
					}
				}
			}
		}
		
		/**
		 * create and fill analytic source
		 */
		createAnalyticSource(opTypeXmlManager, opNode, operClass, analyticNode,
				config, realVariableModel);
		/**
		 * read model in cache
		 */
		readModel(opTypeXmlManager, opNode, config);
		
		analyticNode.setID(opNode.getAttributes().getNamedItem(OP_UUID_ATTR)
				.getNodeValue());
		return analyticNode;
	}

	private static void readModel(XmlDocManager opTypeXmlManager, Node opNode,
			AnalyticConfiguration config) throws Exception {
		ArrayList<Node> modelNodeList = opTypeXmlManager.getNodeList(opNode,
				"Model");
		if (modelNodeList != null && modelNodeList.size() > 0) {
			Node node = modelNodeList.get(0);
			String modelString = node.getTextContent();
			EngineModel model = (EngineModel) stringToObject(modelString);
			// trainer node
			if (config instanceof AbstractModelTrainerConfig) {
				((AbstractModelTrainerConfig) config).setTrainedModel(model);
			} else 	if (config instanceof ModelWrapperConfig) {// model node, need special ...
				ModelWrapperConfig modelConfig = (ModelWrapperConfig)config  ;
				if(StringUtil.isEmpty(modelConfig.getModelFilePath())==true){
					((ModelWrapperConfig) config).setTrainedModel(model);
				}else{
					((ModelWrapperConfig) config).setTrainedModel(WorkFlowUtil.loadModelFromFile(modelConfig.getModelFilePath()));
				}
			}
		}else{
			if (config instanceof ModelWrapperConfig) {// model node, need special ...
				ModelWrapperConfig modelConfig = (ModelWrapperConfig)config  ;
				if(StringUtil.isEmpty(modelConfig.getModelFilePath())==false){
					((ModelWrapperConfig) config).setTrainedModel(WorkFlowUtil.loadModelFromFile(modelConfig.getModelFilePath()));
					}
				}
		}
	}

	

	private static void createAnalyticSource(XmlDocManager opTypeXmlManager,
			Node opNode, String operClass, AnalyticNode analyticNode,
			AnalyticConfiguration config, VariableModel realVariableModel) {
		AnalyticSource source = AnalyticSourceFactory
				.createAnalyticSource(operClass);
		source.setAnalyticConfiguration(config);
	 
		Map<String, String> map = getVariableMap(realVariableModel);
		source.setSourceInfoByNodeIndex(opTypeXmlManager, opNode, 0,map);
		source.setNameAlias(analyticNode.getName());
		analyticNode.setSource(source);

		List<AnalyticSource> allSources = new ArrayList<AnalyticSource>();
		ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode,
				"InPutFieldList");
		int index = 0;
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			iterator.next();
			source = AnalyticSourceFactory.createAnalyticSource(operClass);
			source.setAnalyticConfiguration(config);
			source.setSourceInfoByNodeIndex(opTypeXmlManager, opNode, index,
					map);
			index = index + 1;
			allSources.add(source);
		}
		analyticNode.setAllSources(allSources);
	}

	private static Map<String, String> getVariableMap(
			VariableModel realVariableModel) {
		Map<String, String> map =null;
		if(realVariableModel!=null){
			map =realVariableModel.getVariableMap();
		}else{
			map = new HashMap<String,String>();
		}
		return map;
	}
	
	private static HashMap<String, String> getSubflowUUIDNameMap(
			XmlDocManager xmlDocManager) {
		HashMap<String, String> subflowNameUUIDMap = new HashMap<String, String>();
		ArrayList<Node> opNodes = xmlDocManager.getNodeListByTag(OPERATOR_TAG); // Get
		for (Node operatorNode : opNodes) {
			Element operatorElement = (Element) operatorNode;

			String operClass = operatorElement.getAttribute(OP_TYPE_ATTR);

			if (operClass.equals(SubFlowOperator_CLASS)) {
				String operName = operatorElement.getAttribute(OP_NAME_ATTR);
				String operUUID = operatorElement.getAttribute(OP_UUID_ATTR);
				subflowNameUUIDMap.put(operUUID, operName);
			}
		}

		return subflowNameUUIDMap;
	}

	private static HashMap<String, List<String>> readChildParentMap(
			XmlDocManager xmlManager) {
		HashMap<String, List<String>> childParentMap = new HashMap<String, List<String>>();
		ArrayList<Node> linkNodes = xmlManager.getNodeListByTag(LINK_TAG); // Get

		for (Node linkNode : linkNodes) {
			String source = linkNode.getAttributes().getNamedItem(
					LINK_SOURCE_ATTR).getNodeValue();
			String target = linkNode.getAttributes().getNamedItem(
					LINK_TARGET_ATTR).getNodeValue();
			List<String> parentList = childParentMap.get(target);
			if (parentList == null) {
				parentList = new ArrayList<String>();
				childParentMap.put(target, parentList);
			}
			parentList.add(source);

		}

		return childParentMap;
	}

	private static HashMap<String, VariableModel> readSubflowVariableModel(
			XmlDocManager xmlManager) {
		HashMap<String, VariableModel> subFlowVariableMap = new HashMap<String, VariableModel>();
		ArrayList<Node> opNodes = xmlManager.getNodeListByTag(OPERATOR_TAG); // Get
		for (Node operatorNode : opNodes) {
			Element operatorElement = (Element) operatorNode;
			// VariableModel variableModel =
			// VariableModel.fromXMLElement(operatorElement);
			String operName = operatorElement.getAttribute(OP_NAME_ATTR);
			String operClass = operatorElement.getAttribute(OP_TYPE_ATTR);
			if (operClass.equals(SubFlowOperator_CLASS)) {
				ArrayList<Node> variableModelNodes = xmlManager.getNodeList(
						operatorElement, VariableModel.MODEL_TAG_NAME);
				if (variableModelNodes != null && variableModelNodes.size() > 0) {
					Element variableElement = (Element) variableModelNodes
							.get(0);
					VariableModel variableModel = VariableModel
							.fromXMLElement(variableElement);
					subFlowVariableMap.put(operName, variableModel);
				}
			}
		}

		return subFlowVariableMap;
	}



	private static boolean isInTableMapping(AnalyticNode subflowAnalyticNode,
			HashMap<String, String> tableMapping) {
		if (tableMapping != null && tableMapping.keySet() != null) {
			// data base
			if (DBTableSelector.class.getName().equals(
					subflowAnalyticNode.getAnalyzerClass())) {
				String schemaTable = getOutputSchemaTable(subflowAnalyticNode);
				if (tableMapping.values().contains(schemaTable) == true) {
					return true;
				}
				
			}//now hadoop
			else if (HadoopFileSelector.class.getName().equals(
					subflowAnalyticNode.getAnalyzerClass())) {
				//code is same
				String schemaTable = getOutputSchemaTable(subflowAnalyticNode);
				if (tableMapping.values().contains(schemaTable) == true) {
					return true;
				}
			}
		}
		return false;
	}

	private static void refineTableMappingModelWithVariable(
			List<AnalyticNode> nodes,
			HashMap<AnalyticNode, VariableModel> variableModelMap,
			VariableModel flowVariableModel) {
		if (nodes == null || nodes.size() > 0) {
			for (int i = 0; i < nodes.size(); i++) {
				AnalyticNode node = nodes.get(i);
				if (isSubFlowNode(node) == true) {
					SubFlowConfig subFlowConfig = (SubFlowConfig) node
							.getSource().getAnalyticConfig();
					HashMap<String, String> subSchemaTableMap = subFlowConfig
							.getTableMapping();

					if (hasSubflowNodeParent(node)) {
						if (subSchemaTableMap != null
								&& subSchemaTableMap.keySet() != null) {
							Iterator<String> it = subSchemaTableMap.keySet()
									.iterator();
							HashMap<String, String> resultMap = new HashMap<String, String>();
							while (it.hasNext()) {

								String inputSchemaTableBeforeVaraibleRepalce = it
										.next();
								String subSchemaTable = subSchemaTableMap
										.get(inputSchemaTableBeforeVaraibleRepalce);
								VariableModel parentsubflowVariable = getRealParentsubVariable(
										node,
										inputSchemaTableBeforeVaraibleRepalce,
										variableModelMap, flowVariableModel);
								if (parentsubflowVariable != null) {
									resultMap
											.put(
													VariableModelUtility
															.getReplaceValue(
																	parentsubflowVariable,
																	inputSchemaTableBeforeVaraibleRepalce),
													subSchemaTable);
									itsLogger.debug(
											"refineTableMappingModelWithVariable [parentsubflowVariable:= "
													+ parentsubflowVariable);
								}
							}
							itsLogger.debug(
									node + "   [resultMap:= " + resultMap);
							subFlowConfig.setTableMapping(resultMap);
						}
					} else {
						itsLogger.debug(
								"refineTableMappingModelWithVariable [subSchemaTableMap:= "
										+ subSchemaTableMap
										+ ", flowVariableModel ="
										+ flowVariableModel);
						subFlowConfig.setTableMapping(refineSubSchemaTableMap(
								subSchemaTableMap, flowVariableModel));
						itsLogger.debug(
								node + "   [resultMap:= "
										+ subFlowConfig.getTableMapping());
					}

				}
			}
		}

	}

	private static VariableModel getRealParentsubVariable(AnalyticNode node,
			String inputSchemaTableBeforeVaraibleRepalce,
			HashMap<AnalyticNode, VariableModel> variableModelMap,
			VariableModel flowVariableModel) {
		if (node.getParentNodes() != null && node.getParentNodes().size() > 0) {
			for (int i = 0; i < node.getParentNodes().size(); i++) {
				AnalyticNode parentNode = node.getParentNodes().get(i);
				if (isSubFlowNode(parentNode) == true) {
					AnalyticNode exitNode = ((AnalyticNodeSubflow) parentNode)
							.getExitNode();
					if (exitNode != null && variableModelMap != null
							&& variableModelMap.values() != null) {
						Iterator<VariableModel> it = variableModelMap.values()
								.iterator();
						while (it.hasNext()) {
							VariableModel aVM = it.next();
							String replacedSchemaName = VariableModelUtility
									.getReplaceValue(aVM,
											inputSchemaTableBeforeVaraibleRepalce);
							if (replacedSchemaName
									.equals(getOutputSchemaTable(exitNode))) {
								return aVM;
							}
						}
					}
				}
			}
		}
		return flowVariableModel;
	}

	private static boolean hasSubflowNodeParent(AnalyticNode node) {
		if (node.getParentNodes() != null && node.getParentNodes().size() > 0) {
			for (int i = 0; i < node.getParentNodes().size(); i++) {
				if (isSubFlowNode(node.getParentNodes().get(i)) == true) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean isSubFlowNode(AnalyticNode node) {
		return node != null && node instanceof AnalyticNodeSubflow;

	}

	private static AnalyticNode getRealExitNode(AnalyticProcess subProcess,
			AnalyticNode subflowAnalyticNode) {
		if (isSubFlowNode(subflowAnalyticNode) == true) {
			return ((AnalyticNodeSubflow) subflowAnalyticNode).getExitNode();
		} else {
			return subflowAnalyticNode;
		}

	}

	public static boolean isSubflowNode(Node opNode) {
		return opNode.getAttributes().getNamedItem(OP_TYPE_ATTR).getNodeValue()
				.equals(SubFlowOperator_CLASS);
	}

	private static void handleAllSubFlowLinks(AnalyticFlow flow) {
		if (flow != null && flow.getAllNodes() != null
				&& flow.getAllNodes().size() > 0) {
			List<AnalyticNode> loopList = duplicateList(flow.getAllNodes());
			Iterator<AnalyticNode> iterator = loopList.iterator();
			while (iterator.hasNext()) {
				AnalyticNode node = iterator.next();
				if (isSubFlowNode(node)) {
					handleSubflowParentOperator((AnalyticNodeSubflow) node,
							flow);
					handleSubflowChildOperator((AnalyticNodeSubflow) node);
					flow.getAllNodes().remove(node);
				}
			}

		}
	}

	private static List<AnalyticNode> duplicateList(List<AnalyticNode> allNodes) {
		List<AnalyticNode> result = new ArrayList<AnalyticNode>();
		if (allNodes != null) {
			result.addAll(allNodes);
		}
		return result;
	}

	// if no table mapping ,still need keep the patrent order ...
	private static void handleSubflowParentOperator(
			AnalyticNodeSubflow subFlowNode, AnalyticFlow flow) {
		List<AnalyticNode> parentNodes = subFlowNode.getParentNodes();
		if (parentNodes != null) {

			for (int i = 0; i < parentNodes.size(); i++) {
				AnalyticNode parentNode = parentNodes.get(i);
				itsLogger.debug(
						"handleSubflowParentOperator [parentNode:= "
								+ parentNode);
				parentNode.getChildNodes().remove(subFlowNode);
				if (isSubFlowNode(parentNode)) {

					List<AnalyticNode> parentExitNodes = findExitNode((AnalyticNodeSubflow) parentNode);
					for (int j = 0; j < parentExitNodes.size(); j++) {

						handleParent(subFlowNode, parentExitNodes.get(j), flow);
					}
					itsLogger.debug(
							"handleSubflowParentOperator [parentexitNode:= "
									+ parentExitNodes);
				} else {

					handleParent(subFlowNode, parentNode, flow);
				}

			}
			// make sure the subFlowNode's parent container no node, because it
			// will be removed
			subFlowNode.getParentNodes().clear();
		}
	}

	private static void handleParent(AnalyticNodeSubflow subFlowNode,
			AnalyticNode parentNode, AnalyticFlow flow) {

		// could have multiple child...like one table link to 2 operators
		List<AnalyticNode> realChild = findRealChild4SubfowChild(subFlowNode,
				parentNode);
		if (realChild != null && realChild.size() > 0) { // handle table mapping
			for (int j = 0; j < realChild.size(); j++) {
				AnalyticNode childNode = realChild.get(j);
				// make sure the single table is in for step run...
				if (flow.getAllNodes() != null
						&& flow.getAllNodes().contains(childNode) == false) {
					flow.getAllNodes().add(childNode);
				}
				itsLogger.debug(
						"handleSubflowParentOperator [subFlowRealChild:= "
								+ childNode);
				if (TableJoinAnalyzer.class.getName().equals(
						childNode.getAnalyzerClass())) {
					handleTablejoinSubflowParent(subFlowNode.getSubFlowConfig()
							.getTableMapping(), parentNode, childNode);
				} else if (TableSetAnalyzer.class.getName().equals(
						childNode.getAnalyzerClass())) {
					handleTableSetSubflowParent(subFlowNode.getSubFlowConfig()
							.getTableMapping(), parentNode, childNode);
				} else if (HadoopJoinAnalyzer.class.getName().equals(
						childNode.getAnalyzerClass())) {
					handleHadoopJoinSubflowParent(subFlowNode.getSubFlowConfig()
							.getTableMapping(), parentNode, childNode);
				} else if (HadoopUnionAnalyzer.class.getName().equals(
						childNode.getAnalyzerClass())) {
					handleHadoopUnionSubflowParent(subFlowNode.getSubFlowConfig()
							.getTableMapping(), parentNode, childNode);
				} else {
					resetChildNodeSource(childNode, parentNode, subFlowNode
							.getSubFlowConfig().getTableMapping());

				}

				parentNode.addChildNode(childNode);

				// no need should be the same db connection
				// realChild.get(j).setSource(parentNode.getSource());
				if (childNode.getAllSources() == null) {
					childNode.setAllSources(new ArrayList<AnalyticSource>());
				}
				childNode.getAllSources().add(parentNode.getSource());
			}

		} else { // nothing found
		}
	}

	private static void handleHadoopUnionSubflowParent(
			HashMap<String, String> tableMapping, AnalyticNode parentNode,
			AnalyticNode childNode) {
		Map<String, String> newNap = createNewMapForHadoop(tableMapping);
		HadoopUnionConfig config = (HadoopUnionConfig) childNode.getSource()
				.getAnalyticConfig();
		if (config != null && config.getUnionModel() != null) {
			Map<String,String> uuidmap=new HashMap<String,String>();
			String outputFile = getHadoopOutputFile(parentNode);
			if(outputFile!=null){
				String subOutputFile = newNap.get(outputFile);
				List<AnalysisHadoopUnionFile> jointables = config.getUnionModel()
						.getUnionFiles();
				if (jointables != null && jointables.size() > 0) {
					for (int k = 0; k < jointables.size(); k++) {
						if(subOutputFile!=null&&subOutputFile.equals(jointables.get(k).getFile())){
							uuidmap.put(jointables.get(k).getOperatorModelID(), parentNode.getID());
							jointables.get(k).setOperatorModelID(parentNode.getID());
						}
					}
				}
				List<AnalysisHadoopUnionModelItem> joinColumns = config.getUnionModel().getOutputColumns();
				if (joinColumns != null && joinColumns.size() > 0) {
					for(AnalysisHadoopUnionModelItem joinColumn:joinColumns){
						List<AnalysisHadoopUnionSourceColumn> mappingColumns = joinColumn.getMappingColumns();
						if(mappingColumns!=null){
							for(AnalysisHadoopUnionSourceColumn column:mappingColumns){
								if(uuidmap.containsKey(column.getOperatorModelID())){
									column.setOperatorModelID(uuidmap.get(column.getOperatorModelID()));
								}			
							}
						}
					}
				}
			}
		}
	}

	private static void handleHadoopJoinSubflowParent(
			HashMap<String, String> tableMapping, AnalyticNode parentNode,
			AnalyticNode childNode) {
		Map<String, String> newNap = createNewMapForHadoop(tableMapping);
		HadoopJoinConfig config = (HadoopJoinConfig) childNode.getSource()
					.getAnalyticConfig();
			if (config != null && config.getJoinModel() != null) {
				Map<String,String> uuidmap=new HashMap<String,String>();
				String outputFile = getHadoopOutputFile(parentNode);
				if(outputFile!=null){
					String subOutputFile = newNap.get(outputFile);
					List<AnalysisHadoopJoinFile> jointables = config.getJoinModel()
							.getJoinTables();
					if (jointables != null && jointables.size() > 0) {
						for (int k = 0; k < jointables.size(); k++) {
							if(subOutputFile!=null&&subOutputFile.equals(jointables.get(k).getFile())){
								uuidmap.put(jointables.get(k).getOperatorModelID(), parentNode.getID());
								jointables.get(k).setOperatorModelID(parentNode.getID());
							}
						}
					}
					List<AnalysisHadoopJoinColumn> joinColumns = config.getJoinModel().getJoinColumns();
					if (joinColumns != null && joinColumns.size() > 0) {
						for(AnalysisHadoopJoinColumn joinColumn:joinColumns){
							if(uuidmap.containsKey(joinColumn.getFileId())){
								joinColumn.setFileId(uuidmap.get(joinColumn.getFileId()));
							}	
						}
					}
				}
			}
	}
	
	private static Map<String, String> createNewMapForHadoop(
			HashMap<String, String> tableMapping) {
		Map<String,String> newMap=new HashMap<String,String>();
		if(tableMapping!=null){
			Iterator<Entry<String, String>> iter = tableMapping.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, String> entry = iter.next();
				String key = entry.getKey();
				String value = entry.getValue();
				key=key.substring(1, key.length());
				value=value.substring(1, value.length());
				newMap.put(key, value);
			}
		}
		return newMap;
	}

	private static String getHadoopOutputFile(AnalyticNode parentNode){
		itsLogger.debug(
				"getOutputFile [parentNode= " + parentNode);
		AnalyticSource source = parentNode.getSource();
		if (source.getAnalyticConfig() instanceof HadoopFileSelectorConfig) {
			HadoopFileSelectorConfig config = (HadoopFileSelectorConfig) source
					.getAnalyticConfig();
			return config.getHadoopFileName();
		}else if (source.getAnalyticConfig() instanceof HadoopDataOperationConfig) {
			HadoopDataOperationConfig config = (HadoopDataOperationConfig) source
					.getAnalyticConfig();
			String location = config.getResultsLocation();
			String name = config.getResultsName();
			if(StringUtil.isEmpty(location)==false&&StringUtil.isEmpty(name)){
				if(location.endsWith(HadoopFile.SEPARATOR)==false){
					location=location+HadoopFile.SEPARATOR;
				}
				String file=location+name;
				return file;
			}
		}else if (source.getAnalyticConfig() instanceof HadoopKMeansConfig) {
			HadoopKMeansConfig config = (HadoopKMeansConfig) source
					.getAnalyticConfig();
			String location = config.getResultsLocation();
			String name = config.getResultsName();
			if(StringUtil.isEmpty(location)==false&&StringUtil.isEmpty(name)){
				if(location.endsWith(HadoopFile.SEPARATOR)==false){
					location=location+HadoopFile.SEPARATOR;
				}
				String file=location+name;
				return file;
			}
		}else if (source.getAnalyticConfig() instanceof CopytoHadoopConfig) {
			CopytoHadoopConfig config = (CopytoHadoopConfig) source
					.getAnalyticConfig();
			String location = config.getResultsLocation();
			String name = config.getCopyToFileName();
			if(StringUtil.isEmpty(location)==false&&StringUtil.isEmpty(name)){
				if(location.endsWith(HadoopFile.SEPARATOR)==false){
					location=location+HadoopFile.SEPARATOR;
				}
				String file=location+name;
				return file;
			}
		}else if (source.getAnalyticConfig() instanceof HadoopPigExecuteConfig) {
			HadoopPigExecuteConfig config = (HadoopPigExecuteConfig) source
					.getAnalyticConfig();
			String location = config.getResultsLocation();
			String name = config.getResultsName();
			if(StringUtil.isEmpty(location)==false&&StringUtil.isEmpty(name)){
				if(location.endsWith(HadoopFile.SEPARATOR)==false){
					location=location+HadoopFile.SEPARATOR;
				}
				String file=location+name;
				return file;
			}
		}
		return null;
	}
	private static void handleTableSetSubflowParent(
			HashMap<String, String> tableMappingMap, AnalyticNode parentNode,
			AnalyticNode childNode) {

		String inputSchemaTable = getOutputSchemaTable(parentNode);
		String subSchemaTable = tableMappingMap.get(inputSchemaTable);
		TableSetConfig config = (TableSetConfig) childNode.getSource()
				.getAnalyticConfig();
		String firstTable = config.getTableSetModel().getFirstTable();
		//MINER-2040 	Table Set failed to run in an sub flow operator.
		if(StringUtil.isEmpty(firstTable) ==false){
			firstTable = firstTable.replace("\".\"", ".");
			firstTable = StringHandler.removeDoubleQ(firstTable) ;
			if(subSchemaTable.equals(firstTable)){
				firstTable =inputSchemaTable.replace(".", "\".\"");
				firstTable=  "\"" + firstTable + "\"";
				config.getTableSetModel().setFirstTable(firstTable) ;
			}
		}
		if (subSchemaTable != null && config != null
				&& config.getTableSetModel() != null) {
			List<AnalysisColumnMap> columnMaps = config.getTableSetModel()
					.getColumnMapList();
			if (columnMaps != null && columnMaps.size() > 0) {
				//the first map is the output column
				for (int k = 1; k < columnMaps.size(); k++) {
					if (subSchemaTable.equals((columnMaps.get(k)
							.getSchemaName()
							+ "." + columnMaps.get(k).getTableName()))) {
						columnMaps.get(k).setSchemaName(
								getOutputSchema(parentNode));
						columnMaps.get(k).setTableName(
								getOutputTable(parentNode));
					}
				}
			}
		}
	}

	private static List<AnalyticNode> findExitNode(
			AnalyticNodeSubflow parentNode) {
		List<AnalyticNode> exitNodes = new ArrayList<AnalyticNode>();
		AnalyticNode exitNode = parentNode.getExitNode();
		// exit node not set
		if (exitNode == null) {
			if (parentNode.getChildNodes() != null
					&& parentNode.getChildNodes().size() > 0) {
				for (int i = 0; i < parentNode.getChildNodes().size(); i++) {
					AnalyticNode pNode = parentNode.getChildNodes().get(i);
					// find to a
					if (pNode.getChildNodes() == null
							|| pNode.getChildNodes().size() == 0) {
						exitNodes.add(pNode);
					}
				}

			}
		} else {
			exitNodes.add(exitNode);
		}
		return exitNodes;
	}

	private static void handleTablejoinSubflowParent(
			HashMap<String, String> tableMappingMap, AnalyticNode parentNode,
			AnalyticNode childNode) {

		String inputSchemaTable = getOutputSchemaTable(parentNode);
		String subSchemaTable = tableMappingMap.get(inputSchemaTable);
		if (subSchemaTable != null) {
			TableJoinConfig config = (TableJoinConfig) childNode.getSource()
					.getAnalyticConfig();
			if (config != null && config.getTableJoinDef() != null) {
				List<AnalysisJoinTable> jointables = config.getTableJoinDef()
						.getJoinTables();
				if (jointables != null && jointables.size() > 0) {
					for (int k = 0; k < jointables.size(); k++) {
						if (subSchemaTable.equals((jointables.get(k)
								.getSchema()
								+ "." + jointables.get(k).getTable()))) {
							jointables.get(k).setSchema(
									getOutputSchema(parentNode));
							jointables.get(k).setTable(
									getOutputTable(parentNode));
						}
					}
				}
			}
		}
	}

	public static void resetChildNodeSource(AnalyticNode childNode,
			AnalyticNode parent, HashMap<String, String> schemaTableMap) {
		AnalyticConfiguration parentConfig = parent.getSource()
				.getAnalyticConfig();
		AnalyticSource parentSource = parent.getSource();
		AnalyticSource childSource = childNode.getSource();
		// childNode is not a table..
		String childSchemaTable = getInputSchemaTable4General(childNode);
		if (childSource instanceof DataBaseAnalyticSource
				&& schemaTableMap != null && schemaTableMap.values() != null
				&& schemaTableMap.values().contains(childSchemaTable) == true) { // really
			// mapped
			DataBaseAnalyticSource childDBSource = (DataBaseAnalyticSource) childSource;
			itsLogger.debug(
					("resetChildNodeSource==========\n"));
			itsLogger.debug("childNode = " + childNode);
			itsLogger.debug("parentNode = " + parent);
			itsLogger.debug(
					"datasource before = " + (childDBSource));

			if (parentConfig instanceof DataOperationConfig) {
				if (childDBSource.getTableInfo() != null) {
					childDBSource.getTableInfo().setTableName(
							((DataOperationConfig) parentConfig)
									.getOutputTable());
					childDBSource.getTableInfo().setSchema(
							((DataOperationConfig) parentConfig)
									.getOutputSchema());
					childDBSource.getTableInfo().setTableType(
							((DataOperationConfig) parentConfig)
									.getOutputType());
				}
				if (childDBSource.getDataBaseInfo() != null) {
					childDBSource.getDataBaseInfo().setPassword(
							((DataBaseAnalyticSource) parentSource)
									.getDataBaseInfo().getPassword());
					childDBSource.getDataBaseInfo().setSystem(
							((DataBaseAnalyticSource) parentSource)
									.getDataBaseInfo().getSystem());
					childDBSource.getDataBaseInfo().setUrl(
							((DataBaseAnalyticSource) parentSource)
									.getDataBaseInfo().getUrl());
					childDBSource.getDataBaseInfo().setUserName(
							((DataBaseAnalyticSource) parentSource)
									.getDataBaseInfo().getUserName());
					childDBSource.getDataBaseInfo().setUseSSL(
							((DataBaseAnalyticSource) parentSource)
									.getDataBaseInfo().getUseSSL() );
				}

			} else if (parentConfig instanceof DBTableSelectorConfig) {
				if (childDBSource.getTableInfo() != null) {
					childDBSource.getTableInfo().setTableName(
							((DBTableSelectorConfig) parentConfig)
									.getTableName());
					childDBSource.getTableInfo().setSchema(
							((DBTableSelectorConfig) parentConfig)
									.getSchemaName());
					childDBSource.getTableInfo().setTableType(
							((DBTableSelectorConfig) parentConfig)
									.getTableType());
				}
				if (childDBSource.getDataBaseInfo() != null) {
					childDBSource.getDataBaseInfo().setPassword(
							((DBTableSelectorConfig) parentConfig)
									.getPassword());
					childDBSource.getDataBaseInfo().setSystem(
							((DBTableSelectorConfig) parentConfig).getSystem());
					childDBSource.getDataBaseInfo().setUrl(
							((DBTableSelectorConfig) parentConfig).getUrl());
					childDBSource.getDataBaseInfo().setUserName(
							((DBTableSelectorConfig) parentConfig)
									.getUserName());
					childDBSource.getDataBaseInfo().setUseSSL(
							((DBTableSelectorConfig) parentConfig).getUseSSL() );
				}
			} else if (parentSource instanceof DataBaseAnalyticSource) { // could
				// be
				// sql
				// execute
				DataBaseAnalyticSource dbParentSource = (DataBaseAnalyticSource) parentSource;
				if (childDBSource.getTableInfo() != null
						&& dbParentSource.getTableInfo() != null) {
					childDBSource.getTableInfo().setTableName(
							dbParentSource.getTableInfo().getTableName());
					childDBSource.getTableInfo().setSchema(
							dbParentSource.getTableInfo().getSchema());
					childDBSource.getTableInfo().setTableType(
							dbParentSource.getTableInfo().getTableType());
				}
				if (childDBSource.getDataBaseInfo() != null
						&& dbParentSource.getDataBaseInfo() != null) {
					childDBSource.getDataBaseInfo().setPassword(
							dbParentSource.getDataBaseInfo().getPassword());
					childDBSource.getDataBaseInfo().setSystem(
							dbParentSource.getDataBaseInfo().getSystem());
					childDBSource.getDataBaseInfo().setUrl(
							dbParentSource.getDataBaseInfo().getUrl());
					childDBSource.getDataBaseInfo().setUserName(
							dbParentSource.getDataBaseInfo().getUserName());
					childDBSource.getDataBaseInfo().setUseSSL(
							dbParentSource.getDataBaseInfo().getUseSSL() );
				}

			}
			itsLogger.debug(
					"datasource after = " + childDBSource);
		}

	}

	private static void handleSubflowChildOperator(
			AnalyticNodeSubflow subFlowNode) {
		List<AnalyticNode> childNodes = subFlowNode.getChildNodes(); // could
		// have
		// multiple
		// child
		AnalyticNode subFlowExitNode = subFlowNode.getExitNode();
		if (subFlowExitNode == null) {// if no exit node, just add to any leaf
			// node's child
			if (subFlowNode.getSubFlowChildNodes() != null
					&& subFlowNode.getSubFlowChildNodes().size() > 0) {
				Iterator<AnalyticNode> it = subFlowNode.getSubFlowChildNodes()
						.iterator();
				while (it.hasNext()) {
					AnalyticNode subChild = it.next();
					if (subChild != null) {
						if (subChild.getParentNodes() != null
								&& subChild.getParentNodes().size() > 0) {
							handleChild(subFlowNode, childNodes, subChild);
						} else if (SQLAnalyzer.class.getName().equals(
								subChild.getAnalyzerClass())) {
							handleChild(subFlowNode, childNodes, subChild);
						}
					}
				}
			}
		} else {

			handleChild(subFlowNode, childNodes, subFlowExitNode);
		}
	}

	private static void handleChild(AnalyticNodeSubflow subFlowNode,
			List<AnalyticNode> childNodes, AnalyticNode subFlowExitNode) {
		// 1 handle child
		if (childNodes != null) {
			for (int i = 0; i < childNodes.size(); i++) {
				AnalyticNode childNode = childNodes.get(i);
				if (false == isSubFlowNode(childNode)) {// nothing to do for a
					// child which is a
					// subflow too ,childs
					// handleSubflowParentOperator
					// will do this...
					childNode = childNodes.get(i);
					childNode.getParentNodes().remove(subFlowNode);
					subFlowExitNode.addChildNode(childNode);
				}

			}
			// this subflow node will be remove, so any child link is no use
			// subFlowNode.getChildNodes().clear();
		}
	}

	// could be mulitple
	private static List<AnalyticNode> findRealChild4SubfowChild(
			AnalyticNodeSubflow childNode, AnalyticNode parentSubFlowExitNode) {

		HashMap<String, String> subSchemaTableMap = childNode
				.getSubFlowConfig().getTableMapping();
		String inputSchemaTable = getOutputSchemaTable(parentSubFlowExitNode);

		if (SQLAnalyzer.class.getName().equals(
				parentSubFlowExitNode.getAnalyzerClass())
				&& (inputSchemaTable == null || subSchemaTableMap
						.get(inputSchemaTable) == null)) {// a sql execute node
			// from a null
			// exitnode subflow
			return findRealChild4SubfowChildWithSqlExecuteParent(childNode,
					parentSubFlowExitNode);
		}

		String subSchemaTable = subSchemaTableMap.get(inputSchemaTable);
		itsLogger.debug(
				"[start:]findRealChild4SubfowChild: parentSubFlowExitNode="
						+ parentSubFlowExitNode.toString());
		// input to sub mapping
		List<AnalyticNode> result = new ArrayList<AnalyticNode>();
		if (childNode.getSubFlowChildNodes() == null) {
			return result;
		}
		Iterator<AnalyticNode> subFlowChildNodes = childNode
				.getSubFlowChildNodes().iterator();
		while (subFlowChildNodes.hasNext()) {
			AnalyticNode node = subFlowChildNodes.next();
			// and table also join the input
			if (DBTableSelector.class.getName().equals(node.getAnalyzerClass()) == false 
					&&HadoopFileSelector.class.getName().equals(node.getAnalyzerClass()) == false 
					&& node.getParentNodes() != null&& node.getParentNodes().size() > 0) {
				for (int i = 0; i < node.getParentNodes().size(); i++) {
					AnalyticNode xnode = node.getParentNodes().get(i);
					if ((xnode.getParentNodes() == null || xnode
							.getParentNodes().size() == 0)
							&& subSchemaTable != null
							&& subSchemaTable
									.equals(getInputSchemaTable4DBTable(xnode))) {
						if (result.contains(node) == false) {
							result.add(node);
							itsLogger.debug(
									"[start:]findRealChild4SubfowChild: found node="
											+ node.toString());
						}
						// very careful
						removeRealChildsParent(node, xnode);
					}
				}
			} else if (node.getChildNodes() == null
					|| node.getChildNodes().size() == 0) {// a parent if has no
				// child, if it is
				// maped. need to be
				// added too
				if ((subSchemaTable != null && subSchemaTable
						.equals(getInputSchemaTable4DBTable(node)))) {
					if (result.contains(node) == false) {
						result.add(node);
						itsLogger.debug(
								"[start:]findRealChild4SubfowChild: found node="
										+ node.toString());
					}
				}
				// no mapping configured any more
			} else if (subSchemaTable == null
					&& (DBTableSelector.class.getName().equals(
							node.getAnalyzerClass()) == true || (node
							.getParentNodes() == null || node.getParentNodes()
							.size() == 0))) {
				if (result.contains(node) == false) {
					result.add(node);
					itsLogger.debug(
							"[start:]findRealChild4SubfowChild: found node="
									+ node.toString());
				}

			}
		}

		return result;
	}

	// MINER-1928 An sub flow without exit operator should be able to link to
	// SQL execute.
	private static List<AnalyticNode> findRealChild4SubfowChildWithSqlExecuteParent(
			AnalyticNodeSubflow childNode, AnalyticNode parentSqlExecuteNode) {
		List<AnalyticNode> realChild = new ArrayList<AnalyticNode>();
		if (parentSqlExecuteNode != null && childNode != null
				&& childNode.getSubFlowChildNodes() != null) {
			for (int i = 0; i < childNode.getSubFlowChildNodes().size(); i++) {
				AnalyticNode node = childNode.getSubFlowChildNodes().get(i);
				if (node.getChildNodes() != null) {
					for (int j = 0; j < node.getChildNodes().size(); j++) {
						if (realChild.contains(node.getChildNodes().get(j)) == false) {
							realChild.add(node.getChildNodes().get(j));
						}
					}
				}
			}
		}
		// real child not found ,will add the subflow's start node.
		if (realChild.size() == 0 && childNode.getSubFlowChildNodes() != null) {
			for (int i = 0; i < childNode.getSubFlowChildNodes().size(); i++) {
				AnalyticNode node = childNode.getSubFlowChildNodes().get(i);
				if (node.getParentNodes() == null
						|| node.getParentNodes().size() == 0) {
					for (int j = 0; j < node.getParentNodes().size(); j++) {
						if (realChild.contains(node.getParentNodes().get(j)) == false) {
							realChild.add(node.getParentNodes().get(j));
						}
					}
				}
			}
		}
		return realChild;
	}

	private static void removeRealChildsParent(AnalyticNode childNode,
			AnalyticNode parentNode) {

		if (childNode.getParentNodes() != null
				&& childNode.getParentNodes().contains(parentNode)) {
			childNode.getParentNodes().remove(parentNode);
			if (childNode.getAllSources() != null) {
				childNode.getAllSources().remove(parentNode.getSource());
			}
		}

	}

	// tablemapping can only be mapped to table
	private static String getInputSchemaTable4DBTable(AnalyticNode node) {
		AnalyticSource source = node.getSource();
		if (source.getAnalyticConfig() instanceof DBTableSelectorConfig) {
			DBTableSelectorConfig config = (DBTableSelectorConfig) source
					.getAnalyticConfig();
			String schema = config.getSchemaName();
			String table = config.getTableName();
			return schema + "." + table;
		}//this is only for hadoop
		else if (source.getAnalyticConfig() instanceof HadoopFileSelectorConfig) {
			HadoopFileSelectorConfig config = (HadoopFileSelectorConfig) source
					.getAnalyticConfig();
			return   "." + config.getHadoopFileName();
		}
		// this is very impotant MINER-1898
		else {
			return null;
		}
	}

	private static String getInputSchemaTable4General(AnalyticNode node) {
		AnalyticSource source = node.getSource();
		if (source.getAnalyticConfig() instanceof DBTableSelectorConfig) {
			DBTableSelectorConfig config = (DBTableSelectorConfig) source
					.getAnalyticConfig();
			String schema = config.getSchemaName();
			String table = config.getTableName();
			return schema + "." + table;
		}// this is very impotant MINER-1898
		else if (source instanceof DataBaseAnalyticSource) {
			DataBaseAnalyticSource childDBSource = (DataBaseAnalyticSource) source;
			if (childDBSource.getTableInfo() != null) {

				String schema = childDBSource.getTableInfo().getSchema();
				String table = childDBSource.getTableInfo().getTableName();
				return schema + "." + table;
			}

		}

		return null;

	}

	private static HashMap<String, String> refineSubSchemaTableMap(
			HashMap<String, String> subSchemaTableMap,
			VariableModel parentsubflowVariable) {
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if (subSchemaTableMap != null && subSchemaTableMap.keySet() != null) {
			Iterator<String> it = subSchemaTableMap.keySet().iterator();
			while (it.hasNext()) {
				String inputSchemaTable = it.next();
				String subSchemaTable = subSchemaTableMap.get(inputSchemaTable);
				resultMap.put(VariableModelUtility.getReplaceValue(
						parentsubflowVariable, inputSchemaTable),
						subSchemaTable);
			}
		}
		return resultMap;
	}

	private static String getOutputSchemaTable(AnalyticNode parentNode) {

		if (parentNode instanceof SubFlowOperator
				&& ((SubFlowOperator) parentNode).getExitTableInfo() == null) {
			return null;
		}
		//for hadoop
		if(HadoopFileSelector.class.getName().equals( 
				parentNode.getAnalyzerClass()  )){
			return "." + ((HadoopAnalyticSource)parentNode.getSource()).getFileName();
		}else if(parentNode.getSource().getAnalyticConfig() instanceof HadoopDataOperationConfig){
			HadoopDataOperationConfig conf = ((HadoopDataOperationConfig)parentNode.getSource().getAnalyticConfig());
			String folder =  conf.getResultsLocation();
			if(folder.endsWith(HadoopFile.SEPARATOR)==false){
				folder = folder + HadoopFile.SEPARATOR;
			}
			return "." +folder +conf.getResultsName();

		}
		else{//now for db 
			String schema = getOutputSchema(parentNode);
			String table = getOutputTable(parentNode);
			if (schema != null && table != null) {
				return schema + "." + table;
			}
		}
		return null;

	}

	private static String getOutputSchema(AnalyticNode parentNode) {

		itsLogger.debug(
				"getOutputSchema [parentNode= " + parentNode);
		AnalyticSource source = parentNode.getSource();
		if (source.getAnalyticConfig() instanceof DBTableSelectorConfig) {
			DBTableSelectorConfig config = (DBTableSelectorConfig) source
					.getAnalyticConfig();
			String schema = config.getSchemaName();
			return schema;
		}// this is very impotant MINER-1898
		else if (source.getAnalyticConfig() instanceof DataOperationConfig) {
			DataOperationConfig config = (DataOperationConfig) source
					.getAnalyticConfig();
			String schema = config.getOutputSchema();
			return schema;
		} else if (source instanceof DataBaseAnalyticSource
				&& ((DataBaseAnalyticSource) source).getTableInfo() != null // for
		// a
		// sql
		// execute
		// after
		// a
		// none
		// exit
		// operator
		// subflow
		) {// like sample selector
			String schema = ((DataBaseAnalyticSource) source).getTableInfo()
					.getSchema();
			return schema;
		}

		return null;

	}

	private static String getOutputTable(AnalyticNode parentNode) {
		AnalyticSource source = parentNode.getSource();
		if (source.getAnalyticConfig() instanceof DBTableSelectorConfig) {
			DBTableSelectorConfig config = (DBTableSelectorConfig) source
					.getAnalyticConfig();
			String table = config.getTableName();
			return table;
		}// this is very impotant MINER-1898
		else if (source.getAnalyticConfig() instanceof DataOperationConfig) {
			DataOperationConfig config = (DataOperationConfig) source
					.getAnalyticConfig();
			String table = config.getOutputTable();
			return table;
		} else if (source instanceof DataBaseAnalyticSource
				&& ((DataBaseAnalyticSource) source).getTableInfo() != null)// for
		// a
		// sql
		// execute
		// after
		// a
		// none
		// exit
		// operator
		// subflow){//like
		// sample
		// selector
		{
			if (source.getAnalyticConfig() instanceof SampleSelectorConfig) {
				String schemaTable = ((SampleSelectorConfig) source
						.getAnalyticConfig()).getSelectedTable();
				String schema = ((DataBaseAnalyticSource) source)
						.getTableInfo().getSchema();
				if (schemaTable.startsWith(schema + ".")) {
					return schemaTable.substring(schema.length() + 1,
							schemaTable.length());
				}
			} else {
				return ((DataBaseAnalyticSource) source).getTableInfo()
						.getTableName();
			}
		}

		return null;

	}

	private static List<AnalyticNode> getSubflowInputTableNodes(
			List<AnalyticNode> subFlowNodes) {
		List<AnalyticNode> inputTableNodes = new ArrayList<AnalyticNode>();
		if (subFlowNodes != null && subFlowNodes.size() > 0) {
			for (int i = 0; i < subFlowNodes.size(); i++) {
				if (subFlowNodes.get(i).getParentNodes() == null
						|| subFlowNodes.get(i).getParentNodes().size() == 0) {
					inputTableNodes.add(subFlowNodes.get(i));
				}
			}
		}
		return inputTableNodes;
	}

	private static AnalyticProcess getSubFlowProcess(String parentNodeName,
			Element opNode, XmlDocManager opTypeXmlManager,
			String filePathPrefix, Locale locale, VariableModel variableModel) throws Exception {
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				opNode, PARAMETER);
		AnalyticProcess subprocess = null;
		for (Node parameterNode : parameterNodeList) {
			String paramName = ((Element) parameterNode).getAttribute(KEY);
			String paramValue = ((Element) parameterNode).getAttribute(VALUE);

			if (OperatorParameter.NAME_subflowPath.equals(paramName)) {
				// some is not configured
				if (StringUtil.isEmpty(paramValue) == true) {
					return null;
				}
				String filePath = filePathPrefix + paramValue
						+ Operator.AFM_SUFFIX;
				if (new File(filePath).exists() == false) {
					itsLogger.error(
							"getSubFlowProcess:file does not exists!");
					return null;
				}

				XmlDocManager xmlManager = new XmlDocManager();
				try {
					xmlManager.parseXMLFile(filePath);
				} catch (Exception e) {
					itsLogger.error(
							MiningUtil.class.getName() + "\n" + e.toString());

				}
				HashMap<String, VariableModel> subFlowVariableMap = readSubflowVariableModel(xmlManager);
				// child name->parent name
				HashMap<String, List<String>> childParentMap = readChildParentMap(xmlManager);
				HashMap<String, String> subflowNameUUIDMap = getSubflowUUIDNameMap(xmlManager);
				subprocess = parseXMLWithVariableModel(filePath, locale,
						xmlManager, variableModel, true, subFlowVariableMap,
						childParentMap, subflowNameUUIDMap);

				if (subprocess == null || subprocess.getFlow() == null) {
					return null;
				}

				List<AnalyticNode> nodes = subprocess.getFlow().getAllNodes();
				for (int i = 0; i < nodes.size(); i++) {
					nodes.get(i).setGroupID(parentNodeName);
				}
			}
		}

		return subprocess;
	}

	private static VariableModel getSubFlowVariableModel(Element opNode,
			XmlDocManager opTypeXmlManager) {
		VariableModel variableModel = null;
		ArrayList<Node> variableModelNodes = opTypeXmlManager.getNodeList(
				opNode, VariableModel.MODEL_TAG_NAME);
		for (Node variableModelNode : variableModelNodes) {
			Element variableElement = (Element) variableModelNode;
			variableModel = VariableModel.fromXMLElement(variableElement);
		}
		return variableModel;
	}

	public static Object stringToObject(String str) {
		BASE64Decoder decode = new BASE64Decoder();

		Object out = null;
		if (str != null) {
			try {
				ByteArrayInputStream bios = new ByteArrayInputStream(decode
						.decodeBuffer(str));
				ObjectInputStream ois = new ObjectInputStream(bios);
				out = ois.readObject();
			} catch (Exception e) {
				itsLogger.error(
						StringUtil.class.getName() + "\n" + e.toString());
				return null;
			}
		}
		return out;
	}

	/**
	 * @param nodeMap
	 * @param linkNode
	 */
	private static void setLinks(HashMap<String, AnalyticNode> nodeMap,
			Node linkNode) {
		String source = linkNode.getAttributes().getNamedItem(LINK_SOURCE_ATTR)
				.getNodeValue();
		String target = linkNode.getAttributes().getNamedItem(LINK_TARGET_ATTR)
				.getNodeValue();
		AnalyticNode parentNode = nodeMap.get(source);
		AnalyticNode childNode = nodeMap.get(target);
		parentNode.addChildNode(childNode);
	}

	private static HashMap<String, String> toHashMap(
			VariableModel subflowVariable) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (subflowVariable != null && subflowVariable.getIterator() != null) {
			Iterator<Entry<String, String>> it = subflowVariable.getIterator();
			while (it.hasNext()) {
				Entry<String, String> var = it.next();
				map.put(var.getKey(), var.getValue());
			}
		}

		return map;
	}

	private static HashMap<String, String> toHashMap(
			TableMappingModel tableMpapping, VariableModel variableModel) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (tableMpapping != null && tableMpapping.getMappingItems() != null) {
			List<TableMappingItem> items = tableMpapping.getMappingItems();
			for (int i = 0; i < items.size(); i++) {
				TableMappingItem var = items.get(i);
				// 
				map.put(VariableModelUtility.getReplaceValue(variableModel, var
						.getInputSchema())
						+ "."
						+ VariableModelUtility.getReplaceValue(variableModel,
								var.getInputTable()), var.getSubFlowSchema()
						+ "." + var.getSubFlowTable());
			}
		}

		return map;
	}

	private static VariableModel readVariableModel(
			XmlDocManager opTypeXmlManager) {
		ArrayList<Node> variableModelNodes = opTypeXmlManager.getNodeList(
				opTypeXmlManager.getRootNode(), VariableModel.MODEL_TAG_NAME);
		for (Node variableModelNode : variableModelNodes) {
			Element variableElement = (Element) variableModelNode;
			VariableModel variableModel = VariableModel
					.fromXMLElement(variableElement);
			return variableModel;
		}
		return null;
	}

	/**
	 * @param parameters
	 * @param analyticNode
	 * @param opTypeXmlManager
	 * @param locale
	 * @param variableModel
	 * @return
	 */
	private static AnalyticConfiguration createConfig(
			Map<String, String> parameters, String analyzerClass,
			Node operatorNode, XmlDocManager opTypeXmlManager, Locale locale,
			VariableModel variableModel,
			HashMap<String, String> subflowNameUUIDMap,
			HashMap<String, VariableModel> subFlowVariableMap) {
		String configClass = analyzerConfigMap.get(analyzerClass);
		if (configClass == null) {
			return null;
		}
		AnalyticConfiguration config = null;
		itsLogger.debug(
				"createConfig:AnalyzerClass=" + analyzerClass);

		try {
			config = (AnalyticConfiguration) Class.forName(configClass)
					.newInstance();

			AnalyticConfigUtility.fillConfigParameter(operatorNode, opTypeXmlManager, variableModel,
					subflowNameUUIDMap, subFlowVariableMap, config);

			handleStorageParameters(config, operatorNode, opTypeXmlManager,
					variableModel);

			Set<String> keySet = parameters.keySet();

			if (config instanceof CustomziedConfig) {
				HashMap<String, String> paramap = new HashMap<String, String>();
				for (Iterator<String> iterator = keySet.iterator(); iterator
						.hasNext();) {
					String paramName = iterator.next();
					String value = parameters.get(paramName);
					if (value != null && value.trim().length() > 0) {
						paramap.put(paramName, value);
						String firstChar = String.valueOf(paramName.charAt(0));
						String methodName = "set" + firstChar.toUpperCase()
								+ paramName.substring(1);
						try {
							Method method = config.getClass().getMethod(
									methodName, String.class);
							if (method != null) {
								method.invoke(config, value);
							}
						} catch (NoSuchMethodException e) {
							itsLogger.error(e);
						}
					}
				}
				String udfSchema = operatorNode.getAttributes().getNamedItem(
						"udfschema").getNodeValue();
				String udfName = operatorNode.getAttributes().getNamedItem(
						"udfName").getNodeValue();
				String operatorName = operatorNode.getAttributes()
						.getNamedItem("operatorname").getNodeValue();
				ArrayList<Node> outputColumnList = opTypeXmlManager
						.getNodeList(operatorNode, "outputColumns");
				HashMap<String, String> outputColumnMap = new HashMap<String, String>();
				for (Node paraNode : outputColumnList) {
					String columnName = ((Element) paraNode)
							.getAttribute("column");
					String columnType = ((Element) paraNode)
							.getAttribute(OP_TYPE_ATTR);
					outputColumnMap.put(columnName, columnType);
				}
				((CustomziedConfig) config).setParametersMap(paramap);
				((CustomziedConfig) config).setOutputMap(outputColumnMap);
				((CustomziedConfig) config).setUdfSchema(udfSchema);
				((CustomziedConfig) config).setUdfName(udfName);
				((CustomziedConfig) config).setOperatorName(operatorName);
			} else {
				for (Iterator<String> iterator = keySet.iterator(); iterator
						.hasNext();) {
					String paramName = iterator.next();
					String value = parameters.get(paramName);
					if (value != null && value.trim().length() > 0) {
						String firstChar = String.valueOf(paramName.charAt(0));
						String methodName = "set" + firstChar.toUpperCase()
								+ paramName.substring(1);
						try {
							Method method = config.getClass().getMethod(
									methodName, String.class);
							if (method != null) {
								method.invoke(config, value);
							}
						} catch (NoSuchMethodException e) {
							itsLogger.error(e);
						}
					}

				}

			}

			if (config instanceof AdaboostConfig) {
				AdaboostConfig adaboostConfig = (AdaboostConfig) config;

				adaboostConfig.getConfiglist().clear();
				adaboostConfig.getNameConfigMap().clear();

				HashMap<String, AnalyticConfiguration> nameConfigMap = new HashMap<String, AnalyticConfiguration>();
				List<String> configColumnNameList = new ArrayList<String>();
				ArrayList<Node> nodes = opTypeXmlManager.getNodeList(
						operatorNode, AdaboostPersistenceModel.TAG_NAME);
				if (nodes != null && nodes.size() > 0) {
					Element adaboostPersistenceModelElement = (Element) nodes
							.get(0);
					AdaboostPersistenceModel adaboostPersistenceModel = AdaboostPersistenceModel
							.fromXMLElement(adaboostPersistenceModelElement);
					List<AdaboostPersistenceItem> items = adaboostPersistenceModel
							.getAdaboostUIItems();

					AnalysisAdaboostPersistenceModel sdkAdaboostPersistenceModel = new AnalysisAdaboostPersistenceModel();
					List<AnalysisAdaboostPersistenceItem> sdkAdaboostUIItems = new ArrayList<AnalysisAdaboostPersistenceItem>();
					sdkAdaboostPersistenceModel
							.setAdaboostUIItems(sdkAdaboostUIItems);
					for (AdaboostPersistenceItem item : items) {
						AnalysisAdaboostPersistenceItem sdkItem = new AnalysisAdaboostPersistenceItem(
								item.getAdaType(), item.getAdaName());
						Map<String, String> sdkParameterMap = new HashMap<String, String>();
						Iterator<Entry<String, String>> iter = item
								.getParameterMap().entrySet().iterator();
						while (iter.hasNext()) {
							Entry<String, String> entry = iter.next();
							String value = VariableModelUtility
									.getReplaceValue(variableModel, entry
											.getValue());
							sdkParameterMap.put(entry.getKey(), value);
						}
						sdkItem.setParameterMap(sdkParameterMap);
						sdkAdaboostUIItems.add(sdkItem);
					}
					adaboostConfig
							.setAdaboostUIModel(sdkAdaboostPersistenceModel);
					for (AdaboostPersistenceItem item : items) {
						String adaType;
						if (item.getAdaType().indexOf(".workflow.operator.") > 0) {
							adaType = item.getAdaType().replace(
									".workflow.operator.", ".gef.runoperator.");
						} else {
							adaType = item.getAdaType();
						}
						AnalyticConfiguration adaBoostSingleConfig = createConfig(
								item.getParameterMap(), operatorAnalyzerMap
										.get(adaType), operatorNode,
								opTypeXmlManager, locale, variableModel,
								subflowNameUUIDMap, subFlowVariableMap);
						configColumnNameList.add(item.getAdaName());
						createAdaboostSingleConfig(adaboostConfig, item,
								adaBoostSingleConfig, nameConfigMap,
								variableModel);

						adaboostConfig
								.getConfiglist()
								.add(
										(AbstractModelTrainerConfig) adaBoostSingleConfig);
						adaboostConfig
								.getNameConfigMap()
								.put(
										item.getAdaName(),
										(AbstractModelTrainerConfig) adaBoostSingleConfig);
					}
				}
			}

			if (analyzerVisualTypeMap.get(analyzerClass) != null) {
				config.setVisualizationTypeClass(analyzerVisualTypeMap
						.get(analyzerClass));
			}

			// set visual type for some config...

			config.setLocale(locale);
		} catch (Exception e) {
			itsLogger.error(e);
		}
		return config;
	}

	
	private static void handleStorageParameters(AnalyticConfiguration config,
			Node operatorNode, XmlDocManager opTypeXmlManager,
			VariableModel variableModel) {
		// 1 handle common
		AnalysisStorageParameterModel sdkStorageParamsModel = getStorageModel(
				operatorNode, opTypeXmlManager,
				StorageParameterModel.DEFAULT_TAG_NAME, variableModel);
		if (sdkStorageParamsModel != null) {
			((AbstractAnalyticConfig) config)
					.setStorageParameters(sdkStorageParamsModel);

		}

		// 2 handle special

		if (config instanceof PLDAConfig) {
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_PLDAModelOutputTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((PLDAConfig) config)
						.setPLDAModelOutputTableStorageParameters(sdkStorageParamsModel);

			}
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_topicOutTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((PLDAConfig) config)
						.setTopicOutTableStorageParameters(sdkStorageParamsModel);

			}
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_docTopicOutTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((PLDAConfig) config)
						.setDocTopicOutTableStorageParameters(sdkStorageParamsModel);

			}
		} else if (config instanceof PLDAPredictConfig) {

			sdkStorageParamsModel = getStorageModel(
					operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_PLDADocTopicOutputTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((PLDAPredictConfig) config)
						.setPLDADocTopicOutputTableStorageParameters(sdkStorageParamsModel);

			}
		} else if (config instanceof SVDLanczosConfig) {
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_UmatrixTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((SVDLanczosConfig) config)
						.setUmatrixTableStorageParameters(sdkStorageParamsModel);

			}
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_VmatrixTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((SVDLanczosConfig) config)
						.setVmatrixTableStorageParameters(sdkStorageParamsModel);

			}
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_singularValueTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((SVDLanczosConfig) config)
						.setSingularValueTableStorageParameters(sdkStorageParamsModel);

			}

		} else if (config instanceof PCAConfig) {
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_PCAQoutputTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((PCAConfig) config)
						.setPCAQoutputTableStorageParameters(sdkStorageParamsModel);

			}
			sdkStorageParamsModel = getStorageModel(operatorNode,
					opTypeXmlManager,
					OperatorParameter.NAME_PCAQvalueOutputTable_StorageParams,
					variableModel);
			if (sdkStorageParamsModel != null) {
				((PCAConfig) config)
						.setPCAQvalueOutputTableStorageParameters(sdkStorageParamsModel);

			}
		}
	}

	private static AnalysisStorageParameterModel getStorageModel(
			Node operatorNode, XmlDocManager opTypeXmlManager, String tagName,
			VariableModel variableModel) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(operatorNode,
				tagName);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			StorageParameterModel sotrageParameterModel = StorageParameterModel
					.fromXMLElement(element);
			List<String> distributColumns = sotrageParameterModel
					.getDistributColumns();
			List<String> newDistributColumns = null;
			if (distributColumns != null) {
				newDistributColumns = new ArrayList<String>();
				for (String column : distributColumns) {
					column = VariableModelUtility.getReplaceValue(
							variableModel, column);
					newDistributColumns.add(column);
				}
			}
			AnalysisStorageParameterModel sdkStorageParamsModel = new AnalysisStorageParameterModel(
					sotrageParameterModel.isAppendOnly(), sotrageParameterModel
							.isColumnarStorage(), sotrageParameterModel
							.isCompression(), sotrageParameterModel
							.getCompressionLevel(), sotrageParameterModel
							.isDistributedRandomly(), newDistributColumns);
			return sdkStorageParamsModel;
		}
		return null;
	}

	public static void createAdaboostSingleConfig(
			AdaboostConfig adaboostConfig, AdaboostPersistenceItem item,
			AnalyticConfiguration adaBoostSingleConfig,
			HashMap<String, AnalyticConfiguration> nameConfigMap,
			VariableModel variableModel) {
		String columnNames = adaboostConfig.getColumnNames();
		String dependentColumn = adaboostConfig.getDependentColumn();
		String forceRetrain = adaboostConfig.getForceRetrain();
		if (adaBoostSingleConfig instanceof LogisticRegressionConfigGeneral) {
			LogisticRegressionConfigGeneral logiConfig = (LogisticRegressionConfigGeneral) adaBoostSingleConfig;
			logiConfig.setColumnNames(columnNames);
			logiConfig.setDependentColumn(dependentColumn);
			logiConfig.setForceRetrain(forceRetrain);

			String goodValue = logiConfig.getGoodValue();
			goodValue = VariableModelUtility.getReplaceValue(variableModel,
					goodValue);
			logiConfig.setGoodValue(goodValue);
			String epsilon = logiConfig.getEpsilon();
			epsilon = VariableModelUtility.getReplaceValue(variableModel,
					epsilon);
			logiConfig.setEpsilon(epsilon);
			String max_generations = logiConfig.getMax_generations();
			max_generations = VariableModelUtility.getReplaceValue(
					variableModel, max_generations);
			logiConfig.setMax_generations(max_generations);
		} else if (adaBoostSingleConfig instanceof NaiveBayesConfig) {
			NaiveBayesConfig nbConfig = (NaiveBayesConfig) adaBoostSingleConfig;
			nbConfig.setColumnNames(columnNames);
			nbConfig.setDependentColumn(dependentColumn);
			nbConfig.setForceRetrain(forceRetrain);
		} else if (adaBoostSingleConfig instanceof CartConfig) {
			CartConfig cartConfig = (CartConfig) adaBoostSingleConfig;
			cartConfig.setColumnNames(columnNames);
			cartConfig.setDependentColumn(dependentColumn);
			cartConfig.setForceRetrain(forceRetrain);

			String maximal_depth = cartConfig.getMaximal_depth();
			maximal_depth = VariableModelUtility.getReplaceValue(variableModel,
					maximal_depth);
			cartConfig.setMaximal_depth(maximal_depth);
			String confidence = cartConfig.getConfidence();
			confidence = VariableModelUtility.getReplaceValue(variableModel,
					confidence);
			cartConfig.setConfidence(confidence);
			String number_of_prepruning_alternatives = cartConfig
					.getNumber_of_prepruning_alternatives();
			number_of_prepruning_alternatives = VariableModelUtility
					.getReplaceValue(variableModel,
							number_of_prepruning_alternatives);
			cartConfig
					.setNumber_of_prepruning_alternatives(number_of_prepruning_alternatives);
			String minimal_size_for_split = cartConfig
					.getMinimal_size_for_split();
			minimal_size_for_split = VariableModelUtility.getReplaceValue(
					variableModel, minimal_size_for_split);
			cartConfig.setMinimal_size_for_split(minimal_size_for_split);
			String size_threshold_load_data = cartConfig
					.getSize_threshold_load_data();
			size_threshold_load_data = VariableModelUtility.getReplaceValue(
					variableModel, size_threshold_load_data);
			cartConfig.setSize_threshold_load_data(size_threshold_load_data);
			String minimal_leaf_size = cartConfig.getMinimal_leaf_size();
			minimal_leaf_size = VariableModelUtility.getReplaceValue(
					variableModel, minimal_leaf_size);
			cartConfig.setMinimal_leaf_size(minimal_leaf_size);
		} else if (adaBoostSingleConfig instanceof DecisionTreeConfig) {
			DecisionTreeConfig dTreeConfig = (DecisionTreeConfig) adaBoostSingleConfig;
			dTreeConfig.setColumnNames(columnNames);
			dTreeConfig.setDependentColumn(dependentColumn);
			dTreeConfig.setForceRetrain(forceRetrain);

			String maximal_depth = dTreeConfig.getMaximal_depth();
			maximal_depth = VariableModelUtility.getReplaceValue(variableModel,
					maximal_depth);
			dTreeConfig.setMaximal_depth(maximal_depth);
			String confidence = dTreeConfig.getConfidence();
			confidence = VariableModelUtility.getReplaceValue(variableModel,
					confidence);
			dTreeConfig.setConfidence(confidence);
			String minimal_gain = dTreeConfig.getMinimal_gain();
			minimal_gain = VariableModelUtility.getReplaceValue(variableModel,
					minimal_gain);
			dTreeConfig.setMinimal_gain(minimal_gain);
			String number_of_prepruning_alternatives = dTreeConfig
					.getNumber_of_prepruning_alternatives();
			number_of_prepruning_alternatives = VariableModelUtility
					.getReplaceValue(variableModel,
							number_of_prepruning_alternatives);
			dTreeConfig
					.setNumber_of_prepruning_alternatives(number_of_prepruning_alternatives);
			String minimal_size_for_split = dTreeConfig
					.getMinimal_size_for_split();
			minimal_size_for_split = VariableModelUtility.getReplaceValue(
					variableModel, minimal_size_for_split);
			dTreeConfig.setMinimal_size_for_split(minimal_size_for_split);
			String size_threshold_load_data = dTreeConfig
					.getSize_threshold_load_data();
			size_threshold_load_data = VariableModelUtility.getReplaceValue(
					variableModel, size_threshold_load_data);
			dTreeConfig.setSize_threshold_load_data(size_threshold_load_data);
			String minimal_leaf_size = dTreeConfig.getMinimal_leaf_size();
			minimal_leaf_size = VariableModelUtility.getReplaceValue(
					variableModel, minimal_leaf_size);
			dTreeConfig.setMinimal_leaf_size(minimal_leaf_size);
		} else if (adaBoostSingleConfig instanceof NeuralNetworkConfig) {
			NeuralNetworkConfig nnConfig = (NeuralNetworkConfig) adaBoostSingleConfig;
			nnConfig.setColumnNames(columnNames);
			nnConfig.setDependentColumn(dependentColumn);
			nnConfig.setForceRetrain(forceRetrain);

			String training_cycles = nnConfig.getTraining_cycles();
			training_cycles = VariableModelUtility.getReplaceValue(
					variableModel, training_cycles);
			nnConfig.setTraining_cycles(training_cycles);
			String learning_rate = nnConfig.getLearning_rate();
			learning_rate = VariableModelUtility.getReplaceValue(variableModel,
					learning_rate);
			nnConfig.setLearning_rate(learning_rate);
			String momentum = nnConfig.getMomentum();
			momentum = VariableModelUtility.getReplaceValue(variableModel,
					momentum);
			nnConfig.setMomentum(momentum);
			String fetchSize = nnConfig.getFetchSize();
			fetchSize = VariableModelUtility.getReplaceValue(variableModel,
					fetchSize);
			nnConfig.setFetchSize(fetchSize);
			String local_random_seed = nnConfig.getLocal_random_seed();
			local_random_seed = VariableModelUtility.getReplaceValue(
					variableModel, local_random_seed);
			nnConfig.setLocal_random_seed(local_random_seed);
			String error_epsilon = nnConfig.getError_epsilon();
			error_epsilon = VariableModelUtility.getReplaceValue(variableModel,
					error_epsilon);
			nnConfig.setError_epsilon(error_epsilon);

			String hiddenLayers = item.getParameterMap().get(
					NeuralNetworkConfig.ConstHidden_layers);
			hiddenLayers = VariableModelUtility.getReplaceValue(variableModel,
					hiddenLayers);
			item.getParameterMap().put(NeuralNetworkConfig.ConstHidden_layers,
					hiddenLayers);
		} else if (adaBoostSingleConfig instanceof SVMClassificationConfig) {
			SVMClassificationConfig svmConfig = (SVMClassificationConfig) adaBoostSingleConfig;
			svmConfig.setColumnNames(columnNames);
			svmConfig.setDependentColumn(dependentColumn);
			svmConfig.setForceRetrain(forceRetrain);

			String degree = svmConfig.getDegree();
			degree = VariableModelUtility
					.getReplaceValue(variableModel, degree);
			svmConfig.setDegree(degree);
			String gamma = svmConfig.getGamma();
			gamma = VariableModelUtility.getReplaceValue(variableModel, gamma);
			svmConfig.setGamma(gamma);
			String eta = svmConfig.getEta();
			eta = VariableModelUtility.getReplaceValue(variableModel, eta);
			svmConfig.setEta(eta);
			String nu = svmConfig.getNu();
			nu = VariableModelUtility.getReplaceValue(variableModel, nu);
			svmConfig.setNu(nu);
		}
		nameConfigMap.put(item.getAdaType(), adaBoostSingleConfig);
	}

	public static boolean exportAnalyticProcess(
			AnalyticProcess analyticProcess, String filePath) {
		Document xmlDoc = toXMLDocument(analyticProcess);

		File file = new File(filePath);
		XmlDocManager xmlDocManager = new XmlDocManager();
		try {
			BufferedWriter writer = null;
			// writer = new BufferedWriter(new FileWriter(file));
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), XmlDocManager.ENCODING_UTF8));
			writer.write(xmlDocManager.xmlToStringEnglish(xmlDoc));
			writer.close();
		} catch (Exception e) {
			itsLogger.error(
					MiningUtil.class.getName() + "\n" + e.toString());

		}

		return true;
	}

	public static Document toXMLDocument(AnalyticProcess analyticProcess) {

		Document xmlDoc = null;
		Node root = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {

		}
		root = xmlDoc.createElement("Process");
		xmlDoc.appendChild(root);
		List<AnalyticNode> analyticNodes = analyticProcess.getFlow()
				.getAllNodes();
		for (Iterator<AnalyticNode> iterator = analyticNodes.iterator(); iterator
				.hasNext();) {
			AnalyticNode analyticNode = iterator.next();
			Element operator = createOperatorElement(xmlDoc, analyticNode);
			root.appendChild(operator);
		}
		HashMap<String, String> linkMap = getLinkMap(analyticNodes);

		/**
		 * read link node
		 */
		Set<String> keys = linkMap.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String sourceName = iterator.next();
			Element line = xmlDoc.createElement(LINK_TAG);

			line.setAttribute(LINK_SOURCE_ATTR, sourceName);
			line.setAttribute(LINK_TARGET_ATTR, linkMap.get(sourceName));
			root.appendChild(line);
		}

		return xmlDoc;
	}

	/**
	 * @param analyticNodes
	 * @return
	 */
	private static HashMap<String, String> getLinkMap(
			List<AnalyticNode> analyticNodes) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (Iterator<AnalyticNode> iterator = analyticNodes.iterator(); iterator
				.hasNext();) {
			AnalyticNode analyticNode = iterator.next();
			List<AnalyticNode> child = analyticNode.getChildNodes();
			if (child != null) {
				for (Iterator<AnalyticNode> iterator2 = child.iterator(); iterator2
						.hasNext();) {
					AnalyticNode childNode = iterator2.next();
					map.put(analyticNode.getName(), childNode.getName());
				}
			}
		}

		return map;
	}

	/**
	 * @param operator
	 * @param analyticNode
	 */
	private static Element createOperatorElement(Document xmlDoc,
			AnalyticNode analyticNode) {
		Element operator = xmlDoc.createElement(OPERATOR_TAG);

		String name = analyticNode.getName();
		operator.setAttribute(OP_NAME_ATTR, name);
		String analyticClassName = analyticNode.getAnalyzerClass();
		String opertorClassName = getOpertorClassName(analyticClassName);
		operator.setAttribute(OP_TYPE_ATTR, opertorClassName);
		/**
		 * save parameter
		 */
		AnalyticConfiguration config = analyticNode.getSource()
				.getAnalyticConfig();
		HashMap<String, String> parameterMap = getParameterMap(config);

		List<String> parameterNames = config.getParameterNames();
		if (parameterNames != null) {
			Iterator<String> iter = parameterNames.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Element parameter = xmlDoc.createElement(PARAMETER);
				parameter.setAttribute(KEY, key.toString());
				if (XmlDocManager.PASSWORD.equals(key)) {
					parameter.setAttribute(VALUE, XmlDocManager
							.encryptedPassword(parameterMap.get(key)));
				} else {
					parameter.setAttribute(VALUE, parameterMap.get(key));
				}
				operator.appendChild(parameter);
			}
		}

		return operator;
	}

	/**
	 * @param config
	 * @return
	 */
	private static HashMap<String, String> getParameterMap(
			AnalyticConfiguration config) {
		HashMap<String, String> resMap = new HashMap<String, String>();

		List<String> paramNames = config.getParameterNames();
		try {
			for (Iterator<String> iterator = paramNames.iterator(); iterator
					.hasNext();) {
				String paramName = iterator.next();
				String firstChar = String.valueOf(paramName.charAt(0));
				String methodName = "get" + firstChar.toUpperCase()
						+ paramName.substring(1);
				Method method = config.getClass().getMethod(methodName, null);
				String paramValue;

				paramValue = method.invoke(config).toString();

				resMap.put(paramName, paramValue);
			}
		} catch (Exception e) {
			itsLogger.error(e);
		}

		return resMap;
	}

	/**
	 * @param analyticClassName
	 * @return
	 */
	private static String getOpertorClassName(String analyticClassName) {
		Set<String> keys = operatorAnalyzerMap.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (operatorAnalyzerMap.get(key).equals(analyticClassName)) {
				return key;
			}
		}
		return null;
	}
}
