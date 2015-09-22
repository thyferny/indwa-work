package com.alpine.datamining.api.impl.db.evaluator;


import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.operator.evaluator.ConfusionMatrix;

import java.util.List;

public class ConfusionOutput extends AbstractAnalyzerOutPut {
    /**
     *
     */
    private static final long serialVersionUID = 5688641279802460103L;
    String name;
    String description;

    private List<ConfusionMatrix> resultList;
    /**
     * @param resultList
     */
    public ConfusionOutput(List<ConfusionMatrix> resultList) {
        this.resultList=resultList;

    }
    public List<ConfusionMatrix> getResultList() {
        return resultList;
    }
    public void setResultList(List<ConfusionMatrix> resultList) {
        this.resultList = resultList;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }



}