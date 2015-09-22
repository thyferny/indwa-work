package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
public class AnalyzerOutPutPCA extends AbstractAnalyzerOutPut{
	  private static final long serialVersionUID = -1155669583658641361L;
	  private AnalyzerOutPutTableObject PCAQvalueTables;
	  private AnalyzerOutPutTableObject PCAResultTables;

	  public String toString()
	  {
	    return "";
	  }

	  public void setPCAQvalueTables(AnalyzerOutPutTableObject param)
	  {
	    this.PCAQvalueTables = param;
	  }
	  
	  public void setPCAResultTables(AnalyzerOutPutTableObject param)
	  {
	    this.PCAResultTables = param;
	  }

	  public AnalyzerOutPutTableObject getPCAQvalueTables()
	  {
	    return this.PCAQvalueTables;
	  }
	  
	  public AnalyzerOutPutTableObject getPCAResultTables()
	  {
	    return this.PCAResultTables;
	  }
}
