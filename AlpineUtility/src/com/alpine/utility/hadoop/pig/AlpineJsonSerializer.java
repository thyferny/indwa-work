package com.alpine.utility.hadoop.pig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.AmendableTuple;
import org.apache.pig.data.BinSedesTuple;
import org.apache.pig.data.DataType;
import org.apache.pig.data.DefaultAbstractBag.BagDelimiterTuple;
import org.apache.pig.data.DefaultAbstractBag.EndBag;
import org.apache.pig.data.DefaultAbstractBag.StartBag;
import org.apache.pig.data.DefaultTuple;
import org.apache.pig.data.TargetedTuple;
import org.apache.pig.data.TimestampedTuple;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.pen.util.ExampleTuple;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class AlpineJsonSerializer {
	private static TupleFactory mTupleFactory = TupleFactory.getInstance();
	private static final Map<String,Class> typesMap;
	private static final Map<Byte,Class> tupleClassMap;
	static{
		typesMap=new HashMap<String, Class>();
		typesMap.put(EndBag.class.getCanonicalName(),EndBag.class);
		typesMap.put(StartBag.class.getCanonicalName(), StartBag.class);
		typesMap.put(BagDelimiterTuple.class.getCanonicalName(),BagDelimiterTuple.class);
		typesMap.put(AmendableTuple.class.getCanonicalName(), AmendableTuple.class);
		typesMap.put(BinSedesTuple.class.getCanonicalName(),BinSedesTuple.class);
		typesMap.put(TimestampedTuple.class.getCanonicalName(),TimestampedTuple.class);
		typesMap.put(DefaultTuple.class.getCanonicalName(),DefaultTuple.class);
		typesMap.put(ExampleTuple.class.getCanonicalName(), ExampleTuple.class);
		typesMap.put(TargetedTuple.class.getCanonicalName(),TargetedTuple.class);
		//[TODO] make sure to have all the types
		tupleClassMap=new HashMap<Byte,Class>();
		tupleClassMap.put(DataType.BOOLEAN, Boolean.class);
		tupleClassMap.put(DataType.BYTE,Byte.class );
		tupleClassMap.put(DataType.BYTEARRAY,Byte[].class);
		tupleClassMap.put(DataType.CHARARRAY,char[].class);
		tupleClassMap.put(DataType.DOUBLE,Double.class);
		tupleClassMap.put(DataType.FLOAT, Float.class);
		tupleClassMap.put(DataType.INTEGER, Integer.class);
		tupleClassMap.put(DataType.LONG, Long.class);
		tupleClassMap.put(DataType.UNKNOWN,null);
		
		//{100=MAP, 5=BOOLEAN, 6=BYTE, 110=TUPLE, 10=INTEGER, 15=LONG, 50=BYTEARRAY, 55=CHARARRAY, 20=FLOAT, 25=DOUBLE, 127=INTERNALMAP, 123=GENERIC_WRITABLECOMPARABLE, 120=BAG, 60=BIGCHARARRAY}
		//
		
		
	}
	
	private static Object doTheCastingForAlpineFieldValue(AlpineTupleField af){
		if(null==af||DataType.NULL==af.getTypeByte()){
			return null;
		}
		Class instance = tupleClassMap.get(af.getTypeByte());
		
		if( instance.equals(Boolean.class) ){
			return (Boolean)af.getFieldValue();
		}else if( instance.equals(Byte.class) ){
			return (Byte)af.getFieldValue();
		}else if( instance.equals(Byte[].class) ){
			return (Byte[])af.getFieldValue();
		}else if( instance.equals(char[].class) ){
			if(af.getFieldValue() instanceof char[]){
				return (String)af.getFieldValue();
			}
			return (String)af.getFieldValue();
		}else if( instance.equals(Double.class) ){
			if(af.getFieldValue() instanceof BigDecimal){
				BigDecimal bd = (BigDecimal)af.getFieldValue();
				return bd.doubleValue();
			}
			return (Double)af.getFieldValue();
		}else if( instance.equals(Float.class) ){
			return (Float)af.getFieldValue();
		}else if( instance.equals(Integer.class) ){
			return (Integer)af.getFieldValue();
		}else if( instance.equals(Long.class) ){
			if(af.getFieldValue() instanceof Integer){
				Integer i = (Integer)af.getFieldValue();
				return new Long(i);
			}
			return (Long)af.getFieldValue();
		}
		
		throw new IllegalArgumentException("["+af+"]has the type that is not recognized");
		
		
	}
	
	private static Tuple generateTupleByGivenType(String tupleClassName,
			int size, int level) throws ExecException {
		if (level > 1 || level < 0) {
			throw new IllegalArgumentException(
					"Level can be either 0 or one yet it is[" + level + "]");
		}
		if (0 == level) {
			return mTupleFactory.newTuple(size);
		}

		Tuple p = mTupleFactory.newTuple(1);
		Tuple tChild = mTupleFactory.newTuple(size);
		p.set(0, tChild);
		return p;

	}
	
	public static String serilizeTupleIntoAlpineTuple(Tuple t) throws ExecException{
		AlpineTuple at= new AlpineTuple(t);
		return new Gson().toJson(at);
		
	}
	
	public static Tuple desirilizeAlpineJsonStringIntoTuple(String at) throws ExecException{
		if(null==at||at.trim().isEmpty()){
			return null;
		}
		Gson gson=new Gson();
		AlpineTuple at2=gson.fromJson(at, AlpineTuple.class);
		String fj=at2.getFieldsJson();
		String tupleClassName=at2.getTupleClassName();
		List<String> fields=gson.fromJson(fj, new TypeToken<List<String>>(){}.getType());
		Tuple t=generateTupleByGivenType(tupleClassName,fields.size(),at2.getLevel());
		Tuple dataTuple=0==at2.getLevel()?t:(Tuple)t.get(0);
		for(String f:fields ){
			AlpineTupleField af=gson.fromJson(f, AlpineTupleField.class);
			dataTuple.set(af.getColumnNumber(), doTheCastingForAlpineFieldValue(af));
		}
		return t;
	}
	
	public static String serializeTupleIteratorToJSon(Iterator<Tuple> it) throws ExecException{
		Gson gson=new Gson();
		List<String> ser=new ArrayList<String>();
		while(it.hasNext()){
			ser.add(serilizeTupleIntoAlpineTuple(it.next()));
		}
		return gson.toJson(ser);
	}
	
	
	public static Iterator<Tuple> deSerializeTupleIteratorToJSon(String ser) throws ExecException{
		if(null==ser||ser.trim().isEmpty()){
			return null;
		}
		Gson gson=new Gson();
		Type listType = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> itList= gson.fromJson(ser, listType);
		List<Tuple> tuples=new ArrayList<Tuple>();
		
		for(String st:itList){
			tuples.add(desirilizeAlpineJsonStringIntoTuple(st));
			
		}
		return tuples.iterator();
	}
	
	
	//Below is just play ground and I hide them by declaring them as private
	
	private static void someGsonTutorials(){
		Gson gson=new Gson();
		Collection<Integer> ints = new ArrayList<Integer>();//Lists.immutableList(1,2,3,4,5);
		ints.add(1);ints.add(2);ints.add(3);ints.add(4);
		String json = gson.toJson(ints);
		Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
		Collection<Integer> ints2 = gson.fromJson(json, collectionType);
		
		
	}
	
	
	
	private  static String convertIntoJSon(Tuple t) throws IOException {
		Gson gson=new Gson();
		AlpineTuple at= new AlpineTuple(t);
		String alpineTupleString=gson.toJson(at);
		AlpineTuple at2=gson.fromJson(alpineTupleString, AlpineTuple.class);
//		Type tupleType=AlpineTuple.getTupleType(at2.getTupleClass());
//		Tuple serDesTuple=gson.fromJson(at2.getJsonString(), tupleType);
		
		return gson.toJson(t);
	}

	private  static String convertFromJSon(String jsonString) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream("employee.ser");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Tuple t = (Tuple) in.readObject();
		in.close();
		fileIn.close();

		return "";
	}
	
	private  List<Tuple> readJsonStream(InputStream in) throws IOException {
		 	Gson gson=new Gson();
	        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	        List<Tuple> messages = new ArrayList<Tuple>();
	        reader.beginArray();
	        while (reader.hasNext()) {
	        	Tuple message = gson.fromJson(reader, Tuple.class);
	            messages.add(message);
	        }
	        reader.endArray();
	        reader.close();
	        return messages;
	    }
	 
	private  static String convertTupleToJson(Tuple t){
		Gson gson = new Gson();
		String json=gson.toJson(t);
		return json;
		
	}
	private  static Tuple convertTupleFromJson(String json){
		Gson gson = new Gson();
		Tuple tuple=gson.fromJson(json,Tuple.class);
		return tuple;
		
	}
	
	public static void main(String[] args) {

	}



	private  static String convertTupleToJson(Iterator<Tuple> it) {
		List<String> as=new ArrayList<String>();
		Gson gson=new Gson();
		while(it.hasNext()){
			as.add(gson.toJson(it.next()));
		}
		return gson.toJson(as);
	}
	
	private  static Iterator<Tuple> convertJsonToTupeIterator(String json) {
		Gson gson=new Gson();
		Type collectionType = new TypeToken<List<String>>(){}.getType();
		List<String> tupleStrings = gson.fromJson(json, collectionType);
		
		List<Tuple> lt=new ArrayList<Tuple>();
		for(String st:tupleStrings){
			Tuple t=convertTupleFromJson(st);
			lt.add(t);
		}
		
		
		return lt.iterator();
	}

}
