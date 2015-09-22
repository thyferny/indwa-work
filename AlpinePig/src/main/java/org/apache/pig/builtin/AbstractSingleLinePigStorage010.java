/**
 * ClassName AbstractSingleLinePigStorage010.java
 *
 * Version information: 1.00
 *
 * Date: 2012-7-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
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
public abstract class AbstractSingleLinePigStorage010  extends AbstractPigStorage010  {
	private String headerLine = "" ;
	protected Tuple emptyTuple = mTupleFactory.newTuple() ;
	protected RecordParser recordParser;
			
	public AbstractSingleLinePigStorage010(String headerLine, String typeString,String callBackURL, String uuid, String operatorName, RecordParser recordParser){
		super(typeString,callBackURL, uuid,  operatorName);
		this.headerLine = headerLine;	
		this.recordParser=recordParser;
	}
	@Override
	public Tuple getNext() throws IOException {
		mProtoTuple = new ArrayList<Object>();
		if (!mRequiredColumnsInitialized) {
			if (signature != null) {
				Properties p = UDFContext.getUDFContext().getUDFProperties(
						this.getClass());
				mRequiredColumns = (boolean[]) ObjectSerializer.deserialize(p
						.getProperty(signature));
			}
			mRequiredColumnsInitialized = true;
		}
		// Prepend input source path if source tagging is enabled
		if (tagSource) {
			mProtoTuple.add(new DataByteArray(sourcePath.getName()));
		}

		try {
			boolean notDone = in.nextKeyValue();
			if (!notDone) {
				//this block is finished 
				reportBadDataCount();
				return null;
			}

			Text value = (Text) in.getCurrentValue();

			 
			 
				if (value!=null&&(value.toString()  ).equals(headerLine) == true) {
					// skip the first line
					notDone = in.nextKeyValue();
					if (!notDone) {
						//this block is finished 
						reportBadDataCount();
						return null;
					}

					value = (Text) in.getCurrentValue();
				}
				 
		 try{
			value = getNoneEmptyValue(value,in);
			
			Tuple result = readLine(value);
			if(emptyTuple==result){
				return getNext();
			}else{
				return result;
			}
		 }catch (BadDataException e){
			//	String errMsg = e.getMessage()+" while reading input:" +value+"\n Will skip this record!";
			 //not log this , avoid the explode of log file
				//mLog.error(errMsg);
				return getNext();
		 }

		} catch ( Exception e) {
			int errCode = 6018;
			String errMsg = "Error while reading input";
			throw new ExecException(errMsg, errCode,
					PigException.REMOTE_ENVIRONMENT, e);
		}
	}
    
 
	@Override
	protected Text getNoneEmptyValue(Text value, RecordReader in) throws  Exception {
		return  getNoneEmptyValue4SingleLine(value, in, headerLine);
	}

	 
 	protected Tuple readLine(Text value) throws BadDataException,
			IOException {
 
		String[] lineData = null;
		try {
			// System.out.println("value.toString()="+value.toString()) ;

			lineData = recordParser.parseLine(value.toString());

		} catch (Exception e) {
			//throw new BadDataException("can not parse record");
				badDataLineCount = badDataLineCount+1; 
			return emptyTuple;

		}

		if (lineData == null || lineData.length == 0) {
		//	throw new BadDataException("can not parse record");
			if(value.toString()!=null&&value.toString()!=null){
				badDataLineCount = badDataLineCount+1; 
			 }
			return emptyTuple;

		}
		
		// first time ,should be full column
		if (mRequiredColumns == null&&lineData.length != intColumns.length) {
			// System.out.println("mProtoTuple.size()="+mProtoTuple.size()
			// +" intColumns.length"+intColumns.length) ;
//			throw new BadDataException(
//					"Column Number does not match the total number!");
			badDataLineCount = badDataLineCount+1; 

			return emptyTuple;
		}
		// boolean badDataFound= false;

		for (int i = 0; i < lineData.length; i++) {
				if (i >= intColumns.length) {
					badDataLineCount = badDataLineCount+1; 
					throw new BadDataException("Column index too larger.");
				}

				// projection, need some column
				if (mRequiredColumns == null
						|| (mRequiredColumns.length > i && true == mRequiredColumns[i])) {
					if (lineData[i] == null||lineData[i].length()==0 ) {
						//null value
						mProtoTuple.add(null);
		 
					}else if ((true == intColumns[i] && false == isInt(lineData[i]))
							|| (true == longColumns[i] && false == isLong(lineData[i]))
							|| (true == floatColumns[i] && false == isFloat(lineData[i]))
							|| (true == doubleColumns[i] && false == isDouble(lineData[i]))) {
						//bad data
						mProtoTuple.add(null);
//						badDataFound=true;
						addBadDataColumnIndex(i);

					}else{
						// good data
						mProtoTuple.add(new DataByteArray(lineData[i].getBytes("UTF-8")));
					}
				}
				
		

		}
//		 if(true == badDataFound){
//			 badDataLineCount = badDataLineCount+1; 
//		 }

		Tuple t = mTupleFactory.newTupleNoCopy(mProtoTuple);

		return dontLoadSchema ? t : applySchema(t);
	}
}


 