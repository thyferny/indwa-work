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
import org.apache.pig.impl.logicalLayer.schema.Schema;

/**
 * NO LONGER BEING USED - THIS HAS BEEN REPLACED BY STRAIGHT HADOOP.
 */
public class AlpinePiggyVariableSelection  extends EvalFunc<Tuple> implements
        Algebraic {
    private static TupleFactory mTupleFactory = TupleFactory.getInstance();
    private static BagFactory bagFactory = BagFactory.getInstance();
    private int numberOfTheColumns;
    private static String ALPINE_AGGREGATION_MARKER  = "ALPINE_AGGREGATION_MARKER";


    private static Tuple determineAlphasAndBetas(Tuple finalTuple) throws ExecException
    {
        int size = finalTuple.size() - 1;
        int origNumberOfColumn = (size + 1) / 3;
        Tuple AlpheBetaTuple =  mTupleFactory.newTuple(size + (origNumberOfColumn-1) * 2);
        for (int i = 0; i < size; i++) {
            AlpheBetaTuple.set(i, finalTuple.get(i));
        }
        double count = ((Number) finalTuple.get(0)).doubleValue();
        double sum_dep = ((Number) finalTuple.get(1)).doubleValue();

        for (int i =1;i < origNumberOfColumn;i++)
        {
            double  sum_ind =   ((Number) finalTuple.get(3*i - 1)).doubleValue();
            double  sum_ind_sq =   ((Number) finalTuple.get(3*i )).doubleValue();
            double  sum_ind_dep =   ((Number) finalTuple.get(3*i + 1)).doubleValue();

            double betatop = sum_ind_dep - (sum_ind)*(sum_dep)/count;
            double betabottom = sum_ind_sq - (sum_ind)*(sum_ind)/count;
            double beta = betatop / betabottom;
            double alpha = (sum_dep  - beta * sum_ind)/count;

            AlpheBetaTuple.set(size + 2*(i-1),Double.valueOf(alpha));
            AlpheBetaTuple.set(size+ 2*i-1,Double.valueOf(beta));
        }

        return AlpheBetaTuple;
    }


    private static Tuple initTuple(int numberOfColumns) throws ExecException {
        Tuple tuple = mTupleFactory.newTuple(3 * numberOfColumns); //added extra space for accumulation marker
        //this is a result tuple - it will start as 0 for all columns.
        for (int i = 0; i < tuple.size(); i++) {
            tuple.set(i, Double.valueOf(0));
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
                turnTupleIntoAggregationTuple(vaTuple, it.next());
            }
            Tuple abTuple = determineAlphasAndBetas(vaTuple);
            return abTuple;
        } catch (ExecException ee) {
            ee.printStackTrace();
            //itsLogger.error(ee);
            throw ee;
        } catch (Exception e) {
            e.printStackTrace();
            //itsLogger.error(e);
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
        markTheTupleAsAggregated(aggTuple);

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


    private static void markTheTupleAsAggregated(Tuple tuple) throws ExecException {
        tuple.set(tuple.size()-1, String.valueOf(ALPINE_AGGREGATION_MARKER));
    }

    private static void mergeIntoAppTuple(Tuple newTuple, Tuple aggTuple) throws ExecException
    {
        int size = newTuple.size() - 1; //skip the last spot - that's the aggregation marker.
        for (int i = 0; i < size;i++)
        {
            increaseTheValueOfElInTheTupleBy(aggTuple,i,((Number) newTuple.get(i)).doubleValue());
        }
    }


    private static void turnTupleIntoAggregationTuple(
            Tuple aggregatedTuple, Tuple newTuple) throws ExecException {
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

        Double depValue = ((Number) newTuple.get(0)).doubleValue();
        increaseTheValueOfElInTheTupleByOne(aggregatedTuple,0); //this ups the count by one.
        increaseTheValueOfElInTheTupleBy(aggregatedTuple,1,depValue);  // spot 1 is just a sum of the dep variable

        for (int i=1; i < numberOfTheColumns;i++)
        {
            Double indValue = ((Number) newTuple.get(i)).doubleValue();
            increaseTheValueOfElInTheTupleBy(aggregatedTuple, 3*i - 1, indValue);
            increaseTheValueOfElInTheTupleBy(aggregatedTuple, 3*i, indValue*indValue);
            increaseTheValueOfElInTheTupleBy(aggregatedTuple, 3*i + 1, indValue*depValue);
        }
    }



    /* Accumulator interface implementation */
//   private Tuple intermediateCount;

    // private Map<Double,Double> distincts = new HashMap<Double,Double>();

//    @Override
//    public void accumulate(Tuple b) throws IOException {
//        try {
//            DataBag bag = (DataBag) b.get(0);
//            Iterator<Tuple> it = bag.iterator();
//            while (it.hasNext()) {
//                Tuple t = (Tuple) it.next();
//                if (t != null && t.size() > 0) {
//                    sum(t,this);
//                }
//            }
//        } catch (ExecException ee) {
//            throw ee;
//        } catch (Exception e) {
//            e.printStackTrace();
//            //itsLogger.error(e);
//            int errCode = 2106;
//            String msg = "Error while computing min in "
//                    + this.getClass().getSimpleName();
//            throw new ExecException(msg, errCode, PigException.BUG, e);
//        }
//    }


    private static Tuple getActualTuple(Tuple iTup) throws ExecException
    {
        DataBag zero = (DataBag)iTup.get(0);
        Iterator<Tuple> zIt = zero.iterator();
        Tuple initedTuple = zIt.next();

        if(initedTuple.get(0) instanceof Tuple || initedTuple.get(0) instanceof DataBag){
            zero = (DataBag)initedTuple.get(0);
            zIt = zero.iterator();
            initedTuple = zIt.next();
        }

        return initedTuple;
    }

    private static boolean isAggregatedBefore(Tuple iTup) throws ExecException{
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
                if(ot.equals(ALPINE_AGGREGATION_MARKER)){
                    return true;
                }
            }

        }

        String ot=DataType.toString(iTup.get(iTup.size()-1));
        if(null==ot){
            //The last column might be null
            return false;
        }
        if(ot.equals(ALPINE_AGGREGATION_MARKER)){
            return true;
        }
        return false;
    }


