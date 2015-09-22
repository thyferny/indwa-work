/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * DatabaseOperatorInfo.java
 */
package com.alpine.miner.impls.web.resource.operator;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.utils.ModelUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.PredictOperator;
import com.alpine.miner.workflow.operator.association.AssociationOperator;
import com.alpine.miner.workflow.operator.clustering.KMeansOperator;
import com.alpine.miner.workflow.operator.customize.CustomizedOperator;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.datasource.TableJoinOperator;
import com.alpine.miner.workflow.operator.field.AggregateOperator;
import com.alpine.miner.workflow.operator.field.ColumnFilterOperator;
import com.alpine.miner.workflow.operator.field.FilterOperator;
import com.alpine.miner.workflow.operator.field.IntegerToTextOperator;
import com.alpine.miner.workflow.operator.field.NormalizationOperator;
import com.alpine.miner.workflow.operator.field.PivotOperator;
import com.alpine.miner.workflow.operator.field.ReplaceNullOperator;
import com.alpine.miner.workflow.operator.field.TableSetOperator;
import com.alpine.miner.workflow.operator.field.VariableOperator;
import com.alpine.miner.workflow.operator.hadoop.CopyToDBOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.sampling.SampleSelectorOperator;
import com.alpine.miner.workflow.operator.solutions.ProductRecommendationOperator;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosCalculatorOperator;
import com.alpine.miner.workflow.operator.timeseries.TimeSeriesPredictOperator;

/**
 * @author Gary
 * Jul 9, 2012
 */
public class DatabaseOperatorPrimaryInfo extends OperatorPrimaryInfo {

	private String outputSchema;
	private String outputTable;
	private String outputType;
	private boolean hasDbTableInfo =false;
	private List<IntermediateTableInfo> interTableList = new ArrayList<IntermediateTableInfo>();
	private String modelType;
	
	//add this for data expolorer use
	public void fillOPUIDBInfo(Operator op){
		if(op instanceof SubFlowOperator){
			OperatorWorkFlow workFlow = ((SubFlowOperator)op).getSubWorkflow();
			if(workFlow != null){
				for(UIOperatorModel operatorModel : workFlow.getChildList()){
					fillOPUIDBInfo(operatorModel.getOperator());
				}
				Object outputTableInfo = ((SubFlowOperator)op).getExitTableInfo();
				if(outputTableInfo != null &&outputTableInfo instanceof OperatorInputTableInfo){
					this.outputSchema =((OperatorInputTableInfo) outputTableInfo).getSchema();
					this.outputTable =((OperatorInputTableInfo) outputTableInfo).getTable();
					this.outputType = ((OperatorInputTableInfo)outputTableInfo).getTableType();
				}
			}
		}else{
			List<String[]> operatorColumns = OperatorUtility.getOperatorOutputTables(op, op.getWorkflow().getParentVariableModel());
			
			if(!(op instanceof DbTableOperator)){
				if("IntegerToTextOperator".equals(op.getClass().getSimpleName())){
					OperatorParameter mPara = ParameterUtility.getParameterByName(op, OperatorParameter.NAME_modifyOriginTable);
					List<Operator> parents = op.getParentOperators();
					//modify original table...
					if(mPara != null && "true".equals(mPara.getValue()) && parents != null && parents.size() > 0 && parents.get(0) != null) {
						fillOPUIDBInfo(parents.get(0));
						return;
					}
				}
				//0 schema ,1 table ,2 output_type
				for(int i = 0; i < operatorColumns.size(); i++){
					String[] operatorColumn = operatorColumns.get(i);
					this.interTableList.add(new IntermediateTableInfo(operatorColumn[0], operatorColumn[1], operatorColumn[2]));
				}
			}

			if("ModelOperator".equals(op.getClass().getSimpleName())){
				ModelOperator operator = (ModelOperator) op;
				this.setModelType(ModelUtility.getAlorithmModel(operator.getModel()));
			}
			
			if(this.getConnectionName() == null){
				this.setConnectionName(OperatorUtility.getDBConnectionName(op.getOperModel()));
			} 
	 
			if(operatorColumns.size() > 0){// for normal operator to get output table info. TODO Maybe the first element is not output table. 
				String[] outputTableInfo = operatorColumns.get(0);
				this.outputSchema = outputTableInfo[0];
				this.outputTable = outputTableInfo[1];
				this.outputType = outputTableInfo[2];
			}
		}
	}
	
