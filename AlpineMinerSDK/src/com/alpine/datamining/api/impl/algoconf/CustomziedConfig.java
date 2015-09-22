/**
 * ClassName CustomziedConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
public class CustomziedConfig extends DataOperationConfig {
    private static final Logger itsLogger = Logger.getLogger(CustomziedConfig.class);


    private static final String CURRENTDIRECTORY=AlpineUtil.getCurrentDirectory()+"configuration"+File.separator;
//	private static final String CURRENTDIRECTORY=System.getProperty("java.io.tmpdir");
	public static final String MODEL_PATH = CURRENTDIRECTORY+"CustomizedOperator"+Resources.minerEdition+File.separator;
	public static final String MODEL_SUFFIX = ".cm";
	
	private static String path;

	public static void setPath(String path) {
		CustomziedConfig.path = path;
	}
	public static String getObjectPath(){
		if(!StringUtil.isEmpty(path)){
			return path;
		}else{
			String operatSystem=System.getProperty("os.name");
			if(!operatSystem.startsWith("Mac OS")){
				return MODEL_PATH;
			}else{
				File operatorFile=new File(CURRENTDIRECTORY);
				if(operatorFile.exists()){
					return MODEL_PATH;
				}else{
					return "."+File.separator+"configuration"+File.separator+"CustomizedOperator"+Resources.minerEdition;
				}
			}
		}
	}
	private HashMap<String,String> parametersMap;
	private HashMap<String,String> outputMap;
	private String udfName;
	private String udfSchema;
	private String remainColumns;
	private String operatorName;
	
	public static final String INCLUDE = "remainColumns";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);	
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);

		parameters.add(INCLUDE);
	}
	public CustomziedConfig() {
		super();
		setParameterNames(parameters);
	}
	public CustomziedConfig(String outputType, String outputSchema,
			String outputTable, String dropIfExist) {
		super(outputType, outputSchema, outputTable, dropIfExist);
		setParameterNames(parameters);
	}
	public HashMap<String, String> getParametersMap() {
		return parametersMap;
	}
	public void setParametersMap(HashMap<String, String> parametersMap) {
		this.parametersMap = parametersMap;
	}
	public String getUdfName() {
		return udfName;
	}
	public void setUdfName(String udfName) {
		this.udfName = udfName;
	}
	public String getUdfSchema() {
		return udfSchema;
	}
	public void setUdfSchema(String udfSchema) {
		this.udfSchema = udfSchema;
	}
	public HashMap<String, String> getOutputMap() {
		return outputMap;
	}
	public void setOutputMap(HashMap<String, String> outputMap) {
		this.outputMap = outputMap;
	}
	
	
	public String getRemainColumns() {
		return remainColumns;
	}
	public void setRemainColumns(String remainColumns) {
		this.remainColumns = remainColumns;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	@Override
	public void setValuesMap(HashMap<String, String> valueMap){
  		try {
			if(parameterNames!=null){
				for (Iterator<String> iterator = parameterNames.iterator(); iterator.hasNext();) {
						String paramName = iterator.next();
						String paramValue=valueMap.get(paramName);
						String firstChar=  String.valueOf(paramName.charAt(0));
						String methodName="set"+firstChar.toUpperCase()+paramName.substring(1);
						try {
							Method method = this.getClass().getMethod(methodName, String.class);
							if(method!=null){
								method.invoke(this,paramValue); 
							}
						} catch (NoSuchMethodException e) {
							itsLogger.error(e.getMessage(),e) ;
						}
					}
				setParametersMap((HashMap<String, String>)valueMap.clone());
				}
			} catch ( Exception e) {
					e.printStackTrace();
					itsLogger.debug("getParameterMap Error:"+e);
			} 
		
	}
	
}
