/**
 * ClassName RegressionTree
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartregression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.tree.threshold.Side;
import com.alpine.datamining.operator.tree.threshold.DevideCond;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.utility.Tools;

/**
 * A tree is a node in a tree model
 */
public class RegressionTree extends Tree {
    
/**
	 * 
	 */
	private static final long serialVersionUID = -5892655273119897457L;

    private double avg = Double.NaN;
    
    
    private double deviance = Double.NaN;
    
    private long count = 0;

    
    public RegressionTree(DataSet trainingSet) {
        super(trainingSet);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toString(null, this, "", buffer);
        return buffer.toString();
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
//            buffer.append(tree.getLabel());
//            buffer.append(" " + tree.counterMap.toString());??????
            buffer.append((((RegressionTree)tree).getAvg()));
            buffer.append(" (deviance:").append((((RegressionTree)tree).getDeviance())).append(" count:").append(((RegressionTree)tree).getCount()).append(")");
        }
    }

     public String getLabel(){
    	 if (isLeaf()){
    		 return Double.toString(avg);
    	}else{
    		return super.getLabel() ;
    	}
     }
    
    public List<String> getStats()
    {
    	List<String> stats = new ArrayList<String>();
    	stats.add("Dev: "+(deviance));
    	stats.add("Cnt: "+count);
    	return stats;
    }

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}
//
//	public double getVariance() {
//		return variance;
//	}
//
//	public void setVariance(double variance) {
//		this.variance = variance;
//	}

	public long getCount() {
		return count;
	}

	public void setCount(long count2) {
		this.count = count2;
	}

	public double getDeviance() {
		return deviance;
	}

	public void setDeviance(double deviance) {
		this.deviance = deviance;
	}
}
