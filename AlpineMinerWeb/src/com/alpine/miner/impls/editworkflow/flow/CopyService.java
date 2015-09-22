/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WorkFlowManagement.java
 */
package com.alpine.miner.impls.editworkflow.flow;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.impl.db.attribute.model.pigexe.PigInputMapItem;
import com.alpine.miner.impls.editworkflow.link.LinkManagement;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.model.impl.UIOperatorModelImpl;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.customize.CustomizedOperator;
import com.alpine.miner.workflow.operator.datasource.TableJoinOperator;
import com.alpine.miner.workflow.operator.field.TableSetOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopJoinOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopPigExecuteOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopUnionOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.OperatorParameterImpl;
import com.alpine.miner.workflow.operator.parameter.ParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinColumn;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinCondition;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinFile;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionFile;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModelItem;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionSourceColumn;
import com.alpine.miner.workflow.operator.parameter.pigexe.PigExecutableModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinTable;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.tableset.ColumnMap;
import com.alpine.miner.workflow.operator.parameter.tableset.TableSetModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.common.ListUtility;

/**
 * @author Gary
 * Aug 2, 2012
 */
public class CopyService {

	private static final CopyService INSTANCE = new CopyService();
	
	private CopyService(){
		
	}
	
	public static CopyService getInstance(){
		return INSTANCE;
	}
	
	public CopyItems copyFlowItems(OperatorWorkFlow workflow, 
								OperatorWorkFlow targetWorkFlow,
								OperatorItemInfo[] operatorSet, 
								int offset,
								FlowInfo targetWorkFlowinfo) throws Exception{
		Map<String, UIOperatorModel> operatorModelCopies = new HashMap<String, UIOperatorModel>();
		List<UIOperatorConnectionModel> connectionModelCopies = new ArrayList<UIOperatorConnectionModel>();
		for(OperatorItemInfo item : operatorSet){
			for(UIOperatorModel operatorModel : workflow.getChildList()){
				if(operatorModel.getUUID().equals(item.getUuid())){
					UIOperatorModel operatorModelCopy = cloneUIOperatorModel(operatorModel, offset,targetWorkFlowinfo);
					operatorModelCopy.setUUID(item.getNewUUID());
					operatorModelCopy.setId(item.getNewName());
					operatorModelCopies.put(operatorModel.getUUID(), operatorModelCopy);
					break;
				}
			}
		}
		
		for(UIOperatorConnectionModel connectionModel : workflow.getConnModelList()){
			if(operatorModelCopies.containsKey(connectionModel.getSource().getUUID())
					&& operatorModelCopies.containsKey(connectionModel.getTarget().getUUID())){
				UIOperatorConnectionModel connModel = fillConnectionInfo(operatorModelCopies, connectionModel);
				if(connModel != null){
					connectionModelCopies.add(connModel);
				}
			}
		}
		replaceUUID(operatorModelCopies);
		putCopyIntoWorkFlow(targetWorkFlow, operatorModelCopies.values(), connectionModelCopies);
		return new CopyItems(operatorModelCopies.values(), connectionModelCopies);
	}

