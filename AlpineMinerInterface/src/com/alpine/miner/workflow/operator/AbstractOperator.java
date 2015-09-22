/**
 * ClassName AbstractOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.NumberUtil;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopAggregateOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopDataOperationOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopPigExecuteOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.OperatorParameterImpl;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.ParameterValidateUtility;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateField;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterFactory;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterHelper;
import com.alpine.miner.workflow.operator.parameter.storageparam.StorageParameterModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.FileUtility;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhaoyong
 * 
 */
public abstract class AbstractOperator implements Operator {
    private static final Logger itsLogger=Logger.getLogger(SubFlowOperator.class);
    // most have one ,but sampling and cvd have multipule
	
	//private static final Logger itsLogger=Logger.getLogger(AbstractOperator.class);
	
	protected static final String connString = com.alpine.utility.db.Resources.FieldSeparator;
	
	protected OperatorWorkFlow workflow;

	protected String[] invalidParameters;

	protected String userName;
	protected ResourceType resourceType;
	
	protected Locale locale;
	
	public static final String INVALIDESCAPE="==";

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	private List<OperatorInputTableInfo> opratorInputTableInfo;
	private List<OperatorInputTableInfo> operatorOutputTableInfo;

	public List<OperatorInputTableInfo> getOpratorInputTableInfo() {
		return opratorInputTableInfo;
	}

	public void setOpratorInputputTableInfo(
			List<OperatorInputTableInfo> opratorInputTableInfo) {
		this.opratorInputTableInfo = opratorInputTableInfo;
	}

	public List<OperatorInputTableInfo> getOperatorOutputTableInfo() {
		return operatorOutputTableInfo;
	}

	public OperatorWorkFlow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(OperatorWorkFlow workflow) {
		this.workflow = workflow;
	}

	public void setOperatorOutputTableInfo(
			List<OperatorInputTableInfo> operatorOutputTableInfo) {
		this.operatorOutputTableInfo = operatorOutputTableInfo;
	}

	protected List<String> inputClassList = null;
	protected List<String> outputClassList = null;

	private UIOperatorModel operModel;
	private List<String> parameterNames = null;

