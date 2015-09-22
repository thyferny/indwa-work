/**
 * ClassName SubFlowOperator
 *
 * Version information: 1.00
 *
 * Data: 2012-4-8
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.structual;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.field.IntegerToTextOperator;
import com.alpine.miner.workflow.operator.hadoop.CopyToDBOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopDataOperationOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopFileOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopPredictOperator;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingItem;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.miner.workflow.operator.sampling.SampleSelectorOperator;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class SubFlowOperator extends AbstractOperator {
	//used to read the real subflow
	private String pathPrefix;
    private static final Logger itsLogger=Logger.getLogger(SubFlowOperator.class);


    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_subflowPath, 
			OperatorParameter.NAME_tableMapping,
			OperatorParameter.NAME_exitOperator,
			OperatorParameter.NAME_subflowVariable,
		 
			
	});
	//these 2 are for design time use...
	private VariableModel variableModel = null;
	private OperatorWorkFlow subWorkflow = null; 

	//used for validate ...
	private boolean subflowNotFound = false; 

	//seted when ifrom xml, to tell the UI that the subflow not found
	public boolean isSubflowNotFound() {
		return subflowNotFound;
	}

	public SubFlowOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
		
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(OperatorInputFileInfo.class.getName());

		
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SUBFLOW_OPERATOR,locale);
	}
 
	
	public List<OperatorInputTableInfo> getParentDBTableSet4SubFlow() {
		List<OperatorInputTableInfo> dbTableSets = new ArrayList<OperatorInputTableInfo>();

		List<Object> inputList = this.getOperatorInputList();
		if(inputList==null){
			return dbTableSets;
		}
		for (Object obj : inputList) {
			if (obj instanceof OperatorInputTableInfo) {
				dbTableSets.add((OperatorInputTableInfo) obj);
			}
			if (obj instanceof OperatorInputFileInfo) {
				OperatorInputTableInfo fakeDBTable = toOperatorInputTableInfo((OperatorInputFileInfo) obj);
				if(fakeDBTable!=null){
					dbTableSets.add(fakeDBTable);
				}
			}
		}
		return dbTableSets;
	}

	//this is only for hadoop...
	public static OperatorInputTableInfo toOperatorInputTableInfo(
			OperatorInputFileInfo fileInfo) {
		OperatorInputTableInfo table = new OperatorInputTableInfo();
		table.setTable(		fileInfo.getHadoopFileName()) ;
		List<String> nameList = null;
		List<String> typeList = null;
		if(fileInfo.getColumnInfo() != null){
			nameList = fileInfo.getColumnInfo().getColumnNameList();
			typeList = fileInfo.getColumnInfo().getColumnTypeList();
		}
		if(nameList!=null&&nameList.size()>0&&typeList!=null&&typeList.size()>0&&typeList.size()==nameList.size()){
			List<String[]> fieldColumns =  new ArrayList<String[]>();
			for (int i = 0; i < nameList.size(); i++) {
				fieldColumns.add(new String[]{nameList.get(i),typeList.get(i)});
			}
			table.setFieldColumns(fieldColumns );
			return table;
		}
 		return null;
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		String subflowName = (String)getOperatorParameter(OperatorParameter.NAME_subflowPath).getValue();
		//make sure the subflow parameter is OK
		if(StringUtil.isEmpty(subflowName)==false){
			String subflowPath = pathPrefix+File.separator+subflowName+AFM_SUFFIX;
			
			XMLWorkFlowReader reader=new XMLWorkFlowReader();
			 
			try {
				subWorkflow = reader.doRead(new XMLFileReaderParameters(subflowPath,userName,resourceType),locale);
				subflowNotFound=false;
			}catch (Exception e) {
					itsLogger.error(e.getMessage(),e) ;
					e.printStackTrace();
					subflowNotFound = true;
			}
		}
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		 
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(paraName.equals(OperatorParameter.NAME_tableMapping)){
				//need not validate the table mapping any more ...
				List<OperatorInputTableInfo> parentTableSet = getParentDBTableSet4SubFlow();
				TableMappingModel tableMappingModel = (TableMappingModel)para.getValue() ;
				if(parentTableSet==null||parentTableSet.size()==0||para.getValue()==null||((TableMappingModel)para.getValue())==null
						||((TableMappingModel)para.getValue()).getMappingItems()==null
						||((TableMappingModel)para.getValue()).getMappingItems().size()==0){
					//not set, is OKinvalidParameterList.add(paraName);
				}else if(parentTableSet.size()!= getSubflowInputTablesets().size()){
					invalidParameterList.add(paraName);
				}
				else{
					//inputTableInfo -> subFlowInputTableInfo
					
					for(int i=0 ;i <parentTableSet.size();i++){				
						if(false==hasMappingSource(tableMappingModel,parentTableSet.get(i))){						
							invalidParameterList.add(paraName);
							break;
						}
						
					}
					List<OperatorInputTableInfo> subFlowInputs = this.getSubflowInputTablesets();
					List<TableMappingItem> items = tableMappingModel.getMappingItems();
					for(int i =0;i<items.size();i++){
					
						if(false==hasMappingTarget(items.get(i),subFlowInputs)){						
							invalidParameterList.add(paraName);
							break;
						}
						
					}
					if(invalidParameterList.contains(paraName) ==false){
						HashMap <OperatorInputTableInfo,OperatorInputTableInfo> tableInfoMap = 
							fillTableInfoMap(tableMappingModel,parentTableSet,subFlowInputs);
						Iterator<OperatorInputTableInfo> it = tableInfoMap.keySet().iterator();
						while (it.hasNext()){
							OperatorInputTableInfo parentTableInfo = it.next();
							OperatorInputTableInfo subFlowInputTableInfo = tableInfoMap.get(parentTableInfo);
							if(false==isTableSameColun(parentTableInfo, subFlowInputTableInfo)){
								invalidParameterList.add(paraName);
								break;
							}
						}
					} 
					
				}
			} else if(paraName.equals(OperatorParameter.NAME_subflowPath)){  
				if(subflowNotFound ==true){
					invalidParameterList.add(paraName);
				}
				else {
					String paraValue=(String)para.getValue();
					validateNull(invalidParameterList, paraName, paraValue);
				}
			}
			else if(paraName.equals(OperatorParameter.NAME_exitOperator)){
				List<Operator> childs = this.getChildOperators();
				if(childs!=null&&childs.size()>0){
					boolean hasNoneSQLExecutOperator = false;
					for(int i = 0 ; i <childs.size();i++){
						if(childs.get(i) instanceof SQLExecuteOperator ==false
								&&childs.get(i) instanceof DbTableOperator ==false) {
							hasNoneSQLExecutOperator =true;
						}
					}
					if(hasNoneSQLExecutOperator==true){
						validateNull(invalidParameterList, paraName, (String) para.getValue());
						
						if(false==invalidParameterList.contains(paraName)) {
							if(findExitOperator(para.getValue())==false){
								invalidParameterList.add(paraName);
							}
						}
					}
					
			    }
			}
			
		}
		
		if(invalidParameterList.contains(OperatorParameter.NAME_subflowPath)==false
				&& hasRecursiveSubFlow(this, new HashSet <String>() )==true){
			invalidParameterList.add(OperatorParameter.NAME_subflowPath);
		}
		if(invalidParameterList.contains(OperatorParameter.NAME_subflowPath)==false
				){
			 HashMap<String, String[]> invalidParamerers = getInValidateSubFlowOperatorsParameters(this.getSubFlowModels() );
			
			if(invalidParamerers!=null&&invalidParamerers.keySet().size()>0){
				String paramNames = composeNameString(invalidParamerers);
					invalidParameterList.add(OperatorParameter.NAME_subflowPath+INVALIDESCAPE+
					LanguagePack.getMessage(LanguagePack.SUBFLOW_INVALID,locale)+paramNames);
			}
		}
		
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}
	}
	private boolean findExitOperator(Object value) {
		List<UIOperatorModel> subFLowChilds = getSubFlowModels();
		if(subFLowChilds!=null){
			for(int i = 0 ; i <subFLowChilds.size();i++){
			if((subFLowChilds.get(i).getOperator().getChildOperators()==null
					|| subFLowChilds.get(i).getOperator().getChildOperators().size()==0)
					&&subFLowChilds.get(i).getId().equals(value )) {
				return true;
			}
		} 	
	}
		return false;
	}

	private String composeNameString( HashMap<String, String[]> invalidParamerers) {
		String result = "";
		  Iterator<String> keys = invalidParamerers.keySet().iterator();
		  while(keys.hasNext()){
			  
			  String opName = keys.next();
			  String paramName = composeNameString(invalidParamerers.get(opName));
			  result = result +",[" + opName + ":" + paramName + "]" ;
			
		  }
		  if(result.startsWith(",")){
			  result=result.replaceFirst(",","");
		  }
		return result;
	}


	private String composeNameString(  String[]  invalidParamerers) {
		String result = "";
	 
		for(int i = 0 ;i <invalidParamerers.length;i++){
			result = result + invalidParamerers[i];
			if(i<invalidParamerers.length-1){
				result = result+",";
			}
		}
		return result;
	}

	private HashMap<OperatorInputTableInfo, OperatorInputTableInfo> fillTableInfoMap(
			TableMappingModel tableMappingModel,
			List<OperatorInputTableInfo> parentTableSet,
			List<OperatorInputTableInfo> subFlowInputs) {
		HashMap<OperatorInputTableInfo, OperatorInputTableInfo> resultMap= new HashMap<OperatorInputTableInfo, OperatorInputTableInfo>();
		List<TableMappingItem> items = tableMappingModel.getMappingItems();
		for(int i =0;i<items.size();i++){
		
			 TableMappingItem item = items.get(i); 
			 OperatorInputTableInfo parentInfo = getTableInfo(parentTableSet, item.getInputSchema(),item.getInputTable());
			 OperatorInputTableInfo subInfo = getTableInfo(subFlowInputs, item.getSubFlowSchema(),item.getSubFlowTable());
			 resultMap.put(parentInfo, subInfo) ;
			
		}
		return resultMap;
	}
	
	private OperatorInputTableInfo getTableInfo(	List<OperatorInputTableInfo> tableSet,String schema,String table){ 
		if(tableSet!=null){
			for(OperatorInputTableInfo input :tableSet){
				//for hadoop
				if(StringUtil.isEmpty(schema)){
					if(table.equals(input.getTable())){
						return input;

					}
					
				}//now db
				else if(schema.equals(input.getSchema())
						&&table.equals(input.getTable())){
					return input;
				}
			}
		}
		return null;
	}

	private boolean hasMappingTarget(TableMappingItem tableMappingItem, List<OperatorInputTableInfo> subInputs) {
		String schema = tableMappingItem.getSubFlowSchema() ;
		String table = tableMappingItem.getSubFlowTable();
	
		if(subInputs!=null){
			for(OperatorInputTableInfo input :subInputs){
				if(StringUtil.isEmpty(schema)){
					if(table.equals(input.getTable())){
						return true;
					}
				}//now for db 
				else if(schema.equals(input.getSchema())
						&&table.equals(input.getTable())){
					return true;
				}
			}
		}
 
		return false;
	}

	//operatorName -> parameterNames
	private HashMap<String,String []> getInValidateSubFlowOperatorsParameters(
			List<UIOperatorModel> subFlowModels) {
		HashMap<String,String []> opNameParamNameMap = new HashMap<String,String []>();
		if(subFlowModels!=null&&subFlowModels.size()>0){
			for(int i =0 ;i <subFlowModels.size();i++){
				Operator subFlowSubOperator = subFlowModels.get(i).getOperator(); 
				//    MINER-1968	[subflow variable]subflow operate variable don't affect flow result
				if( subFlowSubOperator.isVaild(getVariableModel()) ==false){
					opNameParamNameMap.put(subFlowSubOperator.getOperModel().getId(), subFlowSubOperator.getInvalidParameters());
				} 
			}
		}
		return opNameParamNameMap;
	}

	private static OperatorInputTableInfo createOperatorInputTableInfo(
			DbTableOperator operator) {
		if(operator!=null&&operator.getOperatorOutputList()!=null&&operator.getOperatorOutputList().size()>0){
			return  (OperatorInputTableInfo)operator.getOperatorOutputList().get(0)	 ;
		}
		return null;
	}
	
	public List<OperatorInputTableInfo> getSubflowInputTablesets() {
		List<UIOperatorModel> uiModels = getSubFlowModels();
		List<OperatorInputTableInfo> infos = new ArrayList<OperatorInputTableInfo> ();
		if(uiModels!=null){
			for(int  i = 0 ;i<uiModels.size();i++){
				Operator operator = uiModels.get(i).getOperator();
				if((operator.getParentOperators()==null||operator.getParentOperators().size()==0)
						&&operator instanceof DbTableOperator){
					infos.add(createOperatorInputTableInfo((DbTableOperator)operator));
				}else if ((operator.getParentOperators()==null||operator.getParentOperators().size()==0)
				&&operator instanceof HadoopFileOperator){
					OperatorInputTableInfo fakeInfo = createOperatorInputTableInfo((HadoopFileOperator)operator);
					if(fakeInfo!=null){
						infos.add(fakeInfo) ;
					}
				}
				 
			}
		}
		
		return infos;
	}

 

	public void setSubflowNotFound(boolean subflowNotFound) {
		this.subflowNotFound = subflowNotFound;
	}

	private boolean hasRecursiveSubFlow( Operator startOperator,Set<String> workflowNameLoopList ) {
		
		workflowNameLoopList.add(startOperator.getWorkflow().getName()) ;
		if(startOperator instanceof SubFlowOperator &&((SubFlowOperator)startOperator).getSubFlowModels()!=null){
			 Iterator<UIOperatorModel> it = ((SubFlowOperator)startOperator).getSubFlowModels().iterator();
			 
			while(it.hasNext()){
				Operator op = it.next().getOperator();
				if(op instanceof SubFlowOperator == false) {
					continue;
				}
				String subflowName = (String) ParameterUtility.getParameterValue(op, OperatorParameter.NAME_subflowPath) ;
			  if(subflowName!=null&& workflowNameLoopList.contains(subflowName)) {
					//find duplicate, means a recursive
					return true;
				}else{//check the child
					if(hasRecursiveSubFlow(op,workflowNameLoopList    )){
						return true;
					}else{
						workflowNameLoopList.remove(op.getWorkflow().getName()) ;
					} 
				}
			}
		}
		workflowNameLoopList.remove(startOperator.getWorkflow().getName()) ;
		return false;
 
	}

	private boolean hasMappingSource(TableMappingModel model,
			 OperatorInputTableInfo  inputTableSet) {
		List<TableMappingItem> items = model.getMappingItems(); 
		for(int i =0;i<items.size();i++){
			String schema = items.get(i).getInputSchema() ;
			String table =  items.get(i).getInputTable();
			//for hadoop 
			if(StringUtil.isEmpty(schema)){
				if(table.equals(inputTableSet.getTable())){
					return true;
				}
			}//now for db 
			else if(schema.equals(inputTableSet.getSchema())
					&&table.equals(inputTableSet.getTable())){
				return true;
			}
		}
		return false;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);

		OperatorParameter tableMappingParam=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_tableMapping);
		TableMappingModel tableMappingModel = (TableMappingModel)tableMappingParam.getValue();
		if(tableMappingModel!=null){
			element.appendChild(tableMappingModel.toXMLElement(xmlDoc,addSuffixToOutput,userName));
		}
		
		/**
		 * save sub-flow variable model
		 */
		if(variableModel!=null){	
			element.appendChild(variableModel.toXMLElement(xmlDoc));
		}
	}
	
	@Override
	public List<String> getOutputClassList() {
		Object exitTableInfo = getExitTableInfo();
		if(exitTableInfo!=null){
			List<String> result= new ArrayList<String>();
			result.add(exitTableInfo.getClass().getName());
			return result;
		}
		return outputClassList;
	}

	
	@Override
	public List<Object> getOutputObjectList() {
	 
		 
		List<Object> list = getOperatorOutputList();
		if(list==null||list.size()==0){ //default
			list.add(new OperatorInputTableInfo());
		}
		return list;
	}
 
	//TODO: here need to avoid the recursive subflow too
 
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode,  HashSet<String>  readedSubFlowNames) throws Exception   {
		subflowNotFound = false ;
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> tableSetNodes=opTypeXmlManager.getNodeList(opNode, TableMappingModel.TAG_NAME);
		if(tableSetNodes!=null&&tableSetNodes.size()>0){
			Element tableSetElement=(Element)tableSetNodes.get(0);
			TableMappingModel tableSetModel=TableMappingModel.fromXMLElement(tableSetElement); 
			getOperatorParameter(OperatorParameter.NAME_tableMapping).setValue(tableSetModel);
		}		
	
		String subflowName = (String)getOperatorParameter(OperatorParameter.NAME_subflowPath).getValue();
		if(readedSubFlowNames.contains(subflowName)==true){
			itsLogger.error("[Error: Subflow Operator] fromXML: recursive flow found:" + readedSubFlowNames) ;
			throw new RecursiveException("LanguagePack.Recursive_subflow_found:"+subflowName);
		} 
		//make sure the subflow parameter is OK
		if(StringUtil.isEmpty(subflowName)==false){
			String subflowPath = pathPrefix+File.separator+subflowName+AFM_SUFFIX;
			
			XMLWorkFlowReader reader=new XMLWorkFlowReader();
			reader.setReadedSubFlowNames(readedSubFlowNames) ;
			VariableModel subFlowVariableModel=null;
			 
				 
			try {
				subWorkflow = reader.doRead(new XMLFileReaderParameters(subflowPath,userName,resourceType),locale);
			} catch(RecursiveException ex){
				throw ex;
			}
			catch (Exception e) {
					itsLogger.error(e.getMessage(),e) ;
					e.printStackTrace();
					subflowNotFound = true ;
			
			}
		
			
			if(subWorkflow!=null&&subWorkflow.getVariableModelList()!=null){
					List<VariableModel> subFlowVariableModelList=subWorkflow.getVariableModelList();
					subFlowVariableModel=subFlowVariableModelList.get(0);
			}
 
			
			if(subFlowVariableModel!=null){			
				VariableModel variableModel = null;
				
				ArrayList<Node> variableModelNodes = opTypeXmlManager.getNodeList(opNode,VariableModel.MODEL_TAG_NAME); 
				if(variableModelNodes!=null&&variableModelNodes.size()>0){
					//only one 
					for (Node variableModelNode:variableModelNodes) {		
						Element quantileElement=(Element)variableModelNode;
						variableModel = VariableModel.fromXMLElement(quantileElement);	
					}
			
					Iterator<Entry<String, String>> iter = subFlowVariableModel.getIterator();
					while(iter.hasNext()){
						Entry<String, String> entry = iter.next();
						String variableName = entry.getKey();
						if(variableModel.containsKey(variableName)){
							entry.setValue(variableModel.getVariable(variableName));
						}
					}
					setVariableModel(subFlowVariableModel);
				}
			}
		}
 
		
		return operatorParameters;
	}
 
	@Override
	public String validateInputLink(Operator parent) {
		 
		if(parent instanceof SQLExecuteOperator){
			List<UIOperatorModel>  operatorList=OperatorUtility.getParentList(parent.getOperModel());
			if(operatorList==null||operatorList.size()==0){
				return LanguagePack.getMessage(LanguagePack.SQLEXECUTE_HAVE_NO_PRECEDING_OPERATOR,locale);
			}else{
				return this.validateInputLink(operatorList.get(0).getOperator());
			}	
		}else{
			/**
			 * check inputClass and outputClass is equals
			 */
			String	message="" ;
			 if(parent instanceof HadoopDataOperationOperator){
				 message = super.validateStoreResult(parent);
					if(message!=null){
						return message;
					} 
			 }
			 	
			  message = isOutputTypeSameAsInputType(parent);
			if(message!=null){
				return message;
			}
		
			/**
			 * check input db table info 
			 */
			message =isInputDBTableInfoOK(parent);
			if(message!=null){
				return message;
			}
		 
			/**
			 * check repick linked
			 */
			if(parent.getOperModel().containTarget(getOperModel())){
				return LanguagePack.getMessage(LanguagePack.MESSAGE_ALREADY_LINK,locale);
			}
			List<OperatorInputTableInfo> inputTables = this.getParentDBTableSet();
			List<Object> newInput = parent.getOperatorOutputList();
			if(newInput!=null&&inputTables!=null){
				for(int i =0;i<newInput.size();i++){
					Object input = newInput.get(i);
					if((input instanceof OperatorInputTableInfo)&&isTableAlreadyLinked((OperatorInputTableInfo)input,inputTables)==true){
						OperatorInputTableInfo table =(OperatorInputTableInfo) input;
						return LanguagePack.getMessage(LanguagePack.TABLE_ALREADY_LINK,locale)
						+" : "+table.getSchema()+"."+table.getTable();
					}
				}
			}
			
			return "";
		}
	
	}
 

	private boolean isTableAlreadyLinked(OperatorInputTableInfo newInput,
			List<OperatorInputTableInfo> inputTables) { 
		if(inputTables!=null&&inputTables.size()>0){
			for(int i =0;i<inputTables.size();i++){
				OperatorInputTableInfo input = inputTables.get(i);
				if(newInput.getSchema()!=null&&ParameterUtility.nullableEquales(newInput.getSchema(), input.getSchema())
						&&ParameterUtility.nullableEquales(newInput.getTable(), input.getTable())){
					return true;
				} 
			}
		}
		return false;
	}

	// can have no input
	@Override
	public boolean isInputObjectsReady() {
		return true;
	}
	
	@Override
	public List<Object> getOperatorOutputList() {
		//get table info from subflow...
		List<Object> operatorInputList = new ArrayList<Object> ();
		Object exitTableInfo = getExitTableInfo(); 
		if(exitTableInfo!=null){
			operatorInputList.add(getExitTableInfo());
		}
 		return operatorInputList;
	}
	
 
	public Object getExitTableInfo() {
		if (subWorkflow == null) {
			return null;
		}
		List<UIOperatorModel> uimodels = subWorkflow.getChildList();
		String exitOperatorName = (String) ParameterUtility.getParameterValue(
				this, OperatorParameter.NAME_exitOperator);
		if (exitOperatorName == null || uimodels == null) {
			return null;
		}
		for (int i = 0; i < uimodels.size(); i++) {
			if (uimodels.get(i).getId().equals(exitOperatorName) == false) {
				continue;
			}
			Operator exitOperator = uimodels.get(i).getOperator(); 
			// the exit operator could be a subflow operator too
			if (exitOperator instanceof SubFlowOperator) {//recursive
				return ((SubFlowOperator) exitOperator).getExitTableInfo();
			} else {
				List<Object> outputs = exitOperator.getOperatorOutputList();
				for (int j = 0; j < outputs.size(); j++) {
					// the table must from outputtable (paramname = outputTable)
					// or a db table MINER-1892
					if (outputs.get(j) instanceof OperatorInputTableInfo) {
						String tableName = ((OperatorInputTableInfo) outputs.get(j)).getTable();
						if (tableName != null
								&& (exitOperator instanceof SampleSelectorOperator
										|| exitOperator instanceof IntegerToTextOperator
										||exitOperator instanceof CopyToDBOperator
										|| tableName.equals(ParameterUtility.getParameterValue(
												exitOperator,OperatorParameter.NAME_tableName)) 
										|| tableName.equals(ParameterUtility.getParameterValue(
												exitOperator,OperatorParameter.NAME_outputTable)))) {
							return (OperatorInputTableInfo) outputs.get(j);
						}
					}
					if (outputs.get(j) instanceof OperatorInputFileInfo) {
						FileStructureModel columnInfo = ((OperatorInputFileInfo) outputs.get(j)).getColumnInfo();
						if (columnInfo != null
								&& exitOperator instanceof HadoopPredictOperator==false) {
							return (OperatorInputFileInfo) outputs.get(j);
						}
					}
				}
			}

		}

		return null;
	}
 

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix=pathPrefix;	
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	//check the parameer value to know whether  the exitoperator is configured
	public boolean hasExitOperator() {
		if(ParameterUtility.getParameterByName(this, OperatorParameter.NAME_exitOperator)!=null
				&&false==StringUtil.isEmpty((String) ParameterUtility.getParameterByName(this, OperatorParameter.NAME_exitOperator).getValue()	))	{
			return true;
		}
		else{
			return false;
		}
	}

	public VariableModel getVariableModel() {
		return variableModel;
	}

	public void setVariableModel(VariableModel variableModel) {
		this.variableModel = variableModel;	}
 
	public List<UIOperatorModel> getSubFlowModels() {
		if(subWorkflow!=null){
			return subWorkflow.getChildList();
		}else {
			return null;
		}
	}

	public void setSubWorkflow(OperatorWorkFlow workflow) {
		this.subWorkflow=workflow;
		
	}
	public OperatorWorkFlow getSubWorkflow(   ) {
		return this.subWorkflow;
		
	}

	public boolean isInputColumnsIncludeSubflowColumns(List<String[]> inputColumns,
			List<String[]> subflowColumns, DataSourceType dbType) { 
		if(inputColumns==null||subflowColumns==null ){
			return false;
		}
//		for(int i = 0 ; i<inputColumns.size();i++){
//			if(hasNoClumnn(inputColumns.get(i),subflowColumns,   dbType)==false){
//				return false;
//			}
//		}
		
		for(int i = 0 ; i<subflowColumns.size();i++){
			if(hasNoClumnn(subflowColumns.get(i),inputColumns,   dbType)==false){
				return false;
			}
		}
		return true;
	}

	private boolean hasNoClumnn(String[] strings, List<String[]> columns, DataSourceType   dbType) { 
		for(int i = 0 ; i<columns.size();i++){
			 	if(columns.get(i)[0].equals(strings[0])
			 			&& isSameType(columns.get(i)[1], strings[1],dbType)){			 
				return true;
			} 
		}
		return false;
	}

	private boolean isSameType(String type1, String type2,DataSourceType   dbType){
			if(type1.equalsIgnoreCase(type2)){
				return true;
			}
			else if(dbType.isNumberColumnType(type1)&&dbType.isNumberColumnType(type2)){
				return true;
			}
			else if(dbType.isIntegerColumnType( type1)&&dbType.isIntegerColumnType(type2)){
				return true;
			}else if(dbType.isDateColumnType( type1)&&dbType.isDateColumnType(type2)){
				return true;
			}
			else if(dbType.isDateColumnType( type1)&&dbType.isDateColumnType(type2)){
				return true;
			}
			else if (type1.indexOf("(") >=0|| type2.indexOf("(")>=0){  
				if (type1.indexOf("(")>=0) { 
					type1= type1.substring(0,type1.indexOf("(")) ;
				}
				if (type2.indexOf("(")>=0) { 
					type2= type2.substring(0,type2.indexOf("(")) ;
				}
				return isSameType(type1,   type2,    dbType);
			}
			else {
				 
				return false;
			}

	}


	private boolean isTableSameColun(OperatorInputTableInfo inputTableInfo,
			OperatorInputTableInfo subFlowInputTableInfo) {
		String intputConnName = inputTableInfo.getConnectionName();
		String subflowConnName = subFlowInputTableInfo.getConnectionName();
 
		// 1 connection is not same...
		DbConnectionInfo inputConn;
		DataSourceType stype = HadoopDataType.INSTANCE;

		try {
			if(intputConnName!=null){//hadoop has no connection name 
				inputConn = DBResourceManagerFactory.INSTANCE.getManager()
						.getDBConnection(System.getProperty("user.name"),
								intputConnName, ResourceType.Personal);
	
				DbConnectionInfo subflowConn = DBResourceManagerFactory.INSTANCE
						.getManager().getDBConnection(
								System.getProperty("user.name"), subflowConnName,
								ResourceType.Personal);
				if (inputConn == null || inputConn.getConnection().equals(subflowConn.getConnection()) == false) {
		 
					return false;
				}
				  stype = DataSourceType.getDataSourceType(inputConn.getConnection().getDbType());

			}
			List<String[]> inputColumns = inputTableInfo.getFieldColumns();
			List<String[]> subflowColumns = subFlowInputTableInfo
					.getFieldColumns();
	 
			if ( isInputColumnsIncludeSubflowColumns(inputColumns ,subflowColumns,stype)  == false) {
				 
				return false;

			}
		} catch (Exception e1) {
			itsLogger.error(e1.getMessage(),e1);
			e1.printStackTrace();
		}
		return true;
	}

	public Operator getExitOperator() {
		if (subWorkflow == null) {
			return null;
		}
		
		String exitOperatorName = (String) ParameterUtility.getParameterValue(
				this, OperatorParameter.NAME_exitOperator);
		
		return getExitOperatorByName(exitOperatorName);
	}

	public Operator getExitOperatorByName(String exitOperatorName) {
		List<UIOperatorModel> uimodels = subWorkflow.getChildList();
		if (exitOperatorName == null || uimodels == null) {
			return null;
		}
		for (int i = 0; i < uimodels.size(); i++) {
			if (uimodels.get(i).getId().equals(exitOperatorName) == false) {
				continue;
			}
			Operator exitOperator = uimodels.get(i).getOperator(); 
			// the exit operator could be a subflow operator too
			if (exitOperator instanceof SubFlowOperator) {//recursive
				return ((SubFlowOperator) exitOperator).getExitOperator();
			} else {
				return exitOperator;
			}

		}

		return null;
	}

	
	public static OperatorInputTableInfo createOperatorInputTableInfo(
			HadoopFileOperator  operator) {
		Object fs = ParameterUtility.getParameterValue(operator,OperatorParameter.NAME_HD_fileStructure);
		if(fs!=null&&ParameterUtility.getParameterValue(operator,OperatorParameter.NAME_HD_fileName)!=null){
			
			List<String> nameList = ((FileStructureModel)fs).getColumnNameList();
			List<String> typeList = ((FileStructureModel)fs).getColumnTypeList();
			if(nameList!=null&&nameList.size()>0&&typeList!=null&&typeList.size()>0&&typeList.size()==nameList.size()){
				OperatorInputTableInfo info = new OperatorInputTableInfo();
				List<String[]> fieldColumns =  new ArrayList<String[]>();
				for (int i = 0; i < nameList.size(); i++) {
					fieldColumns.add(new String[]{nameList.get(i),typeList.get(i)});
				}
				info.setFieldColumns(fieldColumns );
				info.setOperatorUUID(operator.getOperModel().getUUID()) ;
				info.setTable((String)ParameterUtility.getParameterValue(operator,OperatorParameter.NAME_HD_fileName))  ;
				return info;
			}
			
		}
		return null;
	}
}