//    static private DataBag createDataBag(int size) {
//        // by default, we create InternalSortedBag, unless user configures
//        // explicitly to use old bag
//
//        String bagType = null;
//        if (PigMapReduce.sJobConfInternal.get() != null) {
//            bagType = PigMapReduce.sJobConfInternal.get().get("pig.cachedbag.distinct.type");
//        }
//
//        DataBag topBag=null;
//
//        if (bagType != null && bagType.equalsIgnoreCase("default")) {
//            topBag= BagFactory.getInstance().newDistinctBag();
//        } else {
//            topBag= new InternalDistinctBag(3);
//        }
//
//        for(int i=0;i<size;i++){
//            if (bagType != null && bagType.equalsIgnoreCase("default")) {
//                topBag.addAll(BagFactory.getInstance().newDistinctBag());
//            } else {
//                topBag.addAll(new InternalDistinctBag(3));
//            }
//        }
//
//        return topBag;
//
//
//    }


    /**
     *  We do nothing at the initial stage - everything will be handled in intermediate.  That way,
     *  we have less tuples wandering around.
     */
    static public class Initial extends EvalFunc<Tuple> {
        @Override
        public Tuple exec(Tuple input) throws IOException {
            return input;

        }
    }

    /**
     * Since we do nothing at the initial phase, two things can happen here.
     * (1) We have initial tuples that have not been processed
     * (2) We have intermediate tuples that have already been turned into aggregate tuples
     *
     * To tell the difference between the two, we add an "already processed" flag when we
     * turn an initial tuple into an aggregate tuples.  Then the intermediate processors
     * knows what to do with each tuple.
     *
     */
    static public class Intermediate extends EvalFunc<Tuple> {

        @Override
        public Tuple exec(Tuple input) throws IOException {
            try {
                DataBag values = (DataBag) input.get(0);

                Tuple firstTuple = values.iterator().next();
                boolean isAggregatedBefore = isAggregatedBefore(firstTuple);

                if (isAggregatedBefore)
                {
                    return sum(input,this);
                }

                //first time through
                int numberOfTheColumns = getActualTuple(firstTuple).size();
                Tuple vaTuple = initTuple(numberOfTheColumns);
                markTheTupleAsAggregated(vaTuple);

                Iterator<Tuple> it = values.iterator();
                while (it.hasNext())
                {
                    turnTupleIntoAggregationTuple(vaTuple, getActualTuple(it.next()));
                }
                return vaTuple;

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

                Tuple abTuple = determineAlphasAndBetas(finalTuple);
                return abTuple;

            } catch (Exception ee) {
                int errCode = 2106;
                String msg = "Error final "
                        + this.getClass().getSimpleName();
                throw new ExecException(msg, errCode, PigException.BUG, ee);
            }
        }

    }

        @Override
    public Schema outputSchema(Schema input) {
            if (input.size() != 1) {
                throw new RuntimeException(
                        "Bag is empty or having more than one rows");
            }
            Schema.FieldSchema output = new Schema.FieldSchema(null, DataType.TUPLE);
      return new Schema(output);
    }

}
