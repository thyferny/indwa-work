/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * OperatorDTO.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 20, 2011
 */

package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;
import com.alpine.datamining.api.impl.db.attribute.model.customized.ParameterModel;
import com.alpine.miner.impls.controller.PropertyDTO.PropertyType;
import com.alpine.miner.impls.editworkflow.operator.SvdOperatorUtility;
import com.alpine.miner.impls.flowvariables.model.FlowVariable;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.operator.dataset.DataSetWebModel;
import com.alpine.miner.impls.web.resource.operator.dataset.DatasetTransformationException;
import com.alpine.miner.impls.web.resource.operator.ordinary.OutputCreationParameterWebModel;
import com.alpine.miner.impls.web.resource.operator.pigexecute.PigExecuteOperatorUIModel;
import com.alpine.miner.impls.web.resource.operator.sampling.SampleSizeModelUI;
import com.alpine.miner.impls.woe.WOEModelUI;
import com.alpine.miner.model.uitype.AbstractUIControlType;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.association.AssociationOperator;
import com.alpine.miner.workflow.operator.customize.CustomizedOperator;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.datasource.TableJoinOperator;
import com.alpine.miner.workflow.operator.field.*;
import com.alpine.miner.workflow.operator.hadoop.*;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.woe.WOEOperator;
import com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkOperator;
import com.alpine.miner.workflow.operator.parameter.*;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBin;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinColumn;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinCondition;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinFile;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterFactory;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterHelper;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayersModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementItem;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.miner.workflow.operator.parameter.pigexe.PigExecutableModel;
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.miner.workflow.operator.parameter.storageparam.StorageParameterModel;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingItem;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinTable;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.tableset.TableSetModel;
import com.alpine.miner.workflow.operator.parameter.univariate.UnivariateModel;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.miner.workflow.operator.parameter.woe.WOETable;
import com.alpine.miner.workflow.operator.sampling.AbstractSamplingOperator;
import com.alpine.miner.workflow.operator.sampling.RandomSamplingOperator;
import com.alpine.miner.workflow.operator.sampling.StratifiedSamplingOperator;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosCalculatorOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author sam_zang
 * 
 */
