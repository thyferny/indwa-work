/**
 * ClassName ProductRecommendationOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-3-28
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class ProductRecommendationOperator extends AbstractOperator {
	
	private static final String RANK = "rank";
	private static final String PROD_ID = "prod_id";
	private static final String SCORE = "score";
	private static final String D_COHORTS = "1:-Infinity:0;2:0:Infinity";
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_Customer_Table_Name,
			OperatorParameter.NAME_Customer_ID_Column,
			OperatorParameter.NAME_Customer_Value_Column,
			OperatorParameter.NAME_Customer_Product_Column,
			OperatorParameter.NAME_Customer_Product_Count_Column,
			OperatorParameter.NAME_Selection_Table_Name,
			OperatorParameter.NAME_Selection_ID_Column,
			OperatorParameter.NAME_SimThreshold,
			OperatorParameter.NAME_Max_Record,
			OperatorParameter.NAME_Min_Product_Count,
			OperatorParameter.NAME_Score_Threshold,
			OperatorParameter.NAME_Cohorts,
			OperatorParameter.NAME_Above_Cohort ,
			OperatorParameter.NAME_Below_Cohort,
			OperatorParameter.NAME_TargetCohorts,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			
	});
	
	public ProductRecommendationOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.PRODUCT_RECOMMONDATION_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		Map<String, List<String>> fieldMap = OperatorUtility.getFieldMap(this);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();

		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
		
			if(paraName.equals(OperatorParameter.NAME_Customer_Table_Name)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateCustomerTable(fieldMap,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Customer_ID_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Customer_Value_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Customer_Product_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Selection_Table_Name)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSelectionTable(fieldMap,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Selection_ID_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_SimThreshold)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Max_Record)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_Min_Product_Count)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_Score_Threshold)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, false, Double.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_TargetCohorts)){
				if(!StringUtil.isEmpty(paraValue)){
					validateNumber(invalidParameterList, paraName, paraValue, 0, false, Double.MAX_VALUE, true,variableModel);
				}
			}else if(paraName.equals(OperatorParameter.NAME_Cohorts)){
				validateNull(invalidParameterList, paraName, paraValue);
				if(!StringUtil.isEmpty(paraValue)){
					if(paraValue.equals(D_COHORTS)){
						invalidParameterList.add(paraName);
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_Above_Cohort)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_Below_Cohort)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}		
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
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
		List<Object> operatorInputList = new ArrayList<Object>();
		
		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				
				String idColumn=(String)getOperatorParameter(OperatorParameter.NAME_Customer_ID_Column).getValue();
				String proColumn=(String)getOperatorParameter(OperatorParameter.NAME_Customer_Product_Column).getValue();
				List<String[]> newFieldColumns = new ArrayList<String[]>();
				List<String[]> fieldColumns = operatorInputTableInfo.getFieldColumns();
				for(String[] fieldColumn:fieldColumns){
					if(fieldColumn[0].equals(idColumn)){
						String[] newFieldColumn=new String[]{idColumn,fieldColumn[1]};
						newFieldColumns.add(newFieldColumn);
					}else if(fieldColumn[0].equals(proColumn)){
						String[] newFieldColumn=new String[]{proColumn,fieldColumn[1]};
						newFieldColumns.add(newFieldColumn);
					}
				}
				newFieldColumns.add(new String[]{RANK,"BIGINT"});
				newFieldColumns.add(new String[]{PROD_ID,"BIGINT"});
				newFieldColumns.add(new String[]{SCORE,"DOUBLE PRECISION"});
				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo.setTableType((String)getOperatorParameter(OperatorParameter.NAME_outputType).getValue());
				
				operatorInputList.add(operatorInputTableInfo);
				break;
			}
		}	
		return operatorInputList;
	}

	@Override
	public boolean isInputObjectsReady() {
		List<Object> inputObjectList = getOperatorInputList();
		List<OperatorInputTableInfo> list = new ArrayList<OperatorInputTableInfo>();
		if (inputObjectList != null) {
			for (Object obj : inputObjectList) {
				if(obj instanceof OperatorInputTableInfo){
					list.add((OperatorInputTableInfo)obj);
				}
			}
			if(list.size()>1){
//				if(isDBSame(list)==true){
					return true;
//				}
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	@Override
	public String validateInputLink(Operator precedingOperator) {
		if(OperatorUtility.getParentList(getOperModel()).size()==2){
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
					precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
		}
		List<String> sOutputList = precedingOperator.getOutputClassList();
		List<String> tInputList = this.getInputClassList();
		boolean isReady = false;
		if(sOutputList == null || tInputList == null){
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
					precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
		}
		for(int i=0;i<sOutputList.size();i++){
			for(int j=0;j<tInputList.size();j++){
				if(sOutputList.get(i).equals(tInputList.get(j))){
					isReady = true;
				}
			}
		}
		if(!isReady){
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
					precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
		}
		
		/**
		 * check inputClass and outputClass is equals
		 */
		
		List<Object> outPuts = precedingOperator.getOperatorInputList();
		if(outPuts==null||outPuts.size()==0){
			return(precedingOperator.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
		}
	 
		for (Iterator iterator = outPuts.iterator(); iterator.hasNext();) {
			Object obj = (Object) iterator.next();
			if (obj instanceof OperatorInputTableInfo) {
				
				OperatorInputTableInfo dbTableSet= (OperatorInputTableInfo) obj;
				if(dbTableSet.getSchema()==null
						||dbTableSet.getSchema().trim().length()==0
						||dbTableSet.getTable()==null
						||dbTableSet.getTable().trim().length()==0
						){
					return(precedingOperator.getOperModel().getId()+LanguagePack.getMessage(LanguagePack.ERROR_Configure_Operator,locale));
 
				} 
			}
		}
	 	
 	
		/**
		 * check repick linked
		 */
		if(precedingOperator.getOperModel().containTarget(getOperModel())){
			return LanguagePack.getMessage(LanguagePack.MESSAGE_ALREADY_LINK,locale);
		}
		return "";
	}

	private void validateSelectionTable(Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {	
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_Selection_ID_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Selection_ID_Column);
				}
			}else{
				List<String> fieldList=fieldMap.get(paraValue);
				isSelectionColumnExists(fieldList,invalidParameterList);
			}
		}
	}

	private void validateCustomerTable(Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {	
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_Customer_ID_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Customer_ID_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Customer_Value_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Customer_Value_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Customer_Product_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Customer_Product_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Customer_Product_Count_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Customer_Product_Count_Column);
				}
			}else{
				List<String> fieldList=fieldMap.get(paraValue);
				ifCustomerColumnsExists(fieldList,invalidParameterList);
			}
		}
	}

	private void isSelectionColumnExists(List<String> fieldList,List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_Selection_ID_Column);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Selection_ID_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Selection_ID_Column);
		}
	}

	private void ifCustomerColumnsExists(List<String> fieldList, List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_Customer_ID_Column);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Customer_ID_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Customer_ID_Column);
		}

		OperatorParameter productParameter=getOperatorParameter(OperatorParameter.NAME_Customer_Product_Column);
		if(productParameter.getValue()!=null&&!fieldList.contains(productParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Customer_Product_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Customer_Product_Column);
		}
		
		OperatorParameter productCountParameter=getOperatorParameter(OperatorParameter.NAME_Customer_Product_Count_Column);
		if(productCountParameter.getValue()!=null&&!fieldList.contains(productCountParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Customer_Product_Count_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Customer_Product_Count_Column);
		}
		
		OperatorParameter valueParameter=getOperatorParameter(OperatorParameter.NAME_Customer_Value_Column);
		if(valueParameter.getValue()!=null&&!fieldList.contains(valueParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Customer_Value_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Customer_Value_Column);
		}
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_SimThreshold)){
			return "0.9";
		}else if(paraName.equals(OperatorParameter.NAME_Max_Record)){
			return "10";
		}else if(paraName.equals(OperatorParameter.NAME_Min_Product_Count)){
			return "10";
		}else if(paraName.equals(OperatorParameter.NAME_Score_Threshold)){
			return "1";
		}else if(paraName.equals(OperatorParameter.NAME_Above_Cohort)){
			return "1";
		}else if(paraName.equals(OperatorParameter.NAME_Below_Cohort)){
			return "1";
		}else if(paraName.equals(OperatorParameter.NAME_Cohorts)){
			return D_COHORTS;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
	
}
