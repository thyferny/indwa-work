/**
 * 
 */
package com.alpine.miner.impls.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.alpine.miner.impls.editworkflow.operator.OperatorManagement;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.operator.DatabaseOperatorPrimaryInfo;
import com.alpine.miner.impls.web.resource.operator.HadoopOperatorPrimaryInfo;
import com.alpine.miner.impls.web.resource.operator.OperatorPrimaryInfo;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.VariableModel;

/**
 * @author sam_zang
 * 
 */
public class FlowDTO {
	//this is real change, not cange the same value...
	boolean isPropertyChanged = true;

	public boolean isPropertyChanged() {
		return isPropertyChanged;
	}

	public void setPropertyChanged(boolean isPropertyChanged) {
		this.isPropertyChanged = isPropertyChanged;
	}

	public FlowDTO(FlowInfo info, OperatorWorkFlow flow, String user) throws OperationFailedException {
		this.setFlowInfo(info);
		this.result = new LinkedList<OperatorUI>();
		this.links = new LinkedList<LinkUI>();
		this.variableModelList = flow.getVariableModelList();
		if (flow != null) {
			initOperators(info, flow, user);
			initLinks(flow);	
		}
	}

	class OperatorUI {
		/**
		 * operatorType
		 */
		static final String DATABASE_TYPE = "DB",
							HADOOP_TYPE = "HADOOP";
		
		String uid;
		String name;
		String classname;
		String icon;
		String icons;
		int x;
		int y;
		boolean isValid;
		String connectionName = null;
		String operatorType;
		String description;
		
		//********DB**********
		String outputSchema;
		String outputTable;
		String outputType;
		boolean hasDbTableInfo =false;
		List<DatabaseOperatorPrimaryInfo.IntermediateTableInfo> interTableList = new ArrayList<DatabaseOperatorPrimaryInfo.IntermediateTableInfo>();
		public String modelType;
		
		//*********Hadoop**********
		String outputHadoopFilePath;
		boolean storeResult;
	}
	
	class IntermediateTableInfo{
		String 	schemaName,
				tableName,
				outputType;
		IntermediateTableInfo(String schemaName, String tableName, String outputType){
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.outputType = outputType;
		}
	}

	class LinkUI {
		String sourceid;
		String targetid;
		int x1;
		int y1;
		int x2;
		int y2;
	}

	// the operator type must use OperatorUI, because Gson reference each fields to deseries json. It happened when save flow.
	private List<OperatorUI> result;
	private List<LinkUI> links;
	private FlowInfo flowInfo;
	private List<VariableModel> variableModelList;
//	private String getModelType(ModelOperator operator) {
//		if(operator.getModel()!=null){
//			return  ModelUtility.getAlorithmModel(operator.getModel()); 
//		}else{
//		
//			return null;
//		}
//	}

	/**
	 * valid 
	 * @throws OperationFailedException 
	 */
	private void initOperators(FlowInfo info, OperatorWorkFlow flow, String user) throws OperationFailedException {
//		ResourceManager rmgr = ResourceManager.getInstance();
		for (UIOperatorModel op : flow.getChildList()) {
			
//			String[] icons = rmgr.getClassIcons(op.getClassName());

			OperatorPrimaryInfo opInfo = OperatorManagement.getInstance().buildOperatorPrimaryInfo(op, user);
			OperatorUI opUI = new OperatorUI();
//			
			opUI.isValid = true;			
			opUI.uid = opInfo.getUid();
			opUI.name = opInfo.getName();
			opUI.classname = opInfo.getClassname();
			opUI.description = opInfo.getDescription();

//			if (icons != null && icons.length == 3) {
//				opUI.icon = icons[1];
//				opUI.icons = icons[2];
//			}
			opUI.x = op.getPosition().getStartX();
			opUI.y = op.getPosition().getStartY();				
			
			if (info.getResourceType() == ResourceType.Personal) {
				setFields(opUI, opInfo, info);
			}
			 
			result.add(opUI);
		}
		// sort operator order as it's label, in order to make the label pool(in OperatorManagementManager.js) work well
		// e.g. Pivot and Pivot-1. Must push Pivot into label pool before push Pivot-1
		// do same thing in Copy/paste
		Collections.sort(result, new Comparator<OperatorUI>(){
			@Override
			public int compare(OperatorUI o1, OperatorUI o2) {
				return o1.name.compareTo(o2.name);
			}
		});
	}
	
