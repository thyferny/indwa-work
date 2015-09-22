package org.apache.pig.builtin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;


/**
 * User: sasher
 * Date: 11/12/12
 * Time: 2:02 PM
 *
 * NOTE: This code is based off of:
 *
 * https://github.com/linkedin/datafu/blob/master/src/java/datafu/pig/stats/Quantile.java
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
public class AlpinePiggyBoxPlotExact extends EvalFunc<Tuple>  {

       private Double[] quantiles = {0.0,0.25,0.5,0.75,1.0};


    private static class Pair<T1,T2>
    {
        public T1 first;
        public T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }


    private static Pair<Long, Long> getIndexes(double k, long N)
{
    double h = N*k + 0.5;
    long i1 = Math.min(Math.max(1, (long)Math.ceil(h - 0.5)), N);
    long i2 = Math.min(Math.max(1, (long)Math.floor(h + 0.5)), N);

    return new Pair<Long, Long>(i1, i2);
}

    @Override
    public Tuple exec(Tuple input)  throws IOException
    {
        DataBag bag = (DataBag)input.get(0);
        if (bag == null || bag.size() == 0)
            return null;

        Map<Long, Double> d = new HashMap<Long, Double>();
        long N = bag.size(), max_id = 1;

        for (int i=0; i < this.quantiles.length;i++) {

            Pair<Long, Long> idx = getIndexes(this.quantiles[i], N);

            d.put(idx.first, null);
            d.put(idx.second, null);
            max_id = Math.max(max_id, idx.second);
        }

        long i = 1;
        for (Tuple t : bag) {
            if (i > max_id)
                break;

            if (d.containsKey(i)) {
                Object o = t.get(0);
                if ((o instanceof Number))
                    d.put(i, ((Number) o).doubleValue());
            }
            i++;
        }

        Tuple t = TupleFactory.getInstance().newTuple(this.quantiles.length);
        int j = 0;
        for (double k : this.quantiles) {
            Pair<Long, Long> p = getIndexes(k, N);
            double quantile = (d.get(p.first) + d.get(p.second)) / 2;
            t.set(j, quantile);
            j++;
        }
        return t;
    }


    @Override
    public Schema outputSchema(Schema input)
    {
        Schema tupleSchema = new Schema();
        for (int i = 0; i < 5; i++) {
            tupleSchema.add(new Schema.FieldSchema("quantile_" + i, DataType.DOUBLE));
        }
        return tupleSchema;
    }

}



