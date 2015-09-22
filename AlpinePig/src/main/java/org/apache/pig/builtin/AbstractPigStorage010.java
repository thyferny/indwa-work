/**
 * ClassName AbstractPigStorage010.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.pig.Expression;
import org.apache.pig.FileInputLoadFunc;
import org.apache.pig.LoadCaster;
import org.apache.pig.LoadFunc;
import org.apache.pig.LoadMetadata;
import org.apache.pig.LoadPushDown;
import org.apache.pig.ResourceSchema;
import org.apache.pig.ResourceSchema.ResourceFieldSchema;
import org.apache.pig.ResourceStatistics;
import org.apache.pig.StoreFunc;
import org.apache.pig.StoreFuncInterface;
import org.apache.pig.StoreMetadata;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigTextInputFormat;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigTextOutputFormat;
import org.apache.pig.bzip2r.Bzip2TextInputFormat;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.util.CastUtils;
import org.apache.pig.impl.util.ObjectSerializer;
import org.apache.pig.impl.util.UDFContext;
import org.apache.pig.impl.util.Utils;

/**
 * input format could be xml,csv,json,and log 
 * out put is csv, we use dedault "," " and \ 
 * but for csv, it will overwrite to use his own separator...
 * 
 * @author john zhao
 *
 */
public abstract class AbstractPigStorage010  extends FileInputLoadFunc implements StoreFuncInterface,
LoadPushDown, LoadMetadata, StoreMetadata ,AlpinePigConstants{
	
    protected RecordReader in = null;
    protected RecordWriter writer = null;
    protected final Log mLog = LogFactory.getLog(getClass());
    protected String signature;


   
    protected ArrayList<Object> mProtoTuple = null;
    protected TupleFactory mTupleFactory = TupleFactory.getInstance();
    private String loadLocation;

    boolean isSchemaOn = false;
    boolean dontLoadSchema = false;
    protected ResourceSchema schema;
    protected LoadCaster caster;

    private final Options validOptions = new Options();

    protected boolean[] mRequiredColumns = null;
    protected boolean mRequiredColumnsInitialized = false;
    
    //Indicates whether the input file path should be read.
    protected boolean tagSource = false;
    private static final String TAG_SOURCE_PATH = "tagsource";
    protected Path sourcePath = null;

 

  
	private String[] dataType; 
	//use this way to imporvement the performance
	protected boolean intColumns[];
	protected boolean longColumns[];
	protected boolean floatColumns[];
	protected boolean doubleColumns [];
	
	//this is for bad data
	protected String callBackURL =null;
	protected long badDataLineCount = 0;
	List<Integer>  badDataColumnIndex= new ArrayList<Integer>();
	 
	private String uuid;
	private String operatorName;

	//callBackURL is for the bad data
	public AbstractPigStorage010(String typeString,String callBackURL,String uuid,String operatorName) {
		if(typeString!=null){
    		this.dataType = typeString.split("_") ;
    		this.intColumns = new boolean[dataType.length] ;
    		this.longColumns= new boolean[dataType.length] ;
    		this.floatColumns= new boolean[dataType.length] ;
    		this.doubleColumns = new boolean[dataType.length] ;
    		
    		
    		for (int i = 0; i < dataType.length; i++) {
    			//zy: we just roll back and will do more later
    			//see :https://www.pivotaltracker.com/story/show/39743853
				if(dataType[i].equals(TYPE_INT)){
					this.intColumns[i] = true ;

				}else	if(dataType[i].equals(TYPE_LONG)){
	        		this.longColumns [i] = true ;

				}else	if(dataType[i].equals(TYPE_FLOAT)){
	        		this.floatColumns[i] = true;

				}else	if(dataType[i].equals(TYPE_DOUBLE)){
	        		this.doubleColumns[i] = true;

				}else{
					this.intColumns[i] = false ;
	        		this.longColumns [i] = false ;
	        		this.floatColumns[i] = false;
	        		this.doubleColumns[i] = false;
 				}
			}
    	}else{
    		this.dataType = new String[0];
    	}
		this.callBackURL=callBackURL;
		this.uuid = uuid;
		this.operatorName = operatorName;
	}
	
	protected boolean isNumericColumn(int fieldIndex) {
		return true==intColumns[fieldIndex]||
		true== longColumns[fieldIndex]||
		true==floatColumns[fieldIndex]||
		true==doubleColumns [fieldIndex];
	}
	
	 

	protected Tuple applySchema(Tuple tup) throws IOException {
        if ( caster == null) {
            caster = getLoadCaster();
        }
        if (signature != null && schema == null) {
            Properties p = UDFContext.getUDFContext().getUDFProperties(this.getClass(),
                    new String[] {signature});
            String serializedSchema = p.getProperty(signature+".schema");
            if (serializedSchema == null) return tup;
            try {
                schema = new ResourceSchema(Utils.getSchemaFromString(serializedSchema));
            } catch (Exception e) {
                mLog.error("Unable to parse serialized schema " + serializedSchema, e);
            }
        }

        if (schema != null) {

            ResourceFieldSchema[] fieldSchemas = schema.getFields();
            int tupleIdx = 0;
            // If some fields have been projected out, the tuple
            // only contains required fields.
            // We walk the requiredColumns array to find required fields,
            // and cast those.
            for (int i = 0; i < fieldSchemas.length; i++) {
                if (mRequiredColumns == null || (mRequiredColumns.length>i && mRequiredColumns[i])) {
                    Object val = null;
                    if(tup.get(tupleIdx) != null){
                        byte[] bytes = ((DataByteArray) tup.get(tupleIdx)).get();
                        val = CastUtils.convertToType(caster, bytes,
                                fieldSchemas[i], fieldSchemas[i].getType());
                    }
                    tup.set(tupleIdx, val);
                    tupleIdx++;
                }
            }
        }
        return tup;
    }

     

    @Override
    public RequiredFieldResponse pushProjection(RequiredFieldList requiredFieldList) throws FrontendException {
        if (requiredFieldList == null)
            return null;
        if (requiredFieldList.getFields() != null)
        {
            int lastColumn = -1;
            for (RequiredField rf: requiredFieldList.getFields())
            {
                if (rf.getIndex()>lastColumn)
                {
                    lastColumn = rf.getIndex();
                }
            }
            mRequiredColumns = new boolean[lastColumn+1];
            for (RequiredField rf: requiredFieldList.getFields())
            {
                if (rf.getIndex()!=-1)
                    mRequiredColumns[rf.getIndex()] = true;
            }
            Properties p = UDFContext.getUDFContext().getUDFProperties(this.getClass());
            try {
                p.setProperty(signature, ObjectSerializer.serialize(mRequiredColumns));
            } catch (Exception e) {
                throw new RuntimeException("Cannot serialize mRequiredColumns");
            }
        }
        return new RequiredFieldResponse(true);
    }
 

    @Override
    public InputFormat getInputFormat() {
        if(loadLocation.endsWith(".bz2") || loadLocation.endsWith(".bz")) {
            return new Bzip2TextInputFormat();
        } else {
            return new PigTextInputFormat();
        }
    }

    @Override
    public void prepareToRead(RecordReader reader, PigSplit split) {
        in = reader;
        if(tagSource) {
        	sourcePath = ((FileSplit)split.getWrappedSplit()).getPath();
        }
    }

    @Override
    public void setLocation(String location, Job job)
    throws IOException {
        loadLocation = location;
        FileInputFormat.setInputPaths(job, location);
    }

    @Override
    public OutputFormat getOutputFormat() {
        return new PigTextOutputFormat(DEFAULT_fieldDel);
    }

    @Override
    public void prepareToWrite(RecordWriter writer) {
        this.writer = writer;
    }

    @Override
    public void setStoreLocation(String location, Job job) throws IOException {
        job.getConfiguration().set("mapred.textoutputformat.separator", "");
        FileOutputFormat.setOutputPath(job, new Path(location));

        if( "true".equals( job.getConfiguration().get( "output.compression.enabled" ) ) ) {
            FileOutputFormat.setCompressOutput( job, true );
            String codec = job.getConfiguration().get( "output.compression.codec" );
            try {
                FileOutputFormat.setOutputCompressorClass( job,  (Class<? extends CompressionCodec>) Class.forName( codec ) );
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found: " + codec );
            }
        } else {
            // This makes it so that storing to a directory ending with ".gz" or ".bz2" works.
            setCompression(new Path(location), job);
        }
    }

    private void setCompression(Path path, Job job) {
     	String location=path.getName();
        if (location.endsWith(".bz2") || location.endsWith(".bz")) {
            FileOutputFormat.setCompressOutput(job, true);
            FileOutputFormat.setOutputCompressorClass(job,  BZip2Codec.class);
        }  else if (location.endsWith(".gz")) {
            FileOutputFormat.setCompressOutput(job, true);
            FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
        } else {
            FileOutputFormat.setCompressOutput( job, false);
        }
    }

    @Override
    public void checkSchema(ResourceSchema s) throws IOException {

    }

    @Override
    public String relToAbsPathForStoreLocation(String location, Path curDir)
    throws IOException {
        return LoadFunc.getAbsolutePath(location, curDir);
    }

   


    @Override
    public void setUDFContextSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public List<OperatorSet> getFeatures() {
        return Arrays.asList(LoadPushDown.OperatorSet.PROJECTION);
    }

    @Override
    public void setStoreFuncUDFContextSignature(String signature) {
    }

    @Override
    public void cleanupOnFailure(String location, Job job)
    throws IOException {
        StoreFunc.cleanupOnFailureImpl(location, job);
    }


    //------------------------------------------------------------------------
    // Implementation of LoadMetaData interface

    @Override
    public ResourceSchema getSchema(String location,
            Job job) throws IOException {
        if (!dontLoadSchema) {
            schema = (new JsonMetadata()).getSchema(location, job, isSchemaOn);

            if (signature != null && schema != null) {
                if(tagSource) {
                    schema = Utils.getSchemaWithInputSourceTag(schema);
                }
                Properties p = UDFContext.getUDFContext().getUDFProperties(this.getClass(),
                        new String[] {signature});
                p.setProperty(signature + ".schema", schema.toString());
            }
        }
        return schema;
    }

    @Override
    public ResourceStatistics getStatistics(String location,
            Job job) throws IOException {
        return null;
    }

    @Override
    public void setPartitionFilter(Expression partitionFilter)
    throws IOException {
    }

    @Override
    public String[] getPartitionKeys(String location, Job job)
    throws IOException {
        return null;
    }

    //------------------------------------------------------------------------
    // Implementation of StoreMetadata

    @Override
    public void storeSchema(ResourceSchema schema, String location,
            Job job) throws IOException {
        if (isSchemaOn) {
            JsonMetadata metadataWriter = new JsonMetadata();
            byte recordDel = '\n';
            metadataWriter.setFieldDel(DEFAULT_fieldDel);
            metadataWriter.setRecordDel(recordDel);
            metadataWriter.storeSchema(schema, location, job);
        }
    }

    @Override
    public void storeStatistics(ResourceStatistics stats, String location,
            Job job) throws IOException {

    }
    
	 
    
    @Override
    public void putNext(Tuple f) throws IOException {
        try {
        		writeLineWithEscapAndQuote(writer,f,DEFAULT_fieldDel,DEFAULT_quoteChar,DEFAULT_escapeChar);
 
          
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
	
    protected void writeLineWithEscapAndQuote(RecordWriter writer, Tuple f, byte fieldDel, byte quoteChar, byte escapeChar ) throws IOException, InterruptedException {
    	
		 PigUtil.writeLineWithEscapAndQuote(writer, f, fieldDel, quoteChar, escapeChar);
			
		}    
    
	protected Text getNoneEmptyValue(Text value, RecordReader in) throws  Exception {
		return PigUtil.getNoneEmptyValue(value, in) ;
	}
	//"/main/flow.do?method=getConnSchemaTablesMap&resourceType=Personal"
	protected void reportBadDataCount(   ) { 
		PigUtil.reportBadDataCount( badDataLineCount,   callBackURL,  operatorName,  uuid,getBadColumnIndexString());
		 
	}
	
	private String getBadColumnIndexString() {
		StringBuilder sb = new StringBuilder(  );
		if(badDataColumnIndex!=null&&badDataColumnIndex.size()>0){
            Collections.sort(badDataColumnIndex);
			for (Iterator iterator = badDataColumnIndex.iterator(); iterator.hasNext();) {
				Integer index = (Integer) iterator.next();
				sb.append(String.valueOf((1+index))).append(",");
			}
		}
		return sb.toString();
	}
	
	protected void addBadDataColumnIndex(int fieldIndex) {
		if(badDataColumnIndex.contains(fieldIndex)==false){
			badDataColumnIndex.add(fieldIndex);
		}
		
	}

	protected boolean isDouble(String value) {
		  return PigUtil.isDouble(value);
	}

	protected boolean isFloat(String value) {
		  return PigUtil.isFloat(value);
	}

	protected boolean isLong(String value) {
		return PigUtil.isLong(value);

	}

	protected boolean isInt(String value) {
		return PigUtil.isInt(value);
	}
	
	
	public   Text getNoneEmptyValue4SingleLine(Text value, RecordReader in,String headerLine) throws  Exception {
		if(value!=null&&value.getLength()!=0&&value.toString().trim().length()!=0
				&&(value.toString() + "\n").equals(headerLine) == false){
			return value;
		}
		
		boolean notDone= true;
		while(notDone==true&&(
				value==null||value.getLength()==0||value.toString().trim().length()==0
				||(value.toString() + "\n").equals(headerLine) == true
				)){
			badDataLineCount = badDataLineCount + 1; 

			notDone = in.nextKeyValue();
			value=(Text)in.getCurrentValue();
			if(notDone ==false){
				return null;
			}
		}		
		return value;
	}
}


 