	public boolean hasDBTableInfo(UIOperatorModel om) {
		if (om.getOperator() instanceof DbTableOperator ) {
			return true;
		}
		
		return hasTableGenerated(om);			 
	}

	private boolean hasTableGenerated(UIOperatorModel om) {
		if (om.getOperator() instanceof PredictOperator
//				||om.getOperator() instanceof SVDLanczosOperator
				||om.getOperator() instanceof SVDLanczosCalculatorOperator
				||om.getOperator() instanceof CustomizedOperator
				||om.getOperator() instanceof TimeSeriesPredictOperator
				||om.getOperator() instanceof SampleSelectorOperator
				||om.getOperator() instanceof NormalizationOperator
				||om.getOperator() instanceof CopyToDBOperator
				||om.getOperator() instanceof TableJoinOperator
				||om.getOperator() instanceof AggregateOperator
				||om.getOperator() instanceof ColumnFilterOperator
				||om.getOperator() instanceof FilterOperator
				||om.getOperator() instanceof IntegerToTextOperator
				||om.getOperator() instanceof ReplaceNullOperator
				||om.getOperator() instanceof NormalizationOperator
				||om.getOperator() instanceof PivotOperator
				||om.getOperator() instanceof VariableOperator
				||om.getOperator() instanceof CustomizedOperator
				||om.getOperator() instanceof KMeansOperator
				||om.getOperator() instanceof AssociationOperator
				||om.getOperator() instanceof TableSetOperator
//				||om.getOperator() instanceof PLDATrainerOperator
//				||om.getOperator() instanceof PCAOperator
				||om.getOperator() instanceof ProductRecommendationOperator
				||(om.getOperator() instanceof SubFlowOperator && ((SubFlowOperator)om.getOperator()).getExitTableInfo() != null)){
			if(om.getOperator() instanceof IntegerToTextOperator){
				List<OperatorParameter> properties = om.getOperator().getOperatorParameterList();
				for(OperatorParameter property : properties){
					if(OperatorParameter.NAME_modifyOriginTable.equals(property.getName()) && Boolean.TRUE.equals(Boolean.valueOf((String) property.getValue()))){
						return false;
					}
				}
			}
			if(om.getOperator() instanceof PivotOperator){
				List<OperatorParameter> properties = om.getOperator().getOperatorParameterList();
				for(OperatorParameter property : properties){
					if(OperatorParameter.NAME_Use_Array.equals(property.getName()) && Boolean.TRUE.equals(Boolean.valueOf((String) property.getValue()))){
						return false;
					}
				}
			}
			return true;
		}else{
			return false;
		}						
		 
	}
	
	public String getOutputSchema() {
		return outputSchema;
	}

	public String getOutputTable() {
		return outputTable;
	}

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public void setHasDbTableInfo(boolean hasDbTableInfo) {
		this.hasDbTableInfo = hasDbTableInfo;
	}

	public void setInterTableList(List<IntermediateTableInfo> interTableList) {
		this.interTableList = interTableList;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public String getOutputType() {
		return outputType;
	}

	public boolean isHasDbTableInfo() {
		return hasDbTableInfo;
	}

	public List<IntermediateTableInfo> getInterTableList() {
		return interTableList;
	}

	public String getModelType() {
		return modelType;
	}

	public static class IntermediateTableInfo{
		String 	schemaName,
				tableName,
				outputType;
		public IntermediateTableInfo(String schemaName, String tableName, String outputType){
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.outputType = outputType;
		}
	}
}
