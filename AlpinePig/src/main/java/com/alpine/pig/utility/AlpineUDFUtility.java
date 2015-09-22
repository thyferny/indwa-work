package com.alpine.pig.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import com.alpine.datamining.api.impl.hadoop.utility.PigColumnType;


public class AlpineUDFUtility {
    private static TupleFactory mTupleFactory = TupleFactory.getInstance();
    private static BagFactory bagFactory = BagFactory.getInstance();


    private static final String[] sampleStrings={"Zero"+"One","Two","Three","Four","Five"};
    private static final int[] intNumbers ={0,1,2,3,4,5,6};
    private static final Long[] longNumbers ={123L,234L,345L,456L,567L,645L,763L};
    private static final Double[] doubleNumbers ={123.0,234.2,345.3,456.3,567.5,645.6,763.7};

    public static Tuple createARandomRow(List<PigColumnType> columnTypes) throws ExecException {
        Tuple tuple = mTupleFactory.newTuple(columnTypes.size());
        int i=0;
        Random random = new Random();
        for(PigColumnType type:columnTypes){
            int indexNumber = Math.abs(random.nextInt());
            switch (type) {
                case Long:
                    tuple.set(i++, Long.valueOf(longNumbers[(indexNumber)%(longNumbers.length)]));
                    break;

                case Double:
                    tuple.set(i++, Double.valueOf(doubleNumbers[(indexNumber)%(doubleNumbers.length)]));
                    break;

                case Integer:
                    tuple.set(i++, Integer.valueOf(intNumbers[(indexNumber)%(intNumbers.length)]));
                    break;

                case Chararray:
                    tuple.set(i++,"str"+sampleStrings[(indexNumber)%(sampleStrings.length)]+indexNumber+"".toCharArray());
                    break;

                default:
                    tuple.set(i++,random.nextInt());
                    break;
            }
        }

        return tuple;

    }

    public static Tuple createARowWithTheValuesOf(PigColumnType[] columnTypes,String[] values) throws ExecException {
        if(null==columnTypes||null==values||columnTypes.length!=values.length){
            throw new IllegalArgumentException("ColumnTypes and values neither can be null nor they can have diffrent sizes");
        }
        return createARowWithTheValuesOf(Arrays.asList(columnTypes),Arrays.asList(values));
    }

    public static Tuple createARowWithTheValuesOf(List<PigColumnType> columnTypes,List<String> values) throws ExecException {

        if(null==columnTypes||null==values||columnTypes.size()!=values.size()){
            throw new IllegalArgumentException("ColumnTypes and values neither can be null nor they can have diffrent sizes");
        }

        Tuple tuple = mTupleFactory.newTuple(columnTypes.size());
        int i=0;
        for(PigColumnType type:columnTypes){
            switch (type) {
                case Long:
                    tuple.set(i, (null==values.get(i)||"".equals(values.get(i).trim())?null:Long.valueOf(values.get(i))));
                    break;

                case Double:
                    tuple.set(i, (null==values.get(i)||"".equals(values.get(i).trim())?null:Double.valueOf(values.get(i))));
                    break;

                case Integer:
                    tuple.set(i, (null==values.get(i)||"".equals(values.get(i).trim())?null:Integer.valueOf(values.get(i))));
                    break;

                case Chararray:
                    tuple.set(i,(null==values.get(i)||"".equals(values.get(i).trim())?null:values.get(i)));
                    break;

                default:
                    tuple.set(i,(null==values.get(i)||"".equals(values.get(i).trim())?null:values.get(i)));
                    break;
            }
            i++;
        }

        return tuple;

    }



    public static List<Tuple> createTuples(int numberOfRows,List<PigColumnType> columnTypes) throws ExecException{
        List<Tuple> tupleToCreate = new ArrayList<Tuple>();
        for(int i=0;i<numberOfRows;i++){
            tupleToCreate.add(createARandomRow(columnTypes));
        }

        return tupleToCreate;
    }


    public static Tuple bagifyTheTuple(Tuple t) {
        DataBag valueBag = bagFactory.newDefaultBag();
        valueBag.add(t);
        return mTupleFactory. newTuple(valueBag);
    }

