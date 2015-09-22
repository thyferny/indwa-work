package com.alpine.utility.hadoop.pig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.alpine.utility.hadoop.HadoopConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AlpineRestPigJsonObjectSerializerDeserializer {
	
	
	public static String toJson(AlpineRestPigJsonObject pigObject){
		if(null==pigObject){
			return null;
		}
		Gson   gson = new Gson();
		String pigProps= gson.toJson(convertPropsIntoMap(pigObject.getProperties()));
		String script=gson.toJson(pigObject.getScript());
		String params=gson.toJson(pigObject.getMethodParameters());
		String hadoopConnection=gson.toJson(pigObject.getHadoopConnection());
		String jsonObject = gson.toJson(new AlpPigJSON(hadoopConnection,pigProps,pigObject.isLocal(),params,script,pigObject.getMethodName()));	
		return jsonObject;
	}
	
	public static AlpineRestPigJsonObject fromJson(String jsonObject){
		if(null==jsonObject||"".equals(jsonObject.trim())){
			return null;
		}
		
		Gson   gson = new Gson();
		AlpPigJSON jobj=gson.fromJson(jsonObject, AlpPigJSON.class);
		Type type = new TypeToken<HashMap<String,String>>(){}.getType();
		Map<String,String> pigPropsMap=gson.fromJson(jobj.getPigProps(), type);
		String hcStr=jobj.getHadoopConnection();
		Properties props=convertMapIntoProps(pigPropsMap);
		Map<String, String> methodParameters = pigPropsMap=gson.fromJson(jobj.getParams(), type);
		boolean isLocal=jobj.isLocal();
		type = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> script=gson.fromJson(jobj.getScript(), type);
		String methodName=jobj.getMethodName();
		HadoopConnection hadoopConnection = gson.fromJson(hcStr, HadoopConnection.class);
		
		return new AlpineRestPigJsonObject(hadoopConnection,props,script,isLocal,methodName,methodParameters);
		
	}

	private static Map<String,String> convertPropsIntoMap(Properties props) {
		Set<String> propNames = props.stringPropertyNames();
		Map<String,String> propsMap=new HashMap<String,String>();
		for(String pn:propNames){
			propsMap.put(pn, props.getProperty(pn));
		}
		return propsMap;
	}
	
	private static Properties convertMapIntoProps(Map<String,String> props){
		Properties properties=new Properties();
		Set<String> keys=props.keySet();
		for(String p:keys){
			properties.put(p, props.get(p));
		}
		return properties;
	}
	
	private static final class AlpPigJSON{
		String pigProps;
		String hadoopConnection;
		boolean local;
		String params;
		String script;
		String methodName;
		
		public AlpPigJSON(String hadoopConnection,String pigProps, boolean local, String params,
				String script, String methodName) {
			super();
			this.hadoopConnection=hadoopConnection;
			this.pigProps = pigProps;
			this.local = local;
			this.params = params;
			this.script = script;
			this.methodName = methodName;
		}
		public String getPigProps() {
			return pigProps;
		}
		
		public boolean isLocal() {
			return local;
		}
		public String getParams() {
			return params;
		}
		public String getScript() {
			return script;
		}
		public String getMethodName() {
			return methodName;
		}
		public String getHadoopConnection() {
			return hadoopConnection;
		}
		
	}
	
}
