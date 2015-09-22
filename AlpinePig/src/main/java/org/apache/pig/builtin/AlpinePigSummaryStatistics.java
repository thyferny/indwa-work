package org.apache.pig.builtin;

import org.apache.pig.Accumulator;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigMapReduce;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.*;


/**
 * User: sasher
 * Date: 11/12/12
 * Time: 2:02 PM
 *
 * NOTE: This code is based off of:
 *
 * https://github.com/linkedin/datafu/blob/master/src/java/datafu/pig/stats/StreamingQuantile.java
 *
 * That page is under this licensing:
 *
 * Copyright (c) 2011, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 *
 */
public class AlpinePigSummaryStatistics extends EvalFunc<Tuple> implements Accumulator<Tuple> {

	private static int numberOfTheColumns;

	private static final Long MAX_ALLOWED_BAG_SIZE = 1000L;
	public static String[] analyzingColumns = new String[] {
			"StarCount",	//0
			"Count",		//1
			"Null",			//2
			"Zero",			//3
			"Positive",		//4
			"Negative",		//5
			"Sum",			//6
			"Distinct",		//7
			"Min",			//8
			"25%",			//9
			"50%",			//10
			"75%",			//11
			"Max",			//12
			"Mean",			// 13
			"Top 1 Value",	// 14
			"Top 1 Count",	// 15
			"Top 2 Value",	// 16
			"Top 2 Count",	// 17
			"Top 3 Value",	// 18
			"Top 3 Count",	// 19
			"Top 4 Value",	// 20
			"Top 4 Count",	// 21
			"Top 5 Value",	// 22
			"Top 5 Count",	// 23
			"Top 6 Value",	// 24
			"Top 6 Count",	// 25
			"Top 7 Value",	// 26
			"Top 7 Count",	// 27
			"Top 8 Value",	// 28
			"Top 8 Count",	// 29
			"Top 9 Value",	// 30
			"Top 9 Count",	// 31
			"Top 10 Value",	// 32
			"Top 10 Count",	// 33
	};

	private static HashMap<Integer, HashMap<Object, Long>> distinctValsWithCount;
	private static HashMap<Integer, Long> totalCount;
	private static HashMap<Integer, Long> negativeCount;
	private static HashMap<Integer, Long> positiveCount;
	private static HashMap<Integer, Long> zeroCount;
	private static HashMap<Integer, Long> nullCount;
	private static HashMap<Integer, QuantileEstimatorForSummaryStatistics> quantileEstimators;

	@Override
	public Tuple exec(Tuple input) throws IOException {
		try {
			DataBag db = (DataBag)input.get(0);
			accumulate(TupleFactory.getInstance().newTuple(db));
			Tuple ret = getValue();
			cleanup();
			return ret;
		} catch (Exception e) {
			int errCode = 2106;
			String msg = "Error while determining quantiles " + this.getClass().getSimpleName();
			throw new ExecException(msg, errCode, PigException.BUG, e);
		}
	}

	@Override
	public void accumulate(Tuple b)
		throws IOException {
//		if (b.size() != 1) {
//			throw new ExecException("Bag is empty or has more than one row");
//		}

		DataBag bag = (DataBag) b.get(0);
//		if (bag == null || bag.size() == 0)
//			return;

		numberOfTheColumns = bag.iterator().next().size();

		Long distinctCount = acquireMaxDistinctCount();

		if (distinctValsWithCount == null || distinctValsWithCount.isEmpty()) {
			distinctValsWithCount = new HashMap<Integer, HashMap<Object, Long>>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				distinctValsWithCount.put(i, new HashMap<Object, Long>(distinctCount.intValue())); // type downcast so loss of precision but we actually restrict this to 100K
			}
		}

