package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.pig.Accumulator;
import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class AlpinePiggyScatterPlotterMatrix extends EvalFunc<Tuple> implements
		Algebraic, Accumulator<Tuple> {
	private static final String MARKER = "MARKER";
	private static TupleFactory mTupleFactory = TupleFactory.getInstance();

	private static Tuple initTuple(int n) throws ExecException {
		//x0x1=>count,sumx0,sumx1,sumx0x0,sumx0x1,sumx1x1 and where neither xo nor x1 is null
		//For each combination we need to calculate 6 values
		//6*(n*(n+1)/2)=3(n*n+n)
		int n2nDiv2ANC=3*((n*n)-n)+1;

		return mTupleFactory.newTuple(n2nDiv2ANC);
	}

	private ArrayList<Boolean> tupleTypes;

	@Override
	public Tuple exec(Tuple input) throws IOException {
		return input;
	}


	private static void increaseTheValueOfElInTheTupleBy(Tuple tup, int index,
			double incrementValue) throws ExecException {
		double newValue = (null == tup.get(index) ? 0 : DataType.toDouble(tup
				.get(index))) + incrementValue;
		tup.set(index, newValue);

	}
	
	
	private static Tuple buildInitialTupleForTheRow(Tuple input) throws ExecException {
		int numberOfTheColumns=0;
	
		Tuple row=null;
		if(null==input){
			return null;
		}else if(input.get(0) instanceof DataBag){
			DataBag values = (DataBag) input.get(0);
			Iterator<Tuple> it = values.iterator();
			row=it.next();
			numberOfTheColumns = row.size();
		}else{
			numberOfTheColumns = input.size();
			row=input;
		}
		
		Tuple vaTuple = initTuple(numberOfTheColumns);
		//        0      1      2          3         4          5
		//2*3/2+2*2=7
		//x0,x1->sumx0,sumx1,sum(x0*x0),sum(x0x1),sum(x1*x1)
		int i6=-6;
		for (int i = 0; i < numberOfTheColumns; i++) {
			for (int j = i + 1; j < numberOfTheColumns; j++) {
				i6+=6;
				//Jeff: to fix pivotal41573093:Although x or y is null,we can calculate the count.
				// count
				increaseTheValueOfElInTheTupleBy(vaTuple, i6, 1);
				
				if (null == row.get(i) || null == row.get(j)) {
					continue;
				}
				
				Double x = DataType.toDouble(row.get(i));
				Double y = DataType.toDouble(row.get(j));

				// value x
				increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 1, x);
				// value y
				increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 2, y);
				// value xx
				increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 3, x * x);
				// value yy
				increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 4, y * y);
				// value xy
				increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 5, x * y);

			}
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

	static protected Tuple accumulate(Tuple input,EvalFunc evalFunc) throws ExecException,
			NumberFormatException {
		DataBag values = (DataBag) input.get(0);
		int numberOfTheColumns = values.iterator().next().size();
		Tuple tupTmp = createInitTuple(input);
		
		long progressCounter=0;
		for (Iterator<Tuple> it = values.iterator(); it.hasNext();) {
			Tuple t = it.next();
			if ((++progressCounter % 1000) == 0) {
				progressCounter=0;
				evalFunc.progress();
			}
			if(isTupleMarked(t)){
				//removeTheMarker(t);
			}else{
				t=buildInitialTupleForTheRow(t);
			}
			mergeResultsIntoAggregation(tupTmp, t);
		}
		markTheTuple(tupTmp);
		return tupTmp;
	}

	private static Tuple createInitTuple(Tuple input) throws ExecException {
		if(null==input){
			return null;
		}
		if(isTupleMarked(input)){
			Tuple tu = fetchedFirtTuple(input);
			return mTupleFactory.newTuple(tu.size());
		}
		
		Tuple tu=null;
		if(input.get(0) instanceof Tuple){
			tu=(Tuple)input.get(0);
		}else if(input.get(0) instanceof DataBag){
			tu=((DataBag)input.get(0)).iterator().next();
			if((DataBag)tu.get(0) instanceof DataBag){
				tu=((DataBag)tu.get(0)).iterator().next();
			}
			
		}else{
			tu=input;
		}
			
		return initTuple(tu.size());
	}


	private static void removeTheMarker(Tuple t) throws ExecException {
		t.set(t.size()-1, null);
	}


	private static void markTheTuple(Tuple input) throws ExecException {
		Tuple row=null;
		if(null==input){
			return;
		}else if(input.get(0) instanceof DataBag){
			DataBag values = (DataBag) input.get(0);
			Iterator<Tuple> it = values.iterator();
			row=it.next();
		}else{
			row=input;
		}
		
		if(null==row.get(row.size()-1)){
			row.set(row.size()-1, MARKER);
			return;
		}
	}


	private static boolean isTupleMarked(Tuple t) throws ExecException {
		if(null==t){
			return false;
		}
		Tuple tu=null;
		
		try{
			tu = fetchedFirtTuple(t);
			Object o=tu.get(tu.size()-1);
			if(null==o||!(o instanceof String))return false;
			String marker =(String)o;
			return MARKER.equals(marker);
			
		}catch(Exception e){
			return false;
		}
		
	}


	private static Tuple fetchedFirtTuple(Tuple t) throws ExecException {
		Tuple tu;
		if(t.get(0) instanceof DataBag){
			tu=((DataBag)t.get(0)).iterator().next();
			if(tu.get(0) instanceof DataBag){
				tu=((DataBag)tu.get(0)).iterator().next();
			}
		}else if(t.get(0) instanceof Tuple){
			tu=((DataBag)t.get(0)).iterator().next();
			tu=((DataBag)tu.get(0)).iterator().next();
		}
		else{
			tu=t;
		}
		return tu;
	}


	private static Tuple mergeResultsIntoAggregation(Tuple tupTmp, Tuple t)
			throws ExecException {

		int columnCount = tupTmp.size();

		for (int cn = 0; cn < columnCount; cn++) {
			if(null==t.get(cn)||t.get(cn) instanceof String){
				continue;
			}
			increaseTheValueOfElInTheTupleBy(tupTmp,cn,DataType.toDouble(t.get(cn)));
		}
		return tupTmp;
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
					accumulate(t,this);
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
				return accumulate(input,this);
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
				return accumulate(input,this);
				
				
			} catch (Exception ee) {
				int errCode = 2106;
				String msg = "Error while computing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}

	}

}
