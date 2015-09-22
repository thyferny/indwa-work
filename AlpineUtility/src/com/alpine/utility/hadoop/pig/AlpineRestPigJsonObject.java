package com.alpine.utility.hadoop.pig;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.alpine.utility.hadoop.HadoopConnection;

public class AlpineRestPigJsonObject {
	private Properties properties;
	private HadoopConnection hadoopConnection;
	private List<String> script;
	private boolean local;
	private Map<String,String> methodParameters;
	private String methodName;
	
	public AlpineRestPigJsonObject(HadoopConnection hadoopConnecion,Properties properties, 
			List<String> script,boolean local, 
			String methodName,Map<String, String> methodParameters) {
		super();
		this.hadoopConnection=hadoopConnecion;
		this.properties = properties;
		this.script = script;
		this.local = local;
		this.methodParameters = methodParameters;
		this.methodName = methodName;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public List<String> getScript() {
		return script;
	}
	public void setScript(List<String> script) {
		this.script = script;
	}
	public boolean isLocal() {
		return local;
	}
	public void setLocal(boolean local) {
		this.local = local;
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