	private void replaceUUID(Map<String, UIOperatorModel> operatorModelCopies) {
		for(UIOperatorModel copiedOpModel : operatorModelCopies.values()){
			if(copiedOpModel.getOperator() instanceof TableJoinOperator){
				TableJoinOperator op = (TableJoinOperator)copiedOpModel.getOperator();
				TableJoinModel joinModel = (TableJoinModel) ParameterUtility.getParameterByName(op, OperatorParameter.NAME_Set_Table_Join_Parameters).getValue();
				if(joinModel != null){
					List<JoinTable> tables = joinModel.getJoinTables();
					for(int j=0;j<tables.size();j++){
						UIOperatorModel opModel = operatorModelCopies.get(tables.get(j).getOperatorModelID());
						if(opModel != null){
							tables.get(j).setOperatorModelID(opModel.getUUID());
						}
					}
				}
			}else if(copiedOpModel.getOperator() instanceof TableSetOperator){
				TableSetOperator op = (TableSetOperator)copiedOpModel.getOperator();
				TableSetModel joinModel = (TableSetModel) ParameterUtility.getParameterByName(op, 
						OperatorParameter.NAME_tableSetConfig).getValue();
				if(joinModel!=null){
					List<ColumnMap> tables = joinModel.getColumnMapList();
					for(int j=0;j<tables.size();j++){
						UIOperatorModel opModel = operatorModelCopies.get(tables.get(j).getOperatorUUID());
						if(opModel != null){
							tables.get(j).setOperatorUUID(opModel.getUUID());
						}
					}
				}
			}else if(copiedOpModel.getOperator() instanceof HadoopJoinOperator){
				HadoopJoinOperator op = (HadoopJoinOperator) copiedOpModel.getOperator();
				HadoopJoinModel joinModel = (HadoopJoinModel) ParameterUtility.getParameterByName(op, OperatorParameter.NAME_HD_JOIN_MODEL).getValue();
				if(joinModel != null){
					for(HadoopJoinFile joinFile : joinModel.getJoinTables()){
						UIOperatorModel opModel = operatorModelCopies.get(joinFile.getOperatorModelID());
						if(opModel != null){
							joinFile.setOperatorModelID(opModel.getUUID());
						}
					}
					for(HadoopJoinCondition joinCond : joinModel.getJoinConditions()){
						UIOperatorModel opModel = operatorModelCopies.get(joinCond.getFileId());
						if(opModel != null){
							joinCond.setFileId(opModel.getUUID());
						}
					}
					for(HadoopJoinColumn joinColumn : joinModel.getJoinColumns()){
						UIOperatorModel opModel = operatorModelCopies.get(joinColumn.getFileId());
						if(opModel != null){
							joinColumn.setFileId(opModel.getUUID());
						}
					}
				}
			}
		 
			else if(copiedOpModel.getOperator() instanceof HadoopUnionOperator){
				HadoopUnionOperator op = (HadoopUnionOperator)copiedOpModel.getOperator();
				HadoopUnionModel joinModel = (HadoopUnionModel) ParameterUtility.getParameterByName(op, OperatorParameter.NAME_HD_Union_Model).getValue();
				
				if(joinModel != null){
					for(HadoopUnionFile unionFile : joinModel.getUnionFiles()){
						UIOperatorModel opModel = operatorModelCopies.get(unionFile.getOperatorModelID());
						if(opModel != null){
							unionFile.setOperatorModelID(opModel.getUUID());
						}
					}
					for(HadoopUnionModelItem unionColumns : joinModel.getOutputColumns()){
						for(HadoopUnionSourceColumn unionColumn : unionColumns.getMappingColumns()){
							UIOperatorModel opModel = operatorModelCopies.get(unionColumn.getOperatorModelID());
							if(opModel != null){
								unionColumn.setOperatorModelID(opModel.getUUID());
							}
						}
						
					}
				}
			}
			else if(copiedOpModel.getOperator() instanceof HadoopPigExecuteOperator){
				HadoopPigExecuteOperator op = (HadoopPigExecuteOperator)copiedOpModel.getOperator();
			
				PigExecutableModel joinModel = (PigExecutableModel) ParameterUtility.getParameterByName(op, OperatorParameter.NAME_HD_PigScript).getValue();
				
				if(joinModel != null&&joinModel.getPigInputMapItems()!=null){
 
					for(  PigInputMapItem item: joinModel.getPigInputMapItems()){
							UIOperatorModel opModel = operatorModelCopies.get(item.getInputUUID());
							if(opModel != null){
								item.setInputUUID(opModel.getUUID());
							}
						}
						
					}
			}
			
		}
	}
	
	private void putCopyIntoWorkFlow(OperatorWorkFlow workflow, Collection<UIOperatorModel> operatorModelSet, Collection<UIOperatorConnectionModel> connectionModelSet){
		for(UIOperatorModel opModel : operatorModelSet){
			opModel.getOperator().setWorkflow(workflow);
			workflow.addChild(opModel);
		}
		for(UIOperatorConnectionModel connModel : connectionModelSet){
			workflow.getConnModelList().add(connModel);
		}
	}
	

