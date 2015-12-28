
package com.alpine.datamining.db;

import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;



public interface DataSet extends ConsumerProducer, Cloneable ,Iterable<Data> {

	
	
	public Object clone();

	
	public boolean equals(Object o);
	
    
    public Columns getColumns();
    

    
    public long size();
    
 	
 	public Table getDBTable();

	
	public void computeAllColumnStatistics() throws WrongUsedException;

	
	public void computeColumnStatistics(Column label) throws WrongUsedException;

 	
 	public double getStatistics(Column labelColumn, String mode);
 	
 	
 	public void calculateAllNumericStatistics();

	
	public Data getRow(int i);

	
	public double getStatistics(Column label, String count, String value);

}
