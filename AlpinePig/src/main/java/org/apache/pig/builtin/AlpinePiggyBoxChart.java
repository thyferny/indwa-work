package org.apache.pig.builtin;

import org.apache.pig.Accumulator;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;

public class AlpinePiggyBoxChart extends EvalFunc<Tuple> implements Accumulator<Tuple> {

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
            String msg = "Error while determining quantiles "
                    + this.getClass().getSimpleName();
            throw new ExecException(msg, errCode, PigException.BUG, e);
        }
    }

    @Override
    public void accumulate(Tuple b) throws IOException
    {
        DataBag bag = (DataBag) b.get(0);
        if (bag == null || bag.size() == 0)
            return;

        for (Tuple t : bag) {
            Object o = t.get(0);
            if ((o instanceof Number)) {
                QuantileEstimator.getInstance().add(((Number) o).doubleValue());
            } else
            {
                //throw new IllegalStateException("bag must have numerical values (and be non-null)");
            }
        }
    }

    @Override
    public void cleanup()
    {
        QuantileEstimator.getInstance().clear();
    }

    @Override
    public Tuple getValue()
    {
        Tuple t = TupleFactory.getInstance().newTuple(QuantileEstimator.getInstance().getNumberOfQuantiles() + 1);
        try {
                int j = 0;
                for (double quantileValue : QuantileEstimator.getInstance().getQuantiles())
                {
                    t.set(j, quantileValue);
                    j++;
                }
                t.set(j, QuantileEstimator.getInstance().getMean());

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