	private UIOperatorModelImpl cloneUIOperatorModel(UIOperatorModel source, int offset, 
			FlowInfo targetWorkFlowinfo) throws Exception{
		if(source == null){
			return null;
		}
		UIOperatorModelImpl newModel= new UIOperatorModelImpl();
		newModel.setClassName(source.getClassName());
//		String newName =  source.getId();
//		//this is the source, only paste will need new name
//		if(generateNewName==true){
//			newName = CommonMethod.getOperatorName(source.getId());
//		}
//		newModel.setNewName(newName);
		
//		newModel.setId(source.getId());
//		newModel.setUUID(source.getUUID());
		OperatorPosition position = source.getPosition();
		newModel.setPosition(new OperatorPosition( position.getStartX() + offset, position.getStartY() + offset, position.getX(), position.getY()));
		//id for tablejoin mode
		newModel.setOperator(cloneOperator(source.getOperator(),targetWorkFlowinfo)); 
		newModel.getOperator().setOperModel(newModel);
		return newModel;
	}
	

	@SuppressWarnings("unchecked")
	private Operator cloneOperator(Operator source, FlowInfo targetWorkFlowinfo) throws  Exception{
		Class<Operator> targetClass = (Class<Operator>) Class.forName(source.getClass().getName());
		Operator target = null;
		if( source instanceof CustomizedOperator){
			String udfName = ((CustomizedOperator)source).getOperatorName();
			Constructor<?>[] cons = targetClass.getConstructors(); 
			if(cons.length>0){
				target = (CustomizedOperator)cons[0].newInstance(udfName);
			}
		}
		else{	
			target = targetClass.newInstance();
		}
		target.setOperatorParameterList(cloneParameters(source.getOperatorParameterList(), target));
		
		//this is important for database info...
		target.setUserName(source.getUserName()) ;
		target.setResourceType( source.getResourceType());
		
//		target.setWorkflow(source.getWorkflow());
		target.setLocale(source.getLocale());
		target.setResourceType(source.getResourceType()) ;
		target.setUserName(source.getUserName());
		if(target instanceof SubFlowOperator){
			if(((SubFlowOperator)source).getVariableModel()!=null){
				((SubFlowOperator)target).setVariableModel(((SubFlowOperator)source).getVariableModel().clone());
			}
		
				String filePath = targetWorkFlowinfo.getKey();
				String pathPrefix=filePath.substring(0,filePath.lastIndexOf(File.separator));
				((SubFlowOperator)target).setPathPrefix(pathPrefix);
			 
		}
		return target;
	}

	private List<OperatorParameter> cloneParameters(List<OperatorParameter> operatorParameterList, Operator operator) throws  Exception {
		List<OperatorParameter> cloneParameterList = new ArrayList<OperatorParameter> ();
		if(operatorParameterList!=null){
			for( int i = 0;i<operatorParameterList.size();i++){
				OperatorParameter param = operatorParameterList.get(i);
				OperatorParameter newPparam = new OperatorParameterImpl(operator, param.getName());
				Object paramValue = param.getValue();
				if(paramValue instanceof ParameterObject){
					newPparam.setValue(((ParameterObject)paramValue).clone());
				}else if(paramValue instanceof List<?>){
					newPparam.setValue(ListUtility.cloneStringList((List <String>)paramValue));
				}else if(paramValue instanceof String[]){
					String[] stringArray = (String[]) paramValue;
					String[] newStringArray = new String[stringArray.length];
					for(int j=0;j<stringArray.length;j++){
						newStringArray[j] = (stringArray[j]);
					}
					newPparam.setValue(newStringArray);
				}else{
					newPparam.setValue(paramValue);
				}
				cloneParameterList.add(newPparam);
			}
		}
		return cloneParameterList;
	}
	
	private UIOperatorConnectionModel fillConnectionInfo(Map<String, UIOperatorModel> operatorModelCopies, UIOperatorConnectionModel connectionModel){
		String 	sourceId = connectionModel.getSource().getUUID(),
				targetId = connectionModel.getTarget().getUUID();
		UIOperatorModel sourceOperatorModel = operatorModelCopies.get(sourceId),
						targetOperatorModel = operatorModelCopies.get(targetId);
		if(sourceOperatorModel != null && targetOperatorModel != null){// Connection is only valid if it has source operator and target operator
			return LinkManagement.getInstance().createConnectionModel(sourceOperatorModel, targetOperatorModel);
		}
		return null;
	}
}
