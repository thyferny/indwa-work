package com.alpine.hadoop.union;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.alpine.hadoop.UnionKeySet;
import com.alpine.hadoop.util.DataPretreatUtility;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author John Zhao
 * 
 */

public class UnionMapper extends
		Mapper<LongWritable, Text, Text, Text> {
	
 
	MapReduceHelper helper;
	public static final String UNION = "UNION";
	public static final String UNION_ALL="UNION ALL";
	public static final String INTERSECT= "INTERSECT";
	public static final String EXCEPT ="EXCEPT";
  	Text txtKeyWriter=new Text();
  	Text txtValueWriter=new Text();

	private String unionType;
	 
	List<String> outputNames    ;
	List<String> outputTypes  ;
	private List<String> inputFiles;
 
	private HashMap<String,String> realFileMap;


	private ArrayList<List<Integer>> inputColumnIndexList;
	private HashMap<String, String> headerMap=null;
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String input = ((FileSplit)context.getInputSplit()).getPath().toString();
		//   hdfs://alpine-oracle/csv/golfnew.csv - > /csv/golfnew.csv 
		if(input.indexOf("hdfs:")==0){
			input = input.substring("hdfs://".length(),input.length()) ;
		}
		input = input.substring(input.indexOf("/"),input.length()) ;
				
		int index = inputFiles.indexOf(input) ;
		List<Integer> indexList = inputColumnIndexList.get(index) ;
		try{
			if (headerMap.containsKey(input) &&headerMap.get(input)  != null&&
					(value == null || value.toString().trim().length() == 0
					|| headerMap.get(input) .equals(value.toString() ))) {
 				return  ;
			}
		  List<String[]> lines = helper.getRecordParser().parse(value.toString());
		  if(lines==null||lines.size()==0){
				return ;
			}
			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
				String[] lineValue = (String[]) iterator.next(); 
				String [] selectedLineValue= filterValueWithTypeCheck(lineValue,indexList ,outputTypes);
				if(selectedLineValue!=null){
					mapLine(selectedLineValue , context ,realFileMap.get(input )) ;
				}
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	
 
	}

	private String[] filterValueWithTypeCheck(String[]  row,
			List<Integer> indexList, List<String> outputTypes) {
		
 	 
				String [] transformedRow =  this.projectNewColumnValue(row,indexList);
				 
				if (isTypeMatch(transformedRow, outputTypes,indexList)) {
					return transformedRow;
				} else {
 
					helper.dirtyAdd(row);
					return null;
				}
	 
	}
	private boolean isTypeMatch(String[] transformedRow,
			List<String> outputTypes, List<Integer> indexList) {
		if (transformedRow.length == indexList.size()) {
		 
				for (int i = 0; i < transformedRow.length; i++) {
					if (!DataPretreatUtility.checkType(transformedRow[i],
							outputTypes.get( indexList.get(i)))) {
						return false;
					}
				}
				return true;
		 
		}else{
			return false;
		}
	}

	public String[] projectNewColumnValue(String[] columnValues,List<Integer> indexList ){
		List<String> transformResult=new ArrayList<String>();
		for(int i = 0;i<indexList.size();i++){
			int index = indexList.get(i) ;
			transformResult.add(columnValues[index]);
		}
		
		return transformResult.toArray(new String[transformResult.size()]);
	}

	private void mapLine(String[] lineValue,Context context ,String inputFile ) throws IOException, InterruptedException {
		txtKeyWriter.set(helper.generateOutputLine(lineValue) ) ;
		
		if(UNION_ALL.equals(unionType)){ 
			context.write(txtKeyWriter, null) ;
		}else {
			txtValueWriter.set(inputFile) ;
			
			context.write(txtKeyWriter, txtValueWriter) ;

			
		}
		
	}

	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
		 
	}
	
	@Override
	public void setup(Context context) {
		Configuration config = context.getConfiguration();
		helper = new MapReduceHelper(config,
				context.getTaskAttemptID());
 		 outputNames =  Arrays.asList(config.get(UnionKeySet.COLUMN_NAMES).split(","));
		  outputTypes =  Arrays.asList(config.get(UnionKeySet.COLUMN_TYPES).split(","));

 		unionType = config.get ( UnionKeySet.union_type );
		 
		 inputFiles =  Arrays.asList(config.get(UnionKeySet.union_input_files).split(","));
		 List<String> inputRealFiles =  Arrays.asList(config.get(UnionKeySet.union_input_real_files).split(":"));
		  realFileMap = new HashMap<String,String> ();
		 for (int i = 0; i < inputRealFiles.size(); i++) {
			
		 
			String realFiles = (String) inputRealFiles.get(i);
			String[] rFiles = realFiles.split(",") ;
			for (int j = 0; j < rFiles.length; j++) {
				realFileMap.put(rFiles[j], inputFiles.get(i)) ;

			}
		}
		 

		 inputColumnIndexList = new ArrayList<List<Integer>>() ;
		//file1column1,file1column2:file2column1,file2columns2,
		String[] fileColumns = config.get(UnionKeySet.union_input_column_index).split(":");
		for (int i = 0; i < fileColumns.length; i++) {
			List<Integer> indexList = new ArrayList<Integer>();
			List<String> strList =Arrays.asList(fileColumns[i].split(","));
			for (Iterator<String> iterator = strList.iterator(); iterator.hasNext();) {
				String index = (String) iterator.next();
				indexList.add(Integer.parseInt(index)) ;
			}
 			inputColumnIndexList.add(indexList)  ;
		}
		int inputNumbers = inputFiles.size();
		headerMap = new HashMap<String,String>();
		for (int i = 0; i < inputNumbers; i++) {
			if(config.get(UnionKeySet.union_input_headerline+"_"+i)!=null){
				headerMap.put( inputFiles.get(i) , config.get(UnionKeySet.union_input_headerline+"_"+i)) ;
			}
			
		}
		

	}
}