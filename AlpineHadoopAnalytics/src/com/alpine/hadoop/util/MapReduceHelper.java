
package com.alpine.hadoop.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.TaskAttemptID;

import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.AlpineHadoopConstants;
import com.alpine.hadoop.ext.RecordParser;
import com.alpine.hadoop.ext.RecordParserFactory;


public class MapReduceHelper {
	
	private String[] columnTypes;
	private String[] columnNames;
	
	private List<String> newColumnTypes;
	private List<String> newColumnNames; 
	String headerLineValue = "";

	private RecordParser recordParser;
	private String dirtyFileName = "";
	private FileSystem fs = null;
	private FSDataOutputStream out = null;
	private Configuration conf;
	private TaskAttemptID taskid;
	private String dirtyPath;
	
	public String[] getColumnTypes() {
		return columnTypes;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public List<String> getNewColumnTypes() {
		return newColumnTypes;
	}

	public List<String> getNewColumnNames() {
		return newColumnNames;
	}

	private Map<String, String[]> distinctMap;
	
	

	
	public Map<String, String[]> getDistinctMap() {
		return distinctMap;
	}

	public RecordParser getRecordParser() {
		return recordParser;
	}
 
	List<Integer> involvedColumnIds;//ids invovled,[1,2,3:4,5*6]->[1,2,3,4,5,6]

	public List<Integer> getInvolvedColumnIds() {
		return involvedColumnIds;
	}

	
	public void setInvolvedColumnIds(List<Integer> involvedColumnIds) {
		this.involvedColumnIds = involvedColumnIds;
	}

	
	public void initInvolvedColumnIds(List<Integer> columnIds,
			List<HadoopInteractionItem> interactionItems) {
		this.involvedColumnIds = new ArrayList<Integer>();
		for (int id : columnIds) {
			involvedColumnIds.add(id);
		}
		for (HadoopInteractionItem item : interactionItems) {
			int leftId = item.getLeftId();
			int rightId = item.getRightId();
			if (involvedColumnIds.contains(leftId) == false) {
				involvedColumnIds.add(leftId);
			}
			if (involvedColumnIds.contains(rightId) == false) {
				involvedColumnIds.add(rightId);
			}
		}
	}

	
	public void initInvolvedColumnIds(List<Integer> columnIds,
			List<HadoopInteractionItem> interactionItems, int dependId) {
		this.involvedColumnIds = new ArrayList<Integer>();
		initInvolvedColumnIds(columnIds, interactionItems);
		involvedColumnIds.add(dependId);
	}


	long badDataCount =0;

	//the dirty data for each map
	//List<String> dirtyDataList = new ArrayList<String>();

//	public List<String> getDirtyDataList() {
//		return dirtyDataList;
//	}



	public MapReduceHelper(Configuration conf, TaskAttemptID taskid) {
		this.taskid = taskid;
		this.conf = conf;
		this.dirtyPath = conf.get(AlpineHadoopConfKeySet.DIRTY_PATH);

		try {
			recordParser = RecordParserFactory.createRecordParser(conf);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		columnTypes = conf.get(AlpineHadoopConfKeySet.COLUMN_TYPES).split(",");
		columnNames	= conf.get(AlpineHadoopConfKeySet.COLUMN_NAMES).split(",");
		
		newColumnTypes=new ArrayList<String>();
		newColumnNames=new ArrayList<String>();
		distinctMap=new HashMap<String, String[]>();
		
		for(int i=0;i<columnNames.length;i++){

			String distinctValueArrayString=conf.get(AlpineHadoopConfKeySet.ALPINE_PREFIX+columnNames[i]);
			if(distinctValueArrayString==null){
				newColumnNames.add(columnNames[i]);
				newColumnTypes.add(columnTypes[i]);
			}
			else{
				if(distinctValueArrayString.startsWith("[")){
					distinctValueArrayString=distinctValueArrayString.substring(1);
				}
				if(distinctValueArrayString.endsWith("]")){
					distinctValueArrayString=distinctValueArrayString.substring(0,distinctValueArrayString.length()-1);
				}
				
				String[] distinctArray=distinctValueArrayString.split(",");
				distinctMap.put(columnNames[i], distinctArray);
				for(int j=0;j<distinctArray.length-1;j++){
					String distinct=distinctArray[j].trim();
					//how about a value contains ","

					distinct=distinct.replaceAll(AlpineHadoopConstants.SPECIAL_SEP_STRING, ",") ; 
					newColumnNames.add(columnNames[i]+"_"+distinct);
					newColumnTypes.add("int");
				}
			}
		}
		
		try {
			fs = FileSystem.get(conf);
		} catch (IOException e) {
		}
		//this is not proper for union
		headerLineValue = conf.get(AlpineHadoopConfKeySet.HEADER_LINE_VALUE);
	}
	
	public boolean dirtyAdd(String[] columnValues){
		badDataCount =badDataCount+1;
//		String arg0 = Arrays.toString(columnValues);
//		arg0 = arg0.substring(1,
//				arg0.length() - 1);
//		return dirtyDataList.add(arg0);
		return true;
	}

	protected boolean isTypeMatch(String[] columnValues, boolean strict) {
		if (columnValues.length == newColumnTypes.size()) {
			boolean isMatch = true;
			if (strict) {
				for (int i = 0; i < columnValues.length; i++) {
					if (!DataPretreatUtility.checkType(columnValues[i],
						newColumnTypes.get(i))) {
						isMatch = false;
						break;
					}
				}
			} else {
				if (involvedColumnIds == null) {
					throw new NullPointerException();
				} else {
					try {
						for (int i : involvedColumnIds) {
							if(i<columnValues.length){
								if(columnValues[i]==null||"".equals(columnValues[i].trim())){
									isMatch = false;
									break;
								}
								if (false==DataPretreatUtility.checkType(columnValues[i],
									newColumnTypes.get(i))) {
									isMatch = false;
									break;
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			}
			return isMatch;
		} else {
			return false;
		}
	}

	

	public List<String[]> getCleanData(Text value, boolean strict) {
		if (headerLineValue == null) {

		} else if (value == null || value.toString().trim().length() == 0
				|| headerLineValue.equals(value.toString())) {
			return null;
		}
		List<String[]> result = new ArrayList<String[]>();
		List<String[]> valueList = null;
		try {
			valueList = recordParser.parse(value.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (valueList != null) {
			for (String[] row : valueList) {
				String [] transformedRow = row;
				if(row.length==columnNames.length){
					transformedRow=this.projectNewColumnValue(row);
				}
				if (isTypeMatch(transformedRow, strict)) {
					result.add(transformedRow);
				} else {
//					String dirtyline = Arrays.toString(transformedRow);
//					dirtyline = dirtyline.substring(1, dirtyline.length() - 1);
					dirtyAdd(row);
					//dirtyDataList.add(dirtyline);
				}
			}
		}
		return result;
	}
	
	public Map<String[], Boolean> getAllDataWithCleanFlag(Text value,List<String> columnsIdStrings) {
		if (headerLineValue == null) {

		} else if (value == null || value.toString().trim().length() == 0
				|| headerLineValue.equals(value.toString())) {
			return null;
		}
		Map<String[], Boolean> result = new HashMap<String[], Boolean>();
		List<String[]> valueList = null;
		try {
			valueList = recordParser.parse(value.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (valueList != null) {
			for (String[] row : valueList) {
				boolean legal=true;
				if(row.length!=columnTypes.length){
					result.put(row, false);
					continue;
				}
				for (int i = 0; i < columnsIdStrings.size(); i++) {
					String column = columnsIdStrings.get(i);
					if("-1".equals(column)){
						continue;
					}
					if (column.indexOf(":") == -1) {
						int id = Integer.parseInt(column);
						if(id>=newColumnNames.size()){
							legal = false;
							break;
						}
						if (!DataPretreatUtility.checkType(projectNewColumnValue(row)[id],
								newColumnTypes.get(id))) {
							legal = false;
							break;
						}
					} else {
						int leftId = Integer.parseInt(column.split(":")[0]);
						int rightId = Integer.parseInt(column.split(":")[1]);
						if (!DataPretreatUtility.checkType(projectNewColumnValue(row)[leftId],
								newColumnTypes.get(leftId))) {
							legal = false;
							break;
						}
						if (!DataPretreatUtility.checkType(projectNewColumnValue(row)[rightId],
								newColumnTypes.get(rightId))) {
							legal = false;
							break;
						}
					}
				}
				result.put(row, legal);
			}
		}
		return result;
	}
	
	public List<String> getPredictionColumns(String columnsKey) {
		List<String> predictionColumn = new ArrayList<String>();
		String columnsString = conf.get(columnsKey);
		if (null != columnsString && !"".equals(columnsString)) {
			String[] columns = columnsString.split(",");
			for (String columnDescription : columns) {
				predictionColumn.add(columnDescription);
			}
		}
		return predictionColumn;
	}
	
	
	public List<HadoopInteractionItem> getInteractionItems(String interactionKey){
		List<HadoopInteractionItem> interactionItems = new ArrayList<HadoopInteractionItem>();
		String interactionString = conf.get(interactionKey);
		if (null != interactionString && !"".equals(interactionString)) {
			String[] Interactions = interactionString.split(",");
			for (String interaction : Interactions) {
				String left=null;
				String right=null;
				String type=null;
				if(interaction.indexOf(":")!=-1){
					left=interaction.split(":")[0];
					right=interaction.split(":")[1];
					type=":";
				}
				else if(interaction.indexOf("*")!=-1){
					left=interaction.split("\\*")[0];
					right=interaction.split("\\*")[1];
					type="*";
				}
				int leftid=getIndex(left);
				int rightid=getIndex(right);
				interactionItems.add(new HadoopInteractionItem(leftid,rightid,type));
			}
		}
		return interactionItems;
	}

	public List<Integer> getColumnIds(String columnKey) {
		List<Integer> columnIds = new ArrayList<Integer>();
		String columnNames = conf.get(columnKey);

		if (null != columnNames && !"".equals(columnNames)) {
			String[] columnNameArray = columnNames
					.split(",");
			for (String columnName : columnNameArray) {
				String[] distinctArray = distinctMap.get(columnName);
				if(distinctArray==null){
					columnIds.add(getIndex(columnName));
				}
				else{
					for(int i=0;i<distinctArray.length-1;i++){
						columnIds.add(getIndex(columnName+"_"+distinctArray[i].trim()));
					}
				}
			}
		}
		return columnIds;
	}
	
	public int getDependentId(String dependentKey) {
		String dependentColumn = conf.get(dependentKey);
		return newColumnNames.indexOf(dependentColumn);
	}
	
	public int getConfigInt(String Key) {
		String value = conf.get(Key);
		if(value!=null){
			return Integer.parseInt(value);
		}
		return -1;
	}
	
	public double getConfigDouble(String Key) {
		String value = conf.get(Key);
		return Double.parseDouble(value);
	}
	
	public String getConfigString(String Key) {
		String value = conf.get(Key);
		return value;
	}
	
	
	public String[] getConfigArray(String arrayKey){
		String value = conf.get(arrayKey);
		if(value==null) {
			return new String[0];
		}
		else{
			if(value.startsWith("[")){
				value=value.substring(1);
			}
			if(value.endsWith("]")){
				value=value.substring(0,value.length()-1);
			}
			return value.split(",");
		}
	}


	//all bad data should be stastic in map period so it's a Mapper.Context
	public boolean cleanUpAlpineHadoopMap(Context context) {
//		dirtyFileName = taskid.getTaskID().toString();
//		Path path = new Path(dirtyPath + "/" + dirtyFileName);
//		try {
//			if (fs.exists(path)) {
//				fs.delete(path, true);
//			}
//			out = fs.create(path);
//			for (String dirtyLine : dirtyDataList) {
//				out.write(dirtyLine.getBytes("UTF-8"));
//				out.write("\r\n".getBytes("UTF-8"));
//			}
//			out.flush();
			context.getCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER,AlpineHadoopConfKeySet.TYPE_NOT_MATCH).increment(badDataCount);
			badDataCount = 0;
			//			dirtyDataList.clear();
			
//			out.close();
//			return true;
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return false;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
		return true;
	}

	
	public String generateOutputLine(Object[] columnValues) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < columnValues.length; i++) {
			String candidate = "";
			if (columnValues[i] == null) {
				candidate = "null";
			}
			else{
				candidate = columnValues[i].toString();
			}
			if ("chararray".equalsIgnoreCase(columnTypes[i])
					|| "bytearray".equalsIgnoreCase(columnTypes[i])) {
				int quoteIndex = candidate.indexOf('"');
				if (quoteIndex == -1) {
					candidate = candidate.replaceAll("\\n", " ");
					if(candidate.indexOf(",")!=-1){
						candidate = '"' + candidate + '"';
					}
				} else {
					candidate = candidate.replaceAll("\\n", " ");
					if(candidate.indexOf(",")!=-1){
						candidate = '"' + candidate + '"';
					}
				}
			}
			if (i == 0) {
				result.append(candidate);
			} else {
				result.append(",");
				result.append(candidate);
			}
		}
		return result.toString();
	}
	
	public int getIdSize(List<Integer> ids,List<HadoopInteractionItem> items){
		int size=ids.size();
		for(HadoopInteractionItem item:items){
			size++;
			if("*".equals(item.getInteractionType())){
				if(ids.contains(item.getLeftId())==false){
					size++;
				}
				if(ids.contains(item.getRightId())==false){
					size++;
				}
			}
		}
		return size;
	}
	
	public String[] projectNewColumnValue(String[] columnValues){
		List<String> transformResult=new ArrayList<String>();
		for(int i=0;i<columnValues.length;i++){
			String[] distinctValues=distinctMap.get(columnNames[i]);
			if(distinctValues==null){
				transformResult.add(columnValues[i]);
			}
			else{
				if(distinctValues.length==0){
					transformResult.add(columnValues[i]);
				}
				else{
					for(int j=0;j<distinctValues.length-1;j++){
						if(distinctValues[j].trim().equals(columnValues[i])){
							transformResult.add("1");
						}
						else{
							transformResult.add("0");
						}
					}
				}
			}
		}
		return transformResult.toArray(new String[transformResult.size()]);
	}
	
	public int getIndex(String column){
		if(column==null){
			throw new NullPointerException();
		}
		if(column.startsWith("[")){
			column=column.substring(1);
		}
		if(column.endsWith("]")){
			column=column.substring(0,column.length()-1);
		}
		column=column.replaceAll(AlpineHadoopConstants.SPECIAL_SEP_STRING, ",");
		int indexOf=newColumnNames.indexOf(column);
		if(indexOf==-1){
			try{
				return Integer.parseInt(column);//already a column index
			}
			catch(Exception e){
				return -1;
			}
		}
		else{
			return indexOf;
		}
	}

	public String getHeaderLineValue() {
		return headerLineValue;
	}
}
