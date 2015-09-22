package com.alpine.datamining.api.impl.algoconf;


import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;

import java.util.ArrayList;
import java.util.List;

public class HadoopVariableSelectionConfig extends AbstractModelTrainerConfig {

    private static final List<String> parameterNames = new ArrayList<String>();
    public static final String PARAMETER_scoreType = "scoreType";

    static{

        parameterNames.add(PARAMETER_COLUMN_NAMES);
        parameterNames.add(ConstDependentColumn);
        parameterNames.add(PARAMETER_scoreType);
    }

    public HadoopVariableSelectionConfig(){
        setParameterNames(parameterNames );
        setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.VariableSelectionTextAndTableVisualizationType");
    }

    public HadoopVariableSelectionConfig( String columnNames){
        this();
        setColumnNames (columnNames);
    }



}
