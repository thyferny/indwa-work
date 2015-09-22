/**
 * ClassName JsonPigStorage010.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-16
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import org.apache.pig.impl.util.StorageUtil;

import com.alpine.hadoop.ext.CSVRecordParser;

 

/**
 * A load function that parses a line of input into fields using a delimiter to
 * set the fields. The delimiter is given as a regular expression. See
 * {@link java.lang.String#split(String)} and {@link java.util.regex.Pattern}
 * for more information.
 */
@SuppressWarnings("unchecked")
public class PigStorage081 extends AbstractSingleLinePigStorage081 {
 

    private byte fieldDel = '\t';
 
    public PigStorage081(String delimiter) {   
        this(delimiter, "",null,null,null,null);

    }
    
    /**
     * Constructs a Pig loader that uses specified regex as a field delimiter.
     * 
     * @param delimiter
     *            the single byte character that is used to separate fields.
     *            ("\t" is the default.)
     */
    public PigStorage081(String delimiter,String headerLine,String typeString,String callBackURL,String uuid,String operatorName) {
        this(delimiter,  headerLine,null,null,typeString,callBackURL, uuid,  operatorName);

   
    }
    //escapeChar='\\'; 	    quoteChar = '"';
    public PigStorage081(String delimiter,String headerLine,String escapeCharIntValue,String quoteCharIntValue,String typeString,
    		String callBackURL,String uuid,String operatorName) {
    	  super(headerLine,typeString,callBackURL, uuid,  operatorName,null) ;
         this. fieldDel = StorageUtil.parseFieldDel(delimiter);
     	char escapeChar = Character.UNASSIGNED;
		char quoteChar = Character.UNASSIGNED;
        if(escapeCharIntValue!=null&&escapeCharIntValue.equals("0")==false){
        	  escapeChar=  ((char)Integer.parseInt(escapeCharIntValue)) ;
        }
        if(quoteCharIntValue!=null&&quoteCharIntValue.equals("0")==false){
        	 quoteChar=   ((char)Integer.parseInt(quoteCharIntValue)) ;
        
        }
    	this.fieldDel = StorageUtil.parseFieldDel(delimiter);

    	super.recordParser = new CSVRecordParser((char)fieldDel, quoteChar, escapeChar);
        
    }
   
 
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PigStorage081)
            return equals((PigStorage081)obj);
        else
            return false;
    }

    public boolean equals(PigStorage081 other) {
        return this.fieldDel == other.fieldDel;
    }
 

    @Override
    public int hashCode() {
        return fieldDel;
    }
 
}
