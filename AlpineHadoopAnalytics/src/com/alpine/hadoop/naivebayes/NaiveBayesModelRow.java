package com.alpine.hadoop.naivebayes;
/**
 * This is the class that represents a row in the naive bayes final
 * model. Note that for real-valued feature the feature_Val_count is
 * filled with mean and variance in comma separated format.
 */
public class NaiveBayesModelRow {

    public boolean isrealValued_; // if the feature is real-valued
    public long featureValCount_;  // number of samples with this value in this class
    public long classValCount_; // number of samples with this class label
    public long totalSamples_; // total number of samples
    public float mean_; // mean of the gaussian fitted to the feature values withing that class
    public float variance_; // variance of the gaussian fitted to the feature values withing that class

    /*this is a constructor to vreate an object for categorical-valued   feature*/
    public NaiveBayesModelRow(boolean bisrealValued_, long featureValCount_, long clasValCount_, long totalSamples_) {
        this.isrealValued_ = bisrealValued_;
        this.featureValCount_ = featureValCount_;
        this.classValCount_ = clasValCount_;
        this.totalSamples_ = totalSamples_;
    }
    /*this is a constructor to create an object for real-valued  feature*/

    public NaiveBayesModelRow(boolean bisrealValued_, long clasValCount_, long totalSamples_, float mean_, float variance_) {
        this.isrealValued_ = bisrealValued_;
        this.classValCount_ = clasValCount_;
        this.totalSamples_ = totalSamples_;
        this.mean_ = mean_;
        this.variance_ = variance_;
    }
}