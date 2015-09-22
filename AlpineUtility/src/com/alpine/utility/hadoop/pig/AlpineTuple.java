package com.alpine.utility.hadoop.pig;

import java.util.ArrayList;
import java.util.List;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.google.gson.Gson;



public class AlpineTuple{
	private String fieldsJson;
	private String tupleClassName;
	private Integer level;
	
	public AlpineTuple(Tuple t) throws ExecException{
		if(null==t){
			throw new IllegalArgumentException("Tuple that is suppose to be converted into AlpineTuple is null");
		}
		this.tupleClassName=t.getClass().getCanonicalName();
		makeSureOneOrNoneTupleExistAndSetLevel(t);
		acquireFieldStrings(t);
		
	}
	

	private void acquireFieldStrings(Tuple t) throws ExecException {
		if(1==level){
			t=(Tuple)t.get(0);
		}
		List<String>fields = new ArrayList<String>();
		Gson gson=new Gson();
		for(int i=0;i<t.size();i++){
			fields.add(gson.toJson(new AlpineTupleField(t,i)));
		}
		fieldsJson=gson.toJson(fields);
			
	}
	
	private static boolean isColumnBagOrTuple(Tuple t,int i) throws ExecException{
		return (DataType.TUPLE==t.getType(i)||
				DataType.BAG==t.getType(i));
	}
	private void makeSureOneOrNoneTupleExistAndSetLevel(Tuple t) throws ExecException {
		if(t.size()==1&&isColumnBagOrTuple(t, 0)){
			level=1;
			return;
		}
		level=0;
		for(int i=0;i<t.size();i++){
			if(isColumnBagOrTuple(t, i)){
				throw new IllegalArgumentException("Tuple has other tuples or bag and currently we don't support that");
			}
		}
		
	}
	
	public String getFieldsJson() {
		return fieldsJson;
	}

	public void setFieldsJson(String fieldsJson) {
		this.fieldsJson = fieldsJson;
	}

	public String getTupleClassName() {
		return tupleClassName;
	}

	public void setTupleClassName(String tupleClassName) {
		this.tupleClassName = tupleClassName;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

}
