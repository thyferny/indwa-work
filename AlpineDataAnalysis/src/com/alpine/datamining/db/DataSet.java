/**
 * ClassName DataSet.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;


/**
 * DataSet is the interface of data set for algorithms.
 * @author Eason
 */
public interface DataSet extends ConsumerProducer, Cloneable ,Iterable<Data> {

	
	/**
	 * clone
	 * @return
	 */
	public Object clone();

	/**
	 * equals
	 * @param o
	 * @return
	 */
	public boolean equals(Object o);
	
    /**
     * @return all columns
     */
    public Columns getColumns();
    

    /**
     * @return size
     */
    public long size();
    
 	/**
 	 * @return DBTable
 	 */
 	public Table getDBTable();

	/**
	 * compute all column statistics
	 * @throws WrongUsedException
	 */
	public void computeAllColumnStatistics() throws WrongUsedException;

	/**
	 * compute column statistics
	 * @param label
	 * @throws WrongUsedException
	 */
	public void computeColumnStatistics(Column label) throws WrongUsedException;

 	/**
 	 * get statistics by static name
 	 * @param labelColumn
 	 * @param mode
 	 * @return
 	 */
 	public double getStatistics(Column labelColumn, String mode);
 	
 	/**
 	 * 
 	 */
 	public void calculateAllNumericStatistics();

	/**
	 * @param i
	 * @return the ith data
	 */
	public Data getRow(int i);

	/**
	 * @param label
	 * @param count
	 * @param value
	 * @return statistic for column.
	 */
	public double getStatistics(Column label, String count, String value);

}
