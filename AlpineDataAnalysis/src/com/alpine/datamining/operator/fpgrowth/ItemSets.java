
package com.alpine.datamining.operator.fpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.Tools;


public class ItemSets extends OutputObject implements Iterable<ItemSet> {

	
	private static final long serialVersionUID = -2938299071173105508L;

	private static final int MAX_NUMBER_OF_ITEMSETS = 100;

	private static final String File_Extension = "frq";

	private static final String File_Description =  "frequent item set";

	private long numberOfTransactions=0;

    private int maximumSetSize = 0;
    
	private ArrayList<ItemSet> frequentSets;

	private DataSet dataSet;
	
	private String positiveValue;

	public ItemSets(long numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
		this.frequentSets = new ArrayList<ItemSet>();
	}
	
	public ItemSets(long numberOfTransactions, DataSet dataSet) {
		this.numberOfTransactions = numberOfTransactions;
		this.frequentSets = new ArrayList<ItemSet>();
		this.dataSet = dataSet;
	}

	
	public void addFrequentSet(ItemSet itemSet) {
		frequentSets.add(itemSet);
        maximumSetSize = Math.max(itemSet.getNumberOfItems(), maximumSetSize);
	}

	public String getExtension() {
		return File_Extension;
	}

	public String getFileDescription() {
		return File_Description;
	}

    public int getMaximumSetSize() {
        return this.maximumSetSize;
    }
    
	public Iterator<ItemSet> iterator() {
		return frequentSets.iterator();
	}
	
    public ItemSet getItemSet(int index) {
        return frequentSets.get(index);
    }
    
	public void sortSets() {
		Collections.sort(frequentSets);
	}

	public void sortSets(Comparator<ItemSet> comparator) {
		Collections.sort(frequentSets, comparator);
	}
	
    public int size() {
        return frequentSets.size();
    }
    
    public long getNumberOfTransactions() {
        return this.numberOfTransactions;
    }
    
    public String toResultString() {
        return toString(-1);
    }

    
    public String toString() {
        return toString(MAX_NUMBER_OF_ITEMSETS);
    }
    
    
    public String toString(int maxNumber) {
        StringBuffer output = new StringBuffer("Frequent Item Sets (" + size() + "):" + Tools.getLineSeparator());
        if (frequentSets.size() == 0) {
            output.append("no itemsets found");
        } else {
            int counter = 0;
            for (ItemSet set : frequentSets) {
                counter++;
                if ((maxNumber > 0) && (counter > maxNumber)) {
                    output.append("... " + (size() - maxNumber) + " additional item sets ...");
                    break;
                } else {
                    output.append(set.getItemsAsString());
                    output.append(" / ");
                    output.append((double)set.getFrequency() / (double)numberOfTransactions);
                    output.append(Tools.getLineSeparator());
                }
                
            }
        }
        return output.toString();
    }

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public String getPositiveValue() {
		return positiveValue;
	}

	public void setPositiveValue(String positiveValue) {
		this.positiveValue = positiveValue;
	}
    
}
