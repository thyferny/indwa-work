package org.apache.pig.builtin;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.pig.data.Tuple;

public class PigUtil {
    protected static final Log mLog = LogFactory.getLog(PigUtil.class );

	//"/main/flow.do?method=getConnSchemaTablesMap&resourceType=Personal"
	public static void reportBadDataCount(long badDataLineCount, String callBackURL,String operatorName,String uuid ,String  badColumnIndexString) {  
		mLog.debug("reportBadDataCount:" + uuid +":" +operatorName+":"+operatorName+":"+badDataLineCount+":" +badColumnIndexString);
		if( callBackURL==null||callBackURL.trim().length()==0||callBackURL.equalsIgnoreCase( "null")){
			return;
		}
		try{
		HttpClient httpclient = new HttpClient();
		String pKey = AlpinePigConstants.COUNT_BADDATA_PIG + operatorName ; 
		
//		if(Locale.getDefault().getLanguage().startsWith("ZH")){
//			pKey= new String (pKey.getBytes("gb2312"),"utf-8");
//
//		}
		//operatorName could contains blank and cause problem 
		pKey =URLEncoder.encode(pKey);
		pKey =URLEncoder.encode(pKey);

			String url = callBackURL
					+ "/main/flowRunner.do?method=putFlowRunningProperty&uuid="
					+ uuid + "&pKey=" + pKey + "&pValue=" + badDataLineCount ;
		//avoid the blank error in url
		GetMethod getMethod = new GetMethod(url);
		if(badDataLineCount!=0){
			getMethod.addRequestHeader("Accept", "text/plain");
			getMethod.addRequestHeader("Content-Type", "text/plain");
	
			httpclient.executeMethod(getMethod);
			
		}
		if(badColumnIndexString!=null&&badColumnIndexString.trim().length()>0){
			//this time tell the bad index 
			  pKey = AlpinePigConstants.BAD_COLUMN_INDEX + operatorName ; 

			 url = callBackURL
						+ "/main/flowRunner.do?method=putFlowRunningProperty&uuid="
						+ uuid + "&pKey=" + pKey + "&pValue=" + badColumnIndexString ;
			//avoid the blank error in url
			  getMethod = new GetMethod(url);
			
				getMethod.addRequestHeader("Accept", "text/plain");
				getMethod.addRequestHeader("Content-Type", "text/plain");
				httpclient.executeMethod(getMethod);

		}	
				
				
			getMethod.releaseConnection();
		}catch(Exception e){
			//nothing to do
			//e.printStackTrace();
			mLog.error("Can not reportBadDataCount to "+callBackURL,e) ;
		}
	}
	
    
	public static boolean equalsFocusOrder(List source,List target){
		if(source==target){
			return true;
		}
		else if(source==null&&target==null){
			return true;
		}
		else if (source==null&&target!=null){
			return false;
		}
		else if (source!=null&&target==null){
			return false;
		}else if(source.size()!=target.size()){
			return false;
		}else{
			for(int i=0;i<source.size();i++){
				if(source.get(i)==null
						||target.get(i)==null
						||source.get(i).equals(target.get(i))==false){
					return false;
				}
			}
			return true;
		}
	}

	public static boolean isDouble(String value) {
		//java think NaN is double, but pig don't--for pivotal 42034001
		if(value==null||value.equals("NaN")){
			return false;
		}
		try{
			Double.parseDouble(value);
		}catch(Exception e){
			return false ;
		}
		return true;
	}

	public static boolean isFloat(String value) {
		//java think NaN is double, but pig don't--for pivotal 42034001
		if(value==null||value.equals("NaN")){
			return false;
		}
		try{
			Float.parseFloat(value);
		}catch(Exception e){
			return false ;
		}
		
		return true;
	}

	public static boolean isLong(String value) {
		try{
			Long.parseLong(value);
		}catch(Exception e){
			return false ;
		}
		return true;
	}

	public static boolean isInt(String value) {
		try{
			Integer.parseInt(value);
		}catch(Exception e){
			return false ;
		}
		return true;
	}
	
	 public static void writeLineWithEscapAndQuote(RecordWriter writer, Tuple f, byte fieldDel, byte quoteChar, byte escapeChar ) throws IOException, InterruptedException {
	    	
	    	int size = f.size()  ;
	    	String value = null;
	    	for (int i = 0; i < size; i++) {
	    		
	    		if(f.get(i)==null){
	    			continue ;
	    		}
	    		value = f.get(i).toString() ;
	    		if(value!=null ){
	    			value = value.replaceAll("\\n", " ");

	    		} 
	    		if(value.indexOf(fieldDel)>-1||value.indexOf(quoteChar)>-1){//has delimiter
	    			if(value.indexOf(quoteChar)>-1){ //start with a quote
	    			 
	    				value = value.replace(String.valueOf((char)quoteChar), new String(new byte[]{escapeChar,quoteChar}));
	    			}
	    			value = (char)quoteChar + value + (char)quoteChar;
	    		} 
    			f.set(i, value)  ; 

			}
	    	
	    	writer.write(null, f);
			
		}   

		public static Text getNoneEmptyValue(Text value, RecordReader in) throws  Exception {
			if(value!=null&&value.getLength()!=0&&value.toString().trim().length()!=0){
				return value;
			}
			
			boolean notDone= true;
			while(notDone==true&&(value==null||value.getLength()==0||value.toString().trim().length()==0)){
				notDone = in.nextKeyValue();
				value=(Text)in.getCurrentValue();
			}		
			return value;
		}

}
