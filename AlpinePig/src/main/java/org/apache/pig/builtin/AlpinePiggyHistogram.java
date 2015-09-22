package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pig.Accumulator;
import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.BinSedesTuple;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.SingleTupleBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import static com.alpine.hadoop.pig.AlpinePigConstants.*;
public class AlpinePiggyHistogram extends EvalFunc<Tuple> implements
		Algebraic, Accumulator<Tuple> {
	private static Log itsLogger = LogFactory.getLog(AlpinePiggyHistogram.class );
	private static TupleFactory mTupleFactory = TupleFactory.getInstance();

	private static Tuple initTuple(int numberOfColumns) throws ExecException {
		Tuple tuple = mTupleFactory.newTuple(numberOfColumns);
		// First we make everything to be zero
		for (int i = 0; i < tuple.size(); i++) {
			tuple.set(i, null);
		}
		return tuple;
	}
	
	
	private ArrayList<Boolean> tupleTypes;

	@Override
	public Tuple exec(Tuple input) throws IOException {
		try {
			return findBinLocationOfEachColumn(input);
		} catch (ExecException ee) {
			itsLogger.error(ee);
			throw ee;
		} catch (Exception e) {
			itsLogger.error(e);
			int errCode = 2106;
			String msg = "Error while computing AlpinePigyValueAnalyzerDeviationCalculator "
					+ this.getClass().getSimpleName();
			throw new ExecException(msg, errCode, PigException.BUG, e);
		}
	}

	private static List<Double> fetchTheColumns(Tuple row) throws ExecException{
		String rowStr = ("Row is[" + row + "] and it's class is["
				+ row.getClass() + "]");
		try {

			List<Double> columns = new ArrayList<Double>();
			Tuple t=null;
			t = row;
			
			
			for (int i = 0; i < t.size(); i++) {
				columns.add(DataType.toDouble(t.get(i)));

			}
			return columns;
		} catch (ExecException e) {
			itsLogger.error(rowStr, e);
			throw new ExecException(e);
		}
	}
	private static Tuple findBinLocationOfEachColumn(Tuple input) throws ExecException {
		if(itsLogger.isDebugEnabled()){
			itsLogger.info("Tuple is["+input+"] and it's class is["+input.getClass()+"]");
		}
		
		Object o=input.get(0);
		Tuple t0=null;
		Tuple row=input;
		
		
		if(o instanceof SingleTupleBag){
			SingleTupleBag stb=(SingleTupleBag)o;
			t0=stb.iterator().next();
		}else if(o instanceof BinSedesTuple){
			BinSedesTuple stb=(BinSedesTuple)o;
			t0=((DataBag)stb.get(0)).iterator().next();
			row=stb;
		}
		else if(o instanceof Tuple){
			t0=(Tuple)o;
		}else if(o instanceof DefaultDataBag){
			t0=((DefaultDataBag)o).iterator().next();
		}else{
			String err="Tuple is["+input+"] and it's class is["+input.getClass()+"]however object["+o+"]with the class of["+o.getClass()+"]is not expected as this type";
			itsLogger.error(err);
			throw new ExecException(err);
		}
		
		int numberOfTheColumns = t0.size();
		Tuple vaTuple = initTuple(numberOfTheColumns);

		List<Double> columns=fetchTheColumns(t0);
		for(int rowNumber=0;rowNumber<numberOfTheColumns;rowNumber++){
			Double data=columns.get(rowNumber);

			if(null==data){
				continue;
			}
			
			
			double min=DataType.toDouble(row.get(rowNumber*3+1));
			if(data<min){
				continue;
			}
			double max=DataType.toDouble(row.get(rowNumber*3+2));
			if(data>max){
				continue;
			}
			double stepSize=DataType.toDouble(row.get(rowNumber*3+3));
			
			
			int binLocation= (int)Math.floor(0.99999*(DataType.toDouble(columns.get(rowNumber))-min)/stepSize);
			vaTuple.set(rowNumber, binLocation);
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

	// private Map<Double,Double> distincts = new HashMap<Double,Double>();

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
			itsLogger.error(e);
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
		private static Log itsLogger = LogFactory.getLog(Initial.class );

		@Override
		public Tuple exec(Tuple input) throws IOException {
			try{
				if(itsLogger.isDebugEnabled()){
					itsLogger.info("Will try on the data of["+input+"]");
				}
				return findBinLocationOfEachColumn(input);
			}catch(Throwable e){
				itsLogger.error("Failed on the tuple of["+input+"]with class type of["+input.getClass()+"] and the error message is",e);
				throw new IOException("Failed on the tuple of["+input+"]with class type of["+input.getClass()+"] and the error message is",e);
			}
		}

	}

	static public class Intermediate extends EvalFunc<Tuple> {
		private static Log itsLogger = LogFactory.getLog(Intermediate.class );

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
		private static Log itsLogger = LogFactory.getLog(Final.class );

		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				return combineBins(input);
			} catch (Exception ee) {
				itsLogger.error(ee);
				int errCode = 2106;
				String msg = "Error while computing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}

	}
	
	public static Tuple combineBins(Tuple input) throws ExecException {
		Map<Integer,Map<Integer,Integer>> map=new TreeMap<Integer, Map<Integer,Integer>>();
		DataBag db = (DataBag)input.get(0);
		Iterator<Tuple> it = db.iterator();
		Object o=null;
		while(it.hasNext()){
			Tuple t=(Tuple)it.next();
			for(int i=0;i<t.size();i++){
				o=t.get(i);
				Map<Integer, Integer> colMap = map.get(i);
				if(null==colMap){
					colMap=new TreeMap<Integer,Integer>();
					map.put(i, colMap);
				}
				if(null==o){
					continue;
				}
				else if(o instanceof Map){
					Map<Integer,Map<Integer,Integer>> topMap = (Map<Integer,Map<Integer,Integer>>)o;
					Map<Integer,Integer> bins = topMap.get(i);
					Set<Integer> binIds=bins.keySet();
					for(Integer k:binIds){
						Integer cc=colMap.get(k);
						Integer c=bins.get(k);
						colMap.put(k,(null==cc?c:c+cc));
					}
				}else if(o instanceof String){
					Map<Integer,Map<Integer,Integer>> desiralizedMap = desiralizeTheMap((String)o);
					mergeTopMaps(map,desiralizedMap);
					itsLogger.info(desiralizedMap);
				}else{
					Integer c=DataType.toInteger(o);
					Integer cc=colMap.get(c);
					colMap.put(c,(null==cc?1:cc+1));
				}
				
			}
			
			
		}
		String mapString=serilazeTheMap(map);
		Tuple mt=mTupleFactory.newTuple(mapString);
		return mt;
	}
	private static void mergeTopMaps(Map<Integer, Map<Integer, Integer>> map,
			Map<Integer, Map<Integer, Integer>> desiralizedMap) {		
		Set<Integer> columnNumbers=desiralizedMap.keySet();
		for(Integer columnNumber:columnNumbers){
			mergeTheMaps(columnNumber,map,desiralizedMap.get(columnNumber));
		}

	}

	public static Map<Integer,Map<Integer,Integer>> desiralizeTheMap(String strMap) throws ExecException{
		Map<Integer,Map<Integer,Integer>> map =new TreeMap<Integer, Map<Integer,Integer>>();
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
				throw new ExecException("Got Wrong number of bin counts for ["+cn[1]+"]");
			}
			Map<Integer,Integer> binMap=new HashMap<Integer,Integer>();
			for(int i=0;i<binCounts.length/2;i++){
				binMap.put(Integer.parseInt(binCounts[2*i]), Integer.parseInt(binCounts[2*i+1]));
			}
			mergeTheMaps(columnNumber,map,binMap);
		}
		
		
		
		return map;
	}
	private static void mergeTheMaps(Integer columnNumber,
			Map<Integer, Map<Integer, Integer>> map,
			Map<Integer, Integer> binMap) {
		Map<Integer, Integer> cm = map.get(columnNumber);
		if(null==cm){
			map.put(columnNumber, binMap);
			return;
		}
		Set<Integer> binIDs = binMap.keySet();
		for(Integer bID:binIDs){
			Integer count = binMap.get(bID);
			Integer pc=cm.get(bID);
			count=(null==pc?count:pc+count);
			cm.put(bID, count);
		}
		
	}

	private static String serilazeTheMap(Map<Integer,Map<Integer,Integer>> map) {
		StringBuilder sb = new StringBuilder();
		Set<Integer> ck=map.keySet();
		for(Integer co:ck){
			sb.append(co).append(COLUMN_SPLITTER);
			Map<Integer, Integer> binMap = map.get(co);
			Set<Integer> binKeys = binMap.keySet();
			boolean addCommaFront=false;
			for(Integer bk:binKeys){
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
		String ser=sb.toString();
		
		return ser.substring(0,ser.length()-MAP_SPLITTER.length());
	}

}
