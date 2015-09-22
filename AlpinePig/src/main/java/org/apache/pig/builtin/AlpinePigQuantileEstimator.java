package org.apache.pig.builtin;

import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
public class AlpinePigQuantileEstimator extends EvalFunc<Tuple> implements Algebraic {




	@Override
	public Tuple exec(Tuple input) throws IOException {
		try {
			return input;
		} catch (Exception e) {
			int errCode = 2106;
			String msg = "Error while determining quantiles " + this.getClass().getSimpleName();
			throw new ExecException(msg, errCode, PigException.BUG, e);
		}
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

	static public class Initial extends EvalFunc<Tuple> {
		@Override
		public Tuple exec(Tuple input) throws IOException {
			return input;
		}
	}

	static public class Intermediate extends EvalFunc<Tuple> {
		private static TupleFactory mTupleFactory = TupleFactory.getInstance();
		private static final long MAX_TOT_ELEMS = 1024L * 1024L * 1024L * 1024L;
		private final List<List<Double>> buffer = new ArrayList();
		private final int numQuantiles = 5;
		private static int maxElementsPerBuffer;
		private static long totalElements = 0L;
		private static double min = Double.MAX_VALUE;
		private static double max = Double.MIN_VALUE;
		private static double sum = 0d;

		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				maxElementsPerBuffer = computeMaxElementsPerBuffer();

				//TODO: examine why this is a Tuple containing a Bag containing a Tuple containing a Bag

				DataBag values = (DataBag) input.get(0);
				Iterator<Tuple> it = values.iterator();
				long progressCounter=0;
				while (it.hasNext()) {
					Tuple t = it.next();
					// notify pig/hadoop that we are still progressing
					if ((++progressCounter % 1000) == 0) {
						progressCounter=0;
						progress();
					}
					Object o = t.get(0);
					if (o == null) {
						continue;
					}
					if (o != null && o instanceof Number) {
						add(((Number) o).doubleValue());
					} else if (o instanceof DataBag) {
						Iterator<Tuple> it2 = ((DataBag) o).iterator();
						while (it2.hasNext()) {
							Tuple t2 = it2.next();
							Object o2 = t2.get(0);
							if (o2 == null) {
								continue;
							}
							if (o2 != null && o2 instanceof Number) {
								add(((Number) o2).doubleValue());
							} else if (o2 instanceof DataBag) {
								Iterator<Tuple> it3 = ((DataBag) o2).iterator();
								while (it3.hasNext()) {
									Tuple t3 = it3.next();
									Object o3 = t3.get(0);
									if (o3 == null) {
										continue;
									}
									if (o3 != null && o3 instanceof Number) {
										add(((Number) o3).doubleValue());
									}
								}
							}
						}
					}
				}

				if (buffer != null && buffer.size() >= 1)
				{
					BagFactory bagFactory = BagFactory.getInstance();
					DataBag dataBag = bagFactory.newDefaultBag();
					Tuple minMaxSumCountTuple = mTupleFactory.newTuple(4);
					minMaxSumCountTuple.set(0, min);
					minMaxSumCountTuple.set(1, max);
					minMaxSumCountTuple.set(2, sum);
					minMaxSumCountTuple.set(3, totalElements);
					dataBag.add(minMaxSumCountTuple);

					for (List<Double> tempbuf : buffer)
					{
						Tuple tuple = mTupleFactory.newTuple(tempbuf.size());
						for (int i = 0; i < tempbuf.size(); i++)
						{
							tuple.set(i, tempbuf.get(i));
						}
						dataBag.add(tuple);
					}
					Tuple returnTuple = mTupleFactory.newTuple(1);
					returnTuple.set(0, dataBag);
					cleanup();
					return returnTuple;
				}
				Tuple returnVal = mTupleFactory.newTuple(1);
				returnVal.set(0, "{0,0,0,0,0}");
				return returnVal;
			} catch (Exception e) {
				int errCode = 2106;
				String msg = "Error while --- >NHcomputing values in " + this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, e);
			}
		}

		private void cleanup() {
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			sum = 0d;
		}

		private int computeMaxElementsPerBuffer()
		{
			double epsilon = 1.0 / (numQuantiles - 1.0);
			int b = 2;
			while ((b - 2) * (0x1L << (b - 2)) + 0.5 <= epsilon * MAX_TOT_ELEMS) {
				++b;
			}
			return (int) (MAX_TOT_ELEMS / (0x1L << (b - 1)));
		}

		private void ensureBuffer(int level, List<List<Double>> sourceBuffer)
		{
			while (sourceBuffer.size() < level + 1) {
				sourceBuffer.add(null);
			}
			if (sourceBuffer.get(level) == null) {
				sourceBuffer.set(level, new ArrayList<Double>());
			}
		}

		private void collapse(List<Double> a, List<Double> b, List<Double> out)
		{
			int indexA = 0, indexB = 0, count = 0;
			if (a == null || a.size() == 0) return;
			if (b == null || b.size() == 0) return;
			Double smaller = null;
			while (indexA < maxElementsPerBuffer || indexB < maxElementsPerBuffer) {
				if (indexA >= maxElementsPerBuffer ||
						(indexB < maxElementsPerBuffer && a.get(indexA) >= b.get(indexB))) {
					smaller = b.get(indexB++);
				} else {
					smaller = a.get(indexA++);
				}

				if (count++ % 2 == 0) {
					out.add(smaller);
				}
			}
			a.clear();
			b.clear();
		}

		private void recursiveCollapse(List<Double> bufB, int level, List<List<Double>> sourceBuffer)
		{
			ensureBuffer(level + 1, sourceBuffer);

			List<Double> merged;
			if (sourceBuffer.get(level + 1).isEmpty()) {
				merged = sourceBuffer.get(level + 1);
			} else {
				merged = new ArrayList(maxElementsPerBuffer);
			}

			collapse(sourceBuffer.get(level), bufB, merged);
			if (sourceBuffer.get(level + 1) != merged) {
				recursiveCollapse(merged, level + 1, sourceBuffer);
			}
		}

		public void add(double elem)
		{
			if (totalElements == 0 || elem < min) {
				min = elem;
			}
			if (totalElements == 0 || max < elem) {
				max = elem;
			}
			sum += elem;

			if (totalElements > 0 && totalElements % (2 * maxElementsPerBuffer) == 0) {
				Collections.sort(buffer.get(0));
				Collections.sort(buffer.get(1));
				recursiveCollapse(buffer.get(0), 1, buffer);
			}

			ensureBuffer(0, buffer);
			ensureBuffer(1, buffer);
			int index = buffer.get(0).size() < maxElementsPerBuffer ? 0 : 1;
			buffer.get(index).add(elem);
			totalElements++;
		}
	}

	static public class Final extends EvalFunc<Tuple> {
		List<List<Double>> buffers = new ArrayList<List<Double>>();

		private final int numQuantiles = 5;
		private static long totalElements = 0L;
		private static double min = Double.MAX_VALUE;
		private static double max = Double.MIN_VALUE;
		private static double sum = 0d;

		@Override
		public Tuple exec(Tuple input) throws IOException {
			try {
				if (input != null)
				{
					DataBag bag = (DataBag) input.get(0);
					Iterator<Tuple> tuples = bag.iterator();
					//TODO 2: check what size this should be
					List<Double> buffer = new ArrayList<Double>((int)bag.size()); // loss of precision here but we will get back limited size due to Intermediate.computeMaxElementsPerBuffer()
					while (tuples.hasNext()) {
						Tuple tuple = tuples.next();
						DataBag innerBag = (DataBag)tuple.get(0);
						Iterator<Tuple> innerTuples = innerBag.iterator();

						Tuple minMaxSumCountTuple = innerTuples.next();

						min = (Double) minMaxSumCountTuple.get(0);
						max = (Double) minMaxSumCountTuple.get(1);
						sum = (Double) minMaxSumCountTuple.get(2);
						totalElements = (Long) minMaxSumCountTuple.get(3);

						while (innerTuples.hasNext())
						{
							Tuple innerTuple = innerTuples.next();
							for (int i = 0; i < innerTuple.size(); i++) {
								buffer.add((Double) innerTuple.get(i));
							}
							buffers.add(buffer);
							buffer = new ArrayList<Double>((int) bag.size()); // loss of precision here but we will get back limited size due to Intermediate.computeMaxElementsPerBuffer()
						}
					}

					List<Double> quantiles = getQuantiles();
					Tuple t = TupleFactory.getInstance().newTuple(quantiles.size()+1);

					for (int i = 0; i < quantiles.size(); i++)
					{
						t.set(i, quantiles.get(i));
					}
					t.set(quantiles.size(), getMean());
					return t;
				}
				return input;
			} catch (Exception ee) {
				getLogger().error(ee);
				int errCode = 2106;
				String msg = "Error while computing quantiles in "
						+ this.getClass().getSimpleName();
				throw new ExecException(msg, errCode, PigException.BUG, ee);
			}
		}
		public List<Double> getQuantiles()
		{
			List<Double> quantiles = new ArrayList<Double>();
			quantiles.add(min);
			if (buffers.size() > 0 && buffers.get(0) != null) {
				Collections.sort(buffers.get(0));
			}
			if (buffers.size() > 1 && buffers.get(1) != null) {
				Collections.sort(buffers.get(1));
			}

			int[] index = new int[buffers.size()];
			long S = 0;
			for (int i = 1; i <= numQuantiles - 2; i++) {
				long targetS =
						(long) Math.floor(i * (totalElements / (numQuantiles - 1.0))) + 1;  //changed to match database  (used to be ceiling without the +1)
				while (true) {
					double smallest = max;
					int minBufferId = -1;
					for (int j = 0; j < buffers.size(); j++) {
						if (buffers.get(j) != null && index[j] < buffers.get(j).size()) {
							if (!(smallest < buffers.get(j).get(index[j]))) {
								smallest = buffers.get(j).get(index[j]);
								minBufferId = j;
							}
						}
					}

					long incrementS = minBufferId <= 1 ? 1L : (0x1L << (minBufferId - 1));
					if (S + incrementS >= targetS) {
						quantiles.add(smallest);
						break;
					} else {
if (minBufferId <= 0)
{
	System.out.println("minBufferId: " + minBufferId);
}
						index[minBufferId]++;
						S += incrementS;
					}
				}
			}

			quantiles.add(max);

			return quantiles;
		}
		public double getMean()
		{
			double mean = 0.0;
			if (totalElements != 0)
				mean =  sum /  totalElements;
			return mean;
		}
	}
}
