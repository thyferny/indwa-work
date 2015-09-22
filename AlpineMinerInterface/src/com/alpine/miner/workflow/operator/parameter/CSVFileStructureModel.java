package com.alpine.miner.workflow.operator.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;

public class CSVFileStructureModel extends AnalysisCSVFileStructureModel implements FileStructureModel{
	
 
	public static final String TAG_NAME="HadoopFileStructureModel";
	
	public static final String ATTR_DELIMITER = "delimiter";
	
	public static final String ATTR_OTHER = "other";
	
	public static final String ATTR_COLUMNNAME = "columnName";

	public static final String COLUMNNAMES_TAG_NAME = "columnNames";
	
	public static final String ATTR_COLUMNTYPE = "columnType";

	public static final String COLUMNTYPES_TAG_NAME = "columnTypes";

	
	public static final String ATTR_ESCAP_CHAR = "escapChar";
	public static final String ATTR_QUOTE_CHAR = "quoteChar";

	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	
	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element element = xmlDoc.createElement(TAG_NAME);
		
		element.setAttribute(ATTR_DELIMITER, getDelimiter());
		element.setAttribute(ATTR_ESCAP_CHAR, getEscapChar());
		element.setAttribute(ATTR_QUOTE_CHAR, getQuoteChar());
		
		element.setAttribute(ATTR_OTHER, getOther());
		
		FileStructureModelUtility.fillCommonXMLElement(this,xmlDoc, element);
		
		return element;
	}
	
	public static CSVFileStructureModel fromXMLElement(Element element) {
		
		String delimiter=element.getAttribute(ATTR_DELIMITER);
		String other=element.getAttribute(ATTR_OTHER);
		
		String escChar =element.getAttribute(ATTR_ESCAP_CHAR);
		String quoteChar =element.getAttribute(ATTR_QUOTE_CHAR);
	 	
		String includeHeader=element.getAttribute(AnalysisCSVFileStructureModel.ATTR_INCLUDEHEADER);
		
		CSVFileStructureModel model=new CSVFileStructureModel();
		
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
	public void initFromXmlElement(Element element) {
		fromXMLElement(element);
	}	

	
	
	@Override
	public CSVFileStructureModel clone()  throws CloneNotSupportedException {
		CSVFileStructureModel model= new CSVFileStructureModel();
		 super.cloneCommonField(model);
		return model;
	}
}