	private void setFields(OperatorUI opUI, OperatorPrimaryInfo opInfo, FlowInfo flowInfo) throws OperationFailedException{
		if(opInfo instanceof HadoopOperatorPrimaryInfo){
			HadoopOperatorPrimaryInfo hop = (HadoopOperatorPrimaryInfo) opInfo;
			opUI.operatorType = OperatorUI.HADOOP_TYPE;
			opUI.outputHadoopFilePath = hop.getOutputHadoopFilePath();
			opUI.connectionName = hop.getConnectionName();
			opUI.storeResult = hop.isStoreResult();
		}else{//db operator
			DatabaseOperatorPrimaryInfo dop = (DatabaseOperatorPrimaryInfo) opInfo;
			opUI.operatorType = OperatorUI.DATABASE_TYPE;
			opUI.hasDbTableInfo = dop.isHasDbTableInfo();
			opUI.modelType = dop.getModelType();
			opUI.outputSchema = dop.getOutputSchema();
			opUI.outputTable = dop.getOutputTable();
			opUI.outputType = dop.getOutputType();
			opUI.interTableList = dop.getInterTableList();
			opUI.connectionName = dop.getConnectionName();
//			fillOPUIDBInfo(op.getOperator(), opUI, flowInfo, op.getOperator().getWorkflow().getVariableModelList().get(0));
		}
		opUI.isValid = opInfo.isValid();	
	}
	
	//add this for data expolorer use
//	private boolean fillOPUIDBInfo(Operator op, OperatorUI opUI, FlowInfo info, VariableModel variableModel) throws OperationFailedException {
//		if(op instanceof SubFlowOperator){
//			OperatorWorkFlow workFlow = ((SubFlowOperator)op).getSubWorkflow();
//			if(workFlow != null){
//				for(UIOperatorModel operatorModel : workFlow.getChildList()){
//					fillOPUIDBInfo(operatorModel.getOperator(), opUI, info, workFlow.getVariableModelList().get(0));
//				}
//				OperatorInputTableInfo outputTableInfo = ((SubFlowOperator)op).getExitTableInfo();
//				if(outputTableInfo != null){
//					opUI.outputSchema = outputTableInfo.getSchema();
//					opUI.outputTable = outputTableInfo.getTable();
//					opUI.outputType = outputTableInfo.getTableType();
//				}
//			}
//		}else{
//			List<String[]> operatorColumns= OperatorUtility.getOperatorOutputTables(op, variableModel);
//			
//			if(!(op instanceof DbTableOperator)){
//				if(op.getClass().getSimpleName().equals("IntegerToTextOperator")){
//					OperatorParameter mPara = ParameterUtility.getParameterByName(op, OperatorParameter.NAME_modifyOriginTable);
//					List<Operator> parents = op.getParentOperators();
//					//modify original table...
//					if(mPara!=null&&mPara.getValue().equals("true")&&parents!=null&&parents.get(0)!=null) {
//						return fillOPUIDBInfo(  parents.get(0), opUI, info, variableModel) ;
//					}
//				}
//				//0 schema ,1 table ,2 output_type
//				for(int i = 0; i < operatorColumns.size(); i++){
//					String[] operatorColumn = operatorColumns.get(i);
//					opUI.interTableList.add(new DatabaseOperatorPrimaryInfo.IntermediateTableInfo(operatorColumn[0], operatorColumn[1], operatorColumn[2]));
//				}
//			}
//			if(opUI.connectionName==null){
//				opUI.connectionName=OperatorUtility.getDBConnectionName(op.getOperModel());
//			} 
//	 
//			if(operatorColumns.size() > 0){// for normal operator to get output table info. TODO Maybe the first element is not output table. 
//				String[] outputTableInfo = operatorColumns.get(0);
//				opUI.outputSchema = outputTableInfo[0];
//				opUI.outputTable = outputTableInfo[1];
//				opUI.outputType = outputTableInfo[2];
//			}
//		}
//  
//		return true;
//	}
 

//	private boolean hasDBTableInfo(UIOperatorModel om) {
//		if (om.getOperator() instanceof DbTableOperator ) {
//			return true;
//		}
//		
//		return hasTableGenerated(om);			 
//	}

//	private boolean hasTableGenerated(UIOperatorModel om) {
//		if (om.getOperator() instanceof PredictOperator
////				||om.getOperator() instanceof SVDLanczosOperator
//				||om.getOperator() instanceof SVDLanczosCalculatorOperator
//				||om.getOperator() instanceof CustomizedOperator
//				||om.getOperator() instanceof TimeSeriesPredictOperator
//				||om.getOperator() instanceof SampleSelectorOperator
//				||om.getOperator() instanceof NormalizationOperator
//				
//				||om.getOperator() instanceof TableJoinOperator
//				||om.getOperator() instanceof AggregateOperator
//				||om.getOperator() instanceof ColumnFilterOperator
//				||om.getOperator() instanceof FilterOperator
//				||om.getOperator() instanceof IntegerToTextOperator
//				||om.getOperator() instanceof ReplaceNullOperator
//				||om.getOperator() instanceof NormalizationOperator
//				||om.getOperator() instanceof PivotOperator
//				||om.getOperator() instanceof VariableOperator
//				||om.getOperator() instanceof CustomizedOperator
//				||om.getOperator() instanceof KMeansOperator
//				||om.getOperator() instanceof AssociationOperator
//				||om.getOperator() instanceof TableSetOperator
////				||om.getOperator() instanceof PLDATrainerOperator
////				||om.getOperator() instanceof PCAOperator
//				||om.getOperator() instanceof ProductRecommendationOperator
//				||(om.getOperator() instanceof SubFlowOperator && ((SubFlowOperator)om.getOperator()).getExitTableInfo() != null)){
//			if(om.getOperator() instanceof IntegerToTextOperator){
//				List<OperatorParameter> properties = om.getOperator().getOperatorParameterList();
//				for(OperatorParameter property : properties){
//					if(OperatorParameter.NAME_modifyOriginTable.equals(property.getName()) && Boolean.TRUE.equals(Boolean.valueOf((String) property.getValue()))){
//						return false;
//					}
//				}
//			}
//			if(om.getOperator() instanceof PivotOperator){
//				List<OperatorParameter> properties = om.getOperator().getOperatorParameterList();
//				for(OperatorParameter property : properties){
//					if(OperatorParameter.NAME_Use_Array.equals(property.getName()) && Boolean.TRUE.equals(Boolean.valueOf((String) property.getValue()))){
//						return false;
//					}
//				}
//			}
//			return true;
//		}else{
//			return false;
//		}						
//		 
//	}
	
//	private String getOutputType(UIOperatorModel om) {
//
//		if (om.getOperator() instanceof PredictOperator) {
//			return "TABLE";
//		} else if (hasTableGenerated(om) == true) {
//
//			List<OperatorParameter> prams = om.getOperator()
//					.getOperatorParameterList();
//			for (Iterator iterator = prams.iterator(); iterator.hasNext();) {
//				OperatorParameter operatorParameter = (OperatorParameter) iterator
//						.next();
//				if (operatorParameter.getName().equals(
//						OperatorParameter.NAME_outputType)) {
//					return (String) operatorParameter.getValue();
//				}
//
//			}
//			// sample selector and time seriers predict always have table...
//			return "TABLE";
//		}
//
//		return null;
//	}

