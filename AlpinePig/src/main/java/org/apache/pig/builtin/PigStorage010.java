/**
 * ClassName PigStorage010.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.pig.ResourceSchema;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigTextOutputFormat;
import org.apache.pig.impl.util.StorageUtil;

import com.alpine.hadoop.ext.CSVRecordParser;

/**
 * 
 * @author john zhao
 *
 */
public class PigStorage010  extends AbstractSingleLinePigStorage010  {
	
//	boolean isHeaderReaded = false;
 
	    
    private byte fieldDel = '\t';

	
    public PigStorage010(String delimiter) {
        this(delimiter, "",null,null,null,null,null,null);
    }

    /**
     * Constructs a Pig loader that uses specified character as a field delimiter.
     *
     * @param delimiter
     *            the single byte character that is used to separate fields.
     *            ("\t" is the default.)
     * @throws ParseException
     */
    public PigStorage010(String delimiter,String headerLine,String typeString,String callBackURL,String uuid,  String operatorName) {
        this(delimiter,  headerLine,null,null,typeString,callBackURL,uuid,    operatorName);
        
    }

	// escapeChar='\\'; quoteChar = '"';
	public PigStorage010(String delimiter, String headerLine,
			String escapeCharIntValue, String quoteCharIntValue,
			String typeString,String callBackURL,String uuid,  String operatorName) {
		super(headerLine, typeString,callBackURL,uuid,  operatorName,null);
		char escapeChar = Character.UNASSIGNED;
		char quoteChar = Character.UNASSIGNED;
		if (escapeCharIntValue != null
				&& escapeCharIntValue.equals("0") == false) {
			escapeChar =  ((char) Integer.parseInt(escapeCharIntValue));
		}
		if (quoteCharIntValue != null && quoteCharIntValue.equals("0") == false) {
			quoteChar =   ((char) Integer.parseInt(quoteCharIntValue));
		}

		this.fieldDel = StorageUtil.parseFieldDel(delimiter);

		super.recordParser = new CSVRecordParser((char)fieldDel, quoteChar, escapeChar);

	}
  

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PigStorage010)
            return equals((PigStorage010)obj);
        else
            return false;
    }

    public boolean equals(PigStorage010 other) {
        return this.fieldDel == other.fieldDel;
    }
 

    @Override
    public OutputFormat getOutputFormat() {
        return new PigTextOutputFormat(fieldDel);
    }
 

    @Override
    public int hashCode() {
        return fieldDel;
    }

 
    @Override
    public void storeSchema(ResourceSchema schema, String location,
            Job job) throws IOException {
        if (isSchemaOn) {
            JsonMetadata metadataWriter = new JsonMetadata();
            byte recordDel = '\n';
            metadataWriter.setFieldDel(fieldDel);
            metadataWriter.setRecordDel(recordDel);
            metadataWriter.storeSchema(schema, location, job);
        }
    }

    
}


 