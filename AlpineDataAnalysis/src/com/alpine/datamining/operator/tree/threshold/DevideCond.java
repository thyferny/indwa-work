
package com.alpine.datamining.operator.tree.threshold;

import java.io.Serializable;


public interface DevideCond extends Serializable {

	public String getColumnName();
	
	public String getRelation();
	
	public String getValueString();    
	public String getReadableValueString();

}
