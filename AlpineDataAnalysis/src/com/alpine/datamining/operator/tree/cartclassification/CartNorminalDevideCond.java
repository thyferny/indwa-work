
package com.alpine.datamining.operator.tree.cartclassification;

import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.operator.tree.threshold.AbstractDevideCond;


public class CartNorminalDevideCond extends AbstractDevideCond {
    
	
	private static final long serialVersionUID = -8004406184932008839L;

	private List<String> valueString;
	
	private boolean not;
	private String dbType;
	
	public boolean isNot() {
		return not;
	}

	public void setNot(boolean not) {
		this.not = not;
	}
	private Column column;    
    public CartNorminalDevideCond(String dbType, Column column, List<String> valueString,boolean not) {
        super(column.getName());
//      column.getMapping().getIndex(valueString);
      this.dbType = dbType;
      this.column = column;
      this.valueString = valueString;
      this.not = not;
  }
    
	public String getRelation() {
		StringBuffer ret = new StringBuffer();
		if (not)
		{
			ret.append(" not in ");
		}
		else
		{
			ret.append(" in ");
		}
		return ret.toString();
	}

	
	public List<String> getValueStringList() {
	
		return valueString;
	}
	
	
	public String getValueString() {
		StringBuffer value = new StringBuffer();
		value.append(" (");

		for (int i = 0; i < valueString.size(); i++)
		{
			if (i != 0)
			{
				value.append(",");
			}
			value.append(CommonUtility.quoteValue(dbType, column, valueString.get(i)));
		}
		value.append(") ");
		return value.toString();
	}
	public String getReadableValueString() {
		StringBuffer value = new StringBuffer();
		value.append(" (");

		for (int i = 0; i < valueString.size(); i++)
		{
			if (i != 0)
			{
				value.append(",");
			}
			value.append(valueString.get(i));
		}
		value.append(") ");
		return value.toString();
	}
}
