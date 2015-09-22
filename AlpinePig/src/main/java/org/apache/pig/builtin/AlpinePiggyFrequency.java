package org.apache.pig.builtin;

import static com.alpine.hadoop.pig.AlpinePigConstants.ALPINE_PIGGY_FREQUENCY_MAP_PREFIX;
import static com.alpine.hadoop.pig.AlpinePigConstants.BIN_SPLITTER;
import static com.alpine.hadoop.pig.AlpinePigConstants.COLUMN_SPLITTER;
import static com.alpine.hadoop.pig.AlpinePigConstants.MAP_SPLITTER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.pig.Accumulator;
import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigMapReduce;
import org.apache.pig.data.BinSedesTuple;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.SingleTupleBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class AlpinePiggyFrequency extends EvalFunc<Tuple> implements
		Algebraic, Accumulator<Tuple> {
	private static final Long MAX_ALLOWED_GROUP_SIZE = 100L+2;
	private static final Integer MINUS_ONE_INT=-1;
	private static TupleFactory mTupleFactory = TupleFactory.getInstance();

	private static Tuple initTuple(int numberOfColumns) throws ExecException {
		Tuple tuple = mTupleFactory.newTuple(numberOfColumns);
		// First we make everything to be zero
		for (int i = 0; i < tuple.size(); i++) {
			tuple.set(i, Integer.valueOf(-1));
		}
		return tuple;
	}
	
	
	private ArrayList<Boolean> tupleTypes;

	@Override
	public Tuple exec(Tuple input) throws IOException {
		return input;
	}

	private static List<String> fetchTheColumns(Tuple row) throws ExecException{
		String rowStr = ("Row is[" + row + "] and it's class is["
				+ row.getClass() + "]");
		try {

			List<String> columns = new ArrayList<String>();
			Tuple t=null;
			t = row;
			if(t.get(0) instanceof DefaultDataBag){
				DataBag db=(DataBag)t.get(0);
				t=db.iterator().next();
			}
			
			for (int i = 0; i < t.size(); i++) {
				columns.add(DataType.toString(t.get(i)));

			}
			return columns;
		} catch (ExecException e) {
			throw new ExecException(e);
		}
	}
	private static Tuple findBinLocationOfEachColumn(Tuple input) throws ExecException {
		
		Object o=input.get(0);
		Tuple t0=null;
		Tuple row=input;
		
		if(o instanceof SingleTupleBag){
			SingleTupleBag stb=(SingleTupleBag)o;
			t0=stb.iterator().next();
		}else if(o instanceof BinSedesTuple){
			BinSedesTuple stb=(BinSedesTuple)o;
			t0=stb;
		}
		else if(o instanceof Tuple){
			t0=(Tuple)o;
		}else if(o instanceof DefaultDataBag){
			t0=((DefaultDataBag)o).iterator().next();
		}
		
		int numberOfTheColumns = t0.size();
		Tuple vaTuple = initTuple(numberOfTheColumns);

		List<String> columns=fetchTheColumns(t0);
		for(int rowNumber=0;rowNumber<numberOfTheColumns;rowNumber++){
			String data=columns.get(rowNumber);

			if(null==data){
				continue;
			}
			
			vaTuple.set(rowNumber, data);
		}
		return vaTuple;
	}
	
	@Override
	public String getInitial() {
		return Initial.class.getName();
	}

	@Override
	public String getIntermed() {
		return Intermediate.class.getName();
	}

	@Override
	public String getFinal() {
		return Final.class.getName();
	}

	
	/* Accumulator interface implementation */
	private Tuple intermediateCount;

	@Override
	public void accumulate(Tuple b) throws IOException {
		try {
			DataBag bag = (DataBag) b.get(0);
			Iterator<Tuple> it = bag.iterator();
			while (it.hasNext()) {
				Tuple t = (Tuple) it.next();
				if (t != null && t.size() > 0) {
					t=combineBins(t);
				}
			}
		} catch (ExecException ee) {
			throw ee;
		} catch (Exception e) {
			int errCode = 2106;
			String msg = "Error while computing min in "
					+ this.getClass().getSimpleName();
			throw new ExecException(msg, errCode, PigException.BUG, e);
		}
	}

	@Override
	public void cleanup() {
		intermediateCount = mTupleFactory.newTuple(4);
	}

	@Override
	public Tuple getValue() {
		return intermediateCount;
	}

	static public class Initial extends EvalFunc<Tuple> {
		@Override
		public Tuple exec(Tuple input) throws IOException {
			return input;
		}

	}

	static public class Intermediate extends EvalFunc<Tuple> {
		
		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				return combineBins(input);
			} catch (ExecException ee) {
				throw ee;
			} catch (Exception e) {
				int errCode = 2106;
				String msg = "Error while   --- >NHcomputing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, e);
			}
		}
	}
	static public class Final extends EvalFunc<Tuple> {
		
		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				return combineBins(input);
			} catch (Exception ee) {
				int errCode = 2106;
				String msg = "Error while computing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}

	}
	
	public static Tuple combineBins(Tuple input) throws ExecException {
		Map<Integer,SortedMap<String,Integer>> map=new TreeMap<Integer, SortedMap<String,Integer>>();
		DataBag db = (DataBag)input.get(0);
		Iterator<Tuple> it = db.iterator();
		
//		if(it.hasNext()&& it.next().get(0) instanceof DataBag ){
//			Tuple tt=it.next();
//			db=(DataBag)tt.get(0);
//			it = db.iterator();
//		}
		
		while(it.hasNext()){
			Tuple t2=(Tuple)it.next();
			List<String> columns = fetchTheColumns(t2);
			for(int i=0;i<columns.size();i++){
				SortedMap<String, Integer> colMap = map.get(i);
				if(null==colMap){
					colMap=new TreeMap<String,Integer>();
					map.put(i, colMap);
				}
				
				if(null!=columns.get(i)&&columns.get(i).startsWith(ALPINE_PIGGY_FREQUENCY_MAP_PREFIX)){
					String mapString=columns.get(i);
					String clearedMapString=mapString.substring(ALPINE_PIGGY_FREQUENCY_MAP_PREFIX.length(),mapString.length());
					Map<Integer,SortedMap<String,Integer>> desiralizedMap = desiralizeTheMap(clearedMapString);
					mergeTopMaps(map,desiralizedMap);
					
				}else{
					String c=columns.get(i);
					if(null==c){
						c="";
					}
					Integer cc=colMap.get(c);
					colMap.put(c,(null==cc?1:cc+1));
					//If map reaches to max size that we allow we clean it up and mark it
					ifMapMaxedOutCleanAndMarkIt(colMap);
					
				}
				
			}
			
			
		}
		
		String mapString=serilazeTheMap(map);
		Tuple mt=mTupleFactory.newTuple(mapString);
		return mt;
	}

	private static boolean ifMapMaxedOutCleanAndMarkIt(SortedMap<String, Integer> colMap) {
		Long distinctCount = acquireMaxDistinctCount();
		
		if(colMap.size()>=distinctCount){
			String lastKey=colMap.lastKey();
			colMap.remove(lastKey);
			return true;
		}
		return false;
	}

	private static void mergeTopMaps(Map<Integer, SortedMap<String, Integer>> map,
			Map<Integer, SortedMap<String, Integer>> desiralizedMap) {
		Set<Integer> columnNumbers=desiralizedMap.keySet();
		for(Integer columnNumber:columnNumbers){
			mergeTheMaps(columnNumber,map,desiralizedMap.get(columnNumber));
		}

	}

	public static SortedMap<Integer,SortedMap<String,Integer>> desiralizeTheMap(String strMap) throws ExecException{
		SortedMap<Integer,SortedMap<String,Integer>> map =new TreeMap<Integer, SortedMap<String,Integer>>();
		String[] maps=strMap.split(MAP_SPLITTER);
		if(null==maps){
			return map;
		}
		for(String m:maps){
			String[] cn=m.split(COLUMN_SPLITTER);
			if(null==cn||2!=cn.length){
				throw new ExecException("Got Wrong number of columns for ["+m+"]");
			}
			String[] binCounts = cn[1].split(BIN_SPLITTER);
			Integer columnNumber =Integer.parseInt(cn[0]);
			if(null==binCounts||0!=binCounts.length%2){
				throw new ExecException("xGot Wrong number of bin counts for ["+cn[1]+"] for the Map string of["+strMap+"]");
			}
			SortedMap<String,Integer> binMap=new TreeMap<String,Integer>();
			for(int i=0;i<binCounts.length/2;i++){
				binMap.put(binCounts[2*i]+"", Integer.parseInt(binCounts[2*i+1]));
			}
			mergeTheMaps(columnNumber,map,binMap);
		}
		
		
		
		return map;
	}
	private static void mergeTheMaps(Integer columnNumber,
			Map<Integer, SortedMap<String, Integer>> map,
			SortedMap<String, Integer> binMap) {
		SortedMap<String, Integer> cm = map.get(columnNumber);
		if(null==cm){
			map.put(columnNumber, binMap);
			return;
		}
		
		Set<String> binIDs = binMap.keySet();
		for(String bID:binIDs){
			Integer count = binMap.get(bID);
			Integer pc=cm.get(bID);
			count=(null==pc?count:pc+count);
			cm.put(bID, count);
			ifMapMaxedOutCleanAndMarkIt(cm);
		}
		
	}

	private static String serilazeTheMap(Map<Integer,SortedMap<String,Integer>> map) {
		StringBuilder sb = new StringBuilder();
		Set<Integer> ck=map.keySet();
		for(Integer co:ck){
			sb.append(co).append(COLUMN_SPLITTER);
			Map<String, Integer> binMap = map.get(co);
			Set<String> binKeys = binMap.keySet();
			boolean addCommaFront=false;
			for(String bk:binKeys){
				Integer binContentCount=binMap.get(bk);
				if(!addCommaFront){
					addCommaFront=true;
				}else{
					sb.append(BIN_SPLITTER);
				}
				sb.append(bk).append(BIN_SPLITTER).append(binContentCount);
			}
			sb.append(MAP_SPLITTER);
		}
		String ser=ALPINE_PIGGY_FREQUENCY_MAP_PREFIX+sb.toString();
		
		return ser.substring(0,ser.length()-MAP_SPLITTER.length());
	}
	
	private static synchronized Long acquireMaxDistinctCount() {
		try {
			String maxCount = PigMapReduce.sJobConfInternal.get().get(
					"va_distinct_value_count");
			if (null != maxCount) {

				Long maxDistinctCount = Long.parseLong(maxCount);
				if (null == maxDistinctCount | maxDistinctCount < 0) {
					return MAX_ALLOWED_GROUP_SIZE;
				}
				return maxDistinctCount;

			}
		} catch (Throwable e) {
			// We ignore the exception and just return default value
		}
		return MAX_ALLOWED_GROUP_SIZE;
	}

}