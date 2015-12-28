
package com.alpine.datamining.operator.tree.threshold;

import java.io.Serializable;


public class Side implements Serializable, Comparable<Side> {
    
	
	private static final long serialVersionUID = -5512242987059258940L;

	private DevideCond condition;
	
    private Tree child;
    
    public Side(Tree child, DevideCond condition) {
        this.condition = condition;
        this.child = child;
    }
    
    public DevideCond getCondition() { 
        return this.condition; 
    }
    
    public Tree getChild() { 
        return this.child; 
    }

	public int compareTo(Side o) {
		return (this.condition.getRelation() + this.condition.getValueString()).compareTo(o.condition.getRelation() + o.condition.getValueString());
	}
}
