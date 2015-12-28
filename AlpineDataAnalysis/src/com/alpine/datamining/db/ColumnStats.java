
package com.alpine.datamining.db;

import java.io.Serializable;


public interface ColumnStats extends Serializable {
    
    
    public static final String MODE              = "mode";
    public static final String LEAST             = "least";
    public static final String COUNT             = "count";
    public static final String AVERAGE           = "average";
    public static final String VARIANCE          = "variance";
    public static final String SUM               = "sum";
	public static final String MAXIMUM           = "maximum";
	public static final String MINIMUM           = "minimum";
    
    
    public Object clone();
    
    
    public void startCount(Column column);

    
    public void count(double value, double weight);
    
    
    public boolean handleStatistics(String statisticsName);
    
    
    public double getStatistics(Column column, String statisticsName, String parameter);
    
}
