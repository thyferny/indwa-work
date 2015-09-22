/**
 * ClassName  RecommendationEvaluationConfig
 *
 * Version information: 1.00
 *
 * Data: 2011-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eason
 * 
 */
public class RecommendationEvaluationConfig extends DataOperationConfig {
	private String recommendationTable;
	private String recommendationIdColumn;
	private String recommendationProductColumn;
	private String preTable;
	private String preValueColumn;
	private String preIdColumn;
	private String postTable;
	private String postIdColumn;
	private String postValueColumn;
	private String postProductColumn;
	
	public static final String ConstRecommendationTable = "recommendationTable";
	public static final String ConstRecommendationIdColumn = "recommendationIdColumn";
	public static final String ConstRecommendationProductColumn = "recommendationProductColumn";
	public static final String ConstPreTable = "preTable";
	public static final String ConstPreValueColumn = "preValueColumn";
	public static final String ConstPreIdColumn = "preIdColumn";
	public static final String ConstPostTable = "postTable";
	public static final String ConstPostIdColumn = "postIdColumn";
	public static final String ConstPostValueColumn = "postValueColumn";
	public static final String ConstPostProductColumn = "postProductColumn";
	
	private static final List<String> parameterNames = new ArrayList<String>();


	static {
		parameterNames.add(ConstRecommendationTable);
		parameterNames.add(ConstRecommendationIdColumn);
		parameterNames.add(ConstRecommendationProductColumn);
		parameterNames.add(ConstPreTable);
		parameterNames.add(ConstPreValueColumn);
		parameterNames.add(ConstPreIdColumn);
		parameterNames.add(ConstPostTable);
		parameterNames.add(ConstPostIdColumn);
		parameterNames.add(ConstPostValueColumn);
		parameterNames.add(ConstPostProductColumn);
	}

	public RecommendationEvaluationConfig() {
		setParameterNames(parameterNames);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.ProductRecommendationEvaluationVisualiztionType");
	}

	public String getRecommendationTable() {
		return recommendationTable;
	}

	public void setRecommendationTable(String recommendationTable) {
		this.recommendationTable = recommendationTable;
	}

	public String getRecommendationIdColumn() {
		return recommendationIdColumn;
	}

	public void setRecommendationIdColumn(String recommendationIdColumn) {
		this.recommendationIdColumn = recommendationIdColumn;
	}

	public String getRecommendationProductColumn() {
		return recommendationProductColumn;
	}

	public void setRecommendationProductColumn(String recommendationProductColumn) {
		this.recommendationProductColumn = recommendationProductColumn;
	}

	public String getPreTable() {
		return preTable;
	}

	public void setPreTable(String preTable) {
		this.preTable = preTable;
	}

	public String getPreValueColumn() {
		return preValueColumn;
	}

	public void setPreValueColumn(String preValueColumn) {
		this.preValueColumn = preValueColumn;
	}

	public String getPreIdColumn() {
		return preIdColumn;
	}

	public void setPreIdColumn(String preIdColumn) {
		this.preIdColumn = preIdColumn;
	}

	public String getPostTable() {
		return postTable;
	}

	public void setPostTable(String postTable) {
		this.postTable = postTable;
	}

	public String getPostIdColumn() {
		return postIdColumn;
	}

	public void setPostIdColumn(String postIdColumn) {
		this.postIdColumn = postIdColumn;
	}

	public String getPostValueColumn() {
		return postValueColumn;
	}

	public void setPostValueColumn(String postValueColumn) {
		this.postValueColumn = postValueColumn;
	}

	public String getPostProductColumn() {
		return postProductColumn;
	}

	public void setPostProductColumn(String postProductColumn) {
		this.postProductColumn = postProductColumn;
	}
}
