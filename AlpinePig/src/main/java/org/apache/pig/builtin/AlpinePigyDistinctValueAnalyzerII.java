package org.apache.pig.builtin;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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

public class AlpinePigyDistinctValueAnalyzerII extends EvalFunc<Tuple>{
	private static TupleFactory mTupleFactory = TupleFactory.getInstance();
    private static BagFactory bagFactory = BagFactory.getInstance();
	private int numberOfTheColumns;
	private static  final int DISTINCT_INDEX_NUMB=7;
	private static final Double MINUS_ONE_DOUBLE=-1D;
	private static final Long MAX_ALLOWED_BAG_SIZE = 1000L;
	private static String[] analyzingColumns = new String[] { 
			"StarCount",
			"Count", 
			"Null", 
			"Zero", 
			"Positive", 
			"Negative", 
			"Sum", 
			"Distinct",
			"Max", 
			"Min" 
			};
	
	//0"StarCount",
	//1"Count", 
	//2"Null", 
	//3"Zero", 
	//4"Positive", 
	//5"Negative", 
	//6"Sum", 
	//7"Distinct",
	//8"Max", 
	//9"Min" 

	private static Tuple initTuple(int numberOfColumns) throws ExecException {
		Tuple tuple = mTupleFactory.newTuple(analyzingColumns.length
				* numberOfColumns);
		// First we make everything to be zero
		for (int i = 0; i < tuple.size(); i++) {
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
		return tuple;
	}

	@Override
	public Tuple exec(Tuple input) throws IOException {
		try {
			if (input.size() != 1) {
				throw new ExecException(
						"Bag is empty or having more than one rows");
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
				includeTheTupleIntoAggregationTuple(vaTuple, it.next());
			}
			return vaTuple;
		} catch (ExecException ee) {
			throw ee;
		} catch (Exception e) {
			int errCode = 2106;
			String msg = "Error while computing AlpinePigValueAnalysiz "
					+ this.getClass().getSimpleName();
			throw new ExecException(msg, errCode, PigException.BUG, e);
		}
	}

	private static void increaseTheValueOfElInTheTupleByOne(Tuple tup, int i)
			throws ExecException {
		increaseTheValueOfElInTheTupleBy(tup, i, 1D);

	}

	private static void increaseTheValueOfElInTheTupleBy(Tuple tup, int index,
			double incrementValue) throws ExecException {
		double newValue = (null == tup.get(index) ? 0 : DataType.toDouble(tup
				.get(index))) + incrementValue;
		tup.set(index, newValue);

	}

	

	static protected Tuple sum(Tuple input,EvalFunc evalFunc) throws NumberFormatException, IOException {
		DataBag values = (DataBag) input.get(0);
		Tuple tt =  ((Tuple)((DataBag)values.iterator().next().get(0)).iterator().next()); 
		int theSize = ((DataBag)tt.get(0)).iterator().next().size();
		Tuple tupTmp = mTupleFactory.newTuple(theSize);
		int columSize = theSize/analyzingColumns.length;
		for (int i = 0; i < tupTmp.size(); i++) {
			tupTmp.set(i, null);
		}
		
		long progressCounter=0;
		for (Iterator<Tuple> it = values.iterator(); it.hasNext();) {
			Tuple t = it.next();
			t=((DataBag)t.get(0)).iterator().next();
			
			
			if ((++progressCounter % 1000) == 0) {
				progressCounter=0;
				evalFunc.progress();
			}
			DataBag zero = (DataBag)t.get(0);
			Iterator<Tuple> zIt = zero.iterator();
			mergeResultsIntoAggregation(tupTmp, zIt.next());
		}
		//Figuring out Distincts
		DataBag valueBag = bagFactory.newDefaultBag();
		valueBag.add(tupTmp);
		
		DataBag vaBag = bagFactory.newDefaultBag();
		vaBag.add((Tuple)tupTmp);
		Map<Integer,DataBag> bags=new TreeMap<Integer,DataBag>();
		bags.put(0,valueBag);
		for(int i=0;i<columSize;i++){
			bags.put(i+1, createDataBag());
		}
		Iterator<Tuple> it = values.iterator();
		while (it.hasNext()) {
			Tuple tupDist = (Tuple) it.next();
			DataBag zero = (DataBag) tupDist.get(0);
			Iterator<Tuple> zIt = zero.iterator();
			// First tuple is for previous calculation so just move over it
			Tuple valueAnalTuple = zIt.next();
			int i = 1;
			while (zIt.hasNext()) {
				Tuple distTup = (Tuple) zIt.next();
				int indexNumberOfFocusedDistNumber = analyzingColumns.length* ((i - 1)%columSize) + DISTINCT_INDEX_NUMB;
				DataBag iThBag = bags.get(i++);
			
				if (MINUS_ONE_DOUBLE.equals(DataType.toDouble(tupTmp
						.get(indexNumberOfFocusedDistNumber)))) {
					iThBag.clear();
					continue;
				}
				
				//We are not counting null values in distinct so will not add it into bag
				if (isTupleEmptyOrNull(distTup)) {
					// It seems that happens in case we have header
					continue;
				}

				iThBag.add(distTup);

				if (iThBag.size() > MAX_ALLOWED_BAG_SIZE) {
					iThBag.clear();
					tupTmp.set(indexNumberOfFocusedDistNumber,MINUS_ONE_DOUBLE);
					continue;
				}

			}
		}
		
		//Building the tuple that would be usefull on next sum 
		DataBag finalBag = bagFactory.newDefaultBag();
		Set<Integer> keys=bags.keySet();
		for(Integer key:keys){
			DataBag dbForTheRow = bags.get(key);
			Tuple dtt=mTupleFactory. newTuple(dbForTheRow);
			finalBag.add(dtt);
		}
		
		return mTupleFactory. newTuple(finalBag);
	}
	
	private static boolean isTupleEmptyOrNull(Tuple distTup) throws ExecException {
		return (null==distTup||
				null==((DataBag)distTup.get(0))||
				null==((DataBag)distTup.get(0)).iterator()||
				!((DataBag)distTup.get(0)).iterator().hasNext()||
				null==((DataBag)distTup.get(0)).iterator().next().get(0));
	}

	public static int getNumberOfTheColumns(Tuple input) throws ExecException{
		DataBag values = (DataBag) input.get(0);
		Tuple tt =  ((Tuple)((DataBag)values.iterator().next().get(0)).iterator().next()); 
		int theSize = ((DataBag)tt.get(0)).iterator().next().size();
		int columSize = theSize/analyzingColumns.length;
		return columSize;
		
	}
	static protected Tuple finalSum(Tuple inputToBeRed,EvalFunc evalFunc) throws NumberFormatException, IOException {
		
		Tuple input = sum(inputToBeRed, evalFunc);
		
		
		
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
				 tzz= ((DataBag)db.iterator().next().get(0)).size();
				 if(tzz<2){ 
					 DataBag values = (DataBag) tDb.get(0);
					 Tuple tzzz = ((Tuple)((DataBag)values.iterator().next().get(0)).iterator().next());
					 DataBag zero1 = (DataBag)tzzz.get(0);
					 tzz=zero1.size();
				 }
				 
			 }
			 if(MINUS_ONE_DOUBLE.equals(DataType.toDouble(realResult.get(analyzingColumns.length*i+DISTINCT_INDEX_NUMB)))){
				 i++;
				 continue;
			 }
			 realResult.set(analyzingColumns.length*i+DISTINCT_INDEX_NUMB, tzz*1D);
			 i++;
			 
		 }
		
