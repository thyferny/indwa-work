/**
 * ClassName  DataTableHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DataTextAndTableListVisualizationOutPut;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.file.StringUtil;


/**
 * @author John Zhao
 *
 */
public class TextAndTableOutPutHTMLExporter implements VisualOutPutHTMLExporter {
	public static final int MAX_ROWS = 200;
	//for all table
	public static final int MAX_COLS = 6;
	
	private final static Locale locale=Locale.getDefault();
	/* (non-Javadoc)
	 * @see com.alpine.datamining.exporter.VisualOutPutExporter#export(com.alpine.datamining.api.VisualizationOutPut)
	 */
	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut, List<String> tempFileList,
			String rootPath) throws  Exception {

		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		DataTextAndTableListVisualizationOutPut out=(DataTextAndTableListVisualizationOutPut)visualizationOutPut;
		TextAndTableListEntity tableEntityList = out.getTableEntityList();
		
		if(!StringUtil.isEmpty(tableEntityList.getText())){
			htmlWriter.writeH2(tableEntityList.getText());
		}		
//		result.append("<h2>").append(tableEntityList.getText()).append("</h2>");
		
		for(TableEntity tableEntity:tableEntityList.getTableEntityList()){
		
		String[] columns=tableEntity.getColumn();
		List<String[]> items = tableEntity.getItem();
		
		//show all columns...
		int rowNumbers=items.size();
		int colNumbers=columns.length;
//		StringBuffer t= new StringBuffer();
//		t.append("<table>");
		//can only have header...so need not the row numbers >0
		if(colNumbers>0 ){
			//6 columns split!
			int splitLevel=((colNumbers-1)/MAX_COLS)+1;
			htmlWriter.writeP(createTableOverView(out,colNumbers,rowNumbers));
//			result.append(createTableOverView(out,colNumbers,rowNumbers));
			for(int i=0;i<splitLevel;i++){				
//				t=drawTable(colNumbers,i+1,rowNumbers,tableEntity,tempFileList,visualizationOutPut);
				
				if(splitLevel>1){
					int indexTo=(i+1)*MAX_COLS;
					if(indexTo>colNumbers){
						indexTo=colNumbers;
					}
					htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Columns_From,locale)
							+" "+String.valueOf(i*MAX_COLS+1)+" "+String.valueOf(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Columns_To,locale))
							+" "+String.valueOf(indexTo)+":");
//					result.append("<p>").append(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Columns_From).append(" ").
//						   append(String.valueOf(i*MAX_COLS+1)).append(" ").append(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Columns_To).
//						   append(" ").append(String.valueOf(indexTo)).append(":").append("</p>");
					       
				}
				htmlWriter.writeTable(drawTable(colNumbers,i+1,rowNumbers,tableEntity,tempFileList,visualizationOutPut).toString());
//				if(t!=null){
//					result.append("<table>").append(t).append("</table>");
//				}
				
			}
			
		
		}	
	}
		return htmlWriter.toStringBuffer();
	}
	/**
	 * @param out 
	 * @param colNumbers
	 * @param rowNumbers
	 * @return
	 */
	private String createTableOverView(DataTextAndTableListVisualizationOutPut out, int colNumbers, int rowNumbers) {
		String overView="";
		overView=overView+WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Columns_numbers,locale)+colNumbers+", "
		+ WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Row_numbers,locale)+rowNumbers+"\n";
		
		return overView;
	}
	
	/**
	 * @param colNumbers
	 * @param splitLevel - form 1 ++
	 * @param rowNumbers
	 * @param tableEntity
	 * @param tempFileList
	 * @param visualizationOutPut 
	 * @throws BadElementException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private StringBuffer drawTable(int colNumbers, int splitLevel, int rowNumbers,
			TableEntity tableEntity, List<String> tempFileList, VisualizationOutPut visualizationOutPut) throws  MalformedURLException, IOException {
	
		String[] columns=tableEntity.getColumn();
		List<String[]> items = tableEntity.getItem();
		//not the last!
		int clos=0;
		if(colNumbers-MAX_COLS*(splitLevel-1)>MAX_COLS){
			//6
			clos=MAX_COLS;
		}else{//<6
			  clos=colNumbers-MAX_COLS*(splitLevel-1);
		}
		//avoid 
		if(colNumbers<1||clos<1){
			return null;
		}
		int[] colIndexs  =new int[clos];
			for (int i = 0; i < colIndexs.length; i++) {
			colIndexs[i]=i+MAX_COLS*(splitLevel-1);
			}

//		ToHtmlWriter tableWriter=new ToHtmlWriter();
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
//		StringBuffer  t = new StringBuffer();
		columns=getSplitedColumns(columns,colIndexs);
		
//		t.append("<table>");
		 addColumnHeader(columns,htmlWriter);
		 for (int i = 0; i < rowNumbers; i++) {
			{
				addRow(items.get(i),htmlWriter,colIndexs);
			}
		 }
//		t.append("</table>");
//		tableWriter.writeTable(htmlWriter.toString());
		 return htmlWriter.toStringBuffer();
		
	}
	/**
	 * @param columns
	 * @param cloIndexs
	 * @return
	 */
	private String[] getSplitedColumns(String[] columns, int[] colIndexs) {
		String[] newColumns=new String[colIndexs.length];
		for (int i = 0; i < newColumns.length; i++) {
			newColumns[i]=columns[colIndexs[i]];
		}
 
		return newColumns;
	}
	
	private void addColumnHeader(String[] columns, ToHtmlWriter htmlWriter)  {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<tr>");
		for (int i = 0; i < columns.length; i++) {
//			t.append("<td>").append(columns[i]).append("</td>");
			tdWriter.writeTD(columns[i]);
		}
//		t.append("</tr>");
			htmlWriter.writeTR(tdWriter.toString());
	}
	
	private void addRow(String[] row, ToHtmlWriter htmlWriter, int[] cloIndexs)  {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<tr>");
		for (int i = 0; i < cloIndexs.length; i++) {
			int index=cloIndexs[i];

			 if (row[index] != null)
				 tdWriter.writeTD(row[index]);
//					t.append("<td>").append(row[index]).append("</td>");	
				 else 
//					t.append("<td>").append("").append("</td>");
					 tdWriter.writeTD(""); 
		
		}
//		t.append("</tr>");
		htmlWriter.writeTR(tdWriter.toString());
	}
}
