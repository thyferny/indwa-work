package com.alpine.datamining.operator.evaluator;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: adehghani
 * Date: 1/25/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfusionMatrix extends  EvaluatorResultObjectAdapter{

    private long [][] confMX_; // the actual class counts
    private ArrayList<String> classIndexes_;
    private HashMap<String, Float> classRecall_; // class recalls
    private HashMap<String, Float> classPrecision_;// class Precisions
    private float accuracy_;// class accuracy

    public ConfusionMatrix (long [][] Mx,ArrayList<String> class_indx,
                            HashMap<String, Float> class_recall,
                            HashMap<String, Float> class_precision,
                            float accuracy
    ){
        confMX_ = Mx;
        classIndexes_ = class_indx;
        classRecall_ = class_recall;
        classPrecision_ = class_precision;
        accuracy_ = accuracy;
    }


    public long[][] getConfMX_() {
        return confMX_;
    }

    public ArrayList<String> getClassIndexes_() {
        return classIndexes_;
    }

    public HashMap<String, Float> getClassRecall_() {
        return classRecall_;
    }

    public HashMap<String, Float> getClassPrecision_() {
        return classPrecision_;
    }

    public float getAccuracy_() {
        return accuracy_;
    }
}