    public static Tuple combineTuplesIntoANewTuple(List<Tuple> tuples) {
        DataBag valueBag = bagFactory.newDefaultBag();
        for(Tuple t:tuples){
            valueBag.add(t);
        }
        return mTupleFactory. newTuple(valueBag);
    }


    public static Map<Integer, List<Tuple>> divideInitResultIntoNumberOfPieces(
            List<Tuple> tuples,int reduceNumber) {
        Map<Integer,List<Tuple>> map=new HashMap<Integer,List<Tuple>>();
        int i=0;
        for(Tuple t:tuples){
            List<Tuple> list = map.get(i%reduceNumber);
            if(null==list){
                list = new ArrayList<Tuple>();
                map.put(i,list);
            }
            list.add(t);
            i++;
        }
        return map;
    }


    public static List<Tuple> includeSomeTextIntoOriginalTuples(List<Tuple> tuples,String commaDelimatedText) throws ExecException{
        //{1,2}{2,3}1,2,1,2,3,2 =>{{1,2}1,2,1,2,3,2}{{2,3},1,2,1,2,3,2}
        //{(85)},64.0,85.0,2.1)
        String[] minMaxStepValuesForEachColumn = commaDelimatedText.split(",");

        List<Tuple> initedTuples = new ArrayList<Tuple>();
        for(Tuple t:tuples){
            DataBag valueBag = bagFactory.newDefaultBag();
            DataBag vTup = bagFactory.newDefaultBag();
            vTup.add(t);
            Tuple tNew = mTupleFactory.newTuple(minMaxStepValuesForEachColumn.length+1);
            tNew.set(0, vTup);
            for(int i=0;i<minMaxStepValuesForEachColumn.length;i++){
                tNew.set(i+1, minMaxStepValuesForEachColumn[i]);
            }
            valueBag.add(tNew);
            initedTuples.add(mTupleFactory. newTuple(valueBag.iterator().next()));

        }
        return initedTuples;

    }

    public static List<Tuple> appendTheCommaDelimatedValuesIntoEachTuples(List<Tuple> tuples,String minMaxStepSize) throws ExecException{
        String[] minMaxStepValuesForEachColumn = minMaxStepSize.split(",");
        int tupleSize = tuples.get(0).size();

        List<Tuple> initedTuples = new ArrayList<Tuple>();
        for(Tuple t:tuples){
            Tuple tNew = mTupleFactory.newTuple(tupleSize+minMaxStepValuesForEachColumn.length);
            for(int i=0;i<tupleSize;i++){
                tNew.set(i, t.get(i));
            }

            for(int i=0;i<minMaxStepValuesForEachColumn.length;i++){
                tNew.set(i+tupleSize, minMaxStepValuesForEachColumn[i]);
            }

            initedTuples.add(tNew);

        }
        return initedTuples;

    }


    public static List<Tuple> extractIndividualTuplesFromTopTuple(Tuple initTuple) throws ExecException{
        List<Tuple> arrList=new ArrayList<Tuple>();
        DataBag db = (DataBag)initTuple.get(0);
        Iterator<Tuple> it = db.iterator();
        it.next();//There goes value tuple
        while(it.hasNext()){
            Tuple t2 = it.next();
            t2.size();
            DataBag db2=(DataBag)t2.get(0);
            Tuple t3= db2.iterator().next();
            arrList.add(t3);
        }

        return arrList;
    }

    public static Double[] extractMinsFromTuples(List<Tuple> tuples) throws ExecException {
        int columnSize = tuples.get(0).size();
        Double[] maxs=new Double[columnSize];
        for(Tuple t:tuples){
            for(int i=0;i<columnSize;i++){
                Object val = t.get(i);
                if(null==val)continue;
                Double d=  DataType.toDouble(val);
                maxs[i]=null==maxs[i]?d:(maxs[i]<d?maxs[i]:d);
            }

        }


        return maxs;
    }

    public static  Double[] extractMaxsFromTuples(List<Tuple> tuples) throws ExecException {
        int columnSize = tuples.get(0).size();
        Double[] maxs=new Double[columnSize];
        for(Tuple t:tuples){
            for(int i=0;i<columnSize;i++){
                Object val = t.get(i);
                if(null==val)continue;
                Double d=  DataType.toDouble(val);
                maxs[i]=null==maxs[i]?d:(maxs[i]>d?maxs[i]:d);
            }

        }


        return maxs;
    }

}
