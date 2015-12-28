
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;


public class NorminalDevideCond extends AbstractDevideCond {
	
	private static final long serialVersionUID = 8684082108041210453L;
	private String valueString;
	private String dbType;
	private Column column;
    
    public NorminalDevideCond(String dbType, Column column, String valueString) {
        super(column.getName());
        this.dbType = dbType;
        this.column = column;
        column.getMapping().getIndex(valueString);
        this.valueString = valueString;
    }
    
//    public boolean test(Data data) {
//        double currentValue = data.getValue(data.getcolumns().get(getcolumnName()));
//        return CommonUtility.isEqual(currentValue, value);
//    }

	public String getRelation() {
		return "=";
	}

	public String getValueString() {
		return CommonUtility.quoteValue(dbType,column, valueString);
	}
	public String getReadableValueString()
	{
		return this.valueString;
	}
}
