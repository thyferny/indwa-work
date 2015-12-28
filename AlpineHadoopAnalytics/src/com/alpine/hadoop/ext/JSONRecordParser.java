
package com.alpine.hadoop.ext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.hadoop.ext.json.JSONArray;
import com.alpine.hadoop.ext.json.JSONException;
import com.alpine.hadoop.ext.json.JSONObject;


public class JSONRecordParser extends AbstractRecordParser {

	private List<String> jsonPathList=new ArrayList<String>();
	private List<String> transformedPathList=new ArrayList<String>();
	private String sturctureType;
	private String containerPath;
	private static Logger itsLogger = Logger.getLogger(JSONRecordParser.class);

	public JSONRecordParser(List<String> jsonPathList,String sturctureType,String containerPath) {
		this.jsonPathList=jsonPathList;
		this.sturctureType=sturctureType;
		this.containerPath=containerPath;
		generateTransformedPathList(jsonPathList);
	}
	@Override
	public List<String[]> parse(String aRecordContent) throws Exception {
		aRecordContent=aRecordContent.trim();
		List<String[]> result=null;
		if(aRecordContent!=null){			
			XPathRow rootRow=null;
			try {
				rootRow = generateXPathRow(aRecordContent);
			} catch (Exception e) {
				itsLogger.error(aRecordContent);
				return null;
			}
			
			rootRow.countMaXDepth();
			
			result = new ArrayList<String[]>(rootRow.getMaxDepth());
			
			initResultData(result, rootRow.getMaxDepth());
			
			rootRow.fillResultData(result);
		}
		return result;
	}
	private XPathRow generateXPathRow(String aRecordContent)
			throws JSONException, Exception {
		XPathRow rootRow = null;
		if(aRecordContent.startsWith("[")&&
				aRecordContent.endsWith("]")){
			JSONArray jsonArray=new JSONArray(aRecordContent);
			removeNoNeedObjArray(jsonArray,null);
			XPathRow result1 = new XPathRow();
			if(sturctureType.equals(JSONRecordReader.STRUCTURE_TYPE_PURE_DATA_ARRAY)){
				createXPathRowForArray(result1, null, jsonArray,true);
			}else{
				createXPathRowForArray(result1, null, jsonArray,false);
			}
			rootRow=result1;
		}else if(aRecordContent.startsWith("{")&&
				aRecordContent.endsWith("}")){
			JSONObject jsonObj=new JSONObject(aRecordContent);
			if(sturctureType.equals(JSONRecordReader.STRUCTURE_TYPE_LINE)
					&&containerPath!=null&&containerPath.length()>0){
				String[] paths = containerPath.split("/");
				for(int i=0;i<paths.length;i++){
					if(jsonObj.has(paths[i])&&jsonObj.get(paths[i]) instanceof JSONObject){
						jsonObj=(JSONObject)jsonObj.get(paths[i]);
					}else{
						rootRow = new XPathRow();
						return rootRow;
					}
				}
			}
			removeNoNeedObj(jsonObj,null);
			rootRow =createXPathRow(jsonObj, null);
		}else{
			rootRow = new XPathRow();
			itsLogger.error(aRecordContent);
			itsLogger.error("It's not standart json data!");
		}
		return rootRow;
	}

	private void generateTransformedPathList(List<String> jsonPathList) {
		if(jsonPathList!=null&&jsonPathList.size()>0){
			for(String jsonPath:jsonPathList){
				String[] jsonPaths = jsonPath.split("/");
				StringBuffer sb=new StringBuffer();
				for(String json:jsonPaths){
					if(json.isEmpty()==false&&json.equals("*")==false){
						sb.append(json).append("_");
					}
				}
				if(sb.length()>0&&sb.toString().endsWith("_")){
					sb=sb.deleteCharAt(sb.length()-1);
				}
				transformedPathList.add(sb.toString());
			}
		}
	}
	
	private void initResultData(List<String[]> result, int number) {
		for (int i = 0; i < number; i++) {
			result.add(new String[jsonPathList.size()]);
		}
	}
	
	private boolean removeNoNeedObjArray(JSONArray jsonArray,String parentKey) throws JSONException{
		boolean needRemove=true;
		for(int i=0;i<jsonArray.length();i++){
			Object obj = jsonArray.get(i);
			if(obj instanceof JSONObject){
				if(removeNoNeedObj((JSONObject)obj,parentKey)==false){
					needRemove=false;
				}
			}else if(obj instanceof JSONArray){
				if(removeNoNeedObjArray((JSONArray)obj,parentKey)==false){
					needRemove=false;
				}
			}else{
				if(transformedPathList.contains(parentKey)==true){
					needRemove = false;
				}
			}
		}
		return needRemove;
	}