public class
        OperatorDTO {
    private static Logger itsLogger = Logger.getLogger(OperatorDTO.class);

    private static final String dbConnectionName = "dbConnectionName";
	private static final String schemaName = "schemaName";
	private static final String tableName = "tableName";
 

	private List<OperatorInputTableInfo> inputTableInfos = null;
	private List<OperatorInputFileInfo> inputFileInfos = null;
 
	/**
	 * @return the inputFileInfos
	 */
	public List<OperatorInputFileInfo> getInputFileInfos() {
		return inputFileInfos;
	}

	private Locale locale; 

	public List<OperatorInputTableInfo> getInputTableInfos() {
		return inputTableInfos;
	}

	public OperatorDTO(String user, FlowInfo info, UIOperatorModel op, List<VariableModel> variableModelList, Locale locale) throws Exception {
		this.setFlowInfo(info);
		this.setUuid(op.getUUID());
		this.locale=locale;
		this.classname = op.getClassName();
//		if(this.classname.equals("ModelOperator")){
//			this.modelType =getModelType((ModelOperator)op.getOperator());
//		}
		
		this.propertyList = new LinkedList<PropertyDTO>();
		this.isValid = op.getOperator().isVaild(variableModelList.get(0));
		if (this.isValid == false) {
			String[] list = op.getOperator().getInvalidParameters();
			if (list != null && list.length > 0) {
				String[] displaylist = new String[list.length];
				int idx = 0;
				for (String name : list) {
					//MINERWEB-1018
//					if(null!=name && name.indexOf("==")!=-1){
//						String[] names = name.split("==");
//						displaylist[idx++] = PropertyUtil.getDisplayName(names[0],locale)+PropertyUtil.getDisplayName(names[1],locale);
//					}else{
//						displaylist[idx++] = PropertyUtil.getDisplayName(name,locale);
//					}
                    if (name != null && !name.trim().isEmpty()) displaylist[idx++] = name;   //passing regular name, as we're not going to list them in the front any more.
				}
				this.setInvalidPropertyList(displaylist);
			}
		}
		
		// initDataSource(user, info, op);
		if (op.getOperator() instanceof DbTableOperator) {			
			OperatorParameter param = null;
			
			param = getParamByName(op, dbConnectionName);			
			this.propertyList.add(new PropertyDTO((String) param.getValue(),
					param,user,info.getResourceType(),locale));
			
			param = getParamByName(op, schemaName);	
			this.propertyList.add(new PropertyDTO((String) param.getValue(),
					param,user,info.getResourceType(),locale));
			
			param = getParamByName(op, tableName);	
			this.propertyList.add(new PropertyDTO((String) param.getValue(),
					param,user,info.getResourceType(),locale));
		} else {
			List<OperatorParameter> params = op.getOperator().getOperatorParameterList();
			for (OperatorParameter p : params) {
				initPropertyListWithNewAPI(user, op, p, info.getResourceType());
				initCustomProperty(user, op, p, info.getResourceType());
			}
			if(op.getOperator() instanceof CustomizedOperator){
				initPropertyListForUDF(user, op , info.getResourceType(),(CustomizedOperator) op.getOperator() );
			}
			
			
//			initPropertyListWithNewAPI(user, op , info.getResourceType());
			//CustomizedOperator need add more special stuff
//			if(op.getOperator() instanceof CustomizedOperator){
//				initPropertyListForUDF(user, op , info.getResourceType(),(CustomizedOperator) op.getOperator() );
//			}
		}

//		if (info != null) {
//			initCustomProperty(user, op,info.getResourceType());
//		}

        // add input info for the operator, if it has the table input
        List<Object> operatorInputList = op.getOperator()
                .getOperatorInputList();
		// TODO: if an API returns a list, it should not be null.
		if (operatorInputList == null) {
			return;
		}
		List<OperatorInputTableInfo> inputTableInfos = new ArrayList<OperatorInputTableInfo>();
		List<OperatorInputFileInfo> inputFileInfos = new ArrayList<OperatorInputFileInfo>();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				inputTableInfos.add((OperatorInputTableInfo) obj);
			}else if(obj instanceof OperatorInputFileInfo){
//				TODO build inputHadoopInfos, need better way to assemble up with inputTableInfos
				inputFileInfos.add((OperatorInputFileInfo) obj);
			}
		}
		if (inputTableInfos != null && inputTableInfos.size() > 0) {
			this.inputTableInfos = inputTableInfos;
		}
		if (inputFileInfos != null && inputFileInfos.size() > 0) {
			this.inputFileInfos = inputFileInfos;
		}
	}



	private void initPropertyListForUDF(String user, UIOperatorModel op,
			ResourceType resourceType, CustomizedOperator operator) throws Exception {
		CustomizedOperatorModel model = operator.getCoModel();
		HashMap<String, ParameterModel> paramMap = model.getParaMap();
		for (Iterator iterator = paramMap.values().iterator(); iterator.hasNext();) {
			ParameterModel paramModel = (ParameterModel) iterator.next();
			String parameterName = paramModel.getParaName(); 
			String pValue = "";
			if(operator.getOperatorParameter(parameterName).getValue()!=null){
				pValue = operator.getOperatorParameter(parameterName).getValue().toString();
			}
			 
			  OperatorParameterHelper helper = OperatorParameterFactory.INSTANCE.getHelperByParamName(parameterName);
			PropertyDTO pDTO = findDTOByParamName(parameterName); 
			OperatorParameter p = operator.getOperatorParameter(parameterName);
			if(pDTO == null){
				  pDTO = new PropertyDTO((String)pValue,   p, user,  resourceType ,locale );
					this.propertyList.add(pDTO);
			} 
//			else{
//				pDTO.setValue(pValue)	;
//			}
			pDTO.setAvailableValues(helper.getAvaliableValues(p, user, resourceType,locale));
			
			if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.UDF_COLUMNNAME_CONTROL_TYPE)){
				pDTO.setType(PropertyType.PT_MULTI_SELECT);//column name
				
				 
			} //true false type
			else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.CHECK_CONTROL_TYPE)){
				pDTO.setType(PropertyType.PT_BOOLEAN);
			}
			else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.TEXT_CONTROL_TYPE)){
				pDTO.setType(PropertyType.PT_STRING);
			} //true false type
			else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.COMBO_CONTROL_TYPE)){
				pDTO.setType(PropertyType.PT_SINGLE_SELECT);
			}
			
			
		
		}
		
		
	}

	private PropertyDTO findDTOByParamName(String paramName) {
		for (Iterator iterator = this.propertyList.iterator(); iterator.hasNext();) {
			PropertyDTO propertyDTO = ( PropertyDTO) iterator.next();
			if(propertyDTO.getName().equals(paramName)){
				return  propertyDTO;
			}
			
		}
		return null;
	}

 

	/**
	 * @param op
	 * @param resourceType 
	 * @throws Exception 
	 */
	private void initPropertyListWithNewAPI(String user, UIOperatorModel op, OperatorParameter p, ResourceType resourceType) throws Exception {

		//Entry<String, String> entry = it.next();
		String pKey = p.getName().trim();
		Object pValue = p.getValue();

		if (pKey.equals(OperatorParameter.NAME_outputType) 
				&& op.getOperator() instanceof AbstractSamplingOperator) {
			return;
		}

		if (pValue == null || pValue instanceof String) {
			PropertyDTO pDTO = new PropertyDTO((String)pValue,   p, user,  resourceType  ,locale);
			OperatorParameterHelper helper = OperatorParameterFactory.INSTANCE.getHelper(p);
            pDTO.setAvailableValues(helper.getAvaliableValues(p, user, resourceType,locale));
            //for copytodb operator have no @defaultschema
            if(op.getOperator() instanceof CopyToDBOperator && null!=pDTO && "schemaName".equals(pDTO.getName())){
                if(null!=pDTO.getValue() && pDTO.getValue().equals("@default_schema")==true){
                    pDTO.setValue("");
                }
                List<String> selection = Arrays.asList(pDTO.getFullSelection());
                if(null!=selection){
                   // selection.remove("@default_schema");
                    List<String> newSchemaList = new ArrayList<String>();
                    for(int i=0;i<selection.size();i++){
                            if(selection.get(i).equals("@default_schema")==false){
                                newSchemaList.add(selection.get(i));
                            }
                    }
                    if(newSchemaList.size()!=0){
                        String[] newSchemas = new String[newSchemaList.size()];
                        for(int i=0;i<newSchemaList.size();i++){
                            newSchemas[i] = newSchemaList.get(i);
                        }
                        pDTO.setFullSelection(newSchemas);
                    }
                }
            }
            //end
			this.propertyList.add(pDTO);
		}
		
		
		
		// System.out.println("Operator Class: " + op.getClassName());
//		List<OperatorParameter> params = op.getOperator().getOperatorParameterList();
//
//		for (OperatorParameter p : params) {
//			//Entry<String, String> entry = it.next();
//			String pKey = p.getName().trim();
//			Object pValue = p.getValue();
//
//			if (pKey.equals(OperatorParameter.NAME_outputType) 
//					&& op.getOperator() instanceof AbstractSamplingOperator) {
//				continue;
//			}
//
//			if (pValue == null || pValue instanceof String) {
//				PropertyDTO pDTO = new PropertyDTO((String)pValue,   p, user,  resourceType  ,locale);
//				OperatorParameterHelper helper = OperatorParameterFactory.INSTANCE.getHelper(p);
//				pDTO.setAvailableValues(helper.getAvaliableValues(p, user, resourceType,locale));
//				this.propertyList.add(pDTO);
//			}
//		}
	}

	private void initCustomProperty(String user, UIOperatorModel op, OperatorParameter param, ResourceType resourceType) {
		PropertyDTO pDTO = null;
//		OperatorParameter param = null;
//		String paramName = null;

		// here handle the special things
		if (op.getOperator() instanceof TableJoinOperator) {
//			paramName = OperatorParameter.NAME_Set_Table_Join_Parameters;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_Set_Table_Join_Parameters.equals(param.getName())) {
				TableJoinModel obj = (TableJoinModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setTableJoinModel(obj);

			}
		} else if (op.getOperator() instanceof VariableOperator) {
//			paramName = OperatorParameter.NAME_quantileFieldList;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_quantileFieldList.equals(param.getName())) {
				QuantileFieldsModel obj = (QuantileFieldsModel) param
						.getValue();
				QuantileFieldsModelUI uiObj = new QuantileFieldsModelUI(obj);
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setQuantileFieldsModel(uiObj);
				 
			}

//			paramName = OperatorParameter.NAME_fieldList;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_fieldList.equals(param.getName())) {
				DerivedFieldsModel obj = (DerivedFieldsModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setDerivedFieldsModel(obj);
			}
		} else if (op.getOperator() instanceof AggregateOperator) {
//			paramName = OperatorParameter.NAME_aggregateFieldList;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_aggregateFieldList.equals(param.getName())) {
				AggregateFieldsModel obj = (AggregateFieldsModel) param
						.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				if (obj != null) {
					pDTO.setAggregateFieldsModel(obj);
	
				
				}
				
				// break aggregate column config into 2 popups.
				String paramName = OperatorParameter.NAME_groupByColumn;
				pDTO = new PropertyDTO(paramName, null,locale);
				if (obj != null) {
					pDTO.setAggregateFieldsModel(obj);
				}
				// pDTO.setAvailableValues(obj.getParentFieldList());
				// work around for JIRA issue MINERWEB-497
				// The parentFieldList is not updating after
				// parent operator table has changed.
				String[] list = PropertyUtil.getDefaultValues(param, user, resourceType,locale);
				pDTO.setAvailableValues(Arrays.asList(list));
				this.propertyList.add(pDTO);
			}

