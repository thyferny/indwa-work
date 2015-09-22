/**
 * ClassName AbstractMltipleLinePigStorage010.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.io.Text;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.util.ObjectSerializer;
import org.apache.pig.impl.util.UDFContext;

import com.alpine.hadoop.BadDataException;
import com.alpine.hadoop.ext.RecordParser;

/**
 * 
 * @author john zhao
 *
 */

public abstract class AbstractMultipleLinePigStorage081   extends AbstractPigStorage081 {
 
   protected List<Tuple> bufferResult= new ArrayList<Tuple>(); 	    
	private List<Tuple> emptyList = new  ArrayList<Tuple>(); 	

	private RecordParser recordParser;

    public AbstractMultipleLinePigStorage081(RecordParser recordParser,String typeString,String callBackURL,String uuid, String operatorName) { //xpathListString is separatered with ,
    	super(typeString,callBackURL,uuid,  operatorName) ;
    	this.recordParser = recordParser; 
 
    }

	@Override
	public Tuple getNext() throws IOException {
			if(bufferResult.size()>0){
				return bufferResult.remove(0) ;
			}
		
		if (!mRequiredColumnsInitialized) {
			if (signature != null) {
				Properties p = UDFContext.getUDFContext().getUDFProperties(
						this.getClass());
				mRequiredColumns = (boolean[]) ObjectSerializer.deserialize(p
						.getProperty(signature));
			}
			mRequiredColumnsInitialized = true;
		}

		try {
			boolean notDone = in.nextKeyValue();
			if (!notDone) {
				reportBadDataCount();
				return null;
			}

			Text value = (Text) in.getCurrentValue();
			value = getNoneEmptyValue(value,in);
			 try{
					bufferResult= readLine(value);

				 }catch (BadDataException e){
					//	String errMsg = e.getMessage()+" while reading input:" +value+"\n Will skip this record!";
						//mLog.error(errMsg);
						return getNext();
				 }
			//skip an empty line
			if(emptyList==bufferResult||bufferResult.size()<1){
				return getNext();
			}else{
				return bufferResult.remove(0) ;
			}
		} catch ( Exception e) {
			int errCode = 6018;
			String errMsg = "Error while reading input";
			throw new ExecException(errMsg, errCode,
					PigException.REMOTE_ENVIRONMENT, e);
		}
	}
 

	private List<Tuple> readLine(Text value) throws Exception {
		if (value == null || value.getLength() == 0
				|| value.toString().trim().length() == 0) {
			return emptyList;
		}
		// Prepend input source path if source tagging is enabled

		List<String[]> valuesList = recordParser.parse(value.toString());
		if (valuesList == null) {
			if(value.toString()!=null&&value.toString()!=null){
				badDataLineCount = badDataLineCount+1; 
			 }
			return emptyList;
		}
		List<Tuple> resutl = new ArrayList<Tuple>();

		for (String[] values : valuesList) {

			//skip the empty record
			if (values == null || values.length == 0) {
				badDataLineCount = badDataLineCount+1; 

				continue;
			}
//			 boolean badDataFound= false;

			mProtoTuple = new ArrayList<Object>();
			for (int fieldIndex = 0; fieldIndex < values.length; fieldIndex++) {
				// for the projection, make sure filter the columns pig
				// want!!
				if (mRequiredColumns == null
						|| (mRequiredColumns.length > fieldIndex && mRequiredColumns[fieldIndex])) {
					if (values[fieldIndex] == null
							|| values[fieldIndex].length() == 0) {
						// null value
						mProtoTuple.add(null);

					} else if ((true == intColumns[fieldIndex] && false == isInt(values[fieldIndex]))
							|| (true == longColumns[fieldIndex] && false == isLong(values[fieldIndex]))
							|| (true == floatColumns[fieldIndex] && false == isFloat(values[fieldIndex]))
							|| (true == doubleColumns[fieldIndex] && false == isDouble(values[fieldIndex]))) {
						// bad data
						mProtoTuple.add(null);
						addBadDataColumnIndex(fieldIndex);

//						badDataFound=true;

					} else {
						mProtoTuple.add(new DataByteArray(values[fieldIndex]
								.getBytes("UTF-8")));
					}

				}

			}
//			 if(true == badDataFound){
//				 badDataLineCount = badDataLineCount+1; 
//			 }
			Tuple t = mTupleFactory.newTupleNoCopy(mProtoTuple);
			resutl.add(t);
		}

		return resutl;

	}

}


 