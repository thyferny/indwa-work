package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pig.Accumulator;
import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class AlpinePigyValueAnalyzerDeviationCalculator extends EvalFunc<Tuple> implements
		Algebraic, Accumulator<Tuple> {
	private static Log itsLogger = LogFactory.getLog(PigUtil.class );

	private static TupleFactory mTupleFactory = TupleFactory.getInstance();

	private static Tuple initTuple(int numberOfColumns) throws ExecException {
		Tuple tuple = mTupleFactory.newTuple(numberOfColumns);
		// First we make everything to be zero
		for (int i = 0; i < tuple.size(); i++) {
			tuple.set(i, Double.valueOf(0));
		}
		return tuple;
	}

	private ArrayList<Boolean> tupleTypes;

	@Override
	public Tuple exec(Tuple input) throws IOException {
		try {
			return initOfSigma(input);
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


	private static void increaseTheValueOfElInTheTupleBy(Tuple tup, int index,
			double incrementValue) throws ExecException {
		double newValue = (null == tup.get(index) ? 0 : DataType.toDouble(tup
				.get(index))) + incrementValue;
		tup.set(index, newValue);

	}
	
	
	private static Tuple initOfSigma(Tuple input) throws ExecException {
		DataBag values = (DataBag) input.get(0);
		
		int numberOfTheColumns = values.iterator().next().size();
		Tuple vaTuple = initTuple(numberOfTheColumns);

		Iterator<Tuple> it = values.iterator();
		Tuple row=it.next();
		for(int rowNumber=0;rowNumber<row.size();rowNumber++){
			if(null==row.get(rowNumber)){
				continue;
			}
			double avg=(Double)input.get(rowNumber*2+1);
			double countMinusOne=DataType.toDouble(input.get(rowNumber*2+2));
			double increaseBy=(((Double)DataType.toDouble(row.get(rowNumber))-avg));
			increaseBy=increaseBy*increaseBy/countMinusOne;
			increaseTheValueOfElInTheTupleBy(vaTuple,rowNumber,increaseBy);
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

	static protected Tuple calulcateDeviation(Tuple input,EvalFunc evalFunc) throws ExecException,
			NumberFormatException {
		DataBag values = (DataBag) input.get(0);
		Tuple tupTmp = mTupleFactory.newTuple(values.iterator().next().size());
		for (int i = 0; i < tupTmp.size(); i++) {
			tupTmp.set(i, 0);
		}
		
		long progressCounter=0;
		for (Iterator<Tuple> it = values.iterator(); it.hasNext();) {
			Tuple t = it.next();
			if ((++progressCounter % 1000) == 0) {
				progressCounter=0;
				evalFunc.progress();
			}
			
			mergeResultsIntoAggregation(tupTmp, t);
		}
		return tupTmp;
	}

	private static Tuple mergeResultsIntoAggregation(Tuple tupTmp, Tuple t)
			throws ExecException {

		int rowCount = tupTmp.size();

		for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
			increaseTheValueOfElInTheTupleBy(tupTmp,rowNumber,DataType.toDouble(t.get(rowNumber)));
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
					calulcateDeviation(t,this);
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
			return initOfSigma(input);
			}catch(Throwable e){
				itsLogger.error(e);
				throw new IOException(e);
			}
		}

	}

	static public class Intermediate extends EvalFunc<Tuple> {
		
		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				return calulcateDeviation(input,this);
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
				return calulcateDeviation(input,this);
				
				
			} catch (Exception ee) {
				itsLogger.error(ee);
				int errCode = 2106;
				String msg = "Error while computing count in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}

	}

}
