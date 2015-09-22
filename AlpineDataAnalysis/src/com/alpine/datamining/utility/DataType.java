/**
 * ClassName Ontology.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;


/**
 * It provides a single method <tt>boolean isA(int sub, int super)</tt> which
 * does what isA-methods are usually expected to do. Legal parameters are the
 * constants.
 * 
 * @author Eason
 */
public class DataType {

	public static final int VALUE_TYPE = 0;

	public static final int BLOCK_TYPE = 1;

	/**
	 * The parent's index in the array. Root has parent -1.
	 */
	private int parentId[];

	/** Human readable string representations. */
	private String names[];

	public static final int NO_PARENT = -1;

	// -------------------- VALUE TYPE --------------------

	public static final int COLUMN_VALUE = 0;

	public static final int NOMINAL = 1;

	public static final int NUMERICAL = 2;

 	public static final int INTEGER = 3;
//
 	public static final int REAL = 4;
//
 	public static final int STRING = 5;

	public static final int BINOMINAL = 6; // nominal, only +1 and -1

	public static final int POLYNOMINAL = 7;

	public static final int FILE_PATH = 8; // path to a file

    public static final int DATE_TIME = 9;
    
    public static final int DATE = 10;
    
    public static final int TIME = 11;
    
    public static final int BOOLEAN = 12;
    
    public static final int OTHER = 13;

	public static final String[] VALUE_TYPE_NAMES = { 
        "attribute_value", 
        "nominal", 
        "numeric", 
        "integer", 
        "real", 
        "string", 
        "binominal", 
        "polynominal", 
        "file_path",
        "date_time",        
        "date",
        "time",
        "boolean",
        "other"
	};

	/** An ontology for value types (nominal, numerical...) */
	public static final DataType COLUMN_VALUE_TYPE = 
        new DataType(new int[] { 
                NO_PARENT,       // attribute_value (parent type) 0
                COLUMN_VALUE, // nominal
                COLUMN_VALUE, // numeric
                NUMERICAL,       // integer
                NUMERICAL,       // real
                NOMINAL,         // string
                NOMINAL,         // binominal (boolean)
                NOMINAL,         // polynominal
                NOMINAL,         // file_path
                COLUMN_VALUE, // date_time
                COLUMN_VALUE,       // date
                COLUMN_VALUE,        // time
                COLUMN_VALUE,  //BOOLEAN 12
                COLUMN_VALUE
        }, VALUE_TYPE_NAMES);


    
	/** Constructs a new ontology where each of the entries points to its parent. */
	private DataType(int[] parents, String[] names) {
		this.parentId = parents;
		this.names = names;
	}

	/**
	 * @param child
	 * @param parent
	 * @return true if child is a parent. 
	 */
	public boolean is(int child, int parent) {
		while (child != parent) {
			child = parentId[child];
			if (child == -1)
				return false;
		}
		return true;
	}

	/**
	 * Maps the name of a class to its index or -1 if unknown.
	 * @param name
	 * @return
	 */
	public int mapName(String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name))
				return i;
		}
		return -1;
	}

	/**
	 * @param index
	 * @return an name map to its index. 
	 */
	public String mapIndex(int index) {
		if ((index >= 0) && (index < names.length))
			return names[index];
		else
			return null;
	}
}

