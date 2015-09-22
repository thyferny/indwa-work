package com.alpine.utility.hadoop.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.pig.backend.executionengine.ExecJob;
import org.apache.pig.data.Tuple;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.fs.HadoopRestCallerManager;
import com.alpine.utility.hadoop.fs.HadoopRestCallerManager.RESTCallType;
import com.sun.jersey.api.client.ClientResponse;

public class AlpineRestPigServer implements AlpinePigServer{
	//openIterator
	private static final String OPEN_ITERATOR = "openIterator";
	private static final String ITERATOR_VAR_NAME = "iteratorVarName";
	
	//Store
	private static final String STORE = "store";
	public static final String PIG_STORAGE_FUNCTION_WITH_DELIMETER = "pigStorageFunctionWithDelimeter";
	public static final String FULL_PATH_FILE_NAME = "fullPathFileName";
	public static final String OUTPUT_TEMP_FILE_NAME = "outputTempFileName";
	
	
	
	private List<String> scriptCache;
	private boolean anyRestCallMade;
	private Properties properties;
	private boolean localmode;
	private HadoopConnection hadoopConnection;
	
	public AlpineRestPigServer(HadoopConnection hadoopConnection, Properties properties, boolean localmode) {
		this.properties=properties;
		this.localmode=localmode;
		this.hadoopConnection=hadoopConnection;
		scriptCache=new ArrayList<String>();
		anyRestCallMade=false;
		
	}

	@Override
	public void shutdown() {
		if(!anyRestCallMade){
			scriptCache.clear();
		}else{
			
		}
		
	}

	@Override
	public void registerQuery(String line) throws IOException {
		scriptCache.add(line);
	}

	@Override
	public ExecJob store(String outputTempFileName, String fullPathFileName,String pigStorageFunctionWithDelimeter) throws IOException {
		Map<String,String> params=new HashMap<String,String>();
		params.put(OUTPUT_TEMP_FILE_NAME,outputTempFileName);
		params.put(FULL_PATH_FILE_NAME,fullPathFileName);
		params.put(PIG_STORAGE_FUNCTION_WITH_DELIMETER,pigStorageFunctionWithDelimeter);

		
		AlpineRestPigJsonObject jsonObj=new AlpineRestPigJsonObject(hadoopConnection,properties, scriptCache, localmode,STORE,params);
		ClientResponse response=HadoopRestCallerManager.makeARESTCallTo( hadoopConnection, AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj),RESTCallType.pig);
		String result = response.getEntity(String.class);
		if(null!=result&&"".equals(result.trim())==false){
			return new AlpinePigExecJob(new Exception(result));
		}else{
			return new AlpinePigExecJob(null);
		}

	}

	@Override
	public Iterator<Tuple> openIterator(String iteratorVarName) throws IOException {
		Map<String,String> params=new HashMap<String,String>();
		params.put(ITERATOR_VAR_NAME,iteratorVarName);
		
		AlpineRestPigJsonObject jsonObj=new AlpineRestPigJsonObject(hadoopConnection,properties, scriptCache, localmode,OPEN_ITERATOR,params);
//		
//		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
//		AlpineRestPigJsonObject wiredBack=AlpineRestPigJsonObjectSerializerDeserializer.fromJson(jsonString);
//		System.out.println(wiredBack.getHadoopConnection().equals(hadoopConnection));
		ClientResponse response=HadoopRestCallerManager.makeARESTCallTo(hadoopConnection,AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj),RESTCallType.pig);
		String result = response.getEntity(String.class);
		Iterator<Tuple> it = AlpineJsonSerializer.deSerializeTupleIteratorToJSon(result);
		return it;
	}

}