	public void setParameterNames(List<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	private List<OperatorParameter> operatorParameterList;

	public List<OperatorParameter> getOperatorParameterList() {
		return operatorParameterList;
	}

	public void setOperatorParameterList(
			List<OperatorParameter> operatorParameterList) {
		this.operatorParameterList = operatorParameterList;
	}

	protected AbstractOperator(List<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	protected AbstractOperator() {

	}

	public UIOperatorModel getOperModel() {
		return operModel;
	}

	public void setOperModel(UIOperatorModel operModel) {
		this.operModel = operModel;
	}

	public void addInputClass(String className) {
		if (inputClassList == null) {
			inputClassList = new ArrayList<String>();
		}
		inputClassList.add(className);
	}

	public List<String> getInputClassList() {
		return inputClassList;
	}

	public void addOutputClass(String className) {
		if (outputClassList == null) {
			outputClassList = new ArrayList<String>();
		}
		outputClassList.add(className);
	}

	public List<String> getOutputClassList() {
		
		return outputClassList;
	}

	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputTableInfo> dbList = new ArrayList<OperatorInputTableInfo>();
		if (list != null) {
			for (Object obj : list) {
				if (obj instanceof OperatorInputTableInfo) {
					dbList.add((OperatorInputTableInfo) obj);
				}
			}
		}
		if (dbList.size() == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<String> getParameterNames() {
		return this.parameterNames;
	}

	public List<Operator> getParentOperators() {
		List<Operator> parents = new ArrayList<Operator>();
		List<UIConnectionModel> connections = getOperModel()
				.getSourceConnection();
		for (Iterator<UIConnectionModel> iterator = connections.iterator(); iterator
				.hasNext();) {
			UIConnectionModel uiConnectionModel = iterator.next();
			Operator operator = uiConnectionModel.getSource().getOperator();
			parents.add(operator);

		}
		return parents;

	}

	public List<Operator> getChildOperators() {
		List<Operator> children = new ArrayList<Operator>();
		List<UIConnectionModel> connections = getOperModel()
				.getTargetConnection();
		for (Iterator<UIConnectionModel> iterator = connections.iterator(); iterator
				.hasNext();) {
			UIConnectionModel uiConnectionModel = iterator.next();
			Operator operator = uiConnectionModel.getTarget().getOperator();
			children.add(operator);

		}
		return children;

	}

	@Override
	public String validateInputLink(Operator precedingOperator   ) {
		return validateInputLink(  precedingOperator,   false);
	}
	@Override
	public String validateInputLink(Operator precedingOperator, boolean multiple) {
		if (precedingOperator instanceof SubFlowOperator) {
			if (this instanceof DbTableOperator){
				return "";
			}
			 
		}
		if (precedingOperator instanceof SQLExecuteOperator) {

			List<UIOperatorModel> operatorList = OperatorUtility
					.getParentList(precedingOperator.getOperModel());
			if (operatorList == null || operatorList.size() == 0) {
				if (this instanceof DbTableOperator)
					return "";
				else
					return LanguagePack.getMessage(LanguagePack.SQLEXECUTE_HAVE_NO_PRECEDING_OPERATOR,locale);
			} else {
				return this
						.validateInputLink(operatorList.get(0).getOperator(),multiple);
			}
		} else {
			/**
			 * check inputClass and outputClass is equals
			 */
			Operator subFlowOperator = precedingOperator;
			if(precedingOperator instanceof SubFlowOperator){
				precedingOperator = ((SubFlowOperator)precedingOperator).getExitOperator();
			}
			if(precedingOperator ==null){
				precedingOperator = subFlowOperator;
			}
			List<String> sOutputList = precedingOperator.getOutputClassList();
			List<String> tInputList = this.getInputClassList();
			boolean isReady = false;
			
//			if(precedingOperator instanceof ModelOperator &&// (AbstractOperator.this instanceof HadoopPredictOperator||this instanceof PredictOperator)){
			if(precedingOperator instanceof ModelOperator && sOutputList==null){
				sOutputList = fillModelType(precedingOperator);
			 
			}
			 if (sOutputList == null || tInputList == null) {
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						precedingOperator.getToolTipTypeName(), this
								.getToolTipTypeName());

			}else{
				for (int i = 0; i < sOutputList.size(); i++) {
					for (int j = 0; j < tInputList.size(); j++) {
						if (sOutputList.get(i).equals(tInputList.get(j))
								//||isTypeOK4ModelType(sOutputList.get(i),tInputList.get(j))==true
								||isClassOk4SplitModel(sOutputList.get(i),tInputList.get(j))) {
							isReady = true;
							break;
						}
					}
				}
				if (!isReady) {
					return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
							precedingOperator.getToolTipTypeName(), this
									.getToolTipTypeName());

				}
			}
	
			/**
			 * check repick linked
			 */
			if (precedingOperator.getOperModel().containTarget(getOperModel())) {
				return LanguagePack.getMessage(LanguagePack.MESSAGE_ALREADY_LINK,locale);
			}

			if(multiple==false){
				return validateMultipleInput(precedingOperator);
			}
			return "";
			
		}
	}
//
//	private boolean isTypeOK4ModelType(String clas1, String clas2) {
//		if(clas2.equals(AdaboostModel)
//				&&clas1.endsWith(suffix)){
//			
//		}else if(clas2.equals(EMModel)
//				&&clas1.endsWith(suffix)){
//			
//		}else 
//			if(clas2.equals(LogisticRegressionModelDB)
//					&&clas1.endsWith(suffix)){
//				
//			}
//			else 
//				if(clas2.equals(LinearRegressionModelDB)
//						&&clas1.endsWith(suffix)){
//					
//				}
//				else 
//					if(clas2.equals(NBModel)
//							&&clas1.endsWith(suffix)){
//						
//					}
//					else if(clas2.equals(NnModel)
//				&&clas1.endsWith(suffix)){
//			
//		}
//		if(clas2.equals(PLDAModel)
//				&&clas1.endsWith(suffix)){
//			
//		}
//		// TODO Auto-generated method stub
//		return false;
//	}

	public boolean isClassOk4SplitModel(String cls1, String cls2) { 
		if(cls1!=null&&cls1.equals(EngineModel.MPDE_TYPE_LIR_SPLITMODEL)&&cls2.equals(EngineModel.MPDE_TYPE_LIR))	{
			return true;
		}else if(cls1!=null&&cls1.equals(EngineModel.MPDE_TYPE_LR_SPLITMODEL)&&cls2.equals(EngineModel.MPDE_TYPE_LOR))	{
			return true;
		}
		return false;
	}

	protected String validateMultipleInput(Operator precedingOperator) {
		/**
		 * check only one table output operator linked
		 */
		List<String> list = precedingOperator.getOutputClassList();
		if(precedingOperator instanceof ModelOperator && list==null){
			list = fillModelType(precedingOperator);
		 
		}
		boolean sourceTable = false;
		for (String str : list) {
			if (str.equals(OperatorInputTableInfo.class.getName())) {
				sourceTable = true;
			}
		}
		boolean targetTable = false;

		List<UIOperatorModel> parentList = OperatorUtility
				.getParentList(getOperModel());
		for (UIOperatorModel om : parentList) {
			if(om.getOperator().getOutputClassList()!=null){
			for (String str : om.getOperator().getOutputClassList()) {//model can be null in design time
				if (str!=null&&str.equals(OperatorInputTableInfo.class.getName())) {
					targetTable = true;
					break;
				}
			}
		}
		}

		if (sourceTable && targetTable) {
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.CANT_LINK_MUTIL_DATASOURCE,locale),
					this.getToolTipTypeName());
		}else{
			return "";
		}
	}

	protected List<String> fillModelType(Operator precedingOperator) {
		ModelOperator modelOperator = (ModelOperator)(precedingOperator);
		EngineModel engienModel = modelOperator.getModel();
		if(engienModel == null) {
			Object modelFilePath = ParameterUtility.getParameterValue(modelOperator, OperatorParameter.NAME_Model_File_Path) ;
			if(modelFilePath!=null){
				engienModel =loadModelFromFile(modelFilePath.toString());
				itsLogger.debug("Model loaded from :" +modelFilePath.toString()) ;
			}
		}
		List<String> sOutputList= new ArrayList<String>();
		if(engienModel!=null){
			sOutputList.add(engienModel.getModelType());
		}
		return sOutputList;
	}

	@Override
	public String[] getInvalidParameters() {
		return invalidParameters;
	}

	
	
	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale=locale;
	}

	protected void validateNull(List<String> invalidParameterList,
			String paraName, String paraValue) {
		if (StringUtil.isEmpty(paraValue)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected void validateContainColumns(List<String> fieldList, List<String> invalidParameterList, String paraName, String paraValue) {
		if (!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)) {
			if (!fieldList.contains(paraValue)) {
				invalidParameterList.add(paraName);
			} 
		}
	}
	
	protected void validateInteger(List<String> invalidParameterList,
			String paraName, String paraValue,VariableModel variableModel) {
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		if (!StringUtil.isEmpty(paraValue) && !NumberUtil.isInteger(paraValue)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected void validateInteger(List<String> invalidParameterList,
			String paraName, String paraValue, int min,
			boolean isMinOpenInterval, int max, boolean isMaxOpenInterval,VariableModel variableModel) {
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		if (!StringUtil.isEmpty(paraValue)
				&& !NumberUtil.isInteger(paraValue, min, isMinOpenInterval,
						max, isMaxOpenInterval)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected void validateFloat(List<String> invalidParameterList,
			String paraName, String paraValue,VariableModel variableModel) {
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		if (!StringUtil.isEmpty(paraValue) && !NumberUtil.isFloat(paraValue)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected void validateNumber(List<String> invalidParameterList,
			String paraName, String paraValue,VariableModel variableModel) {
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		if (!StringUtil.isEmpty(paraValue) && !NumberUtil.isNumber(paraValue)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected void validateNumber(List<String> invalidParameterList,
			String paraName, String paraValue, double min,
			boolean isMinOpenInterval, double max, boolean isMaxOpenInterval,VariableModel variableModel) {
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		if (!StringUtil.isEmpty(paraValue)
				&& !NumberUtil.isNumber(paraValue, min, isMinOpenInterval, max,
						isMaxOpenInterval)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected void validateTableName(List<String> invalidParameterList,
			String paraName, String paraValue,VariableModel variableModel) {
		boolean isValid;
		String realTableName = VariableModelUtility.getReplaceValue(variableModel, paraValue);
		if (!StringUtil.isEmpty(realTableName)) {
			isValid = realTableName.substring(0, 1).matches("^[a-zA-Z_@]*");
		} else {
			isValid = false;
		}

		if (!isValid && !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}
	
	protected void validateSchemaName(List<String> invalidParameterList,
			String paraName, String paraValue,VariableModel variableModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		OperatorParameterHelper helper = OperatorParameterFactory.INSTANCE
		.getHelperByParamName(paraName);
		boolean isValid=true;
		try {
			List<String> avaliableValues = helper.getAvaliableValues(
					getOperatorParameter(paraName),getUserName(),
					ResourceType.Personal);	
			if(avaliableValues==null||avaliableValues.isEmpty()||avaliableValues.size()==0
					||!avaliableValues.contains(paraValue)){
				isValid=false;
			}
			if(!StringUtil.isEmpty(paraValue)
					&&paraValue.equals(VariableModel.DEFAULT_SCHEMA)){
				String realSchemaName = variableModel.getVariable(VariableModel.DEFAULT_SCHEMA);
				if(!avaliableValues.contains(realSchemaName)){
					isValid=false;
				}
			}
		} catch (Exception e) {
			isValid=false;
		}	
		if (!isValid) {
			invalidParameterList.add(paraName);
		}	
	}

	public OperatorParameter getOperatorParameter(String paraName) {
		List<OperatorParameter> operatorParameterList = getOperatorParameterList();
		if (operatorParameterList == null)
			return null;
		for (OperatorParameter operatorParameter : operatorParameterList) {
			if (operatorParameter.getName().equals(paraName)) {
				return operatorParameter;
			}
		}
		return null;
	}

	public List<Operator> getParentOperatorList() {
		
		List<UIConnectionModel> connList = getOperModel().getSourceConnection();
		List<Operator> parentList = new ArrayList<Operator>();
		for (UIConnectionModel connModel : connList) {
			UIOperatorModel operatorModel = connModel.getSource();
			parentList.add(operatorModel.getOperator());
		}
		return parentList;
	}

	@Override
	public List<Object> getOperatorInputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		// get the parent operators
		List<Operator> parentOperatorList = getParentOperatorList();
		for (Operator operator : parentOperatorList) {
			List<Object> parentOperatorOutlutList = operator
					.getOperatorOutputList();
			if (parentOperatorOutlutList == null)
				continue;
			for (Object object : parentOperatorOutlutList) {
				if(object instanceof OperatorInputTableInfo){
					((OperatorInputTableInfo)object).setOperatorUUID (operator.getOperModel().getUUID()) ;
				}
				operatorInputList.add(object);
			}
		}
		return operatorInputList;
	}

	@Override
	public List<Object> getOperatorOutputList() {
	 
		List<Object> operatorInputList = getOperatorInputList();
		if (operatorInputList == null)
			return null;
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				operatorInputTableInfo.setSchema((String) getOperatorParameter(
						OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String) getOperatorParameter(
						OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo
						.setTableType((String) getOperatorParameter(
								OperatorParameter.NAME_outputType).getValue());
				break;
			}
		}
		return operatorInputList;
	}

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				opNode, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();
		addParameterList(operatorParameters, parameterNodeList, paraMap);
	 
		String[] paramName = StorageParameterModel.getPossibleTags();
		if(paramName!=null){
			for(int i = 0 ;i<paramName.length;i++){
				fillStorageParameters(opTypeXmlManager, opNode, paramName[i], operatorParameters);
			}
		}	
		
		return operatorParameters;
	}
 
	private void addParameterList(List<OperatorParameter> operatorParameters,
			ArrayList<Node> parameterNodeList,
			HashMap<String, OperatorParameter> paraMap) {
		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = null;
			if (paraName.equals("password")) {
				paraValue = XmlDocManager
						.decryptedPassword(((Element) parameterNode)
								.getAttribute("value"));
			} else {
				paraValue = ((Element) parameterNode).getAttribute("value");
			}
			setSimpleParametersValue(this, operatorParameters, paraName,
					paraValue, paraMap);
		}
		setParameters(this, operatorParameters, paraMap);
	}

	//could be resued for plda&pca&svd&pr
	protected void fillStorageParameters(XmlDocManager opTypeXmlManager,
			Node opNode, String paramName,List<OperatorParameter> operatorParameterList) {
		ArrayList<Node> storageModel=opTypeXmlManager.getNodeList(opNode, paramName);
		if(storageModel!=null&&storageModel.size()>0){ 
			Element tableSetElement=(Element)storageModel.get(0);
			StorageParameterModel model=StorageParameterModel.fromXMLElement(tableSetElement); 
			if (operatorParameterList != null){
				for (OperatorParameter operatorParameter : operatorParameterList) {
					if (operatorParameter.getName().equals(paramName)) {
						operatorParameter.setValue(model);
					}
				}
			}
		}
	}

	protected void setSimpleParametersValue(Operator operator,
			List<OperatorParameter> operatorParameters, String paraName,
			String paraValue, HashMap<String, OperatorParameter> paraMap) {
		OperatorParameter operatorParameter = new OperatorParameterImpl(
				operator, paraName);
		operatorParameter.setValue(paraValue);
		operatorParameter.setOperator(operator);
		paraMap.put(paraName, operatorParameter);
	}

	protected void setParameters(Operator operator,
			List<OperatorParameter> operatorParameters,
			HashMap<String, OperatorParameter> paraMap) {
		List<String> paraNamesList = operator.getParameterNames();
		if (paraNamesList == null) {
			itsLogger.warn("Parameter list empty.Return.");
			return;
		}
		Iterator<String> iter = paraNamesList.iterator();
		while (iter.hasNext()) {
			String paraName = iter.next();
			OperatorParameter parameter = paraMap.get(paraName);
			if (parameter != null) {
				operatorParameters.add(parameter);
			} else {
				OperatorParameter operatorParameter = new OperatorParameterImpl(
						operator, paraName);
				operatorParameter.setValue(null);
				operatorParameter.setOperator(operator);
				operatorParameters.add(operatorParameter);
			}
		}
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		List<OperatorParameter> parameterList = getOperatorParameterList();
		if(parameterList!=null){
			Iterator<OperatorParameter> iter_para = parameterList.iterator();
			while (iter_para.hasNext()) {
				OperatorParameter parameter = iter_para.next();
				String paraName = parameter.getName();
				Object value = parameter.getValue();
				if (value instanceof String) {
					createSimpleElements(xmlDoc, element, value, paraName,
							addSuffixToOutput);
				}
				else if (value instanceof StorageParameterModel){
					
					StorageParameterModel model = (StorageParameterModel)ParameterUtility.getParameterByName(this,paraName).getValue();
					Element ele = model.toXMLElement(xmlDoc,paraName); 
	 
					element.appendChild(ele);
					
				}
			}
		}
	}

	@Override
	public boolean isRunningFlowDirty() {
		return false;
	}

	protected List<Object> getParentOutputClassList() {
		ArrayList<Object> list = new ArrayList<Object>();
		List<UIOperatorModel> parentOperatorList = OperatorUtility
				.getParentList(this.getOperModel());
		for (UIOperatorModel operModel : parentOperatorList) {
			for (Object obj : operModel.getOperator().getOutputObjectList()) {
				list.add(obj);
			}
		}
		return list;
	}

	public static void validateColumnNames(List<String> fieldList, List<String> invalidParameterList, String paraName, String paraValue) {
		if (!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)) {
			String[] temp = paraValue.split(",");
			List<String> notContains=new ArrayList<String>();
			 
			for (int i = 0; i < temp.length; i++) {
				if (fieldList==null||!fieldList.contains(temp[i])) {
					notContains.add(temp[i]);
				}
			}
			if (notContains.size()>0) {
				invalidParameterList.add(paraName);
			} 
		}
	}

	@Override
	public String getParameterLabel(String parameterName) {
		return getParameterLabel(parameterName,Locale.getDefault()); 
	}

	@Override
	public String getParameterLabel(String parameterName,Locale locale) {		
		String label = LanguagePack.getMessage(parameterName, locale);
		if (label==null) {
			label = parameterName;
		}
		return label;
	}

	@Override
	public List<Object> getOutputObjectList() {
		return null;
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_dropIfExist)) {
			return Resources.YesOpt;
		} else if (paraName.equals(OperatorParameter.NAME_outputType)) {
			return Resources.TableType;
		} else if (paraName.equals(OperatorParameter.NAME_forceRetrain)) {
			return Resources.YesOpt;
		} else if (paraName.equals(OperatorParameter.NAME_outputSchema)) { 
			//can not apply DEFAULT_SCHEMA for copy to database, 
			return VariableModel.DEFAULT_SCHEMA;
		} 
		return "";
	}

	protected void createSimpleElements(Document xmlDoc,
			Element operatorElement, Object value, String paraName,
			boolean addSuffixToOutput) {
		String str = (String) value;
		createParameterElement(xmlDoc, operatorElement, paraName, str,
				addSuffixToOutput);
	}

	protected void createSimpleElements(Document xmlDoc,
			Element operatorElement, Object value, String paraName) {
		createSimpleElements(xmlDoc, operatorElement, value, paraName, false);
	}

	protected void createParameterElement(Document xmlDoc,
			Element operator_element, String key, String value,
			boolean addSuffixToOutput) {
		Element parameter_element = xmlDoc.createElement("Parameter");
		parameter_element.setAttribute("key", key);
		if (addSuffixToOutput && XmlDocManager.OUTPUTTABLElIST.contains(key)
				&&(this instanceof HadoopPigExecuteOperator)==false) {
			String newTable = StringHandler.addPrefix(value, userName);
			parameter_element.setAttribute("value", newTable);
		} else if (addSuffixToOutput
				&& XmlDocManager.SELECTED_OUTPUT_TABLE.equals(key)) {
			String[] temp = value.split("\\.", 2);
			String newTable = temp[0] + "."
					+ StringHandler.addPrefix(temp[1], userName);
			parameter_element.setAttribute("value", newTable);
		} else if (addSuffixToOutput
				&& XmlDocManager.SELECTED_OUTPUT_FILE.equals(key)) {
			// /output/@default_prefix_rsamp_0_1
			String folder=value.substring(0,value.lastIndexOf(HadoopFile.SEPARATOR));
			String fileName=value.substring(value.lastIndexOf(HadoopFile.SEPARATOR)+1, value.length());

			String newFile = folder + HadoopFile.SEPARATOR
					+ StringHandler.addPrefix(fileName, userName);
			parameter_element.setAttribute("value", newFile);
		} else {
			parameter_element.setAttribute("value", value);
		}

		operator_element.appendChild(parameter_element);
	}

	protected String connectString(List<String> stringList, String connSymbol) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stringList.size(); i++) {
			sb.append(stringList.get(i));
			if (i != stringList.size() - 1) {
				sb.append(connSymbol);
			}
		}
		return sb.toString();
	}

	public void saveInputFieldList(Document xmlDoc, Element operator_element,
			boolean addSuffixToOutput) {
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo info = (OperatorInputTableInfo) obj;
				Element ele = xmlDoc.createElement("InPutFieldList");
				setAttribute(xmlDoc, ele, "url", info.getUrl());
				setAttribute(xmlDoc, ele, "schema", info.getSchema());
				if (addSuffixToOutput && !ifFromDBTableOperator(info.getTable())) {
					String newUserName = StringHandler.addPrefix(info
							.getTable(), userName);
					setAttribute(xmlDoc, ele, "table", newUserName);
				} else {
					setAttribute(xmlDoc, ele, "table", info.getTable());
				}

				setAttribute(xmlDoc, ele, "tableType", info.getTableType());
				setAttribute(xmlDoc, ele, "username", info.getUsername());
				// any encrypt?
				setAttribute(xmlDoc, ele, "password", XmlDocManager
						.encryptedPassword(info.getPassword()));
				setAttribute(xmlDoc, ele, "system", info.getSystem());

				List<String[]> fieldColumns = info.getFieldColumns();

				if (fieldColumns != null && fieldColumns.isEmpty() == false) {
					Element fieldList = xmlDoc.createElement("Fields");
					ele.appendChild(fieldList);
					for (String[] fieldColumn : fieldColumns) {
						Element field = xmlDoc.createElement("Field");
						field.setAttribute("name", fieldColumn[0]);
						field.setAttribute("type", fieldColumn[1]);
						fieldList.appendChild(field);
					}
				}
				operator_element.appendChild(ele);
			}
		}
	}

	protected boolean ifFromDBTableOperator(String table) {
		List<Operator> parentOperatorList = getParentOperators();
		boolean ifFromDBTable = false;
		for (Operator operator : parentOperatorList) {
			if (operator instanceof DbTableOperator) {
				String tableName = (String) ((DbTableOperator) operator)
						.getOperatorParameter(OperatorParameter.NAME_tableName)
						.getValue();
				if (!StringUtil.isEmpty(tableName)&&tableName.equals(table)) {
					ifFromDBTable = true;
					break;
				}
			}else if(operator instanceof SQLExecuteOperator){
				List<Operator> parents = ((SQLExecuteOperator) operator).getParentOperatorList();
				if(parents!=null&&parents.size()!=0){
					Operator parent = parents.get(0);
					if(parent instanceof DbTableOperator){
						ifFromDBTable = true;
						break;
					}
				}
			}
		}
		return ifFromDBTable;
	}

	protected void setAttribute(Document xmlDoc, Element ele, String key, String value) {
		Element para = xmlDoc.createElement("Parameter");
		para.setAttribute("key", key);
		if (StringUtil.isEmpty(key)) {
			para.setAttribute("value", "");
		} else {
			para.setAttribute("value", value);
		}

		ele.appendChild(para);
	}
	
	protected void validateDuplicateValue(List<String> invalidParameterList,
			String  paramName1,String  paramName2) {
		if(ParameterUtility.nullableEquales(
				getOperatorParameter(paramName1).getValue(),
				getOperatorParameter(paramName2).getValue())==true){
			if(invalidParameterList.contains(paramName2)==false){ 
				invalidParameterList.add(paramName2);
			}	
		}
		
	}
	
	protected void validateDuplicateTableName(List<String> invalidParameterList,
			String schemaParam1, String tableParam1,String schemaParam2, String tableParam2) {
		
		
		if(ParameterUtility.nullableEquales(
				getOperatorParameter(schemaParam1).getValue(),
				getOperatorParameter(schemaParam2).getValue())==true
				&&ParameterUtility.nullableEquales(
						getOperatorParameter(tableParam1).getValue(),
						getOperatorParameter(tableParam2).getValue())==true
					){
			//avoid the duplicated
			if(invalidParameterList.contains(tableParam2)==false){ 
				invalidParameterList.add(tableParam2);	
			}
		}
		
	}
	
	public List<OperatorInputTableInfo> getParentDBTableSet() {
		List<OperatorInputTableInfo> dbTableSets = new ArrayList<OperatorInputTableInfo>();

		List<Object> inputList = this.getOperatorInputList();
		if(inputList==null){
			return dbTableSets;
		}
		for (Object obj : inputList) {
			if (obj instanceof OperatorInputTableInfo) {
				dbTableSets.add((OperatorInputTableInfo) obj);
			}
		}
		return dbTableSets;
	}
	@Override
	public boolean equals(Object operator){
		if(operator!=null&&operator instanceof Operator){ 
			Operator that = (Operator)operator ;
			if(this.getOperModel().getUUID().equals(that.getOperModel().getUUID())){
				return true;
			}
		}
		
		return false;
		
	}
	
	protected void attachConn(XmlDocManager opTypeXmlManager, Node opNode,
			List<OperatorParameter> operatorParameterList) {
		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				opNode, "Parameter");
		
		Map<String,String> connMap=new HashMap<String,String>();
		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");
			String paraValue = null;
			if (paraName.equals("password")) {
				paraValue = XmlDocManager
						.decryptedPassword(((Element) parameterNode)
								.getAttribute("value"));
			} else {
				paraValue = ((Element) parameterNode).getAttribute("value");
			}
			connMap.put(paraName, paraValue);
		}
		OperatorParameter operatorParameter=null;
		for (OperatorParameter opParameter : operatorParameterList) {
			if (opParameter.getName().equals(OperatorParameter.NAME_dBConnectionName)) {
				operatorParameter=opParameter;
			}
		}
		if(operatorParameter==null)return;
		Object obj = operatorParameter.getValue();
		if(obj!=null){
			String connName=(String)obj;
			DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE
			.getManager();
			try {

				DbConnectionInfo connInfo=null;
				try {
					connInfo = dbManager.getDBConnection(userName, connName,
							resourceType);
				} catch (Exception e) {
					itsLogger.error(e.getMessage(),e);
				}
				if(connInfo==null||connInfo.getConnection()==null){
					String dbType=connMap.get("system");
					String url = connMap.get("url");
					IDataSourceInfo dataSourceInfo = DataSourceInfoFactory
					.createConnectionInfo(dbType);
					String[] urlArray = dataSourceInfo.deComposeUrl(url);
					DbConnectionInfo info=new DbConnectionInfo(userName,connName,resourceType);
					DbConnection connection=new DbConnection(
							dbType, urlArray[0], Integer.valueOf(urlArray[1]), urlArray[2], 
							connMap.get("userName"), 
							connMap.get("password"), 
							connMap.get("useSSL"));
					connection.setConnName(connName);
					info.setConnection(connection);
					dbManager.createDBConnection(info);
				}
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				return;
			}
		}
	}
	
	protected String isOutputTypeSameAsInputType(Operator parent) {
		List<String> sOutputList = parent.getOutputClassList();
		List<String> tInputList = this.getInputClassList();
		boolean isReady = false;
		if(sOutputList != null && tInputList != null){
		 	for(int i=0;i<sOutputList.size();i++){
				for(int j=0;j<tInputList.size();j++){
					if(sOutputList.get(i).equals(tInputList.get(j))){
						isReady = true;
					}
				}
			}
			if(!isReady){
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						parent.getToolTipTypeName(),this.getToolTipTypeName());
			}
		}
		return null;
	}
	
	protected String isInputDBTableInfoOK(Operator parent) {
		List<Object> outPuts = parent.getOperatorInputList();
		if(outPuts==null||outPuts.size()==0){
			return(parent.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
		}
 
		for (Iterator iterator = outPuts.iterator(); iterator.hasNext();) {
			Object obj = (Object) iterator.next();
			if (obj instanceof OperatorInputTableInfo) {
				
				OperatorInputTableInfo dbTableSet= (OperatorInputTableInfo) obj;
				if(StringUtil.isEmpty(dbTableSet.getSchema())
						||StringUtil.isEmpty(dbTableSet.getTable()) 
						){
					return(parent.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
 
				} 
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected void validateListNull(List<String> invalidParameterList,
			String paraName, List paraValue) {
		if ((paraValue==null||paraValue.size()==0)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}
	
	protected void validateAggregateModel(AggregateFieldsModel fieldModel,
			List<String> invalidParameterList, String paraName) {
		if (fieldModel == null  ) {
			invalidParameterList.add(paraName);
			return ;
		}
		List<AggregateField> aggFieldList = null;
		List<String> groupByList = null;
		if (fieldModel != null) {
			groupByList = fieldModel.getGroupByFieldList();
			aggFieldList = fieldModel.getAggregateFieldList();
		}

		if (groupByList == null || groupByList.size() == 0) {
			invalidParameterList.add(paraName);
			return;
		}
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		if ((aggFieldList == null || aggFieldList.size() == 0)
				) {
			invalidParameterList.add(paraName);
			return;
		}else if(this instanceof HadoopAggregateOperator){
			for(AggregateField  field :aggFieldList){
				String funcString = field.getAggregateExpression().substring(0,field.getAggregateExpression().indexOf("(")).trim();
				String columnName =  field.getAggregateExpression().substring(field.getAggregateExpression().indexOf("(")+1,field.getAggregateExpression().indexOf(")")).trim();
				 
				if(false==funcString.equalsIgnoreCase("COUNT")
						&&false == ParameterValidateUtility.validateNumberColumn(columnName,(HadoopOperator) this, paraName)){
					invalidParameterList.add(paraName);
					return;
				}else if(fieldList.contains(columnName) ==false){
					invalidParameterList.add(paraName);
					return;
				}
			}
		}
	
		validateGroupByField(fieldList,invalidParameterList,paraName,fieldModel);
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
	
	
	protected void storeAggregateFielsdModel(Document xmlDoc, Element element) {
		OperatorParameter fieldsModelParameter = ParameterUtility
				.getParameterByName(this,
						OperatorParameter.NAME_aggregateFieldList);
		Object value = fieldsModelParameter.getValue();
		if (!(value instanceof AggregateFieldsModel)) {
			return;
		}
		AggregateFieldsModel aggModel = (AggregateFieldsModel) value;
		element.appendChild(aggModel.toXMLElement(xmlDoc));
	}
	
	protected void readAggregateModel(XmlDocManager opTypeXmlManager, Node opNode) {
		ArrayList<Node> aggFieldNodeList = opTypeXmlManager.getNodeList(opNode,
				AggregateFieldsModel.TAG_NAME);
		if (aggFieldNodeList != null && aggFieldNodeList.size() > 0) {
			AggregateFieldsModel aggModel = AggregateFieldsModel
					.fromXMLElement((Element) aggFieldNodeList.get(0));
			getOperatorParameter(OperatorParameter.NAME_aggregateFieldList)
					.setValue(aggModel);
		}
	}
	//be careful , some hadoop operator will set to avoid the duplicate code
	@Override
	public boolean isVaild(VariableModel variableModel) {
		invalidParameters = ParameterValidateUtility.validate(this,variableModel) ;

		if(invalidParameters.length==0){
			return true;
		}else{
			return false;
		}	
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractOperator [workflow=");
		builder.append(workflow);
		builder.append(", userName=");
		builder.append(userName);
		builder.append(", resourceType=");
		builder.append(resourceType);
		builder.append(", locale=");
		builder.append(locale);
		builder.append(", operModel=");
		builder.append(operModel);
		builder.append(", parameterNames=");
		builder.append(parameterNames);
		builder.append(", operatorParameterList=");
		builder.append(operatorParameterList);
		builder.append("]");
		return builder.toString();
	}
	

	public   EngineModel loadModelFromFile(String modelFilePath)   {
		 XMLWorkFlowReader reader = new XMLWorkFlowReader();
		 
		try {
			OperatorWorkFlow	owf = reader .doRead(new XMLFileReaderParameters(modelFilePath,System.getProperty("user.name"),ResourceType.Personal),Locale.getDefault());
		
		 
			for(UIOperatorModel om:owf.getChildList()){
				return ((ModelOperator)om.getOperator()).getModel();
				 
			}
		} catch (Exception e) {
			//this . a pure object file
			 
			String objectStr="";
			try {
				objectStr = FileUtility.readFiletoString(new File(modelFilePath)).toString();
			} catch ( Exception e1) {
				itsLogger.error("Can not load model :"+modelFilePath );
				itsLogger.error( e1);
				return null ;
			} 
			Object modelObj = AlpineUtil.stringToObject(objectStr);
			if(modelObj instanceof EngineModel){
				return (EngineModel)modelObj;
			}else{
				itsLogger.error("Can not load model :"+modelFilePath);
				 
			}
		}
		return null;
	}
	
	protected String validateStoreResult(Operator precedingOperator) {
		if(precedingOperator instanceof SubFlowOperator){
			precedingOperator = ((SubFlowOperator)precedingOperator).getExitOperator();
		}
		if((precedingOperator instanceof HadoopDataOperationOperator)==true){
			OperatorParameter opPara = precedingOperator.getOperatorParameter(OperatorParameter.NAME_HD_StoreResults);
			if(opPara!=null){
				String opValue = (String)opPara.getValue();
				if(opValue.equals(Resources.FalseOpt)){
					return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK_HADOOP,locale),
							precedingOperator.getToolTipTypeName(), this
									.getToolTipTypeName());
				}
			}
		}
		return "";
	}
}
