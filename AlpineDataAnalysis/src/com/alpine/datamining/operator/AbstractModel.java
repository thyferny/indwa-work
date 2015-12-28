
package com.alpine.datamining.operator;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBDataSet;


public abstract class AbstractModel extends OutputObject implements Model {

    
	private static final long serialVersionUID = -681238155782726884L;
	
    private DBDataSet headerDataSet;
    
    
    protected AbstractModel(DataSet dataSet) {
        if (dataSet != null)
            this.headerDataSet = new DBDataSet(dataSet);
    }
    
    
    public DBDataSet getTrainingHeader() {
        return this.headerDataSet;
    }
    
	
	public String getName() {
		String result = super.getName();
		if (result.toLowerCase().endsWith("model")) {
			result = result.substring(0, result.length() - "model".length());
		}
		return result;
	}
}
