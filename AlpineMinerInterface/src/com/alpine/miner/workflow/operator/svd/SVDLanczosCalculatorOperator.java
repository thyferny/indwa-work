/**
 * ClassName SVDPredictOperator
 *
 * Version information: 1.00
 *
 * Data: 2011-6-20
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.svd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.storageparam.StorageParameterModel;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class SVDLanczosCalculatorOperator extends AbstractOperator {

	public static final List<String> parameterNames = Arrays
			.asList(new String[] { OperatorParameter.NAME_UmatrixFullTable,
					OperatorParameter.NAME_RowNameF,
					OperatorParameter.NAME_UfeatureColumn,
					OperatorParameter.NAME_UdependentColumn,
					OperatorParameter.NAME_VmatrixFullTable,
					
					OperatorParameter.NAME_ColNameF,
					OperatorParameter.NAME_VfeatureColumn,
					OperatorParameter.NAME_VdependentColumn,
					OperatorParameter.NAME_SmatrixFullTable,
					OperatorParameter.NAME_SfeatureColumn,
					OperatorParameter.NAME_SdependentColumn,
					OperatorParameter.NAME_CrossProduct,
					OperatorParameter.NAME_KeyColumn,
					OperatorParameter.NAME_KeyValue,
					OperatorParameter.NAME_outputSchema,
					OperatorParameter.NAME_outputTable,
					OperatorParameter.NAME_outputTable_StorageParams,
					OperatorParameter.NAME_dropIfExist });

	public SVDLanczosCalculatorOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addInputClass(EngineModel.MPDE_TYPE_SVD);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SVD_CALCULATOR,locale);
	}

	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		List<OperatorInputTableInfo> dbSetList = new ArrayList<OperatorInputTableInfo>();
		if (list != null) {
			for (Object obj : list) {
				if (obj instanceof EngineModel) {
					modelList.add((EngineModel) obj);
				} else if (obj instanceof OperatorInputTableInfo) {
					dbSetList.add((OperatorInputTableInfo) obj);
				}
			}
		}
		if (modelList.size() == 1) {
			return true;
		} else if (dbSetList.size() >= 2) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		boolean isFromSVD=isFromSVD();
		Map<String, List<String>> fieldMap=null;
		if(!isFromSVD){
			fieldMap = OperatorUtility.getFieldMap(this);
		}
		Operator parentOperator=null;
		List<UIOperatorModel> opModels = OperatorUtility
				.getParentList(getOperModel());
		for (UIOperatorModel opModel : opModels) {
			if (opModel.getOperator() instanceof SVDLanczosOperator) {
				parentOperator=opModel.getOperator();
				break;
			}
		}
		String parentRowName=null;
		String parentColName=null;
		String dependentColumn=null;
		List<String> fieldList =new ArrayList<String>();
		List<String> keyColumns=new ArrayList<String>();
		
		String uMatrixSchemaName=null;
		String uMatrixTableName=null;
		String parentUName=null;
		
		String vMatrixSchemaName=null;
		String vMatrixTableName=null;
		String parentVName=null;
		
		String sMatrixSchemaName=null;
		String sMatrixTableName=null;
		String parentSName=null;
		
		if(parentOperator!=null){
			parentRowName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_RowName).getValue();
			parentColName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_ColName).getValue();
			dependentColumn=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_dependentColumn).getValue();
			fieldList = OperatorUtility.getAvailableColumnsList(parentOperator,
					false);
			if(!StringUtil.isEmpty(parentRowName)){
				keyColumns.add(parentRowName);
			}
			if(!StringUtil.isEmpty(parentColName)){
				keyColumns.add(parentColName);
			}
			if(!StringUtil.isEmpty(dependentColumn)){
				keyColumns.add(dependentColumn);
			}
			
			uMatrixSchemaName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_UmatrixSchema).getValue();
			uMatrixTableName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_UmatrixTable).getValue();
			parentUName=StringHandler.combinTableName(uMatrixSchemaName, uMatrixTableName);
			
			vMatrixSchemaName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_VmatrixSchema).getValue();
			vMatrixTableName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_VmatrixTable).getValue();
			parentVName=StringHandler.combinTableName(vMatrixSchemaName, vMatrixTableName);
			
			sMatrixSchemaName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_singularValueSchema).getValue();
			sMatrixTableName=(String)parentOperator.getOperatorParameter(OperatorParameter.NAME_singularValueTable).getValue();
			parentSName=StringHandler.combinTableName(sMatrixSchemaName, sMatrixTableName);
		}
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		String crossProduct = null;

		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			if (paraName.equals(OperatorParameter.NAME_CrossProduct)) {
				crossProduct = (String) para.getValue();
			}
		}
		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue = (String) para.getValue();
			if (paraName.equals(OperatorParameter.NAME_UmatrixFullTable)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)&&isFromSVD){
					if(!parentUName.equals(paraValue)){
						invalidParameterList.add(paraName);
					}
				}else{
					validateUTable(fieldMap,invalidParameterList, paraName, paraValue);
				}
			} else if (paraName.equals(OperatorParameter.NAME_RowNameF)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(isFromSVD){
					validateContainColumns(fieldList,invalidParameterList, paraName, parentRowName);
				}		
			} else if (paraName.equals(OperatorParameter.NAME_UfeatureColumn)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_UdependentColumn)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(isFromSVD){
					validateContainColumns(fieldList,invalidParameterList, paraName, dependentColumn);
				}
			} else if (paraName.equals(OperatorParameter.NAME_VmatrixFullTable)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)&&isFromSVD){
					if(!parentVName.equals(paraValue)){
						invalidParameterList.add(paraName);
					}
				}else{
					validateVTable(fieldMap,invalidParameterList, paraName, paraValue);
				}
			} else if (paraName.equals(OperatorParameter.NAME_ColNameF)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(isFromSVD){
					validateContainColumns(fieldList,invalidParameterList, paraName, parentColName);
				}
			} else if (paraName.equals(OperatorParameter.NAME_VfeatureColumn)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_VdependentColumn)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(isFromSVD){
					validateContainColumns(fieldList,invalidParameterList, paraName, dependentColumn);
				}
			} else if (paraName.equals(OperatorParameter.NAME_SmatrixFullTable)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)&&isFromSVD){
					if(!parentSName.equals(paraValue)){
						invalidParameterList.add(paraName);
					}
				}else{
					validateSTable(fieldMap,invalidParameterList, paraName, paraValue);
				}
			} else if (paraName.equals(OperatorParameter.NAME_SfeatureColumn)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_SdependentColumn)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(isFromSVD){
					validateContainColumns(fieldList,invalidParameterList, paraName, dependentColumn);
				}
			} else if (paraName.equals(OperatorParameter.NAME_CrossProduct)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_KeyColumn)) {
				if (StringUtil.isEmpty(crossProduct)
						|| crossProduct
								.equals(com.alpine.utility.db.Resources.FalseOpt)) {
					validateNull(invalidParameterList, paraName, paraValue);
				}
				if(isFromSVD){
					validateContainColumns(keyColumns,invalidParameterList, paraName, paraValue);
				}	
			} else if (paraName.equals(OperatorParameter.NAME_KeyValue)) {
				if (StringUtil.isEmpty(crossProduct)
						|| crossProduct
								.equals(com.alpine.utility.db.Resources.FalseOpt)) {
					validateNull(invalidParameterList, paraName, paraValue);
				}
			} else if (paraName.equals(OperatorParameter.NAME_outputSchema)) {
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			} else if (paraName.equals(OperatorParameter.NAME_outputTable)) {
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			} else if (paraName.equals(OperatorParameter.NAME_dropIfExist)) {
				validateNull(invalidParameterList, paraName, paraValue);
			}
		}
		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		List<OperatorParameter> parameterList = getOperatorParameterList();
		Iterator<OperatorParameter> iter_para = parameterList.iterator();
		while (iter_para.hasNext()) {
			OperatorParameter parameter = iter_para.next();
			String paraName = parameter.getName();
			if (addSuffixToOutput
					&& XmlDocManager.OUTPUTTABLElIST.contains(paraName)) {
				Object value = parameter.getValue();
				if (value instanceof String) {
					String tableName = (String) value;
					boolean isFromDbTable = false;
					List<UIOperatorModel> parentList = OperatorUtility
							.getParentList(getOperModel());
					for (UIOperatorModel operatorModel : parentList) {
						if (operatorModel.getOperator() instanceof DbTableOperator) {
							String parentSchemaName = (String) operatorModel
									.getOperator().getOperatorParameter(
											OperatorParameter.NAME_schemaName)
									.getValue();
							String parentTablename = (String) operatorModel
									.getOperator().getOperatorParameter(
											OperatorParameter.NAME_tableName)
									.getValue();
							parentTablename = StringHandler
									.doubleQ(parentSchemaName)
									+ "."
									+ StringHandler.doubleQ(parentTablename);
							if (tableName.equals(parentTablename)) {
								isFromDbTable = true;
								break;
							}
						}
						Element parameter_element = xmlDoc
								.createElement("Parameter");
						parameter_element.setAttribute("key", paraName);
						if (isFromDbTable) {
							parameter_element.setAttribute("value", tableName);
						} else {
							if (paraName.equals(XmlDocManager.OUTPUT_TABLE)) {
								String newTable = StringHandler.addPrefix(
										tableName, userName);
								parameter_element.setAttribute("value",
										newTable);
							} else {
								String[] temp = tableName.split("\\.", 2);
								String newTable = temp[0]
										+ "."
										+ StringHandler
												.doubleQ(StringHandler
														.addPrefix(
																StringHandler
																		.removeDoubleQ(temp[1]),
																userName));
								parameter_element.setAttribute("value",
										newTable);
							}

						}
						element.appendChild(parameter_element);
					}
				}else if (value instanceof StorageParameterModel){
					
					StorageParameterModel model = (StorageParameterModel)ParameterUtility.getParameterByName(this,paraName).getValue();
					Element ele = model.toXMLElement(xmlDoc,paraName); 
	 
					element.appendChild(ele);
					
				}
			} else {
				Object value = parameter.getValue();
				if (value instanceof String) {
					createSimpleElements(xmlDoc, element, value, paraName,
							addSuffixToOutput);
				}else if (value instanceof StorageParameterModel){
					
					StorageParameterModel model = (StorageParameterModel)ParameterUtility.getParameterByName(this,paraName).getValue();
					Element ele = model.toXMLElement(xmlDoc,paraName); 
	 
					element.appendChild(ele);
					
				}
			}

		}
	}

	@Override
	public List<Object> getOperatorOutputList() {
		 
		return null;
	}

	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;

	}

	@Override
	public String validateInputLink(Operator precedingOperator) {
		/**
		 * check inputClass and outputClass is equals
		 */
		List<String> sOutputList = precedingOperator.getOutputClassList();
		List<String> tInputList = this.getInputClassList();
		boolean isReady = false;
		if (sOutputList == null || tInputList == null) {
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
					precedingOperator.getToolTipTypeName(), this
							.getToolTipTypeName());

		}
		for (int i = 0; i < sOutputList.size(); i++) {
			for (int j = 0; j < tInputList.size(); j++) {
				if (sOutputList.get(i).equals(tInputList.get(j))) {
					isReady = true;
				}
			}
		}
		if (!isReady) {
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
					precedingOperator.getToolTipTypeName(), this
							.getToolTipTypeName());
		}

		/**
		 * check repick linked
		 */
		if (precedingOperator.getOperModel().containTarget(getOperModel())) {
			return LanguagePack.getMessage(LanguagePack.MESSAGE_ALREADY_LINK,locale);
		}

		if (OperatorUtility.getParentList(getOperModel()).size() == 1) {
			UIOperatorModel model = OperatorUtility.getParentList(
					getOperModel()).get(0);
			if (model.getOperator() instanceof SVDLanczosOperator) {
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.SVDC_CHECK_LINK_SVD_EXISTS,locale),
						LanguagePack.getMessage(LanguagePack.SVD_CALCULATOR,locale), precedingOperator
								.getToolTipTypeName());

			} else if (model.getOperator() instanceof DbTableOperator
					&& precedingOperator instanceof SVDLanczosOperator) {
				return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.SVDC_CHECK_LINK_SVD_EXISTS,locale),
						LanguagePack.getMessage(LanguagePack.DBTABLE_OPERATOR,locale), precedingOperator
								.getToolTipTypeName());
			}
		} else if (OperatorUtility.getParentList(getOperModel()).size() > 1
				&& precedingOperator instanceof SVDLanczosOperator) {
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.SVDC_CHECK_LINK_SVD_EXISTS,locale),
					LanguagePack.getMessage(LanguagePack.DBTABLE_OPERATOR,locale), precedingOperator
							.getToolTipTypeName());
		}

		return "";
	}

	private void validateSTable(Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_SfeatureColumn)){
					invalidParameterList.add(OperatorParameter.NAME_SfeatureColumn);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_SdependentColumn)){
					invalidParameterList.add(OperatorParameter.NAME_SdependentColumn);
				}
			} else {
				List<String> fieldList = fieldMap
						.get(paraValue);
				ifsMatrixColumnsExists(fieldList,invalidParameterList);
			}
		}
	}

	private void validateVTable(Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_ColNameF)){
					invalidParameterList.add(OperatorParameter.NAME_ColNameF);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_KeyColumn)){
					invalidParameterList.add(OperatorParameter.NAME_KeyColumn);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_VfeatureColumn)){
					invalidParameterList.add(OperatorParameter.NAME_VfeatureColumn);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_VdependentColumn)){
					invalidParameterList.add(OperatorParameter.NAME_VdependentColumn);
				}
			} else {
				List<String> fieldList = fieldMap
						.get(paraValue);
				ifvMatrixColumnsExists(fieldList,invalidParameterList);
			}
		}
	}

	private void validateUTable(Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_RowNameF)){
					invalidParameterList.add(OperatorParameter.NAME_RowNameF);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_KeyColumn)){
					invalidParameterList.add(OperatorParameter.NAME_KeyColumn);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_UfeatureColumn)){
					invalidParameterList.add(OperatorParameter.NAME_UfeatureColumn);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_UdependentColumn)){
					invalidParameterList.add(OperatorParameter.NAME_UdependentColumn);
				}
			} else {
				List<String> fieldList = fieldMap
						.get(paraValue);
				ifuMatrixColumnsExists(fieldList,invalidParameterList);
			}
		}
	}

	private boolean isFromSVD() {
		boolean isFromSVD = false;
		List<UIOperatorModel> opModels = OperatorUtility
				.getParentList(getOperModel());
		for (UIOperatorModel opModel : opModels) {
			if (opModel.getOperator() instanceof SVDLanczosOperator) {
				isFromSVD = true;
				break;
			}
		}
		return isFromSVD;
	}

	private void ifsMatrixColumnsExists(List<String> fieldList,List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_SfeatureColumn);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_SfeatureColumn)){
			invalidParameterList.add(OperatorParameter.NAME_SfeatureColumn);
		}
		OperatorParameter dependentColumnParameter=getOperatorParameter(OperatorParameter.NAME_SdependentColumn);
		if(dependentColumnParameter.getValue()!=null&&!fieldList.contains(dependentColumnParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_SdependentColumn)){
			invalidParameterList.add(OperatorParameter.NAME_SdependentColumn);
		}
	}

	private void ifvMatrixColumnsExists(List<String> fieldList, List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_ColNameF);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_ColNameF)){
			invalidParameterList.add(OperatorParameter.NAME_ColNameF);
		}
		
		OperatorParameter keyParameter=getOperatorParameter(OperatorParameter.NAME_KeyColumn);
		if(keyParameter.getValue()!=null&&!fieldList.contains(keyParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_KeyColumn)){
			invalidParameterList.add(OperatorParameter.NAME_KeyColumn);
		}


		OperatorParameter feautreParameter = getOperatorParameter(OperatorParameter.NAME_VfeatureColumn);
		if(feautreParameter.getValue()!=null&&!fieldList.contains(feautreParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_VfeatureColumn)){
			invalidParameterList.add(OperatorParameter.NAME_VfeatureColumn);
		}

		OperatorParameter dependentColumnParameter = getOperatorParameter(OperatorParameter.NAME_VdependentColumn);
		if(dependentColumnParameter.getValue()!=null&&!fieldList.contains(dependentColumnParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_VdependentColumn)){
			invalidParameterList.add(OperatorParameter.NAME_VdependentColumn);
		}
	}

	private void ifuMatrixColumnsExists(List<String> fieldList, List<String> invalidParameterList) {
		OperatorParameter rowParameter = getOperatorParameter(OperatorParameter.NAME_RowNameF);
		if(rowParameter.getValue()!=null&&!fieldList.contains(rowParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_RowNameF)){
			invalidParameterList.add(OperatorParameter.NAME_RowNameF);
		}

		OperatorParameter keyParameter = getOperatorParameter(OperatorParameter.NAME_KeyColumn);
		if(keyParameter.getValue()!=null&&!fieldList.contains(keyParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_KeyColumn)){
			invalidParameterList.add(OperatorParameter.NAME_KeyColumn);
		}

		OperatorParameter feautreParameter = getOperatorParameter(OperatorParameter.NAME_UfeatureColumn);
		if(feautreParameter.getValue()!=null&&!fieldList.contains(feautreParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_UfeatureColumn)){
			invalidParameterList.add(OperatorParameter.NAME_UfeatureColumn);
		}

		OperatorParameter dependentColumnParameter = getOperatorParameter(OperatorParameter.NAME_UdependentColumn);
		if(dependentColumnParameter.getValue()!=null&&!fieldList.contains(dependentColumnParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_UdependentColumn)){
			invalidParameterList.add(OperatorParameter.NAME_UdependentColumn);
		}
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_CrossProduct)) {
			return Resources.FalseOpt;
		} else {
			return super.getOperatorParameterDefaultValue(paraName);
		}

	}
	
	protected void validateContainColumns(boolean isFromSVD,List<String> fieldList, List<String> invalidParameterList, String paraName, String paraValue) {
		if (!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)) {
			if (!fieldList.contains(paraValue)) {
				invalidParameterList.add(paraName);
			} 
		}
	}

}
