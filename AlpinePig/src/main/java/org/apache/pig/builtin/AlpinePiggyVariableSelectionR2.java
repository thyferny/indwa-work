package org.apache.pig.builtin;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;


public class AlpinePiggyVariableSelectionR2 extends EvalFunc<Tuple> implements Algebraic {
    private static TupleFactory mTupleFactory = TupleFactory.getInstance();
    private static BagFactory bagFactory = BagFactory.getInstance();
    private int numberOfTheColumns;
    private Tuple constants;

//    public AlpinePiggyVariableSelectionR2(Tuple constants)
//    {
//          this.constants = constants;
//    }

    private static Tuple determineR2(Tuple finalTuple) throws ExecException
    {
        int size = finalTuple.size();
        int numR2s = size/2;
        Tuple R2Tuple =  mTupleFactory.newTuple(numR2s);
        for (int i =0;i < numR2s;i++)
        {
            double r2top = ((Number) finalTuple.get(2*i)).doubleValue();
            double r2bottom = ((Number) finalTuple.get(2*i+1 )).doubleValue();


            R2Tuple.set(i,Double.valueOf(1 - (r2top/r2bottom)));
        }

        return R2Tuple;
    }


    private static Tuple initTuple(int numberOfColumns) throws ExecException {
        Tuple tuple = mTupleFactory.newTuple(2 * (numberOfColumns-1));
        //this is a result tuple - it will start as 0 for all columns.
        for (int i = 0; i < tuple.size(); i++) {
            tuple.set(i, Double.valueOf(0));
        }

        return tuple;
    }

    @Override
    public Tuple exec(Tuple input) throws IOException {
        try {
            if (input.size() != 2) {
                throw new ExecException(
                        "Bag needs two elements, the file to analyze and the constants from the first UDF");
            }

            Tuple constants = (Tuple) input.get(1);

            DataBag bag = (DataBag) input.get(0);
            numberOfTheColumns = bag.iterator().next().size();
            Tuple vaTuple = initTuple(numberOfTheColumns);
            Iterator<Tuple> it = bag.iterator();
            long progressCounter=0;
            while (it.hasNext()) {
                if ((++progressCounter % 1000) == 0) {
                    progress();
                }
                turnTupleIntoAggregationTuple(vaTuple, it.next(),constants);
            }
            Tuple abTuple = determineR2(vaTuple);
            return abTuple;
        } catch (ExecException ee) {
            ee.printStackTrace();
            throw ee;
        } catch (Exception e) {
            e.printStackTrace();
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

    static protected Tuple sum(Tuple input,EvalFunc evalFunc) throws NumberFormatException, IOException {
        DataBag values = (DataBag) input.get(0);
        int theSize = values.iterator().next().size();
        Tuple aggTuple = mTupleFactory.newTuple(theSize);
        for (int i = 0; i < aggTuple.size(); i++) {
            aggTuple.set(i, 0);
        }

        long progressCounter=0;
        for (Iterator<Tuple> it = values.iterator(); it.hasNext();) {
            Tuple t = it.next();

            if ((++progressCounter % 1000) == 0) {
                progressCounter=0;
                evalFunc.progress();
            }
            mergeIntoAppTuple(t,aggTuple);
        }
        return aggTuple;

    }




    private static void mergeIntoAppTuple(Tuple newTuple, Tuple aggTuple) throws ExecException
    {
        int size = newTuple.size();
        for (int i = 0; i < size;i++)
        {
            increaseTheValueOfElInTheTupleBy(aggTuple,i,((Number) newTuple.get(i)).doubleValue());
        }
    }


    private static void turnTupleIntoAggregationTuple(
            Tuple aggregatedTuple, Tuple newTuple, Tuple constants) throws ExecException {
        int numberOfTheColumns = newTuple.size();
        if (numberOfTheColumns < 2)
        {
            throw new ExecException("You need have have at least one indep column");
        }
        for (int i =0; i < numberOfTheColumns; i++){
            Object o = newTuple.get(0);
            if (o == null || !(o instanceof Number))
            {
                return; //if any null values, just skip the row
            }
        }

        int constantsSize = constants.size();
        int spotWhereABsStart = 3 * numberOfTheColumns-1;
        double count = ((Number) constants.get(0)).doubleValue();
        double depMean = ((Number) constants.get(1)).doubleValue();
        double depValue = ((Number) newTuple.get(0)).doubleValue();

        for (int i=1; i < numberOfTheColumns;i++)
        {
            double alpha = ((Number) constants.get(spotWhereABsStart + 2*(i-1))).doubleValue();
            double beta = ((Number) constants.get(spotWhereABsStart + 2*i-1)).doubleValue();
            Double indValue = ((Number) newTuple.get(i)).doubleValue();
            double temp_r2top = (depValue - alpha - (beta * indValue))*(depValue - alpha - (beta * indValue)) ;
            double temp_r2bottom =  (depValue - (depMean/count)) * (depValue - (depMean/count));

            increaseTheValueOfElInTheTupleBy(aggregatedTuple, 2*(i - 1), temp_r2top);
            increaseTheValueOfElInTheTupleBy(aggregatedTuple, 2*i - 1,temp_r2bottom);
        }
    }





    static public class Initial extends EvalFunc<Tuple> {
        @Override
        public Tuple exec(Tuple input) throws IOException {
            DataBag values = (DataBag) input.get(0);
            Tuple constants = (Tuple) input.get(1);
            int numberOfTheColumns = values.iterator().next().size();
            Tuple vaTuple = initTuple(numberOfTheColumns);

            Iterator<Tuple> it = values.iterator();
            while (it.hasNext()) {
                turnTupleIntoAggregationTuple(vaTuple, it.next(),constants);
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
                String msg = "Error intermediate "
                        + this.getClass().getSimpleName();
                throw new ExecException(msg, errCode, PigException.BUG, e);
            }
        }
    }

    static public class Final extends EvalFunc<Tuple> {
               @Override
        public Tuple exec(Tuple input) throws IOException {
            try {
                Tuple finalTuple =  sum(input,this);

                Tuple abTuple = determineR2(finalTuple);
                return abTuple;

            } catch (Exception ee) {
                int errCode = 2106;
                String msg = "Error final "
                        + this.getClass().getSimpleName();
                throw new ExecException(msg, errCode, PigException.BUG, ee);
            }
        }

    }


//    @Override
//    public Schema outputSchema(Schema input) {
//        if (input.size() != 2) {
//            throw new RuntimeException(
//                    "Bag needs two elements, the file to analyze and the constants from the first UDF");
//        }
//      return super.outputSchema(input);
//    }

}
