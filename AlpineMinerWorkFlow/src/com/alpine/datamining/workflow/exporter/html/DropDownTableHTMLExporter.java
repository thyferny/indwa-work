package com.alpine.datamining.workflow.exporter.html;

import java.util.List;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DropDownAndTableListVisualizationOutput;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.view.ui.dataset.DropDownTableEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;

public class DropDownTableHTMLExporter extends DataTableHTMLExporter implements VisualOutPutHTMLExporter {

	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList, String rootPath) throws Exception {	
		DropDownAndTableListVisualizationOutput output=(DropDownAndTableListVisualizationOutput)visualizationOutPut;
		DropDownTableEntity ddte = output.getEntity();
		String[] columnNames=ddte.getColumnNames();
		StringBuffer result=new StringBuffer();
		for(int i=0;i<columnNames.length;i++){
			StringBuffer eachResult= new StringBuffer();
			TableEntity te=null;;
			te=ddte.getEntityByColumn(columnNames[i]);
			ToHtmlWriter htmlWriter=new ToHtmlWriter();
			htmlWriter.writeP(columnNames[i]+":");
			exportToHtml(visualizationOutPut, tempFileList, output, eachResult,
					te,htmlWriter,null);
			result.append(htmlWriter.toString());
		}
		return result;
	}

}
