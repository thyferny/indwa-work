/**
*
* ClassName DecisionTreeConfigureKeySet.java
*
* Version information: 1.00
*
* Sep 24, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop;

/**
 * @author Shawn
 *  
 */

public class DecisionTreeConfigureKeySet implements AlpineHadoopConfKeySet {
 
//	public static String OUTPUT_ASSIGNMENTS="outputAssignments";
	public static String ROOT_LABEL="tree.rootName";
	public static String LABEL_INDEX="tree.labelIndex";
	public static String FEATURE_LENGTH="tree.featureLength";
	public static String MINIMUM_GAIN="tree.minGain";
	public static String MINIMUM_NODE_SIZE="tree.minNodeSize";
	public static String CONTINUOUS_COLUMNS="tree.continuousCol";
	public static String TREE_FILE="tree.treeFile";
	public static String SPLIT_FILE="tree.splitsFile";
	public static String DEPENDANT_COLUMN="tree.classificationIndex";
	public static String MAX_DEPTH="tree.maxDepth";
	public static String COLUMNS="tree.columns";
	public static String MAXDISTINCTNUMBER="tree.maxdistinctnumber";
	public static String FINALNODESLIST="tree.finalnodeslist";
	public static String DISTRIBUTENUMBER="tree.distributenumber";
}

