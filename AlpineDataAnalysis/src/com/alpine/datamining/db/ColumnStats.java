/**
 * ClassName ColumnStats.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.io.Serializable;

/** The superclass for all column statistics objects. 
 * @author Eason
 */
public interface ColumnStats extends Serializable {
    
    /**
     * indicate the max count of value of the nominal column
     */
    public static final String MODE              = "mode";
    public static final String LEAST             = "least";
    public static final String COUNT             = "count";
    public static final String AVERAGE           = "average";
    public static final String VARIANCE          = "variance";
    public static final String SUM               = "sum";
	public static final String MAXIMUM           = "maximum";
	public static final String MINIMUM           = "minimum";
    
    /**
     * clone
     * @return
     */
    public Object clone();
    
    /**
     * start statistic
     * @param column
     */
    public void startCount(Column column);

    /**
     * statistic
     * @param value
     * @param weight
     */
    public void count(double value, double weight);
    
    /**
     * whether the stats can handle the statisticsName
     * @param statisticsName
     * @return true or false
     */
    public boolean handleStatistics(String statisticsName);
    
    /**
     * @param column
     * @param statisticsName
     * @param parameter
     * @return Statistic by column, name and other parameter
     */
    public double getStatistics(Column column, String statisticsName, String parameter);
    
}