		if (totalCount == null || totalCount.isEmpty()) {
			totalCount = new HashMap<Integer, Long>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				totalCount.put(i, 0L);
			}
		}

		if (quantileEstimators == null || quantileEstimators.isEmpty()) {
			quantileEstimators = new HashMap<Integer, QuantileEstimatorForSummaryStatistics>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				quantileEstimators.put(i, new QuantileEstimatorForSummaryStatistics());
			}
		}

		if (negativeCount == null || negativeCount.isEmpty()) {
			negativeCount = new HashMap<Integer, Long>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				negativeCount.put(i, 0L);
			}
		}

		if (positiveCount == null || positiveCount.isEmpty()) {
			positiveCount = new HashMap<Integer, Long>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				positiveCount.put(i, 0L);
			}
		}

		if (zeroCount == null || zeroCount.isEmpty()) {
			zeroCount = new HashMap<Integer, Long>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				zeroCount.put(i, 0L);
			}
		}

		if (nullCount == null || nullCount.isEmpty()) {
			nullCount = new HashMap<Integer, Long>(numberOfTheColumns);
			for (int i = 0; i < numberOfTheColumns; i++) {
				nullCount.put(i, 0L);
			}
		}

		long progressCounter = 0;

		for (Tuple t : bag) {
			if ((++progressCounter % 1000) == 0) {
				progress();
			}

			for (int i = 0; i < numberOfTheColumns; i++)
			{
				Object object = t.get(i);

				addToDistinctValsWithCount(i, object, distinctCount);
				incrementTotalCount(i);

				if ((object instanceof Number)) {
					double oVal = ((Number) object).doubleValue();

					//QuantileEstimator.getInstance().add(oVal);

					//addToSum(i, oVal);
					addToQuantileEstimator(i, oVal);
					setPositiveCount(i, oVal);
					setNegativeCount(i, oVal);
					setZeroCount(i, oVal);
				} else if (object == null) {
					incrementNullCount(i);
				} else {
					//throw new IllegalStateException("bag must have numerical values (and be non-null)");
				}
			}
		}
	}

	private void addToQuantileEstimator(int i, double v) {
		QuantileEstimatorForSummaryStatistics qe = quantileEstimators.get(i);
		qe.add(v);
	}

	private void setPositiveCount(int i, double v) {
		if (v < 0.0d) return;
		incrementCount(i, positiveCount);
	}

	private void setNegativeCount(int i, double v) {
		if (v >= 0.0d) return;
		incrementCount(i, negativeCount);
	}

	private void setZeroCount(int i, double v) {
		if (v == 0d) { // TODO: check bits
			incrementCount(i, zeroCount);
		}
	}

	private void incrementTotalCount(int i) {
		incrementCount(i, totalCount);
	}

	private void incrementNullCount(int i) {
		incrementCount(i, nullCount);
	}

	private void incrementCount(int i, HashMap<Integer, Long> map) {
		Long total = map.get(i);
		if (total == null) {
			total = new Long(0);
		}
		map.put(i, new Long(total + 1L));
	}

	private void addToDistinctValsWithCount(int i, Object object, Long distinctCount) {
		HashMap<Object, Long> tempHash = distinctValsWithCount.get(new Integer(i));
		Long count = tempHash.get(object);
		if (count == null) {
			// add
			if (tempHash.size() >= distinctCount) {
				getLogger().warn("Hit maximum size for vals");
			}
			else {
				tempHash.put(object, 1L);
			}
		}
		else {
			tempHash.put(object, new Long(count + 1L));
		}
	}

	private static synchronized Long acquireMaxDistinctCount() {
		try {
			String maxCount = PigMapReduce.sJobConfInternal.get().get("va_distinct_value_count");
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

	@Override
	public void cleanup()
	{
		clearHash(distinctValsWithCount);
		distinctValsWithCount = null;
		clearHash(totalCount);
		totalCount = null;
		clearHash(negativeCount);
		negativeCount = null;
		clearHash(positiveCount);
		positiveCount = null;
		clearHash(zeroCount);
		zeroCount = null;
		clearHash(nullCount);
		nullCount = null;
		clearHash(quantileEstimators);
		quantileEstimators = null;

		numberOfTheColumns = 0;
	}

	private void clearHash(HashMap hm) {
		if (hm != null) {
			hm.clear();
		}
	}

	@Override
	public Tuple getValue() {
		Tuple t = TupleFactory.getInstance().newTuple(numberOfTheColumns * analyzingColumns.length);

		try {
			for (int i = 0; i < numberOfTheColumns; i++)
			{
				int index = i * analyzingColumns.length;

				// star count,		0
				t.set(index + 0, totalCount.get(i));
				//"Count",		//1
				t.set(index + 1, totalCount.get(i));
				//"Null",			//2
				t.set(index + 2, nullCount.get(i));
				//"Zero",			//3
				t.set(index + 3, zeroCount.get(i));
				//"Positive",		//4
				t.set(index + 4, positiveCount.get(i));
				//"Negative",		//5
				t.set(index + 5, negativeCount.get(i));

				QuantileEstimatorForSummaryStatistics qe = quantileEstimators.get(i);
				//"Sum",			//6
				t.set(index + 6, qe.getTotalValueOfElements());
				//"Distinct",		//7
				t.set(index + 7, distinctValsWithCount.get(i).size());

				int j = 0;
				for (double quantileValue : qe.getQuantiles()) {
					//"Min",			//index + 8
					//"25%",			//index + 9
					//"50%",			//index + 10
					//"75%",			//index + 11
					//"Max"				//index + 12
					t.set(index + j+8, quantileValue);
					j++;
				}
				//"Mean"			//index + 13
				t.set(index + j+8, qe.getMean());

				// get unique elements
				Set<Map.Entry<Object, Long>> dvwc = distinctValsWithCount.get(i).entrySet();
				// put unique elements with count into list so we can sort
				List<Map.Entry<Object, Long>> dvwclist = new ArrayList<Map.Entry<Object, Long>>(dvwc.size());
				for (Map.Entry<Object, Long> e : dvwc) {
					dvwclist.add(e);
				}

				// sort so largest at top (why we have negative compareTo)
				Collections.sort(
					dvwclist,
					new Comparator<Map.Entry<Object, Long>>() {
						@Override
						public int compare(Map.Entry<Object, Long> a, Map.Entry<Object, Long> b) {
							return -a.getValue().compareTo(b.getValue());
						}
					});

				// + 14
				for (int k = 0; k < 10; k++) {
					// double i so we can set two values into the tuple
					t.set(index + k + k + 14, k < dvwclist.size() ? dvwclist.get(k).getKey() : -1);
					t.set(index + k + k + 15, k < dvwclist.size() ? dvwclist.get(k).getValue() : -1);
				}
			}
		} catch (IOException e) {
			return null;
		}
		return t;
	}

	@Override
	public Schema outputSchema(Schema input)
	{
		Schema tupleSchema = new Schema();
		for (int i = 0; i < QuantileEstimator.getInstance().getNumberOfQuantiles(); i++) {
			tupleSchema.add(new Schema.FieldSchema("quantile_" + i, DataType.DOUBLE));
		}
		return tupleSchema;
	}
}
