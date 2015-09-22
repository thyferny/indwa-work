/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * OutPutVisualAdapter.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
/**
 * OutPutVisualAdapter is an interface can transfer the analytic output to visual model.
 * It will extract any info about the algorithm to compose them to a POJO VisualizationModel.
 * There should have no reference to the analysis library. 
 * */
public interface OutPutVisualAdapter {
	//current visualization support max 30 elements in the chart
	//So we have max 30 colors 
	public static final String[] CONST_Colors = new String[]{
		"#0000EE","#00EE00","#EE0000",
		"#0000BB","#00BB00","#BB0000",
		"#000088","#008800","#880000",
		"#005500",
		"#3f9998","#Cf446f","#3fc0c3","#70c058","#c663a6",
        "#5fBBD8","#Df648f","#5fD0D3","#90D078","#D683B6",
        "#7fDDD8","#Ef84Af","#7fE0E3","#B0E098","#E6A3C6",
        "#9fFFF8","#FfA4Cf","#9fF0F3","#D0F0A8","#F6C3D6"
	};
	
	public static final int MAX_LENGTH_ROTATION_DEFAULT = 8; 
	
	public static final String WEIGHTS = "Weights";
	public static final String STANDARD_DEVIANTION = "Standard Deviantion";
	public static final String MEAN = "Mean";
	public static final String CLASS = "Class";
	public static final String CLASS_PRIORS = "class priors";
	public static final String PRIORS = "priors";
	public static final String Fraction_of_Variance_Explained = "Fraction of Variance Explained";
	public static final String CHI_SQUARE = "chiSquare";
	public static final String NULL_DEVIANCE = "nullDeviance";
	public static final String DEVIANCE = "Deviance";
	public static final String WALD = "Wald";
	public static final String Z_VALUE = "Z-value";
	public static final String ODDS_RATIO = "Odds Ratio";
	public static final String BIAS_OFFSET = "Bias (offset)";
	public static final String SENSITIVITY = "Sensitivity";
	public static final String SPECIFICITY = "Specificity";
	public static final String F1 = "F1";
	public static final String PRECISION = "Precision";
	public static final String RECALL = "Recall";
	public static final String STATS = "Stats";
	public static final String ERROR2 = "Error";
	public static final String ACCURACY = "Accuracy";
	public static final String NN_NODE = "NNNode";
	public static final String WOE = "WOE";
	public static final String IV = "IV";
	public static final String VALUE = "Value";
	public static final String INFORMATION_VALUE_WIGHT_OF_EVIDENCE = "InformationValue/Wight of Evidence";
	
	public static final String R2 = "R2";
	public static final String S = "Standard Error";
	public static final String P_VALUE = "P-value";
	public static final String T_STATISTICS = "T-statistics";
 
	public static final String COEFFICIENT = "Coefficient";
	public static final String ATTRIBUTE = "Attribute";
	public static final String COLUMN_NAME = "Column Name";
	public static final String COUNT = "Count";
	public static final String R_BRAKE = ")";
	public static final String L_BRAKE = "(";
	public static final String BETA = "beta";
	public static final String UI_PARA2 = "ui_para2";
	public static final String SUMMARY = "Summary: ";
	public static final String D2 = "D: ";
	public static final String Q2 = "Q: ";
	public static final String P2 = "P: ";
	public static final String MA = "MA";
	public static final String AR = "AR";
	public static final String INTERCEPT = "Intercept"; 
 
 
	public static final String SE = "SE";
	public static final String RESULT = "Result";
	public static final String ID = "ID";

	public static final int DEFAULT_XLABEL_ROTATION = -30;
 
	VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,
			Locale locale) throws  Exception;
 

}
