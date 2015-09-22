/**
 * ClassName CustomizedOperatorModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-28
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.customized;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.utility.file.MD5Util;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;


public class CustomizedOperatorModel implements Serializable   {
    private static final Logger itsLogger = Logger.getLogger(CustomizedOperatorModel.class);
    private static String[] outputColumnType={"number","integer","text","date","array"};
	private static ArrayList<String> outputColumnTypeArray=new ArrayList<String>();
	static{
		for(int i=0;i<outputColumnType.length;i++){
			outputColumnTypeArray.add(outputColumnType[i]);
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 303807214947188314L;
	String operatorName;
	String udfSchema;
	String udfName;
	HashMap<String,ParameterModel> paraMap;
	HashMap<String,String> outputMap;
	HashMap<String,String> outputColumnMap;
	public CustomizedOperatorModel(String operatorName, String udfSchema,
			String udfName, HashMap<String, ParameterModel> paraMap,HashMap<String, String> outputColumnMap) {
		this.operatorName = operatorName;
		this.udfSchema = udfSchema;
		this.udfName = udfName;
		this.paraMap = paraMap;
		this.outputColumnMap=outputColumnMap;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getUdfSchema() {
		return udfSchema;
	}
	public void setUdfSchema(String udfSchema) {
		this.udfSchema = udfSchema;
	}
	public String getUdfName() {
		return udfName;
	}
	public void setUdfName(String udfName) {
		this.udfName = udfName;
	}
	public HashMap<String, ParameterModel> getParaMap() {
		return paraMap;
	}
	public void setParaMap(HashMap<String, ParameterModel> paraMap) {
		this.paraMap = paraMap;
	}
	public HashMap<String, String> getOutputMap() {
		return outputMap;
	}
	public void setOutputMap(HashMap<String, String> outputMap) {
		this.outputMap = outputMap;
	}
	public HashMap<String, String> getOutputColumnMap() {
		return outputColumnMap;
	}
	public void setOutputColumnMap(HashMap<String, String> outputColumnMap) {
		this.outputColumnMap = outputColumnMap;
	}
	
	//TODO:element is a oprateer
	public static CustomizedOperatorModel fromXMLElement(Element element,String filePath) throws Exception{
		Locale locale = Locale.getDefault();
		String locLang=locale.getLanguage()+"_"+locale.getCountry();
		
		NodeList udfNodeList = element.getElementsByTagName(COUtility.UDF);
		if(udfNodeList==null||udfNodeList.getLength()==0){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"udfNodeList is null "+"\n");
			throw new CustomizedException("udfNodeList is null",CustomizedException.UDF_NODE_NULL);
		}
	 
		String udfSchema=null;
		String udfName=null;
		for(int i=0;i<udfNodeList.getLength();){
			udfSchema=((Element)udfNodeList.item(i)).getAttribute(COUtility.UDF_SCHEMA);
			udfName=((Element)udfNodeList.item(i)).getAttribute(COUtility.UDF_NAME);
			break;
		}
		if(StringUtil.isEmpty(udfName)){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"udfName is null "+"\n");
			throw new CustomizedException("udfName is null",CustomizedException.UDF_NAME_NULL);
		}
		NodeList operatorNameNodeList = element.getElementsByTagName(COUtility.OPERATOR_NAME);
		if(operatorNameNodeList==null||operatorNameNodeList.getLength()==0){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"operatorNameNodeList is null "+"\n");
			throw new CustomizedException("operatorNameNodeList is null",CustomizedException.OPERATOR_NAME_NULL);
		}
		
		String operatorName=null;
		HashMap<String,String> operatorNameMap=new HashMap<String,String>();
		for(int i=0;i<operatorNameNodeList.getLength();i++){
			String lang = ((Element)operatorNameNodeList.item(i)).getAttribute(COUtility.LANG);
			operatorName= operatorNameNodeList.item(i).getFirstChild().getNodeValue();
			if(!StringUtil.isEmpty(lang)){
				operatorNameMap.put(lang, operatorName);
			}else{
				operatorNameMap.put("en", operatorName);
				operatorNameMap.put("en_US", operatorName);
				operatorNameMap.put("zh_CN", operatorName);
			}
		}
		
		operatorName=operatorNameMap.get(locLang);
		
		String md5=MD5Util.md5(new File(filePath));
		
		String distinctOperatorName=operatorName+"_"+md5;
		
		if(isUdfNameExists(operatorName)){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"Udf Name Exists "+"\n");
			throw new CustomizedException(" Udf name '"+operatorName+"' already existed!",CustomizedException.UDF_ALREADY_EXISTS);
		}
		
		NodeList outputNodeList = element.getElementsByTagName(COUtility.OUTPUT);
		if(outputNodeList==null||outputNodeList.getLength()==0){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"outputNodeList is null "+"\n");
			throw new CustomizedException("outputNodeList is null",CustomizedException.OUTPUT_NODE_NULL);
		}
		
		Element outputNode=(Element)outputNodeList.item(0);
		NodeList outputColumnList = outputNode.getElementsByTagName(COUtility.COLUMN);
		 
		if(outputColumnList==null||outputColumnList.getLength()==0){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"outputColumnList is null "+"\n");
			throw new CustomizedException("outputColumnList is null",CustomizedException.OUTPUT_COLUMN_NULL);
		}
		ArrayList<String> outNameValidationList=new ArrayList<String>();
		HashMap<String,String> outputColumnMap=new HashMap<String,String>();
		
		for(int i=0;i<outputColumnList.getLength();i++){
			Element paraNode=((Element)outputColumnList.item(i));
			String columnName = paraNode.getAttribute(COUtility.COLUMN_NAME);
			if(StringUtil.isEmpty(columnName)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"columnName is null"+"\n");
				throw new CustomizedException("columnName is null",CustomizedException.COLUMN_NAME_NULL);
			}
			if(outNameValidationList.contains(columnName)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"outNameValidationList is null"+"\n");
				throw new CustomizedException("outNameValidationList is null",CustomizedException.OUTPUT_NAME_VALIDATION_NULL);
			}else{
				outNameValidationList.add(columnName);
			}			
			String columnType =  paraNode .getAttribute(COUtility.COLUMN_TYPE);
			if(StringUtil.isEmpty(columnType)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"columnType is null"+"\n");
				throw new CustomizedException("columnType is null",CustomizedException.COLUMN_TYPE_NULL);
			}
			if(!outputColumnTypeArray.contains(columnType)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"columnType is invalid"+"\n");
				throw new CustomizedException("columnType is null",CustomizedException.COLUMN_TYPE_INVALID);
			}
			outputColumnMap.put(columnName, columnType);
		}
		
		HashMap<String,ParameterModel> paraMap=new HashMap<String,ParameterModel>();
		NodeList parasNodeList = element.getElementsByTagName(COUtility.PARAMETERS);
		if(parasNodeList==null||parasNodeList.getLength()==0){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"parasNodeList is null"+"\n");
			throw new CustomizedException("parasNodeList is null",CustomizedException.PARA_NODE_NULL);
		}
		Element parasNode=(Element)parasNodeList.item(0);
		NodeList paraNodeList = parasNode.getElementsByTagName(COUtility.PARAMETER);
		if(paraNodeList==null||paraNodeList.getLength()==0){
			itsLogger.error(CustomizedOperatorModel.class.getName()+"paraNodeList is null"+"\n");
			throw new CustomizedException("paraNodeList is null",CustomizedException.PARA_NODE_NULL);
		}
		ArrayList<String> positionValidationList=new ArrayList<String>();
		ArrayList<String> nameValidationList=new ArrayList<String>();
		for(int i=0;i<paraNodeList.getLength();i++){
			Element paraNode =(Element) paraNodeList.item(i);
			String paraName = ( paraNode).getAttribute(COUtility.PARA_NAME);
			String position = ( paraNode).getAttribute(COUtility.PARA_POSITION);
			String defaultValue =( paraNode).getAttribute(COUtility.PARA_DEFAULT_VALUE);
			String dataType =( paraNode).getAttribute(COUtility.PARA_DATA_TYPE);
			String optionalValue =( paraNode).getAttribute(COUtility.PARA_OPTION_VALUE);
			if(StringUtil.isEmpty(paraName)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"paraName is null"+"\n");
				throw new CustomizedException("paraName is null",CustomizedException.PARA_NAME_NULL);
			}
			if(StringUtil.isEmpty(position)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"position is null"+"\n");
				throw new CustomizedException("position is null",CustomizedException.POSITION_NULL);
			}
			if(nameValidationList.contains(paraName)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"Dupulicated paraName"+"\n");
				throw new CustomizedException("Dupulicated paraName",CustomizedException.DUPULICATED_PARA);
			}else{
				nameValidationList.add(paraName);
			}
			if(positionValidationList.contains(position)){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"Dupulicated position"+"\n");
				throw new CustomizedException("Dupulicated position",CustomizedException.DUPULICATED_POSITION);
			}else{
				positionValidationList.add(position);
			}
			
			ParameterModel paraModel=new ParameterModel(paraName);
			paraMap.put(paraName, paraModel);
			String paraType=((Element)paraNode).getFirstChild().getNodeValue();
			paraModel.setParaType(paraType);
			if(!StringUtil.isEmpty(defaultValue)){
				paraModel.setDefaultValue(defaultValue);
			}
			if(!StringUtil.isEmpty(dataType)){
				paraModel.setDataType(dataType);
			}
			if(!StringUtil.isEmpty(optionalValue)){
				String options[]=optionalValue.split(",");
				paraModel.setOptionalValue(Arrays.asList(options));
			}
			if(!StringUtil.isEmpty(position)){
				paraModel.setPosition(position);
			}
		}
		for(int i=1;i<positionValidationList.size()+1;i++){
			if(!positionValidationList.contains(String.valueOf(i))){
				itsLogger.error(CustomizedOperatorModel.class.getName()+"Missing position"+"\n");
				throw new CustomizedException("Missing position",CustomizedException.MISSING_POSITION);
			}
		}
		CustomizedOperatorModel coModel=new CustomizedOperatorModel(distinctOperatorName,udfSchema,udfName,paraMap,outputColumnMap);
		
		return coModel;
	}
	private static boolean isUdfNameExists(String udfName) {
		File modelPath=new File(CustomziedConfig.getObjectPath());
		if(modelPath.exists()){
			ArrayList<String> fileList=new ArrayList<String>();
			String[] fileArray=modelPath.list();
			
			for(int i=0;i<fileArray.length;i++){
				if(fileArray[i].endsWith(CustomziedConfig.MODEL_SUFFIX)
						&&fileArray[i].indexOf("_")>0){ 
					String opNameWithoutTimestamp=fileArray[i].substring(0, fileArray[i].lastIndexOf("_"));
					fileList.add(opNameWithoutTimestamp+CustomziedConfig.MODEL_SUFFIX);
				}
			}
			if(fileList.contains(udfName+CustomziedConfig.MODEL_SUFFIX)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}
	public boolean hasParamPosition(String position) {
		Collection<ParameterModel> values = getParaMap().values();
		for(Iterator<ParameterModel> it= values.iterator();it.hasNext();){
			ParameterModel pm = it.next();
			if(pm.getPosition().equals(position)){
				return true;
			}
		}
		return false;
	}
	public String getParamNameByPosition(String position) { 
		Collection<ParameterModel> values = getParaMap().values();
		for(Iterator<ParameterModel> it= values.iterator();it.hasNext();){
			ParameterModel pm = it.next();
			if(pm.getPosition().equals(position)){
				return pm.getParaName();
			}
		}
		return null;
	}

	

}