		return realResult;
	}

	// We have 10 values for each column
	// So we can just calculate total number of the rows and apply accordingly
	private static Tuple mergeResultsIntoAggregation(Tuple tupTmp, Tuple t)
			throws ExecException {
		
		int rowCount = tupTmp.size() / analyzingColumns.length;
		for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
			// Star Count
			int startCount = rowNumber * analyzingColumns.length;
			for (int i = startCount; i < startCount + 6; i++) {
				tupTmp.set(i,null == tupTmp.get(i) 
										? t.get(i)
										: (null == t.get(i) 
												? tupTmp.get(i) 
												: (DataType.toDouble(tupTmp.get(i)) + DataType.toDouble(t.get(i)))));
			}
			
			int rn = startCount + 6;
			Double tValue = (null == tupTmp.get(rn) ? null : DataType
					.toDouble(tupTmp.get(rn)));
			if (null != t.get(rn)) {
				Double ti = DataType.toDouble(t.get(rn));
				tupTmp.set(rn, null == tValue ? ti : ti + tValue);

			}
			// MaxValue
			rn = startCount + 8;
			tValue = (null == tupTmp.get(rn) ? null : DataType.toDouble(tupTmp
					.get(rn)));
			if (null != t.get(rn)) {
				Double ti = DataType.toDouble(t.get(rn));
				if (null == tValue || ti > tValue) {
					tupTmp.set(rn, ti);
				}
			}
			// Min
			rn = startCount + 9;
			tValue = (null == tupTmp.get(rn) ? null : DataType.toDouble(tupTmp
					.get(rn)));
			if (null != t.get(rn)) {
				Double ti = DataType.toDouble(t.get(rn));
				if (null == tValue || ti < tValue) {
					tupTmp.set(rn, ti);
				}
			}
			
			rn=startCount+7;
			tValue = (null == t.get(rn) ? null : DataType.toDouble(t.get(rn)));
			
			if(null!=tValue&&(tValue.equals(-1D)||-1==tValue)){
				tupTmp.set(rn, -1D);
			}
			
			
		}
		return tupTmp;
	}

	private static Tuple includeTheTupleIntoAggregationTuple(
			Tuple aggregatedTuple, Tuple newTuple) throws ExecException {
		int numberOfTheColumns = newTuple.size();
		for (int i = 0; i < numberOfTheColumns; i++) {
			Object el = newTuple.get(i);
			int startingIndex = i * analyzingColumns.length;
			// StarCount
			increaseTheValueOfElInTheTupleByOne(aggregatedTuple,
					startingIndex + 0);
			// Null
			if (el == null) {
				increaseTheValueOfElInTheTupleByOne(aggregatedTuple,
						startingIndex + 2);
				continue;
			}
			// Count
			increaseTheValueOfElInTheTupleByOne(aggregatedTuple,
					startingIndex + 1);

			boolean isText = determinIfTheColumnTextBased(newTuple.getType(i));
			if (!isText) {
				Double value = DataType.toDouble(el);
				// Zero
				if (value.equals(0D)||0==value) {
					increaseTheValueOfElInTheTupleByOne(aggregatedTuple,
							startingIndex + 3);
				} else if (value > 0) {
					increaseTheValueOfElInTheTupleByOne(aggregatedTuple,
							startingIndex + 4);
				} else {
					increaseTheValueOfElInTheTupleByOne(aggregatedTuple,
							startingIndex + 5);
				}
				// Sum
				increaseTheValueOfElInTheTupleBy(aggregatedTuple,
						startingIndex + 6, value);
				// Max
				Double oldValue = DataType.toDouble(aggregatedTuple
						.get(startingIndex + 8));
				if (null == oldValue || oldValue < value) {
					aggregatedTuple.set(startingIndex + 8, value);
				}
				oldValue = DataType.toDouble(aggregatedTuple
						.get(startingIndex + 9));
				if (null == oldValue || oldValue > value) {
					aggregatedTuple.set(startingIndex + 9, value);
				}
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
			DataBag values = (DataBag) input.get(0);
			
			int numberOfTheColumns = values.iterator().next().size();
			Tuple vaTuple = initTuple(numberOfTheColumns);

			Iterator<Tuple> it = values.iterator();
			while (it.hasNext()) {
				includeTheTupleIntoAggregationTuple(vaTuple, it.next());
			}
			
			
			
			//Figuring out Distincts
			DataBag valueBag = bagFactory.newDefaultBag();
			valueBag.add(vaTuple);
			
			Map<Integer,DataBag> bags=new TreeMap<Integer,DataBag>();
			bags.put(0,valueBag);
			for(int i=0;i<numberOfTheColumns;i++){
				bags.put(i+1, bagFactory.newDefaultBag());
			}

			it = values.iterator();
			Tuple tuple = it.next();
			for (int i=1;i<numberOfTheColumns+1;i++) {
					DataBag tBag = bags.get(i);
					Tuple theNewTuple = mTupleFactory.newTuple(1);
					theNewTuple.set(0, tuple.get(i-1));
					tBag.add(theNewTuple);
			}
			
			//Building the tuple that would be usefull on next sum 
			DataBag finalBag = bagFactory.newDefaultBag();
			Set<Integer> keys=bags.keySet();
			for(Integer key:keys){
				DataBag dbForTheRow = bags.get(key);
				Tuple dtt=mTupleFactory. newTuple(dbForTheRow);
				finalBag.add(dtt);
			}
			
			return mTupleFactory. newTuple(finalBag);
		}
	}

	static public class Intermediate extends EvalFunc<Tuple> {
		
		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				return sum(input,this);
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
				int errCode = 2106;
				String msg = "Error while computing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}

	}

}