	@SuppressWarnings("rawtypes")
	private boolean removeNoNeedObj(JSONObject jsonObj,String parentKey) throws JSONException {
		List<String> needRemoveKeyList=new ArrayList<String>();
		Iterator iter = jsonObj.keys();
		while(iter.hasNext()){
			String key = (String)iter.next();		
			String columnName=null;
			if(parentKey!=null&&parentKey.isEmpty()==false){
				columnName=parentKey+"_"+key;
			}else{
				columnName=key;
			}
			
			Object obj = jsonObj.get(key);
			
			boolean needRemove = false;
			if(obj instanceof JSONObject){
				needRemove = removeNoNeedObj((JSONObject)obj,columnName);
			}else if(obj instanceof JSONArray){
				needRemove = removeNoNeedObjArray((JSONArray)obj,columnName);
			}else{
				if(transformedPathList.contains(columnName)==false){
					needRemove = true;
				}
			}
			if(needRemove){
				needRemoveKeyList.add(key);
			}
		}
		
		Iterator<String> removeIter = needRemoveKeyList.iterator();
		while(removeIter.hasNext()){
			String key = removeIter.next();
			jsonObj.remove(key);
		}
		
		if(jsonObj.length()==0){
			return true;
		}else{
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	private XPathRow createXPathRow(JSONObject jsonObj,String parentKey)
			throws Exception {
		XPathRow result = new XPathRow();
		Iterator iter = jsonObj.keys();
		while(iter.hasNext()){
			String key = (String)iter.next();
			if(jsonObj.isNull(key)){
				continue;
			}
			String columnName=null;
			if(parentKey!=null&&parentKey.isEmpty()==false){
				columnName=parentKey+"_"+key;
			}else{
				columnName=key;
			}
			Object obj = jsonObj.get(key);
			if(obj instanceof String){
				if(((String)obj)==null || ((String)obj).length()==0){
					continue;
				}
				handleStringObject(result, columnName, obj);
			}else if(obj instanceof JSONObject){
				result.addChildXPathRow(createXPathRow((JSONObject)obj,columnName), columnName);
			}else if(obj instanceof JSONArray){
				createXPathRowForArray(result, columnName, (JSONArray)obj,false);
			}else {
				handleOtherObject(result, columnName, obj);
			}
		}
		return result;
	}
	private void handleOtherObject(XPathRow result, String columnName,
			Object obj) {
		XPathRow createdXPathRow = new XPathRow();
		int index = transformedPathList.indexOf(columnName);
		if(index==-1){
			return;
		}
		createdXPathRow.getRowDatas().put(index,
				String.valueOf(obj));
		
		result.addChildXPathRow(createdXPathRow,
				columnName);
	}
	private void handleStringObject(XPathRow result, String columnName,
			Object obj) {
		XPathRow createdXPathRow = new XPathRow();
		int index = transformedPathList.indexOf(columnName);
		if(index==-1){
			return;
		}
		createdXPathRow.getRowDatas().put(index,
				(String)obj);
		
		result.addChildXPathRow(createdXPathRow,
				columnName);
	}

	private void createXPathRowForArray(XPathRow result, String columnName,
			JSONArray jsonArray,boolean isParentArray) throws JSONException, Exception {
		for(int i=0;i<jsonArray.length();i++){
			Object subObj = jsonArray.get(i);
			if(subObj instanceof JSONObject){
				result.addChildXPathRow(createXPathRow((JSONObject)subObj,columnName), columnName);
			}else if(subObj instanceof JSONArray){
				String newColumnName=null;
				if(isParentArray){
					if(columnName!=null&&columnName.isEmpty()==false){
						newColumnName=columnName+"_"+String.valueOf(i);
					}else{
						newColumnName=String.valueOf(i);
					}
				}
				createXPathRowForArray(result, newColumnName, (JSONArray)subObj,true);
			}else if(subObj instanceof String){
				if(isParentArray){
					String newColumnName=null;
					if(columnName!=null&&columnName.isEmpty()==false){
						newColumnName=columnName+"_"+String.valueOf(i);
					}else{
						newColumnName=String.valueOf(i);
					}
					handleStringObject(result, newColumnName, subObj);
				}else{
					handleStringObject(result, columnName, subObj);
				}			
			}else {
				if(isParentArray){
					String newColumnName=null;
					if(columnName!=null&&columnName.isEmpty()==false){
						newColumnName=columnName+"_"+String.valueOf(i);
					}else{
						newColumnName=String.valueOf(i);
					}
					handleOtherObject(result, newColumnName, subObj);
				}else{
					handleOtherObject(result, columnName, subObj);
				}			
			}
		}
	}
	
	@Override
	public String[] parseLine(String string) throws Exception {
		// nothing to do here, n
		throw new UnsupportedOperationException();
	}
}
