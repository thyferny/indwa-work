package com.alpine.utility.hadoop.pig;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

public class AlpineTupleField {
    private Byte typeByte;
    private Object fieldValue;
    private int columnNumber;

    public AlpineTupleField(Tuple t, int columnNumber) throws ExecException {
        this.typeByte = t.getType(columnNumber);
        this.columnNumber = columnNumber;
        this.fieldValue = t.get(columnNumber);
    }

    public Byte getTypeByte() {
        return typeByte;
    }

    public void setTypeByte(Byte typeByte) {
        this.typeByte = typeByte;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

}