//			paramName = OperatorParameter.NAME_windowFieldList;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_windowFieldList.equals(param.getName())) {
				WindowFieldsModel obj = (WindowFieldsModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setWindowFieldsModel(obj);
			}
		} else if (op.getOperator() instanceof NeuralNetworkOperator) {
//			paramName = OperatorParameter.NAME_hidden_layers;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_hidden_layers.equals(param.getName())) {
				HiddenLayersModel obj = (HiddenLayersModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setHiddenLayersModel(obj);
			}
		} else if (op.getOperator() instanceof AdaboostOperator) {
//			paramName = OperatorParameter.NAME_adaboostUIModel;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_adaboostUIModel.equals(param.getName())) {
				AdaboostPersistenceModel obj = (AdaboostPersistenceModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				AdaboostModelUI model = new AdaboostModelUI(obj,locale);

				pDTO.setAdaboostPersistenceModel(model);
			}
		} else if (op.getOperator() instanceof LinearRegressionOperator
				|| op.getOperator() instanceof LogisticRegressionOperator
                || op.getOperator() instanceof HadoopLearnerOperator) {
//			paramName = OperatorParameter.NAME_Interaction_Columns;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_Interaction_Columns.equals(param.getName())) {
				InterActionColumnsModel obj = (InterActionColumnsModel) param
						.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setInterActionModel(obj);
			}
		}else if (op.getOperator() instanceof WOEOperator) {
			
//			paramName = OperatorParameter.NAME_WOEGROUP;
//			param = getParamByName(op, paramName);

			if (param != null && OperatorParameter.NAME_WOEGROUP.equals(param.getName())) {
				WOETable woeTable = (WOETable) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setWoeModel(new WOEModelUI(woeTable,op, user, resourceType));
			}
		} 
		else if (op.getOperator() instanceof ReplaceNullOperator) {
//			paramName = OperatorParameter.NAME_replacement_config;
//			param = getParamByName(op, paramName);
			if (param != null && OperatorParameter.NAME_replacement_config.equals(param.getName())) {
				NullReplacementModel obj = (NullReplacementModel) param
						.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setType(PropertyType.PT_CUSTOM_REPLACEMENT);
				pDTO.setNullReplacementModel(obj);
				if (obj != null && obj.getNullReplacements() != null) {
					String[] selected = new String[obj.getNullReplacements().size()];
					int idx = 0;
					for (NullReplacementItem item : obj.getNullReplacements()) {
						selected[idx++] = item.getColumnName();
					}
					pDTO.setSelected(selected);
				}
			}			
		} else if (op.getOperator() instanceof PivotOperator || op.getOperator() instanceof HadoopPivotOperator) {
//			paramName = OperatorParameter.NAME_groupByColumn;
//			param = getParamByName(op, paramName);
			if (param != null && OperatorParameter.NAME_groupByColumn.equals(param.getName())) {
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setType(PropertyType.PT_SINGLE_SELECT);
			}
								
		} else if (op.getOperator() instanceof HistogramOperator) {
//			paramName = OperatorParameter.NAME_Columns_Bins;
//			param = getParamByName(op, paramName);
			if (param != null && OperatorParameter.NAME_Columns_Bins.equals(param.getName())) {
				ColumnBinsModel obj = (ColumnBinsModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setColumnBinsModel(obj);
				if (obj != null && obj.getColumnBins() != null) {
					String[] selected = new String[obj.getColumnBins().size()];
					int idx = 0;
					for (ColumnBin item : obj.getColumnBins()) {
						selected[idx++] = item.getColumnName();
					}
					pDTO.setSelected(selected);
				}
			}

		}else if(op.getOperator() instanceof HadoopHistogramOperator){
            if (param != null && OperatorParameter.NAME_Columns_Bins.equals(param.getName())) {
                ColumnBinsModel obj = (ColumnBinsModel) param.getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                pDTO.setColumnBinsModel(obj);
                if (obj != null && obj.getColumnBins() != null) {
                    String[] selected = new String[obj.getColumnBins().size()];
                    int idx = 0;
                    for (ColumnBin item : obj.getColumnBins()) {
                        selected[idx++] = item.getColumnName();
                    }
                    pDTO.setSelected(selected);
                }
            }

        }else if (op.getOperator() instanceof AssociationOperator) {
//			paramName = OperatorParameter.NAME_expression;
//			param = getParamByName(op, paramName);
			if (param != null && OperatorParameter.NAME_expression.equals(param.getName())) {
				ExpressionModel obj = (ExpressionModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setExpressionModel(obj);
				
				String exp = "";
				String value = "";
				if (obj != null) {
					exp = obj.getExpression();
					value = obj.getPositiveValue();
				}
				pDTO.setValue(exp);
				
				String paramName = OperatorParameter.NAME_positiveValue;
				pDTO = new PropertyDTO(paramName, null,locale);
				pDTO.setValue(value);
				this.propertyList.add(pDTO);
			}
		}else if(op.getOperator() instanceof TableSetOperator){
//			paramName = OperatorParameter.NAME_tableSetConfig;
//			param = getParamByName(op, paramName);
			if(param != null && OperatorParameter.NAME_tableSetConfig.equals(param.getName())){
				TableSetModel tableset = (TableSetModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				try {
					HashMap<Object,String> inputUUIDMap = getInputUUIDMap(op.getOperator());
					pDTO.setTableSetModel(new DataSetWebModel(tableset, op.getOperator().getOperatorInputList(),inputUUIDMap));
				} catch (DatasetTransformationException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
				}
			}
		}else if(op.getOperator() instanceof SubFlowOperator){
			//paramName = OperatorParameter.NAME_subflowPath;
			//param = getParamByName(op, paramName);
			//String subflowName = (String) param.getValue();
		
		     //1 table mapping		 
//			paramName = OperatorParameter.NAME_tableMapping;
//			param = getParamByName(op, paramName);
			if(param != null && OperatorParameter.NAME_tableMapping.equals(param.getName())){
				TableMappingModel mappingModel = (TableMappingModel) param.getValue();
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);
				pDTO.setSubflowTableMappingModel(mappingModel);
			}
			 //2 variable 	
			//paramName = OperatorParameter.NAME_subflowVariable;
//			paramName = OperatorParameter.NAME_subflowVariable;
//			param = getParamByName(op, paramName);
			VariableModel variableModel = ((SubFlowOperator)op.getOperator()).getVariableModel();
			if(variableModel!=null){
				pDTO = getOrCreatePropertyDTO(param, user, resourceType);				
				pDTO.setFlowVariable(new FlowVariable(variableModel,null));
			}
			
		}else if(op.getOperator() instanceof UnivariateExplorerOperator){
			UnivariateModel univariateModel = (UnivariateModel) param.getValue();
			pDTO = getOrCreatePropertyDTO(param, user, resourceType);
			pDTO.setUnivariateModel(univariateModel);
		}else if(op.getOperator() instanceof HadoopFileOperator){ //hadoopFileOperator
            if(param != null && OperatorParameter.NAME_HD_fileStructure.equals(param.getName())){

                List<OperatorParameter> params = op.getOperator().getOperatorParameterList();
                OperatorParameter fileFormatParam = null;
                for (OperatorParameter p : params) {
                    if(OperatorParameter.NAME_HD_format.equals(p.getName())==true){
                        fileFormatParam = p;
                        break;
                    }
                }
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                if("Text File".equals(fileFormatParam.getValue())==true){
                    CSVFileStructureModel hdFileStructureModel = (CSVFileStructureModel) param.getValue();
                    if(null==hdFileStructureModel){
                        hdFileStructureModel = new CSVFileStructureModel();
                    }
                    pDTO.setCsvFileStructureModel(hdFileStructureModel);
                }else if("XML".equals(fileFormatParam.getValue())==true){
                    XMLFileStructureModel xmlFileStructureModel = (XMLFileStructureModel) param.getValue();
                    if(null==xmlFileStructureModel){
                        xmlFileStructureModel = new XMLFileStructureModel();
                    }
                    pDTO.setXmlFileStructureModel(xmlFileStructureModel);

                }else if("JSON".equals(fileFormatParam.getValue())==true){
                    JSONFileStructureModel jsonFileStructureModel = (JSONFileStructureModel)param.getValue();
                    if(null==jsonFileStructureModel){
                        jsonFileStructureModel = new JSONFileStructureModel();
                    }
                   pDTO.setJsonFileStructureModel(jsonFileStructureModel);
                }else if("Log File".equals(fileFormatParam.getValue())==true){
                    AlpineLogFileStructureModel alpineLogFileStructureModel = (AlpineLogFileStructureModel) param.getValue();
                    if(null==alpineLogFileStructureModel){
                        alpineLogFileStructureModel = new AlpineLogFileStructureModel();
                    }
                    pDTO.setAlpineLogFileStructureModel(alpineLogFileStructureModel);
                }
            }
        }else if(op.getOperator() instanceof HadoopPigExecuteOperator){ //pigExecuteOperator
            if(param != null && OperatorParameter.NAME_HD_PigExecute_fileStructure.equals(param.getName())){
            	FileStructureModel hdFileStructureModel = (FileStructureModel) param.getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                //TODO
//                if(null==hdFileStructureModel){
//                    hdFileStructureModel = new HadoopFileStructureModel();
//                }
                pDTO.setCsvFileStructureModel((CSVFileStructureModel) hdFileStructureModel);
            }else if(param != null && OperatorParameter.NAME_HD_PigScript.equals(param.getName())){
            	PigExecutableModel scriptModel = (PigExecutableModel) param.getValue();
            	pDTO = getOrCreatePropertyDTO(param, user, resourceType);
            	pDTO.setHadoopPigExecuteScriptModel(new PigExecuteOperatorUIModel(scriptModel, op.getOperator()));
            }

        }else if(op.getOperator() instanceof HadoopAggregateOperator){ //hadoop Aggregate operator
            if(param != null && OperatorParameter.NAME_aggregateFieldList.equals(param.getName())){
                AggregateFieldsModel aggregateFieldsModel = (AggregateFieldsModel) param.getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                if(null==aggregateFieldsModel){
                    aggregateFieldsModel = new AggregateFieldsModel();
                }
                pDTO.setAggregateFieldsModel(aggregateFieldsModel);
            }

        }else if(op.getOperator() instanceof HadoopVariableOperator){  //hadoop variable operator
            if(param != null && OperatorParameter.NAME_fieldList.equals(param.getName())){
                DerivedFieldsModel derivedFieldsModel = (DerivedFieldsModel) param.getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
               /* if(null==derivedFieldsModel){
                    derivedFieldsModel = new DerivedFieldsModel();
                }*/
               pDTO.setDerivedFieldsModel(derivedFieldsModel);
            }
        }else if(op.getOperator() instanceof RandomSamplingOperator || op.getOperator() instanceof HadoopRandomSamplingOperator){
            if(param != null && OperatorParameter.NAME_sampleSize.equals(param.getName())){
                SampleSizeModel sampleSizeModel = (SampleSizeModel) param.getValue();
                SampleSizeModelUI sizeModelUI = null;
                if(sampleSizeModel==null){
                    sizeModelUI = new SampleSizeModelUI();
                }else {
                    sizeModelUI = new SampleSizeModelUI(sampleSizeModel); //init for sampleSizeModel
                }
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                pDTO.setSampleSizeModelUI(sizeModelUI);
            }
        }else if(op.getOperator() instanceof StratifiedSamplingOperator){
            if(param != null && OperatorParameter.NAME_sampleSize.equals(param.getName())){
                SampleSizeModel sampleSizeModel = (SampleSizeModel) param.getValue();
                SampleSizeModelUI sizeModelUI = null;
                if(sampleSizeModel==null){
                    sizeModelUI = new SampleSizeModelUI();
                }else {
                    sizeModelUI = new SampleSizeModelUI(sampleSizeModel); //init for sampleSizeModel
                }
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                pDTO.setSampleSizeModelUI(sizeModelUI);
            }

        }else if(op.getOperator() instanceof HadoopJoinOperator){
            if(param != null && OperatorParameter.NAME_HD_JOIN_MODEL.equals(param.getName())){
                HadoopJoinModel hadoopJoinModel = (HadoopJoinModel) param.getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                if(null==hadoopJoinModel){
                    hadoopJoinModel = new HadoopJoinModel(new ArrayList<HadoopJoinFile>(),new ArrayList<HadoopJoinColumn>(),new ArrayList<HadoopJoinCondition>(),"");
                }
                pDTO.setHadoopJoinModel(hadoopJoinModel);
            }

        }else if(op.getOperator() instanceof HadoopUnionOperator){
            if(param != null && OperatorParameter.NAME_HD_Union_Model.equals(param.getName())){
                HadoopUnionModel hadoopUnionModel = (HadoopUnionModel) param.getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                if(null==hadoopUnionModel){
                    hadoopUnionModel = new HadoopUnionModel();
                }
                pDTO.setHadoopUnionModel(hadoopUnionModel);
            }

        }else if (op.getOperator() instanceof HadoopReplaceNullOperator) {
//			paramName = OperatorParameter.NAME_replacement_config;
//			param = getParamByName(op, paramName);
            if (param != null && OperatorParameter.NAME_replacement_config.equals(param.getName())) {
                NullReplacementModel obj = (NullReplacementModel) param
                        .getValue();
                pDTO = getOrCreatePropertyDTO(param, user, resourceType);
                pDTO.setType(PropertyType.PT_CUSTOM_REPLACEMENT);
                pDTO.setNullReplacementModel(obj);
                if (obj != null && obj.getNullReplacements() != null) {
                    String[] selected = new String[obj.getNullReplacements().size()];
                    int idx = 0;
                    for (NullReplacementItem item : obj.getNullReplacements()) {
                        selected[idx++] = item.getColumnName();
                    }
                    pDTO.setSelected(selected);
                }
            }
        }

        String[] tags = StorageParameterModel.getPossibleTags();
		for (int i = 0; i < tags.length; i++) {
			if(tags[i].equals(param.getName())){
				buildStorageParams(user, op, param, resourceType);
			}
		}
		
	}
	
	private HashMap<Object, String> getInputUUIDMap(Operator operator) {
		HashMap<Object, String> result = new HashMap<Object, String>();
		List<UIConnectionModel> connList =operator.getOperModel().getSourceConnection();
		 
		for (UIConnectionModel connModel : connList) {
			UIOperatorModel parentModel = connModel.getSource();
			List<Object> parentOutput = parentModel.getOperator().getOperatorOutputList();
			if(parentOutput!=null){
				for (Iterator iterator = parentOutput.iterator(); iterator
						.hasNext();) {
					Object object = (Object) iterator.next();
					if(object instanceof  OperatorInputTableInfo){ 
						result.put(object, parentModel.getUUID()) ;
					}
				}
			}
		 
		}
		 
		return result;
	}

	private void buildStorageParams(String user, UIOperatorModel op, OperatorParameter param, ResourceType resourceType){
		if(param != null){
			StorageParameterModel outputParam = (StorageParameterModel) param.getValue();
			PropertyDTO pDTO = getOrCreatePropertyDTO(param, user, resourceType);
			pDTO.setOutputCreationParamModel(new OutputCreationParameterWebModel(outputParam, op.getOperator().getOperatorInputList()));
		}
	}

	public void updateCustomProperty(UIOperatorModel op, String user, ResourceType resourceType) {
		PropertyDTO pDTO = null;
		OperatorParameter param = null;
		String paramName = null;

		// here handle the special things
		if (op.getOperator() instanceof TableJoinOperator) {
			paramName = OperatorParameter.NAME_Set_Table_Join_Parameters;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				TableJoinModel joinModel = pDTO.getTableJoinModel();	
				//to fix MINERWEB-1210 
				 List<OperatorInputTableInfo> inputTableInfo = op.getOperator().getParentDBTableSet();
				 for(JoinTable table : joinModel.getJoinTables()){
					 for(OperatorInputTableInfo inputTable : inputTableInfo){
						 if(inputTable.getOperatorUUID().equals(table.getOperatorModelID())){
							 table.setSchema(inputTable.getSchema());// use parent operator's schema instead current operator's.
							 break;
						 }
					 }
				 }
				param.setValue(joinModel);
			}
		} else if (op.getOperator() instanceof VariableOperator) {
			paramName = OperatorParameter.NAME_quantileFieldList;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				QuantileFieldsModelUI objUI = pDTO.getQuantileFieldsModel();
				QuantileFieldsModel obj = objUI.getValue();
				param.setValue(obj);
			}

			paramName = OperatorParameter.NAME_fieldList;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				DerivedFieldsModel obj = pDTO.getDerivedFieldsModel();
                if(null==obj){
                    obj = new DerivedFieldsModel();
                }
				param.setValue(obj);
			}
		} else if (op.getOperator() instanceof AggregateOperator) {
			paramName = OperatorParameter.NAME_aggregateFieldList;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				AggregateFieldsModel obj = pDTO.getAggregateFieldsModel();
				if (obj==null){
					obj= new AggregateFieldsModel();
				}
				// also get group by list.
				paramName = OperatorParameter.NAME_groupByColumn;
				pDTO = findPropertyDTO(paramName);
				if (pDTO != null && pDTO.getAggregateFieldsModel()!=null) {
					List<String> groupby = pDTO.getAggregateFieldsModel().getGroupByFieldList();
					obj.setGroupByFieldList(groupby);
				}

				param.setValue(obj);
			}

			paramName = OperatorParameter.NAME_windowFieldList;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				WindowFieldsModel obj = pDTO.getWindowFieldsModel();
				param.setValue(obj);
			}
		} else if(op.getOperator() instanceof PivotOperator){
			OperatorParameter useArray = getParamByName(op, OperatorParameter.NAME_Use_Array);
			if(Resources.TrueOpt.equals(useArray.getValue())){
				if (op.getOperator().getOutputClassList() == null
						|| !op.getOperator().getOutputClassList().contains(OperatorInputTableInfo.class.getName())) {
					op.getOperator().addOutputClass(OperatorInputTableInfo.class.getName());
				}
			}else{
				List<String> outputClassList = op.getOperator().getOutputClassList();
				if (outputClassList != null && outputClassList.contains(OperatorInputTableInfo.class.getName())) {
					outputClassList.remove(OperatorInputTableInfo.class
							.getName());
				}
			}
		} else if (op.getOperator() instanceof AdaboostOperator) {
			paramName = OperatorParameter.NAME_adaboostUIModel;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null&&pDTO.getAdaboostPersistenceModel()!=null) {
				AdaboostModelUI model = pDTO.getAdaboostPersistenceModel();
				AdaboostPersistenceModel obj = model.getValue();
				param.setValue(obj);
			}
			
		} else if (op.getOperator() instanceof NeuralNetworkOperator) {
			paramName = OperatorParameter.NAME_hidden_layers;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				HiddenLayersModel obj = pDTO.getHiddenLayersModel();
				param.setValue(obj);
			}
		} else if (op.getOperator() instanceof LinearRegressionOperator
				|| op.getOperator() instanceof LogisticRegressionOperator
                || op.getOperator() instanceof HadoopLearnerOperator) {
			paramName = OperatorParameter.NAME_Interaction_Columns;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				InterActionColumnsModel obj = pDTO.getInterActionModel();
				param.setValue(obj);
			}
		}
		else if (op.getOperator() instanceof WOEOperator) {
			paramName = OperatorParameter.NAME_WOEGROUP;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				WOETable obj = pDTO.getWoeModel().getWoeTable();
				param.setValue(obj);
			}
		} 
		else if (op.getOperator() instanceof ReplaceNullOperator) {
			paramName = OperatorParameter.NAME_replacement_config;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				NullReplacementModel obj = pDTO.getNullReplacementModel();
				param.setValue(obj);
			}		
		} else if (op.getOperator() instanceof HistogramOperator) {
			paramName = OperatorParameter.NAME_Columns_Bins;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				ColumnBinsModel obj = pDTO.getColumnBinsModel();
				param.setValue(obj);
			}
		}else if(op.getOperator() instanceof HadoopHistogramOperator){
            paramName = OperatorParameter.NAME_Columns_Bins;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);

            if (param != null && pDTO != null) {
                ColumnBinsModel obj = pDTO.getColumnBinsModel();
                param.setValue(obj);
            }

        }else if (op.getOperator() instanceof AssociationOperator) {
			paramName = OperatorParameter.NAME_expression;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				ExpressionModel obj = pDTO.getExpressionModel();
				if(obj==null){
					obj= new ExpressionModel();
				}
				obj.setExpression(pDTO.getValue());
				
				paramName = OperatorParameter.NAME_positiveValue;
				pDTO = findPropertyDTO(paramName);
				obj.setPositiveValue(pDTO.getValue());
				
				param.setValue(obj);
			}
		}else if(op.getOperator() instanceof TableSetOperator){
			paramName = OperatorParameter.NAME_tableSetConfig;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				TableSetModel obj = pDTO.getTableSetModel().reverse();
				if(obj==null){
					obj= new TableSetModel();
				}
				param.setValue(obj);
			}
		}else if(op.getOperator() instanceof SubFlowOperator){
			
			paramName = OperatorParameter.NAME_tableMapping;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);

			if (param != null && pDTO != null) {
				TableMappingModel obj = pDTO.getSubflowTableMappingModel();
				handleTableMapping4Hadoop(obj);
				param.setValue(obj);
			}
			
			paramName = OperatorParameter.NAME_subflowVariable;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);
		
			if (param != null && pDTO != null) {
				if(pDTO.getFlowVariable()!=null){
					VariableModel obj = pDTO.getFlowVariable().getModel();
					param.setValue(obj);
					((SubFlowOperator)op.getOperator()).setVariableModel(obj) ;
				}
			}
		}else if(op.getOperator() instanceof SVDLanczosOperator){
			List<Operator> operatorList = op.getOperator().getChildOperators();
			for(Operator operator : operatorList){
				if(operator instanceof SVDLanczosCalculatorOperator){
					SvdOperatorUtility.syncSVDParams2SVDCalculator(op.getOperator(), operator);
				}
			}
		}else if(op.getOperator() instanceof UnivariateExplorerOperator){
			paramName = OperatorParameter.NAME_UnivariateModel;
			param = getParamByName(op, paramName);
			pDTO = findPropertyDTO(paramName);
			if (param != null && pDTO != null) {
				UnivariateModel univariateModel = pDTO.getUnivariateModel();
				if(univariateModel==null){
					univariateModel= new UnivariateModel();
				}
				param.setValue(univariateModel);
			}
		}else if(op.getOperator() instanceof HadoopFileOperator){ //hadoopFileOperator
            paramName = OperatorParameter.NAME_HD_fileStructure;
            PropertyDTO fileFormatDTO = findPropertyDTO(OperatorParameter.NAME_HD_format);
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){

                if("Text File".equals(fileFormatDTO.getValue())==true){
                    CSVFileStructureModel hdFileStructureModel = pDTO.getCsvFileStructureModel();
                    param.setValue(hdFileStructureModel);
                }else if("XML".equals(fileFormatDTO.getValue())==true){
                    XMLFileStructureModel xmlFileStructureModel = pDTO.getXmlFileStructureModel();
                    param.setValue(xmlFileStructureModel);
                }else if("JSON".equals(fileFormatDTO.getValue())==true){
                    JSONFileStructureModel jsonFileStructureModel = pDTO.getJsonFileStructureModel();
                    param.setValue(jsonFileStructureModel);
                }else if("Log File".equals(fileFormatDTO.getValue())==true){
                    AlpineLogFileStructureModel alpineLogFileStructureModel = pDTO.getAlpineLogFileStructureModel();
                    param.setValue(alpineLogFileStructureModel);
                }
            }
        }else if(op.getOperator() instanceof HadoopPigExecuteOperator){ //hadoopPigexecute
            paramName = OperatorParameter.NAME_HD_PigExecute_fileStructure;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
            	FileStructureModel hdFileStructureModel = pDTO.getCsvFileStructureModel();
                /*if(null==hdFileStructureModel){
                    hdFileStructureModel = new HadoopFileStructureModel();
                }*/
                param.setValue(hdFileStructureModel);
            }
            paramName = OperatorParameter.NAME_HD_PigScript;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
            	PigExecuteOperatorUIModel scriptModel = pDTO.getHadoopPigExecuteScriptModel();
            	param.setValue(scriptModel.revertModel());
            }
        }else if(op.getOperator() instanceof HadoopAggregateOperator){
            paramName = OperatorParameter.NAME_aggregateFieldList;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
                AggregateFieldsModel aggregateFieldsModel = pDTO.getAggregateFieldsModel();
                if(null==aggregateFieldsModel){
                    aggregateFieldsModel = new AggregateFieldsModel();
                }
                param.setValue(aggregateFieldsModel);
            }
        }else if(op.getOperator() instanceof HadoopVariableOperator){
            paramName = OperatorParameter.NAME_fieldList;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
                DerivedFieldsModel derivedFieldsModel = pDTO.getDerivedFieldsModel();
                if(null==derivedFieldsModel){
                    derivedFieldsModel = new DerivedFieldsModel();
                }
                param.setValue(derivedFieldsModel);
            }
        }else if(op.getOperator() instanceof RandomSamplingOperator || op.getOperator() instanceof HadoopRandomSamplingOperator){
            paramName = OperatorParameter.NAME_sampleSize;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
                SampleSizeModelUI sampleSizeModelUI = pDTO.getSampleSizeModelUI();
                if(null==sampleSizeModelUI){
                    sampleSizeModelUI = new SampleSizeModelUI();
                }
                //System.out.println(sampleSizeModelUI.getSampleSizeList());
                param.setValue(sampleSizeModelUI.getRealModel());
            }

        }else if(op.getOperator() instanceof StratifiedSamplingOperator){
            paramName = OperatorParameter.NAME_sampleSize;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
                SampleSizeModelUI sampleSizeModelUI = pDTO.getSampleSizeModelUI();
                if(null==sampleSizeModelUI){
                    sampleSizeModelUI = new SampleSizeModelUI();
                }
                //System.out.println(sampleSizeModelUI.getSampleSizeList());
                param.setValue(sampleSizeModelUI.getRealModel());
            }
        }else if(op.getOperator() instanceof HadoopJoinOperator){
            paramName = OperatorParameter.NAME_HD_JOIN_MODEL;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
                HadoopJoinModel hadoopJoinModel = pDTO.getHadoopJoinModel();
                if(null==hadoopJoinModel){
                    hadoopJoinModel = new HadoopJoinModel(new ArrayList<HadoopJoinFile>(),new ArrayList<HadoopJoinColumn>(),new ArrayList<HadoopJoinCondition>(),"");
                }
                param.setValue(hadoopJoinModel);
            }
        }else if(op.getOperator() instanceof HadoopUnionOperator){
            paramName = OperatorParameter.NAME_HD_Union_Model;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);
            if(param!=null && pDTO != null){
                HadoopUnionModel hadoopUnionModel = pDTO.getHadoopUnionModel();
                if(null==hadoopUnionModel){
                    hadoopUnionModel = new HadoopUnionModel();
                }
                param.setValue(hadoopUnionModel);
            }
        }else if (op.getOperator() instanceof HadoopReplaceNullOperator) {
            paramName = OperatorParameter.NAME_replacement_config;
            param = getParamByName(op, paramName);
            pDTO = findPropertyDTO(paramName);

            if (param != null && pDTO != null) {
                NullReplacementModel obj = pDTO.getNullReplacementModel();
                param.setValue(obj);
            }
        }
		
		fillOutputCreationParamModel(OperatorParameter.NAME_outputTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_UmatrixTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_VmatrixTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_singularValueTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_PCAQoutputTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_PCAQvalueOutputTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_PLDADocTopicOutputTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_PLDAModelOutputTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_topicOutTable_StorageParams, op);
		fillOutputCreationParamModel(OperatorParameter.NAME_docTopicOutTable_StorageParams, op);
	}
	
	private void handleTableMapping4Hadoop(TableMappingModel obj) {
		if(obj!=null){
			List<TableMappingItem> items = obj.getMappingItems() ;
			for(TableMappingItem item:items){
				if(StringUtil.isEmpty( item.getInputTable() )){
					item.setInputTable(item.getInputSchema())  ;
					item.setInputSchema(null) ;
				}
				if(StringUtil.isEmpty( item.getSubFlowTable() )){
					item.setSubFlowTable(item.getSubFlowSchema()) ;
					item.setSubFlowSchema(null) ;
				}
			}
		}
		
	}
	
	private void fillOutputCreationParamModel(String paramName, UIOperatorModel op ){
		OperatorParameter  param = getParamByName(op, paramName);
		PropertyDTO  pDTO = findPropertyDTO(paramName);
		if (param != null && pDTO != null) {
			StorageParameterModel obj = pDTO.getOutputCreationParamModel().getOriginalModel();
			if(obj==null){
				obj= new StorageParameterModel();
			}
			param.setValue(obj);
		}
	}

	public static OperatorParameter getParamByName(UIOperatorModel op,
			String name) {
		List<OperatorParameter> paramList = op.getOperator()
				.getOperatorParameterList();
		for (OperatorParameter param : paramList) {
			if (param.getName().equals(name)) {
				return param;
			}
		}
		return null;
	}

	/**
	 * @param
	 * @param
	 * @return
	 */
	private PropertyDTO getOrCreatePropertyDTO( OperatorParameter parameter, String userName, ResourceType dbType) {

		for (PropertyDTO propertyDTO : this.propertyList) {
			if (propertyDTO.getName().equals(parameter.getName())) {
				return propertyDTO;
			}
		}
		PropertyDTO pDTO = new PropertyDTO( "",   parameter,   userName,   dbType,locale);
        this.propertyList.add(pDTO);
        return pDTO;
	}

	private PropertyDTO findPropertyDTO(String name) {

		for (PropertyDTO obj : this.propertyList) {
			if (obj.getName().equals(name)) {
				return obj;
			}
		}

		return null;
	}
	private FlowInfo flowInfo;
	private String uuid;
	private String classname;
	private List<PropertyDTO> propertyList;
	private boolean isValid;
	private String[] invalidPropertyList;


	/**
	 * @return the propertyList
	 */
	public List<PropertyDTO> getPropertyList() {
		return propertyList;
	}

	/**
	 * @param propertyList
	 *            the propertyList to set
	 */
	public void setPropertyList(List<PropertyDTO> propertyList) {
		this.propertyList = propertyList;
	}

	/**
	 * @return the flowInfo
	 */
	public FlowInfo getFlowInfo() {
		return flowInfo;
	}

	/**
	 * @param flowInfo
	 *            the flowInfo to set
	 */
	public void setFlowInfo(FlowInfo flowInfo) {
		this.flowInfo = flowInfo;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @param invalidPropertyList the invalidPropertyList to set
	 */
	public void setInvalidPropertyList(String[] invalidPropertyList) {
		this.invalidPropertyList = invalidPropertyList;
	}

	/**
	 * @return the invalidPropertyList
	 */
	public String[] getInvalidPropertyList() {
		return invalidPropertyList;
	}

	public String getClassname() {
		return classname;
	}
}
