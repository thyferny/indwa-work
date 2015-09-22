package com.alpine.utility.hadoop.pig;

import java.util.Map;
import java.util.Properties;

import com.alpine.utility.hadoop.HadoopConnection;

public class AlpineRestCallObject {
	private Properties properties;
	private HadoopConnection hadoopConnection;
	private Map<String,String> methodParameters;
	private String methodName;
	
	public AlpineRestCallObject(HadoopConnection hadoopConnecion,Properties properties, String methodName,Map<String, String> methodParameters) {
		super();
		this.hadoopConnection=hadoopConnecion;
		this.properties = properties;
		this.methodParameters = methodParameters;
		this.methodName = methodName;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Map<String, String> getMethodParameters() {
		return methodParameters;
	}
	public void setMethodParameters(Map<String, String> methodParameters) {
		this.methodParameters = methodParameters;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public HadoopConnection getHadoopConnection() {
		return hadoopConnection;
	}
	public void setHadoopConnection(HadoopConnection hadoopConnection) {
		this.hadoopConnection = hadoopConnection;
	}
	
	
}