	/**
	 * 
	 */
	private void initLinks(OperatorWorkFlow flow) {
		UIOperatorModel op = null;
		List<UIOperatorConnectionModel> connList = flow.getConnModelList();
		
		for (UIOperatorConnectionModel conn : connList ) {
			String source = conn.getSource().getId();
			String target = conn.getTarget().getId();
			
			LinkUI linkUI = new LinkUI();
			
			op = findByName(flow, source);		
			linkUI.sourceid = op.getUUID();
			linkUI.x1 = op.getPosition().getStartX();
			linkUI.y1 = op.getPosition().getStartY();
			
			op = findByName(flow, target);
			linkUI.targetid = op.getUUID();
			linkUI.x2 = op.getPosition().getStartX();
			linkUI.y2 = op.getPosition().getStartY();
			
			links.add(linkUI);			
		}		
	}

	/**
	 * @param flow
	 * @param name
	 * @return
	 */
	private UIOperatorModel findByName(OperatorWorkFlow flow, String name) {
		for (UIOperatorModel op : flow.getChildList()) {
			if (name.equals(op.getId())) {
				return op;
			}
		}
		// should fail if it gets here.
		assert(false);
		return null;
	}
	
	private UIOperatorModel findByUid(OperatorWorkFlow flow, String uid){
		for (UIOperatorModel op : flow.getChildList()) {
			if (uid.equals(op.getUUID())) {
				return op;
			}
		}
		return null;
	}

	/**
	 * @param flow
	 */
	public void updateCoordinate(OperatorWorkFlow flow) {
		for (OperatorUI ui: this.result) {
			UIOperatorModel op = findByUid(flow, ui.uid);
			if (op != null) {
				op.setId(ui.name);
				op.getPosition().setStartX(ui.x);
				op.getPosition().setStartY(ui.y);
			}
		}	
	}

	public FlowInfo getFlowInfo() {
		return flowInfo;
	}

	public void setFlowInfo(FlowInfo flowInfo) {
		this.flowInfo = flowInfo;
	}

	public List<OperatorUI> getResult() {
		return result;
	}

	public void setResult(List<OperatorUI> result) {
		this.result = result;
	}

	public List<LinkUI> getLinks() {
		return links;
	}

	public void setLinks(List<LinkUI> links) {
		this.links = links;
	}
}
