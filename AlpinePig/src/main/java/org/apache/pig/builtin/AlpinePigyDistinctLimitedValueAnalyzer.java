package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.pig.Accumulator;
import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigMapReduce;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.InternalDistinctBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class AlpinePigyDistinctLimitedValueAnalyzer extends EvalFunc<Tuple> implements
		Algebraic, Accumulator<Tuple> {
	private static TupleFactory mTupleFactory = TupleFactory.getInstance();
    private static BagFactory bagFactory = BagFactory.getInstance();
	private int numberOfTheColumns;
	private static  final int DISTINCT_INDEX_NUMB=7;
	private static final Double MINUS_ONE_DOUBLE=-1D;
	private static final Long MAX_ALLOWED_BAG_SIZE = 1000L;
	private static final Object ALPINE_MARKED_FOR_AGGREGATION = "ALPINE_MARKED_FOR_AGGREGATION";
	private static AtomicLong MAX_DISTINCT_COUNT=new AtomicLong(MAX_ALLOWED_BAG_SIZE);
	private static String[] analyzingColumns = new String[] { 
			"StarCount",//0
			"Count", //1
			"Null", //2
			"Zero", //3
			"Positive", //4
			"Negative", //5
			"Sum", //6
			"Distinct",//7
			"Max", //8
			"Min" //9
			};
	

	private static Tuple initTuple(int numberOfColumns) throws ExecException {
		Tuple tuple = mTupleFactory.newTuple((analyzingColumns.length
				* numberOfColumns)+1);
		// First we make everything to be zero
		for (int i = 0; i < tuple.size()-1; i++) {
			tuple.set(i, Double.valueOf(0));
		}
		for (int i = 0; i < numberOfColumns; i++) {
			int iStart = i * analyzingColumns.length;
			// We are setting Sum,Max and Min to be null
			tuple.set(iStart + 6, null);
			tuple.set(iStart + 7, Double.valueOf(0));
			tuple.set(iStart + 8, null);
			tuple.set(iStart + 9, null);
			

		}
		markTheTupleAsAggregated(tuple);
		
		
		return tuple;
	}
	
	private static void markTheTupleAsAggregated(Tuple tuple) throws ExecException {
		tuple.set(tuple.size()-1, String.valueOf(ALPINE_MARKED_FOR_AGGREGATION));
	}

	public static int acquireSizeOfColumns(Tuple input) throws ExecException{
		DataBag values = (DataBag) input.get(0);
		Tuple tt =  ((Tuple)((DataBag)values.iterator().next().get(0)).iterator().next()); 
		Iterator<Tuple> tSize= ((DataBag)tt.get(0)).iterator();
		while(tSize.hasNext()){
			Tuple t=tSize.next();
			if(null!=t){
				return 120;
			}
		}
		return -1;
	}
	@Override
	public Tuple exec(Tuple input) throws IOException {
		try {
			if (input.size() != 1) {
				throw new ExecException("Bag is empty or having more than one rows");
			}
			DataBag bag = (DataBag) input.get(0);
			numberOfTheColumns = bag.iterator().next().size();
			Tuple vaTuple = initTuple(numberOfTheColumns);
			Iterator<Tuple> it = bag.iterator();
			long progressCounter=0;
			while (it.hasNext()) {
				if ((++progressCounter % 1000) == 0) {
					progress();
				}
				includeTheTupleIntoAggregationTuple(vaTuple, it.next(),getLogger());
			}
			return vaTuple;
		} catch (ExecException ee) {
			getLogger().error(ee);
			throw ee;
		} catch (Exception e) {
			getLogger().error(e);
			int errCode = 2106;
			String msg = "Error while computing AlpinePigValueAnalysiz "+ this.getClass().getSimpleName();
			throw new ExecException(msg, errCode, PigException.BUG, e);
		}
	}

	private static void increaseTheValueOfElInTheTupleByOne(Tuple tup, int i) throws ExecException {
		increaseTheValueOfElInTheTupleBy(tup, i, 1D);

	}

	private static void increaseTheValueOfElInTheTupleBy(Tuple tup, int index, double incrementValue) throws ExecException {
		//logger.error("Setting index["+index+"]will increase the value on["+tup+"]");
		double newValue = (null == tup.get(index) ? 0 : DataType.toDouble(tup .get(index))) + incrementValue;
		//logger.error("Setting index["+index+"]with the value of["+newValue+"]");
		tup.set(index, newValue);
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
	public static synchronized void updateMaxCount(){
		
	}
	
	private static int acquireTheSize(Tuple tuple) throws ExecException{
		
			DataBag values = (DataBag) tuple.get(0);
			Tuple tf=values.iterator().next();
			if (!(tf.get(0) instanceof DataBag || tf.get(0) instanceof Tuple )){
				return tf.size()*analyzingColumns.length+1;
			}
			
			
			DataBag sth = (DataBag) values.iterator().next().get(0);

			Iterator<Tuple> itsth = sth.iterator();

			while (itsth.hasNext()) {
				Tuple td = itsth.next();
				return td.size();
			}
			return -1;
	}
	static protected Tuple sum(Tuple input,EvalFunc evalFunc, Long distinctCount,boolean isFromSumUp, Log logger) throws NumberFormatException, IOException {
		
		
		int theSize=acquireTheSize( ((DataBag) input.get(0)).iterator().next());
		
		Tuple aggregatedTuple = mTupleFactory.newTuple(theSize);
		int columSize = theSize/analyzingColumns.length;
		for (int i = 0; i < aggregatedTuple.size()-1; i++) {
			aggregatedTuple.set(i, null);
		}
		markTheTupleAsAggregated(aggregatedTuple);
		
		DataBag values = (DataBag) input.get(0);
		Iterator<Tuple> it = values.iterator();
		long progressCounter=0;
		while(it.hasNext()){
			Tuple t=it.next();
			if ((++progressCounter % 1000) == 0) {
				progressCounter=0;
				evalFunc.progress();
			}
			DataBag zero = (DataBag)t.get(0);
			Iterator<Tuple> zIt = zero.iterator();
			Tuple initedTuple = zIt.next();
			if(isFromSumUp){
				zero = (DataBag)initedTuple.get(0);
				zIt = zero.iterator();
				initedTuple = zIt.next();
			}
			//
			if(initedTuple.get(0) instanceof Tuple || initedTuple.get(0) instanceof DataBag){
				zero = (DataBag)initedTuple.get(0);
				zIt = zero.iterator();
				initedTuple = zIt.next();
			}
			
			
			mergeResultsIntoAggregation(aggregatedTuple, initedTuple,evalFunc.getLogger());
		}
		//Figuring out Distincts
		DataBag valueBag = bagFactory.newDefaultBag();
		valueBag.add(aggregatedTuple);
		
		DataBag vaBag = bagFactory.newDefaultBag();
		vaBag.add((Tuple)aggregatedTuple);
		Map<Integer,DataBag> bags=new TreeMap<Integer,DataBag>();
		bags.put(0,valueBag);
		DataBag finalBag = bagFactory.newDefaultBag();		//incase distinct count is negative number or zero then we ignore th distiction count, nice if(distinctCount=<0){
		if(distinctCount<0){
			return mTupleFactory. newTuple(finalBag);
		}
		for(int i=0;i<columSize;i++){
			bags.put(i+1, createDataBag());
		}

		values = (DataBag) input.get(0);
		it = values.iterator();
		progressCounter=0;
		

		
		
		while(it.hasNext()){
			Iterator<Tuple> zIt=null;
			Tuple tupDist=it.next();
			boolean isAggregated=isTupleAggregateReady(tupDist);
			if(isAggregated){
				DataBag zero = (DataBag) tupDist.get(0);
				zIt = zero.iterator();
				// First tuple is for previous calculation so just move over it
				zIt.next();
			}
			
			
			int i = 1;
			if(isAggregated){
				while (zIt.hasNext()) {
					Tuple distTup = (Tuple) zIt.next();
					int indexNumberOfFocusedDistNumber = analyzingColumns.length* ((i - 1)%columSize) + DISTINCT_INDEX_NUMB;
					DataBag iThBag = bags.get(i++);
				
					if (MINUS_ONE_DOUBLE.equals(DataType.toDouble(aggregatedTuple
							.get(indexNumberOfFocusedDistNumber)))) {
						iThBag.clear();
						continue;
					}
					List<Tuple> merged =mergeTheTuples(distTup);
					
					for(Tuple t:merged){
						if(!isTupleEmptyOrNull(t)){
							iThBag.add(t);
							if (iThBag.size() > distinctCount) {
								iThBag.clear();
								aggregatedTuple.set(indexNumberOfFocusedDistNumber,MINUS_ONE_DOUBLE);
								break;
							}
						}
					}
					
	
				}
			}else{
				DataBag db=(DataBag)tupDist.get(0);
				tupDist=db.iterator().next();
				for(int index=0;index<tupDist.size();index++){
					DataBag iThBag = bags.get(index+1);
					int indexNumberOfFocusedDistNumber = analyzingColumns.length* ((index)%columSize) + DISTINCT_INDEX_NUMB;

				
					if (MINUS_ONE_DOUBLE.equals(DataType.toDouble(aggregatedTuple
							.get(indexNumberOfFocusedDistNumber)))) {
						iThBag.clear();
						continue;
					}
					
					if(null!=tupDist.get(index)){
						Tuple toAdd=mTupleFactory.newTuple(1);
						toAdd.set(0, tupDist.get(index));
						iThBag.add(toAdd);
						if (iThBag.size() > distinctCount) {
							iThBag.clear();
							aggregatedTuple.set(indexNumberOfFocusedDistNumber,MINUS_ONE_DOUBLE);
						}
					}
				}
				
			}
		}
		
		//Building the tuple that would be usefull on next sum 
		
		Set<Integer> keys=bags.keySet();
		for(Integer key:keys){
			DataBag dbForTheRow = bags.get(key);
			Tuple dtt=mTupleFactory. newTuple(dbForTheRow);
			finalBag.add(dtt);
		}
		Tuple result=mTupleFactory. newTuple(finalBag);
		result.size();
		return mTupleFactory. newTuple(finalBag);
	}

	private static List<Tuple> mergeTheTuples(Tuple distTup) throws ExecException {
		List<Tuple> tuples = new ArrayList<Tuple>();
		if(null==distTup||distTup.size()==0 || !(distTup.get(0) instanceof DataBag)){
			tuples.add(distTup);
			return tuples;
		}
		DataBag firstElement =  (DataBag) distTup.get(0);
		Iterator<Tuple> it = firstElement.iterator();
		while(it.hasNext()){
			Tuple t=it.next();
			tuples.add(t);
		}
		
		return tuples;
	}

	
	
	public static List<Tuple> extractDistinctTuplesFromSummonedTuple(Tuple initTuple) throws ExecException{
		List<Tuple> arrList=new ArrayList<Tuple>();
		DataBag db = (DataBag)initTuple.get(0);
		Iterator<Tuple> it = db.iterator();
		it.next();//There goes value tuple
		while(it.hasNext()){
			Tuple t2 = it.next();
			t2.size();
			DataBag db2=(DataBag)t2.get(0);
			Tuple t3= db2.iterator().next();
			arrList.add(t3);
		}
		
		return arrList;
	}
	
	
	
	
	
	
	
	
	
	private static boolean isTupleEmptyOrNull(Tuple distTup) throws ExecException {
		if(null==distTup||0==distTup.size()||null==distTup.get(0))
			return true;
		return false;
	
	}

	public static int getNumberOfTheColumns(Tuple input) throws ExecException{
		DataBag values = (DataBag) input.get(0);
		Tuple tt =  ((Tuple)((DataBag)values.iterator().next().get(0)).iterator().next()); 
		int theSize = ((DataBag)tt.get(0)).iterator().next().size();
		int columSize = theSize/analyzingColumns.length;
		return columSize;
		
	}
	static protected Tuple finalSum(Tuple inputToBeRed,EvalFunc evalFunc) throws NumberFormatException, IOException {
		Long distinctCount = acquireMaxDistinctCount();
		Tuple input = sum(inputToBeRed, evalFunc,distinctCount,true,evalFunc.getLogger());
		inputToBeRed=null;
		DataBag dbz = (DataBag)input.get(0);		
		Iterator<Tuple> itz = dbz.iterator();
		
		
		Tuple realResult= itz.next();
		DataBag dbzz=(DataBag)realResult.get(0);
		realResult=dbzz.iterator().next();
		
		int i =0;
		while(itz.hasNext()){
			 Tuple tDb= itz.next();
			 DataBag db=(DataBag)tDb.get(0); 
			 long tzz=0;
			 if(0L!=db.size()){
				 tzz=db.size();
			 }
			 if(MINUS_ONE_DOUBLE.equals(DataType.toDouble(realResult.get(analyzingColumns.length*i+DISTINCT_INDEX_NUMB)))){
				 i++;
				 continue;
			 }
			 realResult.set(analyzingColumns.length*i+DISTINCT_INDEX_NUMB, tzz*1D);
			 i++;
			 
		 }
		
		
		Tuple finalTuple=mTupleFactory.newTuple(realResult.size()-1);
		for(int iz=0;iz<realResult.size()-1;iz++){
			finalTuple.set(iz, realResult.get(iz));
		}
		
		 
		return finalTuple;
	}

	

	private static synchronized Long acquireMaxDistinctCount() {
		try {
			String maxCount = PigMapReduce.sJobConfInternal.get().get(
					"va_distinct_value_count");
			if (null != maxCount) {

				Long maxDistinctCount = Long.parseLong(maxCount);
				if (null == maxDistinctCount | maxDistinctCount < 0) {
					return MAX_ALLOWED_BAG_SIZE;
				}
				return maxDistinctCount;

			}
		} catch (Throwable e) {
			// We ignore the exception and just return default value
		}
		return MAX_ALLOWED_BAG_SIZE;
	}
	
	private static boolean isTupleAggregateReady(Tuple iTup) throws ExecException{
		if(null==iTup||null==iTup.get(0)){
			return false;
		}
		
		if( iTup.get(0) instanceof DataBag){
			DataBag values = (DataBag) iTup.get(0);
			if(1==values.size()){
				return false;
			}
			if(values.iterator().next().get(0) instanceof DataBag){
				values = (DataBag)values.iterator().next().get(0);
				Tuple tf=values.iterator().next();
				String ot=DataType.toString(tf.get(tf.size()-1));
				if(ot.equals(ALPINE_MARKED_FOR_AGGREGATION)){
					return true;
				}
			}
			
		}
		
		String ot=DataType.toString(iTup.get(iTup.size()-1));
		if(null==ot){
			//The last column might be null
			return false;
		}
		if(ot.equals(ALPINE_MARKED_FOR_AGGREGATION)){
			return true;
		}
		return false;
	}
	

	// We have 10 values for each column
	// So we can just calculate total number of the rows and apply accordingly
	private static void mergeResultsIntoAggregation(Tuple aggregatedTuple, Tuple t,Log logger)
			throws ExecException {
		if(isTupleAggregateReady(t)){
			doAggreagatedMerge(aggregatedTuple, t);
		}else{
			includeTheTupleIntoAggregationTuple(aggregatedTuple, t,logger);
		}
	}

	private static void doAggreagatedMerge(Tuple aggregatedTuple, Tuple plainTuple)
			throws ExecException {
		int rowCount = aggregatedTuple.size() / analyzingColumns.length;
		for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
			// Star Count
			int startCount = rowNumber * analyzingColumns.length;

			
			for (int i = startCount; i < startCount + 6; i++) {
				aggregatedTuple.set(i,null == aggregatedTuple.get(i) 
										? plainTuple.get(i)
										: (null == plainTuple.get(i) 
												? aggregatedTuple.get(i) 
												: (DataType.toDouble(aggregatedTuple.get(i)) + DataType.toDouble(plainTuple.get(i)))));
			}
			
			int rn = startCount + 6;
			Double tValue = (null == aggregatedTuple.get(rn) ? null : DataType
					.toDouble(aggregatedTuple.get(rn)));
			if (null != plainTuple.get(rn)) {
				Double ti = DataType.toDouble(plainTuple.get(rn));
				aggregatedTuple.set(rn, null == tValue ? ti : ti + tValue);

			}
			// MaxValue
			rn = startCount + 8;
			tValue = (null == aggregatedTuple.get(rn) ? null : DataType.toDouble(aggregatedTuple
					.get(rn)));
			if (null != plainTuple.get(rn)) {
				Double ti = DataType.toDouble(plainTuple.get(rn));
				if (null == tValue || ti > tValue) {
					aggregatedTuple.set(rn, ti);
				}
			}
			// Min
			rn = startCount + 9;
			tValue = (null == aggregatedTuple.get(rn) ? null : DataType.toDouble(aggregatedTuple
					.get(rn)));
			if (null != plainTuple.get(rn)) {
				Double ti = DataType.toDouble(plainTuple.get(rn));
				if (null == tValue || ti < tValue) {
					aggregatedTuple.set(rn, ti);
				}
			}
			
			rn=startCount+7;
			tValue = (null == plainTuple.get(rn) ? null : DataType.toDouble(plainTuple.get(rn)));
			
			if(null!=tValue&&(tValue.equals(-1D)||-1==tValue)){
				aggregatedTuple.set(rn, -1D);
			}
			
			
		}
	}
	
	private static Tuple includeTheTupleIntoAggregationTuple( Tuple aggregatedTuple, Tuple newTuple,Log logger) throws ExecException {
		int numberOfTheColumns = newTuple.size();
		for (int i = 0; i < numberOfTheColumns; i++) {
			Object el = newTuple.get(i);
			int startingIndex = i * analyzingColumns.length;
			
			// StarCount
			increaseTheValueOfElInTheTupleByOne(aggregatedTuple,startingIndex + 0);
			// Null
			if (el == null) {
				increaseTheValueOfElInTheTupleByOne(aggregatedTuple,startingIndex + 2);
				continue;
			}
			// Count
			increaseTheValueOfElInTheTupleByOne(aggregatedTuple, startingIndex + 1);

			
			try{
				boolean isText = determinIfTheColumnTextBased(newTuple.getType(i));
				if (!isText) {
					Double value = DataType.toDouble(el);
					// Zero
					if (value.equals(0D)||0==value) {
						increaseTheValueOfElInTheTupleByOne(aggregatedTuple, startingIndex + 3);
					} else if (value > 0) {//Positive
						increaseTheValueOfElInTheTupleByOne(aggregatedTuple,startingIndex + 4);
					} else {//Negative
						increaseTheValueOfElInTheTupleByOne(aggregatedTuple,startingIndex + 5);
					}
					// Sum
					increaseTheValueOfElInTheTupleBy(aggregatedTuple,startingIndex + 6, value);
					// Max
					Double oldValue = DataType.toDouble(aggregatedTuple.get(startingIndex + 8));
					if (null == oldValue || oldValue < value) {
						aggregatedTuple.set(startingIndex + 8, value);
					}
					oldValue = DataType.toDouble(aggregatedTuple.get(startingIndex + 9));
					if (null == oldValue || oldValue > value) {
						aggregatedTuple.set(startingIndex + 9, value);
					}
				}
			}catch(Exception e){
				logger.error("["+el+"]["+newTuple.getType(i)+"]can not be casted into double");
				logger.error("Failure happend on the ["+el+"]on th aggregated tuple of["+newTuple+"]tuple due to exception of",e);
			}
			
		}

		return newTuple;

	}



	private static boolean determinIfTheColumnTextBased(byte type) {
		return type == DataType.CHARARRAY;
	}



	/* Accumulator interface implementation */
	private Tuple intermediateCount;

	// private Map<Double,Double> distincts = new HashMap<Double,Double>();

	@Override
	public void accumulate(Tuple b) throws IOException {
		try {
			Long distinctCount = acquireMaxDistinctCount();
			DataBag bag = (DataBag) b.get(0);
			Iterator<Tuple> it = bag.iterator();
			while (it.hasNext()) {
				Tuple t = (Tuple) it.next();
				if (t != null && t.size() > 0) {
					sum(t,this,distinctCount,false,getLogger());
				}
			}
		} catch (ExecException ee) {
			throw ee;
		} catch (Exception e) {
			getLogger().error(e);
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
	
	
	static private DataBag createDataBag() {
    	return createDataBag(1);
    }
	
	 static private DataBag createDataBag(int size) {
	    	// by default, we create InternalSortedBag, unless user configures
			// explicitly to use old bag
		    
	    	String bagType = null;
	        if (PigMapReduce.sJobConfInternal.get() != null) {     
	   			bagType = PigMapReduce.sJobConfInternal.get().get("pig.cachedbag.distinct.type");       			
	   	    }
	        
	        DataBag topBag=null;
	                      
	    	if (bagType != null && bagType.equalsIgnoreCase("default")) {        	    	
	    		topBag= BagFactory.getInstance().newDistinctBag();    			
	   	    } else {   	    	
	   	    	topBag= new InternalDistinctBag(3);
		    }
	    	
	    	for(int i=0;i<size;i++){
	    		if (bagType != null && bagType.equalsIgnoreCase("default")) {        	    	
		    		topBag.addAll(BagFactory.getInstance().newDistinctBag());    			
		   	    } else {   	    	
		   	    	topBag.addAll(new InternalDistinctBag(3));
			    }
	    	}
	    	
	    	return topBag;
	    	
	    	
	    }
	    
//	static private DataBag getDistinctFromNestedBags(Tuple input,
//			EvalFunc evalFunc) throws IOException {
//		DataBag result = createDataBag();
//		long progressCounter = 0;
//		try {
//			int size = input.size();
//			for (int i = 0; i < size; i++) {
//				++progressCounter;
//				if ((progressCounter % 1000) == 0) {
//					evalFunc.progress();
//				}
//				result.add(mTupleFactory.newTuple(input.get(i)));
//
//			}
//		} catch (ExecException e) {
//			throw e;
//		}
//		return result;
//	}
	    
	    protected DataBag getDistinct(Tuple input) throws IOException {
	        try {
	            DataBag inputBg = (DataBag)input.get(0);
	            DataBag result = createDataBag(input.size());
	            long progressCounter = 0;
	            for (Tuple tuple : inputBg) {
	                result.add(tuple);
	                ++progressCounter;
	                if ((progressCounter % 1000) == 0) {
	                    progress();
	                }
	            }
	            return result;
	        } catch (ExecException e) {
	             throw e;
	        }
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
				Long distinctCount = acquireMaxDistinctCount();
				return sum(input,this,distinctCount,false,getLogger());
				
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
				return finalSum(input,this);
			} catch (Exception ee) {
				getLogger().error(ee);
				int errCode = 2106;
				String msg = "Error while computing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}

	}

}
