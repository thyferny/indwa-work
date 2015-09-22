/**
 *
 * ClassName HadoopTree.java
 *
 * Version information: 1.00
 *
 * Sep 4, 2012
 * 
 * COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
 *
 */

package com.alpine.hadoop.tree.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.IntWritable;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Jonathan
 *  
 */

public class HadoopTree implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5586143787767572903L;
	private List<String> columnMap;
	public List<String> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(List<String> columnMap) {
		this.columnMap = columnMap;
	}

	private String label;
	private int feature;
	private int id;
	private boolean root = false;
	private int uniqueId;
	private boolean categorical;
	private boolean lessThan;
	private int[] counts;
	private int[] featureDemap;
	private List<HadoopTree> children = new ArrayList<HadoopTree>();
	private HashMap<String, Integer> dependantMapping;

	private static Logger itsLogger = Logger
			.getLogger(HadoopTree.class);

	public HadoopTree(String val, int feat, int nodeId, boolean cat, boolean lt) {
		this.label = val;
		this.feature = feat;
		this.id = nodeId;
		this.categorical = cat;
		this.lessThan = lt;
	}

	public String getValue() {
		return this.label;
	}

	// returns the label for the UI for this node.
	//
	// if a leaf node returns the total count.
	public String getLabel() {
		if(this.children.size() > 0) {
			HadoopTree child = this.children.get(0);
			return columnMap.get(child.getPureFeature());
		} else {
			int max = 0;
			int i =0;
			int feature = 0;
			for(int v : counts) {
				if(v > max) {
					max = v;
					feature = i;
				}
				i++;
			}

			String label = "Leaf";

			if(dependantMapping!=null&&dependantMapping.entrySet()!=null){
				for(Entry<String, Integer> mapEl : dependantMapping.entrySet()) {
					String key = mapEl.getKey();
					Integer val = mapEl.getValue();
	
					if(val.equals(feature)) {
						label = key;
					}
				}
			}
			return label;
		}
	}

	public int[] getCount() {
		return this.counts;
	}

	public String getConditionOperator() {
		if(categorical) {
			return "=";
		} else {
			return lessThan? "<=" : ">"; 
		}
	}

	public void setCount(int[] distribution) {
		this.counts = distribution;
	}

	public boolean isRoot() {
		return this.root;
	}

	public void setRoot(boolean val) {
		this.root = val;
	}

	public int getFeature() {
		return Arrays.binarySearch(this.featureDemap, feature);
	}

	public int getId() {
		return this.id;
	}

	public boolean isLeaf() {
		return this.children.size() == 0;
	}

	public void addChild(HadoopTree node) {
		children.add(node);
	}

	public List<HadoopTree> getChildren() {
		return this.children;
	}

	public boolean isCategorical() {
		return this.categorical;
	}

	public boolean getComparison() {
		return this.lessThan;
	}


	// Deep copy of tree
	public HadoopTree clone() {
		HadoopTree copy = new HadoopTree(this.label, this.feature, this.id, this.categorical, this.lessThan);

		for (HadoopTree child : this.children) {
			copy.addChild(child.clone());
		}

		return copy;
	}

	public int computeNode(List<String> vec) {
		
		if (this.children.size() == 0) {
			int i = 1;
			return this.id;
		}

		for(HadoopTree child : this.children) {
			
			if(child.isCategorical()){
				if(child.getValue().equals(vec.get(child.getFeature()))) {
					return child.computeNode(vec);
				}
			} else {
				try {
					boolean thisNode = false;
					Double childVal = new Double(child.getValue());
					Double vecVal = new Double(vec.get(child.getFeature()));

					thisNode = child.getComparison() ? vecVal <= childVal : childVal < vecVal;

					if(thisNode) {
							return child.computeNode(vec);
					}
				} catch(NumberFormatException e) {
					itsLogger.error("Node " + this.id + 
							" not categorical... but encountered categorical feature. " +
							vec, e);
					return -1;
				}
			}
		}

		return this.id;
	}

	public HadoopTree predictNode(List<String> vec) {
		if (this.children.size() == 0) {
			vec.removeAll(Collections.singletonList(null));
			return this;
		}

		for(HadoopTree child : this.children) {
			if(child.isCategorical()){
				if(child.getValue().equals(vec.get(child.getFeature()))) {
//					vec.set(child.getFeature(), null);

					return child.predictNode(vec);
				}
			} else {
				try {
					boolean thisNode = false;
					Double childVal = new Double(child.getValue());
					Double vecVal = new Double(vec.get(child.getFeature()));

					thisNode = child.getComparison() ? vecVal <= childVal : childVal < vecVal;

					if(thisNode) {
//						vec.set(child.getFeature(), null);

						return child.predictNode(vec);
					}
				} catch(NumberFormatException e) {
					itsLogger.error("Node " + child.id + 
							" not categorical... but encountered categorical feature. " +
							vec, e);
					return null;
				}
			}
		}

		return null;
	}

	public boolean equals(Object o) {
		if(o instanceof HadoopTree) {
			if(!((HadoopTree) o).getValue().equals(this.label) ||
					((HadoopTree) o).getPureFeature() != this.feature ||
					((HadoopTree) o).getId() != this.id || 
					((HadoopTree) o).getChildren().size() != this.children.size() ||
					((HadoopTree) o).getComparison() != this.lessThan ||
					((HadoopTree) o).isCategorical() != this.categorical ||
					!Arrays.equals(((HadoopTree) o).getCount(), this.counts)) {
				return false;
			}

			for (HadoopTree child : this.children) {
				boolean found = false;

				for (HadoopTree compare : ((HadoopTree) o).getChildren()) {
					if(child.equals(compare)) {
						found = true;
					}
				}

				if(!found) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// equality that does not check id of nodes. Used to check and test output result
	// of tree to be functionally equivalent as a model. 
	public boolean similar(Object o) {
		if(o instanceof HadoopTree) {
			if(!((HadoopTree) o).getValue().equals(this.label) ||
					((HadoopTree) o).getPureFeature() != this.feature ||
					((HadoopTree) o).getChildren().size() != this.children.size() ||
					((HadoopTree) o).getComparison() != this.lessThan ||
					((HadoopTree) o).isCategorical() != this.categorical ||
					!Arrays.equals(((HadoopTree) o).getCount(), this.counts)) {
				return false;
			}

			for (HadoopTree child : this.children) {
				boolean found = false;

				for (HadoopTree compare : ((HadoopTree) o).getChildren()) {
					if(child.similar(compare)) {
						found = true;
					}
				}

				if(!found) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public int getPureFeature() {
		return this.feature;
	}

	public HadoopTree getNode(int nodeId) {
		if(this.id == nodeId) {
			return this;
		}

		if (this.children.size() == 0) {
			return null;
		}

		for(HadoopTree child : this.children) {
			HadoopTree nodeReturn = child.getNode(nodeId);

			if(nodeReturn != null) {
				return nodeReturn;
			}
		}

		return null;
	}

	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public int getUniqueId() {
		return this.uniqueId;
	}

	public void updateUniqueId(int newId) {
		this.uniqueId = newId;
	}

	public String prettyPrint() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}

	public void setCoulmnMap(List<String> map) {
		this.columnMap = map;
	}

	public int expand(int split, Map<String, Double> splitMap2, String continuous, int newId, int[] selectedColumnsInt, List<String> columnMap2, HashMap<String, Integer> splitMap, int[] distribution) {
		if(continuous.equalsIgnoreCase("NULL")){
			int i=0;
			int eachLength=distribution.length/splitMap2.size();
			for(Entry<String, Double> mapEl : splitMap2.entrySet()) {
				String key = mapEl.getKey();

				HadoopTree newNode = new HadoopTree(key, split, newId, true, false);
				newNode.setDemap(selectedColumnsInt);
				newNode.setColumnMap(columnMap2);
				newNode.setDependent(splitMap);
				newNode.setCount(Arrays.copyOfRange(distribution,i*eachLength,(i+1)*eachLength));
				newId++;
				i=i+1;
				this.addChild(newNode);
			}
		} else {
			// a continuous value was selected for split

			HadoopTree newLessThanNode = new HadoopTree(continuous, split, newId, false, true);
			newLessThanNode.setDemap(selectedColumnsInt);
			newLessThanNode.setColumnMap(columnMap2);
			newLessThanNode.setDependent(splitMap);
			newLessThanNode.setCount(Arrays.copyOfRange(distribution,0, distribution.length/2));
			newId++;

			HadoopTree newGreaterThanNode = new HadoopTree(continuous, split, newId, false, false);
			newGreaterThanNode.setDemap(selectedColumnsInt);
			newGreaterThanNode.setColumnMap(columnMap2);
			newGreaterThanNode.setDependent(splitMap);
			newGreaterThanNode.setCount(Arrays.copyOfRange(distribution, distribution.length/2, distribution.length));
			newId++;

			this.addChild(newLessThanNode);
			this.addChild(newGreaterThanNode);
		}

		return newId;
	}

	public void setDependent(HashMap<String, Integer> hashMap) {
		this.dependantMapping = hashMap;
	}
	
	public HashMap<String, Integer> getDependentMap() {
		return this.dependantMapping;
	}
	
	public void setDemap(int[] d) {
		this.featureDemap = d;
	}

	public String[] predictDistribution(List<String> featureVec) {
		String[] dist = new String[counts.length];
		Double total = 0.0;

		Arrays.fill(dist, "0.0");

		//	String depend = featureVec.get(0);
		String val = this.getValue();
		//		if(depend.equals(val)) {
		//			dist[dependantMapping.get(depend).intValue()] = "1.0";
		//			return dist;
		//		} else {
		for(int c : counts) {
			total += c;
		}

		int i = 0;
		for(int c : counts) {
			dist[i] = String.valueOf(((double) c) / total); 
			i++;
		}

		return dist;
		//	}
	}
	
	

	public int[] getFeatureDemap() {
		return featureDemap;
	}

	public int foundFeatures(int key,ArrayList<TreeCondition> resultsFeatures ) {
		TreeCondition nodeCondition=null;
		if (this.children.size() == 0) {
			if(this.id != key)
				{
					return 0;
				}
			 else {
				 if(this.isRoot())
				 {
					 return 1;
				 }
				 if(this.categorical)
				 {
					nodeCondition=new CategoriCondition(this.label,this.feature);
				 }else{
					nodeCondition=new NumericCondition(Double.parseDouble(this.label),this.feature,this.lessThan);
				 }
				 resultsFeatures.add(nodeCondition);
 
				return 1;
			 }
		} 
		else 
		{
 
			for(HadoopTree child : this.children) {
				if(1== child.foundFeatures(key,resultsFeatures))
				{
					 if(this.isRoot())
					 {
						 return 1;
					 }
					if(this.categorical)
					{
						nodeCondition=new CategoriCondition(this.label,this.feature);
					}else{
//					if(this.lessThan)
						nodeCondition=new NumericCondition(Double.parseDouble(this.label),this.feature,this.lessThan);
					}
					resultsFeatures.add(nodeCondition);

					return 1;
		 
				}
 
			}
		}
		return 0;
		 
	}
}

