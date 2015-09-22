package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;

public class AlpinePigyValueAnalyzer extends EvalFunc<Tuple> implements
		Algebraic, Accumulator<Tuple> {
	protected static final Log itsLogger = LogFactory.getLog(AlpinePigyValueAnalyzer.class );

	private static TupleFactory mTupleFactory = TupleFactory.getInstance();
	private int numberOfTheColumns;

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
			tuple.set(iStart + 8, null);
			tuple.set(iStart + 9, null);

		}
		return tuple;
	}

	private ArrayList<Boolean> tupleTypes;

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
			itsLogger.error(ee);
			throw ee;
		} catch (Exception e) {
			itsLogger.error(e);
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

	static protected Tuple sum(Tuple input,EvalFunc evalFunc) throws ExecException,
			NumberFormatException {
		DataBag values = (DataBag) input.get(0);
		Tuple tupTmp = mTupleFactory.newTuple(values.iterator().next().size());
		for (int i = 0; i < tupTmp.size(); i++) {
			tupTmp.set(i, null);
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

	// We have 10 values for each column
	// So we can just calculate total number of the rows and apply accordingly
	private static Tuple mergeResultsIntoAggregation(Tuple tupTmp, Tuple t)
			throws ExecException {

		int rowCount = tupTmp.size() / analyzingColumns.length;
		for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
			// Star Count
			int startCount = rowNumber * analyzingColumns.length;
			for (int i = startCount; i < startCount + 6; i++) {
				tupTmp.set(
						i,
						null == tupTmp.get(i) ? t.get(i)
								: (null == t.get(i) ? tupTmp.get(i) : (DataType
										.toDouble(tupTmp.get(i)) + DataType
										.toDouble(t.get(i)))));
			}
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
			// Distinct later :)
			// SumValue
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

	@Override
	public Schema outputSchema(Schema input) {

		Schema returnSchema = new Schema();
		try {

			// Check that we were passed two fields
			// BAG-->TUPLE-->FIELDS
			Schema tupleSchema = extractTheTupleSchema(input);
			tupleTypes = new ArrayList<Boolean>();
			int size = tupleSchema.size();
			for (int i = 0; i < size; i++) {
				boolean isText = determinIfTheColumnTextBased(tupleSchema
						.getField(i).type);
				String alias = tupleSchema.getField(i).alias;
				tupleTypes.add(isText);
				List<FieldSchema> fields = new ArrayList<FieldSchema>();
				for (String an : analyzingColumns) {
					FieldSchema newField = new FieldSchema(alias + an,
							DataType.DOUBLE);
					fields.add(newField);
					returnSchema.add(newField);
				}

			}

		} catch (Exception e) {
			itsLogger.error(e);
			throw new RuntimeException(e);
		}

		return returnSchema;
	}

	private static boolean determinIfTheColumnTextBased(byte type) {
		return type == DataType.CHARARRAY;
	}

	private Schema extractTheTupleSchema(Schema input) throws FrontendException {
		if (!(input.size() == 1 && input.getField(0).type == DataType.BAG)) {
			throw new RuntimeException(
					"Expected A row definition that is one element and that element to be a bag");
		}

		// Get the types for both columns and check them. If they are
		// wrong figure out what types were passed and give a good error
		// message.
		Schema rowSchema = input.getField(0).schema;

		if (rowSchema.size() <= 0) {
			throw new RuntimeException("We must have at least a tuple or some columns");
		}

		Schema tupleSchema = (rowSchema.getField(0).type == DataType.TUPLE)?rowSchema.getField(0).schema:rowSchema;

		if (tupleSchema.size() <= 0) {
			throw new RuntimeException("We must have at least one column ");
		}
		return tupleSchema;
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
					sum(t,this);
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
		
		@Override
		public Tuple exec(Tuple input) throws IOException {
			DataBag bag = (DataBag) input.get(0);
			int numberOfTheColumns = bag.iterator().next().size();
			Tuple vaTuple = initTuple(numberOfTheColumns);

			Iterator<Tuple> it = bag.iterator();
			while (it.hasNext()) {
				includeTheTupleIntoAggregationTuple(vaTuple, it.next());
			}

			return vaTuple;
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
		protected static final Log itsLogger = LogFactory.getLog(Final.class );


		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				return sum(input,this);
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
