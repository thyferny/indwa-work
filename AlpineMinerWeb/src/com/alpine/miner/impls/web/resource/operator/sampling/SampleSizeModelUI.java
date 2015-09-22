package com.alpine.miner.impls.web.resource.operator.sampling;



import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: SampleSizeModelUI
 * <p/>
 * Data: 12-7-12
 * <p/>
 * Author: Will
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class SampleSizeModelUI {


    List<String> sampleIdList = null ;// 1,2,3
    List<String> sampleSizeList = null;// 10,20,70


    SampleSizeModel realModel = null;

    public SampleSizeModelUI(){
        this.sampleIdList = new ArrayList<String>();
        this.sampleSizeList = new ArrayList<String>();
        this.realModel = new SampleSizeModel();
    }

    public SampleSizeModelUI(List<String> sampleIdList,List<String> sampleSizeList){
        this.sampleIdList = sampleIdList;
        this.sampleSizeList = sampleSizeList;
    }

    public SampleSizeModelUI(SampleSizeModel model){
            this.realModel = model;
            this.sampleIdList = model.getSampleIdList();
            this.sampleSizeList = model.getSampleSizeList();
    }


    public SampleSizeModel getRealModel(){
        this.realModel.setSampleIdList(this.sampleIdList);
        this.realModel.setSampleSizeList(this.sampleSizeList);
        return this.realModel;
    }

    public void  setRealModel(){
        this.realModel.setSampleIdList(this.sampleIdList);
        this.realModel.setSampleSizeList(this.sampleSizeList);
    }

    public List<String> getSampleIdList() {
        return sampleIdList;
    }

    public void setSampleIdList(List<String> sampleIdList) {
        this.sampleIdList = sampleIdList;
    }

    public List<String> getSampleSizeList() {
        return sampleSizeList;
    }

    public void setSampleSizeList(List<String> sampleSizeList) {
        this.sampleSizeList = sampleSizeList;
    }
}
