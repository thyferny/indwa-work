package com.alpine.datamining.api.impl.hadoop.models;

import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModel;
import com.alpine.hadoop.naivebayes.NaiveBayesTrainer;
import com.alpine.hadoop.util.KnuthMorrisPratt;
import org.apache.hadoop.io.Text;

import java.util.*;

public class NaiveBayesHadoopModel extends AbstractHadoopModel {
    private long numSamples_;
    private List<String> isRealValue_;
    private List<String> featureName_;
    private List<String> featureValue_;
    private List<String> className_;
    private List<String> classValue_;
    private List<String> featureValueCount_;
    private List<String> classValueCount_;
    private List<String> mean_;
    private List<String> variance_;

    private HashMap<String,Long> distinctClassValues_;
    private String modelFileName;
    private  String DependentcolumnName_; // keeps the name of the Y colum

    private String columnNames;


    public NaiveBayesHadoopModel(long num_samples, List<String> isRealValue,List<String> featureName,
                                 List<String> featureValue,List<String> className,
                                 List<String> classValue,List<String> featureValueCount,
                                 List<String> classValueCount,List<String> mean,
                                 List<String> variance, HashMap<String,Long> distinctClassValues,
                                 String DependedCol

    ){
        numSamples_ = num_samples;
        isRealValue_ = isRealValue;
        featureName_= featureName;
        featureValue_ = featureValue;
        className_ = className;
        classValue_ = classValue;
        featureValueCount_ = featureValueCount;
        classValueCount_ = classValueCount;
        mean_ = mean;
        variance_ = variance;
        distinctClassValues_ = distinctClassValues;
        DependentcolumnName_ = DependedCol;
  }


    private String dependentAttribute = null;

    public long getNumSamples_() {
        return numSamples_;
    }

    public void setNumSamples_(long numSamples_) {
        this.numSamples_ = numSamples_;
    }

    public List<String> getRealValue_() {
        return isRealValue_;
    }

    public void setRealValue_(List<String> realValue_) {
        isRealValue_ = realValue_;
    }

    public List<String> getFeatureName_() {
        return featureName_;
    }

    public void setFeatureName_(List<String> featureName_) {
        this.featureName_ = featureName_;
    }

    public List<String> getFeatureValue_() {
        return featureValue_;
    }

    public void setFeatureValue_(List<String> featureValue_) {
        this.featureValue_ = featureValue_;
    }

    public List<String> getClassName_() {
        return className_;
    }

    public void setClassName_(List<String> className_) {
        this.className_ = className_;
    }

    public List<String> getClassValue_() {
        return classValue_;
    }

    public void setClassValue_(List<String> classValue_) {
        this.classValue_ = classValue_;
    }

    public List<String> getFeatureValueCount_() {
        return featureValueCount_;
    }

    public void setFeatureValueCount_(List<String> featureValueCount_) {
        this.featureValueCount_ = featureValueCount_;
    }

    public List<String> getClassValueCount_() {
        return classValueCount_;
    }

    public void setClassValueCount_(List<String> classValueCount_) {
        this.classValueCount_ = classValueCount_;
    }

    public List<String> getMean_() {
        return mean_;
    }

    public void setMean_(List<String> mean_) {
        this.mean_ = mean_;
    }

    public List<String> getVariance_() {
        return variance_;
    }

    public void setVariance_(List<String> variance_) {
        this.variance_ = variance_;
    }


    public HashMap<String,Long> getDistinctClassValues_() {
        return distinctClassValues_;
    }

    public void setDistinctClassValues_(HashMap<String,Long> distinctClassValues_) {
        this.distinctClassValues_ = distinctClassValues_;
    }

    public String getModelFileName() {
        return modelFileName;
    }

    public void setModelFileName(String modelFileName) {
        this.modelFileName = modelFileName;
    }



    public String getDependentcolumnName_() {
        return DependentcolumnName_;
    }

    public void setDependentcolumnName_(String dependentcolumnName_) {
        DependentcolumnName_ = dependentcolumnName_;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

}
