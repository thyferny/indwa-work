/**
 * ClassName Tree
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.Tools;

/**
 * A tree is a node in a tree model 
 * 
 * Leafs contain the class label which should be predicted.
 */
public class Tree implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7082725237610097022L;

	private String label = null;
	
//	private String displayLabel = null;
    
    private List<Side> children = new LinkedList<Side>();
    
    private Map<String,Integer> counterMap = new LinkedHashMap<String, Integer>();
    
    private transient DataSet trainingSet = null;
    
    public Tree(DataSet trainingSet) {
        this.trainingSet = trainingSet;
    }
    
    public DataSet getTrainingSet() {
        return this.trainingSet;
    }
    
    public void addCount(String className, int count) {
        counterMap.put(className, count);
    }
    
    public int getCount(String className) {
        Integer count = counterMap.get(className);
        if (count == null)
            return 0;
        else
            return count;
    }
    
    public int getFrequencySum() {
        int sum = 0;
        for (Integer i : counterMap.values()) {
            sum += i;
        }
        return sum;
    }
    
    public int getSubtreeFrequencySum() {
    	if (children.size() == 0) {
    		return getFrequencySum();
    	} else {
    		int sum = 0;
    		for (Side edge : children) {
    			sum += edge.getChild().getSubtreeFrequencySum();
    		}
    		return sum;
    	}
    }
    
    public Map<String, Integer> getCounterMap() {
    	return counterMap;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void addChild(Tree child, DevideCond condition) {
        this.children.add(new Side(child, condition));
        setLabel(condition.getColumnName());
        Collections.sort(this.children);
    }
    
    
    public void removeChildren() {
        this.children.clear();
    }
    
    public boolean isLeaf() {
        return children.size() == 0;
    }
    
    public String getLabel() { 
        return this.label;
    }
    
    public Iterator<Side> childIterator() {
        return children.iterator();
    }
    public List<String> getStats()
    {
    	List<String> stats = new ArrayList<String>();
    	Iterator<Map.Entry<String, Integer>> keyValuePairs = counterMap.entrySet().iterator();
    	int mapsize = counterMap.size();
    	for (int i = 0; i < mapsize; i++)
    	{
    	  Map.Entry<String, Integer> entry = keyValuePairs.next();
    	  String key = entry.getKey();
    	  String value = entry.getValue().toString();
    	  stats.add("Cnt("+key+"): "+value);
    	}
    	return stats;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toString(null, this, "", buffer);
        return buffer.toString();
    }
    
    public int countLeaf() {
        int leafNumb = 0;
        leafNumb=countLeaf( this, leafNumb);
        return leafNumb;
    }
    
    
    private void toString(DevideCond condition, Tree tree, String indent, StringBuffer buffer) {
        if (condition != null) {
            buffer.append(condition.toString());
        }
        if (!tree.isLeaf()) {
            Iterator<Side> childIterator = tree.childIterator();
            while (childIterator.hasNext()) {
                buffer.append(Tools.getLineSeparator());
                buffer.append(indent);
                Side edge = childIterator.next();
                toString(edge.getCondition(), edge.getChild(), indent + "|   ", buffer);
            }
        } else {
            buffer.append(": ");
            buffer.append(tree.getLabel());
            buffer.append(" " + tree.counterMap.toString());
        }
    }
    
    private int countLeaf( Tree tree,int leafNumber) {
      
        if (!tree.isLeaf()) {
            Iterator<Side> childIterator = tree.childIterator();
            while (childIterator.hasNext()) {
                
                Side edge = childIterator.next();
                leafNumber+=countLeaf( edge.getChild(), leafNumber);
            }
        } else {
        	leafNumber=1;
        }
        return leafNumber;
    }

 
//	public String getDisplayLabel() {
//		return displayLabel;
//	}
//
//	public void setDisplayLabel(String displayLabel) {
//		this.displayLabel = displayLabel;
//	}
  
    
}
