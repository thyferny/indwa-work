/**
 * ClassName ProductRecommendationEvaluationOperator.java
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
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class ProductRecommendationEvaluationOperator extends AbstractOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
		
			OperatorParameter.NAME_Recommendataion_Table,
			OperatorParameter.NAME_Recommendataion_ID_Column,
			OperatorParameter.NAME_Recommendataion_Product_Column,
			
			OperatorParameter.NAME_Pre_Recommendataion_Table,
			OperatorParameter.NAME_Pre_Recommendataion_ID_Column,
			OperatorParameter.NAME_Pre_Recommendataion_Value_Column,
			
			OperatorParameter.NAME_Post_Recommendataion_Table,
			OperatorParameter.NAME_Post_Recommendataion_ID_Column,
			OperatorParameter.NAME_Post_Recommendataion_Product_Column,
			OperatorParameter.NAME_Post_Recommendataion_Value_Column
			
	});
	
	
	public ProductRecommendationEvaluationOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.PRODUCT_RECOMMONDATION_EVALUATION_OPERATOR,locale);
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
			if(paraName.equals(OperatorParameter.NAME_Recommendataion_Table)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateRecommendationTable(fieldMap,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Recommendataion_ID_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Recommendataion_Product_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Pre_Recommendataion_Table)){
				validateNull(invalidParameterList, paraName, paraValue);
				validatePreRecommendationTable(fieldMap,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Pre_Recommendataion_ID_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Pre_Recommendataion_Value_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Post_Recommendataion_Table)){
				validateNull(invalidParameterList, paraName, paraValue);
				validatePostRecommendationTable(fieldMap,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Post_Recommendataion_ID_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Post_Recommendataion_Product_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Post_Recommendataion_Value_Column)){
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
				}
			} else {
				Object value = parameter.getValue();
				if (value instanceof String) {
					createSimpleElements(xmlDoc, element, value, paraName,
							addSuffixToOutput);
				}
			}

		}
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
//				if(isDBSame(list)==true){TODO:
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
	public List<Object> getOperatorOutputList() {
		return null;
	}

	@Override
	public String validateInputLink(Operator precedingOperator) {		
		if(OperatorUtility.getParentList(getOperModel()).size()==3){
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
	
	private void validatePostRecommendationTable(
			Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {		
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_Post_Recommendataion_ID_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Post_Recommendataion_ID_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Post_Recommendataion_Product_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Post_Recommendataion_Product_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Post_Recommendataion_Value_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Post_Recommendataion_Value_Column);
				}
			}else{
				List<String> fieldList=fieldMap.get(paraValue);
				isPostRecommendataionColumnExists(fieldList,invalidParameterList);
			}
		}
	}


	private void validatePreRecommendationTable(
			Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {	
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_Pre_Recommendataion_ID_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Pre_Recommendataion_ID_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Pre_Recommendataion_Value_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Pre_Recommendataion_Value_Column);
				}
			}else{
				List<String> fieldList=fieldMap.get(paraValue);
				isPreRecommendataionColumnExists(fieldList,invalidParameterList);
			}
		}
	}


	private void validateRecommendationTable(Map<String, List<String>> fieldMap,List<String> invalidParameterList,String paraName,String paraValue) {
		if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)){
			if(!fieldMap.containsKey(paraValue)){
				invalidParameterList.add(paraName);
				if(!invalidParameterList.contains(OperatorParameter.NAME_Recommendataion_ID_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Recommendataion_ID_Column);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_Recommendataion_Product_Column)){
					invalidParameterList.add(OperatorParameter.NAME_Recommendataion_Product_Column);
				}
			}else{
				List<String> fieldList=fieldMap.get(paraValue);
				isRecommendataionColumnsExists(fieldList,invalidParameterList);
			}
		}
	}


	private void isPostRecommendataionColumnExists(List<String> fieldList,List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_Post_Recommendataion_ID_Column);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Post_Recommendataion_ID_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Post_Recommendataion_ID_Column);
		}

		OperatorParameter productParameter=getOperatorParameter(OperatorParameter.NAME_Post_Recommendataion_Product_Column);
		if(productParameter.getValue()!=null&&!fieldList.contains(productParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Post_Recommendataion_Product_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Post_Recommendataion_Product_Column);
		}
		
		OperatorParameter valueParameter=getOperatorParameter(OperatorParameter.NAME_Post_Recommendataion_Value_Column);
		if(valueParameter.getValue()!=null&&!fieldList.contains(valueParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Post_Recommendataion_Value_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Post_Recommendataion_Value_Column);
		}
	}


	private void isPreRecommendataionColumnExists(List<String> fieldList, List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_Pre_Recommendataion_ID_Column);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Pre_Recommendataion_ID_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Pre_Recommendataion_ID_Column);
		}

		OperatorParameter valueParameter=getOperatorParameter(OperatorParameter.NAME_Pre_Recommendataion_Value_Column);
		if(valueParameter.getValue()!=null&&!fieldList.contains(valueParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Pre_Recommendataion_Value_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Pre_Recommendataion_Value_Column);
		}
	}


	private void isRecommendataionColumnsExists(List<String> fieldList, List<String> invalidParameterList) {
		OperatorParameter idParameter=getOperatorParameter(OperatorParameter.NAME_Recommendataion_ID_Column);
		if(idParameter.getValue()!=null&&!fieldList.contains(idParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Recommendataion_ID_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Recommendataion_ID_Column);
		}

		OperatorParameter productParameter=getOperatorParameter(OperatorParameter.NAME_Recommendataion_Product_Column);
		if(productParameter.getValue()!=null&&!fieldList.contains(productParameter.getValue())
				&&!invalidParameterList.contains(OperatorParameter.NAME_Recommendataion_Product_Column)){
			invalidParameterList.add(OperatorParameter.NAME_Recommendataion_Product_Column);
		}
	}
	
	
}
