package com.alpine.datamining.api.impl.algoconf.hadoop.filestructure;

import java.util.List;

import org.w3c.dom.Element;


public class AnalysisCSVFileStructureModel extends AbstractFileStructureModel{
	
	public static final String TAG_NAME="HadoopFileStructureModel";
	
	public static final String ATTR_DELIMITER = "delimiter";
	
	public static final String ATTR_OTHER = "other";
	
 
	
	public static final String ATTR_INCLUDEHEADER = "includeHeader";

	
	public static final String ATTR_ESCAP_CHAR = "escapChar";
	public static final String ATTR_QUOTE_CHAR = "quoteChar";
	
	public static final String Delimiter_Other = "Other";
	
	public static String[] DELIMITER={"Tab","Comma","Semicolon","Space",Delimiter_Other};
	public static String[] DELIMITER_VALUE={"	",",",";"," ",""};//last "" is no use
	public static final String ESCAP_VALUE="\\";
	public static final String QUOTE_VALUE="\"";
	public static final String DEFAULT_DELIMITER_VALUE=",";
 
	//set default value
	private String escapChar = ESCAP_VALUE;
	private String quoteChar = QUOTE_VALUE;
	
	public AnalysisCSVFileStructureModel(List<String> columnNameList,
			List<String> columnTypeList) {
		super();
		super.setColumnNameList(columnNameList);
		super.setColumnTypeList(columnTypeList);
		setEscapChar(ESCAP_VALUE);
		setDelimiter(DELIMITER[1]);
		setQuoteChar(QUOTE_VALUE);
	}
	
	public AnalysisCSVFileStructureModel() {
		super();
		setEscapChar(ESCAP_VALUE);
		setDelimiter(DELIMITER[1]);
		setQuoteChar(QUOTE_VALUE);
	}

	public String getEscapChar() {
		return escapChar;
	}
	public void setEscapChar(String escapChar) {
		this.escapChar = escapChar;
	}
	public String getQuoteChar() {
		return quoteChar;
	}
	public void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
	}
	
	private String delimiter="Tab";
	 
	private String other;
	 
	public String getDelimiter() {
		return delimiter;
	}
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	public String getIncludeHeader() {
		return getIsFirstLineHeader();
	}
	public void setIncludeHeader(String includeHeader) {
		setIsFirstLineHeader(includeHeader);
	}
	 
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}

	public static AnalysisCSVFileStructureModel fromXMLElement(Element element) {
		
		String delimiter=element.getAttribute(ATTR_DELIMITER);
		String other=element.getAttribute(ATTR_OTHER);
		
		String escChar =element.getAttribute(ATTR_ESCAP_CHAR);
		String quoteChar =element.getAttribute(ATTR_QUOTE_CHAR);
	 	
		AnalysisCSVFileStructureModel model=new AnalysisCSVFileStructureModel();
		
		String includeHeader=element.getAttribute(ATTR_INCLUDEHEADER);
		
		fillColumnElements(element, model);
	 
	
		model.setIsFirstLineHeader(includeHeader);
		
		model.setDelimiter(delimiter);
		model.setOther(other);
		if(escChar!=null){
			model.setEscapChar(escChar) ;
		}
		if(quoteChar!=null){
			model.setQuoteChar(quoteChar) ;
		}
		return model;
	}
	
	@Override
	public AnalysisCSVFileStructureModel clone()  throws CloneNotSupportedException {
		AnalysisCSVFileStructureModel model= new AnalysisCSVFileStructureModel();
		cloneCommonField(model);
		return model;
	}
	protected void cloneCommonField(AnalysisCSVFileStructureModel model)
			throws CloneNotSupportedException {
		super.cloneCommonField(model);
		cloneCSVField(model);
	}
	protected void cloneCSVField(AnalysisCSVFileStructureModel model) {
		model.setOther(getOther());
		model.setQuoteChar(quoteChar);
		model.setEscapChar(escapChar) ;
		model.setDelimiter(delimiter);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisCSVFileStructureModel){
			  return  super.equals((AnalysisFileStructureModel)obj)
				&&getDelimiter().equals(((AnalysisCSVFileStructureModel)obj).getDelimiter())
				&&getOther().equals(((AnalysisCSVFileStructureModel)obj).getOther())
				&&getEscapChar().equals(((AnalysisCSVFileStructureModel)obj).getEscapChar())
				&&getQuoteChar().equals(((AnalysisCSVFileStructureModel)obj).getQuoteChar()
				);
	}else{
		return false;
	}
	}
	

	public String getDelimiterChar() {
		String delChar= "";
		for(int i =0;i<DELIMITER.length;i++){
			if(DELIMITER[i].equals(getDelimiter()) ){
				delChar = DELIMITER_VALUE[i] ;
			}
		}
		return delChar;
		
	}
	

}
