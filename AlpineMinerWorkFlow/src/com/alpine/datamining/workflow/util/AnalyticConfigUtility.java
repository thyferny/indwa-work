/**
 * ClassName AnalyticConfigUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.impl.algoconf.AggregateConfig;
import com.alpine.datamining.api.impl.algoconf.FPGrowthConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopAggregaterConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopLinearTrainConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopLogisticRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPigExecuteConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopRandomSamplingConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopVariableConfig;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.LinearRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.RandomSamplingConfig;
import com.alpine.datamining.api.impl.algoconf.ReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.StratifiedSamplingConfig;
import com.alpine.datamining.api.impl.algoconf.TableJoinConfig;
import com.alpine.datamining.api.impl.algoconf.TableSetConfig;
import com.alpine.datamining.api.impl.algoconf.TableUnivariateConfig;
import com.alpine.datamining.api.impl.algoconf.VariableConfig;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.association.AnalysisExpressionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModelItem;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionSourceColumn;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayer;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayersModel;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementItem;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;
import com.alpine.datamining.api.impl.db.attribute.model.sampling.AnalysisSampleSizeModel;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinTable;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisTableJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisColumnMap;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisTableSetModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBinCategory;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBinDateTime;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBinNumeric;
import com.alpine.datamining.operator.regressions.AnalysisInterActionColumnsModel;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelFactory;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateField;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowField;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBin;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinColumn;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinCondition;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinFile;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionFile;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModelItem;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionSourceColumn;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayer;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayersModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionItem;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementItem;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinColumn;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinCondition;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinTable;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.tableset.ColumnMap;
import com.alpine.miner.workflow.operator.parameter.tableset.TableSetModel;
import com.alpine.miner.workflow.operator.parameter.univariate.UnivariateModel;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldItem;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItem;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItemBin;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItemBinCategory;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItemBinDateTime;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItemBinNumeric;
import com.alpine.miner.workflow.operator.parameter.woe.WOEInforList;
import com.alpine.miner.workflow.operator.parameter.woe.WOENode;
import com.alpine.miner.workflow.operator.parameter.woe.WOENominalNode;
import com.alpine.miner.workflow.operator.parameter.woe.WOENumericNode;
import com.alpine.miner.workflow.operator.parameter.woe.WOETable;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class AnalyticConfigUtility {

	public static void fillConfigParameter(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			HashMap<String, String> subflowNameUUIDMap,
			HashMap<String, VariableModel> subFlowVariableMap,
			AnalyticConfiguration config) {
		if (config instanceof TableJoinConfig) {
			fillTableJoinConfig(operatorNode, opTypeXmlManager, variableModel,
					subflowNameUUIDMap, subFlowVariableMap, config);
		} else if (config instanceof VariableConfig) {
			fillVariableConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		}
		 
		
		else if (config instanceof LinearRegressionConfig
				||config instanceof HadoopLinearTrainConfig) {
			fillLinearRegressionConfig(operatorNode, opTypeXmlManager, config);
		} else if (config instanceof LogisticRegressionConfigGeneral
				||config instanceof HadoopLogisticRegressionConfig) {
			fillLogisticRegressionConfig(operatorNode, opTypeXmlManager, config);
		} else if (config instanceof AggregateConfig||config instanceof HadoopAggregaterConfig ) {
			fillAggregateConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		} else if (config instanceof HistogramAnalysisConfig) {
			fillHistogramConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		} else if (config instanceof ReplaceNullConfig) {
			fillNullValueReplacementConfig(operatorNode, opTypeXmlManager,
					variableModel, config);
		}
		else if (config instanceof HadoopReplaceNullConfig) {
			fillNullValueReplacementConfig(operatorNode, opTypeXmlManager,
					variableModel, config);
		}
		else if (config instanceof FPGrowthConfig) {
			fillFPGrowthConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		} else if (config instanceof NeuralNetworkConfig) {
			fillNeuralNetworkConfig(operatorNode, opTypeXmlManager,
					variableModel, config);
		} else if (config instanceof WeightOfEvidenceConfig) {
			fillWOEConfig(operatorNode, opTypeXmlManager, variableModel, config);
		} else if (config instanceof TableSetConfig) {
			fillTableSetConfig(operatorNode, opTypeXmlManager, variableModel,
					subflowNameUUIDMap, subFlowVariableMap, config);
		} else if(config instanceof TableUnivariateConfig){
			fillUnivariateConfig(operatorNode, opTypeXmlManager, variableModel,config);
		} else if(config instanceof HadoopFileSelectorConfig){
			fillHadoopFileConfig(operatorNode, opTypeXmlManager, variableModel,(HadoopFileSelectorConfig)config);
		} 
		else if(config instanceof CopytoHadoopConfig){
			fillHadoopFileConfig(operatorNode, opTypeXmlManager, variableModel,(CopytoHadoopConfig)config);
		} else if(config instanceof HadoopVariableConfig){
			fillHadoopVariableConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		}else if(config instanceof HadoopJoinConfig){
			fillHadoopJoinConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		}
		else if(config instanceof HadoopUnionConfig){
			fillHadoopUnionConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		}
		 
		else if(config instanceof RandomSamplingConfig
				||config instanceof StratifiedSamplingConfig
				||config instanceof HadoopRandomSamplingConfig){
			fillSamplingSizeConfig(operatorNode, opTypeXmlManager, variableModel,
					config);
		}else if(config instanceof HadoopPigExecuteConfig){
			fillHadoopPigExecuteFileConfig(operatorNode, opTypeXmlManager, variableModel,(HadoopPigExecuteConfig)config);
		} 
	}

	private static void fillHadoopPigExecuteFileConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			HadoopPigExecuteConfig config) {
		FileStructureModel fileStructureModel=FileStructureModelFactory.
				createFileStructureModelByXML(opTypeXmlManager,operatorNode);
		config.setHadoopFileStructure(fileStructureModel);	

		
		  ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, AnalysisPigExecutableModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			AnalysisPigExecutableModel model = new AnalysisPigExecutableModel();
			Element element = (Element) nodeList.get(0);
			model.initFromXmlElement(element);
			String pigScript = model.getPigScript();
			String newPigScript = VariableModelUtility
			.getReplaceValue(variableModel, pigScript);
			model.setPigScript(newPigScript);
			config.setPigScript(model) ;
		}
		
		 
		
	}

	private static void fillSamplingSizeConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, SampleSizeModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			SampleSizeModel sampleSizeModel=SampleSizeModel.fromXMLElement(element);
			List<Integer> sdkSampleIdList = new ArrayList<Integer>();
			List<Double> sdkSampleSizeList = new ArrayList<Double>();
			List<String> sampleIdList = sampleSizeModel.getSampleIdList();
			List<String> sampleSizeList = sampleSizeModel.getSampleSizeList();
			if(sampleIdList!=null&&sampleSizeList!=null){
				for(int i=0;i<sampleIdList.size();i++){
					String sampleId = VariableModelUtility.getReplaceValue(variableModel, sampleIdList.get(i));
					String sampleSize = VariableModelUtility.getReplaceValue(variableModel, sampleSizeList.get(i));
					sdkSampleIdList.add(Integer.parseInt(sampleId));
					sdkSampleSizeList.add(Double.parseDouble(sampleSize));
				}
			}
			AnalysisSampleSizeModel sdkSampleSizeModel=new AnalysisSampleSizeModel(sdkSampleIdList, sdkSampleSizeList);
			if(config instanceof RandomSamplingConfig){
				((RandomSamplingConfig) config).setSampleSize(sdkSampleSizeModel);
			}else if(config instanceof StratifiedSamplingConfig){
				((StratifiedSamplingConfig) config).setSampleSize(sdkSampleSizeModel);
			}else if(config instanceof HadoopRandomSamplingConfig){
				((HadoopRandomSamplingConfig) config).setSampleSize(sdkSampleSizeModel);
			}
		}
		
	}

	private static void fillHadoopJoinConfig(Node operatorNode, 
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, HadoopJoinModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			HadoopJoinModel joinModel = HadoopJoinModel
					.fromXMLElement(element);
			List<AnalysisHadoopJoinFile> analysisJoinTables=new ArrayList<AnalysisHadoopJoinFile>();
			List<AnalysisHadoopJoinColumn> analysisJoinColumns=new ArrayList<AnalysisHadoopJoinColumn>();
			List<AnalysisHadoopJoinCondition> analysisJoinConditions=new ArrayList<AnalysisHadoopJoinCondition>();
			
			List<HadoopJoinCondition> joinConditions = joinModel.getJoinConditions();
			if(joinConditions!=null){
				for(HadoopJoinCondition joinCondition:joinConditions){
					HadoopJoinFile joinFile = joinModel.getJoinFile(joinCondition.getFileId());
					analysisJoinTables.add(new AnalysisHadoopJoinFile(joinFile.getFile(), joinFile.getOperatorModelID()));
					analysisJoinConditions.add(new AnalysisHadoopJoinCondition(joinCondition.getKeyColumn()));
				}
			}
			List<HadoopJoinColumn> joinColumns = joinModel.getJoinColumns();
			if(joinColumns!=null){
				for(HadoopJoinColumn joinColumn:joinColumns){
					analysisJoinColumns.add(new AnalysisHadoopJoinColumn(joinColumn.getColumnName(), 
							joinColumn.getNewColumnName(), 
							joinColumn.getColumnType(),
							joinModel.getJoinFile(joinColumn.getFileId()).getFile(),
							joinColumn.getFileId()));
				}
			}
			AnalysisHadoopJoinModel sdkJoinModel = new AnalysisHadoopJoinModel(analysisJoinTables, 
					analysisJoinColumns, analysisJoinConditions,joinModel.getJoinType());
			((HadoopJoinConfig) config).setJoinModel(sdkJoinModel);
		}
	}
 
	private static void fillHadoopUnionConfig(Node operatorNode, 
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, HadoopUnionModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			HadoopUnionModel joinModel = HadoopUnionModel
					.fromXMLElement(element);
			List<AnalysisHadoopUnionModelItem> analysisUnionItems=new ArrayList<AnalysisHadoopUnionModelItem>();
 
			List<HadoopUnionModelItem> outputColumns = joinModel.getOutputColumns();
			
			if(outputColumns!=null){
				for(HadoopUnionModelItem modelItem:outputColumns){
					String columnName =modelItem.getColumnName();
					String columnType = modelItem.getColumnType();
				 
					List<AnalysisHadoopUnionSourceColumn> mappingColumns = new ArrayList<AnalysisHadoopUnionSourceColumn>();
					List<HadoopUnionSourceColumn> sourceItems = modelItem.getMappingColumns();
					if(sourceItems!=null&&sourceItems.size()>0){
						for (HadoopUnionSourceColumn hadoopUnionSourceColumn : sourceItems) {
							
							AnalysisHadoopUnionSourceColumn newItem = new  AnalysisHadoopUnionSourceColumn(
									hadoopUnionSourceColumn.getColumnName(), hadoopUnionSourceColumn.getOperatorModelID());
							mappingColumns.add(newItem) ;
						}
					}
					
					analysisUnionItems.add(new AnalysisHadoopUnionModelItem(columnName, columnType, mappingColumns));
				}
			}
			
			List<HadoopUnionFile> unionFiles = joinModel.getUnionFiles() ;
			List<AnalysisHadoopUnionFile> analysisUnionFiles = new ArrayList<AnalysisHadoopUnionFile> (); 
			if(unionFiles!=null){
				for(HadoopUnionFile unionFile:unionFiles){
					analysisUnionFiles.add(new AnalysisHadoopUnionFile(unionFile.getFile(),unionFile.getOperatorModelID())) ;
				 
			 
				}
			}
 
			
			AnalysisHadoopUnionModel sdkJoinModel = new AnalysisHadoopUnionModel(analysisUnionItems,analysisUnionFiles,joinModel.getSetType(),joinModel.getFirstTable()) ;
			((HadoopUnionConfig) config).setUnionModel(sdkJoinModel);
		}
	}

	private static void fillHadoopVariableConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, DerivedFieldsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			DerivedFieldsModel derivedModel = DerivedFieldsModel
					.fromXMLElement(element);
			List<DerivedFieldItem> derivedFieldsList = derivedModel
					.getDerivedFieldsList();
			List<AnalysisDerivedFieldItem> sdkDerivedFieldsList = new ArrayList<AnalysisDerivedFieldItem>();
			for (DerivedFieldItem item : derivedFieldsList) {
				String columnName = VariableModelUtility
						.getReplaceValue(variableModel, item
								.getResultColumnName());
				String sqlExpression = VariableModelUtility
						.getReplaceValue(variableModel, item
								.getSqlExpression());
				sdkDerivedFieldsList.add(new AnalysisDerivedFieldItem(
						columnName, item.getDataType(), sqlExpression));
			}
			List<String> sdkSelectedFieldList = new ArrayList<String>();
			List<String> selectedFieldList = derivedModel
					.getSelectedFieldList();
			for (String s : selectedFieldList) {
				sdkSelectedFieldList.add(s);
			}

			AnalysisDerivedFieldsModel sdkDerivedModel = new AnalysisDerivedFieldsModel();
			sdkDerivedModel.setDerivedFieldsList(sdkDerivedFieldsList);
			sdkDerivedModel.setSelectedFieldList(sdkSelectedFieldList);
			((HadoopVariableConfig) config).setDerivedModel(sdkDerivedModel);
		}	
	}

	private static void fillHadoopFileConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			HadoopFileSelectorConfig config) {	
		FileStructureModel fileStructureModel=FileStructureModelFactory.
				createFileStructureModelByXML(opTypeXmlManager,operatorNode);
		((HadoopFileSelectorConfig)config).setHadoopFileStructure(fileStructureModel);
	}

	private static void fillUnivariateConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel, AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, UnivariateModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
					Element element = (Element) nodeList.get(0);
					UnivariateModel univariateModel = UnivariateModel.fromXMLElement(element);
					List<String> sdkAnalysisColumns=new ArrayList<String>();
					for(String column:univariateModel.getAnalysisColumns()){
						sdkAnalysisColumns.add(VariableModelUtility.getReplaceValue(variableModel, column));
					}
					((TableUnivariateConfig)config).setAnalysisColumn(sdkAnalysisColumns);
					String sdkReferenceColumn = VariableModelUtility.getReplaceValue(variableModel, univariateModel.getReferenceColumn());
					((TableUnivariateConfig)config).setReferenceColumn(sdkReferenceColumn);
					List<String> allSelectedColumn=new ArrayList<String>();
					allSelectedColumn.add(sdkReferenceColumn);
					allSelectedColumn.addAll(sdkAnalysisColumns);						
					((TableUnivariateConfig)config).setAllSelectedColumn(allSelectedColumn);
			}
	}

	private static void fillTableSetConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			HashMap<String, String> subflowNameUUIDMap,
			HashMap<String, VariableModel> subFlowVariableMap,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, TableSetModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {

			Element element = (Element) nodeList.get(0);
			TableSetModel tableSetModel = TableSetModel
					.fromXMLElement(element);
			String setType = tableSetModel.getSetType();
			String firstTable = tableSetModel.getFirstTable();
			VariableModel firstTableVariableModel = variableModel;
			List<ColumnMap> columnMapList = tableSetModel
					.getColumnMapList();
			List<AnalysisColumnMap> sdkColumnMapList = new ArrayList<AnalysisColumnMap>();
			for (ColumnMap columnMap : columnMapList) {
				AnalysisColumnMap sdkColumnMap = new AnalysisColumnMap();
				List<String> sdkTableColumns = new ArrayList<String>();
				for (String s : columnMap.getTableColumns()) {
					s = VariableModelUtility.getReplaceValue(
							variableModel, s);
					sdkTableColumns.add(s);
				}
				String uuid = columnMap.getOperatorUUID();
				VariableModel realVariableModel = variableModel;
				if (subflowNameUUIDMap.keySet().contains(uuid)
						&& subFlowVariableMap.keySet().contains(
								subflowNameUUIDMap.get(uuid))) {
					realVariableModel = subFlowVariableMap
							.get(subflowNameUUIDMap.get(uuid));
				}// MINER-1992
				if (setType.equals(TableSetModel.TABLE_SET_TYPE[3])
						&&firstTable.equals(StringHandler.doubleQ(columnMap
						.getSchemaName())
						+ "."
						+ StringHandler.doubleQ(columnMap
								.getTableName()))) {
					firstTableVariableModel = realVariableModel;
				}
				String schema = VariableModelUtility.getReplaceValue(
						realVariableModel, columnMap.getSchemaName());
				String table = VariableModelUtility.getReplaceValue(
						realVariableModel, columnMap.getTableName());

				sdkColumnMap.setSchemaName(schema);
				sdkColumnMap.setTableName(table);
				sdkColumnMap.setTableColumns(sdkTableColumns);
				sdkColumnMapList.add(sdkColumnMap);
			}
			AnalysisTableSetModel sdkTableSetModel = new AnalysisTableSetModel();
			sdkTableSetModel.setColumnMapList(sdkColumnMapList);

			if(setType.equals(TableSetModel.TABLE_SET_TYPE[3])){
				firstTable = VariableModelUtility.getReplaceValue(
						firstTableVariableModel, firstTable);

				sdkTableSetModel.setFirstTable(firstTable);
			}
			sdkTableSetModel.setSetType(tableSetModel.getSetType());

			((TableSetConfig) config)
					.setTableSetModel(sdkTableSetModel);
		}
	}

	private static void fillWOEConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, WOETable.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			WOETable woeTable = WOETable.fromXMLElement(element);
			List<AnalysisWOEColumnInfo> sdkWOEInfoList = new ArrayList<AnalysisWOEColumnInfo>();
			List<WOEInforList> woeInfoList = woeTable.getDataTableWOE();
			for (WOEInforList woeInfo : woeInfoList) {
				AnalysisWOEColumnInfo sdkWOEInfo = new AnalysisWOEColumnInfo();
				sdkWOEInfoList.add(sdkWOEInfo);
				sdkWOEInfo.setColumnName(woeInfo.getColumnName());
				sdkWOEInfo.setGini(woeInfo.getGini());
				sdkWOEInfo.setInforValue(woeInfo.getInforValue());
				List<AnalysisWOENode> sdkWOENodeList = new ArrayList<AnalysisWOENode>();
				sdkWOEInfo.setInforList(sdkWOENodeList);
				List<WOENode> woeNodeList = woeInfo.getInforList();
				for (int i = 0; i < woeNodeList.size(); i++) {
					if (woeNodeList.get(i) instanceof WOENumericNode) {
						WOENumericNode woeNumericNode = (WOENumericNode) woeNodeList
								.get(i);
						AnalysisWOENumericNode sdkWOENumericNode = new AnalysisWOENumericNode();
						if (!StringUtil.isEmpty(woeNumericNode
								.getBottom())) {
							String bottom = VariableModelUtility
									.getReplaceValue(variableModel,
											woeNumericNode.getBottom());
							sdkWOENumericNode.setBottom(Double
									.parseDouble(bottom));
						}
						sdkWOENumericNode.setGroupInfror(woeNumericNode
								.getGroupInfo());
						if (!StringUtil.isEmpty(woeNumericNode
								.getUpper())) {
							String upper = VariableModelUtility
									.getReplaceValue(variableModel,
											woeNumericNode.getUpper());
							sdkWOENumericNode.setUpper(Double
									.parseDouble(upper));
						}
						sdkWOENumericNode.setWOEValue(woeNumericNode
								.getWOEValue());
						sdkWOENodeList.add(sdkWOENumericNode);
					} else if (woeNodeList.get(i) instanceof WOENominalNode) {
						WOENominalNode woeNominalNode = (WOENominalNode) woeNodeList
								.get(i);
						AnalysisWOENominalNode sdkWOENominalNode = new AnalysisWOENominalNode();
						sdkWOENominalNode.setChoosedList(woeNominalNode
								.getChoosedList());
						sdkWOENominalNode.setGroupInfror(woeNominalNode
								.getGroupInfo());
						sdkWOENominalNode.setWOEValue(woeNominalNode
								.getWOEValue());
						sdkWOENodeList.add(sdkWOENominalNode);
					}
				}
			}
			AnalysisWOETable sdkWOETable = new AnalysisWOETable();
			sdkWOETable.setDataTableWOE(sdkWOEInfoList);
			((WeightOfEvidenceConfig) config)
					.setWOETableInfor(sdkWOETable);
		}
	}

	private static void fillNeuralNetworkConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, HiddenLayersModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			HiddenLayersModel hiddenLayersModel = HiddenLayersModel
					.fromXMLElement(element);
			List<AnalysisHiddenLayer> sdkHiddenLayers = new ArrayList<AnalysisHiddenLayer>();
			List<HiddenLayer> hiddenLayers = hiddenLayersModel
					.getHiddenLayers();
			for (HiddenLayer hiddenLayer : hiddenLayers) {
				String layerSize = hiddenLayer.getLayerSize();
				layerSize = VariableModelUtility.getReplaceValue(
						variableModel, layerSize);
				sdkHiddenLayers.add(new AnalysisHiddenLayer(hiddenLayer
						.getLayerName(), Integer.parseInt(layerSize)));
			}
			AnalysisHiddenLayersModel sdkHiddenLayersModel = new AnalysisHiddenLayersModel();

			sdkHiddenLayersModel.setHiddenLayers(sdkHiddenLayers);
			((NeuralNetworkConfig) config)
					.setHiddenLayersModel(sdkHiddenLayersModel);
		}
	}

	private static void fillFPGrowthConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, ExpressionModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			ExpressionModel expressionModel = ExpressionModel
					.fromXMLElement(element);
			String positiveValue = VariableModelUtility
					.getReplaceValue(variableModel, expressionModel
							.getPositiveValue());
			AnalysisExpressionModel skdexpressionModel = new AnalysisExpressionModel(
					positiveValue, expressionModel.getExpression());
			((FPGrowthConfig) config)
					.setExpressionModel(skdexpressionModel);
		}
	}

	private static void fillNullValueReplacementConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, NullReplacementModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			NullReplacementModel nullReplacementModel = NullReplacementModel
					.fromXMLElement(element);
			List<AnalysisNullReplacementItem> sdkNullReplacements = new ArrayList<AnalysisNullReplacementItem>();
			List<NullReplacementItem> nullReplacements = nullReplacementModel
					.getNullReplacements();
			for (NullReplacementItem nullReplacementItem : nullReplacements) {
				String value = VariableModelUtility.getReplaceValue(
						variableModel, nullReplacementItem.getValue());
				String type = VariableModelUtility.getReplaceValue(
						variableModel, nullReplacementItem.getType());
				sdkNullReplacements
						.add(new AnalysisNullReplacementItem(
								nullReplacementItem.getColumnName(),
								value,type));
			}
			AnalysisNullReplacementModel sdkNullReplacementModel = new AnalysisNullReplacementModel();

			String groupBy = nullReplacementModel.getGroupBy();

            sdkNullReplacementModel.setGroupBy(groupBy);
            sdkNullReplacementModel.setNullReplacements(sdkNullReplacements);

			if(config instanceof HadoopReplaceNullConfig){
					((HadoopReplaceNullConfig) config)
				.setNullReplacementModel(sdkNullReplacementModel);
			}else{
				((ReplaceNullConfig) config)
					.setNullReplacementModel(sdkNullReplacementModel);
			}
		}
	}

	private static void fillHistogramConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, ColumnBinsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			ColumnBinsModel columnBinsModel = ColumnBinsModel
					.fromXMLElement(element);
			List<AnalysisColumnBin> sdkColumnBins = new ArrayList<AnalysisColumnBin>();
			List<ColumnBin> columnBins = columnBinsModel
					.getColumnBins();
			for (ColumnBin columnBin : columnBins) {
				AnalysisColumnBin sdkColumnBin = null;
				if (!StringUtil.isEmpty(columnBin.getBin())) {
					String bin = VariableModelUtility.getReplaceValue(
							variableModel, columnBin.getBin());
					sdkColumnBin = new AnalysisColumnBin(columnBin
							.getColumnName(), Integer.parseInt(bin));

					sdkColumnBin.setType(columnBin.getType());
					if (!StringUtil.isEmpty(columnBin.getWidth())) {
						String width = VariableModelUtility
								.getReplaceValue(variableModel,
										columnBin.getWidth());
						sdkColumnBin
								.setWidth(Double.parseDouble(width));
					}
					sdkColumnBin.setIsMax(columnBin.isMax());
					sdkColumnBin.setIsMin(columnBin.isMin());
					if (!StringUtil.isEmpty(columnBin.getMin())) {
						String min = VariableModelUtility
								.getReplaceValue(variableModel,
										columnBin.getMin());
						sdkColumnBin.setMin(Double.parseDouble(min));
					}
					if (!StringUtil.isEmpty(columnBin.getMax())) {
						String max = VariableModelUtility
								.getReplaceValue(variableModel,
										columnBin.getMax());
						sdkColumnBin.setMax(Double.parseDouble(max));
					}
					sdkColumnBins.add(sdkColumnBin);
				}
			}
			AnalysisColumnBinsModel sdkColumnBinsModel = new AnalysisColumnBinsModel();
			sdkColumnBinsModel.setColumnBins(sdkColumnBins);
			((HistogramAnalysisConfig) config)
					.setColumnBinModel(sdkColumnBinsModel);
		}
	}

	private static void fillAggregateConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, AggregateFieldsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			AggregateFieldsModel aggFieldsModel = AggregateFieldsModel
					.fromXMLElement(element);
			List<AggregateField> aggregateFieldList = aggFieldsModel
					.getAggregateFieldList();
			List<AnalysisAggregateField> sdkAggregateFieldList = new ArrayList<AnalysisAggregateField>();
			for (AggregateField aggregateField : aggregateFieldList) {
				String aggregateExpression = VariableModelUtility
						.getReplaceValue(variableModel, aggregateField
								.getAggregateExpression());
				String aggregateFieldName = VariableModelUtility
						.getReplaceValue(variableModel, aggregateField
								.getAlias());
				sdkAggregateFieldList.add(new AnalysisAggregateField(
						aggregateFieldName, aggregateExpression,
						aggregateField.getDataType()));
			}
			List<String> groupByList = aggFieldsModel
					.getGroupByFieldList();
			List<String> sdkGroupByList = new ArrayList<String>();
			for (String s : groupByList) {
				s = VariableModelUtility.getReplaceValue(variableModel,
						s);
				sdkGroupByList.add(s);
			}
			List<String> parentFieldList = aggFieldsModel
					.getParentFieldList();
			List<String> sdkParentFieldList = new ArrayList<String>();
			for (String s : parentFieldList) {
				s = VariableModelUtility.getReplaceValue(variableModel,
						s);
				sdkParentFieldList.add(s);
			}
			AnalysisAggregateFieldsModel sdkAggFieldsModel = new AnalysisAggregateFieldsModel();
			sdkAggFieldsModel
					.setAggregateFieldList(sdkAggregateFieldList);
			sdkAggFieldsModel.setGroupByFieldList(sdkGroupByList);
			sdkAggFieldsModel.setParentFieldList(sdkParentFieldList);
			if(config instanceof AggregateConfig){
				((AggregateConfig) config)
				.setAggregateFieldsModel(sdkAggFieldsModel);
			
			}else{
				((HadoopAggregaterConfig) config)
				.setAggregateFieldsModel(sdkAggFieldsModel);
				
			}
		}
		nodeList = opTypeXmlManager.getNodeList(operatorNode,
				WindowFieldsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			WindowFieldsModel windowFieldsModel = WindowFieldsModel
					.fromXMLElement(element);
			List<WindowField> winFieldList = windowFieldsModel
					.getWindowFieldList();
			List<AnalysisWindowField> sdkWinFieldList = new ArrayList<AnalysisWindowField>();
			for (WindowField winField : winFieldList) {
				String windowSpecification = VariableModelUtility
						.getReplaceValue(variableModel, winField
								.getWindowSpecification());
				String windowResultColumn = VariableModelUtility
						.getReplaceValue(variableModel, winField
								.getResultColumn());
				String WindowFunction = VariableModelUtility
						.getReplaceValue(variableModel, winField
								.getWindowFunction());
				sdkWinFieldList.add(new AnalysisWindowField(
						windowResultColumn, WindowFunction,
						windowSpecification, winField.getDataType()));
			}

			AnalysisWindowFieldsModel sdkWindowFieldsModel = new AnalysisWindowFieldsModel();
			sdkWindowFieldsModel.setWindowFieldList(sdkWinFieldList);
			((AggregateConfig) config)
					.setWindowFieldsModel(sdkWindowFieldsModel);
		}
	}

	private static void fillLogisticRegressionConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, InterActionColumnsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			InterActionColumnsModel interActionModel = InterActionColumnsModel
					.fromXMLElement(element);
			List<InterActionItem> interActionItems = interActionModel
					.getInterActionItems();
			List<AnalysisInterActionItem> sdkInterActionItems = new ArrayList<AnalysisInterActionItem>();
			for (InterActionItem item : interActionItems) {
				AnalysisInterActionItem sdkItem = new AnalysisInterActionItem(
						item.getId(), item.getFirstColumn(), item
								.getSecondColumn(), item
								.getInteractionType());
				sdkInterActionItems.add(sdkItem);
			}
			AnalysisInterActionColumnsModel sdkModel = new AnalysisInterActionColumnsModel();
			sdkModel.setInterActionItems(sdkInterActionItems);
			if(config instanceof LogisticRegressionConfigGeneral){
				((LogisticRegressionConfigGeneral) config)
					.setInterActionModel(sdkModel);
			}else if(config instanceof HadoopLogisticRegressionConfig){
				((HadoopLogisticRegressionConfig) config)
				.setInterActionModel(sdkModel);
		}
		}
	}

	private static void fillLinearRegressionConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, InterActionColumnsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			InterActionColumnsModel interActionModel = InterActionColumnsModel
					.fromXMLElement(element);
			List<InterActionItem> interActionItems = interActionModel
					.getInterActionItems();
			List<AnalysisInterActionItem> sdkInterActionItems = new ArrayList<AnalysisInterActionItem>();
			for (InterActionItem item : interActionItems) {
				AnalysisInterActionItem sdkItem = new AnalysisInterActionItem(
						item.getId(), item.getFirstColumn(), item
								.getSecondColumn(), item
								.getInteractionType());
				sdkInterActionItems.add(sdkItem);
			}
			AnalysisInterActionColumnsModel sdkModel = new AnalysisInterActionColumnsModel();
			sdkModel.setInterActionItems(sdkInterActionItems);
			if(config instanceof LinearRegressionConfig){
				((LinearRegressionConfig) config).setInterActionModel(sdkModel);
			}
			else if(config instanceof HadoopLinearTrainConfig){
				((HadoopLinearTrainConfig) config).setInterActionModel(sdkModel);
			}
		}
	}

	private static void fillVariableConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, DerivedFieldsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			DerivedFieldsModel derivedModel = DerivedFieldsModel
					.fromXMLElement(element);
			List<DerivedFieldItem> derivedFieldsList = derivedModel
					.getDerivedFieldsList();
			List<AnalysisDerivedFieldItem> sdkDerivedFieldsList = new ArrayList<AnalysisDerivedFieldItem>();
			for (DerivedFieldItem item : derivedFieldsList) {
				String columnName = VariableModelUtility
						.getReplaceValue(variableModel, item
								.getResultColumnName());
				String sqlExpression = VariableModelUtility
						.getReplaceValue(variableModel, item
								.getSqlExpression());
				sdkDerivedFieldsList.add(new AnalysisDerivedFieldItem(
						columnName, item.getDataType(), sqlExpression));
			}
			List<String> sdkSelectedFieldList = new ArrayList<String>();
			List<String> selectedFieldList = derivedModel
					.getSelectedFieldList();
			for (String s : selectedFieldList) {
				sdkSelectedFieldList.add(s);
			}

			AnalysisDerivedFieldsModel sdkDerivedModel = new AnalysisDerivedFieldsModel();
			sdkDerivedModel.setDerivedFieldsList(sdkDerivedFieldsList);
			sdkDerivedModel.setSelectedFieldList(sdkSelectedFieldList);
			((VariableConfig) config).setDerivedModel(sdkDerivedModel);
		}
		nodeList = opTypeXmlManager.getNodeList(operatorNode,
				QuantileFieldsModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			QuantileFieldsModel quantileModel = QuantileFieldsModel
					.fromXMLElement(element);
			List<QuantileItem> quantileItems = quantileModel
					.getQuantileItems();
			List<AnalysisQuantileItem> sdkQuantileItems = new ArrayList<AnalysisQuantileItem>();
			for (QuantileItem item : quantileItems) {
				AnalysisQuantileItem quantileItem = new AnalysisQuantileItem();
				quantileItem.setColumnName(item.getColumnName());
				quantileItem.setIsCreateNewColumn(item
						.isCreateNewColumn());
				quantileItem.setNewColumnName(item.getNewColumnName());
				quantileItem.setNumberOfBin(item.getNumberOfBin());
				quantileItem.setQuantileType(item.getQuantileType());
				sdkQuantileItems.add(quantileItem);
				List<QuantileItemBin> quantileItemBinList = item
						.getBins();
				for (QuantileItemBin quantileItemBin : quantileItemBinList) {
					if (quantileItemBin instanceof QuantileItemBinNumeric) {
						QuantileItemBinNumeric quantileItemBinNumeric = (QuantileItemBinNumeric) quantileItemBin;
						AnalysisQuantileItemBinNumeric sdkQuantileItemBinNumeric = new AnalysisQuantileItemBinNumeric();
						sdkQuantileItemBinNumeric
								.setBinIndex(quantileItemBinNumeric
										.getBinIndex());
						sdkQuantileItemBinNumeric
								.setBinType(quantileItemBinNumeric
										.getBinType());
						String endTo = VariableModelUtility
								.getReplaceValue(variableModel,
										quantileItemBinNumeric
												.getEndTo());
						if (!StringUtil.isEmpty(endTo)) {
							sdkQuantileItemBinNumeric.setEndTo(Double
									.parseDouble(endTo));
						}
						String startFrom = VariableModelUtility
								.getReplaceValue(variableModel,
										quantileItemBinNumeric
												.getStartFrom());
						if (!StringUtil.isEmpty(endTo)) {
							sdkQuantileItemBinNumeric
									.setStartFrom(Double
											.parseDouble(startFrom));
						}
						ArrayList<Double> values = new ArrayList<Double>();
						if (quantileItemBinNumeric.getValues() != null) {
							for (String s : quantileItemBinNumeric
									.getValues()) {
								String value = VariableModelUtility
										.getReplaceValue(variableModel,
												s);
								values.add(Double.parseDouble(value));
							}
						}
						sdkQuantileItemBinNumeric.setValues(values);
						quantileItem
								.AddBinItem(sdkQuantileItemBinNumeric);
					} else if (quantileItemBin instanceof QuantileItemBinCategory) {
						QuantileItemBinCategory quantileItemBinCategory = (QuantileItemBinCategory) quantileItemBin;
						AnalysisQuantileItemBinCategory sdkQuantileItemBinCategory = new AnalysisQuantileItemBinCategory();
						sdkQuantileItemBinCategory
								.setBinIndex(quantileItemBinCategory
										.getBinIndex());
						sdkQuantileItemBinCategory
								.setBinType(quantileItemBinCategory
										.getBinType());
						ArrayList<String> values = new ArrayList<String>();
						if (quantileItemBinCategory.getValues() != null) {
							for (String s : quantileItemBinCategory
									.getValues()) {
								String value = VariableModelUtility
										.getReplaceValue(variableModel,
												s);
								values.add(value);
							}
						}
						sdkQuantileItemBinCategory.setValues(values);
						quantileItem
								.AddBinItem(sdkQuantileItemBinCategory);
					} else if (quantileItemBin instanceof QuantileItemBinDateTime) {
						QuantileItemBinDateTime quantileItemBinDateTime = (QuantileItemBinDateTime) quantileItemBin;
						AnalysisQuantileItemBinDateTime sdkQuantileItemBinDateTime = new AnalysisQuantileItemBinDateTime();
						sdkQuantileItemBinDateTime
								.setBinIndex(quantileItemBinDateTime
										.getBinIndex());
						sdkQuantileItemBinDateTime
								.setBinType(quantileItemBinDateTime
										.getBinType());
						sdkQuantileItemBinDateTime
								.setStartDate(quantileItemBinDateTime
										.getStartDate());
						sdkQuantileItemBinDateTime
								.setStartTime(quantileItemBinDateTime
										.getStartTime());
						sdkQuantileItemBinDateTime
								.setEndDate(quantileItemBinDateTime
										.getEndDate());
						sdkQuantileItemBinDateTime
								.setEndTime(quantileItemBinDateTime
										.getEndTime());
						ArrayList<String> values = new ArrayList<String>();
						values.addAll(quantileItemBinDateTime
								.getValues());
						sdkQuantileItemBinDateTime.setValues(values);
						quantileItem
								.AddBinItem(sdkQuantileItemBinDateTime);
					}
				}
			}

			AnalysisQuantileFieldsModel skdQuantileFieldsModel = new AnalysisQuantileFieldsModel();
			skdQuantileFieldsModel.setQuantileItems(sdkQuantileItems);

			((VariableConfig) config)
					.setQuantileModel(skdQuantileFieldsModel);
		}
	}

	private static void fillTableJoinConfig(Node operatorNode,
			XmlDocManager opTypeXmlManager, VariableModel variableModel,
			HashMap<String, String> subflowNameUUIDMap,
			HashMap<String, VariableModel> subFlowVariableMap,
			AnalyticConfiguration config) {
		ArrayList<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, TableJoinModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			TableJoinModel tableJoinDefinition = TableJoinModel
					.fromXMLElement(element);
			List<AnalysisJoinColumn> sdkJoinColumns = new ArrayList<AnalysisJoinColumn>();
			List<JoinColumn> joinColumns = tableJoinDefinition
					.getJoinColumns();
			for (JoinColumn joinColumn : joinColumns) {
				String tableAlias = VariableModelUtility
						.getReplaceValue(variableModel, joinColumn
								.getTableAlias());
				String columnName = VariableModelUtility
						.getReplaceValue(variableModel, joinColumn
								.getColumnName());
				String newColumnName = VariableModelUtility
						.getReplaceValue(variableModel, joinColumn
								.getNewColumnName());
				sdkJoinColumns.add(new AnalysisJoinColumn(tableAlias,
						columnName, newColumnName, joinColumn
								.getColumnType()));
			}
			List<AnalysisJoinCondition> sdkJoinConditions = new ArrayList<AnalysisJoinCondition>();
			List<JoinCondition> joinConditions = tableJoinDefinition
					.getJoinConditions();
			for (JoinCondition joinCondition : joinConditions) {
				String tableAlias1 = VariableModelUtility
						.getReplaceValue(variableModel, joinCondition
								.getTableAlias1());
				String tableAlias2 = VariableModelUtility
						.getReplaceValue(variableModel, joinCondition
								.getTableAlias2());
				String conditionStr = VariableModelUtility
						.getReplaceValue(variableModel, joinCondition
								.getCondition());
				String column1 = VariableModelUtility.getReplaceValue(
						variableModel, joinCondition.getColumn1());
				String column2 = VariableModelUtility.getReplaceValue(
						variableModel, joinCondition.getColumn2());

				AnalysisJoinCondition condition = new AnalysisJoinCondition(
						tableAlias1, joinCondition.getJoinType(),
						tableAlias2, conditionStr, joinCondition
								.getAndOr());
				condition.setColumn1(column1);
				condition.setColumn2(column2);
				sdkJoinConditions.add(condition);
			}
			List<AnalysisJoinTable> sdkJoinTables = new ArrayList<AnalysisJoinTable>();
			List<JoinTable> joinTables = tableJoinDefinition
					.getJoinTables();
			for (JoinTable joinTable : joinTables) {
				String alias = VariableModelUtility.getReplaceValue(
						variableModel, joinTable.getAlias());

				String uuid = joinTable.getOperatorModelID();
				VariableModel realVariableModel = variableModel;
				if (subflowNameUUIDMap.keySet().contains(uuid)
						&& subFlowVariableMap.keySet().contains(
								subflowNameUUIDMap.get(uuid))) {
					realVariableModel = subFlowVariableMap
							.get(subflowNameUUIDMap.get(uuid));
				}// MINER-1992
				String schema = VariableModelUtility.getReplaceValue(
						realVariableModel, joinTable.getSchema());
				String table = VariableModelUtility.getReplaceValue(
						realVariableModel, joinTable.getTable());

				sdkJoinTables.add(new AnalysisJoinTable(schema, table,
						alias, joinTable.getOperatorModelID()));
			}
			AnalysisTableJoinModel sdkTableJoinDefinition = new AnalysisTableJoinModel();
			sdkTableJoinDefinition.setJoinColumns(sdkJoinColumns);
			sdkTableJoinDefinition.setJoinConditions(sdkJoinConditions);
			sdkTableJoinDefinition.setJoinTables(sdkJoinTables);
			((TableJoinConfig) config)
					.setTableJoinDef(sdkTableJoinDefinition);
		}
	}

}
